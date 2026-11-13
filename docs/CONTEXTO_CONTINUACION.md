# Contexto de continuación — Special Pickaxes

Actualizado: 2026-11-13

## Objetivo del usuario

Mejorar el mod para Minecraft 1.20.1/Forge y usarlo en el modpack `ghouls`. Requisitos que siguen vigentes:

- Compatibilidad con los mods del modpack, especialmente romper bloques usando el nivel/herramienta correcto y respetando el flujo normal de eventos de Forge.
- Los picos deben ser OP y no deben tener recetas de crafteo.
- Se pueden mejorar habilidades, efectos, animaciones y texturas.
- El JAR final debe quedar en `D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods`; allí debe quedar solo el JAR más nuevo de este mod.
- El usuario pidió trabajar desde GitHub, sin checkout/build local, y explícitamente pidió no controlar su PC ni automatizar su interfaz. No usar UI automation. El usuario autorizó que se abra Minecraft si hace falta para revisar logs/pruebas, pero no que se controle su PC.

## Rama y PR

- Repo: https://github.com/Santi-PdR/Special-Pickaxes
- Rama de trabajo: `codex/test1-507-icons`
- PR draft #2: https://github.com/Santi-PdR/Special-Pickaxes/pull/2
- Base del PR: `arena/74873685-special-pickaxes`, commit `fc84a8720e32a5742b0a9758551b98d18dec2800` (baseline 5.10.406).
- El código validado para 5.10.407 estaba en `43e2801226e634b27d39c345c2ebb67e6320f5eb`.
- El PR mantiene baseline 5.10.406 en habilidades, compatibilidad y comportamiento; añade cuatro iconos runtime 32×32 propios para Exodium, Iridium, Hellspec y Worldbreaker, sin quitar las referencias originales/hashes, y ajusta esperas y cobertura visual de CI.
- No se añadieron recetas.

## Estado verificado más reciente

El workflow **Forge verification** del commit de código `43e2801...` terminó en **success**:

- GitHub Actions run: https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/40761842497
- Pasaron la validación de recursos/secretos, build y comprobaciones del JAR, GameTests/logs de runtime, cliente real con galería/tooltips, revisión visual, `git diff --check`, aprobación de publicación y publicación del artefacto.
- Artefacto del JAR: [special-pickaxes-5.10.407-verified](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/40761842497/artifacts/12849296809)
- Artifact ID: `12849296809`; SHA-256 del ZIP del artefacto: `0c9a38a805b0101afc489f8751764d0e00b60f629138be82553459ede5e4cf5e`.
- Dentro del ZIP se publicaron el JAR, `build/RELEASE.json` y `build/SHA256SUMS`. Verifica el nombre y hash del JAR con esos manifiestos antes de instalarlo.
- En la revisión previa se identificó como esperado `special-pickaxes-1.20.1-5.10.407.jar`, SHA-256 `e75aaf8f0ca196a8ee613cb98f3917b077ff4fb168b98ee4e865c0245edcbd0c`. Confirma el SHA contra `build/SHA256SUMS` del artefacto descargado.
- El run también generó el artefacto de evidencias `forge-validation` (ID `12849617497`).
- El repositorio no tiene una GitHub Release; el artefacto de Actions es la entrega verificada disponible.

## Pendiente prioritario

El JAR **todavía no se ha copiado** a `test-1\mods`. Ése es el paso pendiente que dejó al usuario esperando. No afirmes que está instalado sin verificarlo.

1. Revisa este archivo y vuelve a consultar el estado/artefactos del run #40761842497. Usa el artefacto verificado; no reconstruyas ni cambies código solo para obtener el JAR.
2. Descarga el ZIP del artefacto, inspecciona `build/RELEASE.json` y `build/SHA256SUMS`, y confirma nombre, versión y SHA-256 del JAR.
3. Instala ese único JAR en la ruta de `test-1\mods`, quitando allí los JAR anteriores de **este mod** para que quede solamente el nuevo. No borres otros mods.
4. Comprueba el contenido final del directorio (un JAR de Special Pickaxes) y hash. Respeta la restricción de no controlar la interfaz/PC. Si el acceso directo a esa ruta no está disponible, explícalo claramente y ofrece el artefacto/link para que el usuario lo descargue; no simules instalación.
5. Luego actualiza este contexto con el resultado y continúa las mejoras/compatibilidad pedidas; no marques el proyecto completo solo por tener el artefacto de CI.

## Registro breve

