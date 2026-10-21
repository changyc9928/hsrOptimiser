import { useMemo, useState } from 'react';
import type { GameData } from '../api/gameData.ts';
import {
  RELIC_MAIN_STATS,
  RELIC_SLOT_LABEL,
  RELIC_SLOT_ORDER,
  RELIC_SUBSTAT_KEYS,
  displayStat,
  formatSubstat,
  relicSetIcon,
} from '../api/gameData.ts';
import { addRelic, deleteRelic, updateRelic, type BackendConfig } from '../api/client.ts';
import type { Relic, RelicSlot, SubStat } from '../api/types.ts';
import { ConfirmDialog, EmptyState, GameImage, IconEdit, IconPlus, IconSearch, IconTrash, Modal, PageHeader } from './ui.tsx';

type SortKey = 'level' | 'rarity' | 'main' | 'set';

interface SubRow {
  key: string;
  value: string;
  count: number;
  step: number;
}

interface Props {
  config: BackendConfig;
  relics: Relic[];
  game: GameData | null;
  characterNames: Record<string, string>;
  onChanged: () => Promise<void>;
  notify: (text: string, kind?: 'info' | 'good' | 'error') => void;
}

function setName(game: GameData | null, setId: string): string {
  return game?.relicSets[setId]?.name ?? setId;
}

function subRows(relic?: Relic): SubRow[] {
  if (!relic) return [];
  return relic.substats.map((sub) => ({
    key: sub.key,
    value: String(sub.value),
    count: sub.count,
    step: sub.step,
  }));
}

