# Worldbreaker alpha.19 — native Cataclysm milestone, reform incomplete

Native first mode (stable key `carve`) now dispatches Cataclysm rather than a forward tunnel. A beveled 33×17×33 chamber centered on aimed natural geology, 14801 unique candidates, traversed top-down. Horizontal corners taper further in the upper/lower layers. Harvests natural rock and ore with native loot; default configured cap remains 8192 successful removals. Air, ore-ineligible blocks, builds and skipped positions do not consume that removal cap. It is not a promise to remove all 14801 blocks under the default cap.

Reuses hardened work-queue execution: current-cell claims, player provenance, unloaded chunks, build height, tool/loot/durability and drop backpressure. Captured actor/tool context and reentry guard protect activation callbacks. Custom local fragment-ring particles are budgeted and emitted during work without changing item models/textures.

Two JUnit cases cover bounds, uniqueness, top-down ordering and beveled corners; two GameTests cover public native dispatch, broad chamber/ore harvesting, build/exterior preservation and callback relocation/reentry. Local 141 Python (one skipped without Java), 25 resources and diff check pass. No Java runtime locally; Forge alpha.19 and dedicated action capture pending.

This is deliberately a partial milestone: the other five native modes, native alternate, Curios active/passive redesign and global source-specific Curios copying are NOT claimed complete. Existing held-copy primary/alternate dispatch stays intact and bypasses Cataclysm. Regional selector and historical directional regression shapes remain unchanged. No public release.

## Preceding Hellspec alpha.18 validation

CI `44630552998`, source `8186708`: all 500 GameTests passed; client gallery failed at `eventide-gravity_out` activation after 45 seconds, not at the earlier mode acknowledgement. No synthetic activation or relaxed timeout was introduced to conceal it. Hellspec secondary/Curios are not yet approved end-to-end; the previous combined alpha.17 green CI remains historical evidence, not approval of alpha.18 or this code.
