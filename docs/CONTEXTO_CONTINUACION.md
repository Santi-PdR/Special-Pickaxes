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

### Gates confirmados después de publicar 5.10.420

- 5.10.419 `43537824437`, fuente `4e6e60bdd09ef5af61e8eeb578fc054cb38da028`: pasos 10–17 success: build/JUnit, verificación y seguridad JAR, 215 GameTests completos, servidor empaquetado, logs y dependencias cliente. Capturas reales en curso; resultado global desconocido. Monitor `ci-de-special-pickaxes-5-10-419-c86af63b`.
- 5.10.420 `43537968640`, fuente `0bade7a25312c5ea8d58500a65d3439a37d7584c`: mismos pasos 10–17 success con 218 GameTests; capturas reales en curso, sin resultado global confirmado. Monitor `ci-de-special-pickaxes-5-10-420-032c3f8d`.
- Secret scan final: 254 entradas versionadas, sin coincidencias. Working tree limpio antes de esta actualización documental. El siguiente commit documental usa `[skip ci]` sin omitir validación de código nuevo.

## Tanda amplia — 5.10.421

- Combate: Palimpsest, Choir y Null Ward separan selección espacial y permisos. Revalidan posición, dimensión, tool y elegibilidad después del callback justo antes de modificar movimiento/descartar. Choir vuelve a comprobar contacto con suelo y usa grantCombatEffect para no renovar/reclamar resonancias externas. Null Ward revalida owner/hostilidad y reserva sus ocho motas por proyectil en un presupuesto privado de 192/tick y el global de auras; el descarte no depende del presupuesto visual.
- Worldloom: selección sin permiso duplicado; el bind ya valida permiso. Se revalida hostilidad/rango/tool antes de bind para candidatos movidos por otros callbacks. No cambia windup/duración/fuerza/radios/caps.
- ArtifactSkills: valida herramienta exacta/tipo/playable/estado de jugador; set transitorio por UUID con finally impide llamadas recursivas mientras aún no hay cooldown. Usa PersistedCooldown para abandonar plazos inválidos conservando intervalo normal y no cobra al no encontrar objetivos.
- Memorias: RecordedAge rechaza futuro, reloj negativo y tipo de timestamp incorrecto; exige pos long/dim/block string válidos. La fecha epoch inválida no cae silenciosamente al reloj legacy. Prune elimina registros corruptos y snapshots recuperan capacidad expirada solo al llenarse, como máximo un barrido por tick; admisiones ordinarias siguen O(1).
- Animación: ParticleEnvelope compartido con smoothstep acotado; Dawn, Exodium, Hellspec, Ícaro, Iridium, Estasis y raíces Worldloom empiezan con alpha 0 para no mostrar un frame inicial opaco. Se mantienen sprites, tamaños, vida y ventanas de fade. No hay aprobación visual de esta fuente ni sustitución de texturas basada en suposiciones.
- Nueve GameTests añadidos (151–159): objetivos movidos, proyectil vuelto aliado, resonancia externa, reentrada/cooldown, tools inválidas, cambio de suelo, evento único de bind, memorias corruptas/futuras y capacidad caducada. Ocho JUnit nuevos: cuatro de RecordedAge y cuatro de ParticleEnvelope. Fuente total 227 GameTests; Java pendiente del CI propio.
- Validación local: 22 recursos y 28 herramientas Python correctos; parser 130 fuentes Java sin errores; diff-check limpio. Sin build Java local ni acceso/instalación Windows. No recetas/tier/potencia alterados.

## Guardas de orquestación — 5.10.422

- La auditoría posterior detectó que ArtifactActions.useAlternate consultaba permisos antes de entrar en ArtifactSkills (por tanto fuera de su guarda) y repetía la comparación ready>now que impedía recuperar el cooldown corrupto en el camino real. Se añade guarda común ACTIVATING antes del primer permiso, con finally; la comprobación usa PersistedCooldown con duración configurada.
- Activación primaria rechaza herramientas separadas y revalida mano después del permiso inicial, antes de crear dominios o iniciar colas. El alternativo valida además el tipo exacto solicitado. El primario también recupera plazos imposibles, respetando cooldown normal/secondary de 10 ticks.
- Cinco GameTests nuevos (160–164): reentrada en primer permiso, cambio de mano al crear dominio sin coste/cooldown, detached/mismatched antes de eventos, Worldbreaker copiando Eventide sin perder potencia y recuperación de deadline del primario. Fuente total 232 GameTests. Junto con 421: catorce GameTests y ocho JUnit añadidos, Java aún pendiente de CI propio.
- Validación local: 22 recursos + 28 Python, parser 130 Java, diff-check y scan de secretos (258 entradas antes del commit) correctos. No build local ni acceso Windows. Sin cambios a tier, fuerza, daño, radios ni recetas.

