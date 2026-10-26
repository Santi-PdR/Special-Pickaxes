# Estado del mod y registro de versiones

## Estado vigente — 5.10.163

- El trabajo está en [PR #1](https://github.com/Santi-PdR/Special-Pickaxes/pull/1), abierto como borrador y aún sin integrar en `main`.
- La [última ejecución de Forge verification](https://github.com/Santi-PdR/Special-Pickaxes/actions/workflows/verify.yml) es la autoridad para el JAR vigente. Usa solo el artefacto `special-pickaxes-verified-<run>` del último run completado con éxito; su `RELEASE.json` y `SHA256SUMS` indican el nombre y SHA-256 de ese build exacto.
- El workflow comprueba el hash contra el JAR de release, borra JAR previos del staging temporal y falla si no queda exactamente uno. El artefacto verificado incluye ese JAR y sus dos manifiestos; no se instala automáticamente en el modpack.
- La aprobación visual se guarda en `docs/mining-rework/VISUAL-REVIEW.json` con identidad de fuente y hashes. El workflow la compara con la build antes de publicar.
- Los trece picos usan tier IV de Forge, con velocidad 64 o superior, durabilidad 32 768 y ataque 12. Las pruebas cubren drops de obsidiana, mena de diamante y escombros ancestrales, y que una herramienta de tier inferior no obtenga drops de obsidiana.
- El proyecto compila para Forge 1.20.1 y Java 17. No contiene recetas para los picos.
- La matriz del modpack ghouls describe inspección estática del código y los nombres de archivos observados. No equivale a ejecutar la instancia; esta tarea no modificó `ghouls/mods` ni controló el PC.

## Evidencia visual del candidato — 5.10.163

- Rama del PR: `codex/pack-compat-version-sync`. La fuente runtime y los recursos visuales nuevos están en `db30b43f3b6735a7980edf62b9463523c8a2a51a`.
- Validación y capturas: [run #544](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/40255897174), exitoso. Pasó build, JUnit/GameTests, validación de recursos, servidor empaquetado, smoke de cliente, galería normal/SHIFT y evidencia. La publicación binaria se omitió porque la aprobación visual aún apuntaba al runtime anterior.
- Revisión de las texturas nuevas: inventario actual y las seis capturas normales/SHIFT de Exodium Starfall, Iridium Lodebreaker y Hellspec Infernal Bloom del run #544. Los tres iconos originales 32×32 cargan y se distinguen en el modelo en mano. Las otras habilidades no cambiaron desde la revisión completa de 76 capturas del run #526.
- JAR de diagnóstico de #544, aún sin publicar como artefacto verificado: `special-pickaxes-1.20.1-5.10.163.jar`, 365910 bytes, SHA-256 `eb6f7768ff411f9b5d4d86018f88dbd90b40a62ff8c5cc011ec8f4bb3d8ff570`.
- El artefacto `forge-validation` de #544 contiene capturas y logs; su ZIP tiene SHA-256 `d69bf23a07fe62446bce219b7f1bb9c6cc45d99d8335b08e92d4dd6525955883`. El siguiente run comprobará la nueva aprobación contra esta fuente antes de publicar.
- La tabla de compatibilidad sigue siendo una revisión estática; no se ejecutó la instancia ghouls ni se modificó su carpeta `mods`.

## Historial de la versión 5.0.0 (publicación anterior)

> Las notas y hashes de la sección histórica describen exclusivamente 5.0.0; no los uses para instalar 5.10.163.

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


