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

## Auditoría continuada — 5.10.414

- Run 5.10.412 `43447050583`: completed/failure. Build/JAR y 60 JUnit (0 errores/fallos/skips) comprobados en el bundle recuperado mediante Checks, ZIP SHA256 `fecec0e2036aa9329d74f65732ef84be1b3b487390d18aa57ca6364e944a61fa`. Exportador finalizó en cuatro chunks; evidencia completa de esta fase en `/tmp/sp-ci412`. Un GameTest requerido falló: `iridiummarkcannotstealanotherwearersdrops`, aserción `vanilla ownership fixture loaded`; cliente y servidor empaquetado omitidos. No publicación aprobada.
- La investigación mostró un error real, no solo un fixture: `ItemEntity.getOwner()` es la entidad lanzadora, no el UUID `Owner` que reserva la recogida. `DropOwnership` consulta el contrato público `addAdditionalSaveData` sin reflexión ni cambiar/recargar la entidad. Iridium y Eventide usan ahora la reserva de recogida. Coste acotado por los límites de candidatos existentes; no se añade escaneo global.
- Eventide: permiso de claim por cada drop del colapso, revalidación de entidad, posición, dimensión, propiedad y campo activo tras el callback; cuenta solo drops realmente movidos. Conserva fuerza/radio/cadencia.
- WorldSafety.allowed vuelve a comprobar las precondiciones tras el evento (incluyendo retirada, distancia, permisos de construcción y misma dimensión), sin repostearlo. Selección no acepta herramienta separada ni añade esquinas a un selector cancelado en el callback; invalida selecciones tras retroceso del reloj.
- Worldloom: comprueba objetivo antes y después del callback y abandona aprobación sobre una posición anterior. Iteración snapshot acotada para que callbacks que cancelan bindings no causen ConcurrentModificationException ni resuciten raíces.
- Campos: iteración snapshot y comprobación de identidad tras callbacks; las operaciones no deben continuar en un campo ya cancelado o sustituido. Se revalidan proyectiles/objetivos antes de modificar movimiento.
- Siete GameTests añadidos (123–129), pendientes de CI: teleport/revocación de permisos sin cancelación, cancelación del selector, movimiento del objetivo al bind, cancelación reentrante de varias raíces, distinción Thrower/Owner, colapso con claims/propiedad y cancelación reentrante de campos.
- Validación local: 22 pruebas recursos + 28 Python, parser 124 Java, diff-check y scan de secretos correctos. No build ni ejecución Java local, no checkout ni instalación Windows. Run 5.10.413 `43447481028` seguía en curso en la última consulta.

## Cierre de atracción de drops — 5.10.415

- Auditoría de todos los `getOwner()` comparados con UUID encontró el mismo error en el alternativo de Eventide (`magnetDrops`). Sustituido por `DropOwnership`, con permiso por drop y revalidación de propiedad/posición/dimensión/herramienta después del callback. Se conserva la fuerza .65, radio 16 y cap 64.
- Un GameTest añadido (130), pendiente de ejecución: reserva vanilla ajena, claim vetado, ownership cambiado durante callback; no gastar cooldown cuando no se mueve nada y permitir de nuevo el tirón fuerte cuando se levanta el claim. Fuente total: 198 GameTests.
- CI de 5.10.413 `43447481028`: failure, bundle recuperado/verificado en `/tmp/sp-ci413`. 60 JUnit sin errores; 190 GameTests completos, único fallo en el mismo fixture `vanilla ownership fixture loaded`. Esto confirma que no hubo otros GameTests fallidos en esa ejecución, no una aprobación global.
- CI de 5.10.414 `43448248860` (fuente `1bbd645`): build/JUnit, estructura/hash JAR, escaneo JAR y paso completo de 197 GameTests success según API. El paso de servidor empaquetado estaba in_progress. No aprobación del cliente/global ni instalación. Monitor de proceso `ci-de-special-pickaxes-5-10-414-2267620e`.
- Validación local tras el último cambio: 22 recursos y 28 Python OK, parser 124 fuentes Java sin errores, diff-check limpio. Java solo se compila/prueba en Actions.