- Un intento anterior de CI pasó la prueba del cliente, pero no publicó artefacto porque la revisión visual esperaba más capturas. Se amplió la lista de capturas; el run #40761842497 pasó revisión visual y publicó ambos artefactos.
- En prueba automatizada previa se observaron los 13 picos en galería/inventario y los cuatro iconos nuevos distintivos. El run #40761842497 es la referencia de validación para este build.
- La última comprobación fresca de este handoff (2026-11-13) confirmó run exitoso y artefacto publicado; no confirmó el estado del directorio local `test-1\mods`.

## Instrucciones iniciales para la siguiente IA

Lee primero este archivo y las skills `minecraft-modding` aplicables. Trabaja sobre `codex/test1-507-icons` y PR #2. No crees una rama nueva ni vuelvas a empezar el mod desde cero. Primero completa la instalación pendiente del JAR verificado; después sigue con mejoras concretas sin perder el requisito de compatibilidad del modpack, minería con nivel correcto, picos OP y cero recetas. No controles la PC del usuario.

## Sesión 2026-11-13 (rama arena/cec54523-special-pickaxes, versión 5.10.408)

- **Instalación del JAR 5.10.407: sigue pendiente y no puede ejecutarse desde el sandbox.** La carpeta `D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods` está en la PC del usuario; el sandbox no tiene acceso a ella. No se afirmó instalación. El artefacto verificado sigue siendo la entrega: https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/40761842497/artifacts/12849296809 (JAR `special-pickaxes-1.20.1-5.10.407.jar`, SHA-256 `e75aaf8f0ca196a8ee613cb98f3917b077ff4fb168b98ee4e865c0245edcbd0c`, 515303 bytes, verificado contra `build/SHA256SUMS` de la evidencia de CI).
- La API de GitHub agotó su cuota (5200 req) durante la sesión; el reset es el epoch `1794580040`. El estado final del run #622 (`43437302899`, check `Live client diagnostics`) quedó sin confirmar por ese límite; re-consultarlo tras el reset.
- **5.10.408 (sin validar aún en CI):**
  - Partícula propia `dawn_mending_bloom` (4 fotogramas 32×32, atlas 32×128, frametime 3, vida 12 ticks) para el pulso de Remiendo del Alba de Palimpsest; generador determinista `tools/dawn_bloom_asset.py`; validación en `tools/validate_resources.py` (19 tests OK).
  - Tag de bloques `specialpickaxes:geology` (datapack allowlist) + `GeologyNames` (reglas sin registries, con unit test `GeologyNamesTest`): inferencia por segmentos completos, exclusión de piezas fabricadas en `crucibleGeology`, `WorldSafety.inert` incluye el tag. GameTest nuevo `geologyTagAllowsDatapackAddedTerrainAndStructuralPiecesStayOut` (batch `rework_each_105`, 151 GameTests en total).
  - Sintaxis Java verificada con `java-parser` (113 archivos, 0 errores); sin JDK/Gradle en el sandbox, la compilación y los GameTests quedan para CI.
- Pendiente tras el reset de la API: confirmar run #622, hacer push de 5.10.408 y vigilar el workflow Forge verification.

### Refuerzo posterior de 5.10.408

- `matrix` y `crucibleGeology` comparten ahora filtros de menas, bedrock, bloques con entidad, fluidos y formas decorativas antes de consultar tags. La nueva rama de `WorldSafety.inert` exige también esos filtros para bloques del tag `geology`.
- El GameTest de terreno se renombró a `geologyRejectsStructuralPiecesAndUnsafeTerrain`: comprueba además cofres, agua, lava, menas y formas vanilla. No afirma probar una recarga de datapack.
- Remiendo del Alba sustituye cuatro chispas WAX_ON por un sol propio: ocho partículas de polvo + un sol por curación efectiva (9 en vez de las 12 originales).
- Validación local: 19 pruebas de recursos OK, `git diff --check` limpio. Compilación, GameTests y revisión visual siguen pendientes de CI; no se ejecutó build local. El último intento de push falló por TLS; no se confirmó publicación ni actualización del PR #2.
- Precisión de evidencia: el hash del JAR citado arriba se leyó de los manifiestos recuperados del run **40756578748**; los manifiestos del artefacto **12849296809** no pudieron descargarse desde el sandbox. No se ha verificado el hash del binario contenido en ese ZIP específico.

### Cobertura del datapack de geología (continuación)

