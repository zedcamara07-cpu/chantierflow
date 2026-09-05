-- À exécuter UNE FOIS dans un projet Supabase neuf.
-- Aucune clé service_role n'est nécessaire dans l'application.

begin;

create table public.organizations (
  id uuid primary key default gen_random_uuid(),
  name text not null check (char_length(trim(name)) between 2 and 100),
  created_by uuid not null references auth.users(id),
  created_at timestamptz not null default now()
);

create table public.organization_members (
  organization_id uuid not null references public.organizations(id),
  user_id uuid not null references auth.users(id),
  role text not null check (role in ('owner','manager','member')),
  created_at timestamptz not null default now(),
  primary key (organization_id, user_id)
);

create index organization_members_user_idx on public.organization_members(user_id);

-- Fonction booléenne : ne renvoie aucune donnée métier.
-- SECURITY DEFINER permet de vérifier l'appartenance sans récursion RLS.
create function public.has_org_role(
  org uuid,
  accepted text[] default array['owner','manager','member']::text[]
) returns boolean
language sql stable security definer
set search_path = public, pg_temp
as $$
  select exists (
    select 1 from public.organization_members m
    where m.organization_id = org
      and m.user_id = auth.uid()
      and m.role = any(accepted)
  );
$$;

revoke all on function public.has_org_role(uuid,text[]) from public;
grant execute on function public.has_org_role(uuid,text[]) to authenticated;

create function public.create_organization(company_name text)
returns uuid
language plpgsql security definer
set search_path = public, pg_temp
as $$
declare
  new_id uuid;
begin
  if auth.uid() is null then
    raise exception 'Authentication required';
  end if;
  if company_name is null or char_length(trim(company_name)) not between 2 and 100 then
    raise exception 'Invalid company name';
  end if;

  -- Sérialise les créations par utilisateur pour appliquer la limite.
  perform pg_advisory_xact_lock(hashtextextended(auth.uid()::text, 0));
  if (select count(*) from public.organizations where created_by = auth.uid()) >= 5 then
    raise exception 'Company creation limit reached';
  end if;

  insert into public.organizations(name, created_by)
    values(trim(company_name), auth.uid()) returning id into new_id;
  insert into public.organization_members(organization_id, user_id, role)
    values(new_id, auth.uid(), 'owner');
  return new_id;
end;
$$;

revoke all on function public.create_organization(text) from public;
grant execute on function public.create_organization(text) to authenticated;

