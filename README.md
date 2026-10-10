# Special Pickaxes — mining rework

Forge **1.20.1 / Java 17**. La versión candidata **5.10.116** mantiene trece picos tier IV con dos técnicas al usarlos y una pasiva propia. En Curios conservan solo sus pasivas; no necesitan teclas extra. Las habilidades X gastan enfriamiento solo al surtir efecto y el servidor aplica su recarga de 5 s o más. R activa, confirma, pausa o reanuda; X usa la técnica secundaria y cancela una selección o excavación; R cancela dominios activos. Se quitó la tecla de cancelar separada. Worldbreaker copia las técnicas R y X, muestra el pico copiado en el objeto y el Manual, y mantiene su modo propio al copiar habilidades, conserva sus pasivas y prioriza el uso de una técnica regional copiada frente a su propio modo regional; el selector bloquea cambios durante operaciones pendientes. Las consultas de encantamientos recorren solo los IDs guardados en cada pico, no el registro completo de Forge. El icono vacío de Curios usa la silueta pixelada de referencia, con cabeza horizontal, mango diagonal, relleno gris, contorno negro continuo y fondo transparente. El dominio de Stasis resalta su perímetro con partículas más grandes y espaciadas cada dos bloques, manteniendo el presupuesto de red acotado. Su Manual aclara el volumen 16×16×8 y los cuatro beneficios que reciben hasta 16 compañeros. La lista de técnicas del Worldbreaker ahora se desplaza con la rueda en ventanas pequeñas y solo permite elegir filas visibles. Eventide atrae o repele enemigos y proyectiles dentro del campo gravitatorio marcado, sin fracturas de bloques adicionales. La extracción incremental de Iridium identifica drops nuevos en su creación, sin búsquedas antes/después por mena; su orden centro-afuera se prepara durante el inicio del mod, no al activar R. La recuperación con X aborta tras revisar como máximo 4096 entidades y mueve hasta 96 drops propios. La observación de drops de Iridium se realiza una vez por rotura y aborta sus consultas locales al llegar a 256 entidades. El apuntado de habilidades recorta el rayo en el primer chunk descargado, sin invalidar bloques cargados que estén antes del borde. Los radios de minería respetan la configuración completa de hasta 12 bloques. Las instantáneas de regiones y sus índices de restauración comparten un límite configurable de 128 MiB; los modos sin restauración no cargan memorias y la reserva ocurre tras aprobar el presupuesto. Las consultas reflejadas al slot de Curios se reutilizan durante el mismo tick para evitar recorridos redundantes. Las selecciones de objetivos de combate y los campos de Stasis examinan en streaming y se detienen tras 1024 entidades, evitando listas completas en zonas concurridas; los filtros de aliados y proyectiles se aplican antes de seleccionar objetivos. Los sondeos de minería y la transmutación geológica reconocen ahora adoquín, pizarra adoquinada, piedra negra, basalto liso y obsidiana junto con las familias de roca previas. Iridium excluye los tags de roca anfitriona `ores_in_ground` al detectar menas y también identifica minerales de otros mods por tags o nombres de registro convencionales terminados en `_ore`/`_ores` o iniciados en `ore_`/`ores_` con variantes de pizarra profunda, para incluirlos en sondeos, extracción y brillo de drops. El sondeo prioriza las menas más cercanas y usa el límite configurable de pasos, en vez de truncar por orden espacial. Palimpsest busca vetas conectadas en un radio de 12 bloques, también en diagonal y entre variantes normal/pizarra profunda; agrupa solo tags de familia, no tags de roca anfitriona; su límite predeterminado es 256 y no carga chunks. La comprobación de barrera por bloque ya no se repite dentro del mismo sondeo. El audio consulta solo jugadores cercanos con una búsqueda espacial sin listas temporales y aplica el cooldown configurado tanto por emisor como por oyente para evitar ráfagas en grupos; libera ambas cachés al desconectarse o cerrar el servidor. El registro persistente protege todos los bloques no líquidos y sin entidad de bloque que los jugadores coloquen y que los picos puedan minar; Icarus conserva madera y otros materiales, además de geología y menas. Las técnicas que afectan entidades comprueban permisos en la zona de cada objetivo. Los efectos de combate del pico también respetan aliados, PvP y protecciones del servidor; las pasivas de Curios no contraatacan a través de zonas protegidas, pero mantienen sus defensas propias. Palimpsest, Iridium, Worldloom, Axiom, Crucible y Hellspec recorren sus áreas por turnos. Stasis y el eco del Choir comparten selección acotada de objetivos cercanos, sin ordenar listas enteras de entidades. Las excavaciones radiales protegen la geología colocada por jugadores y conservan las menas; Hellspec las mina de forma intencional. La pausa por drops comprueba que el bloque siga siendo rompible, actual y permitido antes de consultar entidades; el recorrido aborta al llegar a 256 drops y no materializa listas completas. Iridium vincula su resplandor a los drops creados durante la rotura de una mena, aunque el mod los genere lejos del bloque, mediante el contexto exacto de rotura; su retorno sigue limitado a drops nuevos o aumentados; en Curios solo ilumina esos drops, sin ejecutar efectos de minería del pico en mano ni depender de que esa herramienta siga equipada. Worldbreaker compacta las instantáneas de selección a 8 o 16 bits cuando el registro de estados lo permite, conservando enteros para registros mayores. Seam Ripper extrae hasta 256 bloques naturales conectados del mismo material con R y una cara de hasta 5×5 con X; ambas habilidades conservan menas y bloques colocados. Eventide atrae o repele criaturas hostiles y proyectiles en un campo visible, sin romper bloques adicionales. Worldloom excava un túnel de 5×5 por 16 bloques; sus raíces inmovilizan durante cuatro segundos. Crucible mantiene un aura antiprojectiles de ocho segundos que también reconoce las balas de Just Enough Guns y acepta variantes geológicas naturales seguras. Hellspec usa Onda Magmática: una ola frontal que lanza y prende enemigos sin modificar el terreno. Worldbreaker muestra el pico copiado en el objeto y el Manual; el Manual solo indica modo copia si hay un pico seleccionado, y C ya no cambia el modo copiado. El icono del slot de Curios se conserva tal como aparece en la captura aprobada. Los trece picos tienen ahora una reacción pasiva propia en Curios, con enfriamiento y sin técnicas activas en ese slot. Worldbreaker Core Drill cancela el daño por caída y mantiene Caída lenta mientras la operación siga activa, incluso durante pausas por chunks o drops. El apuntado usa el raycast nativo tras comprobar el recorrido sin cargar chunks. La cola mide la congestión de drops una sola vez por bloque, también en la rotura regional. Al reanudar un trabajo pausado, R comprueba que ya haya chunks cargados y espacio para los drops antes de continuar, e informa el bloqueo si persiste. Iridium comprueba permisos solo después de hallar una mena válida en su sondeo. Ícaro abre un túnel vertical 3×3 y asciende por él hacia la superficie, protegido contra caídas durante el trabajo; el ascensor cubre hasta 384 bloques según la altura del mundo. Las partículas de trabajo, ráfaga y enlace se envían solo al dueño del pico. Worldloom marca un túnel recto de 5×5 por 16 bloques y puede enraizar hasta 16 enemigos cercanos durante cuatro segundos. El objetivo jugador también ve las partículas de raíces en sus pies; se envían solo al dueño y al atrapado. Las pasivas de Curios también reconocen al tirador de proyectiles JEG. Axiom solo inicia Nullcut al apuntar a geología natural extraíble; marca y excava una corteza esférica hueca, y su Null Ward absorbe proyectiles vanilla y de Just Enough Guns. Paradox Crucible transforma tierra, grava, calcita y obsidiana natural sin exigir que el pico pueda extraer drops del bloque de origen. Hellspec cambia su X por Onda Magmática: una ola frontal visible que lanza y quema enemigos sin tocar bloques ni líquidos. Exodium añade un pozo 5×5 hacia abajo; Hellspec excava una flor de cinco pétalos y lanza una Onda Magmática frontal que desplaza y quema enemigos. Hellspec conserva disponible su defensa de fuego tras recibir golpes físicos. El Manual precisa las condiciones de las pasivas. La velocidad base es 64 o superior, con 32.768 de durabilidad y encantabilidad 50. La renovación de pasivas de Curios recorre jugadores cada 10 ticks, sin escanear la lista en cada tick del servidor. Curios es opcional. Sin recetas obligatorias, minerales propios, worldgen ni energía.

