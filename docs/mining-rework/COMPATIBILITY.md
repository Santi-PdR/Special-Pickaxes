# Compatibilidad con la instancia ghouls

## Base observada

Inventario revisado por nombres de archivo en `D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\ghouls\mods` el 2026-10-25. No se inspeccionaron ni modificaron los JAR del pack. El mod apunta a Forge 1.20.1; `build.gradle` fija Forge 47.4.10 para compilar. La lista de mods no confirma por sí sola la versión de Forge instalada en la instancia.

El inventario muestra, entre otros, Terramity 0.9.8, Undead Revamped 2.1h, Tool Leveling 2.0.0, Create 6.0.8, SecurityCraft 1.9.12, Curios 5.14.1, Relics 0.8.0.9, Just Enough Guns 0.13.1, Sophisticated Backpacks, Embeddium y Photon.

## Compatibilidad implementada

| Mod o sistema | Comportamiento del mod | Estado de validación |
|---|---|---|
| Herramientas y drops | Los trece picos están en la etiqueta vanilla `minecraft:pickaxes`, usan el tier IV del mod y no tienen recetas en `data/`. Sus filtros de bloques añaden tags Forge y familias de menas/geología para bloques de otros mods. | Revisado en fuente y recursos del repositorio; no se ejecutó el pack completo. |
| Terramity 0.9.8 | Los picos superan el Exodium Pickaxe tier IV en velocidad. Se reconocen `brimslag`, `stellar_remnant` y la geología indicada en el README. Exodium, Iridium y Hellspec reutilizan texturas con permiso documentado. | IDs y referencias documentados; pendiente prueba dentro de la instancia. |
| Undead Revamped 2.1h | Se reconocen `bostrokestone`, `bostroxore`, `igneostone` y variantes naturales de dripstone para minería/transmutación. | Compatibilidad basada en IDs explícitos; pendiente prueba en la instancia. |
| Tool Leveling 2.0.0 | El mod conserva encantamientos guardados en el objeto y aplica escalado seguro de Efficiency/Fortune. No añade una integración directa con la progresión o XP de Tool Leveling. | No se validó cómo interactúan sus niveles, XP y restricciones con estos picos. |
| Create 6.0.8 y SecurityCraft 1.9.12 | Las operaciones masivas omiten block entities y comprueban permisos Forge del jugador antes de afectar bloques o entidades. | No se probó el pack ni los casos de máquinas móviles, claims o configuraciones particulares. |
| Just Enough Guns 0.13.1 | Las defensas de Eventide, Crucible y pasivas relacionadas reconocen proyectiles JEG mediante su compatibilidad específica. | Integración en fuente; falta prueba en juego. |
| Curios 5.14.1 / Relics 0.8.0.9 | Curios es opcional; los picos equipados en el slot conservan sus pasivas, sin activar técnicas desde el slot. Relics aparece en el pack, pero no requiere un adaptador propio. | Compatibilidad con Curios documentada; GUI y combinación con Relics pendientes de probar. |
| Embeddium / Photon / AmbientSounds | El mod usa efectos Forge/vanilla y no depende de estos renderizadores para la lógica de minería. | No se revisaron capturas con el perfil visual completo. |

El inventario también incluye `Item-Obliterator-NeoForge-MC1.20.1-2.3.1`. El nombre del JAR indica una distribución NeoForge en un pack objetivo Forge; hace falta revisar el metadata interno del JAR y el loader de la instancia para confirmar que puede cargarse. No se considera una compatibilidad certificada de Special Pickaxes.

## Límites

Esta tabla describe inspección estática del código y de nombres de JAR. No representa una ejecución de la instancia, ni certifica estabilidad de TPS, claims de terceros, shaders, servidores o todos los mods del inventario. Las habilidades de minería se ejecutan en servidor, reparten el trabajo por ticks, vuelven a comprobar permisos y no cargan chunks descargados. No existen recetas, minerales propios, worldgen ni energía obligatorios.
