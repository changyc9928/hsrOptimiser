export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  statusCode: number;
}

export interface CharacterSkills {
  basic: number;
  skill: number;
  ult: number;
  talent: number;
  elation: number;
}

export interface CharacterTraces {
  ability1: boolean;
  ability2: boolean;
  ability3: boolean;
  stat1: boolean;
  stat2: boolean;
  stat3: boolean;
  stat4: boolean;
  stat5: boolean;
  stat6: boolean;
  stat7: boolean;
  stat8: boolean;
  stat9: boolean;
  stat10: boolean;
  special: boolean;
}

export interface HSRCharacter {
  id: string;
  name: string;
  path: string;
  level: number;
  ascension: number;
  eidolon: number;
  skills: CharacterSkills;
  traces: CharacterTraces;
  ability_version: number;
  memosprite: CharacterSkills | null;
}

export interface LightCone {
  id: string;
  name: string;
  level: number;
  ascension: number;
  superimposition: number;
  location: string | null;
  lock: boolean;
  uid: string;
}

export type RelicSlot =
  | 'Head'
  | 'Hands'
  | 'Body'
  | 'Feet'
  | 'LinkRope'
  | 'PlanarSphere';

export interface SubStat {
  key: string;
  value: number;
  count: number;
  step: number;
}

export interface Relic {
  set_id: string;
  name: string;
  slot: RelicSlot;
  rarity: number;
  level: number;
  mainstat: string;
  substats: SubStat[];
  preview_substats?: SubStat[] | null;
  location: string | null;
  lock: boolean;
  discard: boolean;
  uid: string;
}

export interface ScannedData {
  source?: string;
  version?: number;
  build?: string;
  light_cones: LightCone[];
  relics: Relic[];
  characters: HSRCharacter[];
}

export interface ScannedDataSize {
  numCharacters: number;
  numLightCones: number;
  numRelics: number;
}

export interface Loadout {
  character: HSRCharacter;
  lightCone: LightCone | null;
  relics: Relic[];
}

export interface JobQueued {
  jobId: string;
  status: string;
}

export interface JobProgress {
  total_steps?: number;
  current_epoch?: number;
  total_epochs?: number;
  temperature?: number;
  current_damage?: number;
  /** Peak damage seen so far; this is what the finished result reports. */
  best_damage?: number;
}

export interface JobStatus {
  jobId: string;
  status: 'queued' | 'running' | 'finished' | 'failed' | string;
  progress: JobProgress;
  error?: string;
}

export interface CharacterDamage {
  totalDamage: number;
  name: string;
  relics: Relic[];
}

export interface EvaluationResult {
  totalDamage: number;
  characterDamage: CharacterDamage[];
}

export interface RelicInput {
  setId: string;
  name?: string;
  slot: RelicSlot;
  rarity: number;
  level: number;
  mainstat: string;
  substats: SubStat[];
  location?: string;
  lock?: boolean;
  uid?: string;
}

export interface RelicUpdateInput {
  name?: string;
  level?: number;
  mainstat?: string;
  substats?: SubStat[];
  lock?: boolean;
  discard?: boolean;
}

export interface LightConeInput {
  id: string;
  name?: string;
  level?: number;
  ascension?: number;
  superimposition?: number;
  location?: string;
  lock?: boolean;
  uid?: string;
}

export interface LightConeUpdateInput {
  id?: string;
  name?: string;
  level?: number;
  ascension?: number;
  superimposition?: number;
  lock?: boolean;
}

export interface CharacterInput {
  id: string;
  name?: string;
  path?: string;
  level?: number;
  ascension?: number;
  eidolon?: number;
  skills?: CharacterSkills;
  traces?: CharacterTraces;
  abilityVersion?: number;
}

export interface CharacterUpdateInput {
  name?: string;
  path?: string;
  level?: number;
  ascension?: number;
  eidolon?: number;
  skills?: CharacterSkills;
  traces?: CharacterTraces;
}
