import { createClient } from "@supabase/supabase-js";

const url = import.meta.env.VITE_SUPABASE_URL;
const key = import.meta.env.VITE_SUPABASE_ANON_KEY;

export const configured = Boolean(url && key);

export const db = configured
  ? createClient(url, key, {
      auth: {
        persistSession: true,
        autoRefreshToken: true,
        detectSessionInUrl: true
      }
    })
  : null;

export function errorMessage(error: unknown): string {
  const e = error as { message?: string; code?: string };
  if (e?.code === "42501") return "Vous n’avez pas les droits nécessaires.";
  if (e?.code === "23503") return "Cet élément est encore lié à d’autres données.";
  if (e?.code === "23514") return "Certaines valeurs ne respectent pas les règles attendues.";
  if (e?.message === "Invalid login credentials") return "E-mail ou mot de passe incorrect.";
  if (e?.message === "Email not confirmed") return "Confirmez votre adresse e-mail avant de vous connecter.";
  if (e?.message?.includes("Failed to fetch")) return "Connexion impossible. Vérifiez votre réseau.";
  return e?.message || "Une erreur est survenue. Veuillez réessayer.";
}