## Investigación e aislamiento de GameTests — 5.10.423

- 421/run `43539440158` completed/failure. Bundle Checks recuperado/verificado en `/tmp/sp-ci421`. Build y 72 JUnit (0 failures/errors) correctos, incluidos los ocho nuevos de RecordedAge/ParticleEnvelope. Único GameTest fallido: `eventidealternatecannotbypassclaimsorpickupownership`, aserción no-target. No aprobación cliente/global.
- 422/run `43539593937` completed/failure. Bundle verificado `/tmp/sp-ci422`. 72 JUnit correctos; 232 GameTests completos con un fallo distinto: `worldloomalternatepostsonlytheactualbindpermission`, aserción conteo. El caso Eventide anterior pasó en esta ejecución. No se atribuye un bug de gameplay ni se afirma causa exacta sin los conteos de candidatos.
- Se identificó una debilidad real de fixtures: pruebas de radio asumían que solo estaban sus propios objetos en el mundo; sus claim listeners aceptaban entidades de otros lotes y el contador Worldloom contaba cualquier objetivo del área. Ahora se vetan objetivos externos dentro de esos fixtures; el contador de bind solo observa la posición de su propio objetivo. Los casos de movimiento, dueño de proyectil, reentrada y contacto con suelo se aíslan igual para evitar la misma suposición.
- Se conservan todas las aserciones sobre los objetos propios, fuerza de reintento, no-target/no-cooldown y conteo único del bind. La prueba Eventide registra `EVENTIDE_FIXTURE_FOREIGN_DROP_VETOES_N` para verificar si aparecen candidatos ajenos. Esto es aislamiento de prueba, no allowlist de errores ni relajación del gate.
- Sin cambios de gameplay adicionales a 421/422; source total sigue 232 GameTests. Validación Java de 423 pendiente de su run. Parser 130 fuentes y diff-check correctos; no build local.

### Cierre verificado de la tanda amplia

- 5.10.423, fuente `72b7b8b432913b3d78ba906920b5c7e88edce2ae`, run `43540141390` **completed/success**. Build/JUnit/JAR, GameTests, servidor empaquetado, scanner, cliente y exportador completos. Publicación binaria skipped: no aprobación visual completa de esa fuente.
- Bundle Checks descargado y verificado en `/tmp/sp-ci423` (54 chunks). JUnit XML: **72 tests, 0 failures, 0 errors, 0 skipped**. Log: **All 232 required tests passed**. Esto sí valida el código de 421/422 conservado en 423; no cambia los resultados fallidos históricos.
- Cliente: `GRAPHICAL_CLIENT_GALLERY_OK; UX_ARTIFACTS=32; NORMAL_AND_SHIFT=64`, controles teclado-servidor OK y `SUPREME_REMOVED_BLOCKS_9341` / finalización real.
- Contador `EVENTIDE_FIXTURE_FOREIGN_DROP_VETOES_0`: no hubo candidatos ajenos vetados en esta ejecución. El aislamiento y todos los asserts pasaron, pero este cero NO prueba causalmente la hipótesis de contaminación del fallo 421. Se mantiene la causa exacta de aquel fallo como no confirmada; no se borran sus resultados.
- Tres capturas inspeccionadas de esta fuente: `ux-shift-palimpsest-vein.jpg`, `ux-icarus-forward.jpg`, `third-person.jpg`. HUD y herramientas visibles; no aprobación visual completa ni prueba temporal de interpolación a partir de una imagen fija. El toast de recetas de la escena Palimpsest es del cliente; la verificación del JAR sigue exigiendo cero recursos recipes del mod.
- Manifiesto: `special-pickaxes-1.20.1-5.10.423.jar`, 531432 bytes, SHA256 `2ddbc93d6ace512e8c200023af1f2a4f858b9d8e571ed746707ea55640310ad6`. Esto es comprobación de metadatos de evidencia, no descarga local del JAR ni instalación. Artifact Actions `forge-validation` ID `14092098378` (49.512.524 bytes, no expirado al consultar), con capturas completas.
- Además se recuperaron/verificaron los bundles de 419/420 en `/tmp/sp-ci419` y `/tmp/sp-ci420`; ambos runs completed/success con cliente/controles/World Shatter. No se hereda su revisión visual para 423.
- Acumulado de esta tanda desde `bf22ebc`: 20 archivos, 14 GameTests y 8 JUnit añadidos, combate/entry points/memorias/presupuestos y siete fades animados. No compilación local, checkout ni control de PC. Se mantiene rama de sesión, tier OP, herramienta correcta y cero recetas.
- Este cierre documental usa `[skip ci]` para no duplicar la ejecución por cambios de texto; el código validado permanece idéntico al commit fuente citado.

