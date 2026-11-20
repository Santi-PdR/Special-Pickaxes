# Revisión solicitada tras probar alpha.31

## Dirección vigente

La prueba del usuario rechaza la geometría ornamental: nada de anillos que empiezan lejos ni túneles con formas como identidad. Las habilidades mineras frontales deben comenzar delante del jugador y despejar cantidades de terreno realmente grandes. El usuario no volverá a probar hasta que esta revisión esté resuelta; no publicar otra alpha como si fuera el resultado completo.

- Brillo de drops únicamente para menas del extractor Iridium (incluidas sus copias legítimas).
- Bloques rompibles de terreno de mods, arenisca, terracotas, arena y grava compatibles; fluidos y bloques irrompibles fuera de la excavación. Mantener permisos, inventarios y protección de construcciones.
- No detener habilidades por acumulación de loot; conservar presupuestos de trabajo por tick.
- Volver a romper los bloques de gravedad que ocupen el área durante una habilidad activa.
- Resúmenes breves de habilidades/Curios, positivos, sin listas de prohibiciones.
- Rehacer Eventide y la secundaria Paradox: minería frontal útil, no anillos o rejillas decorativas.
- Corregir bloqueo de velocidad de Paradox tras secundaria.
- Ascenso Ícaro: excavar el techo antes del siguiente impulso vertical.
- Arte e identidades nativas de Iridium/Strata siguen congelados.

## Implementado en alpha.35, pendiente Forge CI

Eliminado brillo solar del spawn de drops y de observaciones manuales, también al copiar Alba en Worldbreaker. G de Alba ahora aplica Prisa IV 8 s y Regeneración II 5 s. Sólo Iridium conserva marcado/recogida de drops propios de mena. Pruebas de brillo anteriores actualizadas para afirmar que los drops ordinarios **no** brillan.

Densidad de items deja de ser condición de rechazo en la frontera de minería y en el sondeo de presión de jobs; no se eliminan items ni se cambia loot para conseguirlo. Regresión con 256 entidades ahora exige que la excavación continúe con ellas presentes.

Elegibilidad de los programas que usan NaturalExcavationProgram ampliada a estados rompibles, no sólo una lista geológica. Se mantienen revalidación de estado, chunks, callbacks, tool tier, claims y procedencia. Nueva prueba real de areniscas, terracotas, arena, grava y vidrio. Los programas que conservan filtros particulares siguen pendientes de unificación; no se afirma compatibilidad universal ya cerrada.

Paradox secundaria limpia la selección de esquinas después de que el job sea aceptado: ésta era una ruta que dejaba activo el bloqueo BreakSpeed=0. Falta confirmar en cliente y rehacer la habilidad.

Ícaro excava el volumen barrido por el cuerpo antes de aplicar velocidad usando la misma frontera de minería con lease vivo; revalida la fuente y el cuerpo libre antes del impulso. Test de techo ahora exige rotura y velocidad vertical, conservando inmóvil la posición de servidor en ese tick.

Curios muestra un solo resumen (máximo 200 caracteres por pico/idioma), en vez de pasiva + descripción extensa + pie redundante. Las demás tarjetas aún deben resumirse al cerrar el rediseño.

## Pendiente — no dar por terminado

1. Rehacer las habilidades frontales, especialmente Eventide y Paradox, manteniendo identidades diferentes y potencia útil; eliminar anillos/túneles decorativos rechazados.
2. Seguimiento de arena/grava y otros bloques de caída que llegan a celdas ya visitadas durante ejecución.
3. Unificar filtros de los programas con listas propias sin alterar las habilidades nativas congeladas.
4. Resumir las tarjetas principal/secundaria y retirar mensajes negativos innecesarios.
5. Forge CI, cliente Curios G real, revisión de acción y publicación sólo después del cierre solicitado.

## Referencias consultadas, sin copiar código ni arte

- Mekanism, rama 1.20.x, `ItemAtomicDisassembler.onBlockStartBreak`: la extracción de vetas parte del bloque roto y encuentra posiciones conectadas. Referencia para minería que nace en el frente real, no una forma remota.
  https://github.com/mekanism/Mekanism/blob/1.20.x/src/main/java/mekanism/common/item/gear/ItemAtomicDisassembler.java
- Draconic Evolution, rama 1.20, `IModularMiningTool.breakAOEBlocks`: área de trabajo calculada desde el bloque seleccionado y el jugador, potencia/alcance configurables y comprobaciones de herramientas. Referencia de utilidad y potencia, no para reproducir sus áreas como túneles decorativos.
  https://github.com/Draconic-Inc/Draconic-Evolution/blob/1.20/src/main/java/com/brandon3055/draconicevolution/items/equipment/IModularMiningTool.java
