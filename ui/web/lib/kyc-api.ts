import { authenticatedFetch, readAuthenticatedJson } from "./authenticated-fetch";

export type KycStatus = {
  status: "NOT_STARTED" | "PENDING" | "EXPIRED" | "VERIFIED" | "FLAGGED" | "REJECTED";
  sessionId: number | null;
  hostedUrl: string | null;
  explanation: string | null;
  expiresAt: string | null;
  updatedAt: string | null;
};

async function request(path: string, method = "GET"): Promise<KycStatus> {
  const response = await authenticatedFetch(`/api/v1/kyc${path}`, { method });
  const payload = await readAuthenticatedJson<{ success: boolean; message: string; data: KycStatus }>(response);
  if (!payload.success) throw new Error(payload.message);
  return payload.data;
}

export const kycApi = {
  status: () => request(""),
  start: () => request("/sessions", "POST"),
  refresh: () => request("/refresh", "POST"),
};
