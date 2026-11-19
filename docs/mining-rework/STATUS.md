## Reforma 6.0 — Alba alpha.3 validada, reforma global incompleta

Runtime `d48cc5364253a6daee0a74678a92fe9a326c7996`, CI `44450388632` **SUCCESS**: 173 JUnit sin fallos/errores/omisiones, 437 GameTests, cliente 32 escenas/64 tooltips y servidor empaquetado aprobados. Python 138 (una omisión sin Java), recursos 25 correctos; 58 hashes congelados intactos. Rayos de ocho brazos y nova de radio 12 sustituyen veta y sondeo; radar retirado, Glowing conservado y añadido a variante Curios con lease del slot. Defensa Curios del 65% cada ocho segundos. [Límites, fallos corregidos y alcance](DAWNBREAKER-6.0.md). No se inspeccionó una nueva captura de acción solar; la comprobación gráfica automatizada no equivale a una revisión estética completa. Quedan nueve identidades y la copia Curios global de Worldbreaker.

## Reforma 6.0 — Ícaro alpha.2 validado, reforma global incompleta

Runtime `98f5c06c900461b16cca18d1a177c3d178adf231`, CI `44442689638` SUCCESS: **177 JUnit** sin fallos/errores/omisiones, **436 GameTests**, cliente 32 escenas/64 tooltips, servidor empaquetado y vuelo real de supervivencia por tecla X aprobados. Local: 138 Python (una omisión sin Java), 25 recursos correctos. Ícaro: alas naturales de 32 bloques / 37–49 de envergadura sin desplazamiento, impulso de 32 ticks por velocidad, Pluma temporal 120 ticks, variante aérea Curios, copia en mano y hélices custom activas. Ascensor/anclas TP retirados completamente; se sustituyeron sus pruebas, no se cambiaron Iridium ni Strata. [Detalle, límites, fallos corregidos y captura real](ICARUS-6.0.md).

Vuelo observado: subida 14,21545 bloques, máximo paso entre muestras servidor 1,30760; no confundir muestreo de paquetes con velocidad impuesta por tick. Captura ES inspeccionada: `review/icarus-ascent-6.0.0-alpha.2-es.jpg`. Modelos/texturas de picos intactos según hashes. JAR alpha 824530 bytes, SHA256 `941f79ecb3cdde57c816d872740533fc82cfa25b8951a355c0066bae3fb1bbe0`. No release final: quedan otras diez identidades y variantes/copia Curios global. La base de interfaz se conserva.

## Reforma 6.0 — base alpha.1, incompleta

Fuente `5d0c8491d72f13de1fd6b702267f75b90d08af39`. CI `44433168671`: SUCCESS; 179 JUnit sin fallos/errores/omisiones, 426 GameTests, cliente 32 escenas/64 tooltips y servidor empaquetado aprobados. Local: 137 Python (una omisión sin Java), 25 recursos correctos. Eliminación efectiva del renderer de HUD, tooltips idénticos con/sin SHIFT, errores breves, catálogo completo es_uy, tarjeta Curios actual en I y render real ampliado. Arte y clases dedicadas de Iridium/Strata congelados por SHA256. Capturas finales ES de manual/SHIFT inspeccionadas en `review/*6.0.0-alpha.1-es.jpg`. [Contrato, historial de correcciones y trabajo pendiente](REFORM-6.md).

No se reemplazaron todavía las 11 identidades ni sus variantes Curios. No se presenta la tabla de diseño como habilidades implementadas. No publicar alpha como reforma final. El enfoque de calzada 5.19 queda sustituido por la petición mayor del usuario; aunque su CI `44426271408` pasó, eso no valida 6.0 ni convierte su diseño en el objetivo nuevo.

## 5.18.0 — instalable personal, CI aprobada

Fuente `5c10ccb452afbeb667cd4c2e4ef8e4c65eed5282`, CI `44416777028`: SUCCESS. 178 JUnit sin fallos/errores/omisiones, 420 GameTests obligatorios, 32 escenas/64 tooltips, servidor empaquetado y logs aprobados. Kit de 13 picos con rechazo íntegro si falta espacio, HUD adaptable y causas de pausa localizadas, lectura de cooldown coherente con validación de servidor. [Instalación, descarga y límites](PLAY-5.18.md).

JAR 804691 bytes, SHA256 `c47921400e6865fcdfa86c96b958bd8e07b6d14bb78a5eec191570554e5ee54c`. Artefacto personal `14480089792`, evidencia `14480139804`, retención 7 días. Posteriormente se publicó el JAR exacto por solicitud expresa: release `test-5.18.0-44416777028`, workflow `44421388453` aprobado, tamaño y digest confirmados por API. Primer run falló en fixture preexistente de Ícaro; segundo pasó con diagnósticos más estrictos, sin relajar aserciones. Causa intermitente sin confirmar. No certificación Windows/modpacks ni 50 nuevas mecánicas.

## 5.17.0 — guía activada de sondeo, CI aprobada

Runtime `fc1fff5481e7fff576fb87c1d46fa035b7999dcc`, [CI 44390344841](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44390344841): SUCCESS. **175 JUnit**, cero fallos/errores/omisiones; **418 GameTests**; cliente gráfico 32 escenas/64 tooltips, servidor empaquetado y logs estrictos aprobados. Local: 132 Python sin fallos, una omisión sin Java; 25 recursos correctos.

Alba Eterna y su copia de Rompemundos pueden guiar durante diez segundos al depósito sondeado: dirección/distancia/desnivel y barra temporal. Cancelación con la secundaria registrada; sin nueva búsqueda pasiva, no X-ray, no partículas ambientales ni autoridad de minería devuelta por el cliente. Fuente autorizada, dueño, caducidad, distancia/dimensión y cambios de claim/mena revalidados. Ocho GameTests y cuatro JUnit nuevos.

Smoke gráfico pulsa la secundaria real, espera un readout válido del cliente y captura `survey-guide`, después activa la primaria real. Captura real 1280×720/GUI640×360 inspeccionada y guardada como `review/survey-guide-5.17.0-es.jpg`; todos los campos visibles y mira despejada. Sólo esa escena/idioma inspeccionada, no todas las escalas. Ver `SURVEY-5.17.md` para hash/procedencia y límites.

JAR `special-pickaxes-1.20.1-5.17.0.jar`: **801313 bytes**, SHA256 `ff7567bce1be636232f037d5c5e4ca05fa64c07b94553f8548faa0e0550779fc`. Personal `14468589157`, evidencia `14468394369`, retención 7 días; sin release pública. Evidencia temporal `/tmp/sp517green`. Una mejora funcional adicional a las 47 del inventario histórico de 5.16, sin inflar campos/tests/capturas. Sin nuevos assets de picos ni certificación Windows/Ghouls.

## 5.16.0 — recuperación sostenida, arquitectura y HUD, CI aprobada

Runtime `b904be503ed9a19cc9e34039f57d949abed56928`, [CI 44383199553](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44383199553): SUCCESS. **171 JUnit**, cero fallos/errores/omisiones; **410 GameTests**; cliente gráfico 32 escenas/64 tooltips, servidor empaquetado y logs estrictos aprobados. Local: 131 pruebas Python sin fallos, una omitida por falta de Java; 25 pruebas de recursos correctas. Evidencia temporal `/tmp/sp516green`.

Ocaso/Iridio mantienen ocho segundos una recuperación activa y acotada de sus objetivos seleccionados, sin teleport/robo/colisiones desactivadas, revalidando claims y contexto por intervención. Telar construye escaleras vanilla ascendentes/descendentes pagadas y con gálibo, sin ampliar los estados de transmutación de Crisol. HUD server-owned con dos cooldowns, progreso y actividad. Ver `GAMEPLAY-5.16.md` y el inventario acumulado `IMPROVEMENTS-47-AUDIT.md` (47 cambios, no 47 mecánicas; no incluye tests/atajos/fotogramas).

Primer run 44380776387 pasó 171 JUnit/410 GameTests pero falló el smoke gráfico en la primera escena. Captura mostraba la mena fuera de mira. El fixture ahora restaura una vez su pose mediante handshake después de cerrar el manual; la activación sigue usando tecla real y paquete autorizado. Sin reintentos ni éxito sintético, sin cambiar el raycast del mod. Segundo run completamente verde.

Dos capturas reales del HUD inspeccionadas/conservadas como `review/hud-{palimpsest,eventide}-5.16.0.jpg`, 640×360: textos/barras caben y mira despejada; no todos los estados/escalas. JAR `special-pickaxes-1.20.1-5.16.0.jar`: **793992 bytes**, SHA256 `623c2ca70156c84649fe2ef09b1e6763f21005195a7d662bd546c85c1a9c2caf`. Personal `14465283390`, evidencia `14465963235`, retención 7 días. Sin release pública ni certificación Windows/Ghouls.

## 5.15.0 — impactos activos y dos originales Malum, CI aprobada

Runtime `a527ffbfd83931c57af9266e0865ef3eca3e60d1`, [CI 44375291707](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44375291707): SUCCESS. 168 JUnit, cero fallos/errores/omisiones; 400 GameTests; cliente gráfico y servidor empaquetado aprobados, logs estrictos. 130 pruebas Python locales sin fallos (una omitida sin Java), 25 pruebas de recursos correctas. Evidencia temporal `/tmp/sp515green`.

Impactos custom en el frente de las colas activas y remates exitosos, con geometría por estilo, presupuesto compartido con casts. No partículas ambientales ni emisión durante pausas. Dos sprites originales Malum importados byte por byte para Alba Eterna/Axioma, SHA/commit/procedencia actualizados, sin inferir permisos de redistribución. Capturas reales de ambos en mano inspeccionadas y conservadas en `review/portrait-*-5.15.0.jpg`.

JAR `special-pickaxes-1.20.1-5.15.0.jar`, 782740 bytes, SHA256 `ce337b3b683d135c4860d8a8b18f8f27db0552b0781b9384cbc83068e1eda33e`. Personal `14462437214`; evidencia completa `14462112977`, 7 días. No publicación pública. Ver `EXCAVATION-5.15.md`. No se afirma reforma de 50 completada, nueva mecánica de excavación ni revisión visual de todas las animaciones.

## 5.14.1 — firmas primarias/secundarias, validación completa aprobada

Runtime `8722f0c4b59a419da6637cc1c444be06a83d52f0`, [CI 44367188726](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44367188726): SUCCESS. **166 JUnit**, cero fallos/errores/omisiones; **400 GameTests obligatorios**; cliente gráfico (32 escenas/64 tooltips) y servidor empaquetado aprobados, incluidos logs estrictos. Evidencia recuperada a `/tmp/sp5141green` (temporal). Local: 128 pruebas Python sin fallos, una omitida por falta de Java local; 25 pruebas de recursos correctas.

26 firmas registradas exclusivamente custom: primaria/secundaria por pico, 104 imágenes de partículas de 32×32. Símbolos, trayectorias y siluetas secundarias diferentes; 13 sonidos secundarios distintos de la primaria. Límite compartido 16 por cast/256 globales por tick, una ráfaga por jugador/tick. GameTest comprueba registros únicos y cobertura de los 13 picos reales. No VFX ambientales, no iconos de pico dibujados, no nuevas texturas de objetos integradas. No presentar el número de fotogramas como mejoras independientes ni esta tanda como la reforma de 50 mejoras terminada.

5.14.0 falló por cargar registros de sonido desde un test unitario puro. Comprobación real trasladada a GameTest; test geométrico sin bootstrap. Primer 5.14.1 detectó un Holder de sonido sin `.value()`, corregido. No se omitió ningún test Java ni se redujeron las 400 pruebas para obtener el verde.

JAR `special-pickaxes-1.20.1-5.14.1.jar`: **778556 bytes**, SHA-256 `9912f571f45c6e412fad1f6f34474ed670a8d63f1968d00bc4a740f5567636bc`. Artefacto personal autenticado `14459196289`, evidencia completa `14459910206` (retención 7 días). Sin nueva release pública. El smoke gráfico no equivale a inspección de las 26 animaciones en movimiento, aprobación visual del usuario ni prueba Windows/Ghouls.

### Historial

## 5.13.0 — first broad-review batch verified

Runtime `6aed2b07962f8511d86c4061a1b8e1181774c033`, [CI 44234259503](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44234259503): SUCCESS. 161 JUnit, zero failures/errors/skips; all 400 required GameTests; graphical client and packaged server checks passed with strict log validation. Local 125 Python tests without skips, 25 resource checks and secret scan passed. Evidence retrieved to `/tmp/sp-ci5130-green` (temporary).

Explicit scope: 20 applied changes and 30 pending proposals in `IMPROVEMENT-BATCH-5.13.md`, not 50 completed improvements. Includes pending 5.12.2 keyboard navigation. Immutable mode catalog preserves all saved keys/order and defensive-copy compatibility; a new real GameTest covers every playable kind and extreme mode values. Copy selector navigation/preview/cache/input coordination improved; no registry, asset, VFX or power changes.

The Icarian departure test first failed only its second-block assertion, while departure and position checks passed. Its isolated elevated fixture now proves exactly one pre-departure step and an intact second block before departure; final checks are unchanged and full CI passed. No production lift behavior was modified.

Inspected final Worldbreaker manual screenshot at 1280×720 / GUI 640×360: keyboard help fits without card overlap. Unmodified capture saved as `review/manual-5.13.0-es.jpg`, provenance in `INTERFACE-5.12.md`. Limited visual inspection, not all scales/locales or physical keyboard paths; no Windows/Ghouls certification.

JAR `special-pickaxes-1.20.1-5.13.0.jar`: 593475 bytes, SHA-256 `29e72e8582dcbb9a6b8da144a738a7f240644535c866122620a8511577a8aa22`. Personal authenticated artifact `14397750757` (7 days), full forge-validation artifact `14397940289`. No public release.

### Historical progress notes

## 5.13.0 — broad review first batch; validation pending

User requested roughly 50 improvements and 20–30 necessary changes. `IMPROVEMENT-BATCH-5.13.md` records 50 explicit work points: 20 applied changes and 30 pending proposals; tests are not counted as extra features. First batch extends keyboard copy navigation, preview/invalid-NBT handling, input coordination, context-aware help/cache and allocation reduction through an immutable mode catalog. Saved mode IDs/order and registry/assets/VFX remain unchanged. Copied regional SELECT uses the stack's saved mode. The previous 5.12.2 independent Java model CI passed, but Forge run 44212796462 failed the historical Icarian departure regression; its fixture now explicitly clears natural-block provenance and splits assertions for diagnosis, without changing production lift logic. Local 125 Python tests and 25 resource checks passed. First full run 44233460425 compiled and passed the new catalog contract but the historical Icarian departure test still failed: split assertions localized it to the second shaft block, not departure or position. Elevated that fixture away from other test terrain and added pre-departure assertions for exact budget/step count and the untouched second block. No production lift change or weakened final assertion. Full CI pending; 5.12.1 remains the latest fully verified runtime.

## 5.12.2 — keyboard copy picker; validation pending

Worldbreaker manual copy picker now supports Left/Right focus and Enter/keypad Enter confirmation. Browsing does not send a copy intent. Focus is highlighted and revealed inside narrow scrolled strips. Enter repeats are suppressed until release, preventing held-key copy spam; mouse selection remains supported. EN/ES header explains the keyboard controls. Added four pure navigation JUnit tests and model-runner coverage over every viewport 24–760 and all thirteen choices. Local 122 Python tests without skips, 25 resource checks, secret scan and diff whitespace checks passed. Runtime candidate `bd1e32ce4eb2291d5eb2085cbc24b8c39e053c62`; Forge run `44212796462`, independent Java model run `44212796486`. Status monitoring stopped after GitHub returned API rate-limit HTTP 403; outcomes unconfirmed, no release promotion or graphical keyboard-interaction claim. 5.12.1 remains the latest verified build. No ability, registry, asset or VFX changes.

## 5.12.1 — complete CI verified

