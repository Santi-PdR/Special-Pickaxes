# Worldbreaker alpha.19 — native Cataclysm milestone, reform incomplete

Native first mode (stable key `carve`) now dispatches Cataclysm rather than a forward tunnel. A beveled 33×17×33 chamber centered on aimed natural geology, 14801 unique candidates, traversed top-down. Horizontal corners taper further in the upper/lower layers. Harvests natural rock and ore with native loot; default configured cap remains 8192 successful removals. Air, ore-ineligible blocks, builds and skipped positions do not consume that removal cap. It is not a promise to remove all 14801 blocks under the default cap.

Reuses hardened work-queue execution: current-cell claims, player provenance, unloaded chunks, build height, tool/loot/durability and drop backpressure. Captured actor/tool context and reentry guard protect activation callbacks. Custom local fragment-ring particles are budgeted and emitted during work without changing item models/textures.

Two JUnit cases cover bounds, uniqueness, top-down ordering and beveled corners; two GameTests cover public native dispatch, broad chamber/ore harvesting, build/exterior preservation and callback relocation/reentry. Local 141 Python (one skipped without Java), 25 resources and diff check pass. No Java runtime locally; Forge alpha.19 and dedicated action capture pending.

This is deliberately a partial milestone: the other five native modes, native alternate, Curios active/passive redesign and global source-specific Curios copying are NOT claimed complete. Existing held-copy primary/alternate dispatch stays intact and bypasses Cataclysm. Regional selector and historical directional regression shapes remain unchanged. No public release.

## Preceding Hellspec alpha.18 validation

CI `44630552998`, source `8186708`: all 500 GameTests passed; client gallery failed at `eventide-gravity_out` activation after 45 seconds, not at the earlier mode acknowledgement. No synthetic activation or relaxed timeout was introduced to conceal it. Hellspec secondary/Curios are not yet approved end-to-end; the previous combined alpha.17 green CI remains historical evidence, not approval of alpha.18 or this code.

## Alpha.20 — native Continental Shear alternate implemented

Without a copied skill, X now starts Continental Shear instead of the 11-cubed legacy rift. Three horizontal discs of radius 24 at offsets -6, 0 and +6, 5379 unique candidates. Natural rock and ore yield native loot. Rock between the planes remains intact. Traverses upper-to-lower planes with the same safe queue, captured held context, start reentry guard, execution-time claim/provenance/chunk/tool checks, configured removal cap and drop backpressure. Uses bounded local Worldbreaker fragment-ring particles. No tunnel, explosion, block construction or entity damage.

Copied alternates still use the source-specific echo dispatcher before the native alternate. A new regression verifies copied Axiom cuts its vertical cross, preserves its central core and does not fall back to native horizontal shear. Another GameTest covers native public alternate dispatch, the three strata, ore harvesting, intervening rock and player provenance. New JUnit case verifies exact 5379-cell geometry, uniqueness, ordering and boundaries. Local 141 Python (one skip without Java), 25 resources, frozen hashes and diff check pass; Forge alpha.20 and action-image inspection pending.

This supersedes only the pending native-alternate item in alpha.19's scope list. Other five native modes and global Curios copying remain outstanding. No claim of complete Worldbreaker reform and no public release. Alpha.19 CI `44633175036` was still in progress when this milestone was prepared.

## Alpha.21 — mining-trigger Curios copies, partial global support

Worldbreaker equipped in Curios can now forward confirmed manual mining to nine existing source-specific Curios observation paths: Dawn (new-drop lighting), Choir (natural-mining streak), Eventide, Paradox, Stasis, Worldloom, Axiom, Exodium and Hellspec. Keeps each source's eligibility, actual scar confirmation, cooldown, geometry and execution safety; it does not substitute a generic excavation for them. Native Iridium and Strata code/abilities are untouched.

The lease captures the original Worldbreaker stack, original Curios slot and selected source. A replacement stack, empty slot or changed copiedSkill invalidates pending observation and active excavation, including a source change inside the current block-break callback. Queued excavation cannot recursively feed the copy. Source-specific cooldown state is shared with the real source so swapping copies cannot reset cooldown. Unsupported, invalid and self copies are rejected, not silently mapped to something else.

Seven GameTests cover copied Hellspec geometry/provenance/ore preservation/cooldown, unconfirmed breaks, slot removal before confirmation, current-cell revocation, changing source before confirmation and in a break callback, explicit source allowlist/self rejection, and copied Worldloom's ore-only trigger/own 156-cell roots. Local 141 Python (one skip without Java), 25 resource checks, diff check and 58 frozen hashes pass. Forge alpha.21 and dedicated action captures pending.

This is NOT completed global Curios copying: copied status/combat passives, equipped active controls, Icarus, Iridium and Strata Curios copying remain unimplemented. Worldbreaker retains its native defensive passive in this milestone; other five native modes still need reform. Existing native source behavior is not changed. No public release. Earlier alpha.19/20 CI results still pending at preparation time.
