# Guía del rework minero

**Candidato en validación; no constituye anuncio de release.**

Teclas predeterminadas, reasignables en Controles: **R** activar, **C** modo,
**B** seleccionar (solo Crisol regional), **Enter** confirmar, **V** cancelar,
**K** pausar/reanudar. No se usa Shift + clic derecho. SHIFT sobre el item muestra
la ayuda del modo actual; el tooltip corto muestra función y tecla principal.

| Artefacto | Uso |
|---|---|
| Palimpsest | Mina con él para recordar excavaciones reales. Reconstruye huecos recordados cercanos pagando los bloques del inventario; no sustituye celdas ocupadas. |
| Choir | Aprende minería real y reproduce únicamente material coincidente, rotando o reflejando la geometría. |
| Eventide | Extrae una cantera achatada, del borde al centro o del centro al borde. Conserva menas; no es un campo de combate ni imán. |
| Crucible | Regional: B, dos esquinas diferentes, análisis, Enter. Proximidad: R, esfera alrededor del jugador. Mano secundaria elige piedra/pizarra/basalto/obsidiana; vacía elige piedra. Solo geología elegible existente; sin drops ni XP de transformación. |
| Interregnum | Estasis local para minar muy rápido con menos interrupciones. Minar dentro sostiene el campo; V lo cancela. |
| Worldloom | REFUGIO con puerta abierta, PUENTE o PARED. Material compatible en la secundaria y bloques reales en inventario. Apunta a la base; respeta orientación, ocupación y permisos. |
| Icarus | Perfora secciones y avanza únicamente después de despejarlas. Adelante o atrás sobre el eje de la mirada. |
| Hollow Axiom | Desmonta matriz conectada hasta tres bloques de menas reales en un elipsoide cercano. Conserva las menas, no dibuja recursos ocultos. |
| Worldbreaker | Cinco técnicas directas, sin esquinas. Ver abajo. |

## Worldbreaker

- **CARVE:** corredor redondeado largo sobre el eje dominante de la mirada.
- **FRACTURE:** abanico bajo que se ensancha al avanzar, para abrir un frente amplio.
- **CLEAVE:** sección ancha en rombo, poco profunda, para cortar un frente de cantera.
- **CORE DRILL:** pozo vertical profundo bajo el jugador; cuida dónde pisas.
- **WORLD SHATTER:** gran cuña escalonada que cambia de sección con la profundidad.
  No es una esfera agrandada ni un cubo: ofrece una excavación direccional diseñada,
  de decenas de miles de celdas potenciales, procesada por etapas.

La magnitud no elude seguridad. Bedrock, fluidos, bloques irrompibles, máquinas,
protección y límites del mundo detienen rutas. Los chunks no cargados pausan sin
cargarlos artificialmente. La acumulación de drops pausa: recógelos y reanuda con K.
Los bloques tras una barrera no se extraen. Cancelar no revierte minería ya realizada.

Cambiar de modo cancela selección/trabajo incompatible. Cambiar de herramienta,
morir o cambiar de dimensión invalida el trabajo del propietario. Una segunda
esquina idéntica no cuenta. Durante selección, los clics no realizan minería normal.

Sin energía, maná, combustible, recetas obligatorias, imanes ni almacenamiento.
Se usa durabilidad y cooldown; construir requiere material. Los encantamientos
altos permanecen en NBT, con cálculos y throughput acotados para evitar desbordamientos.
