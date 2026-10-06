# Siege artifact redesign: four-reference audit

## Evidence and scope

This replaces the 1.0 generic utility design; that release's passing tests do **not**
certify the new design. All four local JARs were inspected with the reproducible
class-file reader `tools/audit_jars.py`. Full method bytecode, resolved constant
operands, registrations and resource JSON/TOML are in `docs/audit/`. Exact hashes
and resource counts are in `reference-inventory.json`. No reference binary is
replaced or included in the release. No reference mod was executed. SRG names
are preserved rather than inventing decompiled names. Runtime compatibility with
the actual 122-mod instance is not claimed: the overlap evidence is the owner's
confirmed mod list and specified functions, not possession of all its configs.

## Elemental & Utility Pickaxes: findings, not assumptions from its filename

The file named `utility_pickaxe-1.0.6-forge-1.20.1.jar` actually declares
`more_pickaxe`, display name `More Pickaxe`, version **1.0.0**, author Acer/MCreator.
It bundles MixinExtras 0.5.0. No reason to copy this dependency into our artifact.
Nine pickaxes: Dirt, TNT, Furnace (spelled Furnance), Hammer, Lightning (Ligtning),
Lapis, Sculk, Long, Adamantium. Also long stick and adamantium ingot, recipes,
handheld/generated item models, textures and creative tab; no registered custom
ore/worldgen system in this JAR. A redundant singular `tags/item` directory is
not valid 1.20.1 data; use plural `tags/items` in our implementation.

| Evidence: class/method | Actual mechanic | Classification and decision |
|---|---|---|
| DirtPickaxeItem: only constructor + tier | Plain stat-based pickaxe | COMÚN; discard identity |
| LongPickaxeItem.getAttributeModifiers | Mainhand Forge BLOCK_REACH +2.25 and ENTITY_REACH +1.5, stable UUIDs | ÚTIL COMO COMPONENTE; use bounded targeting, not a new long-reach identity |
| AdamantiumPickaxeItem.mineBlock/use/isFoil | Server mining increments NBT `charge` up to 50; full charge enables glint and message. Use spends charge on directional 3×3×10 destruction; calls Level.destroyBlock directly | INTERESANTE; adapt mining-fed energy, not its direct destruction pipeline |
| AdamantiumHudOverlay.onRenderGuiOverlay | HOTBAR overlay, NBT charge/50, pulsing READY cue and bar | ÚTIL COMO COMPONENTE; visual phase feedback, no need to copy fixed English HUD |
| Furnance…Procedure.onBlockBreak/execute | Explicit comparisons against vanilla block constants, replacement with air and spawning fixed ItemStacks | COMÚN; reject hardcoded smelting and pre-break mutation; no generic RecipeManager here |
| Hammer…Procedure | Event-driven additional block destruction | COMÚN; no hammer-size identity; bounded geometry remains a component |
| Lapis…Procedure | Chooses LocalPlayer on client or **first online player** on server, raycasts 20, recognizes a fixed ore list, awards ExperienceOrb XP and particles | COMÚN; discard XP-farming identity and wrong-player ownership entirely |
| Sculk…Procedure | Same first-player/global lookup; searches around raycast and replaces SculkSensor/Shrieker states with another block constant | INTERESANTE concept of changing environmental rules; reject arbitrary replacement/global-player implementation |
| Ligtning…Procedure | Targeted compound action: entity/particle/sound work, block loop, cooldown and tool damage; direct level destruction | ÚTIL COMO COMPONENTE; staged tell/impact/recovery, not another damage wand |
| Tnt…Procedure | `Level.explode` strength 3, **ExplosionInteraction.NONE**, item cooldown/damage | COMÚN; NONE does not mine terrain but can still affect entities. Do not describe it as safe quarrying |
| MorePickaxeMod | Generic SimpleChannel helper and queued server work processed on tick | ÚTIL COMO COMPONENTE; our queue needs budgets, cancellation, state checks and ownership, not merely delayed Runnables |

Security findings: first-online-player lookup is not the mining actor; global
handlers and item callbacks can both reach Lapis/Sculk procedures; raw destruction
and state replacement are not player-game-mode harvesting; no claim guarantee
can be inferred from having a BreakEvent entry point. Adamantium's destruction
only tests air/hardness, not harvest eligibility and per-block claim cancellation.
No custom-model skeleton or animation controller is evident in these nine item
models; don't promise GeckoLib animation inherited from the JAR.

## Other references re-crossed against the new objective

