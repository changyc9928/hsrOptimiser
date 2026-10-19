import { useMemo, useState } from 'react';
import { formatDamage } from '../api/gameData.ts';

export interface ProgressSample {
  epoch: number;
  temperature: number;
  damage: number;
}

const W = 720;
const H = 240;
const PAD = { top: 16, right: 16, bottom: 28, left: 58 };
const PLOT_W = W - PAD.left - PAD.right;
const PLOT_H = H - PAD.top - PAD.bottom;

function axisTicks(dMin: number, dMax: number, count = 4): number[] {
  return Array.from({ length: count + 1 }, (_, i) => dMin + ((dMax - dMin) / count) * i);
}

/**
 * Best-team damage plotted per epoch (single metric).
 * Rendered as raw SVG to avoid pulling a charting dependency into the bundle.
 */
export function OptimizerChart({ samples }: { samples: ProgressSample[] }) {
  const [hover, setHover] = useState<number | null>(null);

  const geom = useMemo(() => {
    if (samples.length === 0) return null;

    const epochs = samples.map((s) => s.epoch);
    const damages = samples.map((s) => s.damage);

    const minEpoch = Math.min(...epochs);
    const maxEpoch = Math.max(...epochs);
    const epochSpan = Math.max(1, maxEpoch - minEpoch);

    // Auto-domain: fit the observed damage range (with headroom) so the
    // line fills the vertical space instead of hugging the top.
    const rawMin = Math.min(...damages);
    const rawMax = Math.max(...damages);
    const rawSpan = rawMax - rawMin;
    const pad = rawSpan > 0 ? rawSpan * 0.12 : Math.max(1, Math.abs(rawMax) * 0.1);
    const dMin = Math.max(0, rawMin - pad);
    const dMax = Math.max(dMin + 1, rawMax + pad);

    const x = (epoch: number) => PAD.left + ((epoch - minEpoch) / epochSpan) * PLOT_W;
    const yDamage = (damage: number) =>
      PAD.top + PLOT_H - ((damage - dMin) / (dMax - dMin)) * PLOT_H;

    const line = (accessor: (s: ProgressSample) => number, y: (v: number) => number) =>
      samples.map((s, i) => `${i === 0 ? 'M' : 'L'}${x(s.epoch).toFixed(1)},${y(accessor(s)).toFixed(1)}`).join(' ');

    const best = samples.reduce((acc, s) => (s.damage > acc.damage ? s : acc), samples[0]);

    return {
      minEpoch,
      maxEpoch,
      dMin,
      dMax,
      x,
      yDamage,
      damagePath: line((s) => s.damage, yDamage),
      best,
    };
  }, [samples]);

  if (!geom) {
    return <div className="chart-empty">Waiting for the first epoch sample…</div>;
  }

  const { minEpoch, maxEpoch, dMin, dMax, x, yDamage, damagePath, best } = geom;
  const single = samples.length === 1;
  const active = hover !== null ? samples[hover] : null;

  const handleMove = (event: React.MouseEvent<SVGSVGElement>) => {
    const rect = event.currentTarget.getBoundingClientRect();
    // Map client pixels back onto the viewBox before finding the nearest sample.
    const ratio = (event.clientX - rect.left) / rect.width;
    const viewX = ratio * W;
    let nearest = 0;
    let bestDist = Infinity;
    samples.forEach((s, i) => {
      const dist = Math.abs(x(s.epoch) - viewX);
      if (dist < bestDist) {
        bestDist = dist;
        nearest = i;
      }
    });
    setHover(nearest);
  };

  return (
    <div className="chart-wrap">
      <svg
        className="chart"
        viewBox={`0 0 ${W} ${H}`}
        preserveAspectRatio="xMidYMid meet"
        role="img"
        aria-label="Optimisation progress by epoch"
        onMouseMove={handleMove}
        onMouseLeave={() => setHover(null)}
      >
        {/* horizontal grid + damage axis labels */}
        {axisTicks(dMin, dMax).map((t) => {
          const y = yDamage(t);
          return (
            <g key={`d${t}`}>
              <line className="chart-grid" x1={PAD.left} y1={y} x2={W - PAD.right} y2={y} />
              <text className="chart-axis" x={PAD.left - 8} y={y + 4} textAnchor="end">
                {formatDamage(t)}
              </text>
            </g>
          );
        })}

        {/* epoch ticks */}
      {[0, 0.25, 0.5, 0.75, 1].map((f) => {
        const epoch = Math.round(minEpoch + (maxEpoch - minEpoch) * f);
        return (
          <text key={f} className="chart-axis" x={x(epoch)} y={H - 8} textAnchor="middle">
            {epoch}
          </text>
        );
      })}

        <line
          className="chart-axis-line"
          x1={PAD.left}
          y1={PAD.top + PLOT_H}
          x2={W - PAD.right}
          y2={PAD.top + PLOT_H}
        />

        {/* damage (primary) */}
        <path className="chart-damage" d={damagePath} fill="none" />

        {single ? (
          <circle className="chart-dot" cx={x(samples[0].epoch)} cy={yDamage(samples[0].damage)} r={4} />
        ) : null}

        {/* best-so-far marker */}
        <circle className="chart-best" cx={x(best.epoch)} cy={yDamage(best.damage)} r={4.5}>
          <title>{`Best ${formatDamage(best.damage)} at epoch ${best.epoch}`}</title>
        </circle>

        {/* hover crosshair */}
        {active ? (
          <g>
            <line className="chart-crosshair" x1={x(active.epoch)} y1={PAD.top} x2={x(active.epoch)} y2={PAD.top + PLOT_H} />
            <circle className="chart-hover" cx={x(active.epoch)} cy={yDamage(active.damage)} r={4} />
          </g>
        ) : null}
      </svg>

      <div className="chart-legend">
        <span className="chart-key">
          <i className="chart-swatch chart-swatch-damage" /> Damage
        </span>
        <span className="chart-key">
          <i className="chart-swatch chart-swatch-best" /> Best {formatDamage(best.damage)}
        </span>
        {active ? (
          <span className="chart-readout">
            Epoch {active.epoch} · {formatDamage(active.damage)}
          </span>
        ) : null}
      </div>
    </div>
  );
}