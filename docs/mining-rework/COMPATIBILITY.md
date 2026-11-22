# Compatibilidad vigente — alpha.57

- Minecraft **1.20.1**, Forge **47.4.10**, Java **17**.
- Curios opcional: slot `pickaxe`, efectos pasivos y activa **H**, distinta de **G** (inventario de Curios). La guía muestra la tecla reasignada.
- CI del JAR exacto `44966048410`: **216 JUnit, 574 GameTests**, servidor empaquetado/logs y cliente gráfico completos. El cliente verifica un slot Curios real con H, coste de la fuente y minería conectada, manteniendo otra herramienta en mano.
- Seguimiento sólo de pruebas `44958321321`: **216 JUnit, 569 GameTests**, servidor/cliente completos. Incluye fuentes de agua/lava llegadas después de excavar: intactas tras 40 ticks y vigilancia aún activa. Producción/recursos/build sin diferencias frente al JAR alpha.55 publicado entonces; no implica compatibilidad universal con fluidos de mods.
- Seguimiento sólo de pruebas `44955816770`: **216 JUnit, 567 GameTests**, servidor/cliente completos. Verifica polvo de hormigón y yunques con caída real en rutas sostenida y equipada (pala/pico adecuados), con drops nativos sin glowing. Producción/recursos/build idénticos a la fuente del JAR alpha.55 publicado entonces; no se reemplazó el JAR por otro compilado de pruebas.
- CI histórica sólo de pruebas `44816553795`: **214 JUnit, 547 GameTests**, servidor/cliente completos. Verifica loot denso y cambio de copia durante permisos.
- Los tests cubren permisos/callbacks, bloques colocados, contenedores, fluidos, irrompibles, cancelación por cambio de equipo y asentamiento real de arena/grava, polvo de hormigón y yunques. Esto no certifica todos los proveedores de claims ni todos los bloques de otros mods.
- Sin certificación de Windows, Ghouls completo, todos los modpacks o todas las escalas de GUI. Las capturas revisadas y sus límites se registran en [STATUS.md](STATUS.md).

[Descarga vigente](https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-6.0.0-alpha.57-44966048410) · [Guía actual](PLAYER-GUIDE.md)

<details>
<summary>Historial de compatibilidad — resultados y limitaciones de versiones anteriores</summary>

## Entrega verificada 5.10.467

- Runtime: `fe56becab9ce5d368116d031d3f7bca6b9a2dcf7`.
- [CI43683204081](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43683204081): completed/success.
- **126JUnit**, cero fallos/errores/omisiones; **382GameTests** completos.
- Cliente real:32escenas/64tooltips; `ALTERNATE_BINDINGS_REBOUND_OK`.
- Servidor empaquetado: `PACKAGED_SERVER_SMOKE_OK`; logs de runtime limpios.
- [Prerelease de prueba](https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.467-43683204081).
- JAR: **567058bytes**. SHA256:
  `957c7481a45fac57cb5a1ed543453edbed57c4dbc250c90123e9b0e831c6d626`.
  Tamaño/digest del asset GitHub coinciden con RELEASE.json y SHA256SUMS de CI.
- Galería `artifacts/picos-5.10.467/`:13páginas PDF,26imágenes reales enZIP;
  CRC, traducciones es_es y assets comprobados contra el commit del runtime.
- Local:87Python conPillow,25recursos,parser155Java,secretos/diffcheck correctos.
- Sin compilación local Java ni instalaciónWindows; no certifica Curios instalado,
  Ghouls completo ni animaciones completas. Las esperasCI no son trabajo efectivo.

### Alcance comprobado

Los nuevos GameTests ejercitan listeners Forge reales de permisos/efectos,
pero sus inventarios Curios siguen siendo proveedores simulados. CI467 valida
entregas de campo interrumpidas por detener/reemplazar/mover, desvíos Crucible
con proyectiles movidos/propietario cambiado, colapso Eventide con fuente
separada o actores desplazados y ancla Worldloom protegida. No equivale a una
certificación de plugins/proveedores externos ni del modpack completo.

## Historial de compatibilidad

## API Curios contrastada para 5.10.462 (no prueba de instalación)

Se contrastaron las firmas públicas del puente contra el commit oficial
`a91da6ba2396f6ed9042c8a47a471ca5efc97da8` de la rama1.20.x:
https://github.com/TheIllusiveC4/Curios/tree/a91da6ba2396f6ed9042c8a47a471ca5efc97da8/src/main/java/top/theillusivec4/curios/api

getCuriosInventory devuelve LazyOptional; getStacksHandler("pickaxe") devuelve
Optional; getStacks devuelve IDynamicStackHandler, con getSlots/getStackInSlot.
La confirmación lee únicamente el índice capturado, comprobando límites actuales.
Una fuente movida a otra ranura invalida el trabajo anterior. Los adaptadores
simulados siguen cubriendo la identidad; no se certifica Curios/Ghouls real ni
el coste interno de proveedores externos de inventario.

## Evidencia vigente 5.10.460

CI43678491078 pasó117JUnit/358GameTests, servidor empaquetado y cliente real.
Las pruebas de ranura son adaptadores simulados explícitos. MobEffectEvent.Added
real se usa para invalidar fuente/posición durante Lode Surge; se comprueba que
no hay transferencia posterior a compañeros ni propiedad de drops pendientes.
No se probó una instalación real de Curios ni un modpack Ghouls completo.

## Límite defensivo del puente opcional en 5.10.460

La ranura Curios `pickaxe` tiene tamaño1 en los recursos del mod. Si otro
proveedor la amplía, el puente inspecciona como máximo las primeras64entradas;
no encuentra picos que sólo estén después de ese límite. Rechaza respuestas
null o tipos incompatibles en las fronteras comprobadas. Se mantienen el caché
cosmético por tick y las consultas frescas en eventos/transacciones. Este límite
y la identidad de equipo tienen pruebas puras; eso no certifica un Curios real.

## Revisión 5.10.459 — incluida y verificada en460

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

</details>
