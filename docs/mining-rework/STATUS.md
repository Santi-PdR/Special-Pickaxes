# Estado actual — 5.10.411 en verificación de cliente

## Evidencia verificada el 2026-11-13

Fuente: `e2c3699c1ad01ede19f27e22bf19b3b6d3a0ac55`, rama
`arena/cec54523-special-pickaxes`.
Run: https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43443796300

Pasos confirmados **success** mediante la API de Actions:

- Recursos (22 pruebas), pruebas Python del tooling (9) y escaneo de secretos.
- Build y pruebas JUnit (`./gradlew build test`), ejecutados solo en CI.
- `verify_release.py`: estructura del JAR, exclusión de fixtures y recursos de partículas.
- Fase de GameTests, incluido el gate que exige el resumen completo de los **182** tests de fuente y ausencia de fallos required.
- Arranque del servidor con el JAR empaquetado y escaneo de logs de GameTests/servidor.

Al consultar, el job estaba en `Client runtime dependencies`. **No se confirma
éxito del cliente, revisión visual, publicación aprobada ni instalación.** Una
validación parcial no autoriza reutilizar la aprobación visual de 5.10.407.
La carpeta Windows de mods del usuario sigue sin acceso desde el sandbox.

La tanda 5.10.411 cubre selección top-k ordenada y sin duplicados, presupuesto
exacto de consultas de proyectiles, reparación de tipos NBT, caducidad de anclas
tras retroceder el reloj, protección aritmética y pulsación física del manual,
y revalidación de herramienta/barreras antes de escrituras. Incluye 17 nuevos
casos JUnit y tres GameTests de regresión. No modifica tier, potencia ni recetas.

Los commits pendientes hasta 5.10.410 también se subieron a la misma rama. Su run
`43443258552` había pasado build/JUnit, GameTests y servidor empaquetado y estaba
en capturas de cliente. El antiguo run #622 (`43437302899`) terminó en failure
en `Real client gallery and normal and SHIFT tooltip captures`; no se atribuye
una causa concreta sin sus logs.

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
