export const STAR_RAIL_RES = 'https://raw.githubusercontent.com/Mar-7th/StarRailRes/master';

export interface CharacterMeta {
  id: string;
  name: string;
  rarity: number;
  path: string;
  element: string;
  icon: string;
  preview: string;
  portrait: string;
}

export interface LightConeMeta {
  id: string;
  name: string;
  rarity: number;
  path: string;
  icon: string;
  preview: string;
  portrait: string;
}

export interface RelicSetMeta {
  id: string;
  name: string;
  icon: string;
  desc: string[];
}

export interface GameData {
  characters: Record<string, CharacterMeta>;
  lightCones: Record<string, LightConeMeta>;
  relicSets: Record<string, RelicSetMeta>;
}

let cached: Promise<GameData> | null = null;

async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(`${STAR_RAIL_RES}/${path}`);
  if (!response.ok) {
    throw new Error(`Game data request failed: ${response.status}`);
  }
  return (await response.json()) as T;
}

export function getGameData(): Promise<GameData> {
  if (!cached) {
    cached = (async () => {
      const [characters, lightCones, relicSets] = await Promise.all([
        fetchJson<Record<string, CharacterMeta>>('index_new/en/characters.json'),
        fetchJson<Record<string, LightConeMeta>>('index_new/en/light_cones.json'),
        fetchJson<Record<string, RelicSetMeta>>('index_new/en/relic_sets.json'),
      ]);
      return { characters, lightCones, relicSets };
    })().catch((error: unknown) => {
      cached = null;
      throw error;
    });
  }
  return cached;
}

export function assetUrl(path: string): string {
  return `${STAR_RAIL_RES}/${path}`;
}

export function characterIcon(id: string, meta?: { icon?: string }): string {
  return assetUrl(meta?.icon ?? `icon/character/${id}.png`);
}

export function characterPortrait(id: string, meta?: { portrait?: string }): string {
  return assetUrl(meta?.portrait ?? `image/character_portrait/${id}.png`);
}

export function lightConeIcon(id: string, meta?: { icon?: string }): string {
  return assetUrl(meta?.icon ?? `icon/light_cone/${id}.png`);
}

export function lightConePortrait(id: string, meta?: { portrait?: string }): string {
  return assetUrl(meta?.portrait ?? `image/light_cone_portrait/${id}.png`);
}

export function relicSetIcon(setId: string, meta?: { icon?: string }): string {
  return assetUrl(meta?.icon ?? `icon/relic/${setId}.png`);
}

const ELEMENT_ICON_ALIAS: Record<string, string> = {
  Lightning: 'Thunder',
};

export function elementIcon(element: string): string {
  const name = ELEMENT_ICON_ALIAS[element] ?? element;
  return assetUrl(`icon/element/${name}.png`);
}

export function pathIcon(path: string): string {
  const name = path.charAt(0).toUpperCase() + path.slice(1);
  return assetUrl(`icon/path/${name}.png`);
}

export function formatDamage(value: number): string {
  if (!Number.isFinite(value)) return '0';
  if (value >= 1_000_000) return `${(value / 1_000_000).toFixed(2)}M`;
  if (value >= 1_000) return `${(value / 1_000).toFixed(1)}K`;
  return value.toFixed(0);
}

export function normalizeName(name: string): string {
  return name.toLowerCase().replace(/[^a-z0-9]/g, '');
}

export function formatSubstat(value: number): string {
  if (!Number.isFinite(value)) return '0.00';
  return value.toFixed(2);
}

const PERCENT_STATS = new Set(['ATK', 'HP', 'DEF']);

export function displayStat(key: string): string {
  if (!key.endsWith('_')) return key;
  const base = key.slice(0, -1);
  return PERCENT_STATS.has(base) ? `${base}%` : base;
}

export const RELIC_MAIN_STATS = [
  'HP', 'ATK', 'DEF', 'SPD',
  'CRIT Rate', 'CRIT DMG',
  'Effect Hit Rate', 'Break Effect',
  'Energy Regeneration Rate', 'Outgoing Healing Boost',
  'Physical DMG Boost', 'Fire DMG Boost', 'Ice DMG Boost',
  'Lightning DMG Boost', 'Wind DMG Boost',
  'Quantum DMG Boost', 'Imaginary DMG Boost',
];

export const RELIC_SUBSTAT_KEYS = [
  'ATK', 'HP', 'DEF', 'SPD',
  'ATK_', 'HP_', 'DEF_',
  'CRIT Rate_', 'CRIT DMG_',
  'Effect RES_', 'Effect Hit Rate_', 'Break Effect_',
];

export const CHARACTER_PATHS = [
  'Destruction', 'The Hunt', 'Erudition', 'Harmony', 'Nihility',
  'Preservation', 'Abundance', 'Remembrance',
];

export const RELIC_SLOT_ORDER = ['Head', 'Hands', 'Body', 'Feet', 'LinkRope', 'PlanarSphere'] as const;

export const RELIC_SLOT_LABEL: Record<string, string> = {
  Head: 'Head',
  Hands: 'Hands',
  Body: 'Body',
  Feet: 'Feet',
  LinkRope: 'Link Rope',
  PlanarSphere: 'Planar Sphere',
};
