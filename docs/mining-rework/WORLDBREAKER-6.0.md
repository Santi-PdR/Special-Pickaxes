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

## Alpha.22 — two more native modes implemented

Native mode 1 (`fracture`) now opens Fault Crowns rather than a widening forward excavation: three concentric cylindrical annuli at radii 6..8, 14..16 and 22..24, vertical offsets -6..6, 7228 unique candidates. Natural center and the spaces between crowns remain intact. Native mode 2 (`cleave`) opens World Split: two perpendicular vertical cuts, three blocks thick, horizontal radius 24 and vertical offsets -12..12, 6925 candidates; preserves rock diagonally between the cuts. Both harvest natural rock and ore with native loot and budgeted Worldbreaker signature VFX.

Both start paths retain the captured actor/tool context, post-callback checks and reentry guard. Execution rechecks current-cell claims, provenance, tool suitability, chunk loading, height and drop backpressure, capped by configured successful removals. Existing hand-copy dispatch runs before native mode dispatch and is unchanged. Stable mode keys/count are preserved so input/selection and saved NBT are not silently remapped.

Two additional JUnit tests cover exact volumes, uniqueness, traversal and boundaries. Two additional GameTests exercise public native mode dispatch, all crowns and both vertical planes, ores, player builds, preserved center/gaps and diagonal exterior. Local 141 Python (one skip without Java), 25 resource checks, 58 frozen hashes and diff check pass. Forge alpha.22 and dedicated action captures pending. Alpha.19/20/21 CI were still in progress when checked for this milestone; no result inferred.

Native first three modes and alternate are implemented, not yet all validated. Core Drill, World Shatter and Region Break retain previous implementations; Curios global copying is still partial as documented in alpha.21. No public release and no claim of completed global reform.

## Alpha.23 — native Curios mining and sparse-fixture isolation

Without a copiedSkill tag, equipped Worldbreaker observes confirmed manual natural ore mining and triggers Core Fracture every 8 seconds: 702 natural rock/ore candidates in a 9×9×9 cube excluding its central 3×3×3. Retains central rock and builds, native loot and standard execution safeguards. Native Worldbreaker defensive passive remains. Selecting a copied source, removing/replacing the original Curios stack or revoking its slot invalidates native queued work, including the current cell after callbacks. Invalid or unsupported copiedSkill tags do not silently fall back to native mining.

One JUnit tests immutable 702-cell geometry; two GameTests cover native public observation, rock/ore harvesting, core/provenance/cooldown and a copy selection during a block-break callback. Local 141 Python (one skip without Java), 25 resources and diff check pass; Forge alpha.23 and action captures pending. Copied combat/status passives, equipped active controls and Icarus/Iridium/Strata copying remain incomplete; native modes 3..5 remain previous implementations. No public release.

### Failed CI investigation

Alpha.19 CI `44633175036` and alpha.20 `44633627986` SUCCESS. Alpha.21 `44634804567` and alpha.22 `44635858293` failed the same sparse native Cataclysm harvest assertion. The target helper only clears a 17×9×17 fixture while Cataclysm scans 33×17×33; added tests can change generated-world layout around this larger footprint. Alpha.23 explicitly clears/unmarks every Cataclysm candidate in that sparse fixture before reseeding the asserted probes. Production caps, safety, target geometry and assertions remain unchanged; detailed probe states/remaining work were added to future failure messages. This is a suspected fixture-isolation cause, not a verified runtime fix until CI reruns. The retained tests must still pass. Not evidence to approve alpha.21/22 wholesale.

## Alpha.24 — Mantle Descent replaces native narrow Core Drill

Native mode 3 (stable key `core_drill`) now excavates four descending terraces around aimed natural geology: radii 12/9/6/3, offsets 0..-24, 6485 unique candidates, top-down. Uses native rock/ore loot and the shared claim/provenance/chunk/tool/backpressure checks. Starts from the aimed quarry rim, not automatically below the player's feet. Not a 192-block narrow drilling tunnel.

The queue explicitly recognizes Mantle Descent as a descent program for its existing fall guard, refreshes protection during work and preserves post-job landing grace. Existing DirectionalProgram Core Drill infrastructure and guard regressions remain supported. New JUnit checks volume, uniqueness, order and terrace boundaries; two GameTests exercise public mode dispatch, deep ore/boundary preservation, fall guard while working/after completion/until landing and callback reentry/relocation. Local 141 Python (one skip without Java), 25 resources, frozen hashes and diff check pass. Forge alpha.24/action captures pending. Alpha.23 `44637244361` still in progress when checked.

Native modes 0..3 and alternate now implemented. World Shatter/Region Break (modes 4/5) and complete Curios copying still outstanding; no claim of completed reform or public release.

## Alpha.25 — safety correction for remaining native modes

