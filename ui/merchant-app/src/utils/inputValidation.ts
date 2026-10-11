// Draft rules allow editing states; submit rules require complete values.
export const isAmountDraft = (value: string) => /^\d{0,13}(\.\d{0,4})?$/.test(value) && (value === "" || value === "." || Number(value) <= 1e12);
export const isValidAmount = (value: string) => /^\d+(\.\d{1,4})?$/.test(value) && Number(value) >= 0.01 && Number(value) <= 1e12;
export const isPhoneDraft = (value: string) => /^\+?[\d\s().-]*$/.test(value) && value.length <= 40;
export function normalizePhone(value: string): string | null {
  let phone = value.trim().replace(/[\s().-]/g, "");
  if (phone.startsWith("00")) phone = "+" + phone.slice(2);
  if (/^\d{7}$/.test(phone)) phone = "+220" + phone;
  else if (/^\d{8,15}$/.test(phone)) phone = "+" + phone;
  return /^\+[1-9]\d{7,14}$/.test(phone) ? phone : null;
}
export const isNameDraft = (value: string) => /^[\p{L}\p{M} .’'-]*$/u.test(value) && value.length <= 80;
export const isValidName = (value: string) => isNameDraft(value) && /\p{L}/u.test(value);
export const isValidEmail = (value: string) => value.length <= 254 && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
export const isCurrencyDraft = (value: string) => /^[A-Za-z]{0,3}$/.test(value);
export function passwordIssue(value: string): string | null {
  let bytes = 0;
  for (const character of value) { const code = character.codePointAt(0)!; bytes += code <= 0x7f ? 1 : code <= 0x7ff ? 2 : code <= 0xffff ? 3 : 4; }
  return value.length < 8 ? "Use at least 8 characters for your password." : bytes > 72 ? "Your password exceeds the supported length (72 UTF-8 bytes)." : /[\r\n\0]/.test(value) ? "Password cannot contain line breaks or null characters." : null;
}
