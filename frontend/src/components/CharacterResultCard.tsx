import {
  RELIC_SLOT_LABEL,
  characterPortrait,
  displayStat,
  elementIcon,
  formatDamage,
  formatSubstat,
  lightConePortrait,
  pathIcon,
  relicSetIcon,
} from '../api/gameData.ts';
import type { LightConeMeta } from '../api/gameData.ts';
import type { CharacterDamage, HSRCharacter, LightCone, Relic, RelicSlot } from '../api/types.ts';
import { GameImage } from './ui.tsx';

const SLOT_ORDER: RelicSlot[] = ['Head', 'Hands', 'Body', 'Feet', 'LinkRope', 'PlanarSphere'];

export interface CharacterMeta {
  rarity: number;
  path: string;
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
  character,
  meta,
  relicSets,
  cones,
  coneMeta,
}: {
  row: CharacterDamage;
  total: number;
  rank: number;
  characterId?: string;
  character?: HSRCharacter;
  meta?: CharacterMeta;
  relicSets: Record<string, RelicSetMeta>;
  cones: LightCone[];
  coneMeta: Record<string, LightConeMeta>;
}) {
  const relics = row.relics ?? [];
  const slots = bySlot(relics);
  const share = total > 0 ? (row.totalDamage / total) * 100 : 0;
  const equippedCone = characterId ? cones.find((cone) => cone.location === characterId) : undefined;
  const coneInfo = equippedCone ? coneMeta[equippedCone.id] : undefined;
  const path = meta?.path ?? character?.path;
  const displayName = character?.name ?? row.name;
  const setBreakdown = [...relics.reduce((counts, relic) => {
    counts.set(relic.set_id, (counts.get(relic.set_id) ?? 0) + 1);
    return counts;
  }, new Map<string, number>())].sort((a, b) => b[1] - a[1]);
  const uniqueSets = setBreakdown.length;

  return (
    <article className={`result-card ${meta ? `rarity-${meta.rarity}` : ''}`}>
      <div className="result-split">
        <section className="result-char">
          <div className="result-char-card">
            <header className="result-char-head">
              <GameImage
                src={characterPortrait(characterId ?? '', meta)}
                alt={displayName}
                className="result-portrait"
                fallback={displayName.slice(0, 2).toUpperCase()}
              />
              <div className="result-id">
                <span className="result-rank">
                  {meta?.element ? (
                    <GameImage
                      src={elementIcon(meta.element)}
                      alt=""
                      className="result-meta-icon"
                      fallback="EL"
                    />
                  ) : null}
                  #{rank} · {meta?.element ?? 'Unknown element'}
                </span>
                <strong className="result-name">{displayName}</strong>
                <span className="result-sub">
                  {path ?? 'Unknown path'}
                  {character ? ` · Lv ${character.level} · E${character.eidolon}` : ''}
                </span>
              </div>
            </header>

            <div className="result-damage">
              <strong>{formatDamage(row.totalDamage)}</strong>
              <span>{share.toFixed(1)}% of team damage</span>
            </div>

            <div className="result-bar" role="img" aria-label={`${share.toFixed(1)} percent of team damage`}>
              <div className="result-bar-fill" style={{ width: `${Math.max(2, share)}%` }} />
            </div>

            <div className="result-sets">
              {setBreakdown.map(([setId, count]) => (
                <div className="result-set" key={setId}>
                  <GameImage
                    src={relicSetIcon(setId, relicSets[setId])}
                    alt=""
                    className="result-set-icon"
                    fallback="SE"
                  />
                  <span className="result-set-name">{relicSets[setId]?.name ?? setId}</span>
                  <strong className="result-set-count">{count}pc</strong>
                </div>
              ))}
            </div>

            <div className="result-stats">
              <div className="result-stat">
                <span className="result-stat-label">Level</span>
                <strong>{character?.level ?? '—'}</strong>
              </div>
              <div className="result-stat">
                <span className="result-stat-label">Eidolon</span>
                <strong>{character ? `E${character.eidolon}` : '—'}</strong>
              </div>
              <div className="result-stat">
                <span className="result-stat-label">Path</span>
                <strong>{path ?? '—'}</strong>
              </div>
              <div className="result-stat">
                <span className="result-stat-label">Element</span>
                <strong>{meta?.element ?? '—'}</strong>
              </div>
              <div className="result-stat">
                <span className="result-stat-label">Relics</span>
                <strong>{relics.length}</strong>
              </div>
              <div className="result-stat">
                <span className="result-stat-label">Sets</span>
                <strong>{uniqueSets}</strong>
              </div>
            </div>
          </div>

          <div className={`result-cone-card ${equippedCone ? '' : 'empty'}`}>
            {equippedCone ? (
              <GameImage
                src={lightConePortrait(equippedCone.id, coneInfo)}
                alt={coneInfo?.name ?? equippedCone.name}
                className="result-cone-art"
                fallback="LC"
              />
            ) : (
              <span className="img-fallback result-cone-art">LC</span>
            )}
            <div className="result-cone-copy">
              <span className="result-label">Equipped light cone</span>
              <strong className="result-cone-name">
                {equippedCone ? coneInfo?.name ?? equippedCone.name : 'None equipped'}
              </strong>
              {equippedCone ? (
                <>
                  <span className="muted">
                    Lv {equippedCone.level} · S{equippedCone.superimposition}
                  </span>
                  {coneInfo?.path ? (
                    <span className="result-cone-path">
                      <GameImage
                        src={pathIcon(coneInfo.path)}
                        alt=""
                        className="result-meta-icon"
                        fallback="PT"
                      />
                      {coneInfo.path}
                    </span>
                  ) : null}
                </>
              ) : (
                <span className="muted">No light cone assigned</span>
              )}
            </div>
          </div>
        </section>

        <div className="result-relics">
          {SLOT_ORDER.map((slot) => {
            const relic = slots.get(slot);
            if (!relic) return <EmptySlot key={slot} slot={slot} />;
            return <RelicTile key={slot} relic={relic} set={relicSets[relic.set_id]} />;
          })}
        </div>
      </div>
    </article>
  );
}