## Minería observada, calor y resonancia — 5.10.424

- Al iniciar, la rama local apuntaba al base `28b8ebf` con toda la fuente 423 como dirty/untracked. Se descargó la referencia remota sin checkout; se compararon hashes de los 356 archivos del commit remoto `e3cd693`: 0 ausentes/diferentes. Se sincronizaron únicamente branch ref e índice (`update-ref` + `read-tree`), sin cambiar de rama, sobrescribir archivos ni reset/checkout. El árbol quedó limpio antes de editar. El contenido previo publicado se conserva.
- Hellspec: nearbyHeat valida caché por origen/dimensión/tick del servidor y tipos NBT; comprueba límites de altura y hasChunkAt antes de getBlockState. Mantiene volumen 5×3×5 y multiplicador 1.35. Moverse en el mismo tick no hereda un resultado térmico anterior.
- Strata: PendingBreak registra server tick y usa ObservationWindow (incluido wrap int) en lugar del reloj mutable de dimensión. Set acotado junto a la cola impide duplicar una misma observación; flush/forget/clear liberan sus claves. Captura exige objetivo cargado y jugador válido.
- Rachas: contadores Strata/Iridium se acotan antes de sumar para evitar overflow. RecordedAge invalida edades negativas/futuras; Iridium rechaza expiries demasiado futuros, manteniendo el límite de tres cargas y duración 80. Los flashes Strata/Estasis y comida peak Iridium usan PersistedCooldown para recuperar plazos imposibles; no cambian intervalos 6/5/40 ni cantidades.
- Iridium: sparkle, handoff, órbita y meal pulse reservan el presupuesto global de aura. Costes exactos: ore 3/4/5/7 según carga; handoff 8 o 9 contando ambos viewers y un único acento del destinatario. Gameplay (carga/comida) se realiza independientemente de poder emitir partículas. Se acota amplifier negativo en la geometría visual.
- Ocho GameTests nuevos (165–172): movimiento y dimensión de caché, no carga de chunks, dedup/cleanup, overflow/edad de racha, flash Estasis, carga Iridium corrupta y comida peak. Tres JUnit nuevos prueban costes visuales/extremos. Fuente total 240 GameTests, pendientes de CI propio.
- Validación local ejecutada: 22 recursos + 28 Python, parser 131 fuentes Java, diff-check y secret scan correctos. No build/pruebas Java locales; no acceso o instalación Windows. Tier OP, radios, fuerza y cero recetas conservados.

### Cierre verificado 424

