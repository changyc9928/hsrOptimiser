import { useMemo, useState } from 'react';
import type { EvaluationResult, HSRCharacter, Loadout } from '../api/types.ts';
import { characterPortrait, formatDamage, lightConeIcon, normalizeName } from '../api/gameData.ts';
import { GameImage, IconPlus, IconSearch, Modal, PageHeader } from './ui.tsx';

export interface TeamMeta {
  rarity: number;
  element: string;
  portrait: string;
  icon: string;
}

function setSummary(relics: Loadout['relics'], setNames: Record<string, string>): string {
  const counts = new Map<string, number>();
  for (const relic of relics) counts.set(relic.set_id, (counts.get(relic.set_id) ?? 0) + 1);
  const parts = [...counts.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, 2)
    .map(([setId, count]) => `${count}pc ${setNames[setId] ?? setId}`);
  return parts.join(' + ') || 'No relics';
}

function damageFor(result: EvaluationResult | null, team: HSRCharacter[], characterId: string): number | null {
  if (!result) return null;
  const rows = result.characterDamage ?? [];
  const byName = new Map(rows.map((row) => [normalizeName(row.name), row.totalDamage]));
  const self = team.find((member) => member.id === characterId);
  if (self && byName.has(normalizeName(self.name))) return byName.get(normalizeName(self.name)) ?? null;
  if (rows.length === team.length) {
    const index = team.findIndex((member) => member.id === characterId);
    return index >= 0 ? rows[index].totalDamage : null;
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
  onOpenCharacter,
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
  onSave: () => Promise<void>;
  onOpenCharacter: (id: string) => void;
  onManageLoadout: (id: string) => void;
  onOptimize: () => void;
}) {
  const [pickerOpen, setPickerOpen] = useState(false);
  const [query, setQuery] = useState('');

  const slots: Array<HSRCharacter | null> = [0, 1, 2, 3].map((index) => team[index] ?? null);
  const total = result?.totalDamage ?? 0;
  const ranked = useMemo(
    () => [...(result?.characterDamage ?? [])].sort((a, b) => b.totalDamage - a.totalDamage),
    [result],
  );
  const pickerRows = useMemo(() => {
    const term = query.trim().toLowerCase();
    return [...roster]
      .filter((character) => !term || `${character.name} ${character.id} ${character.path}`.toLowerCase().includes(term))
      .sort((a, b) => {
        const aDraft = draftIds.indexOf(a.id);
        const bDraft = draftIds.indexOf(b.id);
        if (aDraft >= 0 && bDraft < 0) return -1;
        if (bDraft >= 0 && aDraft < 0) return 1;
        return a.name.localeCompare(b.name);
      });
  }, [roster, query, draftIds]);

  async function saveTeam() {
    await onSave();
    setPickerOpen(false);
  }

  return (
    <>
      <PageHeader
        eyebrow={team.length > 0 ? `${team.length} of 4 slots filled` : 'No lineup saved'}
        title="Team"
        subtitle="Pick the four characters the optimizer should evaluate together."
        actions={
          <>
            <button type="button" className="btn" onClick={() => setPickerOpen(true)}>
              Edit lineup
            </button>
            <button type="button" className="btn primary" onClick={onOptimize} disabled={team.length === 0}>
              Optimize team
            </button>
          </>
        }
      />

      <div className="team-layout">
        <section className="lineup">
          {slots.map((member, index) => {
            if (!member) {
              return (
                <button
                  key={`empty-${index}`}
                  type="button"
                  className="lineup-slot empty"
                  onClick={() => setPickerOpen(true)}
                >
                  <span className="lineup-index">{index + 1}</span>
                  <IconPlus />
                  <span>Add member</span>
                </button>
              );
            }
            const info = meta[member.id];
            const gear = loadouts[member.id];
            const damage = damageFor(result, team, member.id);
            return (
              <article key={member.id} className={`lineup-slot rarity-${info?.rarity ?? 5}`}>
                <span className="lineup-index">{index + 1}</span>
                <div className="lineup-art">
                  <GameImage
                    src={characterPortrait(member.id, info)}
                    alt={member.name}
                    className="lineup-portrait"
                    fallback={member.name.slice(0, 2).toUpperCase()}
                  />
                </div>
                <div className="lineup-body">
                  <strong>{member.name}</strong>
                  <span className="muted">Lv {member.level} · E{member.eidolon}</span>
                  <div className="lineup-damage">
                    {damage !== null ? formatDamage(damage) : '—'}
                    {damage !== null && total > 0 ? (
                      <span className="muted">{((damage / total) * 100).toFixed(1)}%</span>
                    ) : null}
                  </div>
                  <div className="lineup-gear">
                    {gear?.lightCone ? (
                      <span className="gear-line">
                        <GameImage
                          src={lightConeIcon(gear.lightCone.id)}
                          alt={gear.lightCone.name}
                          className="mini-icon"
                          fallback="LC"
                        />
                        {gear.lightCone.name} S{gear.lightCone.superimposition}
                      </span>
                    ) : (
                      <span className="muted">No light cone</span>
                    )}
                    <span className="muted">{setSummary(gear?.relics ?? [], setNames)}</span>
                  </div>
                  <div className="lineup-actions">
                    <button type="button" className="btn small" onClick={() => onOpenCharacter(member.id)}>
                      Character
                    </button>
                    <button type="button" className="btn small" onClick={() => onManageLoadout(member.id)}>
                      Gear
                    </button>
                  </div>
                </div>
              </article>
            );
          })}
        </section>

        <aside className="team-summary panel">
          <div className="panel-head">
            <h2>Lineup summary</h2>
          </div>
          <div className="panel-body">
            <div className="summary-stat">
              <span className="muted">Team damage</span>
              <strong>{result ? formatDamage(result.totalDamage) : '—'}</strong>
            </div>
            <div className="summary-stat">
              <span className="muted">Members</span>
              <strong>{team.length}/4</strong>
            </div>
            {ranked.length > 0 ? (
              <div className="damage-bars">
                {ranked.map((row) => (
                  <div key={row.name} className="damage-bar-row">
                    <span className="damage-bar-name">{row.name}</span>
                    <div className="damage-bar">
                      <div
                        className="damage-bar-fill"
                        style={{ width: `${total > 0 ? Math.max(2, (row.totalDamage / total) * 100) : 0}%` }}
                      />
                    </div>
                    <span className="damage-bar-value">{formatDamage(row.totalDamage)}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="muted">
                {team.length === 0
                  ? 'Save a lineup to start.'
                  : 'Run the optimizer to preview each member’s damage contribution.'}
              </p>
            )}
          </div>
        </aside>
      </div>

      {pickerOpen ? (
        <Modal
          title="Edit lineup"
          subtitle={`${draftIds.length} of 4 selected`}
          onClose={() => setPickerOpen(false)}
          width="wide"
        >
          <div className="picker-toolbar">
            <label className="filter search-field grow">
              <IconSearch />
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search roster"
                autoFocus
              />
            </label>
            <span className="muted">{draftIds.length}/4 selected</span>
          </div>
          <div className="picker-grid">
            {pickerRows.map((character) => {
              const info = meta[character.id];
              const selected = draftIds.includes(character.id);
              const slot = draftIds.indexOf(character.id) + 1;
              const disabled = !selected && draftIds.length >= 4;
              return (
                <button
                  key={character.id}
                  type="button"
                  className={`picker-char ${selected ? 'selected' : ''}`}
                  disabled={disabled}
                  onClick={() => onToggleDraft(character.id)}
                >
                  <GameImage
                    src={characterPortrait(character.id, info)}
                    alt={character.name}
                    className="picker-char-art"
                    fallback={character.name.slice(0, 2).toUpperCase()}
                  />
                  <span className="picker-char-body">
                    <strong>{character.name}</strong>
                    <span className="muted">
                      Lv {character.level} · {character.path}
                    </span>
                  </span>
                  {selected ? <span className="picker-char-slot">{slot}</span> : null}
                </button>
              );
            })}
          </div>
          {pickerRows.length === 0 ? <p className="muted">No characters match.</p> : null}
          <div className="modal-actions">
            <button type="button" className="btn" onClick={() => setPickerOpen(false)} disabled={busy}>
              Cancel
            </button>
            <button
              type="button"
              className="btn primary"
              onClick={() => void saveTeam()}
              disabled={busy || draftIds.length === 0}
            >
              {busy ? 'Saving…' : 'Save team'}
            </button>
          </div>
        </Modal>
      ) : null}
    </>
  );
}
