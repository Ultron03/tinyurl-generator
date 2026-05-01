import type { AnalyticsResponse } from "../api/urlApi";

interface Props {
  data: AnalyticsResponse;
}

function Stat({ label, value }: { label: string; value: number | string }) {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-4 flex flex-col gap-1">
      <p className="text-2xl font-semibold text-gray-900">{value}</p>
      <p className="text-xs text-gray-500">{label}</p>
    </div>
  );
}

export default function AnalyticsCard({ data }: Props) {
  return (
    <div className="flex flex-col gap-4">
      <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 flex flex-col gap-1">
        <p className="text-xs text-gray-500">Short code</p>
        <p className="font-medium text-gray-900">{data.shortCode}</p>
        <p className="text-xs text-gray-500 break-all mt-1">{data.longUrl}</p>
      </div>

      <div className="grid grid-cols-3 gap-3">
        <Stat label="Total clicks" value={data.totalClicks} />
        <Stat label="Last 24 hours" value={data.clicksLast24Hours} />
        <Stat label="Last 7 days" value={data.clicksLast7Days} />
      </div>

      {data.lastAccessedAt && (
        <p className="text-xs text-gray-500 text-center">
          Last accessed: {new Date(data.lastAccessedAt).toLocaleString()}
        </p>
      )}
    </div>
  );
}