- Fuente `328077af660e9873578d100d5417924af0314cb8`, run `43543547256`: completed/success. El watcher terminó 0 y se confirmó por API.
- Bundle Checks de 55 fragmentos recuperado con verificación SHA256/archivo en `/tmp/sp-ci424`. XML: 75 JUnit, 0 failures/errors/skipped. Log GameTest: All 240 required tests passed. Incluye las ocho regresiones nuevas y los tres JUnit nuevos.
- Build/JAR, servidor empaquetado y validación de logs success. Cliente: GRAPHICAL_CLIENT_GALLERY_OK, UX_ARTIFACTS=32, NORMAL_AND_SHIFT=64, KEYBOARD_TO_SERVER_ACTIVATE_CANCEL_OK y SUPREME_REMOVED_BLOCKS_9341/SUPREME_COMPLETE.
- Publish verified release only skipped. Ninguna captura 424 revisada visualmente en esta tanda: no aprobación visual completa ni animación temporal certificada. No instalación Windows ni control de UI/PC, no build local.
- Artifact `forge-validation` id 14092744566, 46693852 bytes, no expired. RELEASE.json verificado: special-pickaxes-1.20.1-5.10.424.jar, 532550 bytes, SHA256 `1c32c52fa672b9e3ef72c1d8f70669f43afefe64e7357c79ed5a22f5bcb8615c`. Esto verifica el manifiesto, no una instalación/descarga del JAR.
- Próxima auditoría identificada, todavía sin modificar: fallback de aura no-jugador en SearingShell/StasisWard/WorldbreakerAegis usa envío directo en lugar de entityAuraPulse; StasisWard.answerHostileHit conserva comparación raw de su reflex deadline. GraviticTether calcula drag con amplifier negativo sin clamp. No presentar esas posibles mejoras como implementadas.

## Tanda 5.10.425 — pendiente de CI

- Implementa la auditoría pendiente indicada al cierre 424: SearingShell/StasisWard/WorldbreakerAegis usan entityAuraPulse también sobre no-jugadores, eliminando fallbacks de broadcast sin presupuesto. Jugadores conservan ruta nearbyWardPulse dentro del helper; mobs audiencia máxima ocho a 24 bloques. Sin cambiar geometría/cadencia ni quitar efectos por presupuesto agotado.
- StasisWard.answerHostileHit usa guard UUID con finally frente a reentrada y excepciones en permisos. Rechaza eventos ajenos/cancelados, daño no finito/no positivo, bearer muerto/removido/espectador y atacantes muertos/removidos/otra dimensión/aliados. Tras callback revalida ward exacta, evento y posición del atacante. Respeta PvP. PersistedCooldown recupera plazos imposibles y conserva 160 ticks/15%/tether 40 ticks amplifier1. Revalida el cooldown después del callback y lo escribe antes de callbacks de aplicación del efecto.
- GraviticTether.horizontalDrag acota amplifier negativo a cero; positivos conservan progresión y piso .35. Nunca acelera por NBT malformado. Vertical intacta.
- Diez GameTests (173–182) añadidos: deadline/throttle, daño inválido/cancelado/ajeno, reentrada, ward removida, atacante movido, cancelación posterior, cleanup tras excepción, arrastre extremo y dos integraciones de aura/budget con y sin audiencia. Fuente total 250. Tres JUnit de niveles ordinarios/extremos, total esperado 78; todavía no afirmar ejecutados.
- Local: recursos 22 y herramientas Python 28 pasaron; parser 132 fuentes Java, diff check y secret scan correctos. Sin build local, instalación Windows ni revisión visual de nueva versión. Tier OP y cero recetas intactos.

### Resultado real 425 y corrección 426

- `a9016ddf351fb28e13d1e9ec0f85043f061b972c`, run 43545570252 completed/failure. Bundle Checks 5 fragmentos recuperado/verificado `/tmp/sp-ci425`. Build/JAR y 78 JUnit pasaron (XML comprobado al cierre). All 250 required tests passed en log GameTest.
- Fallo posterior: validate_runtime_logs detectó ERROR/IllegalStateException emitido por Forge EventBus al ejecutar la excepción intencional del fixture179. No era fallo de una assertion ni del gameplay. Servidor empaquetado/cliente omitidos. Se conserva este historial como failure, no CI verde.
- 426 reemplaza ese fixture por permiso denegado, verifica daño/cooldown intactos y respuesta posterior válida tras quitar listener. Conserva finally del guard de producción; ya no se afirma cobertura de excepción en esa regresión. No se modifica escaneo/allowlists ni se ocultan logs. Sigue habiendo 250 GameTests/78 JUnit esperados; pendientes de CI propio.

### Cierre 426 comprobado

