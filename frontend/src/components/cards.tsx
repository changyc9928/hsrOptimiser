import type { HSRCharacter, LightCone, Relic } from '../api/types.ts';
import {
  RELIC_SLOT_LABEL,
  characterIcon,
  characterPortrait,
  displayStat,
  formatSubstat,
  lightConePortrait,
  relicSetIcon,
} from '../api/gameData.ts';
import { GameImage, StatChip } from './ui.tsx';

function rarityClass(rarity?: number): string {
  if (rarity === 5) return 'rarity-5';
  if (rarity === 4) return 'rarity-4';
  return 'rarity-3';
}

export function CharacterCard({
  character,
  meta,
  selected,
  onSelect,
  badge,
}: {
  character: HSRCharacter;
  meta?: { rarity: number; element: string; portrait: string; icon: string };
  selected?: boolean;
  onSelect?: () => void;
  badge?: string;
}) {
  return (
    <button
      type="button"
      className={`char-card ${rarityClass(meta?.rarity)} ${selected ? 'selected' : ''}`}
      onClick={onSelect}
    >
      <span className="char-art">
        <GameImage
          src={characterPortrait(character.id, meta)}
          alt={character.name}
          className="char-portrait"
          fallback={character.name.slice(0, 2).toUpperCase()}
        />
      </span>
      <span className="char-meta">
        <strong>{character.name}</strong>
        <small>
          Lv {character.level} · E{character.eidolon} · {character.path}
          {meta ? ` · ${meta.element}` : ''}
        </small>
        {badge ? <span className="badge">{badge}</span> : null}
      </span>
    </button>
  );
}

export function CharacterRow({
  character,
  meta,
}: {
  character: HSRCharacter;
  meta?: { rarity: number; icon: string };
}) {
  return (
    <div className={`row-card ${rarityClass(meta?.rarity)}`}>
      <GameImage
        src={characterIcon(character.id, meta)}
        alt={character.name}
        className="thumb"
        fallback={character.name.slice(0, 1)}
      />
      <div>
        <strong>{character.name}</strong>
        <div className="muted">
          {character.id} · Lv {character.level} · E{character.eidolon} · {character.path}
        </div>
      </div>
    </div>
  );
}

export function LightConeCard({
  cone,
  meta,
}: {
  cone: LightCone;
  meta?: { rarity: number; icon: string; portrait: string };
}) {
  return (
    <div className={`cone-card rarity-${meta?.rarity ?? (cone.id.startsWith('23') ? 5 : 4)}`}>
      <GameImage
        src={lightConePortrait(cone.id, meta)}
        alt={cone.name}
        className="cone-art"
        fallback="LC"
      />
      <div className="cone-meta">
        <strong>{cone.name}</strong>
        <div className="muted">
          Lv {cone.level} · S{cone.superimposition} · {cone.location ? `on ${cone.location}` : 'unequipped'}
        </div>
      </div>
    </div>
  );
}

export function RelicCard({ relic, setName }: { relic: Relic; setName?: string }) {
  return (
    <div className={`item-card rarity-${relic.rarity}`}>
      <GameImage src={relicSetIcon(relic.set_id)} alt={setName ?? relic.name} className="thumb" fallback="RE" />
      <div className="item-main">
        <strong>{setName ?? relic.name}</strong>
        <div className="muted">
          {RELIC_SLOT_LABEL[relic.slot] ?? relic.slot} · +{relic.level} · {relic.location ? `on ${relic.location}` : 'unequipped'}
        </div>
        <div className="chips">
          <StatChip label="Main" value={relic.mainstat} />
          {relic.substats.map((sub) => (
            <StatChip key={`${relic.uid}-${sub.key}`} label={displayStat(sub.key)} value={formatSubstat(sub.value)} />
          ))}
        </div>
      </div>
    </div>
  );
}
