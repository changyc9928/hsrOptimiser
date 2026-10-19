import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  ApiError,
  addCharacter,
  addLightCone,
  addRelic,
  getJobResult,
  getJobStatus,
  getLoadout,
  getScanned,
  getTeam,
  issueToken,
  saveTeam,
  startEvaluation,
  uploadScan,
  type BackendConfig,
} from './api/client.ts';
import type {
  CharacterDamage,
  EvaluationResult,
  HSRCharacter,
  JobProgress,
  JobStatus,
  LightCone,
  Loadout,
  Relic,
  ScannedDataSize,
} from './api/types.ts';
import {
  RELIC_SLOT_LABEL,
  characterIcon,
  characterPortrait,
  formatDamage,
  getGameData,
  normalizeName,
} from './api/gameData.ts';
import { CharacterCard, LightConeCard, RelicCard } from './components/cards.tsx';
import { CharacterPreview } from './components/CharacterPreview.tsx';
import { TeamPage } from './components/TeamPage.tsx';
import { OptimizerChart, type ProgressSample } from './components/OptimizerChart.tsx';
import { CharacterResultCard } from './components/CharacterResultCard.tsx';
import { GameImage, ProgressBar, Section, StatChip } from './components/ui.tsx';

type Tab = 'scanner' | 'roster' | 'character' | 'team' | 'relics' | 'optimizer';

interface MetaMaps {
  characters: Record<string, { rarity: number; element: string; portrait: string; icon: string }>;
  lightCones: Record<string, { rarity: number; icon: string; portrait: string; path: string; name: string }>;
  relicSets: Record<string, { name: string; icon: string; desc: string[] }>;
}

function useStored(key: string, initial: string): [string, (value: string) => void] {
  const [value, setValue] = useState(() => window.localStorage.getItem(key) ?? initial);
  useEffect(() => {
    window.localStorage.setItem(key, value);
  }, [key, value]);
  return [value, setValue];
}

  function messageOf(error: unknown): string {
    if (error instanceof Error) return error.message;
    return 'Unexpected error';
  }

function progressPercent(progress: JobProgress): number {
  if (progress.total_epochs && progress.current_epoch !== undefined) {
    return (progress.current_epoch / Math.max(1, progress.total_epochs)) * 100;
  }
  return 0;
}

