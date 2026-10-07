# Compatibilidad: alcance real y límites

Referencia: inventario confirmado por el propietario y auditoría histórica dentro
**de este repositorio** (`docs/siege/`). No se accede al repositorio `siege`, no se
copia una instancia y no se afirma haber ejecutado Eternal Craft ni test-1.

| Sistema | Decisión de los nueve artefactos | Qué demuestra CI / qué falta |
|---|---|---|
| Advanced XRay, JourneyMap, Jade | Sin escáner, localización de menas, outlines ni paquetes con sus posiciones. Axiom extrae matriz junto a menas existentes. | Contratos del plan y recursos; no prueba de integración con esos mods. |
| Sophisticated Backpacks | Sin almacenamiento, imán ni recolección automática. Drops nativos; congestión pausa el trabajo sin borrar objetos. | Harvesting/Fortune/Silk y límites propios; falta probar upgrades de mochilas. |
| Create y automatización | Minería activa de un propietario, ligada a su herramienta y dimensión. Nunca operaciones sobre block entities. | Preservación de chest/BE, estados cambiados y cancelación; no prueba de contraptions móviles. |
| Relics / movilidad | Icarus excava antes de mover físicamente al propietario, con recorrido barrido y colisión; no teletransporte libre ni vuelo nuevo. | Bloqueos/cuerpo seguro; falta Pehkui, pasajeros y escalado de hitbox del pack. |
| ToolLeveling | NBT numérico preservado, hooks Forge y durabilidad nativa. No progreso consumible propio ni sistema de carga. | Niveles 100/255/1000 y NBT alto, XP/Mending; falta integración de versiones/configuraciones reales. |
| Better Combat | Habilidades mineras por teclas propias, no nuevas armas/daño de área. Interregnum reduce interrupciones del trabajo. | Controles y ausencia de nueva lógica de combate; no prueba de animaciones del pack. |
| SecurityCraft / claims | BreakEvent/EntityPlaceEvent y AbilityUseEvent cancelables, mundo cargado/borde/permiso; rutas abortan ante denegación. | Listeners canceladores reales en Forge; no garantía para un mod que no participe de esos hooks. |
| Construcción | Worldloom solo refugio, puente y pared con pago real y sin sobrescribir celdas ocupadas. No copy/paste de máquinas o regiones. | Tres geometrías/orientaciones, pago, rollback y obstáculos; no integración con otros constructores. |
| Distant Horizons / shaders / Photon | Efectos breves y presupuestados; no dependencia nueva ni renderer de terreno propio. | Cliente Forge estándar; faltan shaders, LOD y medición TPS del pack completo. |

## Encantamientos: nivel almacenado versus ejecución segura

No se escriben niveles truncados en el NBT. 100, 255 y 1000 se exponen exactamente.
Efficiency limita el cálculo vanilla antes de su cuadrado entero; Fortune es exacto
hasta 4096 y usa crecimiento amortiguado por encima; Unbreaking evita desbordar el
argumento de `nextInt`. Esos límites de cálculo no cambian el nivel guardado ni el
mostrado. El scheduler tiene presupuestos independientes del valor de encantamiento.

## Archivos y adquisición

Nueve items por comando administrativo, sin recetas, minerales nuevos ni worldgen.
Los fluidos del namespace `mining_test` existen exclusivamente en GameTests y sus
clases se excluyen del JAR. Ocho sprites archivados fuera del resource pack runtime.
Los cuatro archivos de referencia permanecen intactos y no se empaquetan en el mod.
