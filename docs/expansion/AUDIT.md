# Expansion audit — 2.0.0 baseline bd69d56

The on-disk files matched the published baseline byte-for-byte; Git's restored
index was reconciled to that commit without replacing files or changing branches.

Problems confirmed from source: PickaxeItem-only mining tags; mode numbers rather
than names; immediate activation without confirmation; Atlas fixed cubes; eagerly
materialized jobs capped at 4096; no pause/progress accounting; memory drops other
dimensions and expires after 20 minutes; ten thin, procedural silhouettes;
no saturation audit of enchantment arithmetic; gallery checks only idle models.

Retain: Forge harvest, rollback/payment boundary, bounded round-robin scheduler,
end-of-tick mining observations, player persistence, registry IDs, reference JARs,
old tests and CI release provenance. Evolve these components, do not fork a second
scheduler or substitute fake loot for vanilla harvest.

Design: common inclusive SelectionVolume + six bijective transforms; explicit
arming/selection/confirmation; streamed jobs, pause/resume, accounting and limits;
long-lived dimension-aware memories; named modes and multilingual instructions.
Atlas uses four corners. Worldbreaker combines analysis with excavate/carve/rephase/
transpose/restore. Chronicle records deliberate geological checkpoints rather than
only manual scars. Keystone builds paid mathematical vaults and supports.
Tessellator repeats a selected geological blueprint using paid material, never by
cloning inventories. Considered but deferred: a second kinetic drill (Icarus overlap)
and strata sonar (XRay overlap). Four new artifacts, six evaluated concepts.

The sandbox has no test-1 instance or desktop launcher. Do not access siege.
Automated client runs are disposable CI worlds, not a claim of test-1 execution.

## Ten-artifact review

| Artifact | Baseline issue | Implemented decision |
|---|---|---|
| Palimpsest | short memory, destructive secondary erase | 24-hour persistent multi-dimension history; selected restoration, scar ghosts, no accidental erase |
| Choir | rotation shown as a number | named 0/90/180/270, preview/confirmation, shared combined harvesting and enchanted throughput; retain exact-state stencil identity |
| Eventide | polarity and field hard to discover | attract/repel names, preview, portal accents, phase sounds; cancel stops field and queued work |
| Meridian | invisible relationship until first break | persistent A/B trace, orientation-derived offset rotation, linked status, distance/rotation tooltip and echo trace |
| Crucible | hidden target material | explicit four rock names, preview confirmation, heat/wax accents; retain no-loot 1:1 rephasing |
| Interregnum | local speed rule unclear | named kinetic stasis, domain boundary/end-rod feedback, explicit release; retain x8 rule and bounded momentum |
| Worldloom | shelter/bridge unexplained | named geometry, confirmation, material instructions and typed feedback; distinct from selected parametric vault and arbitrary blueprint copy |
| Icarus | forward/reverse unclear | explicit direction, preview, physical collision guard retained, kinetic particles and enchanted throughput |
| Axiom | rib toggle opaque | preserve-ribs/subtract modes, confirmation, sculk accents; never becomes ore sonar |
| Atlas | fixed cubes and implicit second-click execution | four corners, six transforms, dimension/overlap diagnostics, analysis, explicit execution, streamed large regions |

Shared new tool classification uses BOTH vanilla and Forge's stack-sensitive
harvest predicates: the first expansion CI caught the latter still using DiggerItem's
pickaxe-only tag, and the regression test now covers logs/planks/soil as well.
