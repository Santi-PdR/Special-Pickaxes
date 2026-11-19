# Worldloom alpha.10 — primary quarry milestone, incomplete identity

Public primary replaces the directional bore with a top-down terraced quarry: radius `12-floor(depth/3)` for depths 0 through 11, four terraces and 4164 unique candidate cells. Layers traverse from surface downward, center outward within each layer. Natural geology only; preserves ores to expose deposits, player-placed blocks, ceilings above the targeted surface, machines, fluids and claims. Normal loot, job cap, drop backpressure and unloaded-chunk skipping remain enforced. Crossing custom threads use the existing Worldloom custom particle; item art remains unchanged.

Worldbreaker hand copy dispatches the same quarry. Activation guards reject reentry and actor changes during permission callbacks. Added two geometry JUnit tests and three GameTests covering real primary terraces/provenance/ore, callback reentry and copied downward excavation. Historical direct directional-bore tests remain internal infrastructure regression coverage rather than public primary identity. Updated the public bounded-quarry fixture's outside sample from radius 9 to radius 13, matching the deliberate radius-12 surface instead of the old radius-8 volume.

Local validation: 139 Python tests (one skipped without Java), 25 resource validations, diff check; all 58 frozen hashes unchanged. Forge CI `44590848623`, source `914ec0aed0639a7e4f66b9f5c10a4d910de07827`, SUCCESS: 191 JUnit without failures/errors/skips, 473 required GameTests, real client 32 scenes/64 tooltips, keyboard controls and packaged server passed. No inspected quarry action screenshot yet.

This is NOT the complete Worldloom rework: the old bridge secondary still exists and must be replaced, Curios remains to be redesigned, and visual action review is open. Do not present this primary-only milestone as another completed identity or a public release. Global Worldbreaker Curios copying remains pending.


Inspected authentic ES guide 1280×720: primary describes the four terraces while secondary/Curios correctly still describe the old pending powers. Saved `review/worldloom-primary-6.0.0-alpha.10-es.jpg`. Inspection caught stale heading “Túnel de cantera”; the subsequent text-only locale patch changes it to “Cantera escalonada” and updates legacy primary instructions. That follow-up passed local Python/resource tests; no replacement screenshot was inspected for the new heading. Do not treat this as an action screenshot or completed identity.

Validated runtime build: `special-pickaxes-1.20.1-6.0.0-alpha.10.jar`, 872000 bytes, SHA256 `a0b01c9c153c0a77dd58ede1beab0a4d06ba3d96d4732a84e8018cee725e3d90`. No public alpha.10 release.
