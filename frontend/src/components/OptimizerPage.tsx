import { useMemo } from 'react';
import type { CharacterDamage, EvaluationResult, HSRCharacter, JobStatus, LightCone } from '../api/types.ts';
import { formatDamage, normalizeName } from '../api/gameData.ts';
import type { GameData } from '../api/gameData.ts';
import { CharacterResultCard } from './CharacterResultCard.tsx';
import { OptimizerChart, type ProgressSample } from './OptimizerChart.tsx';
import { EmptyState, PageHeader, ProgressBar, StatChip } from './ui.tsx';

function progressPercent(progress: JobStatus['progress']): number {
  if (progress.total_epochs && progress.current_epoch !== undefined) {
    return (progress.current_epoch / Math.max(1, progress.total_epochs)) * 100;
  }
  return 0;
}

export function OptimizerPage({
  jobStatus,
  jobHistory,
  result,
  busy,
  hasTeam,
  onStart,
  onOpenTeam,
  roster,
  cones,
  game,
}: {
  jobStatus: JobStatus | null;
  jobHistory: ProgressSample[];
  result: EvaluationResult | null;
  busy: boolean;
  hasTeam: boolean;
  onStart: () => void;
  onOpenTeam: () => void;
  roster: HSRCharacter[];
  cones: LightCone[];
  game: GameData | null;
}) {
  const running = jobStatus?.status === 'running' || jobStatus?.status === 'queued';
  const sorted = useMemo(
    () =>
      [...(result?.characterDamage ?? [])].sort(
        (a, b) => b.totalDamage - a.totalDamage,
      ) as CharacterDamage[],
    [result],
  );

  const entryByName = useMemo(() => {
    const map: Record<
      string,
      { id: string; character: HSRCharacter; meta?: GameData['characters'][string] }
    > = {};
    for (const character of roster) {
      const entry = {
        id: character.id,
        character,
        meta: game?.characters[character.id],
      };
      map[normalizeName(character.name)] = entry;
      map[normalizeName(`${character.name}${character.path}`)] = entry;
    }
    return map;
  }, [roster, game]);

  return (
    <>
      <PageHeader
        eyebrow="Simulated annealing"
        title="Optimizer"
        subtitle="The engine searches relic combinations for the saved team and reports the best build it finds."
        actions={
          <>
            <button type="button" className="btn" onClick={onOpenTeam}>
              Team lineup
            </button>
            <button
              type="button"
              className="btn primary"
              onClick={onStart}
              disabled={busy || running || !hasTeam}
              title={!hasTeam ? 'Save a team first' : undefined}
            >
              {running ? 'Optimization running…' : result ? 'Run again' : 'Start optimization'}
            </button>
          </>
        }
      />

      {!hasTeam ? (
        <EmptyState
          title="No team saved"
          hint="Build a team of up to four characters, then start the optimizer."
          action={
            <button type="button" className="btn primary" onClick={onOpenTeam}>
              Choose team
            </button>
          }
        />
      ) : null}

      {jobStatus ? (
        <section className="panel status-panel">
          <div className="panel-body">
            <div className="status-row">
              <div className="chips">
                <StatChip label="Status" value={jobStatus.status} />
                <StatChip
                  label="Epoch"
                  value={`${jobStatus.progress.current_epoch ?? 0}/${jobStatus.progress.total_epochs ?? 0}`}
                />
                <StatChip label="Best" value={formatDamage(jobStatus.progress.best_damage ?? 0)} />
                <StatChip label="Current" value={formatDamage(jobStatus.progress.current_damage ?? 0)} />
                <StatChip label="Temperature" value={formatDamage(jobStatus.progress.temperature ?? 0)} />
              </div>
              <span className="muted">
                {jobStatus.status === 'finished'
                  ? 'Finished'
                  : jobStatus.status === 'failed'
                    ? 'Failed'
                    : 'Evaluating relic combinations…'}
              </span>
            </div>
            <ProgressBar value={progressPercent(jobStatus.progress)} />
            {jobStatus.status === 'failed' ? (
              <div className="notice">{jobStatus.error ?? 'The optimizer failed.'}</div>
            ) : null}
          </div>
        </section>
      ) : null}

      {jobHistory.length > 0 ? <OptimizerChart samples={jobHistory} /> : null}

      {result ? (
        <>
          <div className="result-total">
            <div>
              <span className="eyebrow">Result</span>
              <strong>{formatDamage(result.totalDamage)}</strong>
            </div>
            <span className="muted">Best team damage found</span>
          </div>
          <div className="grid results">
            {sorted.map((row, index) => {
              const entry = entryByName[normalizeName(row.name)];
              return (
                <CharacterResultCard
                  key={row.name}
                  row={row}
                  total={result.totalDamage}
                  rank={index + 1}
                  characterId={entry?.id}
                  character={entry?.character}
                  meta={entry?.meta}
                  relicSets={game?.relicSets ?? {}}
                  cones={cones}
                  coneMeta={game?.lightCones ?? {}}
                />
              );
            })}
          </div>
        </>
      ) : !jobStatus ? (
        <EmptyState
          title="No optimization yet"
          hint="The first evaluation takes a moment while the engine measures the current team, then the search refines it, usually within 10 minutes."
        />
      ) : null}
    </>
  );
}