Runtime `92561baa0d0e1ff43fa89f9eded7b2ea33c1c7e2`, [CI 44208279107](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44208279107): SUCCESS. 157 JUnit, zero failures/errors/skips; all 399 required GameTests; graphical client and packaged server checks passed. Local 122 Python tests without skips, 25 resource checks and secret scan passed. Retrieved evidence: `/tmp/sp-ci5121-green` (temporary).

All active mode labels now have readable EN/ES names; no NBT mode IDs/order changes. Manual adds small-step Up/Down scrolling, preserving other navigation. Inspected the final actual Worldbreaker rounded-tunnel manual screenshot at 1280×720 / GUI 640×360: localized mode label and readable cards, selector and footer. Unmodified reference image saved under `review/manual-5.12.1-es.jpg`, provenance/hash in `INTERFACE-5.12.md`. Limited scene inspection only, not blanket visual/user approval or Windows/Ghouls certification.

JAR `special-pickaxes-1.20.1-5.12.1.jar`: 590273 bytes, SHA-256 `4265d89dc6206fd4742941eaee8992a250c3d6f117804bdc7018b084708ecc55`. Personal authenticated artifact `14384759934` (7 days), full forge-validation artifact `14385682682`. No public release.

### Historical progress notes

## 5.12.1 — localization and keyboard polish; validation pending

Visual inspection of two real 5.12.0 manual screenshots found remaining raw English mode labels in Spanish (VEIN/CARVE). All active mode labels now use concise EN/ES display names, without changing NBT mode keys or order. Added Up/Down small-step keyboard scrolling, retaining wheel/trackpad, page, home/end and drag behavior. Two new presentation tests cover readable active-mode labels and arrow hints. Local 122 Python tests and 25 resource checks pass; full CI pending. Latest fully verified build is 5.12.0.

## 5.12.0 — complete CI verified

Runtime `ebea26e8a6425e0e794cfcd509a0b0646ec71912`, [CI 44197698594](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44197698594): SUCCESS. 157 JUnit, zero failures/errors/skips; 399 required GameTests; graphical client and packaged server checks passed, including strict logs. This also validates the 5.11.12 survey optimization. Evidence retrieved to `/tmp/sp-ci5120-green` (temporary).

Inspected actual `manual-palimpsest-vein.jpg` and `manual-worldbreaker-carve.jpg` screenshots, 1280×720 / GUI 640×360: readable card hierarchy, copy selector and footer without overlap in those scenes. This is limited agent visual inspection, not user approval, not all modes/scales/locales, and not Windows/Ghouls compatibility certification.

JAR `special-pickaxes-1.20.1-5.12.0.jar`: 590269 bytes, SHA-256 `a0b184f99bfa4f273f76a0f373139c3960a6ecee58fbf7d78931a55f644a5c7a`. Authenticated personal artifact `14379864415` (7 days), full validation artifact `14380154242`. No public release.

### Historical progress notes

## 5.12.0 — interface and naming revision; validation pending

User-requested presentation pass: shorter coherent names for the 13 playable picks in EN/ES, without registry/model/save ID changes or asset edits. Manual header now emphasizes the active mode and ability accent; numbered cards separate primary, secondary and controls. Width-bounded text uses ellipses, including the live-key footer and copy selection, avoiding overlap at compact scales. Added an overflow navigation hint. SHIFT instructions reuse concise mode-specific functional summaries and remove retired border/particle promises. Core dimensions, ore limits and payment descriptions remain explicit. Four localized presentation regression tests added. This includes the pending 5.11.12 survey optimization. First CI 44194792592 compiled and passed all 399 GameTests; graphical validation exposed a smoke assertion coupled to old prose punctuation (` key.`) rather than the new key-led label (`key · summary`). Updated that assertion to require the exact new label while retaining real key rebinding and placeholder checks. Prioritized two real manual captures in the bounded evidence export for visual inspection. Final CI 44197698594 on `ebea26e8a6425e0e794cfcd509a0b0646ec71912` started; status monitoring stopped when GitHub returned API rate-limit HTTP 403 (not an authentication request). Outcome currently unconfirmed. Local 120 Python tests, 25 resource checks and secret scan passed. Naming table and interface scope: `INTERFACE-5.12.md`. Full clean Forge/client CI pending; 5.11.11 remains the last fully verified build. No manual visual approval or Windows/Ghouls certification.

## 5.11.12 — validation pending

Palimpsest survey now uses shared prewarmed distance-ordered offsets and stops after the complete distance group containing the 32nd eligible ore. Preserves exact world-coordinate `asLong` tie ordering, loaded/natural-ore filtering, immutable results and existing post-selection claim/context validation. Dense all-ore geometry requires 33 predicate inspections rather than 2,197; sparse/empty scans remain bounded by the same 13³ cube. No radius/result-cap changes, no new VFX, and no measured wall-clock claim. Added three unit tests (80 seeded oracle comparisons, signed-coordinate ties, empty scan, mutability) and a real-world dense/sparse oracle GameTest. Runtime commit `a44667937b950dd5cbb826e1fc598eb965f4e18c`: CI 44188568265 compiled and passed 157 JUnit (zero failures/errors/skips), all 399 GameTests, packaged server and every graphical scene. The overall run FAILED strict graphical runtime-log validation: Mojang profile/skin lookup for the disposable ArtifactTester received HTML instead of JSON, producing AuthenticationUnavailableException. No validator relaxation or release promotion. Evidence retrieved to `/tmp/sp-ci51112-failed` (temporary). Local 116 Python tests (no skips), 25 resource checks and secret scan passed. Full clean CI remains pending; 5.11.11 is still the last verified build.

## 5.11.11 — complete CI verified

Runtime commit `b8ea8da5e484be2cb04b4eef7c2a1c742fb91b7d`, [CI 44181772480](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44181772480): SUCCESS. 154 JUnit with zero failures/errors/skips, all 398 required GameTests, packaged server and graphical client checks passed, including strict runtime-log validation. Local 116 Python tests (no skips), 25 resource checks and secret scan passed. Retrieved evidence: `/tmp/sp-ci51111-green` (temporary, not a durable publication).

Hellspec primary grows from 353 to 1,679 cells (~4.76×), retaining the whole previous quarry. Five-petal envelope is 25×7×25 with radius 12 and half-height 3. Exact surface geometry is cached immutable and derived from the same predicate as mining. A full-volume GameTest harvests exactly 1,675 cells after four exclusions: player-placed rock, denied claim, machine and bedrock. Outer-petal ore is harvested and an immediately exterior block is untouched. Three JUnit tests prove volume, symmetry, old-volume retention and exact six-face boundary. EN/ES summaries updated. No combat change, fluid/lava placement or new particle emissions.

Initial CI 44177744653 completed gameplay and graphical scenes but failed the strict log scan due to Mojang authentication returning HTML instead of JSON. No log filter was relaxed. GitHub refused an explicit rerun; after adding the previous-quarry containment test, the next full run passed cleanly. Neither successful graphical smoke nor this evidence is manual visual approval or Windows/Ghouls certification.

JAR `special-pickaxes-1.20.1-5.11.11.jar`: 590258 bytes; SHA-256 `5ef01cbb5e801deef17fce42c2f0dbf4d515564c21988757119a2c5a95dec615`. Authenticated personal artifact `14374521019` (7 days), full forge-validation artifact `14372844769`. No public release created; third-party artwork redistribution remains unapproved.

### Historical progress notes

## 5.11.11 — validation pending

Hellspec primary expanded from 353 to 1,679 candidate cells (five overlapping ellipsoidal petals, envelope 25×7×25, radius 12 and half-height 3). Mining remains scheduler-budgeted with existing natural-block, ore, claim, machine, bedrock and drop-pressure protections. Shared radial geometry and cached immutable six-face boundary use the same definition. No combat buff, fluids, lava placement, ambient VFX or new particle emissions. EN/ES summaries updated. Added three geometry/boundary JUnit tests (including retention of all 353 previous cells) and a full-volume Minecraft safety regression; updated the historical shape contract to the new intended bounds. CI 44177744653 passed 153 JUnit and all 398 GameTests, packaged server and every graphical scene. Strict client log validation failed on Mojang authentication service unavailability (HTML response parsed as JSON), not a mod exception. Kept the strict scanner unchanged; requested rerun was refused by GitHub. Added the previous-volume containment test and triggered fresh full CI. 5.11.10 remains the verified build until a completely clean run.

## 5.11.10 — complete CI verified

Runtime commit `45d5cbbe02e4814c807d36458b0460ed4ded5eff`, [CI 44170280427](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44170280427): SUCCESS. 151 JUnit with zero failures/errors/skips, all 397 required GameTests, packaged-server smoke and graphical-client smoke passed. Client log confirms 32 manual renders and 32 closes. Local 116 Python tests (no skips), 25 resource checks and secret scan passed. Retrieved evidence: `/tmp/sp-ci51110-green` (temporary, not a durable publication).

Radial abilities now use prewarmed immutable plans, eliminating per-cast volume counting and shell searching from scheduler peeks. All four shapes and radii 0–12 are compared with historical traversal; geometry, ordering, attempt caps and execution-time protections are preserved. Coordinate objects are shared across plans; cursors and centers remain per cast. Copied manual-header icons share the existing bounded display cache. No measured FPS/timing claim, no potency reduction, no new particles or combat changes.

First CI 44167246767 passed JUnit/GameTests/server but failed graphical activation after a manual capture (no input edge logged). Client smoke now waits for real render and close acknowledgements before pressing activation; no retry or synthetic success. Four Python ordering tests enforce the handshake. The final CI passed every graphical scene; this is automated smoke coverage, not manual visual approval or Windows/Ghouls certification.

JAR `special-pickaxes-1.20.1-5.11.10.jar`: 590073 bytes; SHA-256 `49b497246fab43a7caf87a4924d1209df2c7c566e0eef5ca05ae48d62634fccc`. Personal authenticated artifact `14369880511` (7 days), full validation artifact `14369501040`. No public release created; third-party artwork redistribution remains unapproved.

### Historical progress notes

## 5.11.10 — validation pending

Replaced per-activation radial candidate counting and repeated shell searching with shared immutable geometry prepared during mod setup. Every shape/radius preserves the original Chebyshev-shell / x / y / z traversal. Coordinates are shared between plans; each cast owns only its cursor and center. Configured attempt caps and execution-time permission, provenance and drop-pressure checks remain unchanged. Completion at the final candidate remains exact. Added exact-order/unit invariants for all four shapes and radii 0–12, plus a Minecraft independent-cursor/config-cap regression. Manual copied-header icons now reuse the same bounded display cache as the picker instead of allocating ItemStacks per render. No new VFX or power/geometry changes. CI 44167246767 passed 151 JUnit, all 397 GameTests and the packaged server. Client smoke failed waiting for an activation edge after the manual capture; no edge was logged. Replaced fixed-delay manual open/close sequencing with render and close acknowledgements, fail-closed with no retry, and four Python state-order tests. Full validation pending; 5.11.9 remains the last verified build.

## 5.11.9 — complete CI verified

Restored the alternate-started feedback for the streaming Choir/Hellspec jobs, reporting the actual configured number of scheduled cells without building a preview list or scanning the world. Added a Minecraft ceiling-boundary regression for both shapes and a geometry translation test over every cell in all horizontal orientations. No extra particles, combat changes or permission relaxations. Verified runtime commit `462e6ba97649bc7938f775fb611b637874d9a90e`: [CI 44162211767](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44162211767) SUCCESS; 148 JUnit with zero failures/errors/skips, all 396 required GameTests, packaged server and graphical client checks passed. Local 112 Python tests passed without skips, 25 resource checks and secret scan passed. Evidence retrieved to `/tmp/sp-ci5119-green` (temporary). No manual visual, Windows or Ghouls approval.

JAR `special-pickaxes-1.20.1-5.11.9.jar`: 585686 bytes, SHA-256 `89873dc47136ce970fe108c9ea6b07aeed0506a2c472f3a78c36b12fc35ac01d`. Authenticated personal artifact `14365576665` (7 days), full forge-validation artifact `14363909299`. No public release.

## 5.11.8 — complete CI verified

Runtime commit `61101827ef922bfb53f1fe6f9b781e4e9c66b3f2`, [Forge run 44157295693](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44157295693): SUCCESS. 147 JUnit (zero failures/errors/skips), all 395 required Minecraft GameTests, packaged server and graphical client smoke checks passed. Evidence retrieved to `/tmp/sp-ci5118-green` (temporary workspace location, not a published archive). No manual visual approval or Windows/Ghouls compatibility claim.

Choir alternate streams a roof-clipped 9×7×48 vault (2,832 cells); Hellspec alternate streams an 11×11×48 chimney (5,808 cells). Constant plan memory and scheduler-budgeted execution. Regression coverage includes exact geometry, natural/placed blocks, denied cells, machines, bedrock, ore harvesting, unloaded chunks, drop-pressure cursor preservation/resume and callback tool swaps. EN/ES tooltips and manual descriptions match. No combat or ambient-VFX changes.

The first full run (44155208932) passed the new excavation tests but failed the historical Worldloom root timing fixture. Isolated that fixture using fixed-size, separated, gravity-free targets and explicit velocities; no production snare changes. The following compile caught an unqualified test cleanup reference, corrected before the final green run. Neither failed run is release evidence.

JAR `special-pickaxes-1.20.1-5.11.8.jar`: 585604 bytes; SHA-256 `299c30b1f31e5c6a1633f0fa76b7f6c20e85567aa24b5fd17727093bce4cb540`. Authenticated personal artifact `14363880574`, retained 7 days; full forge-validation artifact `14363601441`. No public release created; third-party artwork redistribution remains unapproved.

### Historical progress notes

## 5.11.8 — validation pending

Choir alternate now streams a 9×7×48 roof-clipped vault (2,832 cells); Hellspec streams an 11×11×48 chimney (5,808 cells). Constant plan memory; scheduler-budgeted execution and existing natural-block, claims, harvest, chunk and drop-backpressure checks. Three geometry unit tests and five Minecraft regression tests added, including cursor-preserving drop backpressure/resume and tool swaps inside claim callbacks. EN/ES descriptions updated. No combat-radius changes or new ambient effects. CI 44155208932 compiled successfully: 147 JUnit passed and the five new excavation GameTests passed. Full runtime validation failed in the historical Worldloom root timing test. Its fixture now uses a fixed-size slime, separated airborne targets and controlled velocity rather than random size, collision overlap and physics drift. No production snare logic changed. Full CI rerun pending; 5.11.7 remains the last verified build.

## 5.11.7 — calzada de 64 bloques validada

Commit probado `bb023afbe741a5e1058bfe046c6da693de4d8b5a`.
CI https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44146556631 — success.
144 JUnit sin fallos/errores/omitidos; 390 GameTests requeridos aprobados;
cliente gráfico y servidor empaquetado aprobados. Local: 112 Python sin omitidos
con Pillow, 25 recursos; secrets 322 sin firmas.

Nuevos GameTests confirman 64 colocaciones por 64 bloques de offhand, reserva
de inventario intacta y celda 65 intacta. Denegación en celda 40: se detiene
con 39 colocaciones y 25 bloques restantes, sin saltar el claim.

JAR `special-pickaxes-1.20.1-5.11.7.jar`: 580224 bytes.
SHA256 `3131a2a7e15f22f5e671a5176703f20bf32484691e8f2a93fed545d2dc7c1dca`.
Descarga autenticada temporal (7 días):
https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44146556631/artifacts/14357583794
No hubo release pública nueva. No certifica Windows/Ghouls ni todas las animaciones.
El subconjunto Checks no retuvo manual-worldloom: no se afirma inspección visual
nueva de esa escena. La CI de modelos puros anterior sigue como evidencia separada,
no se atribuye un rerun nuevo a este commit que no cambia esos modelos.

## 5.11.7 — calzada Worldloom de una pila completa (CI pendiente)

Secundaria ampliada de 32 a 64 bloques, constante BRIDGE_LENGTH. Conserva
pago exacto solo desde mano secundaria, rechazo de material con NBT,
planificación sin saltar obstáculos y cancelación al fallar un paso.
No carga chunks ni cambia permisos, materiales o efectos de combate.
Textos bilingües de secundaria actualizados a 64.