| Reference / actual systems | Classification | Reuse/adapt/discard |
|---|---|---|
| POP Seismic: oriented plane, manual drops/XP/destruction | COMÚN / ÚTIL COMO COMPONENTE | Orientation only; replace mutation pipeline |
| POP InfernoReaver: smelting recipes, ammunition/projectiles, feedback stages | COMÚN + INTERESANTE staging | Keep recipe-system knowledge, not autosmelt identity or separate ammo items |
| POP Backpackaxe: inventory capability, menu, auto-pickup | YA CUBIERTA POR SIEGE | Sophisticated Backpacks; do not build another backpack/magnet |
| POP Ender/Elytra: item movement, flight/wearable, beams, dismount networking | COMÚN / ÚTIL COMO COMPONENTE | Swept movement and feedback concepts, not teleport/flight identity |
| POP Pickitooxe: summoned pathfinding miner and remote; animated entities | INTERESANTE | A mining actor with phases, but no autonomous quarry replacing Create; no entity/pathfinding load |
| Ultimate Coal/Fire/Laser/TNT | COMÚN | Speed, smelting, bounded extra mining and explosions are components, not artifacts |
| Ultimate Natural: table of mossy transformations | ÚTIL COMO COMPONENTE | Generalize to curated 1:1 geology transmutation without loot duplication |
| Ultimate Bone/Shadow | COMÚN | Bonus drops and combat effects rejected as primary identities |
| Custom Echo: fixed-set ore scanner with range/direction feedback | YA CUBIERTA POR SIEGE | Advanced XRay/JourneyMap; no scanning artifact |
| Custom Stormstone: actual nearby-monster shock, including chance on mining | COMÚN | Mining-to-energy coupling interesting; raw damage identity redundant with Siege combat |
| Custom Titanium: plane, ThreadLocal guard, ForgeHooks break checks | ÚTIL COMO COMPONENTE | Preserve recursion/protection concepts and improve scheduled work |
| Custom Volcanite: nearby ItemEntity conversions | COMÚN | Reject touching unrelated drops; no fixed smelting map |
| Custom Vitalite: sustenance/healing while mining | COMÚN | Not a healing artifact replacing PlayerRevive/Relics/combat systems |

**REALMENTE ÚNICA** is not assigned to any reference's basic magnet, scanner,
plane, haste or teleport. Our ten designs are differentiated compositions and
workflows, not a claim that no other mod anywhere implements memory or stasis.

## Siege overlap gate (owner-confirmed inventory)

- **Backpacks / Advanced XRay:** hard veto on storage/magnet or ore-location identity.
- **Create, Create Enhanced/Botanic:** avoid passive industrial quarries, conveyors,
  automated factories and logistics; artifact work is personal, actively targeted,
  cancellable and does not run when the holder leaves.
- **ToolLeveling:** do not add a competing XP leveling/enchantment progression.
- **Relics:** no generic random affix accessory system; unique mining workflows.
- **Pehkui:** no growth/shrink gimmick. **Freecam:** no camera clone/probe.
- **JourneyMap/Jade/JEI:** no map, block inspector or recipe viewer.
- **Better Combat, Brutality, Lethality, SlashBlade, JustEnoughGuns, Terramity,
  Magic Evolved, Celestisynth, Aurora's Arsenal:** not ten extra damage weapons;
  combat interactions are non-damaging spatial control coupled to mining.
- **SecurityCraft:** preserve break/place protection, no security-block manipulation.
- **PlayerRevive/ScalingHealth:** no resurrection, permanent HP or progression replacement.
- **GeckoLib/Photon/Player Animator/Visuality:** potential presentation libraries,
  not evidence of a mining mechanic. Vanilla animated sprite models and bounded
  server particles meet the implementation needs without a mandatory dependency.
- **Distant Horizons:** no forced distant chunks, LOD edits or client world illusion.

## Current project audit / migration

Keep the proven `gameMode.destroyBlock` path, cancellation event, no-client common
code, bounded traversal tests, CI installed-JAR smoke and evidence transport.
Replace: generic ten item registrations, old recipes/recipe advancements, palette
recolors, small synchronous area/vein loops, fixed vanilla-tier balance and the
claim that 1.0's tests cover a Siege artifact release. Add: global work scheduler,
state-matched edits, bounded persistent memories, item identity, held-item job
ownership, placement payments and atomic geology exchange. No new ores, worldgen,
recipes or mandatory third-party APIs. Old IDs are mapped to new artifacts when
loading older worlds, without registering twenty items.
