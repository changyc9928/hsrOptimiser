import type {
  ApiResponse,
  EvaluationResult,
  HSRCharacter,
  JobQueued,
  JobStatus,
  LightCone,
  Loadout,
  Relic,
  ScannedData,
  ScannedDataSize,
} from './types.ts';

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

async function parseApi<T>(response: Response): Promise<T> {
  let body: ApiResponse<T> | null = null;
  try {
    body = (await response.json()) as ApiResponse<T>;
  } catch {
    throw new ApiError(`Request failed with status ${response.status}`, response.status);
  }
  if (!response.ok || !body || body.success === false) {
    throw new ApiError(body?.message || `Request failed with status ${response.status}`, response.status);
  }
  return body.data as T;
}

interface CallOptions extends RequestInit {
  token?: string;
}

async function call<T>(url: string, options: CallOptions = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (!(options.body instanceof FormData) && options.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  if (options.token) {
    headers.set('Authorization', `Bearer ${options.token}`);
  }
  const response = await fetch(url, { ...options, headers });
  return parseApi<T>(response);
}

export interface BackendConfig {
  inventoryBase: string;
  engineBase: string;
  token: string;
}

export async function issueToken(inventoryBase: string): Promise<string> {
  const response = await fetch(`${inventoryBase}/auth/token`);
  if (!response.ok) {
    throw new ApiError(`Token request failed with status ${response.status}`, response.status);
  }
  const body = (await response.json()) as { token?: string };
  if (!body?.token) {
    throw new ApiError('Token response did not contain a token', response.status);
  }
  return body.token;
}

export async function uploadScan(config: BackendConfig, file: File): Promise<ScannedDataSize> {
  const form = new FormData();
  form.append('file', file);
  return call<ScannedDataSize>(`${config.inventoryBase}/data/upload`, {
    method: 'POST',
    body: form,
    token: config.token,
  });
}

export async function getScanned(config: BackendConfig): Promise<ScannedData> {
  return call<ScannedData>(`${config.inventoryBase}/data/scanned`, { token: config.token });
}

export async function getTeam(config: BackendConfig): Promise<HSRCharacter[]> {
  return call<HSRCharacter[]>(`${config.inventoryBase}/team`, { token: config.token });
}

export async function saveTeam(config: BackendConfig, characterIds: string[]): Promise<HSRCharacter[]> {
  return call<HSRCharacter[]>(`${config.inventoryBase}/team`, {
    method: 'PUT',
    body: JSON.stringify({ characterIds }),
    token: config.token,
  });
}

export async function getLoadout(config: BackendConfig, characterId: string): Promise<Loadout> {
  return call<Loadout>(`${config.inventoryBase}/loadout/${characterId}`, { token: config.token });
}

export async function equipLightCone(
  config: BackendConfig,
  characterId: string,
  lightConeUid: string,
): Promise<Loadout> {
  return call<Loadout>(`${config.inventoryBase}/loadout/${characterId}/lightcone`, {
    method: 'POST',
    body: JSON.stringify({ lightConeUid }),
    token: config.token,
  });
}

export async function unequipLightCone(config: BackendConfig, characterId: string): Promise<Loadout> {
  return call<Loadout>(`${config.inventoryBase}/loadout/${characterId}/lightcone`, {
    method: 'DELETE',
    token: config.token,
  });
}

export async function equipRelics(
  config: BackendConfig,
  characterId: string,
  relicUids: string[],
): Promise<Loadout> {
  return call<Loadout>(`${config.inventoryBase}/loadout/${characterId}/relics`, {
    method: 'PUT',
    body: JSON.stringify({ relicUids }),
    token: config.token,
  });
}

export async function unequipRelics(config: BackendConfig, characterId: string): Promise<Loadout> {
  return call<Loadout>(`${config.inventoryBase}/loadout/${characterId}/relics`, {
    method: 'DELETE',
    token: config.token,
  });
}

export async function addLightCone(config: BackendConfig, payload: Record<string, unknown>): Promise<LightCone> {
  return call<LightCone>(`${config.inventoryBase}/inventory/lightcones`, {
    method: 'POST',
    body: JSON.stringify(payload),
    token: config.token,
  });
}

export async function addCharacter(config: BackendConfig, payload: Record<string, unknown>): Promise<HSRCharacter> {
  return call<HSRCharacter>(`${config.inventoryBase}/inventory/characters`, {
    method: 'POST',
    body: JSON.stringify(payload),
    token: config.token,
  });
}

export async function addRelic(config: BackendConfig, payload: Record<string, unknown>): Promise<Relic> {
  return call<Relic>(`${config.inventoryBase}/inventory/relics`, {
    method: 'POST',
    body: JSON.stringify(payload),
    token: config.token,
  });
}

export async function updateCharacter(
  config: BackendConfig,
  characterId: string,
  payload: Record<string, unknown>,
): Promise<HSRCharacter> {
  return call<HSRCharacter>(`${config.inventoryBase}/inventory/characters/${characterId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
    token: config.token,
  });
}

export async function updateLightCone(
  config: BackendConfig,
  lightConeUid: string,
  payload: Record<string, unknown>,
): Promise<LightCone> {
  return call<LightCone>(`${config.inventoryBase}/inventory/lightcones/${lightConeUid}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
    token: config.token,
  });
}

export async function updateRelic(
  config: BackendConfig,
  relicUid: string,
  payload: Record<string, unknown>,
): Promise<Relic> {
  return call<Relic>(`${config.inventoryBase}/inventory/relics/${relicUid}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
    token: config.token,
  });
}

export async function startEvaluation(config: BackendConfig): Promise<JobQueued> {
  return call<JobQueued>(`${config.inventoryBase}/evaluate`, {
    method: 'POST',
    token: config.token,
  });
}

export async function getJobStatus(config: BackendConfig, jobId: string): Promise<JobStatus> {
  return call<JobStatus>(`${config.engineBase}/evaluate/${jobId}/status`, { token: config.token });
}

export async function getJobResult(config: BackendConfig, jobId: string): Promise<EvaluationResult> {
  return call<EvaluationResult>(`${config.engineBase}/evaluate/${jobId}/result`, { token: config.token });
}
