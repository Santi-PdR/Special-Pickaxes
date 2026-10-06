# Special Pickaxes — Forge 1.20.1

Java 17 • Forge 47.3.0+ (47.x) • mod id `specialpickaxes` • sin dependencias de otros mods.

**Estado de entrega y resultados reales:** ver [VALIDACION.md](docs/VALIDACION.md).
No confundir fuentes o tests escritos con un binario certificado.

## Compilar y probar

```sh
./gradlew build
./gradlew test
./gradlew runGameTestServer
python3 tools/validate_resources.py
git diff --check
```

JAR esperado: `build/libs/special-pickaxes-1.20.1-1.0.0.jar` (reobfuscado por ForgeGradle).
Para desarrollo: `./gradlew runClient` / `./gradlew runServer` (aceptar EULA si procede).
El workflow `Forge verification` ejecuta build, unit tests, GameTests y publica
JAR/reportes/logs como artefacto `forge-validation`. No requiere secretos.

Instalación final: colocar **solo el JAR de este proyecto** en `mods/` del cliente
y servidor Forge. Los JAR de `referencias/` no son dependencias ni se empaquetan.

## Uso y progresión

Todos aparecen en la pestaña **Picos especiales**. `/give @s specialpickaxes:overdrive`
(y el ID correspondiente) permite obtenerlos. Recetas desbloqueadas al obtener
el ingrediente central, con patrón `MMM / ␣C␣ / ␣S␣`: material M, catalizador C,
palo S. No se añaden minerales al mundo.

| ID | Material / catalizador | Función por defecto |
|---|---|---|
| `overdrive` | Diamante / bloque de redstone | Usar: 10× durante 5 s; cooldown 30 s; coste 8 |
| `excavator` | Diamante / bloque de hierro | Minar: plano 3×3, cooldown 0,5 s; desgaste por bloque |
| `vein_miner` | Diamante / esmeralda | Minar: hasta 32 minerales del mismo tipo, radio 8, cooldown 2 s |
| `inferno` | Hierro / polvo de blaze | Pasivo: funde loot mediante recetas; Silk Touch desactiva fundición |
| `magnetic` | Hierro / redstone | Pasivo en mano: atrae drops visibles en radio 5; no recoge directamente |
| `scanner` | Hierro / amatista | Usar: radio 8, hasta 16 señales y mineral más cercano; cooldown 10 s; coste 2 |
| `storm` | Diamante / pararrayos | Usar: hasta 3 monstruos visibles, radio 6, daño 6 + lentitud 2 s; cooldown 8 s; coste 8 |
| `void` | Netherita / estrella del Nether | Usar: ancla 30 s, retorno dentro de 24 bloques; coste 12 por uso; armado 1 s, retorno 15 s |
| `ender` | Diamante / perla de Ender | Usar: salto de hasta 8 bloques; cooldown 5 s; coste 4 |
| `explosive` | Diamante / TNT | Usar sobre bloque: esfera conectada radio 2, máximo 24; cooldown 10 s; coste 8 + desgaste por bloque |

**Usar** es la tecla vanilla configurable (clic derecho por defecto), no una tecla
fija adicional. Solo mano principal. Agacharse desactiva minería adicional e imán;
agacharse + usar Void elimina el ancla. El cooldown se muestra mediante el overlay
vanilla del item; Overdrive tiene icono/partículas de efecto. Tooltips EN/ES.

Durabilidad, velocidad base, harvest, daño base y encantabilidad proceden del tier
vanilla indicado: los picos de utilidad de hierro no minan obsidiana. No hay
"todos netherita" ni minería de bedrock. Fortune y Unbreaking siguen vanilla.
Auto-smelt no concede XP adicional de horno (solo XP normal del bloque).

## Configuración

