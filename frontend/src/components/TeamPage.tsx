import type { EvaluationResult, HSRCharacter, Loadout } from '../api/types.ts';
import {
  RELIC_SLOT_LABEL,
  characterPortrait,
  formatDamage,
  lightConeIcon,
  normalizeName,
} from '../api/gameData.ts';
import { CharacterCard } from './cards.tsx';
import { GameImage, StatChip } from './ui.tsx';

export interface TeamMeta {
  rarity: number;
  element: string;
  portrait: string;
  icon: string;
}

function setSummary(relics: Loadout['relics'], setNames: Record<string, string>): string {
  const counts = new Map<string, number>();
  for (const r of relics) counts.set(r.set_id, (counts.get(r.set_id) ?? 0) + 1);
  const parts = [...counts.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, 2)
    .map(([setId, n]) => `${n}pc ${setNames[setId] ?? setId}`);
  return parts.join(' + ') || 'No relics';
}

function damageFor(
  result: EvaluationResult | null,
  team: HSRCharacter[],
  characterId: string,
): number | null {
  if (!result) return null;
  const rows = result.characterDamage ?? [];
  const byName = new Map(rows.map((r) => [normalizeName(r.name), r.totalDamage]));
  const self = team.find((t) => t.id === characterId);
  if (self && byName.has(normalizeName(self.name))) return byName.get(normalizeName(self.name)) ?? null;
  if (rows.length === team.length) {
    const idx = team.findIndex((t) => t.id === characterId);
    return idx >= 0 ? rows[idx].totalDamage : null;
  }
  return null;
}

export function TeamPage({
  team,
  draftIds,
  roster,
  meta,
  setNames,
  loadouts,
  result,
  busy,
  onToggleDraft,
  onSave,
  onEditCurrent,
  onViewCharacter,
  onManageLoadout,
  onOptimize,
}: {
  team: HSRCharacter[];
  draftIds: string[];
  roster: HSRCharacter[];
  meta: Record<string, TeamMeta>;
  setNames: Record<string, string>;
  loadouts: Record<string, Loadout>;
  result: EvaluationResult | null;
  busy: boolean;
  onToggleDraft: (id: string) => void;
  onSave: () => void;
  onEditCurrent: () => void;
  onViewCharacter: (id: string) => void;
  onManageLoadout: (id: string) => void;
  onOptimize: () => void;
}) {
  const slots: Array<HSRCharacter | null> = [0, 1, 2, 3].map((i) => team[i] ?? null);
  return (
    <div className="grid">
      <div className="team-head panel">
        <div>
          <h3>Team lineup</h3>
          <div className="chips">
            <StatChip label="Members" value={`${team.length}/4`} />
            {result && <StatChip label="Team damage" value={formatDamage(result.totalDamage)} />}
          </div>
        </div>
        <div className="conn">
          <button type="button" className="btn" onClick={onEditCurrent}>
            Edit current
          </button>
          <button type="button" className="btn primary" disabled={busy || draftIds.length === 0} onClick={onSave}>
            Save team
          </button>
          <button type="button" className="btn primary" disabled={busy || team.length === 0} onClick={onOptimize}>
            Optimize
          </button>
        </div>
      </div>

      <div className="team-slots">
        {slots.map((member, i) => {
          if (!member) {
            return (
              <div key={`empty-${i}`} className="slot empty">
                <span className="slot-num">{i + 1}</span>
                <span className="muted">Empty slot</span>
              </div>
            );
          }
          const m = meta[member.id];
          const gear = loadouts[member.id];
          const dmg = damageFor(result, team, member.id);
          return (
            <div key={member.id} className={`slot rarity-${m?.rarity ?? 5}`}>
              <span className="slot-num">{i + 1}</span>
              <GameImage
                src={characterPortrait(member.id, m)}
                alt={member.name}
                className="slot-art"
                fallback={member.name.slice(0, 2).toUpperCase()}
              />
              <strong>{member.name}</strong>
              <span className="muted">
                Lv {member.level} · E{member.eidolon}
                {dmg !== null ? ` · ${formatDamage(dmg)}` : ''}
              </span>
              <div className="slot-gear">
                {gear?.lightCone ? (
                  <span className="chip" title={gear.lightCone.name}>
                    <GameImage
                      src={lightConeIcon(gear.lightCone.id)}
                      alt={gear.lightCone.name}
                      className="mini-icon"
                      fallback="LC"
                    />
                    <span className="chip-value">
                      {gear.lightCone.name} S{gear.lightCone.superimposition}
                    </span>
                  </span>
                ) : (
                  <span className="muted">No cone</span>
                )}
                <span className="muted">{setSummary(gear?.relics ?? [], setNames)}</span>
              </div>
              <div className="conn">
                <button type="button" className="btn" onClick={() => onViewCharacter(member.id)}>
                  Character
                </button>
                <button type="button" className="btn" onClick={() => onManageLoadout(member.id)}>
                  Gear
                </button>
              </div>
            </div>
          );
        })}
      </div>

      <div>
        <h3>Roster picker</h3>
        <p className="muted">Tap characters to draft slots, then save. Saving caches the lineup for the optimizer.</p>
        <div className="grid chars">
          {roster.map((c) => (
            <CharacterCard
              key={c.id}
              character={c}
              meta={meta[c.id]}
              selected={draftIds.includes(c.id)}
              onSelect={() => onToggleDraft(c.id)}
              badge={draftIds.includes(c.id) ? `SLOT ${draftIds.indexOf(c.id) + 1}` : undefined}
            />
          ))}
        </div>
      </div>

      {result && (
        <div className="grid items">
          {(result.characterDamage ?? []).map((row) => {
            const member = team.find((t) => normalizeName(t.name) === normalizeName(row.name));
            return (
              <div key={row.name} className="item-card">
                <GameImage
                  src={characterPortrait(member?.id ?? '', member ? meta[member.id] : undefined)}
                  alt={row.name}
                  className="thumb"
                  fallback={row.name.slice(0, 2).toUpperCase()}
                />
                <div>
                  <strong>
                    {member?.name ?? row.name} · {formatDamage(row.totalDamage)}
                  </strong>
                  <div className="muted">
                    {row.relics.map((r) => `${RELIC_SLOT_LABEL[r.slot] ?? r.slot}:${r.mainstat}`).join(' · ')}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
