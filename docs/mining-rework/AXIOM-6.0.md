# Axiom alpha.14 — full ability implementation, Forge validation pending

Primary: octahedral natural-rock implosion, Manhattan radius 18, 8446 unique candidates, outer facets inward. Preserves all ore and the central 3×3×3 cube, including the aimed block. Can anchor on natural rock or ore. No tunnel, block construction, fluid removal or item magnet. Native rock loot, execution-time claims, provenance, chunk skipping, configured block cap and drop backpressure remain in force. Custom Axiom particles reuse existing assets; item models/textures remain frozen.

Secondary: Null Cross, three intersecting circular planes of radius 16. Unlike the chamber, diagonal rock outside those planes remains intact. Preserves all ore and the same central cube. Public alternate dispatch and Worldbreaker's existing hand-copy dispatch use the new program rather than the legacy 11-cubed ore-exposure technique.

Curios: a confirmed manual natural ore scar triggers a radius-6 octahedral aftershock (350 candidates), every 8 seconds. Preserves other ore and central cube. Original-slot equipment lease and held-tool context are rechecked through permission and break callbacks; removing the Curios source stops even the current mutation. Existing Null Veil protection remains. No recursive activation from queued excavation.

## Validation and corrected failure

Alpha.13 source `7cdcbe6654a1d10c84771b8e81403d3b61cc4a77`, CI `44617863959` failed one GameTest: copied ore-anchored implosion did not reach the near-core test rock. The 8192 candidate cap truncated the 8446-position outer-in traversal. Alpha.14 counts successful block removals against the configured block cap instead, scanning all candidates unless that cap is reached. Thus air, ore and skipped cells do not prematurely truncate a sparse chamber. The default block cap remains 8192, not raised to hide the failure. The original failing test is retained.

Two additional JUnit geometry cases and four additional GameTests cover cross/core/diagonal geometry and Curios confirmation, cooldown, provenance, source removal and same-cell callback revocation. Earlier three GameTests and two JUnit cases remain. Local 140 Python tests (one skipped without Java), 25 resource validations and diff check pass. No Java runtime locally; alpha.14 Forge CI pending. No Axiom action screenshot inspected; not yet a validated identity. Global Worldbreaker Curios copying and remaining identities are not claimed complete. No public alpha release.