create table public.projects (
  id uuid primary key default gen_random_uuid(),
  organization_id uuid not null references public.organizations(id),
  name text not null check (char_length(trim(name)) between 1 and 250),
  client_name text not null default '' check (char_length(client_name) <= 250),
  address text not null default '' check (char_length(address) <= 250),
  description text not null default '' check (char_length(description) <= 20000),
  status text not null default 'planned'
    check (status in ('planned','active','paused','completed','archived')),
  start_date date,
  end_date date,
  created_by uuid not null references auth.users(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (organization_id, id),
  check (start_date is null or end_date is null or end_date >= start_date)
);

create table public.tasks (
  id uuid primary key default gen_random_uuid(),
  organization_id uuid not null references public.organizations(id),
  project_id uuid not null,
  title text not null check (char_length(trim(title)) between 1 and 250),
  description text not null default '' check (char_length(description) <= 20000),
  status text not null default 'todo' check (status in ('todo','doing','blocked','done')),
  priority text not null default 'normal' check (priority in ('low','normal','high')),
  due_date date,
  created_by uuid not null references auth.users(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  foreign key (organization_id,project_id) references public.projects(organization_id,id)
);

create table public.reports (
  id uuid primary key default gen_random_uuid(),
  organization_id uuid not null references public.organizations(id),
  project_id uuid not null,
  title text not null check (char_length(trim(title)) between 1 and 250),
  content text not null check (char_length(trim(content)) between 1 and 20000),
  report_date date not null default current_date,
  status text not null default 'draft' check (status in ('draft','submitted','validated')),
  created_by uuid not null references auth.users(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  foreign key (organization_id,project_id) references public.projects(organization_id,id)
);

create table public.expenses (
  id uuid primary key default gen_random_uuid(),
  organization_id uuid not null references public.organizations(id),
  project_id uuid not null,
  title text not null check (char_length(trim(title)) between 1 and 250),
  supplier text not null default '' check (char_length(supplier) <= 250),
  category text not null default 'Matériaux'
    check (category in ('Matériaux','Main-d’œuvre','Location','Transport','Autre')),
  amount_cents bigint not null check (amount_cents > 0 and amount_cents <= 9999999999),
  expense_date date not null default current_date,
  status text not null default 'pending' check (status in ('pending','paid')),
  created_by uuid not null references auth.users(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  foreign key (organization_id,project_id) references public.projects(organization_id,id)
);

create table public.photos (
  id uuid primary key default gen_random_uuid(),
  organization_id uuid not null references public.organizations(id),
  project_id uuid not null,
  storage_path text not null unique,
  caption text not null default '' check (char_length(caption) <= 250),
  created_by uuid not null references auth.users(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  foreign key (organization_id,project_id) references public.projects(organization_id,id),
  check (split_part(storage_path,'/',1) = organization_id::text)
);

-- Empêche de déplacer une ligne vers une autre entreprise,
-- de falsifier l'auteur ou les dates de création.
create function public.guard_business_update()
returns trigger language plpgsql
set search_path = public, pg_temp
as $$
begin
  if new.id is distinct from old.id
    or new.organization_id is distinct from old.organization_id
    or new.created_by is distinct from old.created_by
    or new.created_at is distinct from old.created_at then
    raise exception 'Immutable fields cannot be changed';
  end if;
  new.updated_at := now();
  return new;
end;
$$;

-- Les rapports validés sont immuables, même pour un responsable.
create function public.guard_report()
returns trigger language plpgsql
set search_path = public, pg_temp
as $$
begin
  if tg_op = 'INSERT' then
    if new.status <> 'draft' then raise exception 'Reports must start as drafts'; end if;
    return new;
  end if;

  if old.status = 'validated' then
    raise exception 'Validated reports are immutable';
  end if;

  if tg_op = 'DELETE' then return old; end if;

  if new.status = 'validated' then
    if old.status <> 'submitted' then raise exception 'Submit the report before validation'; end if;
    if not public.has_org_role(old.organization_id, array['owner','manager']) then
      raise exception 'Only managers can validate reports';
    end if;
  end if;

  return new;
end;
$$;

create trigger reports_guard
before insert or update or delete on public.reports
for each row execute function public.guard_report();

alter table public.organizations enable row level security;
alter table public.organization_members enable row level security;

create policy organizations_read on public.organizations
for select to authenticated using (public.has_org_role(id));

create policy memberships_read on public.organization_members
for select to authenticated using (public.has_org_role(organization_id));

-- Pas d'écriture directe des membres depuis le navigateur dans cette version.
-- L'ajout manuel éventuel est réservé à l'administrateur du backend.

do $$
declare
  t text;
begin
  foreach t in array array['projects','tasks','reports','expenses','photos'] loop
    execute format('alter table public.%I enable row level security', t);
    execute format('create index %I on public.%I(organization_id, created_at desc)', t || '_org_created_idx', t);
    if t <> 'projects' then
      execute format('create index %I on public.%I(organization_id, project_id)', t || '_org_project_idx', t);
    end if;
    execute format(
      'create trigger business_update_guard before update on public.%I
       for each row execute function public.guard_business_update()', t
    );

    if t = 'expenses' then
      execute format(
        'create policy read_rows on public.%I for select to authenticated
         using (public.has_org_role(organization_id, array[''owner'',''manager'']))', t
      );
    else
      execute format(
        'create policy read_rows on public.%I for select to authenticated
         using (public.has_org_role(organization_id))', t
      );
    end if;

    if t in ('projects','expenses') then
      execute format(
        'create policy insert_rows on public.%I for insert to authenticated
         with check (created_by = auth.uid()
         and public.has_org_role(organization_id, array[''owner'',''manager'']))', t
      );
      execute format(
        'create policy update_rows on public.%I for update to authenticated
         using (public.has_org_role(organization_id, array[''owner'',''manager'']))
         with check (public.has_org_role(organization_id, array[''owner'',''manager'']))', t
      );
    else
      execute format(
        'create policy insert_rows on public.%I for insert to authenticated
         with check (created_by = auth.uid() and public.has_org_role(organization_id))', t
      );
      execute format(
        'create policy update_rows on public.%I for update to authenticated
         using (public.has_org_role(organization_id))
         with check (public.has_org_role(organization_id))', t
      );
    end if;

    execute format(
      'create policy delete_rows on public.%I for delete to authenticated
       using (public.has_org_role(organization_id, array[''owner'',''manager'']))', t
    );

    execute format('revoke all on public.%I from anon', t);
    execute format('grant select, insert, update, delete on public.%I to authenticated', t);
  end loop;
end;
$$;

revoke all on public.organizations, public.organization_members from anon;
revoke all on public.organizations, public.organization_members from authenticated;
grant select on public.organizations, public.organization_members to authenticated;

-- Bucket privé : les accès publics sont désactivés.
insert into storage.buckets(id, name, public, file_size_limit, allowed_mime_types)
values (
  'site-photos', 'site-photos', false, 10485760,
  array['image/jpeg','image/png','image/webp']
);

create policy site_photos_read on storage.objects
for select to authenticated
using (
  bucket_id = 'site-photos'
  and exists (
    select 1 from public.organization_members m
    where m.user_id = auth.uid()
      and m.organization_id::text = (storage.foldername(name))[1]
  )
);

create policy site_photos_upload on storage.objects
for insert to authenticated
with check (
  bucket_id = 'site-photos'
  and exists (
    select 1 from public.organization_members m
    where m.user_id = auth.uid()
      and m.organization_id::text = (storage.foldername(name))[1]
  )
);

-- Le propriétaire de l'upload peut nettoyer son propre fichier
-- si l'enregistrement métier échoue.
create policy site_photos_delete on storage.objects
for delete to authenticated
using (
  bucket_id = 'site-photos'
  and exists (
    select 1 from public.organization_members m
    where m.user_id = auth.uid()
      and m.organization_id::text = (storage.foldername(name))[1]
      and (m.role in ('owner','manager') or owner_id = auth.uid()::text)
  )
);

commit;