- Añadido GameTest `geologyDatapackTagCannotOverrideSafetyFilters` (batch 106): usa un terreno de prueba sin nombre geológico y comprueba clasificación, efectividad de herramienta y elegibilidad para restauración. Comprueba también el rechazo de cofres, bedrock, agua, lava, menas y losas incluso cuando pertenecen al tag.
- La fixture `pickaxe_compat_test:geology_fixture` se referencia opcionalmente; sus entradas son opcionales y el archivo se excluye explícitamente del JAR. `verify_release.py` rechaza una entrega que incluya esa fixture. No se modifica el tier ni se añaden recetas.
- Validación de recursos: 20 tests OK. El GameTest se añadió pero no se ejecutó: requiere CI. No se afirma compatibilidad certificada con el pack completo ni revisión visual nueva.

## Tanda amplia 5.10.409 — 2026-11-13

- Rama de sesión vigente: `arena/cec54523-special-pickaxes`. No se creó ni se cambió de rama; no se actualizó el head del PR #2. No se ejecutó checkout ni build local ni se accedió a la PC del usuario.
- Menas: `ore_rates`, `ore_rates/*` y `ore_rates_*` se excluyen de clasificación; las familias de veta deben pasar también los filtros de tags de menas (evita familias de almacenamiento). Se admite `ores/x`, y el caché de familias de registro se limpia junto con los demás.
- Nuevos casos JUnit en `ArtifactOresTest` y GameTest `oreDropRateTagsNeverInventDepositsOrJoinVeins` (batch 108). Estasis tiene un GameTest geométrico de sus nueve muestras (batch 107). Se añadieron pero no se ejecutaron aquí.
- Estasis: su respuesta usa `nearbyWardPulse`, por lo que reserva el coste de toda la audiencia antes de emitir. Ocho motas más un cristal sustituyen las diez partículas anteriores.
- Hellspec: el aviso Infernal Bloom usa un flare en lugar de dos llamas por núcleo; las partículas de blancos de la erupción reutilizan `MagmaBrandEffect.brandParticles` con `entityAuraPulse` (audiencia local limitada y presupuesto compartido). No se cambian los filtros de blancos ni el combate.
- Empaquetado: se excluye todo `data/pickaxe_compat_test/**`; `verify_release.py` exige su ausencia y verifica definición/atlas PNG de cada partícula registrada. La referencia opcional a la fixture geológica puede quedar sin resolver en producción sin ser obligatoria.
- Recursos: comprobación general de registros, renderers, atlas, índices de fotogramas y transparencia (RGBA o paleta PNG transparente). El atlas de Worldloom usa paleta con tRNS, que es válido: no se convirtió ni se alteró.
- CI: `verify_gametests.py` ahora es importable y rechaza cero tests, resúmenes parciales o inconsistentes y fallos required. Se añadieron siete pruebas unitarias del verificador y dos de regeneración/estructura del sprite del Alba. El workflow ejecuta estas pruebas Python antes del build.
- Guía nueva `docs/mining-rework/DATAPACK-INTEGRATION.md`: ejemplo 1.20.1, contrato del tag y límites de seguridad, diferenciando terreno y rotura manual.
- GitHub: nuevo intento de push falló por TLS (`gnutls_handshake`). La publicación, el estado final del run #622 y la instalación siguen sin confirmar. No se reutiliza aprobación visual antigua para esta versión.

### Comprobaciones al cerrar la tanda

- `python3 tools/validate_resources.py`: **21 tests OK**.
- `python3 -m unittest discover -s tools -p 'test_*.py'`: **9 tests OK**, incluidos regeneración byte a byte del atlas del Alba y casos negativos del verificador de GameTests.
- Parser Java: **113 archivos, 0 errores de sintaxis**. Esto no sustituye compilación ni resolución de APIs de Forge.
- Se descubren **176 GameTests** entre todas las clases `*GameTests.java`; es un recuento de fuente, no un resultado de ejecución.
- `git diff --check` limpio; los scripts de release y GameTests pasan `py_compile`.
- Consulta de run #622 sigue fallando con EOF de red; estado final desconocido. Compilación, GameTests, verificación del JAR generado y validación visual de 5.10.409 permanecen pendientes de CI.

## Tanda 5.10.410 — 2026-11-13

