export function toCents(input: string): number {
  const value = input.trim().replace(",", ".");
  if (!/^\d{1,8}(\.\d{1,2})?$/.test(value)) {
    throw new Error("Saisissez un montant positif avec au maximum deux décimales.");
  }
  const [whole, fraction = ""] = value.split(".");
  const cents = Number(whole) * 100 + Number(fraction.padEnd(2, "0"));
  if (!Number.isSafeInteger(cents) || cents <= 0) {
    throw new Error("Le montant doit être supérieur à zéro.");
  }
  return cents;
}

export function money(cents: number): string {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR"
  }).format(cents / 100);
}

export function localDay(): string {
  const now = new Date();
  return [
    now.getFullYear(),
    String(now.getMonth() + 1).padStart(2, "0"),
    String(now.getDate()).padStart(2, "0")
  ].join("-");
}

export function dateLabel(value?: string): string {
  if (!value) return "Non renseignée";
  const date = new Date(value.length === 10 ? value + "T12:00:00" : value);
  return new Intl.DateTimeFormat("fr-FR", { dateStyle: "medium" }).format(date);
}

export function progress(tasks: { status: string }[]): number | null {
  if (!tasks.length) return null;
  return Math.round(tasks.filter(t => t.status === "done").length / tasks.length * 100);
}