Dos GameTests nuevos: colocar exactamente 64 con una pila de offhand sin tocar
reserva de inventario ni celda 65; denegar celda 40 y exigir solo 39 pagos,
25 bloques restantes en mano y ninguna colocación a partir de la zona denegada.
La versión verde anterior sigue 5.11.6 hasta completar esta nueva cadena CI.

## 5.11.6 — cadena completa en verde

Commit probado: `95fe6729e0483043d2e906d31b0001f20652ac4f`.
- Forge CI: https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43998074898 — success.
- 144 JUnit: cero fallos, errores u omitidos; incluidos los cinco de ManualWheelScroll.
- 388 GameTests requeridos aprobados: incluye Worldloom 2350/2352 celdas con mena
  y roca colocada intactas, límites exactos y parada de sección ante máquinas.
- Cliente gráfico y servidor empaquetado aprobados; 32 manuales renderizados.
- CI independiente: https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43998074916 — success.
  Compila producción con Java 17 y exige más de 10.000 comprobaciones de modelos.
- En este run Forge el wrapper usó un solo intento. Los bloqueos de dependencias
  anteriores son históricos: no se deben presentar como el estado actual.

JAR `special-pickaxes-1.20.1-5.11.6.jar`: 580190 bytes.
SHA256: `f6396605365a18a8a990cb0a5586c4b9724787e95c9ab2790e9a304b199870b6`.
Descarga autenticada temporal (7 días):
https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43998074898/artifacts/14291349249
Evidencia de modelos puros:
https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43998074916/artifacts/14291260633

No se publicó una release nueva. No certifica Windows/Ghouls, animaciones completas
ni aprobación estética. El export Checks acotado no incluyó la captura del manual
Worldloom: no se afirma inspección visual de esa escena en esta versión.

## CI Java independiente para los modelos del manual (pendiente primera ejecución)

Se añade workflow `Pure Java manual models`: JDK 17, javac --release 17 y un
harness sin Gradle/Maven/Forge/JUnit. Compila las tres clases de PRODUCCIÓN
ManualScreenLayout/ManualScrollModel/ManualWheelScroll y ejecuta acumulación,
extremos, inversión, reset, 10000 entradas aleatorias, layouts pequeños/extremos,
selector y 260 alturas de barra. No usa una traducción del algoritmo a Python.

Genera evidencia con SHA de commit y hash de cada fuente solo tras completar
las comprobaciones. Los fallos borran cualquier informe verde anterior.
Tres tests Python comprueban ese contrato del ejecutor; no son las pruebas Java.
Workflow separado, sin permisos de escritura, sin exportar binarios del mod.
Su éxito NO equivaldrá a build Forge, renderizado, JUnit ni GameTests aprobados.

## Resultado CI del scroll preciso

Run 43925814163, SHA `6c21d3caf75c2b8a988b2e864cda531497bbe7d7`: failure
antes de compilar por dependencias Forge. Primer intento: timeout de DiffPatch y
JarJarMetadata. El wrapper hizo su único reintento autorizado; segundo intento:
DiffPatch ausente. Se conservó fallo final, sin JUnit/GameTests ejecutados.
Los cinco JUnit del scroll siguen pendientes. No hay captura ni binario nuevo
validado. Local: 109 Python, 25 recursos, parser 166 Java; secrets 318 sin firmas.
No confundir esas comprobaciones locales con compilación del mod o test visual.

## Manual: scroll preciso para trackpad (pendiente CI)

Se añadió `ManualWheelScroll`, un acumulador fraccional independiente para el
contenido y el selector de copias. Sustituye el salto fijo por cada evento mínimo:
un notch conserva el recorrido habitual, pero los deltas pequeños se acumulan.
La inversión de dirección descarta el residuo anterior; los extremos no acumulan
presión. NaN/infinito se rechazan y desplazamientos enormes se saturan sin overflow.
Resize, cierre, navegación por teclado y arrastre reinician el residuo correspondiente.

Cinco JUnit nuevos cubren fracciones, extremos, inversión/reset, entradas inválidas
y separación de regiones. Aún NO ejecutados: falta CI/Java funcional. Localmente
109 Python, 25 recursos y análisis sintáctico de 166 Java correctos; el parser no
sustituye compilación ni validación visual. Se conserva versión 5.11.6 sin publicar,
con Worldloom y este cambio pendientes. Último binario validado sigue 5.11.5.

## Última comprobación43923206173: Forge sigue sin servir dependencias

SHA `cf067062d49472ea36677e10e502dc6f00b97608`: failure de :classpath,
antes de Java/tests. Ahora faltan POMoficiales de unsafe0.2.0,srgutils0.6.2,
DiffPatch2.0.12yJarJarSelector0.3.19. Los404no son prueba de que esas versiones
sean inválidas: el upstream devolvió distintos artefactos ausentes entre runs.
No se aplican más cambios arbitrarios de resolución ni versiones para ocultarlo.
El wrapper transmitió la evidencia y mantuvo salida no-cero, sin reintento de
artefactos ausentes. Local109Python sin omitidos con Pillow y25recursos correctos.

Inicio manual por workflow_dispatchsigue403por permisos de la integración;
push sí funciona y los cambios están guardados. No hay build5.11.6aprobada.
Última build verde5.11.5. Retomar cadena CI completa cuando Forge se recupere;
no declarar las mejoras Worldloom verificadas por disponer solo de testsPython.

## Diagnóstico Gradle con memoria acotada y salida en vivo

workflow_dispatch rechazado403Resource not accessible by integration; no es
necesario pedir tokens al usuario. Nueva CI se dispara por push del ejecutor.
El wrapper deja de acumular stdout completo en memoria: transmite todo a CI
y guarda cola256000caracteres. Si hubo truncado, no reintenta con evidencia
incompleta. Estado final real, límite2y exclusión de fallos de pruebas intactos.
Tres tests nuevos ejecutan subprocesos Python reales y verifican log sin duplicar,
estado9preservado y truncado acotado que deniega reintento. CI nueva pendiente.
No se afirma compilación5.11.6ni recuperación del artefacto mergetool hasta verla.

## Resultado último run43919178874: bloqueo externo concreto, sin reintento indebido

SHA probado `f6575a8e0df327f300603fb39c3720c7707154d5`: failure antes de
compilar, ya con classpath de plugin resuelto. Falta el artefacto oficial
`net.minecraftforge:mergetool:1.1.7:api`, URL:
https://maven.minecraftforge.net/net/minecraftforge/mergetool/1.1.7/mergetool-1.1.7-api.jar
Log contiene DEPENDENCY_ATTEMPT1/2y Could not find; correctamente NO hubo
segundo intento. No hubo JUnit/GameTests ni binario5.11.6validado. No sustituir
mergetool por otra versión/artefacto arbitrario para eludir el fallo upstream.

Local106Python sin omitidos(Pillow instalado),25recursos; secrets316sin firmas.
Cambios y diagnóstico guardados en la rama. Última entrega validada continúa
5.11.5(ec7b0f0/CI43904324802). Próximo paso es comprobar recuperación de este
artefacto y ejecutar la cadena completa; no seguir acumulando buffs sin validación.

## Reintento acotado de resolución, no de pruebas

Run43917878613superó classpath de plugins y falló configurando _compileJava_1
por Read timed out en eventbus6.0.5.module oficial. No compiló ni ejecutó testsJava.
Se incorpora tools/gradle_dependency_retry.py: como máximo2intentos, espera60s,
solo fallo de configuración/resolución con timeout/429 y ANTES de cualquier
>Task. Compilación/tests, artefactos ausentes, fallosTLS/autorización y señales
no se reintentan. Siempre devuelve el estado final y conserva todos los logs.
Cinco pruebas de wrapper cubren éxito tras timeout, límite2, exclusiones,
clasificación429y señales. La suiteCI de juego/cliente sigue sin omisiones.
Nueva ejecución pendiente; no afirmar validación antes del resultado real.

## 5.11.6 — aislamiento del classpath de plugins (CI pendiente)

settings.gradle ahora separa ForgePluginArtifacts(solo net.minecraftforge y
subgrupos)de CentralPluginDependencies(resto, endpoint oficial repo1).
Esto corrige el caso previo donde Gradle encontraba el POM de gson/jsr305 en
el proxy Forge y quedaba vinculado a ese repositorio aunque allí faltara el JAR.
Se elimina el fallback de Plugin Portal: los únicos plugins son Java incorporado
y ForgeGradle directo. Sin cambios de versiones ni de pruebas de juego.
Nuevo test de fuente cubre ambos filtros. No garantiza disponibilidad del upstream;
JarJarSelector/srgutils aún requieren que Forge sirva sus artefactos oficiales.

## 5.11.6 — último resultado: plugin resuelto, JARs de dependencias ausentes

HEAD probado `3af29cd1f8a3e23ac1c175bcf253f454b6d8c072`, run43916095262:
failure durante configuración:classpath. ForgeGradle6.0.54ya se resuelve,
pero el repositorio oficial devolvió Resource missing para JARs requeridos:
net.minecraftforge:JarJarSelector:0.3.19,net.minecraftforge:srgutils:0.6.2,
y artefactos proxy como gson2.10.1/jsr305/checker-qual. No hubo compilaciónJava,
JUnit/GameTests ni nuevo binario. No cambiar versiones de esas dependencias
al azar ni añadir repositorios ajenos para ocultar el fallo del upstream.

Quedan guardados diagnóstico--info, resolución directa/pin del plugin y
selección oficial de Central. Local100Python sin omitidos con Pillow y25recursos
correctos. Estos tests son de fuente/recursos, NO prueba de disponibilidad Maven.
Último binario validado sigue5.11.5/CI43904324802. Worldloom5.11.6implementado
pero pendiente de que se recuperen las descargas y pase toda la cadena de CI.

## ForgeGradle fijado a la versión observada en evidencia

Run43915566706mostró404(Resource missing)para maven-metadata.xml de ForgeGradle
en ambos repositorios. Se fija6.0.54, cuya descarga/carga exacta consta en el
run43912465024(POM,module,JAR). No es una versión inventada ni actualización
aotra rama: evita metadata de rango dinámico y hace reproducible la resolución.
Los tests de dependencias nuevos comprueban versión fija, módulo oficial y
exclusión de coordenadas que no corresponden a Maven Central. CI pendiente.

## Diagnóstico HTTP confirmado y selección de repositorios

Run43912465024sí descargó y cargó ForgeGradle6.0.54 desde el repositorio
oficial: el paso useModule superó el fallo inicial. Luego falló al resolver
_compileJava_1por HTTP429Too Many Requests de Maven Central, incluso para
coordenadas Forge/Minecraft inexistentes allí; también registró timeout de Forge.
No se ejecutaron testsJava. No afirmar corrección completa de infraestructura.

Se prioriza Forge oficial para plugins y se usa el otro endpoint oficial de
Central(repo1.maven.org)para dependencias generales, excluyendo grupos
net.minecraft/net.minecraftforge/cpw.mods de consultas a Central. No se cambia
versión de Forge/Gradle ni se usan repositorios no oficiales ni TLS inseguro.
Nueva ejecución pendiente; los errores de red no se convierten en aprobaciones.

## 5.11.6 — corrección de resolución de plugin en validación

Se conserva ForgeGradle[6.0,6.2)y el repositorio oficial. settings.gradle
resuelve explícitamente `net.minecraftforge.gradle:ForgeGradle` por useModule,
sin depender del artefacto marcador separado que falló en ambos runs anteriores.
Coordenadas contrastadas con build.gradle oficial FG_6.0(groupnet.minecraftforge.gradle,
proyectoForgeGradle, idnet.minecraftforge.gradle). No se sustituye por mirrors ni
se deshabilita TLS. CIbuild añade--info para guardar causas HTTP/resolución
si el módulo también falla. Esto es una corrección pendiente, no CIverde.

## 5.11.6 — implementación guardada; validación bloqueada en dependencias

Segundo run43910619548(884282d68e126fb42aa9eccfbf72545d40bf8a5e)también
falló al resolver ForgeGradle, antes de compilar. Evidencia descargada y comprobada:
no hay JUnit/GameTests ni JAR5.11.6validado. No se cambió arbitrariamente versión
del plugin ni se omitió validación. Último binario verde sigue5.11.5/CI43904324802.
Los cambios Worldloom y nuevas pruebas están subidos;97Python sin omitidos
con Pillow y25recursos correctos. Secrets313sin firmas. JUnit de geometría y
GameTests nuevos siguen pendientes de ejecución por la resolución de dependencias.
Próximo paso: recuperar acceso al repositorio de ForgeGradle y volver a ejecutar
build/test/GameTests/cliente/servidor antes de entregar binario5.11.6.

## Diagnóstico inicial 5.11.6

Run43910083914falló ANTES de compilar: no se resolvió el plugin
net.minecraftforge.gradle:[6.0,6.2)desde los repositorios de plugins.
No hubo ejecución JUnit/GameTests ni JAR validado. Se intentó rerun fallido,
pero GitHub lo rechazó. Nuevo commit añade geometría Worldloom en seis ejes
y dispara otra ejecución sin saltar fases ni rebajar pruebas. Local97Python
sin omitidos con Pillow y25recursos correctos. Nueva CI pendiente.

## 5.11.6 — Worldloom ampliado (CI pendiente)

Primaria de5×5×16a7×7×48:2352celdas potenciales frente a400. Sin cambiar
presupuestos por turno, carga de chunks ni seguridad de secciones. Conserva
menas y roca colocada; máquinas/fluidos/claims siguen siendo barreras que
terminan la ruta antes de atravesarlas. Secundaria conserva puente pagado32.
Textos bilingües principales/secundarios corregidos: sin contorno verde retirado
ni antigua promesa de inmovilización de hostiles en la secundaria block-only.

Nuevo GameTest exige2350bloques extraídos de2352, preserva mena/placed y
límites laterales/de profundidad. Otro exige que una máquina en el borde nuevo
cancele antes de excavar la sección. JUnit de perfiles ahora exige identidad
única SIN la excepción anterior de Worldloom/core drill; área actual49.
Pendiente CI antes de afirmar compilación y cliente/servidor aprobados.

## 5.11.5 — Fault Choir validado

Commit probado `ec7b0f0e2de3ec343e46ebaedee7e500a5dde4e4`.
CI https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43904324802 : success.
138JUnit sin fallos/errores/omitidos;386GameTests aprobados;cliente gráfico y
servidor empaquetado aprobados. Local96Python con Pillow sin omitidos,25recursos.
Inspeccionado manual Choir español1280×720: descripción5×5/48correcta, sin recortes.

Primer run43903383765detectó perfil idéntico al taladro centrado en el test de
identidad geométrica. Corregido sin quitar la aserción: sección Choir alineada
con pies(-1..3respecto al origen de cabeza),5×5completa/1200celdas. Conserva
pruebas de barreras, exactitud de longitud y celdas exteriores intactas.

JAR `special-pickaxes-1.20.1-5.11.5.jar`,579138bytes;
SHA256 `8081ee1275b83da7e252479f32c24f0bb2eb818824fdad28b5413dcba996a388`.
Artefacto autenticado temporal:
https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43904324802/artifacts/14246481087
Sin release público ni afirmación de compatibilidad Windows/Ghouls/animaciones completas.

## 5.11.5 — galería Fault Choir (CI pendiente)

Primaria ampliada de2×2×20(80celdas) a5×5×48(1200celdas),15veces el
volumen potencial. Sigue comprobación previa por sección, revalidación antes
minado, scheduler compartido y pausa por drops. Secundaria mantiene galería
abovedada7×5×24. Sin aumentar partículas ni reactivar contornos retirados.
Textos bilingües sincronizados; tooltip secundario ya no promete combate obsoleto.

JUnit nuevo verifica1200celdas únicas en seis direcciones y límites de sección.
GameTest de recorrido comprueba último bloque47, preserva48y borde lateral3,
modelando recogida entre ticks manuales sin relajar límite de drops. Se conservan
pruebas de funciones antiguas de feedback, que no reactivan emisiones.
Pendiente CI antes de afirmar compilación/juego/cliente aprobado.

