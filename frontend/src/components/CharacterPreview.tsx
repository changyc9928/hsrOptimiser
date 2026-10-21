import { useEffect, useMemo, useState } from 'react';
import type { HSRCharacter, LightCone, Loadout, Relic } from '../api/types.ts';
import {
  type BackendConfig,
  equipLightCone,
  equipRelics,
  getLoadout,
  unequipLightCone,
  unequipRelics,
  updateCharacter,
  updateLightCone,
  updateRelic,
} from '../api/client.ts';
import {
  CHARACTER_PATHS,
  RELIC_MAIN_STATS,
  RELIC_SLOT_LABEL,
  RELIC_SUBSTAT_KEYS,
  characterPortrait,
  displayStat,
  elementIcon,
  lightConePortrait,
  pathIcon,
  relicSetIcon,
} from '../api/gameData.ts';
import { ConfirmDialog, GameImage, StatChip } from './ui.tsx';

export interface PreviewMeta {
  rarity: number;
  element: string;
  portrait: string;
  icon: string;
}

export interface PreviewSetMeta {
  name: string;
  icon: string;
  desc: string[];
}

function stars(rarity: number): string {
  return '★'.repeat(Math.max(0, Math.min(5, rarity)));
}

function setGroups(relics: Relic[]): Array<{ setId: string; count: number }> {
  const counts = new Map<string, number>();
  for (const r of relics) counts.set(r.set_id, (counts.get(r.set_id) ?? 0) + 1);
  return [...counts.entries()]
    .map(([setId, count]) => ({ setId, count }))
    .sort((a, b) => b.count - a.count);
}

function traceCount(c: HSRCharacter): { on: number; total: number } {
  const t = c.traces;
  if (!t) return { on: 0, total: 0 };
  const keys = [
    'ability1', 'ability2', 'ability3',
    'stat1', 'stat2', 'stat3', 'stat4', 'stat5',
    'stat6', 'stat7', 'stat8', 'stat9', 'stat10',
  ] as const;
  const values = keys.map((k) => t[k]);
  return { on: values.filter(Boolean).length, total: values.length };
}

function num(value: string, fallback: number): number {
  const n = Number(value);
  return Number.isFinite(n) ? n : fallback;
}

function useGridColumns(minCardWidth = 190, maxCols = 7): {
  ref: (node: HTMLDivElement | null) => void;
  cols: number;
} {
  const [el, setEl] = useState<HTMLDivElement | null>(null);
  const [cols, setCols] = useState(maxCols);
  useEffect(() => {
    if (!el) return;
    const update = () => {
      const w = el.clientWidth;
      setCols(Math.max(1, Math.min(maxCols, Math.floor(w / minCardWidth))));
    };
    update();
    const observer = new ResizeObserver(update);
    observer.observe(el);
    return () => observer.disconnect();
  }, [el, minCardWidth, maxCols]);
  return { ref: setEl, cols };
}

