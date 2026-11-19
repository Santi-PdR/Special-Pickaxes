# Worldloom alpha.10 — primary quarry milestone, incomplete identity

Public primary replaces the directional bore with a top-down terraced quarry: radius `12-floor(depth/3)` for depths 0 through 11, four terraces and 4164 unique candidate cells. Layers traverse from surface downward, center outward within each layer. Natural geology only; preserves ores to expose deposits, player-placed blocks, ceilings above the targeted surface, machines, fluids and claims. Normal loot, job cap, drop backpressure and unloaded-chunk skipping remain enforced. Crossing custom threads use the existing Worldloom custom particle; item art remains unchanged.

Worldbreaker hand copy dispatches the same quarry. Activation guards reject reentry and actor changes during permission callbacks. Added two geometry JUnit tests and three GameTests covering real primary terraces/provenance/ore, callback reentry and copied downward excavation. Historical direct directional-bore tests remain internal infrastructure regression coverage rather than public primary identity. Updated the public bounded-quarry fixture's outside sample from radius 9 to radius 13, matching the deliberate radius-12 surface instead of the old radius-8 volume.

Local validation: 139 Python tests (one skipped without Java), 25 resource validations, diff check; all 58 frozen hashes unchanged. Forge CI `44590848623`, source `914ec0aed0639a7e4f66b9f5c10a4d910de07827`, SUCCESS: 191 JUnit without failures/errors/skips, 473 required GameTests, real client 32 scenes/64 tooltips, keyboard controls and packaged server passed. No inspected quarry action screenshot yet.

This is NOT the complete Worldloom rework: the old bridge secondary still exists and must be replaced, Curios remains to be redesigned, and visual action review is open. Do not present this primary-only milestone as another completed identity or a public release. Global Worldbreaker Curios copying remains pending.


Inspected authentic ES guide 1280×720: primary describes the four terraces while secondary/Curios correctly still describe the old pending powers. Saved `review/worldloom-primary-6.0.0-alpha.10-es.jpg`. Inspection caught stale heading “Túnel de cantera”; the subsequent text-only locale patch changes it to “Cantera escalonada” and updates legacy primary instructions. That follow-up passed local Python/resource tests; no replacement screenshot was inspected for the new heading. Do not treat this as an action screenshot or completed identity.

Validated runtime build: `special-pickaxes-1.20.1-6.0.0-alpha.10.jar`, 872000 bytes, SHA256 `a0b01c9c153c0a77dd58ede1beab0a4d06ba3d96d4732a84e8018cee725e3d90`. No public alpha.10 release.


## Exposed harvest secondary — alpha.11, final CI still open

Source `e844013d906b9d2612321ac15bdb230021044ba0` removes Worldloom's public bridge construction. Secondary scans a loaded cylinder of radius 12 and height 25 (11025 candidates, center-out) for natural ore with at least one loaded air face. Does not excavate the surrounding matrix, buried ore or placed ore; never pays/places offhand materials. Exposure is revalidated after external break callbacks using the leased natural-mining transaction. Scan throughput, extraction cap and drop backpressure remain bounded. Hand-copy Worldbreaker uses this same secondary; global Curios copy remains pending.

Historical paid-placement tests now call a helper entirely inside PickaxeGameTests, absent from the release and not reachable through ability dispatch. Their payment/claims/headroom assertions remain intact; they are not active powers. Public alternate creature-safety fixture now uses exposed ore and asserts excavation rather than material consumption. Two new geometry JUnit cases and three new GameTests cover exposure/buried/placed/matrix preservation, unchanged named offhand materials, exposure sealed during a Forge callback and Worldbreaker copy.

CI `44598734141` compiled and passed **193 JUnit (zero failures/errors/skips), 476 required GameTests and packaged server**. Client reached all scene/control/supreme completion markers but strict runtime-log validation failed on an external authentication service returning HTML instead of JSON. This is **not a green CI**; no validator exception was added or check relaxed. GitHub refused `gh run rerun --failed` with “workflow file may be broken”; a following documentation push requests a fresh run normally. Final clean client validation remains open.

