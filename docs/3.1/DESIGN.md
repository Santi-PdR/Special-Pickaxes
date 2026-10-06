# 3.1 interaction audit and decisions

Baseline: 4b6155fa0e66cbf549f184a1969e0140819085e1, preserved in place.

| Artifact | Fantasy / reason to choose it | 3.1 interaction |
|---|---|---|
| Palimpsest | repair your own recent damage, paid, without arbitrary copying | aim at a scar and Use; local memory ghosts, no corners |
| Choir | reuse a personal excavation score with exact-state matching | aim and Use; sneak-use rotates the score |
| Eventide | quarry plus hostile trajectory control | aim and Use; polarity toggle |
| Meridian | synchronized mining at a second worksite | two point anchors, persistent link; never corner selection |
| Crucible | geological material conversion | two corners, analyze, transform; four named rock families |
| Interregnum | local kinetic rule, not a damage spell | aim and Use; sneak-use releases |
| Worldloom | fast useful preset shelter/bridge | aim and Use, inventory material, mode toggle |
| Icarus | physically travel through a bore | Use launches; reverse toggle; swept collision remains |
| Axiom | subtract geological matrix without harvesting its ore | aim and Use, optional structural ribs |
| Atlas | bijective matter permutation | four corners; transforms; analysis before exchange |
| Worldbreaker | supreme composite world tool, not mandatory bureaucracy | regions for BREAK/REPHASE/TRANSPOSE/RECORD; direct aimed CARVE and local RESTORE |
| Chronicle | deliberately checkpoint a region | two corners for record/restore |
| Keystone | build a vault, not draw a cuboid | aim and Use creates an oriented parabolic arch; support variant |
| Tessellator | paid structural reproduction | four corners and transforms |

Six additions, no corner selectors:
- Aegis: redirect incoming hostile projectiles, reflect or shear; never changes ownership/damage or players.
- Lodestar: retrace recorded footsteps through still-clear space; not wall teleport or another excavating ride.
- Seam Ripper: remove only a connected material interface, not the whole vein/volume; offhand chooses the neighboring material.
- Causeway: paid scaffolding reacts to the player's route over gaps, unlike a preset Worldloom bridge.
- Counterseal: temporary protection of terrain from explosions; no entity invulnerability.
- Covenant: seal confirmed manual mining holes after the miner passes them; paid, never fills a player's body or a fluid.

Reject fluid transport for this release (source regeneration and mod fluid callbacks require
more compatibility work), additional vein miners and generic damage tools.

Energy is removed, not renamed. Full native capacities are available immediately;
durability, cooldowns, actual inventory and scheduler budgets remain. Old numeric
energy NBT is discarded as migration-only data. No regeneration or resource meter.

Art: reference Minecraft's existing pickaxe models/textures, without redistributing
Mojang textures or extracting uncertain third-party art. The four reference JARs
remain untouched. Identity is carried by animation of glint, local head auras,
operation particles, colors, sounds and distinct mechanics. No new experimental sprites.

Normal tooltip: identity + mode + a short SHIFT hint. Physical-client tooltip event
adds the contextual manual only while SHIFT is down; no client class in common item code.

No test-1 path is assumed or invented; no access to siege. CI worlds are disposable,
not a claim of user-instance validation.
