# Verificación 4.0

La entrega se identifica por el Build-Commit del JAR y un run final exitoso de
Forge verification. No se usan resultados de 3.1 para certificar cambios nuevos.

## Automatización

- JUnit: traversal acotado, geometría y transformaciones.
- GameTests existentes: herramientas triples, encantamientos 100/255/1000 y
  superiores, loot/XP, permisos, estados, bloques especiales, memoria, lifecycle,
  regiones grandes, desconexión/muerte/dimensión, chunks ausentes y colisiones.
- Nuevos contratos: intención con herramienta equivocada, activación directa,
  cancelación durante cooldown, selección incompleta, análisis automático,
  confirmación explícita, transformación independiente y composición suprema.
- Recursos: veinte sprites de referencia distintos, modelos propios, traducciones,
  ausencia de recetas/worldgen y energía.
- Servidor dedicado con el JAR reobfuscado; Java 17. Fixtures fuera del JAR.
- Cliente real Xvfb: galería, veinte hovers normales y veinte SHIFT, regiones,
  modos y pulsaciones físicas R/V que atraviesan keybinding, red y estado del
  servidor. No se sustituye este recorrido con llamadas al handler únicamente.
- Inspección de logs, whitespace, manifiesto, hash y firmas comunes de secretos.

## Límites honestos

No hay Java local confirmado; Java se ejecuta en Actions. No hay package.json,
así que npm run check no aplica. test-1 no tiene un acceso confirmado; siege no
se utiliza ni modifica. No se certifican todos los mods/claims externos, todas
las reasignaciones posibles, todos los efectos vistos desde otros clientes ni
TPS sostenido de un servidor real. Las capturas no son imágenes generadas.

El presupuesto de efectos limita emisiones globales por tick. La lógica de
mutación sigue dentro de WorkQueue/WorldSafety. Los checkpoints y snapshots
mantienen sus límites anteriores; no hay nueva promesa de undo global ni copia
de máquinas o NBT arbitrario.
