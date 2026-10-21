# hsrOptimiser

Relic optimiser for Honkai: Star Rail.

Evaluates a team of HSR characters against a given relic inventory, and searches for better relic equips using simulated annealing against the [asagi damage-calc simulator](https://honkai.asagi-game.com).

## Stack

- Java 17 + Spring Boot 3.3.4
- Spring Web + WebClient (calls asagi)
- Redis (in-memory eval progress, statuses and temp tokens)
- Kafka (async evaluate-job trigger)
- Maven
- React + TypeScript frontend (`frontend/`, served on 3001 via Compose)

## Data sources

- Game metadata: [Mar-7th/StarRailRes](https://github.com/Mar-7th/StarRailRes) (derived from [Dimbreath/StarRailData](https://github.com/DimbreathBot/TurnBasedGameData))
- Light-cone / character / relic-set identifiers and SA tuning tables: extracted from the live asagi web app bundle.
- Repo has hard-coded enums regenerated from the above at `src/main/java/com/hsrOptimiser/clientConfig/`.

## Config

`src/main/resources/application.yml`:

```yaml
asagi:
  url: "https://honkai.asagi-game.com"

simulated-annealing:
  initial-temperature-factor: 0.005
  cooling-rate: 0.9
  epoch: 12
  steps-per-epoch: 20
  search-round: 6
  final-round: 6
  refine-fraction: 0
  min-steps: 120
  patience: 100
  polish-temperature-factor: 0.005
```

The search can use up to 240 round-6 evaluations. It explores at the configured starting
temperature, then stops after at least 120 steps once there has been no meaningful
improvement for 100 steps. Before stopping it restarts from the best build and spends one
more patience window polishing at a low temperature. Strong starting loadouts converge
quickly; poor or incomplete ones keep exploring. The equipped build is seeded as the
initial best, and characters with missing relics are repaired into a complete loadout
before the first evaluation. Duplicate relic configurations are served from an in-run
cache. `search-round`/`refine-fraction` can split the budget between cheaper and
full-horizon rounds, but shorter rounds can rank speed/energy builds poorly. All settings
can be overridden with `SA_*` environment variables (see `docker-compose.yml`).

## Run

1. Start redis, kafka, and both services:

   ```bash
   docker compose up -d --build
   ```

Inventory listens on 8080; SA engine listens on 8081; frontend listens on 3001.

2. Build & run:

   ```bash
   mvn compile
   mvn spring-boot:run
   ```

Local defaults remain inventory 8080 and engine 8081. For frontend development:

```bash
npm --prefix frontend install
npm --prefix frontend run dev
```

Vite proxies `/api/inventory` to `localhost:8080` and `/api/engine` to `localhost:8081`.

## API

### Auth

No accounts. Get a temp token (TTL 24h, Redis):

```bash
curl http://localhost:8080/auth/token
```

Use as bearer: `Authorization: Bearer <token>` on `/data/**` and `/evaluate/**`. Missing/invalid → 401.

### Upload scanned data

```bash
curl -X POST http://localhost:8080/data/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@scanned.json"
```

Response: `{numCharacters, numLightCones, numRelics}`.

### Start async optimize job

```bash
curl -X POST http://localhost:8080/evaluate \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
        "characterIds": ["1308","1310","1407","1505"],
        "fixedCharacterIds": [],
        "allowedToScrapRelicsCharacterIds": ["1308","1310","1407","1505"],
        "disallowedToScrapRelicsCharacterIds": []
      }'
```

Response: `{jobId, status:"queued"}`.

### Poll status / progress

```bash
curl http://localhost:8080/evaluate/$JOBID/status \
  -H "Authorization: Bearer $TOKEN"
```

Returns `{status: running|finished|failed, progress: {total_steps, current_epoch, total_epochs, temperature, current_damage}}`.

### Fetch result

```bash
curl http://localhost:8080/evaluate/$JOBID/result \
  -H "Authorization: Bearer $TOKEN"
```

Returns `EvaluationResult {total_damage, character_damage: [{name, total_damage, relics}] }` once `status=finished`.

### Loadout (light cones & relics)

All responses return the character's full loadout `{character, lightCone, relics}`.

```bash
# view
curl http://localhost:8080/loadout/1308 -H "Authorization: Bearer $TOKEN"
# equip light cone (moves it from whoever holds it)
curl -X POST http://localhost:8080/loadout/1308/lightcone \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"lightConeUid":"14"}'
# replace full relic set (max 6, one per slot)
curl -X PUT http://localhost:8080/loadout/1308/relics \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"relicUids":["6749","7312","7442","7506","713","10246"]}'
# unequip
curl -X DELETE http://localhost:8080/loadout/1308/lightcone -H "Authorization: Bearer $TOKEN"
curl -X DELETE http://localhost:8080/loadout/1308/relics -H "Authorization: Bearer $TOKEN"
```

### Team

```bash
curl -X PUT http://localhost:8080/team \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"characterIds":["1308","1310","1407","1505"]}'
curl http://localhost:8080/team -H "Authorization: Bearer $TOKEN"
```

Team holds 1–4 unique owned characters.

### Inventory (add items)

```bash
# add light cone (level + superimposition 1-5; id must exist in game data)
curl -X POST http://localhost:8080/inventory/lightcones \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"id":"23000","level":80,"superimposition":5}'
# add character (level + eidolon 0-6 + skill levels)
curl -X POST http://localhost:8080/inventory/characters \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"id":"1506","level":80,"eidolon":0,
       "skills":{"basic":6,"skill":10,"ult":10,"talent":10,"elation":10}}'
# add relic (level + mainstat + substats with values)
curl -X POST http://localhost:8080/inventory/relics \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"setId":"129","slot":"Body","level":15,"mainstat":"CRIT DMG",
       "substats":[{"key":"CRIT Rate_","value":10.3,"count":1,"step":1}]}'
```

All three accept optional `uid` (auto-generated if omitted), `name` (defaults
to game-data name), and `location` (a character id to equip immediately).
Ids are validated against game data; unknown id/set/slot/stat names are rejected.

## Dev notes

- `EvaluationServiceImpl.evaluateAsagi(..., null)` runs a single SA sync (used by tests and direct invocation); the async pipeline wraps the same path.
- Engine strategies: `SingleRelicMutationStrategy` ~70%, `ExistingPairMutationStrategy` 20%, `PlanarPairMutationStrategy` 5%, `CavernSetMutationStrategy` 5%. Mutation only touches permitted relic pools (`allowedToScrapRelicsCharacterIds` / `disallowedToScrapRelicsCharacterIds`).
