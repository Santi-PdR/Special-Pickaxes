# Special Pickaxes: Siege Artifacts 2.0

**Minecraft 1.20.1 · Forge 47.3.0+ · Java 17 · no mandatory third-party APIs.**

Ten endgame artifacts for **Eternal Craft — Siege**, not ten resized vanilla tools.
No crafting recipes, ores, worldgen, random affix system, item magnet or XRay.
The four reference JARs are preserved and are **not** bundled or dependencies.

## The artifacts

| ID | Name | Primary / secondary |
|---|---|---|
| `palimpsest` | Palimpsesto del Último Alba | Restore personal mining scars with paid materials / erase history |
| `fault_choir` | Coro de las Fallas | Translate and replay a mining stencil / rotate it |
| `eventide` | La Noche que Pesa | Geological shell collapse plus hostile trajectory field / reverse polarity |
| `meridian` | Meridiano de Dos Mundos | Link two same-dimension anchors; mining at A echoes at B / unlink |
| `paradox_crucible` | Crisol de la Paradoja | Rephase inert geology 1:1 / cycle rock family |
| `interregnum` | Corona del Interregno | Mining-fed kinetic stasis with local mining rules / release momentum |
| `worldloom` | Bastidor de la Primera Muralla | Weave a paid structural shelter or bridge / switch shape |
| `icarus` | Lágrima de Ícaro | Ride an excavated, collision-validated kinetic bore / reverse direction |
| `hollow_axiom` | El Axioma Hueco | Remove geological matrix, preserve ore and machinery / toggle structural ribs |
| `bifold_atlas` | Atlas de las Dos Orillas | Mark two regions and permute their geology in atomic pairs / clear anchor |

Complete appearance, mechanics, inspiration, controls, costs and Siege-overlap
rationale: [design contract](docs/siege/DESIGNS.md).
[Four-reference audit and overlap decisions](docs/siege/AUDIT.md).

## Acquisition and controls

Install **only** `special-pickaxes-1.20.1-2.0.0.jar` in client/server `mods/`.
Admin permission level 2:

```mcfunction
/specialpickaxes grant PlayerName palimpsest
/give @s specialpickaxes:palimpsest
```

All ten also appear in **Siege — Artifacts**. No special currency or new resource
is required; hook these IDs into Siege's administrative/reward system.

- Mainhand **Use** (default right click): primary; vanilla key can be rebound.
- **Sneak + Use**: secondary. If a queued job is active, cancels that job first.
- **Switch tools**: immediately stops further queued edits on the next scheduler tick.
- Worldloom: hold plain stone/deepslate/etc. in offhand. Payment can come from any
  matching inventory slot. It does not consume named/NBT-bearing materials.
- Meridian/Atlas: aim at first anchor and use, then at second after cooldown.
  Anchors require the same dimension and loaded chunks, within the player's tether.
- Palimpsest/Choir record successful **manual** mining of curated inert geology;
  scheduled work does not recursively record/replay itself. No ore-copy exploit.
- Charge builds from manual mining; it amplifies volumes, not a grind prerequisite.
  Artifact HUD, glint, animated textures, sounds, colored rings and work counts
  show state. The HUD contains server-origin display data, not authority.

All artifacts use a registered tier above netherite: 32,768 durability, speed 64,
40 enchantability, fire resistance and epic rarity. Forge tier ordering uses its
own **empty requirement tag**, not a redefinition of vanilla harvest restrictions.
Normal mining still consumes durability and follows Fortune, Silk Touch and
Unbreaking. Activation costs 4 durability by default, with a 40-tick cooldown.
Those are configurable operational pacing, not vanilla power restrictions.

## Safe operation and integration

Default configuration: `<world>/serverconfig/specialpickaxes-server.toml`.
Default work limits: 24 candidates/player/tick, 192 globally, 4096 steps/job,
64-block tether, 120-second timeout, 256 memory records, 24 entities/domain.
Failures consume scheduling budget too. One active job and one field per owner.

- Every mining step rechecks block state, tool, dimension, loaded chunk, border,
  player restrictions and protection, then uses `ServerPlayerGameMode.destroyBlock`.
- Forge 47 calls item mining before block removal: an end-of-tick observation
  confirms the resulting hole before recording memory or triggering entanglement.
- Restoration and building cannot overwrite occupied cells and require real
  inventory payment. Canceled placement or invalidated payment rolls back.
- Rephasing/exchange only handles a closed list of inert vanilla default block
  states, with vanilla air allowed as an Atlas endpoint for terrain relocation. Never ores, block entities, fluids, falling blocks, machinery or security
  blocks. Break/place hooks are honored. No loot/XP emitted by matter permutation.
- Atlas exchanges are atomic **per pair**, not a transactional undo of a whole region.
- Memories/anchors/cooldowns persist per player, carry dimension/age limits and
  are not arbitrary inventory or block-entity NBT. Death keeps cooldowns but clears
  spatial state. Queues/fields deliberately do not persist across server restart.
- Stasis never alters AI/NBT flags, players or dropped items. It holds hostile
  positions/velocities and suppresses attacks from captured entities while active;
  release restores bounded momentum. It does not globally stop world time.
- Icarus advances only after a full-body swept path is clear; an obstructed movement
  step stops the remaining bore. No noclip or wall teleport.
- `AbilityUseEvent` remains a cancelable integration point for territory policy,
  including entity-domain effects. Per-block Forge BreakEvent/EntityPlaceEvent
  handles normal claims. Real SecurityCraft/claims/modpack combinations still
  require integration testing; no universal compatibility claim is made.

Vanilla animated sprite extrusion produces ten distinct silhouettes (64px,
4 frames each), with client-only HUD and vanilla particles/sounds. No GeckoLib,
Photon, Visuality or Player Animator dependency is necessary. Old 1.0 item IDs
are remapped on world load instead of registering twenty items.

## Build, tests and exact provenance

```sh
./gradlew build
./gradlew test
./gradlew runGameTestServer
python3 tools/validate_resources.py
python3 tools/verify_release.py
python3 tools/dedicated_smoke.py
git diff --check
```

No `package.json`: `npm run check` is not applicable.

CI `Forge verification` installs Java 17, builds/reobfuscates, tests, verifies the
JAR, boots it in a clean dedicated server, and runs a real graphical Forge client
under Xvfb. The client uses a disposable world copy with a gallery of all ten
artifacts; this is **CI-only data**, not mod world generation. The graphical smoke
records a screenshot and resource-load log, then terminates its disposable process.

The downloadable `forge-validation` artifact includes the JAR, test reports,
server/client logs, gallery screenshot, **SHA256SUMS** and **RELEASE.json**.
The manifest's **Build-Commit** and RELEASE.json's commit must equal the run's
checkout SHA. Read the run result: an artifact from a failed run is diagnostic,
not automatically a release. Source GameTests and fixture structures are excluded
from the release JAR.

The editing environment may lack Java / block external downloads. In that case
CI is the execution authority, not an unexecuted local command. Checks API evidence
is a bounded fallback for downloading the same products when the artifact CDN is
blocked. It exports only explicitly listed build outputs, not secrets/world data.

[Validation scope and reproduction](docs/VALIDACION.md). The retained 1.0 logs
are historical evidence and do not certify the 2.0 artifact implementation.