## 5.11.4 — buff Seam validado

Commit probado `bba971017124e3d4be5bebcfade52ba4fab4f4b0`.
CI https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43891412754 : success.
137JUnit sin fallos/errores/omitidos;386GameTests aprobados, incluido el nuevo
fixture de1331bloques con exactamente1024retirados y protección de mena/placed.
Cliente gráfico y servidor empaquetado aprobados. Local96Python sin omitidos,
25recursos correctos; secrets311sin firmas. Inspeccionada captura real Seam
español1280×720: alcance1024/15×15correcto y controles visibles sin recortes.

JAR `special-pickaxes-1.20.1-5.11.4.jar`,579210bytes;
SHA256 `437fb6e66ebf1ebd08d8ea0460edaad61bf44763a8db64a1913c4d58d2b9d2bf`.
Artefacto autenticado temporal:
https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43891412754/artifacts/14238671596
Sin release público ni afirmación de compatibilidad Windows/Ghouls/animaciones completas.

## 5.11.4 — Seam Ripper ampliado (CI pendiente)

Primaria: tope1024bloques (antes256), búsqueda hasta2048posiciones (antes512).
La búsqueda termina al reunir1024candidatos: evita inspeccionar roca que ya
no se extraería. Sin cambiar presupuesto por turno, pausa por drops, chunks,
material exacto ni revalidación de claims/bloques colocados antes de excavar.
Textos bilingües Seam sincronizados con1024y secundaria15×15; se elimina una
promesa de rastro luminoso continuo que el rediseño event-only ya no emite.

Nuevo GameTest construye1331bloques aislados, modela recogida entre turnos y
exige exactamente1024retirados preservando mena y roca marcada por jugador.
Python impide reintroducir límites256/512o cara7×7en los textos Seam.
Pendiente compilación/cliente/servidor CI; no se declara verde anticipadamente.

## 5.11.3 — conexión recuperada y CI verde

Se recuperó la conexión; se restauraron los cambios pendientes sobre el HEAD
remoto sin sobrescribir trabajo. Commit probado `651b7538ff39cee09f738a776d2097130980c5d6`.
CI https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43884854097 : success.
137JUnit sin fallos/errores/omitidos;385GameTests aprobados;cliente gráfico,
32manuales renderizados y servidor empaquetado aprobados.

Inspeccionadas capturas reales Crucible-stone e Interregnum-aura a1280×720/español:
texto visible, instrucciones contextuales correctas y panel compacto sin clipping.
No certifica todas las escalas/idiomas/animaciones ni Windows/Ghouls.

JAR `special-pickaxes-1.20.1-5.11.3.jar`,579220bytes;
SHA256 `315ca708224193c88dfc8b93f81beb6ce55c2eebdd88712a4d50183f1c76d238`.
Artefacto autenticado temporal:
https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43884854097/artifacts/14236145832
Sin release público nuevo. Las notas de bloqueo siguientes son históricas/resueltas.

## Estado local 5.11.3: subida bloqueada por conexión

Commit de implementación `f5e49eb`. Los intentos de push devolvieron
`gnutls_handshake() failed: The TLS connection was non-properly terminated`;
`gh api rate_limit` devolvió EOF. No fue un error de credenciales y no se pidieron.
Cambios guardados localmente, aún sin CI nueva ni binario5.11.3validado.
Local:95Python sin omitidos con Pillow,25recursos,162Java parseados sin errores;
el parser no sustituye javac/JUnit/GameTests. Secrets311entradas sin firmas.
Último binario validado sigue siendo5.11.2/441d256/CI43796976117.

## 5.11.3 — controles claros y manual compacto (CI pendiente)

El manual diferencia activación/pausa, campos que se desactivan con la misma
tecla, regiones que requieren esquinas y alternativa que cancela trabajo/selección.
Regiones muestran el gesto real de clic izquierdo para ambas esquinas; teclas
siguen resolviéndose en vivo y el contexto sigue la habilidad copiada.
Panel máximo reducido340→260píxeles de GUI para evitar grandes zonas vacías.
Scroll, clipping, teclado y selector se conservan.

Tres JUnit nuevos cubren128combinaciones de geometría GUI, panel compacto y
los260altos de scrollbar. Python cubre traducciones de los cinco controles.
Pendiente CI/inspección gráfica; no se asume resultado antes de ejecutarla.

## 5.11.2 — validación confirmada

Commit probado `441d2569bd03e8d2ae81c39d45ddbf824d958676`.
CI https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43796976117 : success.
134JUnit sin fallos/errores/omitidos;385GameTests aprobados;cliente gráfico,
32manuales renderizados y servidor empaquetado aprobados. Local94Python sin
omitidos y25recursos correctos. Inspeccionadas capturas reales de Icarus-wide y
Eventide-gravity_out en1280×720: descripción específica correcta y texto visible.
No es una evaluación visual completa de VFX, otros tamaños/idiomas o modpacks.

Primer intento43796716602falló en JUnit por inicializar ArtifactKind/SoundEvents
sin bootstrap de Minecraft. Se desacopló la geometría pura mediante Shapeenum;
no se omitieron pruebas ni se relajaron sus aserciones. Nuevo intento todo verde.

JAR `special-pickaxes-1.20.1-5.11.2.jar`,577902bytes;
SHA256 `fd8db251184a4a282a31e798fcecbaf10a9834fe91229844b37bfa7383e04ab7`.
Artefacto autenticado temporal:
https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43796976117/artifacts/14195168983
Sin release público. No afirma redistribución autorizada de assets ni prueba Windows/Ghouls.

## 5.11.2 — identidad visual de activación y manual por modo (pendiente CI)

Las ráfagas custom dejan de usar un anillo idéntico: alas ascendentes Icarus,
espiral Iridium, retícula Seam, onda Choir, caída Exodium, hélice Hellspec/Crucible,
raíles Worldloom, corte circular Axiom y pulso Palimpsest. Mismos16puntos por
activación,256globales/tick y deduplicación por jugador; sin suscriptor ambiental
ni texturas generadas. Marco horizontal estable incluso mirando arriba/abajo.
Pruebas JUnit nuevas: puntos únicos/finitos/acotados, orientación y rechazo de índices.

Descripciones específicas bilingües para TODOS los modos de picos multimodo,
incluyendo los12materiales de Crucible y6excavaciones Worldbreaker. El manual
conserva sus tres bloques, controles reasignables y seguimiento del modo copiado.
Python comprueba cobertura/distinción/longitud de cada descripción. No se afirma
aprobación estética ni compatibilidad con Ghouls/Windows; nueva CI pendiente.

## 5.11.1 — CI verde y manual capturado

Commit probado `ed4410bbec763561d11370f3f230ca7e40c107c8`, ejecución
https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43794533685 : success.
131JUnit sin fallos/errores/omitidos;385GameTests aprobados;cliente gráfico y
servidor empaquetado aprobados.32aperturas reales de manual confirmadas por
ScreenEvent.Render.Post y capturas requeridas antes de activar cada escena.
Inspección visual local de la captura Icarus-forward: tres bloques concisos,
texto11×7correcto, controles visibles y sin desbordamiento en1280×720.
Esto no certifica visualmente los demás tamaños/idiomas/copia ni todas las animaciones.

JAR `special-pickaxes-1.20.1-5.11.1.jar`:573312bytes,
SHA256 `1d985e616b468ca552943a78c501fbf8ad53ba5ae45e0b1e7123937454927a70`.
Descarga autenticada temporal:
https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43794533685/artifacts/14194616616
Sin release público nuevo. Capturas completas en forge-validation del mismo run.
Local actualizado:93Python sin omitidos tras instalar Pillow;25recursos correctos.

## 5.11.1 — segunda fase en validación

- Icarus: cortes específicos ampliados de7×5/9×7 a11×7/15×9. Conserva
  comprobación previa por sección, movimiento seguro y presupuesto compartido.
  La secundaria conserva el ascenso hasta superficie y sus barreras de fluidos.
- Iridium: extracción elipsoidal de radio24/altura±12 (antes14/±7).
  Plan único inmutable precalculado; el mundo solo se consulta gradualmente
  bajo ORE_SCAN_BUDGET y presupuesto global, sin forzar chunks. Añadido rechazo
  explícito de alturas fuera del mundo. Recall sigue48/96 y ownership intactos.
- Manual: descripción primaria sensible al modo real, incluido modo copiado de
  Worldbreaker; Icarus muestra la geometría del corte elegido. Iridium explica
  alcance y recall sin relleno. Continúan tres bloques: primaria/secundaria/teclas.
- Nuevos JUnit: unicidad/orden/límites/inmutabilidad del plan Iridium y geometría
  Icarus en las seis direcciones. Pendientes de compilación/ejecución en CI.
- Local:93Python (1omitido sin Pillow),25recursos; no Java local.
- No se afirma validación visual del manual ni compatibilidad Windows/Ghouls.

## 5.11.0 — validación verde confirmada

- Commit probado: `9ca5fa1a16244697aab6baccbeb37f68178acffd`.
- CI: https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43735290477 — success.
- 129JUnit, sin fallos/errores/omitidos;385GameTests requeridos aprobados.
- Cliente real, controles reasignados, tooltips y servidor empaquetado: pasos CI aprobados.
- Galería de13páginas/26imágenes generada a partir de capturas y recursos verificados.
- JAR: `special-pickaxes-1.20.1-5.11.0.jar`,572854bytes.
- SHA256: `c4eb4acf51f8ebc7e8ad3292f4a003238968b68ff429dcb2f360096b56a98643`.
- Descarga autenticada temporal (7días): https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43735290477/artifacts/14170384567
- Sin release público nuevo. Autenticación no equivale a permiso de redistribución de assets.
- No valida Windows/Ghouls, todas las animaciones, ni aprobación estética. Manual
  simplificado; su revisión visual completa y mejoras específicas de Icarus/Iridium
  siguen pendientes. Buffs compartidos sí integrados. No afirmar rediseño total terminado.

## Cliente real detectó una regresión de traducción

CI43733821509 llegó al cliente gráfico tras las pruebas de servidor, pero su
comprobación de teclas reasignadas detectó que la nueva descripción ampliada
Eventide había perdido%1$s/%3$s. Restaurados ambos controles dinámicos, sin
volver a la prosa larga del manual. Nuevo Python cubre los dos idiomas.
El fallo se repitió por frame y el ZIP comprimido superó128MiBdescomprimido:
el extractor mantuvo su límite y se usó el Check de diagnóstico acotado,
sin elevar presupuestos ni declarar aprobado el run fallido. Nueva CI pendiente.

## Diagnóstico de la segunda regresión Axiom

CI43733112154 mostró status=paused y885pasos pendientes: el scheduler se
había detenido correctamente por acumulación de drops en el test de ticks
manuales sin tick de entidades (sin merge/recogida). La prueba ahora modela
recogida entre turnos y reanuda por el control normal, sin subir ni quitar el
límite de256drops. El radio ampliado de Eventide también requería aislar el
fixture negativo de botín a100bloques de altura y limpiar todo su radio50,
no solo20. Conserva rechazo/cooldown/durabilidad y contexto mutado intactos.
Los runs fallidos no se consideran aprobación de5.11.0.

## Primera compilación5.11.0:129JUnit correctos; dos expectativas históricas actualizadas

CI43731818984 compiló y pasó129JUnit. GameTests detectó dos contratos antiguos:
Seam aún exigía preservar la fila4del barrido anterior7×7y Axiom esperaba
extraer una celda a9cuando el radio por defecto ahora12deja esa celda dentro
del núcleo preservado. Se actualizan fixtures a borde7/fuera8para15×15y
radio_configurado−1para la envolvente de Axiom. Se conservan las aserciones de
ore/build protection, cara posterior intacta y núcleo preservado. No se
eliminan pruebas ni se acepta ese run fallido como validación del rediseño.

# 5.11.0 — reestructuración visual y habilidades OP; pendiente de CI

## Cambio visual solicitado
Los13modelos apuntan a sprites originales de Mythic Metals, sin editar píxeles,
recolorear, escalar ni generar arte. Fuente fija1cc825572b1e3a8697af197fa58da34636521ce4;
manifest registra ruta/dimensiones/SHA256de cada fuente. Créditos y términos
originales incluidos. Son assets reservados para la compilación personal
solicitada: publicación automática de binarios desactivada, también enChecks.
No se afirma permiso general de redistribución ni aprobación estética del usuario.

## VFX y UI
Se retira el suscriptor de auras de mano/Curios. Todos los emisores antiguos
pasan por la API retirada que no emite. No quedan envíos vanilla en el paquete
artifact: solo AbilityVfx emite una ráfaga breve de sprites custom, tras éxito
de activación/confirmación;16partículas por jugador/tick,256globales. Los campos
persistentes no mandan bordes/partículas y los efectos no muestran partículas
de poción. Se conservan las geometrías antiguas para regresiones y futura limpieza.
Manual: solo habilidad principal, secundaria y controles reasignados; sin perfil,
melee, costes, límites, curios ni párrafos de relleno. Panel más compacto,
colores neutros, scroll/clipping y selector de copia conservados.

## Potencia sin tick spikes
Presupuesto base8→32por jugador,96→192global; escaneo de menas32→96;
vein limit256→1024; campo600→1200ticks; enfriamiento principal20→10ticks,
secundarias mínimo100→40ticks. Las configs existentes no se sobrescriben.
Stasis cambia un bloque por Fractura Temporal11×11×11streaming con drops,
protección de construcciones, bedrock y claims. Axiom expone ese volumen sin
romper menas; Crucible transmuta esfera radio12; Choir galería7×5×24;
Hellspec chimenea7×7×32; Worldloom puente32; Eventide botín256/radio48;
Worldbreaker obtiene secundaria nativa11cúbicos; Seam barrido15×15;
Exodium pozo hasta64. Icarus e Iridium conservan sus mecánicas y reciben
las mejoras compartidas de velocidad/enfriamiento; no se afirma un rediseño
completo de todas sus primarias. Se mantienen daño4y secundarias mineras.

## Pruebas
TresJUnit nuevos:1331celdas únicas, centro primero/copia inmutable e índices
inválidos. TresGameTests nuevos: excavación volumétrica real, claims en ejecución
y registros custom/deduplicación VFX. Esperados129JUnit/385GameTests.
Local25recursos/87Python/parser158Java verdes; falta javac/Forge/cliente real.
La entrega467 inferior es histórica, no certifica esta reestructuración.

## Historial

# Estado vigente — 5.10.467 verificada

## Entrega verificada 5.10.467

- Runtime: `fe56becab9ce5d368116d031d3f7bca6b9a2dcf7`.
- [CI43683204081](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43683204081): completed/success.
- **126JUnit**, cero fallos/errores/omisiones; **382GameTests** completos.
- Cliente real:32escenas/64tooltips; `ALTERNATE_BINDINGS_REBOUND_OK`.
- Servidor empaquetado: `PACKAGED_SERVER_SMOKE_OK`; logs de runtime limpios.
- [Prerelease de prueba](https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.467-43683204081).
- JAR: **567058bytes**. SHA256:
  `957c7481a45fac57cb5a1ed543453edbed57c4dbc250c90123e9b0e831c6d626`.
  Tamaño/digest del asset GitHub coinciden con RELEASE.json y SHA256SUMS de CI.
- Galería `artifacts/picos-5.10.467/`:13páginas PDF,26imágenes reales enZIP;
  CRC, traducciones es_es y assets comprobados contra el commit del runtime.
- Local:87Python conPillow,25recursos,parser155Java,secretos/diffcheck correctos.
- Sin compilación local Java ni instalaciónWindows; no certifica Curios instalado,
  Ghouls completo ni animaciones completas. Las esperasCI no son trabajo efectivo.

### Cambios acumulados desde460

- Manual: reconstruye tarjetas tras recargar el idioma, reinicializar y cerrar;
  misma cadena de locale no equivale a mismos recursos.
- Cosméticos: presupuesto real por audiencia y revalidación de visibilidad/rango
  aun al reutilizar una audiencia cacheada en el mismo tick.
