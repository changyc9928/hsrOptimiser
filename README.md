# hsrOptimiser

Relic optimiser for Honkai: Star Rail.

Evaluates a team of HSR characters against a given relic inventory, and searches for better relic equips using simulated annealing against the [asagi damage-calc simulator](https://honkai.asagi-game.com).

## Stack

- Java 17 + Spring Boot 3.3.4
- Spring Web + WebClient (calls asagi)
- Redis (in-memory eval progress, statuses and temp tokens)
- RabbitMQ (async evaluate-job queue)
- Maven

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
  initial-temperature: 250000
  cooling-rate: 0.95
  epoch: 32
```

Recommended defaults based on real-scan sweeps: `epoch=32, cooling-rate=0.95` (~40–60 s per eval). Raising to `epoch=64, cooling-rate=0.90` is ~3.5 min for marginally better damage.

## Run

1. Start rabbitmq (redis must be up on localhost:6379):

   ```bash
   docker compose up -d
   ```

2. Build & run:

   ```bash
   mvn compile
   mvn spring-boot:run
   ```

Default port 8080.

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