Inspected authentic ES guide 1280×720 from the failed overall run, saved `review/worldloom-harvest-6.0.0-alpha.11-es.jpg`: corrected terraced-quarry heading and exposed-harvest description are visible, with original item model. This is narrow visual evidence, not clean runtime approval or a quarry action screenshot. Local 139 Python (one skipped without Java), 25 resources; 58 frozen hashes unchanged. Curios redesign and action visual review remain pending. No public alpha.11 release.


## Curios quarry roots — alpha.12 implemented, strict client validation open

Runtime `4530594f797e98cb0951507da49822f27e7750b0`, CI `44605433716`: **195 JUnit without failures/errors/skips, 479 required GameTests and packaged server pass**. Not green overall: client completes the keyboard scenes/control/supreme markers, but strict log validation rejects external authentication HTML instead of JSON. No log exception or relaxed assertion was added.

Confirmed manual natural ore mining with Worldloom in Curios triggers four descending mini-terraces, radii 4/4/3/3, 156 candidate cells, eight-second cooldown. Removes natural geology only and preserves ore to expose it. No buried-ore scan or queued recursion. Original slot source and exact held tool remain mandatory; callback slot removal protects the current cell. Existing Heartwood Renewal/ally defense retained. Added two geometry JUnit tests and three GameTests covering confirmed mining/cooldown/ore and provenance, unconfirmed break/removed slot, and callback revocation. Earlier pending-Curios notes describe alpha.11 and are superseded as implementation scope, not approval status.

Disposable real-key client fixture now supplies a solid quarry bank, two ore samples and an external camera. It waits for the actual queued primary, simulates ordinary drop collection/resume (without relaxing backpressure), asserts deep excavation and preserved ore, and captures `worldloom-quarry-active`. This ran through in `44605433716`, but both new Worldloom images were dropped from the bounded Checks export under its 2 MB cap. Full artifact download failed at the storage CDN, so **no quarry action image has been inspected**. Exporter follow-up protects the two current Worldloom review shots before historical priority images, with a regression test; no image fabrication or rendering change.

First alpha.12 run `44604024637` passed all 479 GameTests but strict logs caught vanilla village AI opening a door and recursively generating lava chunks. Set generate-structures=false and seed=0 only in newly created disposable GameTest server properties. The next run above has clean GameTest logs; production world generation and gameplay remain unchanged. The client authentication failure remains independent and unresolved.

Local after exporter follow-up: 140 Python tests (one skipped without Java), 25 resource checks, diff check; all 58 frozen art/conserved-mechanics hashes unchanged. Worldloom primary/secondary/Curios code is implemented, but clean full CI and action-image inspection remain open. Do not yet count it as a fully validated identity. Global Worldbreaker Curios copy remains pending; no public alpha.12 release.


## Final alpha.12 verification — closed

Source `dde36814a8a0cef56166e509fd42e63d2e9ebe6c`, CI `44609459397` **SUCCESS**: 195 JUnit without failures/errors/skips, 479 required GameTests, real client 32 scenes/64 tooltips, keyboard controls and packaged server pass with strict logs unchanged. The authentication failures in previous runs are not present in this final run. Export prioritization retained both current review images.

Inspected authentic 1280×720 ES action image `review/worldloom-quarry-active-6.0.0-alpha.12-es.jpg`: four descending terraces and preserved ore samples visible after real keyboard activation and queued excavation. Water has flowed into the lowest part of this disposable island; no claim of fluid removal. Also inspected `review/worldloom-curios-6.0.0-alpha.12-es.jpg`: primary/secondary and new roots details are readable; general controls continue below the scroll viewport. The top-left tooltip in the action image is drawn by ClientTooltipValidation, excluded from the release, not an equipment HUD. Inspection found one remaining old short label “Taladro del Dosel”; a subsequent locale-only patch renames it to “Cantera escalonada”. These images do not approve every graphics setting, GUI scale or particle animation frame.

Validated JAR: `special-pickaxes-1.20.1-6.0.0-alpha.12.jar`, 881740 bytes, SHA256 `5dbe0311397bf912840e8669c188facf3215af73c201935c0c86a2db2266a864`. No public release. Primary, secondary, Curios and hand copy now implemented and validated; global Worldbreaker Curios copy remains pending. Four identities remain: Axiom, Exodium, Hellspec and Worldbreaker. Earlier open-validation notes are historical, superseded by this final successful run.
