import { useMemo, useState } from 'react';
import type { GameData } from '../api/gameData.ts';
import { lightConePortrait, pathIcon } from '../api/gameData.ts';
import { addLightCone, deleteLightCone, updateLightCone, type BackendConfig } from '../api/client.ts';
import type { LightCone } from '../api/types.ts';
import { ConfirmDialog, EmptyState, GameImage, IconEdit, IconPlus, IconSearch, IconTrash, Modal, PageHeader } from './ui.tsx';

type SortKey = 'level' | 'rarity' | 'name';

interface Props {
  config: BackendConfig;
  cones: LightCone[];
  game: GameData | null;
  characterNames: Record<string, string>;
  onChanged: () => Promise<void>;
  notify: (text: string, kind?: 'info' | 'good' | 'error') => void;
}

function coneRarity(game: GameData | null, cone: LightCone): number {
  return game?.lightCones[cone.id]?.rarity ?? (cone.id.startsWith('23') ? 5 : 4);
}

function coneName(game: GameData | null, cone: LightCone): string {
  return game?.lightCones[cone.id]?.name || cone.name || cone.id;
}

export function LightConeInventory({ config, cones, game, characterNames, onChanged, notify }: Props) {
  const [q, setQ] = useState('');
  const [path, setPath] = useState('all');
  const [rarity, setRarity] = useState('all');
  const [equipped, setEquipped] = useState('all');
  const [sortKey, setSortKey] = useState<SortKey>('level');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc');
  const [form, setForm] = useState<{ mode: 'create' } | { mode: 'edit'; cone: LightCone } | null>(null);
  const [deleting, setDeleting] = useState<LightCone | null>(null);
  const [busy, setBusy] = useState(false);

  const paths = useMemo(() => {
    const values = new Set<string>();
    for (const cone of cones) {
      const value = game?.lightCones[cone.id]?.path;
      if (value) values.add(value);
    }
    return [...values].sort();
  }, [cones, game]);

  const rows = useMemo(() => {
    const term = q.trim().toLowerCase();
    const filtered = cones.filter((cone) => {
      const meta = game?.lightCones[cone.id];
      if (path !== 'all' && meta?.path !== path) return false;
      if (rarity !== 'all' && String(coneRarity(game, cone)) !== rarity) return false;
      if (equipped === 'free' && cone.location) return false;
      if (equipped === 'equipped' && !cone.location) return false;
      if (!term) return true;
      const holder = cone.location ? characterNames[cone.location] ?? cone.location : '';
      return `${coneName(game, cone)} ${cone.uid} ${holder}`.toLowerCase().includes(term);
    });
    const dir = sortDir === 'asc' ? 1 : -1;
    return [...filtered].sort((a, b) => {
      if (sortKey === 'name') return dir * coneName(game, a).localeCompare(coneName(game, b));
      if (sortKey === 'rarity') {
        return dir * (coneRarity(game, a) - coneRarity(game, b) || a.level - b.level);
      }
      return dir * (a.level - b.level || a.superimposition - b.superimposition);
    });
  }, [cones, q, path, rarity, equipped, sortKey, sortDir, game, characterNames]);

  function resetFilters() {
    setQ('');
    setPath('all');
    setRarity('all');
    setEquipped('all');
  }

  async function confirmDelete() {
    if (!deleting) return;
    setBusy(true);
    try {
      await deleteLightCone(config, deleting.uid);
      notify('Light cone deleted.', 'good');
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
        eyebrow={`${cones.length} owned`}
        title="Light cones"
        subtitle="Every cone you own, with its current holder and superimposition."
        actions={
          <button type="button" className="btn primary" onClick={() => setForm({ mode: 'create' })}>
            <IconPlus /> Add light cone
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
            <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search name, holder" />
          </label>
          <label className="filter">
            Path
            <select value={path} onChange={(e) => setPath(e.target.value)}>
              <option value="all">All paths</option>
              {paths.map((value) => (
                <option key={value} value={value}>
                  {value}
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
              <option value="all">All cones</option>
              <option value="free">Unequipped</option>
              <option value="equipped">Equipped</option>
            </select>
          </label>
        </aside>

        <section className="inventory-content">
          <div className="list-toolbar">
            <span className="muted">
              {rows.length} of {cones.length} light cones
            </span>
            <div className="toolbar-controls">
              <label className="inline-field">
                Sort
                <select value={sortKey} onChange={(e) => setSortKey(e.target.value as SortKey)}>
                  <option value="level">Level</option>
                  <option value="rarity">Rarity</option>
                  <option value="name">Name</option>
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
              title="No light cones match"
              hint={cones.length === 0 ? 'Import a scanner export to populate your inventory.' : 'Try clearing the filters.'}
              action={
                cones.length === 0 ? null : (
                  <button type="button" className="btn" onClick={resetFilters}>
                    Clear filters
                  </button>
                )
              }
            />
          ) : (
            <div className="cone-grid">
              {rows.map((cone) => {
                const meta = game?.lightCones[cone.id];
                const holder = cone.location ? characterNames[cone.location] ?? cone.location : '';
                return (
                  <article key={cone.uid} className={`cone-item rarity-${coneRarity(game, cone)}`}>
                    <GameImage
                      src={lightConePortrait(cone.id, meta)}
                      alt={coneName(game, cone)}
                      className="cone-item-art"
                      fallback="LC"
                    />
                    <div className="cone-item-body">
                      <div className="cone-item-title">
                        <strong>{coneName(game, cone)}</strong>
                        <span className="stars">{'★'.repeat(coneRarity(game, cone))}</span>
                      </div>
                      <div className="cone-item-meta">
                        <span>Lv {cone.level}</span>
                        <span className="accent">S{cone.superimposition}</span>
                        {meta?.path ? (
                          <span className="path-tag">
                            <GameImage src={pathIcon(meta.path)} alt={meta.path} className="mini-icon" fallback="P" />
                            {meta.path}
                          </span>
                        ) : null}
                      </div>
                      <div className="cone-item-foot">
                        <span className={`pill ${holder ? 'pill-equipped' : ''}`}>{holder || 'Free'}</span>
                        {cone.lock ? <span className="pill">Locked</span> : null}
                        <div className="item-actions">
                          <button
                            type="button"
                            className="icon-btn"
                            title="Edit light cone"
                            onClick={() => setForm({ mode: 'edit', cone })}
                          >
                            <IconEdit />
                          </button>
                          <button
                            type="button"
                            className="icon-btn danger"
                            title="Delete light cone"
                            onClick={() => setDeleting(cone)}
                          >
                            <IconTrash />
                          </button>
                        </div>
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
        <LightConeForm
          config={config}
          game={game}
          cone={form.mode === 'edit' ? form.cone : undefined}
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
        title="Delete light cone?"
        message={
          deleting
            ? `${coneName(game, deleting)} (Lv ${deleting.level}, S${deleting.superimposition}). This cannot be undone.`
            : ''
        }
        busy={busy}
        onCancel={() => setDeleting(null)}
        onConfirm={() => void confirmDelete()}
      />
    </>
  );
}

function LightConeForm({
  config,
  game,
  cone,
  onClose,
  onSaved,
  notify,
}: {
  config: BackendConfig;
  game: GameData | null;
  cone?: LightCone;
  onClose: () => void;
  onSaved: (message: string) => Promise<void>;
  notify: (text: string, kind?: 'info' | 'good' | 'error') => void;
}) {
  const editing = Boolean(cone);
  const [id, setId] = useState(cone?.id ?? '');
  const [level, setLevel] = useState(String(cone?.level ?? 80));
  const [ascension, setAscension] = useState(String(cone?.ascension ?? 6));
  const [superimposition, setSuperimposition] = useState(String(cone?.superimposition ?? 1));
  const [lock, setLock] = useState(cone?.lock ?? false);
  const [busy, setBusy] = useState(false);

  async function submit() {
    if (!id.trim()) {
      notify('Pick a light cone.', 'error');
      return;
    }
    const levelValue = Number(level);
    const ascensionValue = Number(ascension);
    const superimpositionValue = Number(superimposition);
    if (!Number.isFinite(levelValue) || levelValue < 1 || levelValue > 80) {
      notify('Light cone level must be between 1 and 80.', 'error');
      return;
    }
    if (!Number.isFinite(ascensionValue) || ascensionValue < 0 || ascensionValue > 6) {
      notify('Ascension must be between 0 and 6.', 'error');
      return;
    }
    if (!Number.isFinite(superimpositionValue) || superimpositionValue < 1 || superimpositionValue > 5) {
      notify('Superimposition must be between 1 and 5.', 'error');
      return;
    }
    setBusy(true);
    try {
      if (cone) {
        await updateLightCone(config, cone.uid, {
          id: id.trim(),
          level: levelValue,
          ascension: ascensionValue,
          superimposition: superimpositionValue,
          lock,
        });
        await onSaved('Light cone updated.');
      } else {
        await addLightCone(config, {
          id: id.trim(),
          level: levelValue,
          ascension: ascensionValue,
          superimposition: superimpositionValue,
          lock,
        });
        await onSaved('Light cone added.');
      }
    } catch (err) {
      notify(err instanceof Error ? err.message : 'Save failed', 'error');
    } finally {
      setBusy(false);
    }
  }

  const meta = id ? game?.lightCones[id] : undefined;

  return (
    <Modal
      title={editing ? 'Edit light cone' : 'Add light cone'}
      subtitle={editing ? coneName(game, cone ?? { id, name: '', level: 1, ascension: 0, superimposition: 1, location: null, lock: false, uid: '' }) : 'Pick from the game data index.'}
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
          <label className="field wide">
            Light cone
            <select value={id} onChange={(e) => setId(e.target.value)}>
              <option value="">Select light cone…</option>
              {Object.entries(game?.lightCones ?? {})
                .sort(([, a], [, b]) => a.name.localeCompare(b.name))
                .map(([coneId, info]) => (
                  <option key={coneId} value={coneId}>
                    {info.name} · {info.rarity}★
                  </option>
                ))}
            </select>
          </label>
          {meta ? (
            <div className="wide meta-preview">
              <GameImage src={lightConePortrait(id, meta)} alt={meta.name} className="meta-preview-art" fallback="LC" />
              <div>
                <strong>{meta.name}</strong>
                <div className="muted">
                  {meta.rarity} star · {meta.path}
                </div>
              </div>
            </div>
          ) : null}
          <label className="field">
            Level
            <input value={level} inputMode="numeric" onChange={(e) => setLevel(e.target.value)} />
          </label>
          <label className="field">
            Ascension
            <input value={ascension} inputMode="numeric" onChange={(e) => setAscension(e.target.value)} />
          </label>
          <label className="field">
            Superimposition
            <input
              value={superimposition}
              inputMode="numeric"
              onChange={(e) => setSuperimposition(e.target.value)}
            />
          </label>
          <label className="field checkbox-field">
            Locked
            <input type="checkbox" checked={lock} onChange={(e) => setLock(e.target.checked)} />
          </label>
        </div>

        <div className="modal-actions">
          <button type="button" className="btn" onClick={onClose} disabled={busy}>
            Cancel
          </button>
          <button type="submit" className="btn primary" disabled={busy}>
            {busy ? 'Saving…' : editing ? 'Save light cone' : 'Add light cone'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
