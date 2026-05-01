import { useState } from "react";
import { Link } from "react-router-dom";
import ShortenForm from "../components/ShortenForm";
import ShortenResult from "../components/ShortenResult";
import type { ShortenResponse } from "../api/urlApi";

export default function HomePage() {
  const [result, setResult] = useState<ShortenResponse | null>(null);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4">
      <div className="w-full max-w-lg">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">TinyURL Generator</h1>
          <p className="text-gray-500 mt-2 text-sm">Shorten any URL in one click</p>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 flex flex-col gap-5">
          <ShortenForm onSuccess={setResult} />
          {result && <ShortenResult result={result} />}
        </div>

        <p className="text-center mt-4 text-sm text-gray-500">
          Want to see click stats?{" "}
          <Link to="/analytics" className="text-indigo-600 hover:underline">
            View analytics →
          </Link>
        </p>
      </div>
    </div>
  );
}
