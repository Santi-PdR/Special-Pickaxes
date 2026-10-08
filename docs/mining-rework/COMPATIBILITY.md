# Compatibilidad del catálogo 5.2.0

La referencia local es `/home/Santipdr/.sklauncher/instances/ghouls/mods/`, no
una integración certificada. Hay Forge 1.20.1, Terramity 0.9.8, Create, Relics,
ToolLeveling, SecurityCraft, JourneyMap, Jade, Advanced XRay, Sophisticated
Backpacks, Better Combat y otros mods del pack.

| Sistema | Comportamiento deliberado | Límite de esta revisión |
|---|---|---|
| Terramity | Tier IV; las herramientas tienen velocidad base 64 o superior, 32.768 de durabilidad y encantabilidad 50. Las menas y geología Forge se consideran picables aunque un mod no declare nivel de herramienta. | Confirmado comparando con el Exodium Pickaxe del JAR local: tier IV, velocidad 20, 8.124 de durabilidad, encantabilidad 50. No se ejecutó el pack completo. |
| Advanced XRay / JourneyMap / Jade | La resonancia usa menas existentes y manda sus partículas solo al propietario; el mod no bloquea el uso de XRay. | No se comprobó convivencia visual en juego con shaders, LOD o configuración de servidor real. |
| Create / máquinas / cofres | Las habilidades de área excluyen block entities y fluidos; el Crucible solo cambia estados geológicos predeterminados y seguros. | No se probó automatización móvil o máquinas de terceros. |
| SecurityCraft / claims | Las operaciones consultan `mayInteract`, `mayBuild` y eventos Forge de habilidad/rotura. Una denegación detiene el paso y no salta la barrera. | Un mod que no participe de los hooks Forge puede aplicar reglas propias no observables. |
| ToolLeveling / encantamientos | Se conserva el nivel de Efficiency, Fortune, Silk Touch, Unbreaking y Mending en el NBT; el cálculo de trabajo y la cola siguen acotados. | No se probó el progreso/XP de ToolLeveling dentro del pack completo. |
| Distant Horizons / Photon / shaders | Las habilidades usan partículas Forge/vanilla limitadas y no dependen del renderer del mundo. | No hay medición de TPS ni revisión visual con el perfil completo. |
| Mochilas / recolección | Drops nativos; no añade almacenamiento, duplicación ni imán global. La cola pausa si los drops cercanos alcanzan el umbral de congestión. | La capacidad real de una mochila no se integra con el limitador. |
| Modo multijugador | Los cambios se ejecutan en servidor, ligados al propietario, herramienta, dimensión y permiso; el sonido se limita a jugadores cercanos. | No se considera certificación para TPS, claims o configuraciones específicas del servidor. |
| Curios | Registra un slot `pickaxe` y permite dos habilidades de apoyo por pico con H/J. El enlace es opcional y no impide iniciar sin Curios. | La disponibilidad del slot y su GUI requieren Curios instalado en cliente/servidor. |

Los picos de Terramity Exodium, Nyxium y Reverium usan el tier IV más alto de
Terramity. La herramienta Special Pickaxes es tier IV, con velocidad muy superior,
y permite cosechar los bloques del mod etiquetados como picables. Exodium, Iridium
y Hellspec usan las texturas de Terramity 0.9.8 bajo el permiso del propietario;
las procedencias y hashes están en `ASSET-PROVENANCE.json`.

Las transformaciones protegen geología marcada al colocarla desde esta versión.
Vanilla no conserva el historial de colocación para bloques preexistentes; por
eso no se afirma que pueda distinguir cada bloque natural de cada bloque antiguo
colocado por jugador.
