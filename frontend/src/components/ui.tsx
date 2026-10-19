import { useState } from 'react';

export function GameImage({
  src,
  alt,
  className,
  fallback,
}: {
  src: string;
  alt: string;
  className?: string;
  fallback: string;
}) {
  const [failed, setFailed] = useState(false);
  if (failed) {
    return <span className={`img-fallback ${className ?? ''}`.trim()}>{fallback}</span>;
  }
  return <img src={src} alt={alt} className={className} loading="lazy" onError={() => setFailed(true)} />;
}

export function Section({
  title,
  action,
  children,
}: {
  title: string;
  action?: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <section className="panel">
      <div className="panel-head">
        <h2>{title}</h2>
        {action}
      </div>
      <div className="panel-body">{children}</div>
    </section>
  );
}

export function StatChip({ label, value }: { label: string; value: string }) {
  return (
    <span className="chip">
      <span className="chip-label">{label}</span>
      <span className="chip-value">{value}</span>
    </span>
  );
}

export function ProgressBar({ value }: { value: number }) {
  const safe = Number.isFinite(value) ? Math.min(100, Math.max(0, value)) : 0;
  return (
    <div className="progress" role="progressbar" aria-valuenow={Math.round(safe)} aria-valuemin={0} aria-valuemax={100}>
      <div className="progress-fill" style={{ width: `${safe}%` }} />
    </div>
  );
}
