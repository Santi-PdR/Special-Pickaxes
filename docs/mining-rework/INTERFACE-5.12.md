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

120 pruebas Python, 25 comprobaciones de recursos y escaneo de secretos correctos. La primera CI compiló y pasó 399 GameTests, pero su comprobación gráfica de reasignación esperaba la puntuación antigua. Corregida para comprobar el formato nuevo sin quitar la prueba de reasignación. La CI final es 44197698594 sobre `ebea26e8a6425e0e794cfcd509a0b0646ec71912`; no se confirmó su resultado porque GitHub devolvió un límite de cuota de API. No hay aprobación visual manual ni nueva build completamente verificada todavía.
