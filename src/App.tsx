import {
  useCallback, useEffect, useMemo, useRef, useState,
  type FormEvent, type ReactNode
} from "react";
import type { Session } from "@supabase/supabase-js";
import {
  Activity, ArrowUpRight, Building2, Camera, Check, CheckSquare,
  ClipboardList, CreditCard, FolderKanban, LayoutDashboard,
  LogOut, MapPin, Pencil, Plus, RefreshCw, Search, Trash2, WifiOff, X
} from "lucide-react";
import { configured, db, errorMessage } from "./lib/backend";
import { dateLabel, localDay, money, progress, toCents } from "./lib/domain";

type Row = Record<string, any>;
type Page = "dashboard" | "projects" | "tasks" | "reports" | "expenses" | "photos";
type Table = "projects" | "tasks" | "reports" | "expenses";

const labels: Record<string, string> = {
  planned: "À préparer", active: "En cours", paused: "En pause",
  completed: "Terminé", archived: "Archivé",
  todo: "À faire", doing: "En cours", blocked: "Bloquée", done: "Terminée",
  low: "Basse", normal: "Normale", high: "Haute",
  draft: "Brouillon", submitted: "Soumis", validated: "Validé",
  pending: "À payer", paid: "Payée",
  owner: "Propriétaire", manager: "Responsable", member: "Collaborateur"
};

const sections = [
  { id: "dashboard", label: "Accueil", icon: LayoutDashboard },
  { id: "projects", label: "Chantiers", icon: FolderKanban },
  { id: "tasks", label: "Tâches", icon: CheckSquare },
  { id: "reports", label: "Rapports", icon: ClipboardList },
  { id: "expenses", label: "Dépenses", icon: CreditCard },
  { id: "photos", label: "Photos", icon: Camera }
] as const;

const singular: Record<Table, string> = {
  projects: "chantier", tasks: "tâche", reports: "rapport", expenses: "dépense"
};

function Badge({ value }: { value: string }) {
  return <span className={`badge badge-${value}`}>{labels[value] || value}</span>;
}

function Empty({ title, text, action }: {
  title: string; text: string; action?: ReactNode
}) {
  return <div className="empty">
    <Building2 size={36} />
    <h3>{title}</h3><p>{text}</p>{action}
  </div>;
}

function Dialog({ title, children, onClose }: {
  title: string; children: ReactNode; onClose: () => void
}) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const previous = document.activeElement as HTMLElement | null;
    const dialog = ref.current;
    dialog?.showModal();
    return () => {
      dialog?.close();
      previous?.focus();
    };
  }, []);
  return <dialog ref={ref} className="dialog"
    aria-labelledby="dialog-title"
    onCancel={e => { e.preventDefault(); onClose(); }}>
    <header className="dialog-header">
      <h2 id="dialog-title">{title}</h2>
      <button type="button" className="icon-button" onClick={onClose} aria-label="Fermer">
        <X size={22} />
      </button>
    </header>
    {children}
  </dialog>;
}

function Auth({ recovery, onRecovered }: {
  recovery: boolean; onRecovered: () => void
}) {
  const [mode, setMode] = useState<"login" | "signup" | "forgot">("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true); setError(""); setMessage("");
    try {
      if (recovery) {
        const result = await db!.auth.updateUser({ password });
        if (result.error) throw result.error;
        onRecovered();
      } else if (mode === "login") {
        const result = await db!.auth.signInWithPassword({ email, password });
        if (result.error) throw result.error;
      } else if (mode === "signup") {
        const result = await db!.auth.signUp({ email, password });
        if (result.error) throw result.error;
        setMessage("Compte créé. Si la confirmation est activée, consultez votre boîte e-mail.");
      } else {
        const result = await db!.auth.resetPasswordForEmail(email);
        if (result.error) throw result.error;
        setMessage("Si ce compte existe, un e-mail de récupération vous sera envoyé.");
      }
    } catch (err) {
      setError(errorMessage(err));
    } finally { setBusy(false); }
  }

  return <main className="auth-layout">
    <section className="auth-story">
      <div className="brand brand-light"><Building2 /> ChantierFlow<span>•</span></div>
      <div>
        <div className="eyebrow light">LE TERRAIN. L’ÉQUIPE. L’ESSENTIEL.</div>
        <h1>Vos chantiers,<br />en toute clarté.</h1>
        <p>Moins de dispersion. Plus de visibilité sur ce qui fait avancer vos projets.</p>
        <div className="story-card"><CheckSquare /><div>
          <strong>Un quotidien mieux organisé</strong>
          <span>Chantiers, tâches, rapports et photos au même endroit.</span>
        </div></div>
      </div>
      <small>Conçu pour travailler ensemble, du bureau au terrain.</small>
    </section>
    <section className="auth-form-wrap">
      <form className="auth-form" onSubmit={submit}>
        <span className="eyebrow">BIENVENUE SUR CHANTIERFLOW</span>
        <h2>{recovery ? "Nouveau mot de passe" : mode === "signup" ? "Créer mon compte" :
          mode === "forgot" ? "Retrouver mon accès" : "Heureux de vous revoir"}</h2>
        <p className="muted">Votre espace de travail, simplement.</p>
        {!recovery && <label>Adresse e-mail
          <input required type="email" autoComplete="email" value={email}
            onChange={e => setEmail(e.target.value)} placeholder="vous@entreprise.fr" />
        </label>}
        {(recovery || mode !== "forgot") && <label>Mot de passe
          <input required minLength={8} type="password" value={password}
            autoComplete={mode === "login" && !recovery ? "current-password" : "new-password"}
            onChange={e => setPassword(e.target.value)} placeholder="8 caractères minimum" />
        </label>}
        {error && <div className="alert error" role="alert">{error}</div>}
        {message && <div className="alert success" role="status">{message}</div>}
        <button className="button primary wide" disabled={busy}>
          {busy ? "Veuillez patienter…" : recovery ? "Enregistrer" :
            mode === "login" ? "Se connecter" : mode === "signup" ? "Créer mon compte" : "Recevoir le lien"}
          <ArrowUpRight size={18} />
        </button>
        {!recovery && <div className="auth-links">
          <button type="button" className="text-button" onClick={() => {
            setMode(mode === "signup" ? "login" : "signup"); setError(""); setMessage("");
          }}>{mode === "signup" ? "J’ai déjà un compte" : "Créer un compte"}</button>
          <button type="button" className="text-button" onClick={() => {
            setMode(mode === "forgot" ? "login" : "forgot"); setError(""); setMessage("");
          }}>{mode === "forgot" ? "Retour à la connexion" : "Mot de passe oublié ?"}</button>
        </div>}
      </form>
    </section>
  </main>;
}

