# Special Pickaxes — mining rework

Forge **1.20.1 / Java 17**. La versión instalada para pruebas es **5.10.270**: trece picos tier V con dos técnicas al usarlos y pasivas propias. En Curios conservan solo sus pasivas; no necesitan teclas extra. El manual reúne las instrucciones completas y los objetos mantienen descripciones breves. Las excavaciones se procesan por ticks, vuelven a validar cada bloque y respetan menas, fluidos, máquinas, zonas protegidas y bloques colocados. Las búsquedas de entidades, drops y jugadores tienen límites para mantener el coste del servidor controlado. Curios es opcional.

R activa, confirma, pausa o reanuda; X usa la técnica secundaria; C cambia el modo disponible e I abre el manual. Worldbreaker permite elegir visualmente otro pico desde el manual y muestra cuál está copiando. El sonido de habilidades se limita a jugadores cercanos y tiene enfriamiento por emisor y oyente.

La versión 5.10.270 añade cuatro columnas de enredaderas al cierre de la trampa del Telar, sin cambiar sus cuatro segundos de aviso ni su límite de partículas. La 5.10.269 evita consumir el pulso de Renovación de Duramen cuando otra habilidad bloquea la curación; tampoco muestra partículas hasta curar de verdad. La 5.10.268 solo consume la recarga y muestra el pulso de Remiendo del Alba cuando la curación se aplica de verdad; una curación bloqueada conserva el siguiente pulso. La 5.10.267 dibuja tres cortes luminosos cuando Desgarro de Estrato reduce la armadura del atacante; la animación solo aparece al activar la pasiva. La 5.10.266 dibuja el Velo Nulo como una espiral ascendente al limpiar un efecto dañino, sin animación continua ni cambios a su recarga. La 5.10.265 conserva las picadas deliberadas con élitros mientras la pasiva icaria controla las caídas normales. La 5.10.264 anima la recarga de Manto Estelar con un halo compacto y violeta que se expande y vuelve a brillar al recuperarse la intercepción, sin aumentar el ritmo de partículas. La 5.10.263 recarga la intercepción en 1,5 s, manteniendo el barrido de trayectorias cada tres ticks y la reducción de daño de los disparos que atraviesan el velo. La 5.10.262 hace visible el cierre de las raíces del Telar a los jugadores cercanos y reparte cuatro puntos de anclaje alrededor del objetivo sin aumentar la frecuencia del efecto. La 5.10.261 amplía el alcance predictivo del velo de Exodium a 10 bloques y reduce sus barridos de proyectiles a una vez cada tres ticks. La versión 5.10.260 añade un halo breve al alcanzar la tercera carga del Foco Estratal; la 5.10.259 confirmó roturas reales antes de avanzar la racha o mostrar el pulso de Estasis. La apariencia de las animaciones requiere validación visual dentro del cliente.
El análisis del Paradox Crucible usa ahora las mismas restricciones de seguridad que la transformación real y omite los bloques que ya coinciden con el material seleccionado; así no promete cambios que luego fallarán ni reporta falsamente “Nothing changed”.
La habilidad radial del Crucible ahora filtra materiales iguales al resultado y comprueba las mismas protecciones que el modo de selección antes de intentar transmutar.

El Crucible informa específicamente cuando no encuentra geología segura para transformar y qué tipo de bloque y modo revisar.

Los campos de Stasis y Eventide ahora consultan directamente criaturas hostiles y proyectiles; entidades irrelevantes como drops no consumen el límite de búsqueda.

En la instancia ghouls, las vetas mundiales de Terramity `brimslag` y `stellar_remnant`, y `undead_revamp2:bostrokestone`, cuentan como menas; `igneostone` y los bloques naturales de dripstone de Undead Revamped participan en túneles y transmutación geológica.

## Catálogo

Palimpsest · Choir of Faults · Eventide · Paradox Crucible · Crown of Stasis ·
Loom of the First Quarry · Tear of Icarus · Hollow Axiom · Worldbreaker ·
Exodium Starfall · Iridium Lodebreaker · Hellspec Infernal Bloom.

- **Palimpsest:** extracción segura de vetas conectadas con un límite configurable; su pasiva ayuda a minar y recuperarte.
- **Choir:** túnel resonante fijo 2×2, 20 bloques hacia delante; ya no repite una plantilla vieja.
- **Eventide:** campo gravitatorio que ordena derrumbes controlados al minar piedra.
- **Crucible:** transforma doce materiales geológicos, ahora también calcita y obsidiana; respeta menas, cofres, fluidos y bloques colocados.
- **Stasis:** dominio 16×16 con borde visible, ayuda a compañeros dentro o te sigue como aura; X traslada el dominio conservando su tiempo.
- **Worldloom:** túnel recto de 5×5 por 16 bloques; raíces atrapan enemigos cercanos.
- **Icarus:** perforación hacia delante y ascensor de un bloque de ancho por dos de alto que sube por el techo de terreno.
- **Axiom:** excava una malla radial de geología natural y conserva las menas; no detecta vetas. Su pliegue secundario sirve para combate.
- **Worldbreaker:** varios cortes, rotura regional y selección visual de otro pico para copiar con R/X.
- **Exodium:** galería minera de 13×9 hasta 48 bloques; X abre un pozo octogonal 5×5 hacia abajo.
- **Iridium:** mina menas cargadas desde el centro hacia afuera, a medida que las encuentra; prioriza las cercanas, ilumina sus drops y omite chunks descargados y zonas protegidas. X atrae hasta 96 drops propios.
- **Hellspec:** excava una flor de cinco pétalos; su X lanza una onda de magma que empuja, quema y marca enemigos sin alterar bloques ni líquidos.
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

La versión 5.10.127 hace que el Pozo Estelar de Exodium tenga una sección biselada propia, contorno limitado al usuario y un pulso de partículas durante la perforación. La prueba de geometría verifica que sus 21 bloques por sección no se confundan con el túnel cuadrado de Worldloom. El inventario y el validador de recursos ahora cubren los trece picos jugables y solo las cuatro teclas realmente registradas. La procedencia visual incluye un archivo compacto de las tres texturas autorizadas de Terramity para que la revisión funcione también fuera de la instancia local. La versión 5.10.126 añade las variantes superior e inferior del pilar de dripstone de Undead Revamped a la geología reconocida para minería y transmutación. La versión 5.10.125 amplía el reconocimiento de geología de mods mediante nombres de registro habituales (piedras, basaltos, granitos, pizarras, tobas, calcitas, obsidianas, espeleotemas, gravas, tierras y arcillas). Las menas, los líquidos y los bloques con entidad quedan excluidos; el resultado se recalcula tras cada actualización de tags. El icono aprobado de Curios permanece intacto.

La versión 5.10.125 reconoce también `undead_revamp2:bostroxore`, una mena mundial de Undead Revamped que no usa sufijo ni tag convencional; sus drops `rawbostrox` ahora entran en la extracción de Iridium y su iluminación.

La versión 5.10.125 evita inferir geología a partir de sufijos fusionados como `lodestone`: solo usa tokens geológicos separados o IDs de worldgen aprobados, y mantiene la lista explícita para geología vanilla.
