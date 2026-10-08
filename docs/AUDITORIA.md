# Auditoría previa — 2026-10-06

Método: inspección de **todos** los class files (métodos, instrucciones y referencias
resueltas) y JSON/TOML de los tres JAR locales. `python3 tools/audit_jars.py`
reproduce los informes en `docs/audit/`, con SHA-256 de cada entrada. No se ha
confundido un nombre de clase o un tooltip con una implementación. Los nombres
SRG de dos JAR permanecen en el informe: no se presenta esto como una
descompilación Java remapeada. No se han ejecutado los mods originales.

## CustomPickaxes

- Cinco clases PickaxeItem y tiers propios, diez variantes de minerales,
  materiales, recetas, generación de mundo, modelos/texturas/lang y creative tab.
- `EchoPickaxe.use`: búsqueda espacial, conjunto explícito VALUABLE_ORES,
  mineral más cercano, dirección vertical/distancia, partículas sculk y cooldown.
  No usa un registro extensible de minerales: reemplazar por `forge:ores`.
- `StormstonePickaxe`: inventoryTick, hurtEnemy y mineBlock; este último tiene
  probabilidad 0.15 de descarga. `triggerElectricShock` busca Monster en volumen
  de 6 y aplica daño 6, partículas y sonido. Es daño real, no rayos decorativos.
- `TitaniumPickaxe.mine3x3Area`: orientación, guardia ThreadLocal,
  ForgeHooks.onBlockBreakEvent, comparación de dureza, drops y durabilidad.
  Conservar la idea de plano y guardia, no duplicar el pipeline de destrucción.
- `VitalitePickaxe.mineBlock`: efectos de sustento/curación y partículas.
- `VolcanitePickaxe`: fuego al golpear; mineBlock busca ItemEntity alrededor y
  transforma mediante mapa estático. Rechazado: puede afectar drops ajenos y no
  es una solución genérica de recetas.
- No hay clases de networking, keybind o configuración en este JAR.

## UltimatePickaxes

- Siete picos registrados: Bone, Coal, Fire, Laser, Natural, Shadow, TNT.
- Bone: drop adicional probabilístico en minerales; configurable.
- Coal: override de velocidad por bloque/tag (`50.0`, `5.0`), no un Overdrive
  temporal ni multiplicador universal de 10.
- Fire: consulta RecipeManager/SMELTING sobre item del bloque, usa Fortune
  manualmente, destruye/reemplaza drops y prende fuego al golpear. Se conserva
  la consulta de recetas, se reemplaza el cálculo manual de Fortune por loot
  vanilla seguido de transformación de cada drop.
- Laser: minado adicional acotado por config, elegibilidad por forge:ores y
  lista extra configurable. No se toma como garantía de protección de claims.
- Natural: tabla explícita de transformación a variantes musgosas.
- Shadow: efectos al minar y handler LivingAttackEvent.
- TNT: mecánica explosiva con interruptor de configuración. No se portará una
  explosión destructiva de mundo; se usará selección de bloques y break normal.
- Config: habilitar TNT/Fire, probabilidad Bone, límite y lista extra Laser.
  Hay modelos, texturas, recetas, idiomas y creative tab. No hay paquetes de
  keybind/network ni framework de habilidades independiente.

## PicksOfPower

- Arquitectura de procedimientos generados con items delegantes, capabilities
  de jugador, sincronización por mensajes, GUI de inventario y entidades.
- Seismic: plano de bloques según cara observada; invoca dropResources,
  destrucción directa, XP y daño de herramienta. Activo: proyectil/onda.
  Reemplazar la destrucción manual por ServerPlayerGameMode.destroyBlock.
- InfernoReaver: consulta SmeltingRecipe en procedimiento de minería;
  proyectiles pyromagmic, contador de munición, sonidos/partículas/overlay.
- Ender: procedimiento de desplazamiento, tick de herramienta y teleportación
  de objetos; hay lógica específica de caída posterior. Se retiene el concepto
  de movilidad, no teleports sin barrido del volumen completo del jugador.
- Backpackaxe: inventario capability, menú/pantalla, botón de auto-pickup y
  procedimientos de recogida. No se reutiliza su GUI ni captura global de drops.
- Elytra: variante wearable, vuelo, reducción de caída, beam y tecla para
  desmontar. Pickitooxe: entidad invocable con pathfinding y mando remoto.
- Configuración, entidades/renderers/modelos, sonidos propios, partículas,
  recetas, texturas, tooltips, tabs y paquetes reales. No copiar el entramado
  generado ni añadir dependencia de animación para diez herramientas simples.

## Decisión de diseño (antes de implementar)

Arquitectura original por composición: definición de pico + lista de habilidades,
servicio de activación, utilidades de minería segura, selector espacial limitado,
loot modifier para auto-smelt y feedback vanilla sincronizado. Una sola clase de
pico. Forge SERVER config; sin dependencias de los JAR de referencia.

- Overdrive nuevo: estado temporal por jugador, BreakSpeed en ambos lados,
  servidor como autoridad y cooldown persistido para evitar cambio de pico/relog.
- Excavador: plano de Titanium/Seismic, pipeline vanilla/Forge por bloque.
- Vein: BFS propia acotada, visitados, seis vecinos, solo minerales del mismo tipo.
- Inferno: recetas de Fire/Inferno, pero sobre loot real; Silk Touch tiene prioridad.
- Magnetic: atracción física, no inserción directa en inventario ni robo de objetos.
- Scanner: concepto Echo, tag Forge, radio/contador/cooldown limitados.
- Storm: descarga dirigida a monstruos visibles, inspirada en Stormstone.
- Void: ancla personal temporal y retorno validado; no inventario ilimitado.
- Ender: desplazamiento corto con barrido, sin atravesar paredes.
- Explosive: esfera pequeña, sin daño a entidades/fuego/explosión vanilla.

La compatibilidad con claims significa honrar BreakEvent y las validaciones
normales por bloque; no implica certificar todos los plugins existentes. Se añade
un evento de habilidad cancelable para integraciones adicionales. Scanner y
teleportación también pasan por ese evento. Las texturas serán originales y los
sonidos/partículas serán de Minecraft (sin referencias externas rotas).
