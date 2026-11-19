# Hellspec alpha.17 — infernal-rifts primary implemented, identity partial

Public primary replaces the old five-petal bloom with eight broad radial fissures through natural strata. Radius 18, vertical offsets -8 through +4 (13 layers), 4049 unique candidates; cardinal and diagonal fissures narrow toward depth. Traversal is top-down. Harvests natural rock and ore with native tool loot. No lava placement, real explosions, entity damage, building, generic tunnel or magnet. Budgeted custom infernal jets follow active execution, reusing existing signature particle assets; item textures/models remain frozen.

The hardened queue skips player-placed blocks and unloaded chunks, respects build height, tool/claims/durability/drop pressure, counts actual removals against configured cap, and validates captured actor/tool context after permission callbacks. Reentrant starts are blocked. Worldbreaker hand primary copying dispatches the same fissures. Legacy HellspecBloomShape geometry remains infrastructure regression coverage but no longer public primary.

Two JUnit tests cover volume, uniqueness, top-down order, depth narrowing and rock between fissures. Three GameTests cover central/diagonal arms and ores, exterior/provenance preservation, hand copying and callback reentry/relocation. Local 141 Python tests (one skipped without Java), 25 resource checks and diff check pass. Forge validation and action screenshots pending. Secondary chimney and Curios remain previous implementation, explicitly not reformed in this milestone. Not a completed identity; no release published.

## Client validation synchronization correction

Previous Axiom CI 44621956811 passed 199 JUnit / 486 GameTests but failed when advancing from Eventide mode 0 to 1. The screenshot helper sends a real C key before writing filesystem ACK; the ACK and network packet travel independently. Dev-only fixture now waits for the actual server mode before advancing, up to 100 server ticks. It does not change mode, resend the key, remove assertions or alter production controls. A missing/rejected key still fails with a bounded timeout. This addresses the possible ACK race; runtime confirmation remains pending and it is not claimed as the proven cause of every lost input.

## Alpha.17 runtime approval (primary milestone)

Source `09afad161605683eaf0d1f4ac697088401183d2e`, CI `44626533396` SUCCESS: 205 JUnit with zero failures/errors/skips; all 496 GameTests, client/input and packaged-server checks pass. This validates the primary milestone, including the client acknowledgement correction, not the new alpha.18 code or every potential lost-key cause. JAR `special-pickaxes-1.20.1-6.0.0-alpha.17.jar`, 921802 bytes, SHA256 `96e5b3be5dbad98898d53d576ea6091c058fe7d1d6dad8f20db46a8fe16058b7`. No Hellspec action screenshot inspected; no public release. Exodium alpha.16 CI `44624910270` also SUCCESS.

## Alpha.18 — secondary and Curios implemented, Forge pending

Secondary Infernal Eruption replaces the uniform chimney with an ascending, widening cone through natural rock and ore: 19 layers (vertical offsets 0..18), base radius 3, crown radius 12, 3571 unique candidates. Traverses from the foot upward. Natural drops and standard claim/provenance/tool/chunk/backpressure safeguards; no lava, fire placement, creature damage or explosion. Existing Worldbreaker hand-alternate echo dispatch reaches the new secondary. Uses bounded infernal custom VFX without item-art changes.

Curios Cinder Ring activates after confirmed manual natural rock or ore mining, once per 8 seconds. Three layers (offset -1..1), radii 3 through 5 inclusive, 168 candidates. Removes only natural non-ore geology, preserving ore, the central rock, exterior and player builds. Exact original-slot equipment lease is revalidated after external callbacks before current-cell mutation. Unconfirmed/queued breaks and removed sources cannot trigger it. Existing Cinder Mantle heat/fire/lava protections and retort remain.

Adds two JUnit tests for cone ordering/bounds/volume and immutable annulus geometry; four GameTests cover public secondary flare/exterior/ore behavior, confirmed natural-rock Curios trigger/cooldown/ore/provenance preservation, unconfirmed/removed source and same-cell callback revocation. Local 141 Python (one skip without Java), 25 resources and diff check pass. Forge alpha.18 and dedicated action screenshots pending; implementation is not full validation. No public release. Worldbreaker native rework and global Curios copying still outstanding.

Alpha.18 CI `44630552998`: all 500 GameTests passed, but overall CI failed when the client stalled at `eventide-gravity_out` activation for 45 seconds. Mode acknowledgement succeeded. This does not approve alpha.18 end-to-end; input failure remains open without relaxing the test.
