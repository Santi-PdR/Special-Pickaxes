# Interfaz y nombres — 5.12.0

## Nombres visibles

Solo cambia la traducción del nombre. Los IDs, modelos, texturas y datos de los objetos guardados se conservan; los nombres personalizados por el jugador no se sustituyen.

| ID estable | Español | English |
| --- | --- | --- |
| `palimpsest` | Alba Eterna | Dawnbreaker |
| `fault_choir` | Coro Sísmico | Faultsong |
| `eventide` | Ocaso Gravitatorio | Eventide |
| `paradox_crucible` | Crisol Alquímico | Paradox Crucible |
| `interregnum` | Corona del Tiempo | Crown of Stasis |
| `worldloom` | Telar del Mundo | Worldloom |
| `icarus` | Ícaro | Icarus |
| `hollow_axiom` | Axioma del Vacío | Void Axiom |
| `worldbreaker` | Rompemundos | Worldbreaker |
| `exodium` | Exodio Estelar | Exodium Starfall |
| `iridium` | Iridio Rompevetas | Iridium Lodebreaker |
| `hellspec` | Flor Infernal | Hellspec Bloom |
| `seam_ripper` | Desgarraestratos | Strata Ripper |

## Manual

- Cabecera: nombre del pico, modo activo y acento de color por habilidad.
- Tres tarjetas numeradas: **01 Principal**, **02 Secundaria**, **03 Controles**.
- Pie: tecla real del manual, cierre y ayuda de desplazamiento cuando hay desbordamiento.
- Selección copiada, nombres largos y modos limitados a su espacio con puntos suspensivos.
- Se conserva el tamaño compacto, el desplazamiento preciso y el selector de copias.

## Descripciones

Resúmenes de función y controles, con dimensiones importantes y coste de materiales cuando corresponde. Las instrucciones SHIFT reutilizan el resumen del modo activo y muestran la tecla reasignada. Se retiraron promesas de contornos y destellos ya eliminados; no se añaden VFX ni se cambia la potencia de las habilidades.

## Validación

La 5.12.0 quedó verificada: CI 44197698594 sobre `ebea26e8a6425e0e794cfcd509a0b0646ec71912`, 157 JUnit sin fallos/errores/omitidos, 399 GameTests, cliente gráfico y servidor empaquetado correctos. Además, 120 pruebas Python y 25 comprobaciones de recursos. Se inspeccionaron las capturas reales `manual-palimpsest-vein.jpg` y `manual-worldbreaker-carve.jpg` a 1280×720 (GUI 640×360): tarjetas legibles y selector/pie sin solapamientos en esas escenas. No implica revisión visual de todos los modos, idiomas o escalas ni compatibilidad Windows/Ghouls.

La 5.12.1 añade etiquetas de modo completamente localizadas y desplazamiento por flechas ↑↓. CI `44208279107` sobre `92561baa0d0e1ff43fa89f9eded7b2ea33c1c7e2` completamente correcta: 157 JUnit sin fallos/errores/omitidos, 399 GameTests, cliente gráfico y servidor empaquetado. Localmente: 122 pruebas Python y 25 comprobaciones de recursos.

Se revisó la captura real final de Rompemundos a 1280×720 (GUI 640×360): «Túnel redondeado» sustituye a CARVE; tarjetas, selector y pie legibles en esta escena. La imagen de referencia se conserva [aquí](review/manual-5.12.1-es.jpg), sin retoques ni generación. Origen: `build/client-smoke/manual-worldbreaker-carve.jpg` de la CI indicada; SHA-256 `defc542c6326d0b6527f13def0d7f3e70f95a5c7d1b71a004f837eebe0e91a4e`. La inspección no cubre todos los modos, idiomas, escalas ni Windows/Ghouls.