- Partículas de aliados: las cuatro reservas que usaban `level.getGameTime()` usan ahora `level.getServer().getTickCount()`, igual que las auras. Evita reinicios del presupuesto por alternar relojes de mundo y servidor. Una comprobación de recursos impide reintroducir ese patrón en `ArtifactFeedback`.
- `RelicParticleBudget` inicializa explícitamente su primer tick; acepta también `Long.MIN_VALUE` como primer valor en lugar de confundirlo con un sentinel no inicializado. Cuatro pruebas JUnit nuevas cubren reserva atómica, rechazo sin consumir remanente, wrap/reinicio, capacidad cero y argumentos inválidos. No se ejecutaron aquí.
- Reserva del Alba: un sol propio + tres motas (4 en vez de 9 partículas). Conserva restauración de un punto de comida por segundo y omite pulsos al estar lleno. GameTest nuevo (batch 109).
- Copia de Worldbreaker: `acceptCopy` rechaza removidos, muertos y espectadores antes de consumir secuencia o modificar NBT. GameTest nuevo (batch 110): rechazo en espectador, retorno a supervivencia, repetición y jugador muerto.
- Magma Brand: un daño cancelado no elimina los fotogramas de invulnerabilidad que tenía el blanco; si un listener establece otro valor, se respeta. GameTest nuevo (batch 111) usa un listener de `LivingAttackEvent` que veta el pulso y comprueba salud, fuego y ambas variantes de invulnerabilidad. Daño y cadencia de pulsos permitidos sin cambios.
- Validación ejecutada: 22 tests de recursos OK; 9 tests Python OK; parser Java sobre 114 archivos sin errores de sintaxis; `git diff --check` limpio. Compilación/JUnit/GameTests y visuales siguen sin ejecutar, pendientes de CI. No hubo build local.
- Nuevo intento de push volvió a fallar por TLS. Se mantiene la rama `arena/cec54523-special-pickaxes`; no se ha actualizado el PR #2 ni se ha instalado ningún JAR.

## Auditoría amplia 5.10.411 — 2026-11-13

### Red y CI recuperados

- El push de todos los commits anteriores hasta `a347b14` a la rama vigente `arena/cec54523-special-pickaxes` tuvo éxito. No se creó otra rama de trabajo; el remoto no tenía aún esta rama de sesión.
- Run #622 (`43437302899`) confirmado **completed / failure**: falló el paso `Real client gallery and normal and SHIFT tooltip captures`. No se consultaron logs externos inaccesibles ni se infirió la causa exacta.
- CI nuevo de 5.10.410: https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43443258552. Se confirmó éxito de `./gradlew build test`, `verify_release.py`, validadores Python y escaneo de secretos. Al consultar seguía ejecutando la fase de GameTests. Esto valida build/JUnit de **5.10.410**, no de los cambios posteriores ni una publicación aprobada.

### Cambios de 5.10.411

- `NearestCandidates`: colector genérico top-k, memoria O(k), sin duplicados y snapshots inmutables. `EntitySelection` lo usa en consultas del nivel y listas combinadas; ordena también cuando hay menos candidatos que el límite, con desempate por ID de entidad. Se revisaron consumidores: no mutan la lista devuelta. Siete pruebas JUnit nuevas cubren selección aleatoria contra referencia, duplicados, límites, snapshots y desempate.
- `DomainFields`: omite el segundo escaneo si JEG hereda de `Projectile`; combina con el colector común. `ProjectileScanBudget` reparte exactamente el total sin redondear dos consultas de un solo candidato sobre un límite de uno. Tres pruebas JUnit nuevas cubren tipos independientes, valores impares, cero y extremos. No se ejecutó el pack con JEG.
- `ArtifactState`: valida tipos `TAG_COMPOUND` antes de acceder al root y estado de cada reliquia; evita tags vacíos desconectados cuando un mod o save antiguo guarda otro tipo. Las anclas exigen posición long y tiempo válido, rechazan tiempos futuros y se limpian al iniciar otra selección tras retroceder el reloj. Dos GameTests nuevos (batches 112/113).
- Manual: `ManualScrollModel` no desborda con contenido negativo o coordenadas de arrastre extremas; `ManualScreenLayout` suma offsets con long y normaliza dimensiones negativas. Cinco casos JUnit nuevos. `RelicKeys` usa latch físico también para el manual y descarta clicks repetidos; dos casos JUnit adicionales de latch al sostener/reconectar.
- `WorldSafety`: la colocación exige la misma referencia de herramienta desde el inicio. Transmutación e intercambio revalidan mano, herramienta y barreras después de callbacks de permiso, antes de escrituras tentativas. GameTest nuevo (batch 114): un listener cambia la mano; no debe haber escrituras ni eventos de colocación; incluye herramienta separada en creativo.
- Sin cambios de potencia, tier o recetas; sin build local, checkout o control de la PC.