- Curios: confirmación fresca sólo en la ranura original, sin rescaneo completo
  por cada callback; identidad exacta de la fuente preservada.
- Stasis: detiene beneficios pendientes al detener/reemplazar el campo o mover
  al portador/receptor durante permisos o efectos; no revierte beneficios previos.
- Crucible: selección acotada sin callbacks de permisos en el iterador; revalida
  proyectil, propietario, posición exacta y contexto antes del desvío.
- Eventide: sólo carga la herramienta capturada; un cambio de contexto cancela
  pulls pendientes y la confirmación cosmética del colapso invalidado.
- Worldloom: exige permiso también en el ancla antes de devolver un objetivo.
- CI: logs vacíos no cuentan como evidencia, GameTests requieren éxito explícito,
  retirada histórica idempotente y espera por run/SHA con salida inequívoca.
- Se mantienen13picos jugables, daño4, cero recetas y secundarias sólo mineras.

## Historial técnico (los pendientes siguientes son históricos)

## Esperas CI con resultado inequívoco y menor uso de API

wait_ci.py consulta sólo los metadatos de un run concreto a intervalos
limitados, sin descargar repetidamente todos sus jobs/Checks. Comprueba ID y
SHA completos; sólo conclusion=success termina con0. Fallos/cancelaciones
terminan con1, errores de consulta/identidad con2 y vencimiento con124.
La consulta fallida por cuota/autenticación no se reintenta en bucle. Diez
Python verifican transiciones, identidad, códigos, timeout y entradas inválidas.
Total local87Python conPillow. La espera sigue sin contarse como trabajo activo.

# 5.10.467 — contexto de colapso Eventide; pendiente de CI

Eventide no acepta cargas provenientes de una copia separada de la herramienta
del campo. Su colapso conserva la posición del portador al seleccionar drops:
si un listener lo mueve o reemplaza/detiene el campo, termina el lote y no
emite una confirmación de colapso desde el contexto invalidado. También exige
posición exacta del drop después del permiso, no sólo coincidencia de bloque.
Tres GameTests312–314 verifican desplazamiento sub-bloque del drop, movimiento
del portador y copias de herramienta sin carga; comprueban que24roturas con
la herramienta real siguen alcanzando el umbral normal.
Incluye465/466. Esperados126JUnit/382GameTests, pendientes de CI.
Local77Python conPillow/25recursos/parser155Java/secretos/diffcheck correctos.

## 464: verificación completa recuperada

Run43681728499 completed/success, commit7471264a42d254c3d6034706bcb24f3eeb80988f.
EvidenciaChecks descargada y SHA de cada archivo comprobado:126JUnit sin
fallos/errores/omisiones;372GameTests. Logs de GameTests,cliente y servidor
empaquetado limpios. Cliente32escenas/64tooltips. Se generó galería464 con
13páginas y26imágenes comprobando assets/nombres españoles contra el commit.
Inspección de la captura Crucible confirma el pico en mano, no toda su animación.
JAR566321bytes, SHA256d8df8ab1220d1d4fc5d939dbf235dbee6227783e4dbe0933fcf54fb35adb33ab.
Esta evidencia no valida465–467. La API GitHub alcanzó su cuota de lectura;
no se confunde con fallo de credenciales ni se omiten validaciones por ello.

## Endurecimiento de evidencias CI (sin omitir fallos)

El verificador de GameTests exige ahora tanto el conteo completo como la línea
All N required tests passed con el mismoN. También rechaza fallos individuales
o líneasERROR aunque aparezca una confirmación posterior contradictoria.
El escáner de runtime rechaza logs vacíos/blancos como evidencia insuficiente.
Cuatro pruebas Python adicionales cubren estos falsos positivos;77Python
conPillow y25recursos correctos localmente. No prueba por sí mismo Minecraft.

# 5.10.466 — desvío Crucible con permiso en ejecución; pendiente de CI

La selección de proyectiles del campo Crucible deja de ejecutar listeners de
permisos dentro de la iteración de entidades. Primero reúne hasta32candidatos
con presupuesto de inspección256; después comprueba permisos por candidato y
revalida identidad/contexto del campo, posición exacta del proyectil, vida,
dimensión, propietario hostil y trayectoria antes de cambiar la velocidad.
Una traslación sub-bloque o cambio a propietario aliado cancela el desvío;
si se mueve el portador, termina el lote. No añade daño ni altera entidades
aliadas. Tres GameTests309–311 cubren esos cambios durante el callback.
Incluye465. Esperados126JUnit/379GameTests; pendientes de CI.
Local parser155Java,25recursos,73Python conPillow y diffcheck correctos.

# 5.10.465 — entregas de campos resistentes a callbacks; pendiente de CI

Revisión de Stasis colocado: sus entregas de efectos podían continuar después
que un listener detuviera/reemplazara el campo o moviera al portador/receptor.
Ahora se revalida identidad exacta del campo y herramienta, vida, dimensión,
posición capturada, vencimiento y pertenencia al campo antes/después de cada
efecto. Para compañeros se exige alianza recíproca y no espectador. Si cambia
el contexto se detienen los siguientes efectos/alimento, sin revertir efectos
ya autorizados ni eliminar un campo sustituto. Eventide también revalida tras
su efecto de prisa antes de continuar. La posición de entrada al tick se
conserva a través del callback de permisos inicial.

Cuatro GameTests nuevos305–308: detener durante Dominion, mover receptor
sin salir del campo, sustituir por otro campo y mover portador en permisos.
Esperados126JUnit/376GameTests. Local73Python conPillow,25recursos,
parser155Java,escáner secretos y diffcheck correctos; no compilación local.
Incluye464, cuya ejecución43681728499 seguía pendiente al preparar este cambio.

## CI464: correcciones tras dos fallos, nueva ejecución pendiente

43681212818 compiló pero falló en dosGameTests. La audiencia revalidada había
perdido la identidad de lista estable que comprueba el test77: se conserva la
lista original sólo si todos sus miembros siguen siendo elegibles; si no,
se devuelve la filtrada sin repetir el escaneo. La prueba nueva304 se ejecuta
ahora síncronamente con reloj restaurado en finally, sin81ticks de otros
subsistemas; separa las aserciones para identificar un eventual fallo.
No se declara verde ni se publica galería464 con este resultado.

## Corrección de compilación del manual

CI43681067221 confirmó retirada histórica sin escrituras (40Checks,0updates),
pero detectó dos referencias a campos sustituidos en removed() del manual.
Corregidas a layoutRevision.invalidate(), igual que init(). El parser local
sólo comprueba sintaxis, no resolución de símbolos; no sustituye javac/Forge.
La corrección y464 siguen pendientes de compilación y pruebas completas.

# 5.10.464 — permiso en el ancla de raíces; pendiente de CI

WorldloomSnare ya comprobaba permisos en la posición actual, pero no en el
ancla capturada cuando el objetivo había sido desplazado a otro bloque.
Ahora verifica ambos destinos antes de devolverlo, incluyendo identidad del
binding, posición y elegibilidad después del callback. Una denegación libera
el vínculo sin teletransportar ni bloquear el movimiento. Un GameTest con
windup real, objetivo desplazado y ancla denegada verifica también que pueda
iniciarse un vínculo nuevo después. No cambia las secundarias mineras.
Incluye463. Esperados126JUnit/372GameTests; pendientes de CI.

## Seguridad CI: retirada histórica idempotente, sin fail-open

El run43680877100 falló antes de compilar en withdraw_unvalidated.py; no había
log exportado de ese paso. No se atribuye una causa concreta sin ese stderr.
La rutina anterior reescribía todos los Checks históricos ya retirados en cada
run. Ahora sólo modifica los que aún no tienen el marcador completo, pagina
más allá de100resultados y exporta verification-withdraw.log también al fallar.
Reintenta de forma limitada sólo errores transitorios; autenticación o fallo
persistente siguen bloqueando. Es compatible con gh antiguo sin --slurp.
Siete Python verifican idempotencia, alcance, paginación y bloqueo de errores.
Local73Python conPillow/25recursos/parser155Java/diffcheck correctos.

## Regresión intermitente detectada en CI histórica460

El run43679707143 (commit sólo de documentación2248a09) falló en
primaryRecoversItsImpossiblePersistedDeadline. Ese test activaba Stasis colocado
sin crear un objetivo: dependía de que el raycast encontrase otra estructura
vecina. La prueba de plazo usa ahora el modo aura, cuyo objetivo es el propio
portador. Se añade otra prueba del modo colocado con bloque propio, mirada y
comprobación explícita del raycast antes de activar; conserva las aserciones
reales de campo activo y deadline configurado. No se ignoran fallos ni se
relaja el escáner. Esperados126JUnit/371GameTests en463, pendientes de CI.

# 5.10.463 — manual tras recargas y contraste Crucible; pendiente de CI

ManualLayoutRevision liga las tarjetas envueltas al ancho, controles/contenido
y a la identidad de Language, no sólo al nombre es_es/en_us. Una recarga de
recursos con el mismo idioma invalida el texto cacheado. init también invalida
las tarjetas y termina un arrastre de scrollbar pendiente, manteniendo el
scroll sujeto al nuevo tamaño de contenido. Cuatro JUnit puros comprueban
caché estable, identidad tras recarga, controles/ancho e invalidación explícita.
No se afirma haber probado un resourcepack externo real.

Paradox Crucible recibe la décima adaptación de contraste reproducible,
gamma.86, con original, alpha y silueta conservados. Se mantiene el modelo
handheld y el nombre canónico del pico. No es un redibujo original.
Incluye461/462. Esperados126JUnit/370GameTests; sin build local.
Local25recursos/66Python conPillow/parser155Java/secrets/diffcheck correctos.
Sin daño/ataques/recetas nuevos; las secundarias siguen siendo mineras.

# 5.10.462 — consultas de ranura, privacidad y partículas; pendiente de CI

Las observaciones del puente Curios conservan la ranura descubierta y la leen
fresca directamente al confirmar, en vez de recorrer hasta64entradas en cada
revalidación. El evento pasa Equipped a la captura para no repetir la búsqueda
inicial. La fuente no migra a otra ranura ni acepta una copia equivalente.
CurioSlotScan.contains impide índices negativos, fuera de inventario o más allá
del límite. Dos JUnit nuevos cubren fronteras. Se contrastaron las firmas de
CuriosApi/ICuriosItemHandler/ICurioStacksHandler/IDynamicStackHandler con la rama
oficial1.20.x commit a91da6ba2396f6ed9042c8a47a471ca5efc97da8; esto no sustituye
probar Curios instalado.

Los flashes mineros Strata/Stasis ahora reservan el coste real para toda su
audiencia (máximo4personas), con conteos primario/acento limitados a3cada uno.
PicoStrata, órbita/borde/desvíoCrucible, progresoWorldloom, carga/colapsoEventide
y wireframes de dominio también descuentan partículas. La lógica y mensajes
siguen funcionando al omitir cosméticos. Tres JUnit verifican el coste clamped.

Se corrige la dirección de visibilidad: se pregunta si el portador es invisible
al observador, no al revés. La audiencia cacheada filtra de nuevo distancia,
dimensión, vida/espectador y visibilidad antes de reutilizarse dentro del tick.
Seis GameTests cubren privacidad, salida del radio, reserva exacta de20/58motes
y Stasis bajo saturación cosmética. Incluye461. Esperados122JUnit/370GameTests.
Local25recursos/66Python conPillow/parser153Java/secrets/diffcheck correctos;
sin build local. Sin ataques/daño/recetas nuevos ni cambios de secundarias.

# 5.10.461 — entregas de auras coherentes; pendiente de CI

AuraShareContext congela el nivel y posición del portador y revalida cada
aliado antes/después de callbacks de efectos. Once auras compartidas ya no
continúan una selección vieja si el portador se mueve, un aliado sale del
rango, cambia su relación o deja de ser elegible. Stasis comprueba de nuevo
entre absorción y ward; Heartwood entre renovación y alimento. Icarus exige
que ambos sigan en el aire. La parte ya autorizada no se revierte arbitrariamente.
El tick completo tampoco sigue al portador movido al conceder Night Vision o
su firma propia. Los enlaces de éxito exigen que el efecto se haya concedido.

Seis GameTests nuevos cubren movimiento del destinatario/portador, concesión
doble, salida de rango y veto real de MobEffectEvent.Applicable sin enlace
engañoso ni gasto cosmético. Esperados117JUnit/364GameTests; sin build local.
Local25recursos/66Python conPillow/parser151Java/secrets/diffcheck correctos.
No añade ataques, daño, recetas ni cambios en las secundarias mineras.

## Entrega verificada 5.10.460

- Fuente runtime: `c39a7b9df7bceee184ff3f264571e7108421a508`.
- CI43678491078 completed/success:117JUnit sin fallos/errores/omisiones,
 358GameTests; servidor empaquetado y logs validados.
- Cliente real:32escenas/64tooltips; ALTERNATE_BINDINGS_REBOUND_OK presente.
- Prerelease: https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.460-43678491078
- JAR559543bytes,SHA256`381b5279d3e914c1ec3a74ed995a6882cfa6a248957209dc7f759bb7db36db10`.
- Asset749898528 uploaded; tamaño/digest API coinciden con RELEASE.json CI.
- Evidencia:/tmp/sp-ci460,recuperable por fetch_ci_evidence con fuente/run.
- Galería:artifacts/picos-5.10.460/ PDF13páginas/ZIP26imágenes;CRC/procedencia
 correctos. Nombres es_es y SHA256 de traducciones ligados al commit del JAR.
- Local25recursos/66Python conPillow/parser150Java/secrets/diffcheck correctos.
- No buildlocal ni accesoWindows ni certificaciónCurios/Ghouls real/animaciones completas.

## Galería 460 — etiquetas verificadas en español

El PDF y HTML muestran los nombres españoles del propio mod, no títulos
inventados a partir del ID. Se exige que es_es.json coincida byte a byte con
el commit del JAR capturado; su SHA256/locale quedan en PROVENANCE.json. Tres
pruebas rechazan traducciones de otra versión, nombres ausentes y valores que
no son texto. Los nombres HTML se escapan; los títulos PDF ajustan tamaño y
eliden sólo si no caben, conservando el nombre completo en la procedencia.
Los ZIP mantienen IDs canónicos en los nombres de archivo y las26imágenes
exactas. Local66Python conPillow/25recursos/parser150Java/diffcheck correctos.

# 5.10.460 — revalidación tras efectos e Interregnum legible; CI correcta

MiningObservations vuelve a validar herramienta, fuente Curios y actor después
de Lode Surge: MobEffectEvent también puede cambiar el equipo o mover al minero.
No continúa con propiedad de drops ni comparte a compañeros desde el nuevo
contexto. Lode Surge conserva la concesión ya autorizada, pero omite pulsos y
transferencias pendientes si se invalida la fuente durante el callback; no
revierte efectos ajenos. También revalida al portador y a cada aliado entre las
concesiones compartidas. Dos GameTests usan MobEffectEvent.Added real para
quitar la fuente o mover al portador durante la concesión.

Interregnum añade la novena adaptación de contraste reproducible, gamma.86,
con referencia/alpha/silueta preservados. Los créditos distinguen ahora el
redibujo histórico Worldbreaker de su adaptación runtime actual, eliminando
la afirmación contradictoria. No cambia daño, recetas ni secundarias.

El puente opcional ignora respuestas incompatibles/null del proveedor en vez
de hacer casts ciegos, y limita el escaneo de la ranura pickaxe a64entradas
(el slot predeterminado sigue siendo1). Tres JUnit cubren límites normales,
negativos y extremos. Incluye459. Esperados117JUnit/358GameTests. Local25recursos/
63Python conPillow/parser150Java/secrets/diffcheck correctos; no build local.
Verificado por CI43678491078:117JUnit/358GameTests.

# 5.10.459 — equipo diferido y firmas adicionales seguras; pendiente de CI