`<mundo>/serverconfig/specialpickaxes-server.toml`, sincronizada por Forge.
Para valores por defecto de mundos nuevos: `defaultconfigs/`.
Cooldown, duración y coste por habilidad en ticks; radios, límite de resultados,
velocidad/objetos por tick del imán, objetivos/daño/lentitud Storm, límites de
explosión, multiplicador Overdrive y relación máxima de dureza son configurables.
Los rangos de config tienen topes de seguridad. Un cooldown de cero lo desactiva;
una duración de Overdrive cero desactiva su activación.

## Arquitectura e integración

- `PickaxeDefinition` + `SpecialPickaxeItem`: un único item por composición de
  `PickaxeAbility`; añadir combinaciones no requiere copiar clases de pico.
- `AbilityRuntime`: permisos, evento cancelable, persistencia de cooldown por
  jugador, coste y feedback. Reemplazar el item/reconectar no elimina el cooldown.
- `MiningSafety`: una sola ruta de `ServerPlayerGameMode.destroyBlock` por bloque;
  Forge BreakEvent, restricciones de jugador, loot tables, XP, daño y hooks vanilla.
  Excluye inventarios/block entities, fluidos, bloques inadecuados/no cosechables,
  chunks descargados, límites de mundo y bloques demasiado duros.
- `BoundedTraversal`: BFS iterativa con deduplicación, tope de resultados y tope
  independiente de exploración. Un bloque denegado es una barrera de recorrido.
- `SmeltingLootModifier`: transforma cada drop real con RecipeManager; cantidades
  Fortune ya decididas, respeta Silk Touch y recetas de otros mods. No busca ni
  modifica ItemEntity ajenas. Otros loot modifiers posteriores pueden transformar
  a su vez el resultado; no se promete un orden universal entre mods.
- `SafeTeleport`: barrido del AABB completo, sin chunks nuevos ni paredes/fluidos;
  valida destino, frontera, altura, dimensión/rango de ancla. No resetea caída.
- Lógica de mundo solo en servidor. Activación mediante paquetes vanilla de uso;
  efectos, cooldowns, movimiento, partículas y mensajes usan sincronización vanilla.
  No hay clases cliente cargadas desde el servidor ni un protocolo propio innecesario.

**Claims:** la minería honra cancelaciones del evento Forge de rotura para cada
bloque, incluida la raíz de Explosive. Para impedir escaneo, movimiento, imán o
Storm por zonas, las integraciones pueden cancelar `AbilityUseEvent` por jugador,
habilidad y posición; las habilidades también respetan `Level.mayInteract`.
Esto no equivale a afirmar compatibilidad probada con cada mod de claims: se
requiere probar las integraciones concretas. Storm usa daño atribuido al jugador
(hooks normales), solo monstruos, nunca rayos reales ni daño a otros jugadores.

Texturas pixel art e icono originales generados por `tools/generate_assets.py`;
modelos vanilla handheld, partículas y sonidos vanilla referenciados por registros.
No hay archivos de sonido falsos ni dependencias de assets externos.

## Referencias y auditoría

[Auditoría y decisiones](docs/AUDITORIA.md), bytecode resuelto y recursos en
`docs/audit/`. `tools/audit_jars.py` reproduce los informes. Los JAR originales se
conservan intactos. Se han adaptado conceptos, no redistribuido sus clases dentro
del mod. La autorización para inspeccionar/adaptar los JAR procede del propietario.

## Limitaciones conocidas

- No se afirma prueba visual de cliente ni certificación con mods de claims externos
  sin sus correspondientes sesiones de juego.
- Scanner usa tags `forge:ores`: minerales no etiquetados requieren datapack.
- Vein Miner solo conecta seis caras, no diagonales; es deliberado.
- Void no es un teletransporte interdimensional ni atraviesa muros; el retorno
  puede fallar si se cierra el camino. Al morir se descarta el ancla.
- Las operaciones masivas tienen límite por activación, pero muchos jugadores
  simultáneos requieren evaluación de carga en el servidor real.
