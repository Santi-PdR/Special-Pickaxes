# Special Pickaxes — mining rework

Forge **1.20.1 / Java 17**. La versión **5.10.65** lleva trece picos tier IV con dos técnicas al usarlos y una pasiva propia. En Curios conservan solo sus pasivas; no necesitan teclas extra. Las habilidades X gastan enfriamiento solo al surtir efecto y el servidor aplica su recarga de 5 s o más. R activa, confirma, pausa o reanuda; X usa la técnica secundaria y cancela una selección o excavación; R cancela dominios activos. Se quitó la tecla de cancelar separada. Worldbreaker copia las técnicas R y X, permite cambiar con C el modo copiado, refleja ese modo y esas técnicas en el Manual, conserva sus pasivas y prioriza el uso de una técnica regional copiada frente a su propio modo regional; el selector bloquea cambios durante operaciones pendientes. Las consultas de encantamientos recorren solo los IDs guardados en cada pico, no el registro completo de Forge. El icono vacío de Curios es ahora un pico pixelado legible, con cabeza horizontal, mango diagonal, relleno gris, contorno negro y fondo transparente. Los radios de minería respetan la configuración completa de hasta 12 bloques. Las instantáneas de regiones y sus índices de restauración comparten un límite configurable de 128 MiB; los modos sin restauración no cargan memorias y la reserva ocurre tras aprobar el presupuesto. Las consultas reflejadas al slot de Curios se reutilizan durante el mismo tick para evitar recorridos redundantes. Los sondeos de minería y la transmutación geológica reconocen ahora adoquín, pizarra adoquinada, piedra negra, basalto liso y obsidiana junto con las familias de roca previas. Iridium también identifica minerales de otros mods por tags o nombres de registro convencionales terminados en `_ore`/`_ores` o iniciados en `ore_`/`ores_` con variantes de pizarra profunda, para incluirlos en sondeos, extracción y brillo de drops. El sondeo prioriza las menas más cercanas y usa el límite configurable de pasos, en vez de truncar por orden espacial. Palimpsest busca vetas conectadas en un radio de 12 bloques, también en diagonal y entre variantes normal/pizarra profunda; agrupa solo tags de familia, no tags de roca anfitriona; su límite predeterminado es 256 y no carga chunks. La comprobación de barrera por bloque ya no se repite dentro del mismo sondeo. El audio consulta solo jugadores cercanos mediante la búsqueda espacial del mundo y libera su caché al desconectarse o cerrar el servidor. La procedencia de bloques colocados conserva el mismo guardado con un conjunto primitivo de posiciones, reduciendo el costo de memoria en mundos grandes. Las técnicas que afectan entidades comprueban permisos en la zona de cada objetivo. Los efectos de combate del pico también respetan aliados, PvP y protecciones del servidor; las pasivas de Curios no contraatacan a través de zonas protegidas, pero mantienen sus defensas propias. Palimpsest, Iridium, Worldloom, Axiom, Crucible y Hellspec recorren sus áreas por turnos. Stasis y el eco del Choir comparten selección acotada de objetivos cercanos, sin ordenar listas enteras de entidades. Las excavaciones radiales protegen la geología colocada por jugadores y conservan las menas; Hellspec las mina de forma intencional. El control de drops ahora consulta permisos solo cuando hay congestión real. Iridium vincula su resplandor y retorno únicamente a drops nuevos o aumentados por la rotura actual; en Curios solo ilumina esos drops, sin ejecutar efectos de minería del pico en mano. Worldbreaker compacta las instantáneas de selección a 8 o 16 bits cuando el registro de estados lo permite, conservando enteros para registros mayores. Seam Ripper pela de forma conectada bordes de geología natural o su superficie expuesta, preservando menas y geología colocada; X desgarra hasta 48 puntos de durabilidad de una pieza de armadura del hostil apuntado y respeta las protecciones del servidor. Eventide requiere cuatro bloques de piedra iguales seguidos para desatar su derrumbe frontal. Los trece picos tienen ahora una reacción pasiva propia en Curios, con enfriamiento y sin técnicas activas en ese slot. El apuntado usa el raycast nativo tras comprobar el recorrido sin cargar chunks. La cola mide la congestión de drops una sola vez por bloque, también en la rotura regional. Iridium comprueba permisos solo después de hallar una mena válida en su sondeo. Ícaro lanza al objetivo hacia arriba y atrás con una ráfaga visual propia. Las partículas de trabajo, ráfaga y enlace se envían solo al dueño del pico. Worldloom recupera hambre gradualmente. Hellspec conserva disponible su defensa de fuego tras recibir golpes físicos. El Manual precisa las condiciones de las pasivas. La velocidad base es 64 o superior, con 32.768 de durabilidad y encantabilidad 50. Curios es opcional. Sin recetas obligatorias, minerales propios, worldgen ni energía.

La versión 5.0.0 anterior cuenta con publicación verificada. La rama 5.10.65 es un candidato de desarrollo; los GameTests cubren sus mecánicas, pero todavía no se ha validado con el modpack completo.

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
- **Iridium:** prospección acotada sin revelar menas ocultas con partículas y progreso privado al dueño; prioriza las menas cercanas y extrae por tandas hasta el límite configurable de pasos, ilumina sus drops y X atrae hasta 96 drops propios cercanos.
- **Hellspec:** excava cavidades seguras; su X convierte fuentes de lava apuntadas en obsidiana bajo permisos.
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
