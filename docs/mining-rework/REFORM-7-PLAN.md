# Reforma 7 — identidad y rendimiento (en implementación, no publicada)

## Nuevo criterio del jugador

Las formas están permitidas; cada pico necesita una habilidad útil, OP y diferente. Curios sólo efectos/bonificaciones/partículas: ninguna minería ni modificación automática del mundo. Modelos/texturas intactos. El ejemplo 6×6 no es una petición de habilidad. No publicar alpha.60 como esta reforma.

## Dirección por pico (propuesta técnica, pendiente implementación y balance)

| Pico | Identidad sostenida | Curios, sin modificar bloques |
|---|---|---|
| Palimpsest | Corte de superficie seleccionada, ancho y poco profundo | Prisa breve al minar manualmente |
| Fault Choir | Fractura en pulsos sobre la veta conectada del objetivo | Resistencia temporal tras recibir daño |
| Eventide | Abanico corto y ancho sobre la cara apuntada | Visión nocturna y motas oscuras espaciadas |
| Paradox Crucible | Guardar y repetir una selección pequeña, sin bloqueo de velocidad | Velocidad de minería temporal tras cadena manual |
| Interregnum | Ráfaga precisa de roturas en una selección acotada, sin campo invisible | Renovación de un efecto defensivo breve |
| Worldloom | Desmonte por capas, cancelable y con volumen máximo explícito | Bonificación de saturación moderada |
| Icarus | Ascenso físico que abre sólo el espacio corporal | Gracia de caída, sin abrir camino |
| Hollow Axiom | Núcleo profundo compacto sobre el objetivo, no túnel genérico | Resistencia al retroceso |
| Exodium | Desprendimiento de techo local desde arriba hacia abajo | Partículas de estrella y protección breve |
| Hellspec | Extracción de materiales duros en un volumen compacto | Resistencia al fuego |
| Worldbreaker | Copia explícita de las identidades y habilidad propia acotada | Selección de bonificación, nunca copia de minería automática |
| Iridium / Strata | Originales congelados | Auditar compatibilidad con el nuevo criterio antes de tocar originales |

Esta tabla NO afirma mecánicas terminadas. Evitar compartir un mismo excavador salvo infraestructura del scheduler/seguridad; mantener presupuesto total global, no uno adicional por habilidad.

## Bolsa: primera base implementada

`PickaxePouch` es almacenamiento acotado, todavía desconectado de drops y GUI. 64 tipos × 4096 unidades; prototipo de una unidad y contador entero, extracción máxima de un stack por llamada. No guardar stacks de tamaño ilegal como representación del total.

Primera etapa admite materiales apilables sin tag ni capacidades serializadas. Rechaza objetos con NBT, capacidades, artefactos anidados o datos desconocidos: devuelve copia íntegra del excedente. Es una restricción de seguridad inicial, no compatibilidad universal. No mezclar datos diferentes ni borrar el excedente. Pruebas de conservación, persistencia, exceso y rechazo.

Antes de activarla:
1. Menú servidor-autoritativo de retirada, validando identidad/ubicación del pico y cantidades, sin confiar en el cliente.
2. Captura durante la rotura nativa, preservando Fortuna/Seda/loot tables y respetando cancelaciones. Nunca aspirar objetos ajenos del suelo.
3. Captura sólo tras compatibilidad con los listeners de drops y pruebas de cancelación, sin duplicación ni pérdida. Registrar coste de serialización/sincronización.
4. Extracción a inventario/cofre con presupuesto y devolución segura del remanente. Sin extracción masiva a entidades.
5. Capacidad llena: notificar y preferir inventario; excedentes con salida segura explícita. No borrar ni detener por densidad de loot.
6. Evaluar datos de bolsa fuera del ItemStack si las mediciones de sincronización lo requieren; límites no negociables, sin NBT infinito.

## Rendimiento y aceptación

Medir servidor (tiempo de tick, intentos/roturas por tick), cliente (partículas/render), entidades y tamaño/sincronización del inventario, con y sin bolsa en el mismo escenario. No confundir 584 tests aprobados con ausencia de lag. Reducir volúmenes y actualizaciones por tick antes de compensar con almacenamiento. Nunca prometer rendimiento de todos los modpacks.

Pendiente: retirar minería activa y reactiva de Curios y sus descripciones, reemplazar mecánicas sostenidas, menú de bolsa y captura transaccional. No es una reforma completa ni lista para jugar.

## Segundo paso implementado: entradas runtime Curios

La tecla H ya despacha `CuriosEffectActive`: efecto de 200 ticks y cooldown de 200 ticks, sin coste, plan de bloques, escaneo de loot ni impulso. La entrada Forge de rotura manual dejó de registrar observaciones de minería añadida equipada para los picos rediseñados y Worldbreaker. Se mantienen expresamente los hooks originales de Strata/Iridium. Las bonificaciones actuales son una primera distribución, no identidades finales únicas.

Código legado de minería equipada y sus pruebas todavía existen pero no se despachan desde estas entradas runtime. Pendiente retirarlos y sustituir las expectativas de las pruebas de cliente equipado, textos y manual antes de release. No declarar que toda la suite de cliente sigue aprobada: la antigua fixture esperaba minería equipada y debe migrarse al nuevo contrato. Nuevo GameTest verifica todos los tipos: efecto presente, cero trabajos/ascenso, objetivo intacto, sin desgaste/cambio de mano/impulso y cooldown.

Validación local tras cambio: 178 Python (una omisión). Nuevos GameTests de efectos y bolsa pendientes de Forge; no se publica este estado intermedio.