function OrganizationSetup({ onCreated, onLogout }: {
  onCreated: () => Promise<void>; onLogout: () => Promise<void>
}) {
  const [name, setName] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function submit(e: FormEvent) {
    e.preventDefault(); setBusy(true); setError("");
    try {
      const { error } = await db!.rpc("create_organization", { company_name: name.trim() });
      if (error) throw error;
      await onCreated();
    } catch (err) { setError(errorMessage(err)); }
    finally { setBusy(false); }
  }
  return <main className="setup-page"><form className="panel setup-card" onSubmit={submit}>
    <div className="brand"><Building2 /> ChantierFlow</div>
    <h1>Créons votre espace.</h1>
    <p className="muted">Les données de votre entreprise seront séparées de celles des autres entreprises.</p>
    <label>Nom de l’entreprise
      <input required minLength={2} maxLength={100} value={name}
        onChange={e => setName(e.target.value)} placeholder="Bâtir Horizon" />
    </label>
    {error && <div className="alert error" role="alert">{error}</div>}
    <button className="button primary wide" disabled={busy}>
      {busy ? "Création…" : "Créer mon entreprise"}<ArrowUpRight size={18} />
    </button>
    <button type="button" className="text-button" onClick={() => void onLogout()}>Se déconnecter</button>
  </form></main>;
}