Iridium Curios conserva un EquipmentLease de identidad: se consulta la ranura
actual al admitir y confirmar la observación; una copia equivalente no sustituye
la fuente original. Cambios/quitar/vaciar el pico invalidan ritmo y marcado de
drops pendientes. Los adaptadores de inventario pueden proporcionar un lookup
vivo; el puente integrado usa CuriosCompat.currentPick/findFresh, sin reutilizar
la caché del tick en estas fronteras. Las consultas visuales conservan caché.
Los eventos de minería y daño del mod obtienen equipo fresco. No se añaden
recetas, daño ni secundarias de combate.

Se rechazan observaciones con actor inválido/herramienta vacía o distinta antes
de consultar drops. ArtifactItem reutiliza PassiveReactionContext antes de
las firmas adicionales de un golpe normal: si claims mueven alguno de los
actores, conserva el bookkeeping nativo pero no añade el efecto obsoleto.

Cuatro JUnit de identidad de equipo y siete GameTests nuevos. Los tests de
inventario usan proveedores de ranura simulados explícitos, no una instalación
real de Curios ni una certificación de esa integración. Pruebas anteriores de
LodeSurge ahora declaran ese adaptador en vez de fingir equipo real.
Esperados114JUnit/356GameTests. Local25recursos/63Python conPillow/parser148Java,
secrets/diffcheck correctos; sin build local. CI pendiente.

## Entrega verificada 5.10.458

- Fuente runtime: `f5082b28de716b0f0089946a34b1f2e42a8b909a`.
- CI43676520675 completed/success:110JUnit sin fallos/errores/omisiones,
 349GameTests, servidor empaquetado correcto y logs validados.
- Cliente real:32escenas/64tooltips; ALTERNATE_BINDINGS_REBOUND_OK presente.
- Prerelease: https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.458-43676520675
- JAR555724bytes,SHA256`3d763a1c7218094f92367fe21d48f404277d08c59f3a13317ddbb6fac8d3a83b`.
- Asset749837917 uploaded: tamaño/digest API coinciden con RELEASE.json CI.
- Evidencia:/tmp/sp-ci458; galería artifacts/picos-5.10.458/ PDF13páginas,
  ZIP26imágenes, procedencia exacta por commit/manifest y CRC correcto.
- Local25recursos/63Python conPillow/parser146Java/secrets/diffcheck correctos.
- No build local, no instalaciónWindows ni certificación de Ghouls/animaciones completas.

# 5.10.458 — regresiones de defensa propia y contraste Worldbreaker; CI correcta

Distingue las firmas Curios de defensa propia de las respuestas que consultan
claims: Palimpsest, Worldbreaker e Iridium conservan su defensa en aventura sin
permiso de construcción. Un GameTest recorre los tres. Otro cubre Null Veil
compartido: no gasta recarga si un claim mueve al atacante; tras estabilizarse,
recupera un plazo corrupto pero mantiene los ocho segundos reales.

Worldbreaker_v2 se reproduce ahora desde la referencia conservada mediante
item_readability_asset.py con gamma.86. Es la octava adaptación reproducible;
no un redibujo. Tests verifican alpha/silueta, brillo no decreciente, modelo y
bytes reproducidos. Conserva su nombre de textura runtime para compatibilidad.

Incluye457. Verificados110JUnit/349GameTests; sin build local. Local25recursos,
63Python conPillow/parser146Java/secrets/diffcheck correctos. CI43676520675 verde.

# 5.10.457 — pasivas seguras y presupuesto de enlaces; incluidas y verificadas en458

PassiveReactionContext fija jugador, atacante, nivel y posiciones antes de
consultar claims. Tras callbacks no aplica represalias a actores movidos,
muertos, aliados o desautorizados; Null Veil no consume su recarga si el
contexto dejó de ser válido. Heartwood conserva su defensa propia, pero no
aplica raíces a un atacante movido por otro mod. No se añaden ataques ni se
modifican las secundarias mineras. Cuatro GameTests nuevos cubren movimiento
del atacante/del portador, revocación de construcción y defensa Heartwood.

MiningObservations exige la herramienta original también en observaciones
Iridium encoladas/Curios, y descarta al pasar a espectador. Dos GameTests
comprueban que no conceden Lode Surge ni propiedad de drops tras invalidarse.
Los siete enlaces antiguos de efectos compartidos también reservan ahora su
coste real para ambos espectadores, incluyendo el acento de Worldbreaker.
Un GameTest exige exactamente 58 partículas para los siete enlaces juntos.

Local: 25 recursos, 63 Python con Pillow, 146 Java parseados, secrets y diffcheck
correctos. Sin build local. Esperados 110 JUnit y 347 GameTests; no afirmar
resultados remotos hasta recuperar evidencia CI. Base visual preservada.

## Entrega verificada 5.10.456

- Fuente: `95b36f35908e32db1f630dfd6523cb2704da0da1`.
- CI: https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43674513875
- Prerelease: https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.456-43674513875
- JAR 553744 bytes; SHA256 `40700d0844c55e267ba41349c5022bb1969a15ca0d46e333df99b3cb557cbdfa`.
- Asset GitHub749778973 uploaded: tamaño y digest API coinciden con RELEASE.json.
- Marcadores reales de cliente: ALTERNATE_BINDINGS_REBOUND_OK e INSTRUCTION_BINDINGS_REBOUND_OK.
- Galería: `artifacts/picos-5.10.456/`, PDF13páginas, ZIP26imágenes con CRC correcto;
  procedencia vinculada a manifest CI y recursos exactos del commit.
- No certifica todas las animaciones, Ghouls ni una instalación Windows.

# 5.10.456 — CI completa correcta; prerelease de prueba publicado

WorldSafety.exchange conserva nivel, posición y herramienta originales desde
antes del primer permiso. Revalida ambas posiciones antes de escribir, entre
los dos callbacks de colocación y al confirmar. Si un listener mueve al actor o
revoca construcción, no confirma; revierte sólo escrituras que todavía le
pertenecen, sin deshacer el movimiento externo. Cuatro GameTests cubren cambios
en permisos, rotura, primera y segunda colocación.

Los plazos de ocho segundos de los contadores Curios y de Heartwood/Null Veil
compartidos usan la política validada PersistedCooldown: un plazo imposible no
bloquea la pasiva indefinidamente. Dos GameTests nuevos comprueban recuperación
y bloqueo de repetición real. CI 43674513875 completed/success: 110 JUnit sin
fallos/errores/omisiones y 340 GameTests; servidor empaquetado correcto, logs
validados, cliente con 32 escenas y 64 tooltips. Sin build local. La CI 455 inicial falló por tres pruebas que publicaban login Forge
con una conexión simulada: se extrajo restoreCooldowns, usada tanto por login
real como por pruebas; no se omitieron pruebas ni se relajaron los logs.

# 5.10.455 — recargas y referencias secundarias, pendiente de CI

Al reconectar se sincroniza sólo el tiempo restante de plazos válidos, no un
Integer.MAX_VALUE derivado de NBT corrupto. Tres JUnit y tres GameTests cubren
plazos válidos, inválidos y el mayor intervalo legítimo de principal/secundaria.
Las descripciones de secundarias también usan keybinds, no letras R/X fijas;
el cliente reasigna principal a O y secundaria a P para validar Eventide, además
del test de instrucciones principal existente. Sintonía Curios reserva20partículas
antes de emitir; saltarla no impide pasivas. Palimpsest: séptima adaptación de
contraste reproducible, gamma.84, alpha/silueta/original preservados.
454/run43672786521 pasó CI completa.455 debe repetirla:110JUnit/334GameTests
esperados; local25recursos/63Python/parser145Java/diffcheck correctos.

# 5.10.454 — ascenso sin recaptura; CI correcta

Icarian Lift detecta que el minero salió de su eje o fue teletransportado fuera
del progreso esperado. Termina sin seguir cortando ni arrastrarlo de vuelta;
respeta saltos normales con tolerancia vertical. Salir no provoca carga de chunks
ni consultas de congestión antes de parar. Mensaje ES/EN explica la detención.
Tres GameTests y cinco JUnit nuevos: esperados330GameTests/107JUnit. Mantiene
ascenso normal, barreras y fluidos bajo sus regresiones existentes. Local25recursos,
63Python/parser145Java/diffcheck correctos. Sin build local. CI43672786521
completed/success:107JUnit sin fallos/errores/omisiones y330GameTests.
Estos cambios quedan incluidos en456, cuya CI completa también pasó.

# Resultado actual: 5.10.453 verificada y publicada como prueba

- **Fuente:** `25fd933b670bae285c37b90059971e38ef1946ea`.
- **CI:** [43671030909](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43671030909), completed/success.
- **Java:** 102 JUnit, cero fallos/errores/omisiones; 327 GameTests completos.
- **Recursos:** 25 pruebas en CI. Herramientas Python: 63 pasan localmente con
  Pillow; en CI sin Pillow pasan 62 y se omite sólo el renderer opcional del PDF.
- **Runtime:** servidor empaquetado y logs de servidor/GameTests/cliente limpios.
  Cliente con 32 escenas, 64 comprobaciones de tooltip final y altura, controles
  al servidor y WorldShatter de 9.341 bloques. Marcador nuevo
  `INSTRUCTION_BINDINGS_REBOUND_OK` observado una vez tras reasignar activación a O.
- **JAR:** `special-pickaxes-1.20.1-5.10.453.jar`, 551.671 bytes, SHA-256
  `8d9a95ad465fec905eb1724e6c592640d64362bdbec45fd9566f9b07183a7967`.
  Recuperado íntegro mediante Checks; tamaño/hash/CRC correctos. Los trece modelos
  y recursos coinciden con sus fuentes y procedencia. Sin recetas, worldgen ni fixtures.
- **Publicación:** prerelease `test-5.10.453-43671030909`, asset 749676275 uploaded;
  tamaño y digest de API coinciden con el JAR verificado.
- **Galería:** `artifacts/picos-5.10.453/los-13-picos.pdf`, 13 páginas, 26 imágenes
  exactas, HTML, PROVENANCE y SHA256SUMS. ZIP con CRC correcto. Se inspeccionaron
  el retrato Icarus y SHIFT Fault Choir de esta revisión; no se certifican todas
  las animaciones ni el pack Ghouls/shaders/TPS.

## Cambios comprobados en esta tanda

1. La colocación pagada conserva el contexto creativo inicial, revalida antes de
   escribir y después del evento, y revierte su propia escritura si el modo cambia.
   Cuatro regresiones cubren ambas direcciones en claims y eventos de colocación.
2. Seam Ripper no admite un bloque marcado por un callback como origen de
   descubrimiento de más terreno. Se revalidan contexto, estado y procedencia.
3. Preview de hasta 24 puntos uniformes, incluyendo ambos extremos. Cinco JUnit
   cubren límites, planes pequeños, unicidad, espaciado y aritmética con int máximo.
4. Instrucciones de manual/SHIFT con keybinds para acción, modo y secundaria.
   El regenerador conserva placeholders; regresión recorre modos de los 13 picos.
5. Sexta adaptación de legibilidad: Icarus, gamma .82, alpha/silueta/original intactos.

452/run43669594248 pasó 102 JUnit y 326 GameTests antes del cambio de instrucciones.
El verificador histórico debe usar 326 para ese log: compararlo con el catálogo
actual de 327 lo rechaza correctamente. 453 repitió toda la CI con 327 completos.

No hubo checkout, build local, instalación Windows ni automatización de interfaz
local. La publicación de prueba sigue separada del gate visual estable.

## Historial (las notas «pendiente» siguientes preceden al resultado actual)

# 5.10.453 — instrucciones reasignables y legibilidad: pendiente de CI

Las instrucciones largas ya no fijan las letras R/C/X: se traducen con componentes
keybind para acción, modo y secundaria, en SHIFT y manual. El regenerador de
catálogo conserva el contrato. Cliente CI comprueba instrucciones con activación
reasignada a O, además del tooltip compacto. Una regresión recorre los modos de
los13picos; dos Python y una prueba de recursos comprueban traducciones.
Icarus usa una sexta adaptación reproducible de contraste (.82), conservando
alpha/silueta y el original; no se declara redibujo ni aprobación visual completa.
Local25recursos/63Python/parser143Java/diffcheck; esperados102JUnit/327GameTests.
452/run43669594248 ya completó CI correctamente;453 debe repetirla con cambios.

# 5.10.452 — previsualización completa: pendiente de CI

El muestreo de previews ahora incluye el principio y el final del plan y reparte
hasta 24 puntos uniformes. El stride anterior sólo mostraba la primera mitad de
planes de 25–47 pasos y omitía finales de chimeneas/caras mayores. No aumenta
paquetes ni toca bloques para dibujar. Cinco JUnit puros prueban orden, unicidad,
intervalos, casos pequeños e índices de tamaño Integer.MAX_VALUE sin overflow.
Conserva las transacciones y descubrimiento natural de451. Esperados102JUnit,
326GameTests. Local24recursos/61Python/parser143Java/diffcheck correctos.
No verificada todavía; última validación completa:450.

# 5.10.451 — construcción y descubrimiento: pendiente de CI

Una transacción de colocación pagada conserva el estado creativo inicial y rechaza
cambios durante claims o eventos de colocación. Evita material gratuito al pasar
a creativo y consultar el slot -1 al pasar de creativo a supervivencia. Cuatro
regresiones cubren ambas direcciones y ambas fases sin perder material.
El descubrimiento de Seam Ripper revalida actor y procedencia después del claim:
un bloque recién marcado no sirve de puente para descubrir terreno detrás.
Una regresión adicional; 326 GameTests y 97 JUnit esperados, no confirmados aún.
Local: 24 recursos, 61 Python con Pillow, parser de 141 Java y diffcheck correctos.
Sin build local ni automatización de interfaz local; última verificada: 450.

# Resultado actual: 5.10.450 verificada y publicada como prueba

- **Fuente:** `b1f495880c1debcc029422e3841120b52992e9a9`.
- **CI:** [43667431107](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43667431107), completed/success.
- **Pruebas Java:** 97 JUnit, cero fallos, errores u omisiones; 321 GameTests completos.
- **Recursos/herramientas:** 24 pruebas de recursos en CI; 61 Python correctas en
  venv local con Pillow. En el unittest de CI sin Pillow se omite únicamente el
  render opcional del PDF: 60 pasan y 1 se omite. No se confunde con JUnit.
- **Servidor/cliente:** servidor empaquetado y logs limpios. Cliente completo:
  32 escenas, 64 tooltips, 64 comprobaciones de viewport y del evento final,
  controles al servidor y WorldShatter de 9.341 bloques.
- **JAR:** `special-pickaxes-1.20.1-5.10.450.jar`, 550.032 bytes, SHA-256
  `ac50c5e88fbec79089708136b36997bd2adc7feb3f1e0ea35c8d71ce866c0fb5`.
  Recuperado íntegro mediante Checks; tamaño, hash y CRC correctos. Trece iconos
  y modelos coinciden con la procedencia exacta; sin recetas, worldgen ni fixtures.
- **Publicación:** prerelease `test-5.10.450-43667431107`, asset 749577195 uploaded;
  digest y tamaño de la API coinciden con el archivo verificado.
- **Galería:** `artifacts/picos-5.10.450/los-13-picos.pdf` (13 páginas), ZIP con
  26 imágenes exactas, HTML, procedencia y SHA256SUMS; CRC del ZIP correcto.
  Revisada vista comparativa de los 13 retratos reales. Esto no aprueba toda la
  animación temporal ni todo el pack Ghouls.

## Cambios de esta tanda

1. Tickets de limpieza de procedencia: recolocar un bloque invalida observaciones
   antiguas sin borrar la marca nueva; sólo se mantienen tickets pendientes.
2. Forge EntityMultiPlaceEvent: cada snapshot válido del mismo mundo se registra,
   con exclusiones de fluidos y contenedores; no se fuerzan chunks.
3. Lode Sense: se revalidan permisos, actor y stack exacto después de callbacks.
4. Contornos de selección y trazas comparten presupuesto visual. Agotarlo no
   cancela la selección ni la minería autorizada.
5. Eventide: quinta adaptación de legibilidad reproducible, sin cambiar alpha,
   silueta ni el original preservado.