La versión 5.0.0 anterior cuenta con publicación verificada. La rama 5.10.116 es un candidato de desarrollo; los GameTests cubren sus mecánicas, pero todavía no se ha validado con el modpack completo.

## Catálogo

Palimpsest · Choir of Faults · Eventide · Paradox Crucible · Crown of Stasis ·
Loom of the First Quarry · Tear of Icarus · Hollow Axiom · Worldbreaker ·
Exodium Starfall · Iridium Lodebreaker · Hellspec Infernal Bloom.

- **Palimpsest:** extracción segura de vetas conectadas con un límite configurable; su pasiva ayuda a minar y recuperarte.
- **Choir:** túnel resonante fijo 2×2, 20 bloques hacia delante; ya no repite una plantilla vieja.
- **Eventide:** campo gravitatorio que ordena derrumbes controlados al minar piedra.
- **Crucible:** transforma doce materiales geológicos, ahora también calcita y obsidiana; respeta menas, cofres, fluidos y bloques colocados.
- **Stasis:** dominio 16×16 con borde visible, ayuda a compañeros dentro o te sigue como aura; X traslada el dominio conservando su tiempo.
- **Worldloom:** cantera esférica de radio 8, repartida por ticks.
- **Icarus:** perforación hacia delante, partículas angelicales y Ascenso de Ícaro para movilidad.
- **Axiom:** excava una malla radial de geología natural y conserva las menas; no detecta vetas. Su pliegue secundario sirve para combate.
- **Worldbreaker:** varios cortes, rotura regional y selección visual de otro pico para copiar con R/X.
- **Exodium:** lanza minera de 13×9 hasta 48 bloques; X pliega detrás de un enemigo visible.
- **Iridium:** mina menas cargadas desde el centro hacia afuera, a medida que las encuentra; prioriza las cercanas, ilumina sus drops y omite chunks descargados y zonas protegidas. X atrae hasta 96 drops propios.
- **Hellspec:** excava una flor de cinco pétalos; su X lanza una onda visible que impulsa y quema enemigos en su trayectoria sin alterar bloques ni líquidos.
- **Sutura de los Estratos:** R pela la cara conectada entre dos materiales o la superficie expuesta; X pela el lado B opuesto sin excavar el interior.
  Su rastreo también se reparte por la cola global de ticks; no hace el sondeo completo en una sola activación.