export function CharacterPreview({
  config,
  character,
  meta,
  setMeta,
  coneMeta,
  characterNames,
  cones,
  allRelics,
  inTeam,
  onToggleTeam,
  onOptimize,
  onChanged,
  onDelete,
}: {
  config: BackendConfig;
  character: HSRCharacter;
  meta?: PreviewMeta;
  setMeta: Record<string, PreviewSetMeta>;
  coneMeta: Record<string, { rarity: number; icon: string; portrait: string; path: string; name: string }>;
  characterNames: Record<string, string>;
  cones: LightCone[];
  allRelics: Relic[];
  inTeam: boolean;
  onToggleTeam: () => void;
  onOptimize: () => void;
  onChanged: () => Promise<void>;
  onDelete: () => void;
}) {
  const [loadout, setLoadout] = useState<Loadout | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const [name, setName] = useState(character.name);
  const [charPath, setCharPath] = useState(character.path);
  const [level, setLevel] = useState(String(character.level));
  const [ascension, setAscension] = useState(String(character.ascension));
  const [eidolon, setEidolon] = useState(String(character.eidolon));
  const [skills, setSkills] = useState({
    basic: String(character.skills.basic),
    skill: String(character.skills.skill),
    ult: String(character.skills.ult),
    talent: String(character.skills.talent),
  });
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [conePickerOpen, setConePickerOpen] = useState(false);
  const [coneEditOpen, setConeEditOpen] = useState(false);
  const [coneFilter, setConeFilter] = useState({ q: '', path: 'all', holder: 'all', rarity: 'all' });
  const [coneSort, setConeSort] = useState({ key: 'level', dir: 'desc' });
  const [coneLevel, setConeLevel] = useState('');
  const [coneSuper, setConeSuper] = useState('');
  const [coneLock, setConeLock] = useState(false);
  const [coneIdentity, setConeIdentity] = useState('');
  const [relicPickSlot, setRelicPickSlot] = useState<string | null>(null);
  const [relicFilter, setRelicFilter] = useState({ q: '', setId: 'all', main: 'all', holder: 'all' });
  const [relicEditUid, setRelicEditUid] = useState<string | null>(null);
  const [relicForm, setRelicForm] = useState<{
    mainstat: string;
    level: string;
    lock: boolean;
    subs: Array<{ key: string; value: string }>;
  }>({ mainstat: '', level: '', lock: false, subs: [] });
  const picker = useGridColumns(190, 7);

  useEffect(() => {
    if (conePickerOpen) {
      setConeFilter({ q: '', path: 'all', holder: 'all', rarity: 'all' });
      setConeSort({ key: 'level', dir: 'desc' });
    }
  }, [conePickerOpen]);

  const conePaths = useMemo(
    () => Array.from(new Set(cones.map((c) => coneMeta[c.id]?.path).filter((p): p is string => Boolean(p)))).sort(),
    [cones, coneMeta],
  );

  const visibleCones = useMemo(() => {
    const q = coneFilter.q.trim().toLowerCase();
    const rows = cones.filter((c) => {
      if (coneFilter.path !== 'all' && coneMeta[c.id]?.path !== coneFilter.path) return false;
      if (coneFilter.rarity !== 'all' && String(coneMeta[c.id]?.rarity ?? 0) !== coneFilter.rarity) return false;
      if (coneFilter.holder === 'free' && c.location) return false;
      if (coneFilter.holder === 'equipped' && !c.location) return false;
      if (!q) return true;
      return `${c.name} ${c.uid}`.toLowerCase().includes(q);
    });
    const dir = coneSort.dir === 'asc' ? 1 : -1;
    return [...rows].sort((a, b) => {
      if (coneSort.key === 'name') return dir * a.name.localeCompare(b.name);
      if (coneSort.key === 'rarity') {
        return dir * ((coneMeta[a.id]?.rarity ?? 0) - (coneMeta[b.id]?.rarity ?? 0));
      }
      return dir * (a.level - b.level || a.superimposition - b.superimposition);
    });
  }, [cones, coneMeta, coneFilter, coneSort]);

  useEffect(() => {
    setName(character.name);
    setCharPath(character.path);
    setLevel(String(character.level));
    setAscension(String(character.ascension));
    setEidolon(String(character.eidolon));
    setSkills({
      basic: String(character.skills.basic),
      skill: String(character.skills.skill),
      ult: String(character.skills.ult),
      talent: String(character.skills.talent),
    });
  }, [character]);

  useEffect(() => {
    let cancelled = false;
    setBusy(true);
    getLoadout(config, character.id)
      .then((next) => {
        if (cancelled) return;
        setLoadout(next);
        setConeLevel(next.lightCone ? String(next.lightCone.level) : '');
        setConeIdentity(next.lightCone ? next.lightCone.id : '');
        setConeSuper(next.lightCone ? String(next.lightCone.superimposition) : '');
        setConeLock(next.lightCone?.lock ?? false);
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Loadout failed');
      })
      .finally(() => {
        if (!cancelled) setBusy(false);
      });
    return () => {
      cancelled = true;
    };
  }, [config, character.id]);

  const equipped = loadout?.relics ?? [];
  const cone = loadout?.lightCone ?? null;
  const traces = traceCount(character);
  const groups = setGroups(equipped);
  useEffect(() => {
    setRelicFilter({ q: '', setId: 'all', main: 'all', holder: 'all' });
  }, [relicPickSlot]);

  const slotCandidates = useMemo(() => {
    if (!relicPickSlot) return [];
    return allRelics
      .filter((r) => r.slot === relicPickSlot)
      .sort((a, b) => b.level - a.level || b.rarity - a.rarity);
  }, [allRelics, relicPickSlot]);

  const pickCandidates = useMemo(() => {
    const q = relicFilter.q.trim().toLowerCase();
    return slotCandidates.filter((r) => {
      if (relicFilter.setId !== 'all' && r.set_id !== relicFilter.setId) return false;
      if (relicFilter.main !== 'all' && r.mainstat !== relicFilter.main) return false;
      if (relicFilter.holder === 'free' && r.location) return false;
      if (relicFilter.holder === 'equipped' && !r.location) return false;
      if (!q) return true;
      return `${setMeta[r.set_id]?.name ?? r.name} ${r.mainstat} ${r.uid}`.toLowerCase().includes(q);
    });
  }, [slotCandidates, relicFilter, setMeta]);

  const pickSets = useMemo(
    () => Array.from(new Set(slotCandidates.map((r) => r.set_id))).sort(),
    [slotCandidates],
  );
  const pickMains = useMemo(
    () => Array.from(new Set(slotCandidates.map((r) => r.mainstat))).sort(),
    [slotCandidates],
  );

  async function refresh() {
    const next = await getLoadout(config, character.id);
    setLoadout(next);
    await onChanged();
  }

  async function run(label: string, fn: () => Promise<unknown>) {
    setBusy(true);
    setError('');
    try {
      await fn();
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? `${label}: ${err.message}` : label);
    } finally {
      setBusy(false);
    }
  }

  function saveDetails() {
    void run('Save failed', () =>
      updateCharacter(config, character.id, {
        name: name.trim() || character.name,
        path: charPath,
        level: num(level, character.level),
        ascension: num(ascension, character.ascension),
        eidolon: num(eidolon, character.eidolon),
        skills: {
          basic: num(skills.basic, character.skills.basic),
          skill: num(skills.skill, character.skills.skill),
          ult: num(skills.ult, character.skills.ult),
          talent: num(skills.talent, character.skills.talent),
          elation: character.skills.elation,
        },
      }),
    );
  }

  function applyConeEdit() {
    if (!cone) return;
    setBusy(true);
    setError('');
    updateLightCone(config, cone.uid, {
      id: coneIdentity || cone.id,
      level: num(coneLevel, cone.level),
      superimposition: num(coneSuper, cone.superimposition),
      lock: coneLock,
    })
      .then(() => {
        setConeEditOpen(false);
        return refresh();
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? `Cone save failed: ${err.message}` : 'Cone save failed');
      })
      .finally(() => setBusy(false));
  }

  function swapRelic(slot: string, uid: string) {
    const uids = equipped
      .filter((r) => r.slot !== slot)
      .map((r) => r.uid);
    uids.push(uid);
    setRelicPickSlot(null);
    void run('Relic swap failed', () => equipRelics(config, character.id, uids));
  }

  function openRelicEdit(relic: Relic) {
    setRelicEditUid(relic.uid);
    setRelicForm({
      mainstat: relic.mainstat,
      level: String(relic.level),
      lock: relic.lock,
      subs: relic.substats.map((s) => ({ key: s.key, value: s.value.toFixed(2) })),
    });
  }

  function saveRelicEdit() {
    if (!relicEditUid) return;
    const current = equipped.find((r) => r.uid === relicEditUid);
    const subs = relicForm.subs
      .filter((s) => s.key)
      .map((s, i) => ({
        key: s.key,
        value: num(s.value, current?.substats[i]?.value ?? 0),
        count: 1,
        step: 1,
      }));
    setBusy(true);
    setError('');
    updateRelic(config, relicEditUid, {
      mainstat: relicForm.mainstat,
      level: num(relicForm.level, current?.level ?? 0),
      lock: relicForm.lock,
      substats: subs,
    })
      .then(() => {
        setRelicEditUid(null);
        return refresh();
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? `Relic save failed: ${err.message}` : 'Relic save failed');
      })
      .finally(() => setBusy(false));
  }

  return (
    <div className="grid">
      {error && <div className="notice">{error}</div>}
      <div className={`hero rarity-${meta?.rarity ?? 5}`}>
        <div className="hero-art">
          <GameImage
            src={characterPortrait(character.id, meta)}
            alt={character.name}
            className="hero-portrait"
            fallback={character.name.slice(0, 2).toUpperCase()}
          />
        </div>
        <div className="hero-main">
          <div className="hero-title">
            <h2>{character.name}</h2>
            <span className="stars">{stars(meta?.rarity ?? 5)}</span>
          </div>
          <div className="chips">
            {meta && (
              <span className="chip">
                <GameImage src={elementIcon(meta.element)} alt={meta.element} className="mini-icon" fallback="◈" />
                <span className="chip-value">{meta.element}</span>
              </span>
            )}
            <span className="chip">
              <GameImage src={pathIcon(character.path)} alt={character.path} className="mini-icon" fallback="✦" />
              <span className="chip-value">{character.path}</span>
            </span>
            <StatChip label="Lv" value={String(character.level)} />
            <StatChip label="Asc" value={String(character.ascension)} />
            <StatChip label="Eidolon" value={`E${character.eidolon}`} />
            {inTeam && <span className="badge">IN TEAM</span>}
          </div>
          <div className="eido-pips" aria-label={`Eidolon ${character.eidolon}`}>
            {[1, 2, 3, 4, 5, 6].map((n) => (
              <span key={n} className={n <= character.eidolon ? 'pip on' : 'pip'} />
            ))}
          </div>
          <div className="conn">
            <button type="button" className="btn" disabled={busy} onClick={onToggleTeam}>
              {inTeam ? 'Remove from team' : 'Add to team'}
            </button>
            <button type="button" className="btn primary" disabled={busy} onClick={onOptimize}>
              Optimize
            </button>
            <button type="button" className="btn danger" disabled={busy} onClick={() => setConfirmDelete(true)}>
              Delete character
            </button>
          </div>
        </div>
      </div>

      <div className="panel-body panel">
        <div className="char-split">
          <div className="char-cone">
                      <h3>Light cone</h3>
                      {cone ? (
                        <div className={`cone-row rarity-${coneMeta[cone.id]?.rarity ?? 5}`}>
                          <button
                            type="button"
                            className="cone-row-art"
                            onClick={() => setConePickerOpen(true)}
                            title="Change light cone"
                          >
                            <GameImage
                              src={lightConePortrait(cone.id, coneMeta[cone.id])}
                              alt={cone.name}
                              className="cone-thumb"
                              fallback="LC"
                            />
                          </button>
                          <button
                            type="button"
                            className="cone-row-main"
                            onClick={() => setConePickerOpen(true)}
                            title="Change light cone"
                          >
                            <strong>{cone.name}</strong>
                            <div className="muted">
                              Lv {cone.level} · S{cone.superimposition} · tap to change
                            </div>
                          </button>
                          <button
                            type="button"
                            className="icon-btn"
                            title="Edit light cone"
                            onClick={() => setConeEditOpen(true)}
                          >
                            ✏️
                          </button>
                        </div>
                      ) : (
                        <button type="button" className="btn wide" disabled={busy} onClick={() => setConePickerOpen(true)}>
                          Choose light cone
                        </button>
                      )}
                      <div className="conn">
                        <button
                          type="button"
                          className="btn warn"
                          disabled={busy || !cone}
                          onClick={() => void run('Unequip failed', () => unequipLightCone(config, character.id))}
                        >
                          Unequip
                        </button>
                      </div>
                      {conePickerOpen && (
                        <div className="modal-overlay" onClick={() => setConePickerOpen(false)}>
                          <div className="modal modal-wide" onClick={(e) => e.stopPropagation()}>
                            <div className="panel-head">
                              <h2>Light cone inventory</h2>
                              <button type="button" className="btn" onClick={() => setConePickerOpen(false)}>
                                Close
                              </button>
                            </div>
                            <div className="panel-body">
                              <div className="conn">
                                <label className="field">
                                  Search
                                  <input
                                    value={coneFilter.q}
                                    onChange={(e) => setConeFilter((p) => ({ ...p, q: e.target.value }))}
                                    placeholder="Name or uid"
                                  />
                                </label>
                                <label className="field">
                                  Path
                                  <select
                                    value={coneFilter.path}
                                    onChange={(e) => setConeFilter((p) => ({ ...p, path: e.target.value }))}
                                  >
                                    <option value="all">All paths</option>
                                    {conePaths.map((p) => (
                                      <option key={p} value={p}>
                                        {p}
                                      </option>
                                    ))}
                                  </select>
                                </label>
                                <label className="field">
                                  Rarity
                                  <select
                                    value={coneFilter.rarity}
                                    onChange={(e) => setConeFilter((p) => ({ ...p, rarity: e.target.value }))}
                                  >
                                    <option value="all">All</option>
                                    <option value="5">5★</option>
                                    <option value="4">4★</option>
                                    <option value="3">3★</option>
                                  </select>
                                </label>
                                <label className="field">
                                  Holder
                                  <select
                                    value={coneFilter.holder}
                                    onChange={(e) => setConeFilter((p) => ({ ...p, holder: e.target.value }))}
                                  >
                                    <option value="all">All</option>
                                    <option value="free">Unequipped</option>
                                    <option value="equipped">Equipped</option>
                                  </select>
                                </label>
                                <label className="field">
                                  Sort by
                                  <select
                                    value={coneSort.key}
                                    onChange={(e) => setConeSort((p) => ({ ...p, key: e.target.value }))}
                                  >
                                    <option value="level">Level</option>
                                    <option value="rarity">Rarity</option>
                                    <option value="name">Name</option>
                                  </select>
                                </label>
                                <button
                                  type="button"
                                  className="btn"
                                  onClick={() => setConeSort((p) => ({ ...p, dir: p.dir === 'asc' ? 'desc' : 'asc' }))}
                                  title="Toggle sort direction"
                                >
                                  {coneSort.dir === 'asc' ? '↑' : '↓'}
                                </button>
                              </div>
                              <div
                                ref={picker.ref}
                                className="picker-grid"
                                style={{ gridTemplateColumns: `repeat(${picker.cols}, minmax(0, 1fr))` }}
                              >
                                {visibleCones.map((c) => (
                                  <button
                                    key={c.uid}
                                    type="button"
                                    className={`cone-card clickable rarity-${coneMeta[c.id]?.rarity ?? 4}${
                                      cone?.uid === c.uid ? ' selected' : ''
                                    }`}
                                    disabled={busy}
                                    onClick={() => {
                                      setConePickerOpen(false);
                                      void run('Equip failed', () => equipLightCone(config, character.id, c.uid));
                                    }}
                                  >
                                    <GameImage
                                      src={lightConePortrait(c.id, coneMeta[c.id])}
                                      alt={c.name}
                                      className="cone-art"
                                      fallback="LC"
                                    />
                                    <div className="cone-meta">
                                      <strong className="cone-name">{c.name}</strong>
                                      <div className="cone-stats">
                                        <span className="cone-level">Lv {c.level}</span>
                                        <span className="cone-super">S{c.superimposition}</span>
                                      </div>
                                      <div className="muted">
                                        {c.location ? characterNames[c.location] ?? c.location : 'free'}
                                        {c.lock ? ' · 🔒' : ''}
                                      </div>
                                    </div>
                                  </button>
                                ))}
                              </div>
                              {visibleCones.length === 0 && (
                                <p className="muted">
                                  {cones.length === 0 ? 'No light cones in inventory.' : 'No cones match the filters.'}
                                </p>
                              )}
                            </div>
                          </div>
                        </div>
                      )}
                      {coneEditOpen && cone && (
                        <div className="modal-overlay" onClick={() => setConeEditOpen(false)}>
                          <div className="modal modal-edit" onClick={(e) => e.stopPropagation()}>
                            <div className="panel-head">
                              <h2>Edit {cone.name}</h2>
                              <button type="button" className="btn" onClick={() => setConeEditOpen(false)}>
                                Close
                              </button>
                            </div>
                            <div className="panel-body">
                              <div className="form-grid">
                                <label className="field">
                                  Light cone
                                  <select value={coneIdentity} onChange={(e) => setConeIdentity(e.target.value)}>
                                    {Object.entries(coneMeta)
                                      .sort(([, a], [, b]) => a.name.localeCompare(b.name))
                                      .map(([id, m]) => (
                                        <option key={id} value={id}>
                                          {m.name}
                                        </option>
                                      ))}
                                  </select>
                                </label>
                                <label className="field">
                                  Cone level
                                  <input value={coneLevel} inputMode="numeric" onChange={(e) => setConeLevel(e.target.value)} />
                                </label>
                                <label className="field">
                                  Superimposition
                                  <input value={coneSuper} inputMode="numeric" onChange={(e) => setConeSuper(e.target.value)} />
                                </label>
                                <label className="field">
                                  Locked
                                  <input type="checkbox" checked={coneLock} onChange={(e) => setConeLock(e.target.checked)} />
                                </label>
                                <button
                                  type="button"
                                  className="btn primary wide"
                                  disabled={busy}
                                  onClick={applyConeEdit}
                                >
                                  Save cone
                                </button>
                              </div>
                            </div>
                          </div>
                        </div>
                      )}
          </div>
          <div className="char-details">
                      <h3>Details</h3>
                      <div className="form-grid">
                        <label className="field wide">
                          Name
                          <input value={name} onChange={(e) => setName(e.target.value)} />
                        </label>
                        <label className="field">
                          Path
                          <select value={charPath} onChange={(e) => setCharPath(e.target.value)}>
                            {CHARACTER_PATHS.map((value) => (
                              <option key={value} value={value}>
                                {value}
                              </option>
                            ))}
                          </select>
                        </label>
                        <label className="field">
                          Level
                          <input value={level} inputMode="numeric" onChange={(e) => setLevel(e.target.value)} />
                        </label>
                        <label className="field">
                          Ascension
                          <input value={ascension} inputMode="numeric" onChange={(e) => setAscension(e.target.value)} />
                        </label>
                        <label className="field">
                          Eidolon
                          <input value={eidolon} inputMode="numeric" onChange={(e) => setEidolon(e.target.value)} />
                        </label>
                        {(['basic', 'skill', 'ult', 'talent'] as const).map((k) => (
                          <label key={k} className="field">
                            {k[0].toUpperCase() + k.slice(1)}
                            <input
                              value={skills[k]}
                              inputMode="numeric"
                              onChange={(e) => setSkills((p) => ({ ...p, [k]: e.target.value }))}
                            />
                          </label>
                        ))}
                      </div>
                      <div className="conn">
                        <button type="button" className="btn primary wide" disabled={busy} onClick={saveDetails}>
                          Save details
                        </button>
                        <StatChip label="Traces" value={`${traces.on}/${traces.total}`} />
                      </div>
          </div>
        </div>
      </div>

      <div className="panel-body panel">
        <h3>Relics ({equipped.length}/6)</h3>
        {groups.length > 0 && (
          <div className="chips">
            {groups.map((g) => (
              <StatChip
                key={g.setId}
                label={setMeta[g.setId]?.name ?? g.setId}
                value={`${g.count}pc`}
              />
            ))}
          </div>
        )}
        <div className="conn">
          <button
            type="button"
            className="btn warn"
            disabled={busy || equipped.length === 0}
            onClick={() => void run('Unequip failed', () => unequipRelics(config, character.id))}
          >
            Unequip all
          </button>
        </div>
        {relicPickSlot && (
          <div className="modal-overlay" onClick={() => setRelicPickSlot(null)}>
            <div className="modal modal-wide" onClick={(e) => e.stopPropagation()}>
              <div className="panel-head">
                <h2>{RELIC_SLOT_LABEL[relicPickSlot] ?? relicPickSlot} relics</h2>
                <button type="button" className="btn" onClick={() => setRelicPickSlot(null)}>
                  Close
                </button>
              </div>
              <div className="panel-body">
                <div className="conn">
                  <label className="field">
                    Search
                    <input
                      value={relicFilter.q}
                      onChange={(e) => setRelicFilter((p) => ({ ...p, q: e.target.value }))}
                      placeholder="Set, stat, uid"
                    />
                  </label>
                  <label className="field">
                    Set
                    <select
                      value={relicFilter.setId}
                      onChange={(e) => setRelicFilter((p) => ({ ...p, setId: e.target.value }))}
                    >
                      <option value="all">All sets</option>
                      {pickSets.map((id) => (
                        <option key={id} value={id}>
                          {setMeta[id]?.name ?? id}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="field">
                    Main stat
                    <select
                      value={relicFilter.main}
                      onChange={(e) => setRelicFilter((p) => ({ ...p, main: e.target.value }))}
                    >
                      <option value="all">All</option>
                      {pickMains.map((m) => (
                        <option key={m} value={m}>
                          {m}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="field">
                    Holder
                    <select
                      value={relicFilter.holder}
                      onChange={(e) => setRelicFilter((p) => ({ ...p, holder: e.target.value }))}
                    >
                      <option value="all">All</option>
                      <option value="free">Unequipped</option>
                      <option value="equipped">Equipped</option>
                    </select>
                  </label>
                </div>
                <div
                  ref={picker.ref}
                  className="picker-grid"
                  style={{ gridTemplateColumns: `repeat(${picker.cols}, minmax(0, 1fr))` }}
                >
                  {pickCandidates.map((r) => (
                    <button
                      key={r.uid}
                      type="button"
                      className="relic-detail clickable"
                      disabled={busy}
                      onClick={() => swapRelic(relicPickSlot, r.uid)}
                    >
                      <div className="relic-top">
                        <GameImage
                          src={relicSetIcon(r.set_id, setMeta[r.set_id])}
                          alt={r.name}
                          className="relic-art"
                          fallback="RE"
                        />
                        <div className="relic-head">
                          <span className="muted">{setMeta[r.set_id]?.name ?? r.name}</span>
                          <strong className="relic-main">{r.mainstat}</strong>
                          <span className="relic-value">+{r.level}</span>
                        </div>
                      </div>
                      <div className="relic-subs">
                        {r.substats.map((s) => (
                          <div key={`${r.uid}-${s.key}`} className="relic-sub">
                            <span>{displayStat(s.key)}</span>
                            <span>{s.value.toFixed(2)}</span>
                          </div>
                        ))}
                      </div>
                      <div className="muted">
                        {r.location ? characterNames[r.location] ?? r.location : 'free'}
                        {r.lock ? ' · 🔒' : ''}
                      </div>
                    </button>
                  ))}
                </div>
                {pickCandidates.length === 0 && <p className="muted">No relics for this slot.</p>}
              </div>
            </div>
          </div>
        )}
        <div className="grid items">
          {equipped.map((r) => {
            return (
              <div key={r.uid} className="relic-detail">
                <div className="relic-top">
                  <button
                    type="button"
                    className="relic-swap"
                    onClick={() => setRelicPickSlot(r.slot)}
                    title="Change relic"
                  >
                    <GameImage
                      src={relicSetIcon(r.set_id, setMeta[r.set_id])}
                      alt={r.name}
                      className="relic-art"
                      fallback="RE"
                    />
                  </button>
                  <button
                    type="button"
                    className="relic-swap relic-head-btn"
                    onClick={() => setRelicPickSlot(r.slot)}
                    title="Change relic"
                  >
                    <span className="muted">
                      {(RELIC_SLOT_LABEL[r.slot] ?? r.slot)} · {setMeta[r.set_id]?.name ?? r.name}
                    </span>
                    <strong className="relic-main">{r.mainstat}</strong>
                    <span className="relic-value">+{r.level}</span>
                  </button>
                  <button
                    type="button"
                    className="icon-btn"
                    title="Edit relic"
                    onClick={() => openRelicEdit(r)}
                  >
                    ✏️
                  </button>
                </div>
                <div className="relic-subs">
                  {r.substats.map((s) => (
                    <div key={`${r.uid}-${s.key}`} className="relic-sub">
                      <span>{displayStat(s.key)}</span>
                      <span>{s.value.toFixed(2)}</span>
                    </div>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
        {equipped.length === 0 && <p className="muted">No relics equipped.</p>}
        {relicEditUid && (
          <div className="modal-overlay" onClick={() => setRelicEditUid(null)}>
            <div className="modal modal-edit" onClick={(e) => e.stopPropagation()}>
              <div className="panel-head">
                <h2>Edit relic</h2>
                <button type="button" className="btn" onClick={() => setRelicEditUid(null)}>
                  Close
                </button>
              </div>
              <div className="panel-body">
                <div className="form-grid">
                  <label className="field">
                    Main stat
                    <select
                      value={relicForm.mainstat}
                      onChange={(e) => setRelicForm((p) => ({ ...p, mainstat: e.target.value }))}
                    >
                      {RELIC_MAIN_STATS.map((m) => (
                        <option key={m} value={m}>
                          {m}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="field">
                    Level
                    <input
                      value={relicForm.level}
                      inputMode="numeric"
                      onChange={(e) => setRelicForm((p) => ({ ...p, level: e.target.value }))}
                    />
                  </label>
                  <label className="field">
                    Locked
                    <input
                      type="checkbox"
                      checked={relicForm.lock}
                      onChange={(e) => setRelicForm((p) => ({ ...p, lock: e.target.checked }))}
                    />
                  </label>
                </div>
                <h3>Sub stats</h3>
                <div className="grid">
                  {relicForm.subs.map((s, i) => (
                    <div key={i} className="form-grid">
                      <label className="field">
                        Stat
                        <select
                          value={s.key}
                          onChange={(e) =>
                            setRelicForm((p) => ({
                              ...p,
                              subs: p.subs.map((x, j) => (j === i ? { ...x, key: e.target.value } : x)),
                            }))
                          }
                        >
                          {RELIC_SUBSTAT_KEYS.map((k) => (
                            <option key={k} value={k}>
                              {displayStat(k)}
                            </option>
                          ))}
                        </select>
                      </label>
                      <label className="field">
                        Value
                        <input
                          value={s.value}
                          inputMode="decimal"
                          onChange={(e) =>
                            setRelicForm((p) => ({
                              ...p,
                              subs: p.subs.map((x, j) => (j === i ? { ...x, value: e.target.value } : x)),
                            }))
                          }
                        />
                      </label>
                      <button
                        type="button"
                        className="btn warn"
                        disabled={busy}
                        onClick={() =>
                          setRelicForm((p) => ({ ...p, subs: p.subs.filter((_, j) => j !== i) }))
                        }
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
                <div className="conn">
                  <button
                    type="button"
                    className="btn"
                    disabled={busy}
                    onClick={() => setRelicForm((p) => ({ ...p, subs: [...p.subs, { key: 'ATK_', value: '' }] }))}
                  >
                    Add sub stat
                  </button>
                  <button
                    type="button"
                    className="btn primary wide"
                    disabled={busy}
                    onClick={saveRelicEdit}
                  >
                    Save relic
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
      <ConfirmDialog
        open={confirmDelete}
        title="Delete character?"
        message={`${character.name} will be removed from the roster, their gear will be unequipped, and they will leave the saved team.`}
        busy={busy}
        onCancel={() => setConfirmDelete(false)}
        onConfirm={() => {
          setConfirmDelete(false);
          onDelete();
        }}
      />
    </div>
  );
}
