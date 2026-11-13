# Integración de terreno mediante datapacks

Fuente en desarrollo: **5.10.409**, Forge 1.20.1. Los ejemplos describen el
contrato implementado; no certifican compatibilidad con el modpack completo.
Compilación y GameTests de esta versión pendientes de CI.

## Añadir terreno de otro mod

`specialpickaxes:geology` permite declarar bloques de terreno cuyos nombres no
reconoce la inferencia. No añade recetas ni cambia el nivel de las herramientas.

Dentro de un datapack de Minecraft 1.20.1:

```text
mi_terreno/
  pack.mcmeta
  data/specialpickaxes/tags/blocks/geology.json
```

`pack.mcmeta`:

```json
{
  "pack": {
    "pack_format": 15,
    "description": "Terreno adicional para Special Pickaxes"
  }
}
```

`data/specialpickaxes/tags/blocks/geology.json`:

```json
{
  "replace": false,
  "values": [
    {"id": "mi_mod:terreno_natural", "required": false}
  ]
}
```

Sustituye `mi_mod:terreno_natural` por un ID real del mod instalado. No uses ese
placeholder como entrada de producción. `replace: false` conserva las entradas
de otros packs; `required: false` permite retirar un mod sin exigir ese bloque.

El tag se consulta en la clasificación de terreno de excavaciones y del Crisol,
en la efectividad de herramienta y en la elegibilidad del terreno para memoria o
restauración. **No autoriza por sí solo ninguna operación:** siguen aplicándose
las restricciones de cada habilidad, claims, eventos Forge, alcance, chunks
cargados, bloques colocados por jugadores y límites de trabajo.

### Exclusiones

El clasificador compartido rechaza menas, bedrock, bloques con entidad, estados
con fluido y formas decorativas reconocidas por el sufijo del ID (losas,
escaleras, muros, puertas, lámparas, etc.), incluso si se incluyen en `geology`.
La elegibilidad para memoria exige además el estado predeterminado del bloque.
No etiquetes máquinas o construcciones como terreno: esos filtros son defensas,
no una detección exhaustiva de todas las funciones de todos los mods.

La rotura manual normal sigue su propio flujo de herramienta y eventos; una
pieza que no sea terreno puede seguir siendo picable manualmente. No confundas
la seguridad de transmutación con el permiso de romper cualquier bloque.

Después de cambiar tags, recarga los datapacks con `/reload` en un entorno de
prueba con copia de seguridad. El listener de tags invalida los cachés de menas
y geología. La prueba añadida cubre la carga inicial del tag; no se ha ejecutado
una prueba completa de recarga en esta sesión.

## Tags de menas y vetas

Usa familias específicas como `forge:ores/copper` para agrupar variantes de una
mena. El agregado `forge:ores` identifica recursos, pero no debe unir minerales
diferentes en una sola veta. `ores_in_ground/*` y `ore_bearing_ground/*` describen
roca huésped; `ore_rates/*` describe tasas de drops. Ninguno de esos metadatos
debe crear por sí solo una mena ni conectar minerales distintos.

Los recursos `data/pickaxe_compat_test/**` son fixtures de desarrollo y se
excluyen del JAR. No dependas de ese namespace en un datapack del servidor.
