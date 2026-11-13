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