- Fuente `4262a10c219873d49abba6a53509ed352b5a3c45`, run 43546125974 completed/success (watcher exit0 + API). Bundle Checks de 56 fragmentos recuperado/verificado en `/tmp/sp-ci426`.
- XML: 78 JUnit, 0 failures/errors/skipped. GameTest: All 250 required tests passed. JAR/servidor empaquetado/logs success. Volvió a ejecutarse localmente el scanner sobre logs GameTest y cliente recuperados: limpio.
- Cliente: GRAPHICAL_CLIENT_GALLERY_OK; UX_ARTIFACTS=32; NORMAL_AND_SHIFT=64; KEYBOARD_TO_SERVER_ACTIVATE_CANCEL_OK; SUPREME_REMOVED_BLOCKS_9341 y SUPREME_COMPLETE. Publicación binaria skipped.
- Una sola captura inspeccionada: ux-shift-interregnum-domain.jpg, escena/HUD/herramienta visibles con partículas blancas en primer plano. No revisión de toda la galería ni validación temporal de animación/partículas, no aprobación visual completa. Sin instalación Windows ni build local/UI automation.
- Artifact forge-validation 14094209314, 43789979 bytes, no expired. RELEASE.json: special-pickaxes-1.20.1-5.10.426.jar, 532425 bytes, SHA256 `18f357ac5b551b744c626461bfaed64f3647f3fb04ba7f5843c8abfd8ddc32f0`. Manifiesto verificado dentro del bundle, JAR no descargado/instalado.
- Próxima auditoría sin implementar: comparaciones raw de plazos en ArtifactPassives (sharedHeartwoodReady/sharedNullVeilReady/curiosPassiveReady/allyAuraScanDue), GraviticBalance eventideImpactReady, IcarianMomentum LANDING_READY, OreDropHighlight NEXT_LODE_SENSE y StarfallWard starfallCatchReady/NEXT_FLASH. Revisar semántica y callbacks caso a caso, no reemplazar ciegamente. Los fallbacks de aura y GraviticTether pendientes de 424 ya se resolvieron en esta tanda.

## Tanda 5.10.427 — pendiente de CI

- GraviticBalance landingSnare: guard UUID/finally antes de callback; fallDistance finita y bearer válido. Captura efecto/origen/nivel; selector de targets sin WorldSafety callback dentro del predicate. Permisos por objetivo con posición/equipo lógico/hostilidad/radio/PvP revalidados; no snare a un target movido ni desde origen stale. Cooldown 200 recuperable con PersistedCooldown, escrito al primer efecto concedido. Si todos rechazados no consume. Ripple cosmético 15 instancias bajo presupuesto global.
- IcarianMomentum: convertLanding rechaza NaN/infinito, bearer muerto/removido/espectador y recupera deadlines fuera del intervalo40. Conserva fuerza y distancia mínima5.
- Starfall: catch deadline30 y flash5 acotados con helper; geometría/cadencia de recarga usan idéntica semántica, sin aparentar carga infinita/negativa en NBT extremo. Público tryCatchIncoming exige ward/bearer vivo/no espectador/nivel correcto. consume revalida shot/nivel/hostilidad y escribe cooldown antes de discard callback; el hook sólo cambia impact result tras éxito. Flashes de catch (14 por viewer, bearer+8) y guard (8 por viewer) reservan allowance global; catch no depende de emisión visual. Carga NaN/infinita en orbitParticles usa estado ready finito. Aliados en ambas direcciones no se interceptan.
- OreDropHighlight: Lode Sense usa reloj ArtifactState y cooldown recuperable10; owner inválido no escanea. Captura posiciones owner/drop y nivel; permiso sobre posición antigua no autoriza pull tras teleport del drop o owner. Continúa revalidando ownership/radio.
- allyAuraScanDue: helper PersistedCooldown para scan20, compartido por once familias de auras. No se tocaron aún sharedHeartwoodReady/sharedNullVeilReady/curiosPassiveReady (requieren auditoría callback propia, no reemplazo ciego).
- Once GameTests183–193: gravedad invalid/deadline/reentrada/movimiento/ward/origen, Starfall deadline+budget y wearer/nivel inválido, Icarus no finito/deadline, Lode Sense deadline/movimiento, aura Choir deadline. Fuente261; tres JUnit Starfall (recarga normal/extremos/wrap cadence), esperado81. No afirmar ejecutados hasta CI.
- Local: recursos22, Python28, parser133, secret scan y diff-check OK. Sin build local, Windows ni control de PC/UI. Tier OP, radios, potencia y cero recetas conservados.

