import { useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { getAnalytics } from "../api/urlApi";
import type { AnalyticsResponse } from "../api/urlApi";
import AnalyticsCard from "../components/AnalyticsCard";

function extractShortCode(input: string): string {
  try {
    return new URL(input).pathname.replace(/^\//, "");
  } catch {
    return input;
  }
}

export default function AnalyticsPage() {
  const [searchParams] = useSearchParams();
  const [shortCode, setShortCode] = useState(searchParams.get("code") ?? "");
  const [data, setData] = useState<AnalyticsResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setData(null);
    setLoading(true);

    try {
      const code = extractShortCode(shortCode.trim());
      const result = await getAnalytics(code);
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4">
      <div className="w-full max-w-lg">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Analytics</h1>
          <p className="text-gray-500 mt-2 text-sm">Enter a short code to see its click stats</p>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 flex flex-col gap-5">
          <form onSubmit={handleSubmit} className="flex gap-2">
            <input
              type="text"
              required
              placeholder="e.g. 1Z or my-link"
              value={shortCode}
              onChange={(e) => setShortCode(e.target.value)}
              className="flex-1 border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
            <button
              type="submit"
              disabled={loading}
              className="bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white font-medium rounded-lg px-5 py-2.5 text-sm transition-colors cursor-pointer"
            >
              {loading ? "…" : "Lookup"}
            </button>
          </form>

          {error && (
            <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-2.5">
              {error}
            </p>
          )}

          {data && <AnalyticsCard data={data} />}
        </div>

        <p className="text-center mt-4 text-sm text-gray-500">
          <Link to="/" className="text-indigo-600 hover:underline">
            ← Back to shortener
          </Link>
        </p>
      </div>
    </div>
  );
}
