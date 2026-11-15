## Revisión 5.10.459 — pendiente de CI

La minería diferida Curios lee la fuente actual de la ranura, sin aceptar
una copia equivalente del ItemStack original. El puente sigue siendo opcional
por reflexión: no introduce dependencia obligatoria de Curios. El lookup fresco
se reserva para eventos/transacciones; las consultas cosméticas siguen cacheadas.
Los proveedores de inventario alternativos deben pasar un lookup vivo a
MiningObservations.captureCuriosIridium, no una captura constante. Los GameTests
modelan la ranura mediante AtomicReference; no prueban una distribución real
de Curios. Sigue pendiente una prueba del modpack Ghouls instalado.

## Regresiones verificadas en 5.10.458

La CI43676520675 probó movimiento del portador/atacante y revocación de permiso
por listeners de AbilityUseEvent, sin cancelar el evento. No se aplica la
represalia a actores cambiados; el efecto defensivo propio Heartwood conserva
su función. Null Veil compartido no consume el contador si cambia el contexto.
Las observaciones Iridium diferidas no sobreviven a cambios de herramienta ni
a transición a espectador. Esto no certifica equipar/desequipar Curios real en
el mismo tick, todos los plugins de claims ni un modpack Ghouls completo.

# Compatibilidad — revisión 453 validada en CI, pack completo no certificado

Las filas históricas describen decisiones, no una certificación del pack completo.
La revisión441 conserva la detección de menas sin tags de herramienta, pero
respeta requisitos de tiers superiores explícitos en TierSortingRegistry de Forge.
No se ha ejecutado Ghouls completo. CI 43671030909 pasó los 327 GameTests; tres de ellos cubren tier superior,
minería ordinaria OP y rechazo en la frontera de rotura.

# Compatibilidad del catálogo 5.10.345

La defensa compartida de Worldloom se activa solo para jugadores que tienen Heartwood Renewal, el estado otorgado a los aliados cercanos. Cada beneficiario tiene su propio cooldown de 160 ticks. La represalia requiere un atacante válido, respeta daño entre jugadores y consulta de zona protegida antes de aplicar Heartwood Grasp. Si el portador ya lleva Worldloom en Curios, usa su contraataque habitual sin duplicarlo.

La referencia local es `/home/Santipdr/.sklauncher/instances/ghouls/mods/`, no
una integración certificada. Hay Forge 1.20.1, Terramity 0.9.8, Create, Relics,
ToolLeveling, SecurityCraft, JourneyMap, Jade, Advanced XRay, Sophisticated
Backpacks, Better Combat y otros mods del pack.

| Sistema | Comportamiento deliberado | Límite de esta revisión |
|---|---|---|
| Terramity | Los picos propios usan tier V; las herramientas tienen velocidad base 64 o superior, 32.768 de durabilidad y encantabilidad 50. Las menas y geología Forge se consideran picables aunque un mod no declare nivel de herramienta. | El código y GameTests confirman el tier V frente al Exodium de Terramity tier IV; la comparación local registrada para el Exodium es velocidad 20, durabilidad 8.124 y encantabilidad 50. No se ejecutó el pack completo. |
| Advanced XRay / JourneyMap / Jade | Palimpsest puede sondear hasta 32 menas naturales cercanas en chunks cargados, respetando permisos; no es un bloqueo de XRay. Iridium extrae menas existentes por tandas y resalta sus drops; el mod no bloquea XRay. | No se comprobó convivencia visual en juego con shaders, LOD o configuración de servidor real. |
| Create / máquinas / cofres | Las habilidades de área excluyen block entities y fluidos; el Crucible solo cambia estados geológicos predeterminados y seguros. | No se probó automatización móvil o máquinas de terceros. |
| SecurityCraft / claims | Las operaciones consultan `mayInteract`, `mayBuild` y eventos Forge de habilidad/rotura. Una denegación detiene el paso y no salta la barrera. | Un mod que no participe de los hooks Forge puede aplicar reglas propias no observables. |
| ToolLeveling / encantamientos | Se conserva el nivel de Efficiency, Fortune, Silk Touch, Unbreaking y Mending en el NBT; el cálculo de trabajo y la cola siguen acotados. | No se probó el progreso/XP de ToolLeveling dentro del pack completo. |
| Distant Horizons / Photon / shaders | Las habilidades usan partículas Forge/vanilla limitadas y no dependen del renderer del mundo. | No hay medición de TPS ni revisión visual con el perfil completo. |
| Mochilas / recolección | Drops nativos; no añade almacenamiento, duplicación ni imán global. La cola pausa si los drops cercanos alcanzan el umbral de congestión. | La capacidad real de una mochila no se integra con el limitador. |
| Modo multijugador | Los cambios se ejecutan en servidor, ligados al propietario, herramienta, dimensión y permiso; el sonido se limita a jugadores cercanos. | No se considera certificación para TPS, claims o configuraciones específicas del servidor. |
| Curios | Registra un slot `pickaxe`; los picos equipados mantienen sus pasivas. No agrega teclas ni habilidades activas al slot. | El slot y su GUI requieren Curios en cliente/servidor. Relics requiere Curios y OctoLib; los JAR de `ghouls` se usan como referencia para la instancia de prueba. |

Los picos de Terramity Exodium, Nyxium y Reverium usan el tier IV más alto de
Terramity. Special Pickaxes usa tier V, con velocidad muy superior,
y permite cosechar los bloques del mod etiquetados como picables. Exodium, Iridium
y Hellspec usan las texturas de Terramity 0.9.8 bajo el permiso del propietario;
las procedencias y hashes están en `ASSET-PROVENANCE.json`.

La geología de otros mods se reconoce en este orden: el tag de bloques
`specialpickaxes:geology` (los packs pueden declarar terreno seguro con un datapack,
sin actualizar el mod; las entradas del mod ya incluyen las piedras de Terramity y
los pilares de dripstone de undead_revamp2 con `required: false`), la lista explícita
de Ghouls y, por último, una inferencia conservadora por nombre que exige segmentos
completos del id. Las piezas fabricadas (losas, escaleras, muros, puertas, lámparas,
vallas) nunca cuentan como terreno aunque su nombre contenga roca, y las menas, los
bloques con entidad y los fluidos se excluyen antes de cualquier regla de nombre.

Las transformaciones protegen geología marcada al colocarla desde esta versión.
Vanilla no conserva el historial de colocación para bloques preexistentes; por
eso no se afirma que pueda distinguir cada bloque natural de cada bloque antiguo
colocado por jugador.

## Procedencia de construcciones (450)

La limpieza tras rotura no borra marcas nuevas de una recolocación. Los eventos
Forge de colocación múltiple registran cada celda elegible del mismo nivel. Esto
mejora la convivencia con constructores que emiten EntityMultiPlaceEvent, pero
no identifica bloques antiguos ni colocaciones de mods que no emiten esos hooks.

## Transacciones y controles (453)

Las colocaciones pagadas rechazan cambios de modo creativo/supervivencia hechos
por listeners durante la transacción, sin gastar su material capturado. El
descubrimiento de Seam Ripper también revalida procedencia tras el claim. Las
instrucciones del manual y SHIFT usan las teclas configuradas; no se afirma
compatibilidad visual completa con interfaces de terceros.