### Resultado 427 y corrección 428

- Fuente `aa7315888015a7c057606787f0900a38a88c13e6`, run43547927879 completed/failure. Bundle recuperado/verificado `/tmp/sp-ci427`. Build/JAR y 81 JUnit pasados; 260/261 GameTests, las once nuevas regresiones pasaron. Único fallo: exodiumStarfallCatchRechargesAfterThirtyTicks / batch71, primer catch.
- Ese fixture no aplicaba STARFALL_WARD. El public helper ahora requiere bearer protegido (cubierto explícitamente por la nueva regresión189). 428 prepara la ward100 en el fixture71, sin relajar validación ni retirar assertions de recarga29/30. No cambia gameplay427. Nueva versión aún pendiente de CI propio; no certificar servidor/cliente de427, omitidos.

### Cierre 428 comprobado

- Fuente `7904bf8a6be920e4b6227620cb3addd5a4a10e20`, run43548490641 completed/success (watcher exit0+API). Bundle54 fragmentos recuperado/verificado `/tmp/sp-ci428`.
- XML81 JUnit, cero failures/errors/skipped. All 261 required tests passed. Incluye once GameTests/tres JUnit nuevos de esta tanda, más fixture71 completado con su ward. Historial427failure no se reescribe como verde.
- JAR/servidor empaquetado/logs correctos. Cliente: GRAPHICAL_CLIENT_GALLERY_OK; UX_ARTIFACTS=32; NORMAL_AND_SHIFT=64; KEYBOARD_TO_SERVER_ACTIVATE_CANCEL_OK; SUPREME_REMOVED_BLOCKS_9341 y SUPREME_COMPLETE. Scanner local de logs GameTest+cliente recuperados: limpio.
- Publish verified release only skipped. Ninguna captura428 inspeccionada visualmente, no aprobación visual completa ni temporal de animaciones. Sin instalación Windows/control de PC/UI ni build local.
- Artifact forge-validation14095304337,40237972bytes,noexpired. RELEASE.json verificado: special-pickaxes-1.20.1-5.10.428.jar,533981bytes,SHA256 `32fc53ed93f7a6003554d276faa94d63bfe9c6f5cb09704d35fe49c64046de03`. Se verificó el manifiesto, no JAR descargado/instalado.
- Pendiente próxima auditoría: ArtifactPassives sharedHeartwood/sharedNullVeil/onCuriosDamage conservan plazos raw y callbacks que pueden cambiar actor/target/efectos. Revisar preservando semántica (Heartwood permite mitigación propia aunque claim rechace retaliación; otros necesitan permiso). No afirmar que están corregidos. Revisión visual completa sigue pendiente.

## Nuevo alcance explícito del usuario — 5.10.429 pendiente CI

El usuario pide efectos/habilidades más únicos, secundarias SIN atacar/controlar/afectar mobs ni proyectiles de combate, daño menor y foto de cada pico (icono y mano). Esto modifica el criterio anterior de mantener dañoOP: se conserva mineríaOP pero se baja melee base de25 a4 (tierbonus12→2 y itembonus12→1, más baseplayer1). Encantamientos/pasivas melee existentes no se eliminan ni se afirma que todo daño final quede limitado4.

