import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  ApiError,
  deleteCharacter,
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
  EvaluationResult,
  HSRCharacter,
  JobStatus,
  LightCone,
  Loadout,
  Relic,
  ScannedDataSize,
} from './api/types.ts';
import { type GameData, formatDamage, getGameData } from './api/gameData.ts';
import { CharacterPreview } from './components/CharacterPreview.tsx';
import { CharacterRoster } from './components/CharacterRoster.tsx';
import { ImportModal } from './components/ImportModal.tsx';
import { LightConeInventory } from './components/LightConeInventory.tsx';
import { OptimizerPage } from './components/OptimizerPage.tsx';
import { RelicInventory } from './components/RelicInventory.tsx';
import { TeamPage } from './components/TeamPage.tsx';
import { EmptyState, PageHeader, ToastViewport, type Toast } from './components/ui.tsx';

type Page = 'characters' | 'relics' | 'cones' | 'team' | 'optimizer';

const PAGES: Array<{ id: Page; label: string }> = [
  { id: 'characters', label: 'Characters' },
  { id: 'relics', label: 'Relics' },
  { id: 'cones', label: 'Light Cones' },
  { id: 'team', label: 'Team' },
  { id: 'optimizer', label: 'Optimizer' },
];

const LAST_JOB_KEY = 'hsr.lastJobId';

function useStored(key: string, initial: string): [string, (value: string) => void] {
  const [value, setValue] = useState(() => window.localStorage.getItem(key) ?? initial);
  useEffect(() => {
    window.localStorage.setItem(key, value);
  }, [key, value]);
  return [value, setValue];
}

function messageOf(error: unknown): string {
  return error instanceof Error ? error.message : 'Unexpected error';
}