## Estasis y liberación — 5.10.416

- La auditoría encontró que el mantenimiento de entidades ya congeladas (`holdFrozen`, ticks sin escaneo y `tickPulses`) no revalidaba claims. Ahora cada pin comprueba estado vivo/no retirado, alianza, hostilidad del proyectil, dimensión, misma herramienta y permiso en posición actual y destino capturado; revalida tras callbacks. El camino de escaneo también comprueba el destino de entidades ya capturadas.
- Los pulsos se iteran mediante snapshots acotados de sus objetivos; se verifica la identidad del pulso después de callbacks. Cancelarlo desde el evento no causa ConcurrentModificationException ni vuelve a fijar entidades liberadas.
- Liberación común: restaura la velocidad original acotada únicamente si la entidad sigue en la dimensión capturada; marca impulso. Ya no teletransporta al liberar. Esto evita devolver a un objetivo a un ancla ahora protegida y evita modificar movimiento después de una transferencia de dimensión.
- La captura inicial del pulso revalida herramienta/objetivo/posición/dimensión tras el permiso y no roba membership de otra congelación creada durante el callback.
- Cuatro GameTests nuevos (131–134), pendientes de CI: veto de claim en mantenimiento de tick impar; veto en el punto de retorno del pulso; cancelación reentrante con dos objetivos; cambio de herramienta durante captura. Total en fuente 202.
- No cambios de potencia, radio, duración, tier ni recetas. Sin build local ni instalación Windows. Validación local final se registra tras ejecutarse, no confundir parser con compilación.
- Última consulta: 5.10.414 y 5.10.415 seguían en capturas reales de cliente, sin fallo registrado hasta ese paso; esto no confirma éxito del cliente ni revisión visual/global.
- Validación local ejecutada para 5.10.416: 22 pruebas de recursos, 28 unittest Python, parser 124 archivos Java, escaneo de secretos (252 entradas) y diff-check correctos. No pruebas Java locales.

### Resultado recuperado de 5.10.414

- `43448248860` completed/success. Confirmados servidor empaquetado, escaneo de logs, cliente real, exportador Checks y upload de Actions. `Publish verified release only` skipped: no aprobación visual aplicable. No confundir gate no bloqueante de aprobación con aprobación real.
- Evidencia recuperada/verificada en `/tmp/sp-ci414`; incluye `GRAPHICAL_CLIENT_GALLERY_OK; UX_ARTIFACTS=32; NORMAL_AND_SHIFT=64`, controles teclado-servidor y `SUPREME_REMOVED_BLOCKS_9341` / `SUPREME_COMPLETE`.
- Manifiesto de esa fuente: `special-pickaxes-1.20.1-5.10.414.jar`, 525972 bytes, SHA256 `690976b34add7ec8775ffb18565822219370928f40fe0ff1062d2155f09ef8a1`. Es metadato comprobado del bundle, no descarga local/instalación del JAR ni autorización para publicación.
- Inspeccionadas únicamente tres capturas: `ux-shift-interregnum-domain.jpg`, `ux-shift-iridium-orefall.jpg`, `supreme-complete.jpg`. Tool HUD/iconos visibles; en el resultado supremo aparece un mensaje de acción «Operación cancelada», aunque el harness informa 9341 bloques y finalización. No se atribuye causa ni se aprueba visualmente la galería completa. Algunas partículas de primer plano ocupan bastante pantalla; requiere evaluación visual más amplia antes de ajustar tamaños.
- 5.10.416 sigue pendiente de su propio CI `43450387194`, fuente `3e4eecb7609fe6d352e69908cfb3291b92931fdc`. Monitor `ci-de-special-pickaxes-5-10-416-d1dbf4dc`.

### Avance confirmado posterior

- 5.10.415 `43449061170` completed/success según API. No se inspeccionó su galería ni se instaló su binario.
- 5.10.416 fuente `3e4eecb`, run `43450387194`: pasos 10–17 success (build/JUnit, verificación JAR, seguridad JAR, 202 GameTests completos, servidor empaquetado, scanner de logs y dependencias cliente). Cliente real aún in_progress; sin resultado global conocido. Estas pruebas sí corresponden al cambio de Estasis, no se heredan de 414.