Region Break (mode 5) previously accepted arbitrary tool-effective blocks and used non-natural WorkStep.Mine. It now analyzes and harvests only eligible natural rock/ore, excludes player provenance and uses the natural mutation path that rechecks provenance after external break callbacks. Existing selection, snapshot budget, review/confirmation, native drops and normal skip behavior remain. This intentionally preserves cuboid selection as a useful precision tool, not a new geometry advertised as a redesign.

World Shatter (mode 4) keeps its existing enormous terraced wedge and barrier preflight, but now rejects player-placed and non-geological blocks during preflight/execution and uses natural queued mining. Such obstacles stop the route rather than demolishing a build. Existing wedge length, slice geometry, controls and client wedge regression stay unchanged. This is a safety rework, not a claim that the retained wedge is a new mechanic.

Three new GameTests cover region natural rock/ore versus player stone/non-geology, provenance added inside the current Forge break callback, and wedge stopping at player-built stone. Local 141 Python (one skipped without Java), 25 resources and diff check pass. Forge alpha.25 and action captures pending. Worldbreaker native mode implementation/safety now has explicit coverage across all six modes; this is not global completion: full Curios copying/controls, tuning and dedicated visual review remain outstanding.

Alpha.23 source `b122931ea89ff713d94d313a79c8f35814362085`, CI `44637244361` SUCCESS: 213 JUnit without failures/errors/skips, 515 GameTests, client/input and packaged server. Confirms the isolated Cataclysm fixture and native/mining-copy Curios milestone. Does not validate alpha.24/25. Alpha.24 CI `44637858136` still running at preparation time. No public release.

## Alpha.26 — source-specific Curios status/combat passives implemented

Equipped Worldbreaker now resolves the copied source for ordinary passive ticking and hostile-hit reactions, replacing rather than stacking native Aegis with the copied source. All twelve non-self playable sources resolve, including preserved Iridium/Strata passive implementations without changing their native algorithms. No selection means native Worldbreaker as before; invalid/self copies resolve to no copied passive.

A synchronous original-stack/original-slot/source lease surrounds copied ticking and hurt dispatch. Aura owner and reaction context checks include the active lease; source mutation in permission callbacks cannot retaliate or spend the old cooldown. Effect callback revocation stops subsequent grants/shares and removes owned source effects using the existing non-destructive unequip cleanup. Ordinary source changes use the existing passive-source tracker and cleanup. Shared Heartwood/Null Veil checks recognize copied sources to avoid applying their shared fallback and owner reaction together. Source cooldowns remain shared with the original pick; no new native source mutation or damage multiplier inflation.

Five GameTests cover copied shell instead of native Aegis, source cleanup, revocation during effect grants, source mutation inside combat permissions, actual infernal combat reaction instead of native absorption, and native/copied source resolution/self rejection. Local 141 Python (one skip without Java), 25 resources, frozen hashes and diff check pass. Forge alpha.26 and dedicated copied-passive visuals pending. Existing direct native picks retain their dispatch; no frozen Iridium/Strata files changed.

Still NOT complete global Curios copying: equipped active controls and remaining source-specific mining triggers (Icarus/Iridium/Strata) need separate work. This milestone does not imply that every asynchronous effect implementation has undergone dedicated copied-source stress testing. No public release or completed reform claim.

## Alpha.27 — copied Iridium mining and full-run CI transcript

Curios Worldbreaker now forwards copied Iridium's confirmed manual natural ore scar into the existing Iridium surge/new-loot ownership path. Uses the same source/slot lease as other mining copies; changing source before confirmation invalidates the observation. Holding native Iridium suppresses duplicate copied observation. Two GameTests cover the real surge and new drop owner tag, plus changed-source rejection; the explicit supported-source regression now includes Iridium. No native Iridium/Strata implementation or frozen files changed. Icarus/Strata remaining mining-trigger parity and equipped active controls are still outstanding; not complete global copying.

Alpha.26 CI `44641371608` compiled and passed 214 JUnit (zero failures/errors/skips), all 525 GameTests, and a strict scan of its full GameTest transcript. Overall CI still failed in the post-GameTest step, before packaged-server/client validation. The run crossed midnight: the pass summary is at 23:59:55, shutdown extends through 00:00:10. The workflow previously required the summary inside potentially rotated latest.log. Alpha.27 requires the exact complete count/pass summary in the unrotated tee transcript and strictly scans BOTH latest.log and the full transcript for errors. No failure pattern or expected count is relaxed. A Python regression demonstrates that shutdown-only rotated logs fail and a complete transcript passes. Exact remote post-step failure log was unavailable via CDN; rotation is the suspected cause, not independently confirmed.

Local 142 Python tests (one skip without Java), 25 resources, diff check and 58 frozen hashes pass. Forge alpha.27 and dedicated runtime visuals pending; no public release and no overall completion claim.