6. Controles: recuperación ante inputTick corrupto por overflow; una reanudación
   denegada ya no devuelve éxito ni emite su señal de confirmación.
7. Evidencia Checks: selección por intento/digest, rechazo de exportaciones
   incompletas nuevas sin recurrir a una antigua; compatible con historial.

449 falló por AuthenticationUnavailableException del servicio externo Minecraft,
aunque completó las escenas y las pruebas de servidor. No se ignoró el escáner:
450 repitió la validación completa y sus logs están limpios. Se conservan las
notas históricas siguientes para no reescribir ese fallo como éxito.

No hubo checkout, build local, instalación Windows ni automatización de interfaz
local. La publicación es de prueba; el gate visual de versión estable sigue separado.

## Historial (las notas «pendiente» siguientes son anteriores al resultado actual)

# 5.10.450 — validación repetida y evidencias por intento, pendiente de CI

449/run43666174413 pasó97JUnit(cero fallos/errores/skips),321GameTests y
servidor empaquetado. Cliente completó escenas/controles/WorldShatter9341pero
logs detectaron AuthenticationUnavailableException al contactar servicio externo
Minecraft. No se ignora el error ni se certifica449verde. Se repite CI íntegro.

Exportación Checks identifica intento; recuperación selecciona un conjunto completo
por intento/digest, nunca mezcla chunks ni cae a evidencia vieja si el intento más
reciente está incompleto. Compatible con exports históricos, siete regresiones
Python y límite de128chunks. Local24recursos/61Python pasan conPillow; sinPillow
60pasan/1skip(rendereropcional). Runtimegameplayidéntico449,versión450.

# 5.10.449 — colocación múltiple Forge, pendiente de CI

EntityMultiPlaceEvent hereda EntityPlaceEvent, pero getPos sólo cubre la primera
celda. El rastreador ahora recorre snapshots del mismo nivel, sin cargar chunks,
con las mismas exclusiones de fluidos/blockentities. Protege construcciones de
colocación múltiple frente a minería de terreno natural. Tres GameTests cubren
todas las celdas/cancelación/exclusiones.321GameTests y97JUnit esperados.
Conserva447/448. Local24recursos/54Python/parser141Java/diffcheck correctos.
Aún no verificada;446permanece la última confirmada.

# 5.10.448 — controles recuperables, pendiente de CI

El throttle de inputs rechaza diferencias negativas por overflow: inputTick
Long.MIN_VALUE no puede bloquear controles indefinidamente. Cinco JUnit puros
cubren límites/rollback/extremos. Pausa/reanudación ya propaga un rechazo real
por chunks/drops envezde informar éxito y emitir cue. Dos GameTests nuevos.
Conserva cambios447: procedencia con tickets,passivepull,Eventide y contornos.
Esperados97JUnit/318GameTests. Local24recursos/54Python/parser141Java correctos.
No buildlocal ni lanzamiento de interfaz local.446sigue última verificada.

# 5.10.447 — nueva tanda integral pendiente de CI

La limpieza diferida de procedencia usa tickets: una colocación/recolocación
invalida el break anterior, sin borrar la protección del bloque nuevo. El mapa
transitorio sólo conserva posiciones con limpieza pendiente; no se serializa.
Lode Sense revalida mayBuild/actor y stack exacto tras claims. Wireframes y trazas
comparten presupuesto cosmético sin impedir selección. Eventide obtiene adaptación
RGB reproducible 32px, conserva alpha/silueta/original. Siete regresiones nuevas,
316GameTests esperados. Local24recursos/54Python/parser139Java correctos.
Sin buildlocal; compilar/servidor/cliente/publicación pendientes de CI.

# Resultado actual — 5.10.446 verificada y publicada como prueba

Fuente `63057f66abb6acd765f2dc5172c3f6fdd02a7c92`.
CI [43663630681](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43663630681)
completed/success. Evidencia Checks recuperada con manifiesto verificado.

-92JUnit,0fallos,0errores,0skips;309GameTests completos.
-Servidor empaquetado y escaneo de logs de servidor/GameTests/cliente limpios.
-32escenas,64tooltips normales/SHIFT,64marcadores viewportfit/stackevent;
 controles de teclado al servidor yWorldShatter9341completados.
-24recursos enCI;54Python envenvlocal conPillow. El render PDF opcional se
 omite enel unittestCI sinPillow;53restantes pasan. No se confunde conJUnit.
-JAR548113bytes,SHA256`ebf010a1ca26379eaed0f4428323e73485a57bf5757bbd24661a7522ed3c0bb0`;
 JARrecuperado íntegro,CRC/hashOK,sinrecetas,worldgen,clases/tags de fixtures.
-Release`test-5.10.446-43663630681`,prerelease=true,asset749449808uploaded;
 tamaño/digestAPI coinciden. No se afirma descargaCDN desdeWindows.
-Galería`artifacts/picos-5.10.446/`:PDF13páginas,26imágenes originales,
 HTML,PROVENANCE,SHA256SUMS yZIPCRCcorrecto. Íconos ligados acommit exacto.
 Capturas reales, no equivalen a aprobación de animación completa.

Inspeccionadas capturasSHIFTWorldbreaker446 ySeamRipper446. Worldbreaker ya
muestra nombre/qué/cómo/alternativa/coste/tecla manual sinclipping visible enesa
captura. No se aprueba automáticamente todo el catálogo niGhouls/shaders/TPS.

Fallos441/443/444 por tagfixture requerido encliente no se reescriben como éxito;
445 corrige referencia opcional.446 añade referenciaSHIFT legible con gatealtura.
No hubo checkout,buildlocal,instalaciónWindows ni automatización de interfaz local.

## Historial de desarrollo (las notas «pendiente» siguientes son históricas)

# 5.10.446 — SHIFT legible, pendiente de CI

Inspección de la captura real444worldbreaker-carve: SHIFT recuperó detalles pero
la ficha completa se salía por arriba. Separa referencia rápida de12líneas lógicas
(qué/cómo/alternativa/coste/tecla de manual) y manual completo desplazable, que
conserva pasivas/perfil/modos. Cliente CI ahora calcula wrapping a320px y exige
que tooltip completo quepa enel viewport capturado. No seoculta clipping
suprimiendo la prueba. Una regresión recorre13picos.309GameTests esperados.
24recursos/54Python local, parser139Java; compilación/runtime pendientes.

# 5.10.445 — corrección de tag de fixture, pendiente de CI

444/run43662073543 falló al escanear logs del cliente: needs_higher_tool exigía
superior_ore aunque testOres no registra el bloque enrunClient. No se silencia el
escáner: referencia opcional como las demás fixtures; regresión recorre tags de
fixtures y exige optional enreferencias ajenas. Los GameTests conservan asserts
de membership/tier para que optional no oculte una fixture ausente enservidor.
444 sí pasó92JUnit(sinfallos/errores/skips),308GameTests,servidor empaquetado;
cliente llegó a32escenas/64tooltips/WorldShatter9341pero no se certifica verde.
445mantiene las mejoras runtime y debe repetir todaCI.

# 5.10.444 — pendiente de CI

Mantenimiento integral: el actualizador del catálogo ya no elimina iconos_v2,
fuentes preservadas ni metadatos de animación. Regresión con árbol desechable.
Guía de13picos actualizada: elimina secundarias obsoletas de combate y explica
controles, durabilidad, permisos y alcance real de las comprobaciones.
La prueba multivariante de revocación sólo declara éxito al terminar las4familias.
Exportador PDF/ZIP comprobado con fixtures temporales:13páginas,26imágenes,
bytes originales, hashes yCRC; no son fotos de juego ni se entregan como tales.
Local:23recursos y54Python pasan envenvconPillow (sinPillow:53pasan/1skip),
parser139Java/diffcheckOK.308GameTests/92JUnit esperados pendientes deCI.

# 5.10.443 — revisión integral pendiente de CI

Cierra la ventana entre WorkProgram.next y apply: no aplica pasos con actor
invalidado. Secundarias mineras rechazan permisos revocados en callbacks;
Starfall y Seam Ripper comparten la captura de contexto del resto de utilidades.
308GameTests esperados,92JUnit esperados (no confirmados en esta revisión).
Local:23recursos,52Python,parser139Java,diffcheck. Galería reproducible con
`tools/gallery_deliverable.py`, sin arrancar juego ni automatizar interfaz local.
No entregar443como verificada sinCI;439sigue siendo la última confirmada.

# 5.10.442 — pendiente de CI, no entregar como verificada

Mensajes ES/EN de secundaria sin objetivo incluyen una orientación específica
por pico; cooldown principal/secundario muestra segundos restantes y el rechazo
por durabilidad explica el mínimo necesario. Reanudación y ejecución de colas
revalidan actor, herramienta exacta y permiso de construcción también después
de callbacks. Tres regresiones adicionales:305GameTests esperados, no comprobados.
Local:22recursos,49Python y parser139Java pasan; no se ejecutó build local.

# Estado actual — 5.10.441: segunda fase pendiente de CI

441: tiers explícitos Forge respetados aunque la mena se reconozca por nombre;
regresiones mantienen tier5/velocidad64+ en bloques vanilla y menas extranjeras.
SeamRipper32px: adaptación de contraste reproducible, alpha/silueta/fuente intactos.
Bursts/previews y pulsos de trabajo de las13familias cubiertos por reservas para
más rutas mineras (no se afirma que todo feedback legado use el mismo presupuesto).
Paleta de bursts reutilizada; saturación visual nunca cancela minería.
Cinco GameTests adicionales,302esperados;92JUnit esperados. Local22recursos,
49Python, parser139Java ydiffcheck pasados. Java/cliente pendientes, sin buildlocal.

# Estado actual — 5.10.440: primera fase pendiente de CI

440: snapshot de actor/herramienta y posición/stack exactos de drops tras permisos
para Eventide/Iridium. Movimiento de drops finito y acotado, incluso con
velocidades inválidas/extremas de otros mods. Tooltip SHIFT reconstruido también
en ItemTooltipEvent; CI ahora valida getTooltipLines completo, no sólo el builder.
Diez GameTests nuevos (297 esperados), seis JUnit nuevos (92 esperados).
Local:22recursos/49Python/parser139Java/diffcheck. Sin build local.

# Estado actual — 5.10.439: CI completo y prerelease de prueba verificados

## Resultado verificado — 5.10.439

Fuente `f07795818b39ce3d7172d7c175cb06a8992bff10`;
[CI43562331609](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43562331609)
completed/success. 86JUnit, 0fallos/errores/omitidos; 287GameTests completos.
Servidor empaquetado y logs limpios; cliente32escenas/64tooltips, teclas reales
activar/cancelar y WorldShatter9341bloques verificados. 49Python y22recursos
pasados en el workflow. No build local ni instalación Windows.

[Prerelease de prueba](https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.439-43562331609),
no latest estable ni aprobación visual completa. JAR `special-pickaxes-1.20.1-5.10.439.jar`:
541982bytes; SHA256 `37069f2693c3cbe3eb2cd7c9b65ff17dd4e266fdc9fae6d43416d5ea3b1e5c44`.
Asset746466840 uploaded y digest API coincidente. Bytes recuperados por Checks,
CRC/commit/hash y13modelos/texturas runtime verificados; no afirmar descarga CDN.

Cambios de esta tanda: pasarela sin raycast y pago sólo offhand, detención ante
obstáculo/claim/material agotado; sondeo nearest32natural con coordenadas;
revalidación de actor/procedencia/permiso en minería nativa, radiales, IcarianLift,
SeamRipper y rutas naturales; transmutación/pago con rollback tras callbacks;
firmas orientadas; tres adaptaciones de legibilidad sin cambiar alpha/silueta;
manual/SHIFT coherentes con copia, teclas reasignadas, costes y scroll/nombres.
Conserva13picos, daño base4, tier5, velocidad de tier64 y32768usos; cero recetas.

Fotos439: `artifacts/picos-5.10.439/` (ignorado por Git): PDF13páginas,
ZIP26fotos+HTML+PDF+vista general+PROVENANCE.json. Capturas genuine del cliente
439, hashes/commit/run verificados. No son imágenes generadas ni prueba de todas
las fases de animación. Los registros «pendiente» que siguen son históricos.


433 (pendiente CI): tres adaptaciones reproducibles de legibilidad, sin cambiar
silueta/alpha ni archivos originales. Manual respeta teclas reasignadas, invalida
caché de idioma/controles y evita desbordar nombres largos. Pasarela/transmutación
revalidan actor, permisos y procedencia tras eventos externos; rollback y pago
protegidos. Cuatro GameTests nuevos (277 esperados), cinco Python nuevos (33
pasados localmente); 22 validaciones de recursos y parser de 136 Java pasados.
No build local. Resultados Java/cliente todavía pendientes; no aprobación visual.

429: nuevas secundarias mineras para siete familias, sin interacciones de combate
en X; conserva utilidades de las restantes y copia Worldbreaker. Daño melee base
25→4; mineríaOP conservada. Trece firmas visuales propias, traducciones ES/EN y
galería genuine icono+mano en preparación. 263 GameTests/83JUnit esperados, pendientes.

431 fuente `47e535100fbb348e809e7a68927819f3f83700b2`,
[run43557399012](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43557399012)
completed/success. 83JUnit/263GameTests pasados, cliente32escenas/64tooltips,
teclado-servidor yWorldShatter9341. Prerelease público
[test-5.10.431-43557399012](https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.431-43557399012)
con JAR/RELEASE.json/SHA256SUMS, no marcado latest estable. Sin aprobaciónvisual
completa. JAR536128bytes,SHA256
`6b388089413289bc53253529a31d2d6e2d8f5f1ea070584bcd20835cfa32ae46`.
Bytes recuperados por Checks y estructura/hash verificados; digest público del
asset746333222 coincide. CDN de release bloqueado en este sandbox; no afirmar
una descarga directa CDN completada ni instalación en Windows.

**430 fuente `5f7f8443d1b6a16a80e8859d156f0d47399e9f49`,
[run43551696313](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43551696313): completed/success.**
83 JUnit cero fallos/errores/skips;263GameTests pasados. JAR/servidor/logs y
cliente32escenas/64tooltips, teclado y WorldShatter9341. Bundle54fragmentos
recuperado/verificado con13capturas de mano. GaleríaPDF/HTML/ZIP en
`/home/user/entrega-picos-5.10.430`; trecepares revisados en vista general, no
aprobación temporal completa. Publishskipped, no instalación Windows.
Detalle: [MINING-ALTERNATES-430.md](MINING-ALTERNATES-430.md).

**Fuente `7904bf8a6be920e4b6227620cb3addd5a4a10e20`,
[run 43548490641](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43548490641): completed/success.**
81 JUnit sin fallos/errores/skips y All 261 required tests passed. JAR/servidor/logs
correctos; cliente real 32 escenas/64 tooltips, teclado-servidor y World Shatter
9341. Bundle Checks54 recuperado/verificado `/tmp/sp-ci428`; logs GameTest y cliente
reescaneados localmente, limpios. Sin inspección visual de esta galería, no aprobación
visual completa/temporal. Publicación binaria skipped; no instalación Windows.
Artifact forge-validation 14095304337 (40237972 bytes). RELEASE.json verificado:
JAR533981 bytes, SHA256 `32fc53ed93f7a6003554d276faa94d63bfe9c6f5cb09704d35fe49c64046de03`.
Se verificó el manifiesto del bundle, no una descarga/instalación del JAR.

427: aterrizajes Eventide protegidos frente a reentrada/cambios durante permisos;
Icarus valida caídas finitas; Starfall/Lode Sense/escaneos aliados recuperan plazos
imposibles. Flashes Starfall y ripple Eventide tienen presupuesto global. Once
GameTests (261 totales) y tres JUnit nuevos, ejecutados y pasados en 428.

427 `aa73158` / run43547927879 completed/failure: 81 JUnit pasados,
260/261 GameTests. Falló únicamente exodiumStarfallCatchRechargesAfterThirtyTicks,
que llamaba a tryCatchIncoming sin aplicar STARFALL_WARD. 428 añade la protección
al fixture y conserva la comprobación en producción; CI propio exitoso indicado arriba.

