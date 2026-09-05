import { describe, expect, it } from "vitest";
import { toCents, progress } from "./domain";

describe("montants", () => {
  it("convertit sans calcul financier flottant", () => {
    expect(toCents("12,34")).toBe(1234);
    expect(toCents("0.01")).toBe(1);
    expect(toCents("15")).toBe(1500);
  });
  it("refuse les formats invalides", () => {
    for (const value of ["-5", "0", "1.234", "NaN", "1e5", ""]) {
      expect(() => toCents(value)).toThrow();
    }
  });
});

describe("avancement", () => {
  it("distingue l’absence de tâches", () => {
    expect(progress([])).toBeNull();
  });
  it("compte les tâches terminées", () => {
    expect(progress([{ status: "done" }, { status: "todo" }])).toBe(50);
  });
});
