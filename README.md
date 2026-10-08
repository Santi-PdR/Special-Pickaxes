# Special Pickaxes — mining rework

Forge **1.20.1 / Java 17**. El candidato **5.2.3** lleva doce picos tier IV con dos técnicas al usarlos y una pasiva propia. En Curios conservan solo sus pasivas; no necesitan teclas extra. El tooltip queda en tres líneas y el Manual conserva los detalles. La velocidad base es 64 o superior, con 32.768 de durabilidad y encantabilidad 50. Curios es opcional. Sin recetas obligatorias, minerales propios, worldgen ni energía.

La versión 5.0.0 anterior cuenta con publicación verificada. La rama 5.2.3 es un candidato de desarrollo; no se ha validado en el modpack completo.

## Catálogo

Palimpsest · Choir of Faults · Eventide · Paradox Crucible · Crown of Stasis ·
Loom of the First Quarry · Tear of Icarus · Hollow Axiom · Worldbreaker ·
Exodium Starfall · Iridium Lodebreaker · Hellspec Infernal Bloom.

- **Palimpsest:** extracción segura de vetas conectadas con un límite configurable; su pasiva ayuda a minar y recuperarte.
- **Choir:** túnel resonante fijo 2×2, 20 bloques hacia delante; ya no repite una plantilla vieja.
- **Eventide:** campo gravitatorio que ordena derrumbes controlados al minar piedra.
- **Crucible:** transforma doce materiales geológicos, ahora también calcita y obsidiana; respeta menas, cofres, fluidos y bloques colocados.
- **Stasis:** dominio 16×16 con borde visible, ayuda a compañeros dentro o te sigue como aura.
- **Worldloom:** cantera esférica de radio 6, repartida por ticks.
- **Icarus:** perforación hacia delante, partículas angelicales y Ascenso de Ícaro para movilidad.
- **Axiom:** excava una malla radial de geología natural y conserva las menas; no detecta vetas. Su pliegue secundario sirve para combate.
- **Worldbreaker:** varios cortes, rotura regional y selección visual de otro pico para copiar con R/X.
- **Exodium:** lanza minera de 13×9 hasta 48 bloques.
- **Iridium:** extrae menas por tandas dentro de un volumen acotado; las menas y sus drops brillan.
- **Hellspec:** excava cavidades seguras y gana resistencia al fuego cerca de lava.

Toda minería se agenda por ticks y vuelve a validar cada bloque. No carga chunks,
no procesa fluids, bedrock, máquinas ni celdas protegidas, y pausa ante congestión
de drops. Los tags Forge amplían la compatibilidad con menas y geología de mods.
Los bloques geológicos colocados por jugadores desde esta versión quedan protegidos
de las transformaciones masivas del Crucible y de la cantera Hellspec.

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