Toda minería se agenda por ticks y vuelve a validar cada bloque. No carga chunks,
no procesa fluids, bedrock, máquinas ni celdas protegidas, y pausa ante congestión
de drops. Los tags Forge y comunes amplían la compatibilidad con menas y geología de mods.
Los bloques geológicos colocados por jugadores desde esta versión quedan protegidos
de las transformaciones masivas del Crucible y de la cantera Hellspec.

La versión 5.10.8 depura las marcas de bloques colocados al verificar las roturas
al final del tick. El borrado y el barrido gradual están limitados por tick, no
cargan chunks y mantienen la protección si otro mod cancela el evento de rotura.

## Referencia de nivel

La instancia `ghouls` trae Terramity 0.9.8. Su Exodium Pickaxe es tier IV, velocidad
20, durabilidad 8.124 y encantabilidad 50. Los picos Special Pickaxes usan tier IV,
velocidad base 64 o mayor, durabilidad 32.768 y encantabilidad 50; la habilidad de
herramienta permite extraer bloques picables y menas con tags de Forge.

Exodium, Iridium y Hellspec usan los sprites de Terramity con permiso del
propietario. La procedencia y SHA-256 están en
[TEXTURE_CREDITS.md](src/main/resources/assets/specialpickaxes/TEXTURE_CREDITS.md)
y [ASSET-PROVENANCE.json](docs/mining-rework/ASSET-PROVENANCE.json).

## Jugar y administrar

R activa la minería; X usa la técnica secundaria; C cambia de modo; I abre el
Manual. Worldbreaker usa R/X para las técnicas copiadas desde el Manual.
B selecciona regiones; Enter confirma solo cuando el análisis está listo; V
cancela; K pausa/reanuda (R también reanuda una pausa por drops). Los controles
son reasignables. Curios solo aplica las pasivas del pico equipado.

```text
/specialpickaxes grant <jugador> <id>
```

IDs: `palimpsest`, `fault_choir`, `eventide`, `paradox_crucible`, `interregnum`,
`worldloom`, `icarus`, `hollow_axiom`, `worldbreaker`, `exodium`, `iridium`,
`hellspec`.

- [Guía del jugador](docs/mining-rework/PLAYER-GUIDE.md)
- [Compatibilidad](docs/mining-rework/COMPATIBILITY.md)
- [Evidencia histórica de la versión 5.0.0](docs/mining-rework/STATUS.md)
- [Assets archivados](asset-library/README.md)