## Cola de excavación — 5.10.417

- Auditoría detectó que cancelar o sustituir un job durante `WorkStep.apply` no impedía ejecutar los siguientes pasos del job antiguo en ese turno. La finalización antigua también podía borrar el sustituto indexado por el mismo UUID. Se comprueba identidad antes/después de callbacks y antes de feedback/finalización/reencolado; pausa desde callback detiene el turno.
- Guarda `ticking` separada de `running`: impide reentrada durante preparación, consultas y progreso, no solo durante apply. `clear()` no desactiva prematuramente el marcador de mutación nativa; el finally del apply lo restaura.
- Cambio de dimensión, retirada, muerte o cambio de herramienta después de un paso aborta el trabajo antiguo antes de otra operación. La protección de caída Core Drill itera snapshot y verifica identidad para tolerar callbacks de efectos que la eliminan.
- Cinco GameTests añadidos (135–139), pendientes de CI: cancelación en rotura nativa, sustitución sin borrado/ejecución vieja, clear+tick recursivo, pausa durante apply y reentrada desde loaded de programa regional. Total 207 en fuente.
- Validación local ejecutada: 22 recursos + 28 Python correctos, parser 124 Java sin errores, diff-check y scan de secretos correctos. No compilación/pruebas Java locales ni instalación Windows. No cambio de tier, fuerza, presupuestos ni recetas.
- Run 5.10.416 `43450387194` aún en capturas reales de cliente al consultar; build/JUnit, 202 GameTests, servidor/logs ya confirmados, resultado global pendiente.

## Hellspec — 5.10.418

- Se detectó que Magma Brand conservaba su UUID caster cuando un efecto externo sustituía la instancia; el pulso podía dañarla como propia o borrarla por completo al perder permisos el caster. Ahora exige el fingerprint combat lease antes y después del callback de permiso, limpia solo atribución obsoleta y usa cleanup conservador sin `removeEffect` incondicional.
- Otro portador no puede reatribuir/mejorar una marca activa de otro caster. La comprobación de daño revalida estado vivo, dimensión, PvP/alianza y posición tras el permiso.
- Erupción: selección geométrica sin eventos en el predicado; cada objetivo pasa permiso y revalidación completa justo antes de daño/ignición/lanzamiento. Si todos dejan de ser válidos devuelve false; partículas y mensaje usan únicamente la lista afectada. Preserva daño 5, radio 5, cap 8, velocidad/ignición y marca de 80 ticks.
- Cuatro GameTests añadidos (140–143), pendientes de CI: preservar reemplazo externo sin daño, propiedad del caster original, reemplazo durante callback, objetivo movido durante permiso sin efectos residuales. Total de fuente 211 GameTests.
- Validación local ejecutada: 22 recursos + 28 Python, parser 124 Java, diff-check y secret scan (252 entradas) correctos; sin build/pruebas Java locales. Sin cambios de recetas, potencia ni tier, sin acceso/instalación Windows.
- Última consulta: 5.10.417 `43452383160` en build; 5.10.416 `43450387194` en exportación de evidencia tras pasar cliente. No resultado global confirmado aún para esas fuentes.

### Resultados CI confirmados al cerrar la tanda

- 5.10.416 `43450387194` completed/success. Bundle Checks recuperado/verificado en `/tmp/sp-ci416`; cliente confirma 32 escenas, 64 tooltips, controles teclado-servidor y World Shatter con 9341 bloques. Sin revisión visual completa ni instalación. Manifiesto: JAR `special-pickaxes-1.20.1-5.10.416.jar`, 526753 bytes, SHA256 `af48c5cfe3921a8e1983195c96031b32c6658cc2fb7bde1703fbc40a8d65bd77`, fuente `3e4eecb7609fe6d352e69908cfb3291b92931fdc`.
- 5.10.417 `43452383160`, fuente `5ec728889e25f843285f5caad9198da60ec553e2`: build/JUnit, JAR, 207 GameTests, servidor empaquetado y logs success; cliente real aún en curso. Monitor `ci-de-special-pickaxes-5-10-417-6fe5c741`.
- 5.10.418 `43452644898`, fuente `cb2c5bec55b43b66ce69903cacf132dc02acba54`: build/JUnit, JAR, 211 GameTests, servidor empaquetado y logs success; cliente real aún en curso. Monitor `ci-de-special-pickaxes-5-10-418-58a8f494`.
- El siguiente commit solo sincroniza estos resultados documentales y usa `[skip ci]` para no duplicar todos los runs por un cambio de texto. No omite ninguna validación de código fuente nueva.