function Editor({ table, row, projects, organizationId, userId, onClose, onSaved }: {
  table: Table; row: Row | null; projects: Row[];
  organizationId: string; userId: string;
  onClose: () => void; onSaved: () => Promise<void>
}) {
  const [form, setForm] = useState<Row>(() => ({
    name: "", client_name: "", address: "", description: "",
    title: "", content: "", supplier: "", amount: "", category: "Matériaux",
    project_id: projects.find(p => p.status !== "archived")?.id || "",
    start_date: "", end_date: "", due_date: "",
    report_date: localDay(), expense_date: localDay(), priority: "normal",
    status: table === "projects" ? "planned" : table === "tasks" ? "todo" :
      table === "reports" ? "draft" : "pending",
    ...row,
    ...(row && table === "expenses" ? { amount: (row.amount_cents / 100).toFixed(2) } : {})
  }));
  const [busy, setBusy] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [error, setError] = useState("");

  const change = (key: string, value: string) => {
    setDirty(true); setForm(current => ({ ...current, [key]: value }));
  };
  const close = () => {
    if (!busy && (!dirty || window.confirm("Fermer et abandonner les modifications ?"))) onClose();
  };

  function input(key: string, label: string, type = "text", required = false) {
    return <label key={key}>{label}
      <input type={type} required={required} value={form[key] ?? ""}
        maxLength={type === "text" ? 250 : undefined}
        onChange={e => change(key, e.target.value)} />
    </label>;
  }
  function select(key: string, label: string, values: string[]) {
    return <label>{label}<select value={form[key]} onChange={e => change(key, e.target.value)}>
      {values.map(value => <option key={value} value={value}>{labels[value] || value}</option>)}
    </select></label>;
  }

  async function save(e: FormEvent) {
    e.preventDefault(); setError(""); setBusy(true);
    try {
      let payload: Row = {};
      if (table === "projects") {
        if (form.start_date && form.end_date && form.end_date < form.start_date) {
          throw new Error("La date de fin doit être postérieure à la date de début.");
        }
        payload = {
          name: form.name.trim(), client_name: form.client_name.trim(),
          address: form.address.trim(), description: form.description.trim(),
          status: form.status, start_date: form.start_date || null, end_date: form.end_date || null
        };
      } else if (table === "tasks") {
        payload = {
          project_id: form.project_id, title: form.title.trim(),
          description: form.description.trim(), status: form.status,
          priority: form.priority, due_date: form.due_date || null
        };
      } else if (table === "reports") {
        payload = {
          project_id: form.project_id, title: form.title.trim(),
          content: form.content.trim(), report_date: form.report_date,
          status: row?.status || "draft"
        };
      } else {
        payload = {
          project_id: form.project_id, title: form.title.trim(),
          supplier: form.supplier.trim(), category: form.category,
          amount_cents: toCents(form.amount), expense_date: form.expense_date,
          status: form.status
        };
      }
      const query = row
        ? db!.from(table).update(payload).eq("id", row.id).eq("organization_id", organizationId)
        : db!.from(table).insert({ ...payload, organization_id: organizationId, created_by: userId });
      const result = await query.select("id").single();
      if (result.error) throw result.error;
      await onSaved(); onClose();
    } catch (err) { setError(errorMessage(err)); }
    finally { setBusy(false); }
  }

  return <Dialog title={`${row ? "Modifier" : "Créer"} ${table === "tasks" || table === "expenses" ? "une" : "un"} ${singular[table]}`} onClose={close}>
    <form onSubmit={save} className="editor">
      <fieldset disabled={busy}>
        {table !== "projects" && <label>Chantier
          <select required value={form.project_id} onChange={e => change("project_id", e.target.value)}>
            <option value="">Sélectionner un chantier</option>
            {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        </label>}
        {table === "projects" ? <>
          {input("name", "Nom du chantier", "text", true)}
          {input("client_name", "Client")}
          {input("address", "Adresse")}
          <div className="form-grid">{input("start_date", "Début prévu", "date")}{input("end_date", "Fin prévue", "date")}</div>
          {select("status", "Statut", ["planned", "active", "paused", "completed", "archived"])}
        </> : input("title", "Titre", "text", true)}
        {table === "tasks" && <>
          <div className="form-grid">
            {select("status", "Statut", ["todo", "doing", "blocked", "done"])}
            {select("priority", "Priorité", ["low", "normal", "high"])}
          </div>
          {input("due_date", "Échéance", "date")}
        </>}
        {table === "reports" && input("report_date", "Date du rapport", "date", true)}
        {table === "expenses" ? <>
          {input("supplier", "Fournisseur")}
          <div className="form-grid">
            <label>Montant TTC (€)<input required inputMode="decimal" value={form.amount}
              placeholder="1250,00" onChange={e => change("amount", e.target.value)} /></label>
            {input("expense_date", "Date", "date", true)}
          </div>
          {select("category", "Catégorie", ["Matériaux", "Main-d’œuvre", "Location", "Transport", "Autre"])}
          {select("status", "Paiement", ["pending", "paid"])}
        </> : <label>{table === "reports" ? "Travaux réalisés, difficultés et suites à prévoir" : "Description"}
          <textarea rows={5} required={table === "reports"} maxLength={20000}
            value={form[table === "reports" ? "content" : "description"]}
            onChange={e => change(table === "reports" ? "content" : "description", e.target.value)} />
        </label>}
      </fieldset>
      {error && <div className="alert error" role="alert">{error}</div>}
      <footer className="form-actions">
        <button type="button" className="button secondary" disabled={busy} onClick={close}>Annuler</button>
        <button className="button primary" disabled={busy}>{busy ? "Enregistrement…" : "Enregistrer"}<Check size={18} /></button>
      </footer>
    </form>
  </Dialog>;
}

function PhotoUpload({ projects, organizationId, userId, onClose, onSaved }: {
  projects: Row[]; organizationId: string; userId: string;
  onClose: () => void; onSaved: () => Promise<void>
}) {
  const [projectId, setProjectId] = useState(projects[0]?.id || "");
  const [caption, setCaption] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function upload(e: FormEvent) {
    e.preventDefault(); if (!file) return;
    setError(""); setBusy(true);
    let uploadedPath: string | null = null;
    try {
      if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
        throw new Error("Formats acceptés : JPG, PNG et WebP.");
      }
      if (file.size > 10 * 1024 * 1024) throw new Error("La photo ne doit pas dépasser 10 Mo.");
      const bitmap = await createImageBitmap(file);
      const scale = Math.min(1, 1920 / Math.max(bitmap.width, bitmap.height));
      const canvas = document.createElement("canvas");
      canvas.width = Math.max(1, Math.round(bitmap.width * scale));
      canvas.height = Math.max(1, Math.round(bitmap.height * scale));
      const context = canvas.getContext("2d");
      if (!context) throw new Error("Le traitement d’image n’est pas disponible sur cet appareil.");
      context.fillStyle = "#fff";
      context.fillRect(0, 0, canvas.width, canvas.height);
      context.drawImage(bitmap, 0, 0, canvas.width, canvas.height);
      bitmap.close();
      const blob = await new Promise<Blob>((resolve, reject) =>
        canvas.toBlob(b => b ? resolve(b) : reject(new Error("Compression impossible.")), "image/jpeg", 0.82)
      );
      const path = `${organizationId}/${crypto.randomUUID()}.jpg`;
      const storage = await db!.storage.from("site-photos").upload(path, blob, {
        contentType: "image/jpeg", upsert: false
      });
      if (storage.error) throw storage.error;
      uploadedPath = path;
      const record = await db!.from("photos").insert({
        organization_id: organizationId, project_id: projectId,
        storage_path: path, caption: caption.trim(), created_by: userId
      }).select("id").single();
      if (record.error) throw record.error;
      uploadedPath = null;
      await onSaved(); onClose();
    } catch (err) {
      if (uploadedPath) {
        const cleanup = await db!.storage.from("site-photos").remove([uploadedPath]);
        if (cleanup.error) console.warn("Nettoyage de fichier nécessaire", uploadedPath);
      }
      setError(errorMessage(err));
    } finally { setBusy(false); }
  }

  return <Dialog title="Ajouter une photo" onClose={() => { if (!busy) onClose(); }}>
    <form className="editor" onSubmit={upload}>
      <fieldset disabled={busy}>
        <label>Chantier<select required value={projectId} onChange={e => setProjectId(e.target.value)}>
          <option value="">Sélectionner</option>
          {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
        </select></label>
        <label>Photo — 10 Mo maximum
          <input required type="file" accept="image/jpeg,image/png,image/webp"
            onChange={e => setFile(e.target.files?.[0] || null)} />
        </label>
        <label>Légende<input value={caption} maxLength={250} onChange={e => setCaption(e.target.value)} /></label>
      </fieldset>
      <p className="muted small">La photo est redimensionnée et réencodée avant envoi. Elle reste privée à votre entreprise.</p>
      {error && <div role="alert" className="alert error">{error}</div>}
      <button className="button primary wide" disabled={busy || !file || !projectId}>
        <Camera size={18} />{busy ? "Envoi en cours…" : "Enregistrer la photo"}
      </button>
    </form>
  </Dialog>;
}

function Workspace({ session, organizations, onLogout }: {
  session: Session; organizations: Row[]; onLogout: () => Promise<void>
}) {
  const [organizationId, setOrganizationId] = useState(organizations[0].id);
  const [role, setRole] = useState("");
  const [page, setPage] = useState<Page>("dashboard");
  const [data, setData] = useState<Record<string, Row[]>>({
    projects: [], tasks: [], reports: [], expenses: [], photos: []
  });
  const [query, setQuery] = useState("");
  const [projectFilter, setProjectFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [editor, setEditor] = useState<{ table: Table; row: Row | null } | null>(null);
  const [uploading, setUploading] = useState(false);
  const [online, setOnline] = useState(navigator.onLine);
  const loadId = useRef(0);
  const canManage = role === "owner" || role === "manager";

  useEffect(() => {
    const update = () => setOnline(navigator.onLine);
    window.addEventListener("online", update); window.addEventListener("offline", update);
    return () => {
      window.removeEventListener("online", update); window.removeEventListener("offline", update);
    };
  }, []);

  const load = useCallback(async () => {
    const ticket = ++loadId.current;
    setLoading(true); setError(""); setRole("");
    setData({ projects: [], tasks: [], reports: [], expenses: [], photos: [] });
    try {
      const membership = await db!.from("organization_members").select("role")
        .eq("organization_id", organizationId).eq("user_id", session.user.id).single();
      if (membership.error) throw membership.error;
      const nextRole = membership.data.role;
      const tables = ["projects", "tasks", "reports", "photos"];
      if (["owner", "manager"].includes(nextRole)) tables.push("expenses");
      const results = await Promise.all(tables.map(async table => {
        const result = await db!.from(table).select("*")
          .eq("organization_id", organizationId).order("created_at", { ascending: false }).limit(500);
        if (result.error) throw result.error;
        return [table, result.data || []] as const;
      }));
      const next = { projects: [], tasks: [], reports: [], expenses: [], photos: [], ...Object.fromEntries(results) };
      if (next.photos.length) {
        const signed = await db!.storage.from("site-photos")
          .createSignedUrls(next.photos.map((p: Row) => p.storage_path), 3600);
        if (signed.error) throw signed.error;
        next.photos = next.photos.map((p: Row) => ({
          ...p, url: signed.data?.find(item => item.path === p.storage_path)?.signedUrl
        }));
      }
      if (ticket === loadId.current) { setRole(nextRole); setData(next); }
    } catch (err) {
      if (ticket === loadId.current) setError(errorMessage(err));
    } finally {
      if (ticket === loadId.current) setLoading(false);
    }
  }, [organizationId, session.user.id]);

  useEffect(() => { void load(); }, [load]);
  useEffect(() => {
    if (!notice) return;
    const timer = setTimeout(() => setNotice(""), 4500);
    return () => clearTimeout(timer);
  }, [notice]);

  const projectName = (id: string) => data.projects.find(p => p.id === id)?.name || "Chantier";
  const organization = organizations.find(o => o.id === organizationId);
  const visibleSections = sections.filter(s => s.id !== "expenses" || canManage);
  const today = localDay();
  const overdue = data.tasks.filter(t => t.due_date && t.due_date < today && t.status !== "done");
  const monthExpenses = data.expenses.filter(e => e.expense_date.startsWith(today.slice(0, 7)));
  const rows = useMemo(() => (data[page] || []).filter(row => {
    const text = [row.name, row.title, row.caption, row.client_name, row.address, row.supplier]
      .filter(Boolean).join(" ").toLocaleLowerCase("fr");
    return text.includes(query.toLocaleLowerCase("fr"))
      && (!projectFilter || row.project_id === projectFilter || row.id === projectFilter)
      && (!statusFilter || row.status === statusFilter);
  }), [data, page, query, projectFilter, statusFilter]);

  function navigate(next: Page, filter = "", status = "") {
    setPage(next); setQuery(""); setProjectFilter(filter); setStatusFilter(status);
  }
  async function saved() {
    await load(); setNotice("Enregistrement effectué.");
  }
  async function action(work: () => Promise<void>) {
    if (busy) return;
    setBusy(true); setError("");
    try { await work(); await load(); setNotice("Modification enregistrée."); }
    catch (err) { setError(errorMessage(err)); }
    finally { setBusy(false); }
  }
  function remove(table: string, row: Row) {
    if (!window.confirm("Supprimer définitivement cet élément ? Cette action est irréversible.")) return;
    void action(async () => {
      if (table === "photos") {
        const storage = await db!.storage.from("site-photos").remove([row.storage_path]);
        if (storage.error) throw storage.error;
      }
      const result = await db!.from(table).delete()
        .eq("id", row.id).eq("organization_id", organizationId).select("id").single();
      if (result.error) throw result.error;
    });
  }
  function setStatus(table: Table, row: Row, status: string) {
    void action(async () => {
      const result = await db!.from(table).update({ status })
        .eq("id", row.id).eq("organization_id", organizationId).select("id").single();
      if (result.error) throw result.error;
    });
  }
  const editButton = (table: Table, row: Row) => <button className="icon-button"
    disabled={busy} aria-label={`Modifier ${row.name || row.title}`}
    onClick={() => setEditor({ table, row })}><Pencil size={17} /></button>;
  const deleteButton = (table: string, row: Row) => canManage &&
    <button className="icon-button danger" disabled={busy}
      aria-label={`Supprimer ${row.name || row.title || "la photo"}`}
      onClick={() => remove(table, row)}><Trash2 size={17} /></button>;

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><Building2 size={27} />ChantierFlow<span>•</span></div>
      <div className="workspace-label">ESPACE DE TRAVAIL</div>
      <label className="sr-only" htmlFor="organization">Entreprise active</label>
      <select id="organization" className="organization-select" value={organizationId}
        onChange={e => {
          setRole(""); setEditor(null); setUploading(false);
          setOrganizationId(e.target.value); navigate("dashboard");
        }}>
        {organizations.map(o => <option key={o.id} value={o.id}>{o.name}</option>)}
      </select>
      <nav aria-label="Navigation principale">
        {visibleSections.map(s => <button key={s.id}
          aria-current={page === s.id ? "page" : undefined}
          className={`nav-item ${page === s.id ? "selected" : ""}`}
          onClick={() => navigate(s.id)}><s.icon size={21} /><span>{s.label}</span>
          {s.id === "tasks" && data.tasks.some(t => t.status !== "done") &&
            <span className="nav-count">{data.tasks.filter(t => t.status !== "done").length}</span>}
        </button>)}
      </nav>
      <div className="sidebar-bottom">
        <div className="workspace-note"><Activity size={20} /><p>Une vision claire.<br /><strong>Des projets qui avancent.</strong></p></div>
        <div className="profile"><span className="avatar">{session.user.email?.slice(0, 2).toUpperCase()}</span>
          <div><strong>{session.user.email?.split("@")[0]}</strong><small>{labels[role] || "Chargement…"}</small></div>
          <button className="icon-button" aria-label="Se déconnecter" onClick={() => void onLogout()}><LogOut size={19} /></button>
        </div>
      </div>
    </aside>

    <div className="main-shell">
      <header className="topbar">
        <div className="breadcrumb">Espace entreprise <span>/</span> <strong>{organization?.name}</strong></div>
        <div className="topbar-actions"><span className="today">{dateLabel(today)}</span>
          <button className="icon-button" disabled={loading || busy} onClick={() => void load()} aria-label="Actualiser">
            <RefreshCw size={19} className={loading ? "spin" : ""} />
          </button>
          <button className="icon-button mobile-logout" aria-label="Se déconnecter" onClick={() => void onLogout()}><LogOut size={19} /></button>
        </div>
      </header>
      <main className="main-content" id="main-content">
        {!online && <div className="alert warning" role="status"><WifiOff size={18} />Hors connexion. Les enregistrements nécessitent Internet dans cette version.</div>}
        {error && <div className="alert error" role="alert">{error}<button className="text-button" onClick={() => void load()}>Réessayer</button></div>}
        {notice && <div className="toast" role="status"><Check size={18} />{notice}</div>}

        <section className="page-heading">
          <div><div className="eyebrow">VOTRE QUOTIDIEN, SIMPLIFIÉ</div>
            <h1>{page === "dashboard" ? "Vue d’ensemble" : sections.find(s => s.id === page)?.label}</h1>
            <p className="muted">{page === "dashboard" ? "L’essentiel pour garder le cap sur vos chantiers." :
              "Retrouvez et mettez à jour les informations de votre entreprise."}</p>
          </div>
          {page !== "dashboard" && !loading && (page !== "projects" || canManage) &&
            <button className="button primary" disabled={busy || !online || (page !== "projects" && !data.projects.length)}
              onClick={() => page === "photos" ? setUploading(true) : setEditor({ table: page as Table, row: null })}>
              <Plus size={19} />{page === "photos" ? "Ajouter une photo" : `Nouveau${page === "tasks" || page === "expenses" ? "lle" : ""} ${singular[page as Table]}`}
            </button>}
        </section>

        {loading ? <div className="loading-grid" aria-label="Chargement" aria-busy="true">
          {[1, 2, 3, 4].map(n => <div className="skeleton" key={n} />)}
        </div> : page === "dashboard" ? <>
          <section className="hero-banner">
            <div><span className="hero-pill"><span /> Votre espace opérationnel</span>
              <h2>Chaque détail compte.<br />Gardez une longueur d’avance.</h2>
              <p>{data.projects.filter(p => p.status === "active").length} chantier(s) en cours · {data.tasks.filter(t => t.status !== "done").length} tâche(s) à suivre</p>
              <button className="button white" onClick={() => navigate("projects")}>Voir mes chantiers<ArrowUpRight size={18} /></button>
            </div>
            <div className="hero-art" aria-hidden="true"><Building2 size={140} strokeWidth={1} /></div>
          </section>
          <section className="stats-grid">
            {[
              { label: "Chantiers actifs", value: data.projects.filter(p => p.status === "active").length, icon: Building2, page: "projects", status: "active", hint: "Opérations en cours" },
              { label: "Tâches à faire", value: data.tasks.filter(t => t.status === "todo").length, icon: CheckSquare, page: "tasks", status: "todo", hint: `${overdue.length} tâche(s) en retard au total` },
              { label: "Rapports soumis", value: data.reports.filter(r => r.status === "submitted").length, icon: ClipboardList, page: "reports", status: "submitted", hint: "En attente de validation" },
              ...(canManage ? [{ label: "Dépenses enregistrées", value: money(data.expenses.reduce((s, e) => s + Number(e.amount_cents), 0)), icon: CreditCard, page: "expenses", status: "", hint: `${money(monthExpenses.reduce((s, e) => s + Number(e.amount_cents), 0))} ce mois-ci` }] : [])
            ].map(stat => <button className="stat-card" key={stat.label} onClick={() => navigate(stat.page as Page, "", stat.status)}>
              <span className="stat-top">{stat.label}<stat.icon size={20} /></span>
              <strong>{stat.value}</strong><small>{stat.hint}</small>
            </button>)}
          </section>
          <div className="dashboard-grid">
            <section className="panel">
              <div className="panel-heading"><h2>Derniers chantiers</h2><button className="text-button" onClick={() => navigate("projects")}>Tout voir <ArrowUpRight size={16} /></button></div>
              {!data.projects.length ? <Empty title="Votre premier chantier vous attend" text="Créez un chantier pour organiser vos opérations." action={canManage &&
                <button className="button primary" onClick={() => setEditor({ table: "projects", row: null })}><Plus size={18} />Créer un chantier</button>} /> :
                data.projects.slice(0, 4).map(p => <button className="project-line" key={p.id} onClick={() => navigate("tasks", p.id)}>
                  <span className="project-symbol"><Building2 size={23} /></span>
                  <span className="line-main"><strong>{p.name}</strong><small>{p.client_name || "Client non renseigné"}</small></span>
                  <Badge value={p.status} /><ArrowUpRight size={17} />
                </button>)}
            </section>
            <section className="panel">
              <div className="panel-heading"><h2>À surveiller</h2><span className="badge badge-high">{overdue.length} retard(s)</span></div>
              {!overdue.length ? <Empty title="Aucun retard enregistré" text="Les tâches échues non terminées apparaîtront ici." /> :
                overdue.slice(0, 5).map(t => <button key={t.id} className="attention-line" onClick={() => setEditor({ table: "tasks", row: t })}>
                  <span className="attention-dot" /><span><strong>{t.title}</strong><small>{projectName(t.project_id)} · {dateLabel(t.due_date)}</small></span>
                  <ArrowUpRight size={17} />
                </button>)}
            </section>
          </div>
        </> : <>
          <section className="filters">
            <div className="search-box"><Search size={19} /><input aria-label="Rechercher" placeholder="Rechercher…" value={query} onChange={e => setQuery(e.target.value)} /></div>
            {page !== "projects" && <select aria-label="Filtrer par chantier" value={projectFilter} onChange={e => setProjectFilter(e.target.value)}>
              <option value="">Tous les chantiers</option>{data.projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>}
            {page !== "photos" && <select aria-label="Filtrer par statut" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
              <option value="">Tous les statuts</option>
              {(page === "projects" ? ["planned", "active", "paused", "completed", "archived"] :
                page === "tasks" ? ["todo", "doing", "blocked", "done"] :
                page === "reports" ? ["draft", "submitted", "validated"] : ["pending", "paid"])
                .map(s => <option key={s} value={s}>{labels[s]}</option>)}
            </select>}
          </section>
          {page === "expenses" && <div className="expense-summary panel">
            <span>Total de la sélection<strong>{money(rows.reduce((sum, r) => sum + Number(r.amount_cents), 0))}</strong></span>
            <span>Reste à payer<strong>{money(rows.filter(r => r.status === "pending").reduce((sum, r) => sum + Number(r.amount_cents), 0))}</strong></span>
          </div>}
          {!rows.length ? <div className="panel"><Empty title={query || projectFilter || statusFilter ? "Aucun résultat" : "Rien pour le moment"}
            text={data.projects.length || page === "projects" ? "Ajoutez un élément ou ajustez vos filtres." : "Créez d’abord un chantier pour commencer."} /></div> :
          <div className={page === "photos" ? "photo-grid" : page === "projects" ? "projects-grid" : "records-list"}>
            {rows.map(row => page === "projects" ? <article className="panel project-card" key={row.id}>
              <div className="card-top"><span className="project-symbol"><Building2 size={26} /></span><Badge value={row.status} /></div>
              <h2>{row.name}</h2><p className="muted">{row.client_name || "Client à renseigner"}</p>
              <div className="address"><MapPin size={15} /><span>{row.address || "Adresse non renseignée"}</span></div>
              {(() => {
                const tasks = data.tasks.filter(t => t.project_id === row.id);
                const value = progress(tasks);
                return <div className="progress-section"><div><span>Avancement des tâches</span><strong>{value === null ? "—" : `${value} %`}</strong></div>
                  <div className="progress-track"><span style={{ width: `${value || 0}%` }} /></div>
                  <small className="muted">{tasks.length ? `${tasks.filter(t => t.status === "done").length} / ${tasks.length} terminée(s)` : "Aucune tâche pour le moment"}</small>
                </div>;
              })()}
              <footer className="card-footer"><button className="text-button" onClick={() => navigate("tasks", row.id)}>Voir les tâches<ArrowUpRight size={17} /></button>
                <div className="row-actions">{canManage && editButton("projects", row)}{deleteButton("projects", row)}</div></footer>
            </article> : page === "photos" ? <article className="panel photo-card" key={row.id}>
              {row.url ? <a href={row.url} target="_blank" rel="noreferrer"><img src={row.url} alt={row.caption || "Photo de chantier"} loading="lazy" /></a> :
                <div className="photo-placeholder"><Camera /><span>Photo indisponible — actualisez la page</span></div>}
              <div className="photo-caption"><div><strong>{row.caption || "Photo de chantier"}</strong><small>{projectName(row.project_id)} · {dateLabel(row.created_at)}</small></div>{deleteButton("photos", row)}</div>
            </article> : <article className="panel record-card" key={row.id}>
              <div className="record-main"><div className="record-title"><h2>{row.title}</h2><Badge value={row.status} /></div>
                <p className="muted small">{projectName(row.project_id)}{page === "tasks" && row.due_date ? ` · Échéance : ${dateLabel(row.due_date)}` : ""}
                  {page === "reports" ? ` · ${dateLabel(row.report_date)}` : ""}
                  {page === "expenses" ? ` · ${row.supplier || "Fournisseur non renseigné"} · ${dateLabel(row.expense_date)}` : ""}</p>
                {page === "tasks" && <><Badge value={row.priority} />{row.description && <p className="record-text">{row.description}</p>}</>}
                {page === "reports" && <p className="record-text">{row.content}</p>}
                {page === "expenses" && <div className="amount">{money(Number(row.amount_cents))}<small>{row.category} · TTC</small></div>}
              </div>
              <div className="record-actions">
                {page === "tasks" && <select aria-label={`Statut de ${row.title}`} disabled={busy} value={row.status}
                  onChange={e => setStatus("tasks", row, e.target.value)}>
                  {["todo", "doing", "blocked", "done"].map(s => <option value={s} key={s}>{labels[s]}</option>)}
                </select>}
                {page === "reports" && row.status === "draft" && <button className="button secondary" disabled={busy}
                  onClick={() => setStatus("reports", row, "submitted")}>Soumettre</button>}
                {page === "reports" && row.status === "submitted" && canManage && <button className="button primary" disabled={busy}
                  onClick={() => setStatus("reports", row, "validated")}>Valider</button>}
                {page === "expenses" && row.status === "pending" && <button className="button secondary" disabled={busy}
                  onClick={() => setStatus("expenses", row, "paid")}>Marquer payée</button>}
                <div className="row-actions">
                  {!(page === "reports" && row.status === "validated") && editButton(page as Table, row)}
                  {!(page === "reports" && row.status === "validated") && deleteButton(page, row)}
                </div>
              </div>
            </article>)}
          </div>}
          {(data[page] || []).length >= 500 && <div className="alert warning">Cette version affiche au maximum 500 éléments par module. Les totaux portent sur les éléments chargés.</div>}
        </>}
        <footer className="page-footer">ChantierFlow · Version initiale 0.1 · Données en EUR</footer>
      </main>
    </div>
    <nav className="mobile-nav" aria-label="Navigation mobile">
      {visibleSections.map(s => <button key={s.id} className={page === s.id ? "selected" : ""}
        aria-current={page === s.id ? "page" : undefined} onClick={() => navigate(s.id)}><s.icon size={21} /><span>{s.label}</span></button>)}
    </nav>
    {editor && <Editor key={`${editor.table}-${editor.row?.id || "new"}`} table={editor.table} row={editor.row}
      projects={data.projects} organizationId={organizationId} userId={session.user.id}
      onClose={() => setEditor(null)} onSaved={saved} />}
    {uploading && <PhotoUpload projects={data.projects} organizationId={organizationId}
      userId={session.user.id} onClose={() => setUploading(false)} onSaved={saved} />}
  </div>;
}

export default function App() {
  const [session, setSession] = useState<Session | null>(null);
  const [ready, setReady] = useState(false);
  const [recovery, setRecovery] = useState(false);
  const [organizations, setOrganizations] = useState<Row[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!db) { setReady(true); return; }
    void db.auth.getSession().then(({ data, error }) => {
      if (error) setError(errorMessage(error));
      setSession(data.session); setReady(true);
    });
    const { data } = db.auth.onAuthStateChange((event, next) => {
      setSession(next);
      if (event === "PASSWORD_RECOVERY") setRecovery(true);
      if (event === "SIGNED_OUT") { setOrganizations([]); setRecovery(false); }
    });
    return () => data.subscription.unsubscribe();
  }, []);

  const loadOrganizations = useCallback(async () => {
    if (!session) return;
    setLoading(true); setError("");
    try {
      const result = await db!.from("organizations").select("*").order("created_at");
      if (result.error) throw result.error;
      setOrganizations(result.data || []);
    } catch (err) { setError(errorMessage(err)); }
    finally { setLoading(false); }
  }, [session?.user.id]);

  useEffect(() => { void loadOrganizations(); }, [loadOrganizations]);

  async function logout() {
    const result = await db!.auth.signOut();
    if (result.error) { setError(errorMessage(result.error)); return; }
    setOrganizations([]); setSession(null);
  }

  if (!configured) return <main className="setup-page"><section className="panel setup-card">
    <div className="brand"><Building2 />ChantierFlow</div><h1>Connectez votre backend.</h1>
    <p>Créez le fichier <code>.env</code> à partir de <code>.env.example</code>, renseignez l’URL Supabase et sa clé publique, puis redémarrez l’application.</p>
    <p className="muted">Exécutez également le fichier SQL fourni dans votre projet Supabase.</p>
  </section></main>;
  if (!ready) return <main className="setup-page"><p role="status">Ouverture de votre espace…</p></main>;
  if (!session || recovery) return <Auth recovery={recovery} onRecovered={() => setRecovery(false)} />;
  if (loading) return <main className="setup-page"><p role="status">Chargement de votre entreprise…</p></main>;
  if (error) return <main className="setup-page"><section className="panel setup-card">
    <h1>Impossible de charger l’espace</h1><div role="alert" className="alert error">{error}</div>
    <button className="button primary" onClick={() => void loadOrganizations()}>Réessayer</button>
    <button className="text-button" onClick={() => void logout()}>Se déconnecter</button>
  </section></main>;
  if (!organizations.length) return <OrganizationSetup onCreated={loadOrganizations} onLogout={logout} />;
  return <Workspace key={session.user.id} session={session} organizations={organizations} onLogout={logout} />;
}