export function RelicInventory({ config, relics, game, characterNames, onChanged, notify }: Props) {
  const [q, setQ] = useState('');
  const [slot, setSlot] = useState('all');
  const [setId, setSetId] = useState('all');
  const [main, setMain] = useState('all');
  const [rarity, setRarity] = useState('all');
  const [equipped, setEquipped] = useState('all');
  const [sortKey, setSortKey] = useState<SortKey>('level');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc');
  const [form, setForm] = useState<{ mode: 'create' } | { mode: 'edit'; relic: Relic } | null>(null);
  const [deleting, setDeleting] = useState<Relic | null>(null);
  const [busy, setBusy] = useState(false);

  const setOptions = useMemo(() => {
    const ids = new Set(relics.map((r) => r.set_id));
    return [...ids].sort((a, b) => setName(game, a).localeCompare(setName(game, b)));
  }, [relics, game]);

  const mainOptions = useMemo(
    () => [...new Set(relics.map((r) => r.mainstat))].sort(),
    [relics],
  );

  const rows = useMemo(() => {
    const term = q.trim().toLowerCase();
    const filtered = relics.filter((relic) => {
      if (slot !== 'all' && relic.slot !== slot) return false;
      if (setId !== 'all' && relic.set_id !== setId) return false;
      if (main !== 'all' && relic.mainstat !== main) return false;
      if (rarity !== 'all' && String(relic.rarity) !== rarity) return false;
      if (equipped === 'free' && relic.location) return false;
      if (equipped === 'equipped' && !relic.location) return false;
      if (!term) return true;
      const holder = relic.location ? characterNames[relic.location] ?? relic.location : '';
      return `${setName(game, relic.set_id)} ${relic.mainstat} ${relic.uid} ${holder}`
        .toLowerCase()
        .includes(term);
    });
    const dir = sortDir === 'asc' ? 1 : -1;
    return [...filtered].sort((a, b) => {
      if (sortKey === 'rarity') return dir * (a.rarity - b.rarity || a.level - b.level);
      if (sortKey === 'main') return dir * a.mainstat.localeCompare(b.mainstat);
      if (sortKey === 'set') {
        return dir * setName(game, a.set_id).localeCompare(setName(game, b.set_id));
      }
      return dir * (a.level - b.level || a.rarity - b.rarity);
    });
  }, [relics, q, slot, setId, main, rarity, equipped, sortKey, sortDir, game, characterNames]);

  function resetFilters() {
    setQ('');
    setSlot('all');
    setSetId('all');
    setMain('all');
    setRarity('all');
    setEquipped('all');
  }

  async function confirmDelete() {
    if (!deleting) return;
    setBusy(true);
    try {
      await deleteRelic(config, deleting.uid);
      notify('Relic deleted.', 'good');
      setDeleting(null);
      await onChanged();
    } catch (err) {
      notify(err instanceof Error ? err.message : 'Delete failed', 'error');
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow={`${relics.length} owned`}
        title="Relics"
        subtitle="Filter, edit and curate every relic in your scanned inventory."
        actions={
          <button type="button" className="btn primary" onClick={() => setForm({ mode: 'create' })}>
            <IconPlus /> Add relic
          </button>
        }
      />

      <div className="inventory-layout">
        <aside className="filters">
          <div className="filter-head">
            <span>Filters</span>
            <button type="button" className="link-btn" onClick={resetFilters}>
              Reset
            </button>
          </div>
          <label className="filter search-field">
            <IconSearch />
            <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search set, stat, holder" />
          </label>
          <label className="filter">
            Slot
            <select value={slot} onChange={(e) => setSlot(e.target.value)}>
              <option value="all">All slots</option>
              {RELIC_SLOT_ORDER.map((value) => (
                <option key={value} value={value}>
                  {RELIC_SLOT_LABEL[value]}
                </option>
              ))}
            </select>
          </label>
          <label className="filter">
            Set
            <select value={setId} onChange={(e) => setSetId(e.target.value)}>
              <option value="all">All sets</option>
              {setOptions.map((id) => (
                <option key={id} value={id}>
                  {setName(game, id)}
                </option>
              ))}
            </select>
          </label>
          <label className="filter">
            Main stat
            <select value={main} onChange={(e) => setMain(e.target.value)}>
              <option value="all">All main stats</option>
              {mainOptions.map((value) => (
                <option key={value} value={value}>
                  {displayStat(value)}
                </option>
              ))}
            </select>
          </label>
          <label className="filter">
            Rarity
            <select value={rarity} onChange={(e) => setRarity(e.target.value)}>
              <option value="all">All rarities</option>
              <option value="5">5 star</option>
              <option value="4">4 star</option>
              <option value="3">3 star</option>
            </select>
          </label>
          <label className="filter">
            Status
            <select value={equipped} onChange={(e) => setEquipped(e.target.value)}>
              <option value="all">All relics</option>
              <option value="free">Unequipped</option>
              <option value="equipped">Equipped</option>
            </select>
          </label>
        </aside>

        <section className="inventory-content">
          <div className="list-toolbar">
            <span className="muted">
              {rows.length} of {relics.length} relics
            </span>
            <div className="toolbar-controls">
              <label className="inline-field">
                Sort
                <select value={sortKey} onChange={(e) => setSortKey(e.target.value as SortKey)}>
                  <option value="level">Level</option>
                  <option value="rarity">Rarity</option>
                  <option value="main">Main stat</option>
                  <option value="set">Set</option>
                </select>
              </label>
              <button
                type="button"
                className="btn small"
                onClick={() => setSortDir((prev) => (prev === 'asc' ? 'desc' : 'asc'))}
                title="Toggle sort direction"
              >
                {sortDir === 'asc' ? 'Ascending' : 'Descending'}
              </button>
            </div>
          </div>

          {rows.length === 0 ? (
            <EmptyState
              title="No relics match"
              hint={relics.length === 0 ? 'Import a scanner export to populate your inventory.' : 'Try clearing the filters.'}
              action={
                relics.length === 0 ? null : (
                  <button type="button" className="btn" onClick={resetFilters}>
                    Clear filters
                  </button>
                )
              }
            />
          ) : (
            <div className="relic-grid">
              {rows.map((relic) => {
                const holder = relic.location ? characterNames[relic.location] ?? relic.location : '';
                return (
                  <article key={relic.uid} className={`relic-item rarity-${relic.rarity}`}>
                    <div className="relic-item-top">
                      <GameImage
                        src={relicSetIcon(relic.set_id, game?.relicSets[relic.set_id])}
                        alt={setName(game, relic.set_id)}
                        className="relic-item-art"
                        fallback="RE"
                      />
                      <div className="relic-item-id">
                        <span className="eyebrow">{RELIC_SLOT_LABEL[relic.slot] ?? relic.slot}</span>
                        <strong>{setName(game, relic.set_id)}</strong>
                        <span className="stars">{'★'.repeat(relic.rarity)}</span>
                      </div>
                      <span className="relic-item-level">+{relic.level}</span>
                    </div>

                    <div className="relic-item-main">
                      <span className="muted">Main stat</span>
                      <strong>{displayStat(relic.mainstat)}</strong>
                    </div>

                    <div className="relic-item-subs">
                      {relic.substats.length === 0 ? (
                        <span className="muted">No substats</span>
                      ) : (
                        relic.substats.map((sub) => (
                          <div key={`${relic.uid}-${sub.key}`} className="sub-row">
                            <span>{displayStat(sub.key)}</span>
                            <span>{formatSubstat(sub.value)}</span>
                          </div>
                        ))
                      )}
                    </div>

                    <div className="relic-item-foot">
                      <span className={`pill ${holder ? 'pill-equipped' : ''}`}>{holder || 'Free'}</span>
                      {relic.lock ? <span className="pill">Locked</span> : null}
                      <div className="item-actions">
                        <button
                          type="button"
                          className="icon-btn"
                          title="Edit relic"
                          onClick={() => setForm({ mode: 'edit', relic })}
                        >
                          <IconEdit />
                        </button>
                        <button
                          type="button"
                          className="icon-btn danger"
                          title="Delete relic"
                          onClick={() => setDeleting(relic)}
                        >
                          <IconTrash />
                        </button>
                      </div>
                    </div>
                  </article>
                );
              })}
            </div>
          )}
        </section>
      </div>

      {form ? (
        <RelicForm
          config={config}
          game={game}
          relic={form.mode === 'edit' ? form.relic : undefined}
          onClose={() => setForm(null)}
          onSaved={async (message) => {
            setForm(null);
            notify(message, 'good');
            await onChanged();
          }}
          notify={notify}
        />
      ) : null}

      <ConfirmDialog
        open={deleting !== null}
        title="Delete relic?"
        message={
          deleting
            ? `${setName(game, deleting.set_id)} ${RELIC_SLOT_LABEL[deleting.slot] ?? deleting.slot} (+${deleting.level}). This cannot be undone.`
            : ''
        }
        busy={busy}
        onCancel={() => setDeleting(null)}
        onConfirm={() => void confirmDelete()}
      />
    </>
  );
}

function RelicForm({
  config,
  game,
  relic,
  onClose,
  onSaved,
  notify,
}: {
  config: BackendConfig;
  game: GameData | null;
  relic?: Relic;
  onClose: () => void;
  onSaved: (message: string) => Promise<void>;
  notify: (text: string, kind?: 'info' | 'good' | 'error') => void;
}) {
  const editing = Boolean(relic);
  const [setId, setSetId] = useState(relic?.set_id ?? '');
  const [slot, setSlot] = useState<RelicSlot>(relic?.slot ?? 'Body');
  const [rarity, setRarity] = useState(String(relic?.rarity ?? 5));
  const [level, setLevel] = useState(String(relic?.level ?? 15));
  const [mainstat, setMainstat] = useState(relic?.mainstat ?? 'CRIT DMG');
  const [lock, setLock] = useState(relic?.lock ?? false);
  const [discard, setDiscard] = useState(relic?.discard ?? false);
  const [subs, setSubs] = useState<SubRow[]>(subRows(relic));
  const [busy, setBusy] = useState(false);

  async function submit() {
    if (!editing && !setId.trim()) {
      notify('Pick a relic set.', 'error');
      return;
    }
    const substats: SubStat[] = subs
      .filter((sub) => sub.key)
      .map((sub) => ({
        key: sub.key,
        value: Number(sub.value) || 0,
        count: Math.max(1, sub.count),
        step: Math.max(1, sub.step),
      }));
    const levelValue = Number(level);
    if (!Number.isFinite(levelValue) || levelValue < 0 || levelValue > 15) {
      notify('Relic level must be between 0 and 15.', 'error');
      return;
    }
    setBusy(true);
    try {
      if (relic) {
        await updateRelic(config, relic.uid, {
          level: levelValue,
          mainstat,
          substats,
          lock,
          discard,
        });
        await onSaved('Relic updated.');
      } else {
        await addRelic(config, {
          setId: setId.trim(),
          slot,
          rarity: Number(rarity) || 5,
          level: levelValue,
          mainstat,
          substats,
          lock,
        });
        await onSaved('Relic added.');
      }
    } catch (err) {
      notify(err instanceof Error ? err.message : 'Save failed', 'error');
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal
      title={editing ? 'Edit relic' : 'Add relic'}
      subtitle={editing ? `${setName(game, relic?.set_id ?? '')} · ${RELIC_SLOT_LABEL[relic?.slot ?? ''] ?? ''}` : 'New relics start in your inventory and can be equipped from a character.'}
      onClose={onClose}
      width="regular"
    >
      <form
        className="form-stack"
        onSubmit={(event) => {
          event.preventDefault();
          void submit();
        }}
      >
        <div className="form-grid">
          <label className="field">
            Set
            <select value={setId} onChange={(e) => setSetId(e.target.value)} disabled={editing}>
              <option value="">Select set…</option>
              {Object.entries(game?.relicSets ?? {})
                .sort(([, a], [, b]) => a.name.localeCompare(b.name))
                .map(([id, meta]) => (
                  <option key={id} value={id}>
                    {meta.name}
                  </option>
                ))}
            </select>
          </label>
          <label className="field">
            Slot
            <select value={slot} onChange={(e) => setSlot(e.target.value as RelicSlot)} disabled={editing}>
              {RELIC_SLOT_ORDER.map((value) => (
                <option key={value} value={value}>
                  {RELIC_SLOT_LABEL[value]}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            Rarity
            <select value={rarity} onChange={(e) => setRarity(e.target.value)} disabled={editing}>
              <option value="5">5 star</option>
              <option value="4">4 star</option>
              <option value="3">3 star</option>
            </select>
          </label>
          <label className="field">
            Level
            <input value={level} inputMode="numeric" onChange={(e) => setLevel(e.target.value)} />
          </label>
          <label className="field">
            Main stat
            <select value={mainstat} onChange={(e) => setMainstat(e.target.value)}>
              {RELIC_MAIN_STATS.map((value) => (
                <option key={value} value={value}>
                  {displayStat(value)}
                </option>
              ))}
            </select>
          </label>
          <label className="field checkbox-field">
            Locked
            <input type="checkbox" checked={lock} onChange={(e) => setLock(e.target.checked)} />
          </label>
          {editing ? (
            <label className="field checkbox-field">
              Discarded
              <input type="checkbox" checked={discard} onChange={(e) => setDiscard(e.target.checked)} />
            </label>
          ) : null}
        </div>

        <div className="sub-editor">
          <div className="sub-editor-head">
            <h3>Substats</h3>
            <button
              type="button"
              className="btn small"
              onClick={() => setSubs((prev) => [...prev, { key: 'CRIT Rate_', value: '', count: 1, step: 1 }])}
            >
              <IconPlus /> Add substat
            </button>
          </div>
          {subs.length === 0 ? <p className="muted">No substats yet.</p> : null}
          {subs.map((sub, index) => (
            <div key={`${sub.key}-${index}`} className="sub-editor-row">
              <select
                value={sub.key}
                onChange={(e) =>
                  setSubs((prev) =>
                    prev.map((row, i) => (i === index ? { ...row, key: e.target.value } : row)),
                  )
                }
              >
                {RELIC_SUBSTAT_KEYS.map((key) => (
                  <option key={key} value={key}>
                    {displayStat(key)}
                  </option>
                ))}
              </select>
              <input
                value={sub.value}
                inputMode="decimal"
                placeholder="Value"
                onChange={(e) =>
                  setSubs((prev) =>
                    prev.map((row, i) => (i === index ? { ...row, value: e.target.value } : row)),
                  )
                }
              />
              <button
                type="button"
                className="icon-btn danger"
                title="Remove substat"
                onClick={() => setSubs((prev) => prev.filter((_, i) => i !== index))}
              >
                <IconTrash />
              </button>
            </div>
          ))}
        </div>

        <div className="modal-actions">
          <button type="button" className="btn" onClick={onClose} disabled={busy}>
            Cancel
          </button>
          <button type="submit" className="btn primary" disabled={busy}>
            {busy ? 'Saving…' : editing ? 'Save relic' : 'Add relic'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
