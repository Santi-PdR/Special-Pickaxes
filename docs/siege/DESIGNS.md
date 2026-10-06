# Ten final Siege artifacts — implementation contract (2.0)

Common base: registered artifact tier above netherite, 32,768 durability, speed 64,
40 enchantability, fire resistant, epic rarity. Obtained only via creative, `/give`
or permission-level-2 `/specialpickaxes grant <player> <artifact>`.
Use = right click (vanilla rebindable); Shift+Use = secondary. While a job is
running, Shift+Use cancels it. No remote chunk loads. Default global budget 192
candidate operations/tick, 24/player/tick, one job/player, maximum 4096/job,
64-block tether, 120-second job timeout. OP volume, bounded tick work.

| Name / ID | Appearance | Primary | Secondary | Passive / mining | VFX / SFX | Inspiration; Siege distinction |
|---|---|---|---|---|---|---|
| **Palimpsesto del Último Alba** / `palimpsest` | Split ivory crescent and gold clock dial | Rebuild recent personal mining scars at their original coordinates using actual inventory blocks | Erase remembered scars | Records up to 256 inert geology states with dimension/time; no duplicated loot | Amber reverse spiral, page-like particles / amethyst chime | POP storage concept reduced to state-only memory + Adamantium state; not backup mod, free rollback or backpack |
| **Coro de las Fallas** / `fault_choir` | Three separated cyan tines, vibrating strings | Replay a recorded excavation stencil translated to the aimed anchor, matching source block types | Rotate stencil 90° | Manual strokes record shape, not loot; up to 256 points | Teal spatial wire stencil, pulsing prongs / note-block chime | Seismic orientation + queued Adamantium actions; personal reusable spatial score, not vein miner |
| **La Noche que Pesa** / `eventide` | Black ring, orbiting violet shards | Anchored singularity pulls hostile entities/projectiles inward while excavating successive geological shells | Reverse field polarity (repulsion); active job cancel first | Mining charge increases excavation radius; no item magnet, players untouched | Converging violet orbit rings / respawn anchor | Stormstone coupling + POP waves; matter domain and entity trajectory control, not backpack magnet or explosion damage |
| **Meridiano de Dos Mundos** / `meridian` | Two opposing blue/gold compass heads | First use links A, second B in the **same loaded dimension**; subsequent mining near A repeats at B | Unlink | Live relative-action entanglement, original-state matching and bounded remote queue | Two-color link sparks / enderman sound | POP Ender spatial association, replaced teleport with paired work; no portal travel, no cross-dimensional fake player |
| **Crisol de la Paradoja** / `paradox_crucible` | Open furnace cage with white-hot core | Rephase curated inert geology 1:1 to the selected rock family in a region | Cycle stone/deepslate/basalt/obsidian | Mining charges the core, expanding treatment radius | Orange lattices and cycling white heat / furnace crackle | Ultimate Natural's block transformations; not smelting, ores, free ingots or Create processing chain |
| **Corona del Interregno** / `interregnum` | Suspended royal crown and blue hourglass | Anchor a stasis domain freezing hostile entities/projectiles, powered and extended by mining inside | Release domain and stored bounded momentum | Inside own domain mining accelerates ×8; no global haste or damage | Blue clock-ring boundary, stationary motes / beacon hum | Sculk environmental-rule idea + Adamantium charge; mining-fed local rule domain, not a damage spell |
| **Bastidor de la Primera Muralla** / `worldloom` | Rectangular loom with emerald shuttle | Weave a hollow shelter or traversable bridge from the offhand's actual inert blocks/inventory | Switch shelter/bridge | Mining records/charges geology; construction consumes one item per successful cell, never unpaid cloning | Green stitch trails, structure preview / stone placement | POP spatial patterns + Custom safe mining concepts; active paid structural membrane, not factory/logistics automation |
| **Lágrima de Ícaro** / `icarus` | Swept copper fins around a comet core | Kinetic bore: successive cross-sections are mined, then the player advances only through a cleared swept corridor | Reverse launch direction | Mining energy lengthens the bore; slow fall during the active ride only | Gold/copper comet wake / piston + wind-like enderman | Adamantium directional charged excavation + POP movement; physically validated tunneling ride, not wall teleport |
| **El Axioma Hueco** / `hollow_axiom` | White geometric cage around a green negative-space gem | Excavate surrounding inert geology while preserving ores, containers and a structural rib lattice | Toggle rib preservation vs complete inert-shell removal | Mining expands processing envelope; high native harvest speed | White rectangular cuts with green preserved nodes / sculk pulse | Laser eligibility inverted; preserves the valuable solid while subtracting its matrix, not XRay or vein harvest |
| **Atlas de las Dos Orillas** / `bifold_atlas` | Hinged double silver blade and red hinge | Mark two anchors, then exchange matching offset geology cells atomically between their regions | Clear anchors | Mining charge enlarges the exchange; blocks relocate without drops/XP, no copied matter | Paired red/silver planes / portal-like chime | Long reach + spatial inspiration; no reference direct copy; matter permutation, not mobility or inventory storage |

## Costs, persistence and technical rules

- Primary cooldown default 40 ticks, configurable; toggles 10 ticks. Durability
  activation cost 4, plus normal vanilla per-block mining cost. No energy grind
  prerequisite: charge amplifies range/volume (hard maximums apply).
- Memories, anchor coordinates, mode and per-artifact cooldown belong to the player
  and survive reconnect. States carry dimension, time and maximum length; no block
  entity NBT/contents or arbitrary serialized loot. Death discards spatial memories.
- Queues and fields are intentionally ephemeral: cancel on item switch, logout,
  death, dimension change, timeout or server restart; no replay on load.
- Every mining candidate checks current state, permissions, loaded chunk, border,
  correct tool and Forge harvesting. Fortune/Silk Touch remain vanilla.
- Reconstruction/building consumes inventory **after successful protected placement**.
  No refunds of drops already emitted. No overwrite of occupied target cells.
- Transmutation/exchange operate only on curated vanilla inert default block states,
  with vanilla air allowed as an exchange endpoint; never ores, fluids, inventories, machinery, security blocks or block entities.
  They post break/place hooks, revalidate states and rollback denied placements;
  neither emits drops or XP. Exchange is pairwise atomic, not an all-region rollback.
- Client uses vanilla use packets, NBT item synchronization, cooldown packets and
  particle/sound packets. A command or client cannot choose arbitrary unvalidated
  positions for a destructive operation. No optional animation API is required.

Uniqueness is relative to the identified Siege functions, not a fabricated audit
of every spell/config in the full instance. Actual combined-modpack validation is
still a separate deployment test. Models are ten distinct animated silhouettes,
not one recolored shape. A harmless preview indicates spatial targets; it cannot
reveal unknown ores or load unseen chunks.