export default function App() {
  const [tab, setTab] = useState<Tab>('scanner');
  const [inventoryBase] = useStored(
    'hsr.inventoryBase',
    (import.meta.env.VITE_INVENTORY_URL as string | undefined) ?? '/api/inventory',
  );
  const [engineBase] = useStored(
    'hsr.engineBase',
    (import.meta.env.VITE_ENGINE_URL as string | undefined) ?? '/api/engine',
  );
  const [token, setToken] = useStored('hsr.token', '');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [meta, setMeta] = useState<MetaMaps | null>(null);

  const [roster, setRoster] = useState<HSRCharacter[]>([]);
  const [cones, setCones] = useState<LightCone[]>([]);
  const [relics, setRelics] = useState<Relic[]>([]);
  const [lastScan, setLastScan] = useState<ScannedDataSize | null>(null);
  const [team, setTeam] = useState<HSRCharacter[]>([]);
  const [draftTeam, setDraftTeam] = useState<string[]>([]);

  const [query, setQuery] = useState('');
  const [pathFilter, setPathFilter] = useState('all');
  const [detailId, setDetailId] = useState('');
  const [relicQuery, setRelicQuery] = useState('');
  const [relicSlot, setRelicSlot] = useState('all');
  const [relicWhere, setRelicWhere] = useState('all');
  const [inventoryTab, setInventoryTab] = useState<'relics' | 'cones'>('relics');

  const [teamLoadouts, setTeamLoadouts] = useState<Record<string, Loadout>>({});

  const [jobId, setJobId] = useState('');
  const [watchId, setWatchId] = useState('');
  const [jobStatus, setJobStatus] = useState<JobStatus | null>(null);
  const [jobHistory, setJobHistory] = useState<ProgressSample[]>([]);
  const [result, setResult] = useState<EvaluationResult | null>(null);

  const config: BackendConfig = useMemo(
    () => ({ inventoryBase, engineBase, token }),
    [inventoryBase, engineBase, token],
  );

  useEffect(() => {
    getGameData()
      .then(setMeta)
      .catch(() => setMeta(null));
  }, []);

  useEffect(() => {
    if (token) return;
    let cancelled = false;
    issueToken(inventoryBase)
      .then((next) => {
        if (cancelled) return;
        setToken(next);
        setNotice('Access token issued automatically for 24h.');
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(messageOf(err));
      });
    return () => {
      cancelled = true;
    };
  }, [inventoryBase, token, setToken]);

  const autoLoadedFor = useRef('');

  // Sessions expire after 24h. Dropping the token lets the auto-issue
  // effect above fetch a fresh one and resume.
  const renewSession = useCallback(() => {
    autoLoadedFor.current = '';
    setToken('');
  }, [setToken]);

  const refreshAll = useCallback(async () => {
    if (!token) {
      setError('Session is starting — try again in a moment.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      let hasScan = true;
      try {
        const scanned = await getScanned(config);
        setRoster(scanned.characters ?? []);
        setCones(scanned.light_cones ?? []);
        setRelics(scanned.relics ?? []);
        if (!detailId && scanned.characters?.length) setDetailId(scanned.characters[0].id);
      } catch (err) {
        if (err instanceof ApiError && err.status === 404) {
          hasScan = false;
          setRoster([]);
          setCones([]);
          setRelics([]);
          setNotice('No scan uploaded yet. Import scanner JSON to begin.');
        } else {
          throw err;
        }
      }
      const currentTeam = await getTeam(config);
      setTeam(currentTeam ?? []);
      if (hasScan && draftTeam.length === 0 && currentTeam?.length) {
        setDraftTeam(currentTeam.map((c) => c.id));
      }
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setNotice('Session expired — renewing…');
        renewSession();
      } else {
        setError(messageOf(err));
      }
    } finally {
      setBusy(false);
    }
  }, [config, token, detailId, draftTeam.length, renewSession]);

  useEffect(() => {
    if (!token || autoLoadedFor.current === token) return;
    autoLoadedFor.current = token;
    void refreshAll();
  }, [token, refreshAll]);

  useEffect(() => {
    if (!jobId) return;
    let cancelled = false;
    const poll = async () => {
      try {
        const status = await getJobStatus(config, jobId);
        if (cancelled) return;
        setJobStatus(status);
        // Polls land every 2s but epochs advance independently, so fold each
        // poll into the sample for its epoch instead of appending duplicates.
        const { current_epoch: epoch, temperature, current_damage: damage } = status.progress;
        if (typeof epoch === 'number') {
          setJobHistory((prev) => {
            const next = prev.filter((s) => s.epoch !== epoch);
            next.push({ epoch, temperature: temperature ?? 0, damage: damage ?? 0 });
            next.sort((a, b) => a.epoch - b.epoch);
            return next;
          });
        }
        if (status.status === 'finished') {
          const finished = await getJobResult(config, jobId);
          if (!cancelled) setResult(finished);
        }
        if (status.status === 'finished' || status.status === 'failed') {
          setWatchId('');
        }
      } catch (err) {
        if (cancelled) return;
        if (err instanceof ApiError && err.status === 401) {
          setNotice('Session expired — renewing…');
          renewSession();
        } else {
          setError(messageOf(err));
        }
      }
    };
    void poll();
    const timer = window.setInterval(poll, 2000);
    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
  }, [jobId, config, renewSession]);

  useEffect(() => {
    // A new job must not inherit the previous run's curve.
    setJobHistory([]);
    setResult(null);
    setJobStatus(null);
  }, [jobId]);

  const paths = useMemo(() => Array.from(new Set(roster.map((c) => c.path))).sort(), [roster]);
  const filteredRoster = useMemo(() => {
    const q = query.trim().toLowerCase();
    return roster.filter((c) => {
      if (pathFilter !== 'all' && c.path !== pathFilter) return false;
      if (!q) return true;
      return `${c.name} ${c.id} ${c.path}`.toLowerCase().includes(q);
    });
  }, [roster, query, pathFilter]);
  const detail = useMemo(() => roster.find((c) => c.id === detailId), [roster, detailId]);
  const filteredRelics = useMemo(() => {
    const q = relicQuery.trim().toLowerCase();
    return relics.filter((r) => {
      if (relicSlot !== 'all' && r.slot !== relicSlot) return false;
      if (relicWhere === 'equipped' && !r.location) return false;
      if (relicWhere === 'free' && r.location) return false;
      if (!q) return true;
      return `${r.name} ${r.uid} ${r.mainstat} ${r.set_id}`.toLowerCase().includes(q);
    });
  }, [relics, relicQuery, relicSlot, relicWhere]);
  const sortedDamages = useMemo(() => {
    const rows = [...(result?.characterDamage ?? [])] as CharacterDamage[];
    return rows.sort((a, b) => b.totalDamage - a.totalDamage);
  }, [result]);

  // CharacterDamage only carries a name, so build a lookup from the roster.
  // Names differ between the scan and the result payload ("YaoGuang" vs
  // "Yao Guang"), so key on a normalised name, and keep the character id as a
  // fallback for characters missing from the game-data index (e.g. Trailblazer).
  const rosterEntryByName = useMemo(() => {
    const map: Record<string, { id: string; meta?: MetaMaps['characters'][string] }> = {};
    for (const character of roster) {
      map[normalizeName(character.name)] = {
        id: character.id,
        meta: meta?.characters[character.id],
      };
    }
    return map;
  }, [roster, meta]);

  async function handleUpload(file: File | undefined) {
    if (!file) return;
    setBusy(true);
    setError('');
    try {
      const size = await uploadScan(config, file);
      setLastScan(size);
      setNotice(`Imported ${size.numCharacters} characters, ${size.numLightCones} cones, ${size.numRelics} relics.`);
      await refreshAll();
    } catch (err) {
      setError(messageOf(err));
    } finally {
      setBusy(false);
    }
  }

  function toggleDraft(id: string) {
    setDraftTeam((prev) => {
      if (prev.includes(id)) return prev.filter((x) => x !== id);
      if (prev.length >= 4) return prev;
      return [...prev, id];
    });
  }

  async function toggleTeamMembership(id: string) {
    const ids = team.some((t) => t.id === id)
      ? team.filter((t) => t.id !== id).map((t) => t.id)
      : [...team.map((t) => t.id), id];
    if (ids.length > 4) {
      setError('Team can contain at most 4 characters.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const saved = await saveTeam(config, ids);
      setTeam(saved);
      setDraftTeam(ids);
      setNotice('Team saved and cached for the optimizer.');
    } catch (err) {
      setError(messageOf(err));
    } finally {
      setBusy(false);
    }
  }

  async function handleSaveTeam() {
    setBusy(true);
    setError('');
    try {
      const saved = await saveTeam(config, draftTeam);
      setTeam(saved);
      setNotice('Team saved and cached for the optimizer.');
    } catch (err) {
      setError(messageOf(err));
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => {
    if (tab !== 'team' || !token || team.length === 0) return;
    let cancelled = false;
    void (async () => {
      try {
        const entries = await Promise.all(
          team.map(async (c) => [c.id, await getLoadout(config, c.id)] as const),
        );
        if (!cancelled) setTeamLoadouts(Object.fromEntries(entries));
      } catch (err) {
        if (!cancelled) setError(messageOf(err));
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [tab, team, config, token]);

  async function handleStart() {
    setBusy(true);
    setError('');
    setResult(null);
    try {
      const currentTeam = await getTeam(config);
      setTeam(currentTeam ?? []);
      if (!currentTeam?.length) {
        throw new Error('Save a team before starting optimization.');
      }
      const queued = await startEvaluation(config);
      setJobId(queued.jobId);
      setWatchId(queued.jobId);
      setNotice(`Optimization queued as ${queued.jobId}.`);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setNotice('Session expired — renewing…');
        renewSession();
      } else {
        setError(messageOf(err));
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="app-shell">
      <aside className="side">
        <div className="brand">
          <h1>HSR Optimizer</h1>
          <p>Relic optimizer · Genshin-Optimizer style</p>
        </div>
        <nav className="nav">
          {(
            [
              ['scanner', 'Scanner'],
              ['roster', 'Characters'],
              ['team', 'Team'],
              ['relics', 'Inventory'],
              ['optimizer', 'Optimizer'],
            ] as Array<[Tab, string]>
          ).map(([value, label]) => (
            <button key={value} type="button" className={tab === value ? 'active' : ''} onClick={() => setTab(value)}>
              {label}
            </button>
          ))}
          {detailId && (
            <button
              key="character"
              type="button"
              className={tab === 'character' ? 'active' : ''}
              onClick={() => setTab('character')}
            >
              Character
            </button>
          )}
        </nav>
        <div className="side-foot">Images: StarRailRes · Engine: simulated annealing</div>
      </aside>

      <main className="main">
        <div className="topbar">
          <div>
            <h2>
              {tab === 'scanner' && 'Scan import'}
              {tab === 'roster' && 'Character roster'}
              {tab === 'character' && 'Character preview'}
              {tab === 'team' && 'Team lineup'}
              {tab === 'relics' && 'Relics & light cones'}
              {tab === 'optimizer' && 'Damage optimizer'}
            </h2>
            <p>Inventory API plus the async SA engine, in one workspace.</p>
          </div>
        </div>

        {error && <div className="notice">{error}</div>}
        {notice && <div className="notice good">{notice}</div>}

        {tab === 'scanner' && (
          <Section
            title="HSR scanner import"
            action={
              <label className="btn primary">
                Choose scan JSON
                <input
                  type="file"
                  accept="application/json"
                  hidden
                  onChange={(e) => {
                    void handleUpload(e.target.files?.[0]);
                    e.target.value = '';
                  }}
                />
              </label>
            }
          >
            <p className="muted">
              Upload the scanner JSON. Counts come straight from <code>/data/upload</code>, then the roster reloads from{' '}
              <code>/data/scanned</code>.
            </p>
            {lastScan && (
              <div className="chips">
                <StatChip label="Characters" value={String(lastScan.numCharacters)} />
                <StatChip label="Light cones" value={String(lastScan.numLightCones)} />
                <StatChip label="Relics" value={String(lastScan.numRelics)} />
              </div>
            )}
            <AddForms config={config} busy={busy} onDone={refreshAll} onError={setError} />
          </Section>
        )}

        {tab === 'roster' && (
          <Section
            title={`Owned characters (${filteredRoster.length})`}
            action={
              <div className="conn">
                <label className="field">
                  Search
                  <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Name or id" />
                </label>
                <label className="field">
                  Path
                  <select value={pathFilter} onChange={(e) => setPathFilter(e.target.value)}>
                    <option value="all">All</option>
                    {paths.map((p) => (
                      <option key={p} value={p}>
                        {p}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
            }
          >
            <div className="grid chars">
              {filteredRoster.map((c) => (
                <CharacterCard
                  key={c.id}
                  character={c}
                  meta={meta?.characters[c.id]}
                  selected={detailId === c.id}
                  onSelect={() => {
                    setDetailId(c.id);
                    setTab('character');
                  }}
                  badge={team.some((t) => t.id === c.id) ? 'TEAM' : undefined}
                />
              ))}
            </div>
            {detail && (
              <div className="grid two">
                <div className="row-card">
                  <GameImage
                    src={characterPortrait(detail.id, meta?.characters[detail.id])}
                    alt={detail.name}
                    className="thumb"
                    fallback={detail.name.slice(0, 1)}
                  />
                  <div>
                    <strong>{detail.name}</strong>
                    <div className="muted">
                      {detail.id} · Lv {detail.level} · A{detail.ascension} · E{detail.eidolon}
                    </div>
                    <div className="chips">
                      <StatChip label="Basic" value={String(detail.skills.basic)} />
                      <StatChip label="Skill" value={String(detail.skills.skill)} />
                      <StatChip label="Ult" value={String(detail.skills.ult)} />
                      <StatChip label="Talent" value={String(detail.skills.talent)} />
                    </div>
                  </div>
                </div>
                <div className="row-card">
                  <GameImage
                    src={characterIcon(detail.id, meta?.characters[detail.id])}
                    alt={detail.name}
                    className="thumb"
                    fallback={detail.name.slice(0, 1)}
                  />
                  <div>
                    <strong>Scanner record</strong>
                    <div className="muted">Path {detail.path} · ability v{detail.ability_version}</div>
                    <div className="muted">
                      Equipped relics: {relics.filter((r) => r.location === detail.id).length} · Light cone:{' '}
                      {cones.find((c) => c.location === detail.id)?.name ?? 'none'}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </Section>
        )}

        {tab === 'character' && detail && (
          <Section
            title={detail.name}
            action={
              <button type="button" className="btn" onClick={() => setTab('roster')}>
                Back to roster
              </button>
            }
          >
            <CharacterPreview
              config={config}
              character={detail}
              meta={meta?.characters[detail.id]}
              setMeta={meta?.relicSets ?? {}}
              coneMeta={meta?.lightCones ?? {}}
              characterNames={Object.fromEntries(roster.map((c) => [c.id, c.name]))}
              cones={cones}
              allRelics={relics}
              inTeam={team.some((t) => t.id === detail.id)}
              onToggleTeam={() => void toggleTeamMembership(detail.id)}
              onOptimize={() => setTab('optimizer')}
              onChanged={() => refreshAll()}
            />
          </Section>
        )}

        {tab === 'team' && (
          <Section title="Team loadout">
            <TeamPage
              team={team}
              draftIds={draftTeam}
              roster={roster}
              meta={meta?.characters ?? {}}
              setNames={Object.fromEntries(
                Object.entries(meta?.relicSets ?? {}).map(([id, s]) => [id, s.name]),
              )}
              loadouts={teamLoadouts}
              result={result}
              busy={busy}
              onToggleDraft={toggleDraft}
              onSave={() => void handleSaveTeam()}
              onEditCurrent={() => setDraftTeam(team.map((c) => c.id))}
              onViewCharacter={(id) => {
                setDetailId(id);
                setTab('character');
              }}
              onManageLoadout={(id) => {
                setDetailId(id);
                setTab('character');
              }}
              onOptimize={() => setTab('optimizer')}
            />
          </Section>
        )}

        {tab === 'relics' && (
          <Section
            title="Inventory"
            action={
              <div className="conn">
                <button type="button" className={`btn ${inventoryTab === 'relics' ? 'primary' : ''}`} onClick={() => setInventoryTab('relics')}>
                  Relics
                </button>
                <button type="button" className={`btn ${inventoryTab === 'cones' ? 'primary' : ''}`} onClick={() => setInventoryTab('cones')}>
                  Light cones
                </button>
              </div>
            }
          >
            {inventoryTab === 'relics' ? (
              <>
                <div className="conn">
                  <label className="field">
                    Search
                    <input value={relicQuery} onChange={(e) => setRelicQuery(e.target.value)} placeholder="Set, stat, uid" />
                  </label>
                  <label className="field">
                    Slot
                    <select value={relicSlot} onChange={(e) => setRelicSlot(e.target.value)}>
                      <option value="all">All</option>
                      {Object.entries(RELIC_SLOT_LABEL).map(([value, label]) => (
                        <option key={value} value={value}>
                          {label}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="field">
                    Status
                    <select value={relicWhere} onChange={(e) => setRelicWhere(e.target.value)}>
                      <option value="all">All</option>
                      <option value="equipped">Equipped</option>
                      <option value="free">Unequipped</option>
                    </select>
                  </label>
                </div>
                <div className="grid items">
                  {filteredRelics.map((r) => (
                    <RelicCard key={r.uid} relic={r} setName={meta?.relicSets[r.set_id]?.name} />
                  ))}
                </div>
              </>
            ) : (
              <div className="grid items">
                {cones.map((cone) => (
                  <LightConeCard key={cone.uid} cone={cone} meta={meta?.lightCones[cone.id]} />
                ))}
              </div>
            )}
          </Section>
        )}


        {tab === 'optimizer' && (
          <Section
            title="Simulated annealing"
            action={<button type="button" className="btn primary" disabled={busy} onClick={handleStart}>Start optimization</button>}
          >
            <div className="conn">
              <label className="field">
                Watch job id
                <input value={watchId} onChange={(e) => setWatchId(e.target.value)} placeholder="Paste job id" />
              </label>
              <button
                type="button"
                className="btn"
                onClick={() => {
                  if (watchId.trim()) setJobId(watchId.trim());
                }}
              >
                Watch
              </button>
              {jobId && <StatChip label="Job" value={jobId} />}
            </div>
            {jobStatus && (
              <>
                <div className="chips">
                  <StatChip label="Status" value={jobStatus.status} />
                  <StatChip label="Epoch" value={`${jobStatus.progress.current_epoch ?? 0}/${jobStatus.progress.total_epochs ?? 0}`} />
                  <StatChip label="Damage" value={formatDamage(jobStatus.progress.current_damage ?? 0)} />
                  {(jobStatus.progress.best_damage ?? 0) > 0 && (
                    <StatChip label="Best" value={formatDamage(jobStatus.progress.best_damage ?? 0)} />
                  )}
                  <StatChip label="Temp" value={String(Math.round(jobStatus.progress.temperature ?? 0))} />
                  {jobHistory.length > 1 ? <StatChip label="Samples" value={String(jobHistory.length)} /> : null}
                </div>
                <ProgressBar value={progressPercent(jobStatus.progress)} />
                {jobStatus.status === 'failed' && <div className="notice">{jobStatus.error ?? 'Job failed'}</div>}
              </>
            )}

            {jobHistory.length > 0 && <OptimizerChart samples={jobHistory} />}

            {result && (
              <>
                <div className="result-total">
                  <span className="muted">Total team damage</span>
                  <strong>{formatDamage(result.totalDamage)}</strong>
                </div>
                <div className="grid results">
                  {sortedDamages.map((row, index) => {
                    const entry = rosterEntryByName[normalizeName(row.name)];
                    return (
                      <CharacterResultCard
                        key={row.name}
                        row={row}
                        total={result.totalDamage}
                        rank={index + 1}
                        characterId={entry?.id}
                        meta={entry?.meta}
                        relicSets={meta?.relicSets ?? {}}
                      />
                    );
                  })}
                </div>
              </>
            )}
          </Section>
        )}
      </main>
    </div>
  );
}

function AddForms({
  config,
  busy,
  onDone,
  onError,
}: {
  config: BackendConfig;
  busy: boolean;
  onDone: () => Promise<void>;
  onError: (message: string) => void;
}) {
  const [charId, setCharId] = useState('');
  const [coneId, setConeId] = useState('');
  const [setId, setSetId] = useState('');
  const [slot, setSlot] = useState('Body');

  async function submit(kind: 'character' | 'cone' | 'relic') {
    try {
      if (kind === 'character') {
        if (!charId.trim()) throw new Error('Character id is required');
        await addCharacter(config, { id: charId.trim(), level: 80, eidolon: 0 });
      } else if (kind === 'cone') {
        if (!coneId.trim()) throw new Error('Light cone id is required');
        await addLightCone(config, { id: coneId.trim(), level: 80, superimposition: 1 });
      } else {
        if (!setId.trim()) throw new Error('Relic set id is required');
        await addRelic(config, {
          setId: setId.trim(),
          slot,
          rarity: 5,
          level: 15,
          mainstat: 'CRIT DMG',
          substats: [],
        });
      }
      await onDone();
    } catch (err) {
      onError(err instanceof Error ? err.message : 'Add failed');
    }
  }

  return (
    <div className="grid two">
      <div className="form-grid">
        <label className="field">Character id<input value={charId} onChange={(e) => setCharId(e.target.value)} placeholder="1310" /></label>
        <button type="button" className="btn" disabled={busy} onClick={() => void submit('character')}>Add character</button>
        <label className="field">Light cone id<input value={coneId} onChange={(e) => setConeId(e.target.value)} placeholder="23000" /></label>
        <button type="button" className="btn" disabled={busy} onClick={() => void submit('cone')}>Add cone</button>
      </div>
      <div className="form-grid">
        <label className="field">Relic set id<input value={setId} onChange={(e) => setSetId(e.target.value)} placeholder="129" /></label>
        <label className="field">
          Slot
          <select value={slot} onChange={(e) => setSlot(e.target.value)}>
            {Object.entries(RELIC_SLOT_LABEL).map(([value, label]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
        </label>
        <button type="button" className="btn wide" disabled={busy} onClick={() => void submit('relic')}>Add relic shell</button>
      </div>
    </div>
  );
}