- Nuevas MiningAlternates block-only: Palimpsest sondeo hasta32ores en cubo radio6; Choir arco3ancho/3alto/8profundo; Crucible muestra unbloque al material seleccionado; Interregnum unbloque precisión; Worldloom pasarela9 pagando offhand; Axiom seiscaras de mena preservando núcleo; Hellspec chimenea3×3×12. Respetan cargas, naturales/playerplaced y límites; jobs usan WorkQueue/WorldSafety y permisos revalidados.
- Eventide conserva recogida de ItemEntity (no criaturas), Iridium sólo drops propios, Icarus ascensor, Exodium pozo, SeamRipper cara7×7 y Worldbreaker copia utilidad. Quitadas rutas y métodos de combate privados de secundarios; magmaEruptionAt retirado también (sólo testsloinvocaban). Pasivas/primarias de campos/gravedad y melee signatures existentes continúan: no confundir con X.
- MiningSignature: trece geometrías distintas16puntos, color propio, allowance global y audiencia bearer+3. Activación primaria y secundaria muestran firma; forma copiadasource para Worldbreaker. No se afirma animación revisada.
- Traducciones ES/EN de secundarias actualizadas. Siete GameTests obsoletos de combate reescritos para verificar utilidad sin tocar mob ni proyectil; tres erupción sustituidos por contrato nuevo. Test independiente de WorldloomSnare usa bind explícito, no X. TestpasivoChoir conserva verificaciones pasivas, no exige ataqueX. Dos GameTests adicionales: Hellspec noataque y daño/tier de los13. Total263 esperados. Dos JUnit nuevos geometrías únicas/acotadas/traslación, total83 esperado.
- Evidencia: ci_evidence exporta una captura genuine ux por pico (portrait-id.jpg, sólo reducción640×360 con ResizePortrait.java, sin controlar UI) y las prioriza en bundleChecks. No se genera una mano falsa desde sprite. pickaxe_gallery.py extrae icono según modelo (primer frame si animado), empareja13capturas y escribe HTMLembebido+ZIP. Pendiente ejecutar tras CI.
- Local recursos22/Python28/parser136/diffcheck correctos. No buildlocal/instalaciónWindows. CI propio aún pendiente. Primarias no se han rediseñado mecánicamente: se añade firma visual, no afirmar overhaul de todas.

### Corrección 430 tras CI429

429 `b428e3a6c731c9dedc678656ae480c9479033677`, run43551389102 failure en JUnit: tests nuevos de MiningSignature intentaban inicializar ArtifactKind/SoundEvents sin bootstrapMinecraft. 81/83JUnit pasaron, dosfallos de inicialización, no se ejecutaron GameTests/cliente. Bundle recuperado `/tmp/sp-ci429`.430 separa geometría pura por índice estable0–12 de mappingruntimeArtifactKind, sin modificar las formas; JUnit no inicializa registros Forge. Además los contratos block-only drenan WorkQueue y comprueban destrucción/transmutación/pago real/preservaciónore, no sólo arranque. Se eliminan comentarios/constante ya inactivos de NullWard. Fuente430pendienteCI.

### Cierre 430 — pedido de herramientas y fotos satisfecho con límites explícitos

- Fuente `5f7f8443d1b6a16a80e8859d156f0d47399e9f49`, run43551696313 completed/success. Watcherexit0+API. BundleChecks54 recuperado/verificado `/tmp/sp-ci430` con13portrait-id.jpg.
- XML83JUnit0fail0error0skip; All263requiredtestspassed. Build/JAR/servidor/logs correctos. Cliente32escenas64tooltips, KEYBOARD_TO_SERVER_ACTIVATE_CANCEL_OK, SUPREME_REMOVED_BLOCKS_9341/SUPREME_COMPLETE. Reescaneo local de logsGameTest+cliente limpio.
- Artefactoforge-validation14097126723,33468300bytes,noexpired. RELEASE.json special-pickaxes-1.20.1-5.10.430.jar,536128bytes,SHA256 `5d4fc9bed26e922ef139235c17fe1f7d6a55f63d9c4279b214f67bc56613de9a`. Sólo manifiesto verificado, no JARdescargado/instalado. Publicaciónbinariaskipped.
- Fotos generadas en `/home/user/entrega-picos-5.10.430`: HTMLembebido, PDF13páginas, vista-general.jpg, ZIP con13iconPNG+13handJPG y documentos. Galería herramienta tools/pickaxe_gallery.py usando venv `/home/user/.cache/pickaxe-gallery-env` (Pillow12). PDF/vistageneral compuestos con PIL desde esos mismos archivos, sin síntesis de mano. Archivosfuera del repositorio; no añadir grandes entregas aGit.
- Inspeccionada vista-general.jpg: trece iconos y manos reconocibles, con efectos/glint/estadocopiado según cada frame; no revisión de toda animación ni aprobación temporal. PresentarPDF principal y enlazarZIP/HTML. Usuario pidió fotos reales, no generar imágenes nuevas.
- Daño base25→4 verificado test13picos; minería tier5/uses32768/speed64 intacta. Secundariasya block/drop/self-movement-only; ataques de mobs retirados de X. No afirmar que primarias/pasivas/melee desaparecieron: se conservan mecánicas y firmas previas, con nueva firma visual para todas las activaciones. Survey da conteo/partículas, no garantizar x-ray a través de piedra. Worldloomrequiereapuntaralgúnblock y offhandinerte. Ver detalle MINING-ALTERNATES-430.md.
- Pendientes scope anterior: auditoría callbacks de ArtifactPassives sharedHeartwood/sharedNullVeil/onCuriosDamage y revisión visualtemporal completa. No modificar el enfoque explícito nuevo: secundarias sin mobs y daño menor.

