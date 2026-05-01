import { useState, type FormEvent } from "react";
import { shortenUrl } from "../api/urlApi";
import type { ShortenRequest, ShortenResponse } from "../api/urlApi";

interface Props {
  onSuccess: (result: ShortenResponse) => void;
}

export default function ShortenForm({ onSuccess }: Props) {
  const [longUrl, setLongUrl] = useState("");
  const [customAlias, setCustomAlias] = useState("");
  const [expiryDays, setExpiryDays] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    const payload: ShortenRequest = {
      longUrl,
      customAlias: customAlias.trim() || undefined,
      expiryDays: expiryDays ? parseInt(expiryDays) : undefined,
    };

    try {
      const result = await shortenUrl(payload);
      onSuccess(result);
      setLongUrl("");
      setCustomAlias("");
      setExpiryDays("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <label className="text-sm font-medium text-gray-700">Long URL *</label>
        <input
          type="url"
          required
          placeholder="https://example.com/very/long/url"
          value={longUrl}
          onChange={(e) => setLongUrl(e.target.value)}
          className="border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      <div className="flex gap-3">
        <div className="flex flex-col gap-1 flex-1">
          <label className="text-sm font-medium text-gray-700">Custom alias</label>
          <input
            type="text"
            placeholder="my-link (optional)"
            value={customAlias}
            onChange={(e) => setCustomAlias(e.target.value)}
            className="border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>
        <div className="flex flex-col gap-1 w-36">
          <label className="text-sm font-medium text-gray-700">Expires in (days)</label>
          <input
            type="number"
            min={1}
            placeholder="Never"
            value={expiryDays}
            onChange={(e) => setExpiryDays(e.target.value)}
            className="border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>
      </div>

      {error && (
        <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-2.5">
          {error}
        </p>
      )}

      <button
        type="submit"
        disabled={loading}
        className="bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white font-medium rounded-lg px-6 py-2.5 text-sm transition-colors cursor-pointer"
      >
        {loading ? "Shortening…" : "Shorten URL"}
      </button>
    </form>
  );
}