426: Estasis compartida protegida frente a reentrada, eventos inválidos y cambios
en callbacks; recuperación de cooldown y arrastre Gravitic acotado. Tres auras
beneficiosas sobre mobs ahora respetan audiencia/coste global. Diez GameTests
(250 totales) y tres JUnit añadidos: ejecutados y pasados.

**Fuente `4262a10c219873d49abba6a53509ed352b5a3c45`,
[run 43546125974](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43546125974): completed/success.**
78 JUnit, cero fallos/errores/skips; All 250 required tests passed. JAR/servidor/logs
success; cliente real 32 escenas/64 tooltips, teclado-servidor activate/cancel y
World Shatter 9341. Bundle 56 fragmentos recuperado/verificado `/tmp/sp-ci426`.
Sólo una captura 426 inspeccionada, no aprobación visual completa/temporal.
Publicación binaria skipped; no instalación Windows. Artefacto forge-validation
14094209314 (43789979 bytes). Manifiesto de JAR 532425 bytes, SHA256
`18f357ac5b551b744c626461bfaed64f3647f3fb04ba7f5843c8abfd8ddc32f0`.
Se verificó el manifiesto del bundle, no una descarga/instalación del JAR.

425 / `a9016dd` / run 43545570252: failure global. Build y 78 JUnit OK;
250 GameTests pasados. Escaneo de logs rechazó el error deliberado emitido por
Forge al lanzar la excepción del fixture179. Servidor empaquetado y cliente
omitidos. En 426 se conserva el escaneo sin excepciones/allowlists y se sustituye
el fixture por denegación/reintento; no cambia el código de gameplay de 425.

## Evidencia verificada el 2026-11-13

Rama de sesión: `arena/cec54523-special-pickaxes`. No instalación Windows ni
control de la PC. Las validaciones de una fuente no certifican cambios posteriores.

| Versión | Fuente / run | Resultado comprobado |
|---|---|---|
| 5.10.411 | `e2c3699` / [43443796300](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43443796300) | Build/JUnit, 182 GameTests, servidor y cliente completos. Falló después al exportar evidencia. Sin aprobación visual/global. |
| 5.10.412 | `3cd8c71` / [43447050583](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43447050583) | Build y 60 JUnit OK; 187 GameTests, uno fallido en fixture de propiedad Iridium. Bundle Checks recuperado y verificado. Cliente omitido. |
| 5.10.413 | `460a5af` / [43447481028](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43447481028) | Build y 60 JUnit OK; 190 GameTests, mismo único fallo. Bundle recuperado y verificado. Cliente omitido. |
| 5.10.414 | `1bbd645` / [43448248860](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43448248860) | CI global success: build/JUnit, 197 GameTests, servidor, logs, cliente y exportación de evidencia. 32 escenas/64 tooltips, 9341 bloques. Publicación binaria omitida; sin revisión visual completa. |
| 5.10.415 | `74c4481` / [43449061170](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43449061170) | CI global success confirmado. Sin inferir aprobación visual ni instalación. |
| 5.10.416 | `3e4eecb` / [43450387194](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43450387194) | CI global success; evidencia cliente recuperada/verificada (32 escenas, 64 tooltips y World Shatter). Sin aprobación visual completa ni instalación. |
| 5.10.417 | `5ec7288` / [43452383160](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43452383160) | Failure cliente por autenticación devolviendo HTML; bundle recuperado. Build/JUnit, 207 GameTests, servidor y logs pasaron. |
| 5.10.418 | `cb2c5be` / [43452644898](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43452644898) | CI global success, evidencia cliente recuperada/verificada. Sin aprobación visual completa ni instalación. |
| 5.10.419 | `4e6e60b` / [43537824437](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43537824437) | Build/JUnit, 215 GameTests, JAR, servidor y logs success; cliente en curso. |
| 5.10.420 | `0bade7a` / [43537968640](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43537968640) | Build/JUnit, 218 GameTests, JAR, servidor y logs success; cliente en curso. |

5.10.424 mejora caché térmica sin cargar chunks, deduplicación/clock de roturas,
recuperación y overflow de rachas, y presupuesto cosmético de Iridium. Ocho
GameTests y tres JUnit añadidos. Fuente `328077af660e9873578d100d5417924af0314cb8`,
[run 43543547256](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43543547256)
completed/success: 75 JUnit sin fallos/errores/skips y 240 GameTests pasados.
JAR/servidor/logs y cliente 32 escenas/64 tooltips, teclado-servidor y World Shatter
9341 correctos. Bundle de 55 fragmentos recuperado/verificado en `/tmp/sp-ci424`.
No revisión visual completa de esta galería; publicación binaria skipped, no instalación Windows.
Artefacto `forge-validation` 14092744566. Manifiesto: JAR 532550 bytes, SHA256
`1c32c52fa672b9e3ef72c1d8f70669f43afefe64e7357c79ed5a22f5bcb8615c`.
El manifiesto se verificó dentro del bundle; no se descargó/instaló el JAR aquí.

**Fuente `72b7b8b432913b3d78ba906920b5c7e88edce2ae`: [run 43540141390](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43540141390) completed/success.**
72 JUnit sin fallos/errores/skips, 232 GameTests completos, JAR/servidor/logs y
cliente real (32 escenas, 64 tooltips, controles y 9341 bloques World Shatter).
Bundle Checks recuperado/verificado en `/tmp/sp-ci423`; tres capturas revisadas,
no galería completa. Publicación binaria skipped; no instalación Windows.

421/422: build y 72 JUnit correctos, runs globales fallidos en un GameTest espacial
distinto cada uno. Bundles recuperados. 423 aísla los fixtures sin borrar sus
aserciones de gameplay; 232 GameTests ya pasados por el CI de esta nueva fuente.

5.10.422 extiende guardas/revalidación a la ruta real de controles, recupera sus
cooldowns y prueba Worldbreaker copiado. Cinco GameTests adicionales; total 232
en fuente, ya pasados por el CI de 423, que conserva estos cambios.

5.10.421 reúne cambios de combate, reentrada de alternativos, memorias tipadas y
caducidad, presupuestos cosméticos y fundido de siete partículas. Añade nueve
GameTests y ocho JUnit; 227 GameTests al añadirse; los cambios se conservan en 423, cuyo CI pasó 232.

5.10.420 añade cuatro JUnit y tres GameTests de recuperación de cooldowns
persistentes sin cambiar cadencia normal. 218 GameTests pasados
en su propio CI; cliente y resultado global aún pendientes.

5.10.419 añade cuatro regresiones de movimiento/recolocación; 215 GameTests
pasados en su propio CI; cliente y resultado global aún pendientes.

5.10.418 añade cuatro regresiones de Hellspec: atribución, reemplazos externos y
objetivos movidos en callbacks; total 211 GameTests ya pasados por el CI de esa fuente.

5.10.417 añade cinco regresiones de cancelación/sustitución/pausa y reentrada del
scheduler; 207 GameTests pasados en su propio CI.

5.10.414 corrige la confusión entre lanzador de `ItemEntity` y reserva vanilla de
recogida, refuerza permisos tras callbacks y tolera cancelación reentrante de
raíces/campos/selecciones. 5.10.415 extiende la corrección al alternativo Eventide.
Tier OP, herramienta correcta al minar y cero recetas conservados. Validación
local: 22 pruebas de recursos, 28 Python y parser de 124 archivos Java; no build local.

## Registro histórico: 5.0.0

# Rework minero — 5.0.0 validado y publicado

Se mantiene el diseño de nueve artefactos de 5.0.0. La fuente, revisión visual y reproducción binaria están aprobadas. El JAR final está publicado.
Los resultados de un commit anterior no certifican sus correcciones posteriores.

## Correcciones verificables

- Nueve herramientas combinadas; 23 modos; retiradas las expectativas de los once
  artefactos eliminados. Referencias y ocho texturas archivadas conservadas.
- Modos y manuales específicos, etiquetas españolas de Worldloom, README actual,
  migración `miningSchema=5` que no convierte modos antiguos de Worldbreaker/Crisol
  en operaciones nuevas. Eliminado el texto de construcción de costes genéricos.
- Selección con esquinas distintas, propiedad de herramienta y modo; paquetes con
  UUID y secuencia; latch físico y guardas contra reentrada de activación/scheduler.
- Bedrock, fluidos source/flowing y fluido Forge externo, claims, bloques con
  entidad, fronteras, chunks y barreras de rutas cubiertos por pruebas nativas.
- Rotura nativa revalida el objetivo tras callbacks Forge. Las reversiones de
  colocación, transmutación e intercambio no sobrescriben barreras o máquinas
  introducidas por otro listener. Dos regresiones nuevas cubren estos casos.
- Worldloom: tres estructuras pagadas y orientadas. Crisol: regional y radial.
  Axiom no muestra candidatos ocultos antes de excavar. Estasis libera velocidad
  acotada, sin aumentarla. Niveles altos de encantamiento conservados y probados.

## Evidencia reciente — no mezclar identidades de build

| Fuente | Run de Actions | Resultado observado |
|---|---|---|
| `9fc9542` | `37681746690` | Éxito completo. 55 GameTests; cliente con 23 activaciones y 46 tooltips. WORLD SHATTER retiró 9341 bloques reales y conservó la barrera. Se inspeccionaron sus dos capturas supremas, no toda su galería. No aprobado para publicación. |
| `44577ce` | `37682889162` | Run completo exitoso; anterior al endurecimiento de rollback. No certifica el candidato actual. |
| `a5c2554` | `37683328797` | Build Java 17 y los 57 GameTests aprobados; servidor empaquetado superó su etapa. Cliente falló antes de producir capturas: timeout de la ventana temprana de Forge. Run fallido, no publicable. |
| `0c7233659d3b695da9211575f3356d81a1aeb1a8` | `37685753507` | Éxito completo: 20 JUnit, 57 GameTests, 8 pruebas de recursos, servidor empaquetado, 23 modos y 46 tooltips; 9341 bloques en la escena suprema. Deshabilita únicamente el splash temprano de Forge en la configuración desechable de CI para evitar el fallo anterior. Conserva la ventana real, capturas y todas las aserciones del cliente. |

El conjunto actual contiene 20 JUnit, 57 GameTests y 8 pruebas Python de recursos.
Las ocho pruebas Python y el chequeo de diff pasaron localmente; Java se ejecuta
con Java 17 en Actions, no se afirma una ejecución Java local.

## Galería y publicación

La galería exige inventario, vista de los nueve items, 23 normales, 23 SHIFT,
cambios de modo, ejecuciones, controles físicos y escena suprema. El cliente usa
Xvfb/renderizado por software. La escena suprema tiene visión nocturna VANILLA
concedida exclusivamente por el fixture de prueba y cámara en primera persona;
no es una habilidad del artefacto ni se incluye el fixture en el JAR distribuible.
Los JPG de revisión proceden del mismo frame Robot que los PNG, vinculados por
`image-manifest.json` y hashes; no son imágenes generadas ni recreaciones.

Se revisaron las 95 copias JPG, con hashes contrastados: 23 normales, 23 SHIFT,
23 ejecuciones, 14 cambios de modo y 12 vistas adicionales. Los tooltips se
inspeccionaron en planchas de sus regiones completas; las escenas, en planchas
y vistas completas de inventario, galería y suprema. `VISUAL-REVIEW.json` registra
la procedencia y las limitaciones visuales: túneles oscuros de Ícaro/CORE DRILL,
ancla elevada de Worldloom y cancelación de seguridad ante la barrera suprema.
La publicación reconstruyó el commit exacto y comprobó SHA-256 idéntico al JAR
ya sometido a la suite completa. El workflow `37687885085` terminó exitosamente;
la descarga recuperada también pasó comparación binaria, ZIP/Java 17, catálogo
y escáner de secretos. No se publica un JAR provisional.

## Limitaciones pendientes de pruebas reales

No se usó ni modificó el repositorio siege, ni se accedió a test-1. No se probó el
modpack completo, shaders, LOD, configuraciones reales de claims ni TPS
multijugador. Las pruebas de Forge/claims no equivalen a certificar SecurityCraft,
Create, ToolLeveling, Better Combat, Relics o las configuraciones del servidor real.
La protección frente a callbacks cubre los casos descritos, no una garantía ante
modificaciones arbitrarias del mundo por cualquier otro mod.

## Identidad de la entrega final

- Fuente: `0c7233659d3b695da9211575f3356d81a1aeb1a8`. Run de suite: `37685753507`.
- JAR: `special-pickaxes-1.20.1-5.0.0.jar`, 186554 bytes.
- SHA-256 esperado: `c4b7a6f2b3041532cd4dff9823b5e8adbfc317b169153491f69f916fb0635857`.
- Publicación: run `37687885085`, aprobado. Commit de aprobación: `fb6dd2d064a9a986dd0f8259438922e5bce660f0`.
- Artefacto: [special-pickaxes-5.0.0-verified](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/37687885085/artifacts/11512342063).
- Copia local: `artifacts/5.0.0/special-pickaxes-1.20.1-5.0.0.jar`.
- Galería autocontenida: `artifacts/5.0.0/GALERIA.html` (95 capturas y hashes).
- Los commits posteriores de documentación no cambian la identidad de la fuente compilada.

429 run43551389102: failure JUnit por bootstrap de ArtifactKind en los dos nuevos tests de geometría; 81/83 pasados. 430 separa geometría pura y añade verificación de trabajo completado. CI propio pendiente.

434 pendiente CI: contexto de rotura nativa captura nivel/posición/permisos;
propaga naturalOnly a Seam Ripper, radiales, Icarian Lift y rutas naturales
Worldloom/Exodium. Tres regresiones adicionales (280 GameTests esperados).
432 terminó con 86 JUnit pasados y 272/273 GameTests: falló la comprobación
combinada de la pasarela sin apuntar. Fixture ahora despeja columna vertical y
separa las aserciones de raycast/activación. No se publicó432. 433 sigue en CI.

435 pendiente CI: SHIFT y manual comparten identidad/modo copiado; mantienen
pasivas propias. Tooltips no anuncian cambio de modo inexistente en picos de un
solo modo o copias. Costes usan teclas reasignadas y aritmética long; caché y
manual consumen la misma tarjeta de coste. Cinco regresiones nuevas (285
GameTests esperados). La rotura captura actor antes, no después, del callback
de permisos. Validadores estáticos pasados, Java/cliente aún pendientes.

436 pendiente CI: galería atada a commit/run/manifest y hashes de retratos;
verificación de bytes exactos y procedencia de modelos/texturas/animaciones del
JAR. Dieciséis pruebas Python nuevas (49 pasadas localmente), sin build local.
433 completó86JUnit y276/277GameTests: sólo falló el test combinado de pasarela
anterior a separar/despejarfixture; sus cuatro nuevas regresiones de pago/Crisol
pasaron. Ningún JAR432/433 publicado. 434/435 todavía en CI.

437 pendiente CI: Barrido de Falla reutiliza la cara del único raycast que evita
chunks descargados, sin segundo clip de24bloques; conserva procedencia natural
también tras preview y valida actor tras callbacks. Dos regresiones nuevas:
287GameTests esperados. 49Python/22recursos/parser136Java/diffcheck locales OK.

438 pendiente CI: estabiliza la regresión histórica de cancelación reentrante de
Worldloom. En435 falló sólo ese test (284/285GameTests), no los nuevos tests de
guía/contexto. El fixture ahora espera la cadencia real del mundo en lugar de
suponer un offset entre GameTest y ticks; slimes suspendidos fuera de paredes.
Sin cambiar la habilidad ni relajar sus aserciones. 287GameTests esperados.

439 pendiente CI: la espera en438 no estabilizó el fixture histórico (286/287
GameTests; sólo falló126). Regresión reentrante ahora síncrona: presta un instante
del reloj alineado, ejecuta tick/restablece reloj/listener/bindings en finally.
Evita ticks de entidades entre setup y aserción, sin cambiar la habilidad ni
reducir las dos vinculaciones/assertcalls==1. 86JUnit observados pasados en437.
