import {
  RELIC_SLOT_LABEL,
  characterPortrait,
  displayStat,
  formatDamage,
  formatSubstat,
  relicSetIcon,
} from '../api/gameData.ts';
import type { CharacterDamage, Relic, RelicSlot } from '../api/types.ts';
import { GameImage, StatChip } from './ui.tsx';

const SLOT_ORDER: RelicSlot[] = ['Head', 'Hands', 'Body', 'Feet', 'LinkRope', 'PlanarSphere'];

export interface CharacterMeta {
  rarity: number;
  element: string;
  portrait: string;
  icon: string;
}

export interface RelicSetMeta {
  name: string;
  icon: string;
}

function bySlot(relics: Relic[]): Map<RelicSlot, Relic> {
  const map = new Map<RelicSlot, Relic>();
  for (const relic of relics) {
    if (!map.has(relic.slot)) map.set(relic.slot, relic);
  }
  return map;
}

/** Mirrors the relic card design used on the character page. */
function RelicTile({
  relic,
  set,
}: {
  relic: Relic;
  set?: RelicSetMeta;
}) {
  return (
    <div className="relic-tile relic-detail">
      <div className="relic-top">
        <GameImage
          src={relicSetIcon(relic.set_id, set)}
          alt={set?.name ?? relic.name}
          className="relic-art"
          fallback="RE"
        />
        <div className="relic-head">
          <span className="muted">
            {RELIC_SLOT_LABEL[relic.slot] ?? relic.slot} · {set?.name ?? relic.name}
          </span>
          <strong className="relic-main">{relic.mainstat}</strong>
          <span className="relic-value">+{relic.level}</span>
        </div>
      </div>
      <div className="relic-subs">
        {relic.substats.map((sub) => (
          <div key={`${relic.uid}-${sub.key}`} className="relic-sub">
            <span>{displayStat(sub.key)}</span>
            <span>{formatSubstat(sub.value)}</span>
          </div>
        ))}
        {relic.substats.length === 0 ? <span className="relic-none">No substats</span> : null}
      </div>
    </div>
  );
}

function EmptySlot({ slot }: { slot: RelicSlot }) {
  return (
    <div className="relic-tile relic-tile-empty">
      <span className="relic-slot">{RELIC_SLOT_LABEL[slot] ?? slot}</span>
      <span className="relic-none">Empty</span>
    </div>
  );
}

/** One character from the optimisation result, with their six relic slots. */
export function CharacterResultCard({
  row,
  total,
  rank,
  characterId,
  meta,
  relicSets,
}: {
  row: CharacterDamage;
  total: number;
  rank: number;
  characterId?: string;
  meta?: CharacterMeta;
  relicSets: Record<string, RelicSetMeta>;
}) {
  const relics = row.relics ?? [];
  const slots = bySlot(relics);
  const share = total > 0 ? (row.totalDamage / total) * 100 : 0;

  return (
    <article className={`result-card ${meta ? `rarity-${meta.rarity}` : ''}`}>
      <header className="result-head">
        <GameImage
          src={characterPortrait(characterId ?? '', meta)}
          alt={row.name}
          className="result-portrait"
          fallback={row.name.slice(0, 2).toUpperCase()}
        />
        <div className="result-id">
          <strong>{row.name}</strong>
          {meta?.element ? <span className="result-element">{meta.element}</span> : null}
        </div>
        <div className="result-damage">
          <span className="result-rank">#{rank}</span>
          <strong>{formatDamage(row.totalDamage)}</strong>
          <span className="result-share">{share.toFixed(1)}% of team</span>
        </div>
      </header>

      <div className="result-bar" role="img" aria-label={`${share.toFixed(1)} percent of team damage`}>
        <div className="result-bar-fill" style={{ width: `${Math.max(2, share)}%` }} />
      </div>

      <div className="result-relics">
        {SLOT_ORDER.map((slot) => {
          const relic = slots.get(slot);
          if (!relic) return <EmptySlot key={slot} slot={slot} />;
          return <RelicTile key={slot} relic={relic} set={relicSets[relic.set_id]} />;
        })}
      </div>

      {relics.length > 0 ? (
        <footer className="result-foot">
          <StatChip label="Relics" value={String(relics.length)} />
          <StatChip label="Sets" value={String(new Set(relics.map((r) => r.set_id)).size)} />
        </footer>
      ) : null}
    </article>
  );
}