## Sesión sostenida — 5.10.412, 2026-11-13

- 5.10.411 (`43443796300`) terminó **failure**, pero **sí pasó** la fase completa de cliente: 32 escenas, 64 tooltips normal/SHIFT, controles, `SUPREME_REMOVED_BLOCKS_9341` y finalización. Falló después en `ci_evidence.py`: quedaron 116/222 checks de evidencia. No se dispone del traceback del exportador; no se afirma una causa concreta del fallo HTTP. Aprobación de publicación omitida por fuente distinta de la revisión visual.
- 5.10.410 (`43443258552`) falló en el escaneo final del cliente. Se recuperó el ZIP de evidencia por Checks y se verificó con el nuevo extractor en `/tmp/sp-ci410`. El log muestra `AuthenticationUnavailableException` al consultar propiedades del perfil, con HTML recibido donde esperaba JSON; no se suprimió ni se marcó verde el run. Los logs de servidor/GameTests pasan también el nuevo scanner. Se inspeccionaron dos capturas (Hellspec y final de World Shatter), sin aprobar toda la revisión visual.
- Iridium: no reasigna marcas a otro portador ni ignora el propietario vanilla. Activo y pasivo consultan `AbilityUseEvent` en cada drop y revalidan propiedad/alcance/dimensión después del callback. Se rechazan radios no finitos o negativos. Tres GameTests nuevos cubren propiedad, veto de claims y cambio de propietario desde callbacks.
- El límite privado de esquirlas de Iridium usa `RelicParticleBudget(32)` con tick del servidor; tanto marcas como estelas consumen ese presupuesto y el de auras. No cambia el número de drops atraídos ni su velocidad.
- Observaciones de minería: ventana transitoria medida con tick del servidor, soportando wrap de int; tres casos JUnit nuevos. No cambia la cadencia ni el contenido de memorias persistentes.
- Ícaro: el presupuesto de finalización incluye sus dos acentos adicionales (18, no 16); GameTest nuevo.
- Worldloom: no acepta un objetivo de otra dimensión; invalida bindings al retroceder el reloj. Revalida permisos en cada captura/pin, no solo cada cinco ticks. GameTest asíncrono nuevo introduce un veto entre los antiguos ticks de comprobación.
- Estasis: brillo móvil cada 4/8/16 ticks según calidad, y un solo acento en mínima; el contorno del volumen no se modifica. Tres casos JUnit de cadencia nuevos.
- Logs: scanner importable, detecta nombres completos de excepciones y errores Java (antes `Exception` aislado no cubría `NullPointerException`). Seis pruebas Python. No hay allowlist de autenticación: el gate sigue siendo estricto.
- Evidencia: validación de ZIP y manifiesto antes de escribir archivos, tamaños totales, hashes, rutas, duplicados y symlinks. Ocho pruebas Python. El exportador Checks conserva todo el texto, reduce solo imágenes, escribe `evidence-export.json` con omisiones y mantiene las capturas completas en Actions. ZIP reproducible; techo comprimido 2 MB (máximo 56 chunks), pausas entre publicaciones y reintentos acotados. Cinco pruebas Python adicionales. Sobre evidencia real de 5.10.410 produce 1.994.828 bytes sin truncar logs.
- No se compila localmente ni se controla la PC; se mantiene la rama de sesión y cero recetas/tier OP.

## Continuación de auditoría — 5.10.413

- Se detectó que las marcas booleanas de ownership y flags ambient/visible/icon no bastaban para preservar reemplazos externos más largos o de distinta potencia con los mismos flags. Ahora cada concesión exitosa registra también amplificador y duración máxima; ownership se invalida si cambian. Marcas antiguas sin fingerprint se abandonan de forma conservadora sin borrar efectos.
- Un veto de `addEffect` ya no crea ownership de una pasiva que nunca se concedió; una mejora de combate vetada no devuelve éxito salvo que el efecto actual ya satisfaga lo pedido.
- Tres GameTests añadidos (120–122), pendientes de ejecución: reemplazos ambient largos de renovación/visión nocturna, absorción de Estasis y reemplazo de combate de mayor potencia. Esta protección no distingue un reemplazo externo exactamente idéntico al efecto propio.
- Validación local: 22 pruebas de recursos, 28 unittest de herramientas, parser de 123 archivos Java y diff-check correctos. No compilación ni ejecución Java local.
- El CI 5.10.412 seguía en curso al comenzar estos cambios; no se hereda su resultado para 5.10.413.
