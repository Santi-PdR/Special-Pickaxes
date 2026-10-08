# Special Pickaxes — mining rework

Forge **1.20.1 / Java 17**. El candidato **5.2.1** lleva doce picos tier IV, cada uno con pasiva, habilidad principal, habilidad alternativa y dos habilidades mientras está equipado en Curios. La velocidad base es 64 o superior, con 32.768 de durabilidad y encantabilidad 50. Curios es opcional. Sin recetas obligatorias, minerales propios, worldgen ni energía.

La versión 5.0.0 anterior cuenta con publicación verificada. La rama 5.2.1 es un candidato de desarrollo; no se ha validado en el modpack completo.

## Catálogo

Palimpsest · Choir of Faults · Eventide · Paradox Crucible · Crown of Stasis ·
Loom of the First Quarry · Tear of Icarus · Hollow Axiom · Worldbreaker ·
Exodium Starfall · Iridium Lodebreaker · Hellspec Infernal Bloom.

- **Palimpsest:** extracción de vetas conectadas hasta un límite configurable de 192, Prisa V y Regeneración II breves.
- **Choir:** túnel resonante fijo 2×2, 20 bloques hacia delante; ya no repite una plantilla vieja.
- **Eventide:** campo gravitatorio y pulso telepático que revela hostiles de noche.
- **Crucible:** transforma doce materiales geológicos, ahora también calcita y obsidiana; X activa lectura geológica.
- **Stasis:** dominio 16×16 con borde visible, ayuda a compañeros dentro o te sigue como aura.
- **Worldloom:** cantera esférica de radio 8 y Pacto Silvestre para curar/alimentar aliados.
- **Icarus:** perforación hacia delante, partículas angelicales y Ascenso de Ícaro para movilidad.
- **Axiom:** escanea una elipse cargada de 17×9×17 y te señala hasta 32 menas; también pliega al jugador por espacio libre.
- **Worldbreaker:** varios cortes, rotura regional y selección visual de una habilidad de otro pico para copiar.
- **Exodium:** lanza minera de 13×9 hasta 48 bloques.
- **Iridium:** sondea por tandas un volumen mayor; menas y drops brillan y cosecha hasta 192.
- **Hellspec:** cava con su textura intacta; apunta a lava para activar el Manto Magmático.

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

R activa; X usa la habilidad alternativa; C cambia de modo; H/J activan las dos
habilidades del slot Curios; I abre el Manual con la textura y las habilidades.
B selecciona regiones; Enter confirma solo cuando el análisis está listo; V
cancela; K pausa/reanuda (R también reanuda una pausa por drops). Los controles
son reasignables.

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