export default function App() {
  const [page, setPage] = useState<Page>('characters');
  const [inventoryBase] = useStored(
    'hsr.inventoryBase',
    (import.meta.env.VITE_INVENTORY_URL as string | undefined) ?? '/api/inventory',
  );
  const [engineBase] = useStored(
    'hsr.engineBase',
    (import.meta.env.VITE_ENGINE_URL as string | undefined) ?? '/api/engine',
  );
  const [token, setToken] = useStored('hsr.token', '');

  const [game, setGame] = useState<GameData | null>(null);
  const [roster, setRoster] = useState<HSRCharacter[]>([]);
  const [cones, setCones] = useState<LightCone[]>([]);
  const [relics, setRelics] = useState<Relic[]>([]);
  const [team, setTeam] = useState<HSRCharacter[]>([]);
  const [draftTeam, setDraftTeam] = useState<string[]>([]);
  const [teamLoadouts, setTeamLoadouts] = useState<Record<string, Loadout>>({});
  const [lastScan, setLastScan] = useState<ScannedDataSize | null>(null);
  const [hasScan, setHasScan] = useState(false);
  const [initialized, setInitialized] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [busy, setBusy] = useState(false);
  const [detailId, setDetailId] = useState('');
  const [importOpen, setImportOpen] = useState(false);

  const [jobId, setJobId] = useState(() => window.localStorage.getItem(LAST_JOB_KEY) ?? '');
  const [jobStatus, setJobStatus] = useState<JobStatus | null>(null);
  const [jobHistory, setJobHistory] = useState<{ epoch: number; temperature: number; damage: number }[]>([]);
  const [result, setResult] = useState<EvaluationResult | null>(null);

  const [toasts, setToasts] = useState<Toast[]>([]);
  const toastSeq = useRef(0);

  const notify = useCallback((text: string, kind: Toast['kind'] = 'info') => {
    const id = ++toastSeq.current;
    setToasts((prev) => [...prev, { id, kind, text }]);
    window.setTimeout(() => {
      setToasts((prev) => prev.filter((toast) => toast.id !== id));
    }, 5000);
  }, []);

  const config: BackendConfig = useMemo(
    () => ({ inventoryBase, engineBase, token }),
    [inventoryBase, engineBase, token],
  );

  const characterNames = useMemo(
    () => Object.fromEntries(roster.map((character) => [character.id, character.name])),
    [roster],
  );

  useEffect(() => {
    getGameData()
      .then(setGame)
      .catch(() => setGame(null));
  }, []);

  useEffect(() => {
    if (token) return;
    let cancelled = false;
    issueToken(inventoryBase)
      .then((next) => {
        if (!cancelled) setToken(next);
      })
      .catch((err: unknown) => {
        if (!cancelled) notify(messageOf(err), 'error');
      });
    return () => {
      cancelled = true;
    };
  }, [inventoryBase, token, setToken, notify]);

  const autoLoadedFor = useRef('');

  const renewSession = useCallback(() => {
    autoLoadedFor.current = '';
    setToken('');
  }, [setToken]);

  const refreshAll = useCallback(async () => {
    if (!token) return;
    setRefreshing(true);
    try {
      let scannedFound = true;
      try {
        const scanned = await getScanned(config);
        setRoster(scanned.characters ?? []);
        setCones(scanned.light_cones ?? []);
        setRelics(scanned.relics ?? []);
        setHasScan(true);
        setLastScan({
          numCharacters: scanned.characters?.length ?? 0,
          numLightCones: scanned.light_cones?.length ?? 0,
          numRelics: scanned.relics?.length ?? 0,
        });
      } catch (err) {
        if (err instanceof ApiError && err.status === 404) {
          scannedFound = false;
          setHasScan(false);
          setRoster([]);
          setCones([]);
          setRelics([]);
        } else {
          throw err;
        }
      }
      if (scannedFound) {
        const currentTeam = await getTeam(config);
        setTeam(currentTeam ?? []);
        setDraftTeam((currentTeam ?? []).map((character) => character.id));
      } else {
        setTeam([]);
        setDraftTeam([]);
      }
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        renewSession();
      } else {
        notify(messageOf(err), 'error');
      }
    } finally {
      setRefreshing(false);
      setInitialized(true);
    }
  }, [config, token, renewSession, notify]);

  useEffect(() => {
    if (!token || autoLoadedFor.current === token) return;
    autoLoadedFor.current = token;
    void refreshAll();
  }, [token, refreshAll]);

  useEffect(() => {
    if (!token || team.length === 0) {
      setTeamLoadouts({});
      return;
    }
    let cancelled = false;
    void (async () => {
      try {
        const entries = await Promise.all(
          team.map(async (character) => [character.id, await getLoadout(config, character.id)] as const),
        );
        if (!cancelled) setTeamLoadouts(Object.fromEntries(entries));
      } catch (err) {
        if (cancelled) return;
        if (err instanceof ApiError && err.status === 401) {
          renewSession();
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [team, config, token, renewSession]);

  useEffect(() => {
    if (!jobId || !token) return;
    let cancelled = false;
    let timer = 0;

    const poll = async () => {
      try {
        const status = await getJobStatus(config, jobId);
        if (cancelled) return;
        setJobStatus(status);
        const { current_epoch: epoch, temperature, current_damage: damage } = status.progress;
        if (typeof epoch === 'number') {
          setJobHistory((prev) => {
            const next = prev.filter((sample) => sample.epoch !== epoch);
            next.push({ epoch, temperature: temperature ?? 0, damage: damage ?? 0 });
            next.sort((a, b) => a.epoch - b.epoch);
            return next;
          });
        }
        if (status.status === 'finished') {
          window.clearInterval(timer);
          const finished = await getJobResult(config, jobId);
          if (!cancelled) {
            setResult(finished);
            notify(`Optimization finished: ${formatDamage(finished.totalDamage)} team damage.`, 'good');
          }
        } else if (status.status === 'failed') {
          window.clearInterval(timer);
        }
      } catch (err) {
        if (cancelled) return;
        if (err instanceof ApiError && err.status === 404) {
          window.clearInterval(timer);
          window.localStorage.removeItem(LAST_JOB_KEY);
          setJobId('');
          setJobStatus(null);
        } else if (err instanceof ApiError && err.status === 401) {
          renewSession();
        }
      }
    };

    void poll();
    timer = window.setInterval(poll, 3000);
    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
  }, [jobId, token, config, renewSession, notify]);

  useEffect(() => {
    setJobHistory([]);
    setJobStatus(null);
    setResult(null);
  }, [jobId]);

  async function handleUpload(file: File): Promise<boolean> {
    if (!token) {
      notify('Session is starting — try again in a moment.', 'error');
      return false;
    }
    setBusy(true);
    try {
      const size = await uploadScan(config, file);
      setLastScan(size);
      notify(`Imported ${size.numCharacters} characters, ${size.numLightCones} cones, ${size.numRelics} relics.`, 'good');
      await refreshAll();
      return true;
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        renewSession();
      } else {
        notify(messageOf(err), 'error');
      }
      return false;
    } finally {
      setBusy(false);
    }
  }

  function toggleDraft(id: string) {
    setDraftTeam((prev) => {
      if (prev.includes(id)) return prev.filter((value) => value !== id);
      if (prev.length >= 4) return prev;
      return [...prev, id];
    });
  }

  async function handleSaveTeam() {
    if (draftTeam.length === 0) {
      notify('A team needs at least one character.', 'error');
      return;
    }
    setBusy(true);
    try {
      const saved = await saveTeam(config, draftTeam);
      setTeam(saved);
      setDraftTeam(saved.map((character) => character.id));
      notify('Team saved.', 'good');
    } catch (err) {
      notify(messageOf(err), 'error');
    } finally {
      setBusy(false);
    }
  }

  async function toggleTeamMembership(id: string) {
    const ids = team.some((member) => member.id === id)
      ? team.filter((member) => member.id !== id).map((member) => member.id)
      : [...team.map((member) => member.id), id];
    if (ids.length === 0) {
      notify('A team needs at least one character.', 'error');
      return;
    }
    if (ids.length > 4) {
      notify('A team can contain at most four characters.', 'error');
      return;
    }
    setBusy(true);
    try {
      const saved = await saveTeam(config, ids);
      setTeam(saved);
      setDraftTeam(saved.map((character) => character.id));
      notify('Team updated.', 'good');
    } catch (err) {
      notify(messageOf(err), 'error');
    } finally {
      setBusy(false);
    }
  }

  async function handleDeleteCharacter(id: string) {
    setBusy(true);
    try {
      await deleteCharacter(config, id);
      if (detailId === id) setDetailId('');
      notify('Character deleted.', 'good');
      await refreshAll();
    } catch (err) {
      notify(messageOf(err), 'error');
    } finally {
      setBusy(false);
    }
  }

  async function handleStart() {
    if (team.length === 0) {
      notify('Save a team before starting the optimizer.', 'error');
      setPage('team');
      return;
    }
    setBusy(true);
    try {
      const queued = await startEvaluation(config);
      window.localStorage.setItem(LAST_JOB_KEY, queued.jobId);
      setJobId(queued.jobId);
      setPage('optimizer');
      notify('Optimization queued.', 'info');
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        renewSession();
      } else {
        notify(messageOf(err), 'error');
      }
    } finally {
      setBusy(false);
    }
  }

  const detail = detailId ? roster.find((character) => character.id === detailId) : undefined;

  const teamSetNames = useMemo(
    () => Object.fromEntries(Object.entries(game?.relicSets ?? {}).map(([id, meta]) => [id, meta.name])),
    [game],
  );

  function openCharacter(id: string) {
    setDetailId(id);
    setPage('characters');
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="brand">
          <span className="brand-mark">HSR</span>
          <div className="brand-copy">
            <strong>Optimizer</strong>
            <span>Relic lab</span>
          </div>
        </div>
        <nav className="topnav">
          {PAGES.map((entry) => (
            <button
              key={entry.id}
              type="button"
              className={page === entry.id ? 'active' : ''}
              onClick={() => {
                setPage(entry.id);
                if (entry.id !== 'characters') setDetailId('');
              }}
            >
              {entry.label}
            </button>
          ))}
        </nav>
        <div className="top-actions">
          <span className="db-stat">
            {hasScan ? `${relics.length} relics · ${cones.length} cones · ${roster.length} characters` : 'No scan loaded'}
          </span>
          <button type="button" className="btn primary small" onClick={() => setImportOpen(true)}>
            Import
          </button>
        </div>
      </header>

      <main className="app-main">
        {!initialized ? (
          <EmptyState title="Loading inventory…" hint="Connecting to the inventory service." />
        ) : !hasScan ? (
          <section className="welcome">
            <div className="welcome-copy">
              <span className="eyebrow">Welcome</span>
              <h1>Build the best team you own.</h1>
              <p>
                Import an HSR scanner export to browse relics and light cones, manage your roster, assemble a lineup
                and run the simulated-annealing optimizer.
              </p>
              <div className="welcome-actions">
                <button type="button" className="btn primary" onClick={() => setImportOpen(true)}>
                  Import scanner data
                </button>
                <button type="button" className="btn" onClick={() => setPage('optimizer')}>
                  View optimizer
                </button>
              </div>
              <ul className="welcome-list">
                <li>Filter and edit every relic and light cone</li>
                <li>Full character roster with loadout editing</li>
                <li>Four-slot team preview before optimizing</li>
              </ul>
            </div>
            <div className="welcome-art" aria-hidden="true">
              <span>HSR</span>
              <strong>Relic Lab</strong>
            </div>
          </section>
        ) : (
          <>
            {page === 'characters' && detail ? (
              <>
                <PageHeader
                  eyebrow="Character"
                  title={detail.name}
                  subtitle={`${detail.path} · Lv ${detail.level} · E${detail.eidolon}`}
                  actions={
                    <button type="button" className="btn" onClick={() => setDetailId('')}>
                      Back to roster
                    </button>
                  }
                />
                <CharacterPreview
                  config={config}
                  character={detail}
                  meta={game?.characters[detail.id]}
                  setMeta={game?.relicSets ?? {}}
                  coneMeta={game?.lightCones ?? {}}
                  characterNames={characterNames}
                  cones={cones}
                  allRelics={relics}
                  inTeam={team.some((member) => member.id === detail.id)}
                  onToggleTeam={() => void toggleTeamMembership(detail.id)}
                  onOptimize={() => setPage('optimizer')}
                  onChanged={refreshAll}
                  onDelete={() => void handleDeleteCharacter(detail.id)}
                />
              </>
            ) : null}

            {page === 'characters' && !detail ? (
              <CharacterRoster
                config={config}
                roster={roster}
                game={game}
                teamIds={team.map((member) => member.id)}
                onSelect={openCharacter}
                onChanged={refreshAll}
                notify={notify}
              />
            ) : null}

            {page === 'relics' ? (
              <RelicInventory
                config={config}
                relics={relics}
                game={game}
                characterNames={characterNames}
                onChanged={refreshAll}
                notify={notify}
              />
            ) : null}

            {page === 'cones' ? (
              <LightConeInventory
                config={config}
                cones={cones}
                game={game}
                characterNames={characterNames}
                onChanged={refreshAll}
                notify={notify}
              />
            ) : null}

            {page === 'team' ? (
              <TeamPage
                team={team}
                draftIds={draftTeam}
                roster={roster}
                meta={game?.characters ?? {}}
                setNames={teamSetNames}
                loadouts={teamLoadouts}
                result={result}
                busy={busy || refreshing}
                onToggleDraft={toggleDraft}
                onSave={handleSaveTeam}
                onOpenCharacter={openCharacter}
                onManageLoadout={openCharacter}
                onOptimize={() => setPage('optimizer')}
              />
            ) : null}

            {page === 'optimizer' ? (
              <OptimizerPage
                jobStatus={jobStatus}
                jobHistory={jobHistory}
                result={result}
                busy={busy}
                hasTeam={team.length > 0}
                onStart={() => void handleStart()}
                onOpenTeam={() => setPage('team')}
                roster={roster}
                cones={cones}
                game={game}
              />
            ) : null}
          </>
        )}
      </main>

      <ToastViewport toasts={toasts} onDismiss={(id) => setToasts((prev) => prev.filter((toast) => toast.id !== id))} />
      <ImportModal
        open={importOpen}
        onClose={() => setImportOpen(false)}
        onUpload={handleUpload}
        busy={busy}
        counts={lastScan}
        hasScan={hasScan}
      />
    </div>
  );
}