## Desplazamientos — 5.10.419

- `WorldSafety.move` calculaba delta una vez pero sumaba cada punto a la posición viva, que un listener podía cambiar; además comprobaba colisión antes del permiso sin recomprobar tras el callback. Ahora usa origen/nivel/herramienta capturados, aborta ante cambios de posición/dimensión/mano/montura/sueño y revalida cuerpo libre después de cada permiso. No sobrescribe la posición elegida por un mod externo. Mantiene distancia máxima 8 e inspección cada .2 bloques.
- `freeBody` rechaza NaN e infinitos antes de formar cajas o posiciones de bloque.
- `DomainFields.relocate` verifica identidad del campo, dimensión y herramienta antes/después del permiso; no emite una recolocación exitosa de una referencia que el listener canceló/sustituyó.
- Cuatro GameTests añadidos (144–147), pendientes de CI: cambio de posición por callback, pared creada tras comprobación inicial de colisión, coordenadas no finitas y dominio sustituido durante recolocación. Fuente total 215 GameTests.
- Validación local: 22 recursos + 28 Python, parser 124 Java, diff-check y secret scan correctos. Sin build/pruebas Java locales, sin instalación Windows. No cambios de potencia, tier ni recetas.

### Evidencia recuperada de las tandas anteriores

- 5.10.417 `43452383160` completed/failure en cliente. Bundle verificado `/tmp/sp-ci417`: `AuthenticationUnavailableException` al consultar autenticación; respuesta HTML seguida de Gson `Expected BEGIN_OBJECT but was STRING`. El scanner provoca el fallo; no se añade allowlist ni se declara verde. Sus 207 GameTests/servidor habían pasado.
- 5.10.418 `43452644898` completed/success. Bundle verificado `/tmp/sp-ci418` confirma 32 escenas/64 tooltips, controles teclado-servidor y World Shatter real (9341 bloques). Sin inspección visual de la galería de esa fuente ni instalación. JAR manifest: `special-pickaxes-1.20.1-5.10.418.jar`, 527455 bytes, SHA256 `eb25948d104c0f8dc031080895130de61df4e59a77c1a009d281042c26eadbbc`, fuente `cb2c5bec55b43b66ce69903cacf132dc02acba54`.

## Cooldowns persistentes — 5.10.420

- Se detectó que Dawn Mending/Heartwood/comida Worldloom/Null Veil comparaban únicamente readyAt>now: un plazo malformado o procedente de otro reloj/retroceso podía bloquear la pasiva indefinidamente. `PersistedCooldown.active` acepta solo el intervalo positivo dentro de la duración original, rechazando plazos demasiado futuros y overflow.
- Dawn/Heartwood usan ahora tiempo del overworld del servidor, igual que ArtifactState, en vez de tiempo de dimensión local. Cadencias normales conservadas: 40/20 ticks de curación, 100 comida y 160 limpieza. No se modifican las cantidades ni tier/recetas.
- Cuatro JUnit nuevos (intervalo completo, plazo demasiado futuro, extremos long y duración inválida). Tres GameTests nuevos (148–150) restauran cada pasiva con plazo Long.MAX_VALUE y verifican que un segundo pulso en el mismo tick sigue bloqueado. Total fuente: 218 GameTests, pendiente de CI para este commit.
- Validación local: 22 recursos + 28 Python, parser 126 Java, diff-check y scan de secretos correctos. Java no compilado ni ejecutado localmente; sin instalación Windows.
