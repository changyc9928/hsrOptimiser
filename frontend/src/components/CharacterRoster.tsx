import { useMemo, useState } from 'react';
import type { GameData } from '../api/gameData.ts';
import { characterPortrait, elementIcon, pathIcon } from '../api/gameData.ts';
import { addCharacter, type BackendConfig } from '../api/client.ts';
import type { HSRCharacter } from '../api/types.ts';
import { EmptyState, GameImage, IconPlus, IconSearch, Modal, PageHeader } from './ui.tsx';

interface Props {
  config: BackendConfig;
  roster: HSRCharacter[];
  game: GameData | null;
  teamIds: string[];
  onSelect: (id: string) => void;
  onChanged: () => Promise<void>;
  notify: (text: string, kind?: 'info' | 'good' | 'error') => void;
}

function charRarity(game: GameData | null, id: string): number {
  return game?.characters[id]?.rarity ?? 5;
}

function charElement(game: GameData | null, id: string): string | undefined {
  return game?.characters[id]?.element;
}

export function CharacterRoster({ config, roster, game, teamIds, onSelect, onChanged, notify }: Props) {
  const [q, setQ] = useState('');
  const [path, setPath] = useState('all');
  const [element, setElement] = useState('all');
  const [adding, setAdding] = useState(false);

  const paths = useMemo(
    () => [...new Set(roster.map((c) => c.path))].sort(),
    [roster],
  );
  const elements = useMemo(() => {
    const values = new Set<string>();
    for (const character of roster) {
      const value = charElement(game, character.id);
      if (value) values.add(value);
    }
    return [...values].sort();
  }, [roster, game]);

  const rows = useMemo(() => {
    const term = q.trim().toLowerCase();
    return roster
      .filter((character) => {
        if (path !== 'all' && character.path !== path) return false;
        if (element !== 'all' && charElement(game, character.id) !== element) return false;
        if (!term) return true;
        return `${character.name} ${character.id} ${character.path}`.toLowerCase().includes(term);
      })
      .sort((a, b) => a.name.localeCompare(b.name));
  }, [roster, q, path, element, game]);

  return (
    <>
      <PageHeader
        eyebrow={`${roster.length} owned`}
        title="Characters"
        subtitle="Open a character to manage their level, light cone and relic loadout."
        actions={
          <button type="button" className="btn primary" onClick={() => setAdding(true)}>
            <IconPlus /> Add character
          </button>
        }
      />

      <div className="inventory-layout">
        <aside className="filters">
          <div className="filter-head">
            <span>Filters</span>
            <button
              type="button"
              className="link-btn"
              onClick={() => {
                setQ('');
                setPath('all');
                setElement('all');
              }}
            >
              Reset
            </button>
          </div>
          <label className="filter search-field">
            <IconSearch />
            <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search name or id" />
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
            Element
            <select value={element} onChange={(e) => setElement(e.target.value)}>
              <option value="all">All elements</option>
              {elements.map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </select>
          </label>
        </aside>

        <section className="inventory-content">
          <div className="list-toolbar">
            <span className="muted">
              {rows.length} of {roster.length} characters
            </span>
            <span className="muted">Click a character to open their loadout</span>
          </div>

          {rows.length === 0 ? (
            <EmptyState
              title="No characters match"
              hint={roster.length === 0 ? 'Import a scanner export or add a character manually.' : 'Try clearing the filters.'}
            />
          ) : (
            <div className="roster-grid">
              {rows.map((character) => {
                const meta = game?.characters[character.id];
                const rarity = charRarity(game, character.id);
                const roleElement = charElement(game, character.id);
                const inTeam = teamIds.includes(character.id);
                return (
                  <button
                    key={character.id}
                    type="button"
                    className={`char-tile rarity-${rarity}`}
                    onClick={() => onSelect(character.id)}
                  >
                    <span className="char-tile-art">
                      <GameImage
                        src={characterPortrait(character.id, meta)}
                        alt={character.name}
                        className="char-tile-portrait"
                        fallback={character.name.slice(0, 2).toUpperCase()}
                      />
                      {roleElement ? (
                        <span className="char-tile-element" title={roleElement}>
                          <GameImage src={elementIcon(roleElement)} alt={roleElement} className="mini-icon" fallback="E" />
                        </span>
                      ) : null}
                      {inTeam ? <span className="char-tile-team">TEAM</span> : null}
                    </span>
                    <span className="char-tile-body">
                      <strong>{character.name}</strong>
                      <span className="char-tile-meta">
                        Lv {character.level} · E{character.eidolon}
                      </span>
                      <span className="char-tile-path">
                        <GameImage src={pathIcon(character.path)} alt={character.path} className="mini-icon" fallback="P" />
                        {character.path}
                      </span>
                    </span>
                  </button>
                );
              })}
            </div>
          )}
        </section>
      </div>

      {adding ? (
        <AddCharacter
          config={config}
          game={game}
          onClose={() => setAdding(false)}
          onSaved={async () => {
            setAdding(false);
            notify('Character added.', 'good');
            await onChanged();
          }}
          notify={notify}
        />
      ) : null}
    </>
  );
}

function AddCharacter({
  config,
  game,
  onClose,
  onSaved,
  notify,
}: {
  config: BackendConfig;
  game: GameData | null;
  onClose: () => void;
  onSaved: () => Promise<void>;
  notify: (text: string, kind?: 'info' | 'good' | 'error') => void;
}) {
  const [id, setId] = useState('');
  const [level, setLevel] = useState('80');
  const [ascension, setAscension] = useState('6');
  const [eidolon, setEidolon] = useState('0');
  const [busy, setBusy] = useState(false);

  const meta = id ? game?.characters[id] : undefined;

  async function submit() {
    if (!id.trim()) {
      notify('Pick a character.', 'error');
      return;
    }
    const levelValue = Number(level);
    const ascensionValue = Number(ascension);
    const eidolonValue = Number(eidolon);
    if (!Number.isFinite(levelValue) || levelValue < 1 || levelValue > 80) {
      notify('Level must be between 1 and 80.', 'error');
      return;
    }
    if (!Number.isFinite(ascensionValue) || ascensionValue < 0 || ascensionValue > 6) {
      notify('Ascension must be between 0 and 6.', 'error');
      return;
    }
    if (!Number.isFinite(eidolonValue) || eidolonValue < 0 || eidolonValue > 6) {
      notify('Eidolon must be between 0 and 6.', 'error');
      return;
    }
    setBusy(true);
    try {
      await addCharacter(config, {
        id: id.trim(),
        level: levelValue,
        ascension: ascensionValue,
        eidolon: eidolonValue,
      });
      await onSaved();
    } catch (err) {
      notify(err instanceof Error ? err.message : 'Add failed', 'error');
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal
      title="Add character"
      subtitle="Characters already in your scan cannot be added twice."
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
            Character
            <select value={id} onChange={(e) => setId(e.target.value)}>
              <option value="">Select character…</option>
              {Object.entries(game?.characters ?? {})
                .sort(([, a], [, b]) => a.name.localeCompare(b.name))
                .map(([characterId, info]) => (
                  <option key={characterId} value={characterId}>
                    {info.name}
                  </option>
                ))}
            </select>
          </label>
          {meta ? (
            <div className="wide meta-preview">
              <GameImage
                src={characterPortrait(id, meta)}
                alt={meta.name}
                className="meta-preview-art"
                fallback={meta.name.slice(0, 2).toUpperCase()}
              />
              <div>
                <strong>{meta.name}</strong>
                <div className="muted">
                  {meta.rarity} star · {meta.element} · {meta.path}
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
            Eidolon
            <input value={eidolon} inputMode="numeric" onChange={(e) => setEidolon(e.target.value)} />
          </label>
        </div>

        <div className="modal-actions">
          <button type="button" className="btn" onClick={onClose} disabled={busy}>
            Cancel
          </button>
          <button type="submit" className="btn primary" disabled={busy}>
            {busy ? 'Adding…' : 'Add character'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
