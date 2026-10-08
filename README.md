# Special Pickaxes — mining rework

Forge **1.20.1 / Java 17**. La rama de trabajo `arena/74873685-special-pickaxes` prepara el candidato **5.1.0**: doce picos mineros extremos con habilidades distintas. Todos son tier IV, con velocidad base mínima 64, 32.768 de durabilidad y encantabilidad 50; los picos de especialidad superan esa velocidad. Sin recetas obligatorias, minerales propios, worldgen, energía ni X-Ray.

La versión 5.0.0 anterior cuenta con publicación verificada. La rama 5.1.0 todavía no es una publicación ni una validación del modpack completo.

## Catálogo

Palimpsest · Choir of Faults · Eventide · Paradox Crucible · Crown of Stasis ·
Loom of the First Quarry · Tear of Icarus · Hollow Axiom · Worldbreaker ·
Exodium Starfall · Iridium Lodebreaker · Hellspec Infernal Bloom.

- **Palimpsest:** extracción de vetas conectadas hasta un límite configurable de 192, Prisa V y Regeneración II breves.
- **Choir:** recuerda una plantilla minada y la vuelve a extraer mirando hacia delante.
- **Eventide:** campo gravitatorio que carga pulsos de minería direccionales.
- **Crucible:** transforma doce materiales geológicos con selección de dos esquinas y confirmación.
- **Stasis:** dominio de 16×16 marcado o aura personal; velocidad minera ×8 y efectos defensivos.
- **Worldloom:** cantera esférica de geología natural, radio 8; nunca construye ni toca menas.
- **Icarus:** perforación hacia delante normal o amplia; para ante fluidos y protege contra caídas mientras avanza.
- **Axiom:** escanea una elipse cargada de 17×9×17 y te señala hasta 32 menas existentes.
- **Worldbreaker:** cinco cortes direccionales y un modo de rotura por región seleccionada.
- **Exodium:** lanza minera de 13×9 hasta 48 bloques.
- **Iridium:** localiza y extrae hasta 128 menas en el volumen cercano.
- **Hellspec:** excava geología y menas en una cavidad acotada; aporta Resistencia al fuego y Prisa IV.

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

R activa; C cambia de modo cuando hay más de uno; B activa la selección regional;
Enter confirma; V cancela; K pausa/reanuda. Los controles son reasignables y el
SHIFT del tooltip describe el modo actual.

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
