# Siege artifact verification (2.0)

The authoritative result is the **latest completed Forge verification run for the
exact release commit**, with matching `META-INF/MANIFEST.MF` Build-Commit and
`build/RELEASE.json`. Do not apply results from 1.0 or an earlier failed run.

## Executable suites

- JUnit: six bounded traversal cases plus three geometry cases (result limits,
  visit budgets, denied barriers, cycles, rotations, planes and unique positions).
- Forge GameTests: registration/no recipes, all admin grants and permission gate;
  paid Palimpsest reconstruction; rotated/translated Choir state matching;
  Eventide quarry and trajectory control; Meridian consequences; Crucible material
  conservation; Interregnum stasis/release/local speed; Worldloom paid bridge;
  Icarus safe bore and bedrock stop; Axiom rib/ore preservation; Atlas permutation and conserved relocation into air;
  break and placement cancellation; atomic rollback; payment mutation; serialized
  cooldown/memory; queue budget/tool cancellation; unloaded chunks; restricted
  blocks; Silk Touch/Fortune drops and wear; no unpaid placement; isolated tier;
  stale dimension anchor rejection. Tests use server fake players with a real
  packet handler for server movement. This alone is not an external network test.
- Static resources: parse JSON and mcmeta, ten distinct animated sprites and models,
  matching languages, pickaxe tag, no crafting recipes/new blockstates/worldgen.
- Installed release JAR: clean Forge 47.3.0 dedicated server, world startup, item-ID
  command parsing, save and stop. Empty `@a` commands do not claim actual delivery
  to a human player; administrative acquisition is verified separately in GameTests.
- Graphical smoke: actual Minecraft client and integrated server under Xvfb,
  disposable gallery world, ten item displays and inventory grants, HUD/animated
  asset loading, runtime log checks and screenshot. The test ends the disposable
  process intentionally; it is not a manual ten-artifact gameplay playthrough.
- Log gate: ERROR/Exception/Crash/Stacktrace/Failed/Invalid cause failure. Additional
  missing-texture/model/bake checks apply to the client log.

## Limitations that must stay explicit

No full Eternal Craft — Siege instance/configuration was supplied. We use the
owner's confirmed overlap list, not a fabricated inspection of all 122 JARs.
No certification of external claims plugins, every modded block/loot table,
ToolLeveling-specific callbacks, dedicated multiplayer latency or large-player
load testing. Client gallery execution, when successful, demonstrates actual
rendering/resource loading and an integrated server, not those integrations.

## Reproduction / artifact

See README and `.github/workflows/verify.yml`. Output:
`build/libs/special-pickaxes-1.20.1-2.0.0.jar`.
`python3 tools/verify_release.py` validates the archive and writes its exact size,
SHA-256 and checkout commit. `forge-validation` publishes these alongside logs and
reports, including a real screenshot if the graphical test reached its ready marker.
The full tool suite runs only inside the workspace/disposable build directories.

The original release's logs/report are retained in `docs/validation/` and
`LEGACY-1.0.md` for traceability, explicitly **not current 2.0 evidence**.
