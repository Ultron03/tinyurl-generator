const BASE = "http://localhost:8080";

export interface ShortenRequest {
  longUrl: string;
  customAlias?: string;
  expiryDays?: number;
}

export interface ShortenResponse {
  shortUrl: string;
  shortCode: string;
  longUrl: string;
  createdAt: string;
  expiresAt: string | null;
}

export interface AnalyticsResponse {
  shortCode: string;
  longUrl: string;
  totalClicks: number;
  clicksLast24Hours: number;
  clicksLast7Days: number;
  lastAccessedAt: string;
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error ?? `Request failed: ${res.status}`);
  }
  return res.json();
}

export async function shortenUrl(data: ShortenRequest): Promise<ShortenResponse> {
  const res = await fetch(`${BASE}/api/v1/shorten`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  return handleResponse<ShortenResponse>(res);
}

export async function getAnalytics(shortCode: string): Promise<AnalyticsResponse> {
  const res = await fetch(`${BASE}/api/v1/analytics/${shortCode}`);
  return handleResponse<AnalyticsResponse>(res);
}
