import { useState } from "react";
import { Link } from "react-router-dom";
import type { ShortenResponse } from "../api/urlApi";

interface Props {
  result: ShortenResponse;
}

export default function ShortenResult({ result }: Props) {
  const [copied, setCopied] = useState(false);

  function copy() {
    navigator.clipboard.writeText(result.shortUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  return (
    <div className="bg-indigo-50 border border-indigo-200 rounded-lg p-4 flex flex-col gap-3">
      <p className="text-sm font-medium text-gray-700">Your short URL</p>

      <div className="flex items-center gap-2">
        <a
          href={result.shortUrl}
          target="_blank"
          rel="noreferrer"
          className="flex-1 text-indigo-600 font-medium text-sm break-all hover:underline"
        >
          {result.shortUrl}
        </a>
        <button
          onClick={copy}
          className="shrink-0 text-xs border border-indigo-300 bg-white hover:bg-indigo-50 text-indigo-600 rounded-md px-3 py-1.5 transition-colors cursor-pointer"
        >
          {copied ? "Copied!" : "Copy"}
        </button>
      </div>

      <div className="text-xs text-gray-500 flex items-center justify-between">
        <div className="flex gap-4">
          <span>Code: <strong className="text-gray-700">{result.shortCode}</strong></span>
          {result.expiresAt && (
            <span>Expires: <strong className="text-gray-700">{new Date(result.expiresAt).toLocaleDateString()}</strong></span>
          )}
        </div>
        <Link
          to={`/analytics?code=${result.shortCode}`}
          className="text-indigo-500 hover:underline"
        >
          View analytics →
        </Link>
      </div>
    </div>
  );
}