## 431 — resolver realmente la instalación pública

Usuario pegó dos instaladores que abortaban: primero requería gh no instalado; segundo apuntaba /releases/latest cuando no existían releases. No repetir comandos que dependen de un binario inexistente. Pide PowerShell pegado en shell, sin guardar.ps1 y sin finally, no requieregh/token por repo público. Mantener backup/rollback y SHA256 y sólo reemplazar SpecialPickaxes.

431 cambia sólo versión/entrega, no gameplay430: verify.yml puede publicar un prerelease explícito de prueba después de todos los pasos exitosos (incluida evidencia/artifact), únicamente desde ramaactiva y no PR. contents:write permitecrear release/tag apuntandoSHAactual. Gateverificaciónvisualestable se mantiene separado, sin inventar aprobación. ExportChecks incluye JAR de candidato para recuperar/verificar bytes. Prerelease test-VERSION-RUN, latestfalse, notas advierten revisión visual incompleta y enlazanrun. Assets JAR,RELEASE.json,SHA256SUMS comprobados SHA/bytes/commit/run inmediatamente antes de publicar. Pendiente nuevoCI431; no pasar comando con URL hasta comprobarrelease existe.

### Cierre 431 y formato del instalador solicitado

Usuario detuvo espera del watcher y dio ejemplo de instalador de una línea conURL/tamaño/SHAfijos. Mantiene instrucción anterior SINfinally, SINgh/token/SINguardarps1. Corregir Markdown del ejemplo: URLliteral y [guid]/$_.Name sin escapes. Cada final debe incluir comando pegable de la versión publicada real; nunca volver a dar instalador de releaseinexistente.

Se comprobó run43557399012 seguíaenprogreso tras stop; watcheroriginal había acabado sinconfirmarCI. Se inició watcher nuevo `jar-p-blico-de-special-pickaxes-cae91817`, terminó0. API: completed/success, fuente47e535100fbb348e809e7a68927819f3f83700b2. Bundle54 recuperado/verificado `/tmp/sp-ci431`, ahora incluyeJAR. XML83JUnit0fails/errors/skips; All263requiredtestspassed; cliente32/64keyboardSupreme9341, JAR/servidor/logs correctos.

Release público creado por workflow: `test-5.10.431-43557399012`, prereleasetrue, latestfalse, sinaprobarvisióncompleta. AssetJAR746333222,536128bytes,digestGitHub `sha256:6b388089413289bc53253529a31d2d6e2d8f5f1ea070584bcd20835cfa32ae46`, stateuploaded. RELEASE.json746333224, SHA256SUMS746333223. GHreleaseviewmuestra digestnull porCLI peroAPIraw sítraedigestcorrecto; usarrawAPI.

JARrealrecuperadoChecks `/tmp/sp-ci431/build/libs/special-pickaxes-1.20.1-5.10.431.jar`: hash/bytescoincidenconGitHub; zipcontieneMETA-INF/MANIFEST.MF ymods.toml. IntentodescargacurlURLpública devolvióredirectrealalCDN release-assets.githubusercontent.com y SSL_ERROR_SYSCALL porhostnolista; no afirmardescargadirectacompletada. URLasset verificadaporAPI/GitHubdigest, descargapúblicadisponiblefueraentorno. SininstalaciónWindows. No modificarPC.

Final: comandoPSuna línea, URLdirecta https://github.com/Santi-PdR/Special-Pickaxes/releases/download/test-5.10.431-43557399012/special-pickaxes-1.20.1-5.10.431.jar, tamaño536128, hasharriba. VerificardescargaANTESmover/borrarsóloSpecialPickaxesviejos, validarhashdestino, cleanupcatch SINfinally. No guardarcomandoenarchivo.
