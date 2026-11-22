# Reforma 7: evidencia de carga controlada

Fuente probada: `47047173522f940ede4fb8c04622913569e69321`.
Forge verification: **45039589537 SUCCESS**. 216 JUnit, 603 GameTests, servidor y cliente real completos.

## Método

`MiningLoadGameTests.eightConcurrentSignaturesConserveLootWithinGlobalBudget` ejecuta ocho firmas nativas Worldbreaker en posiciones disjuntas: 96 bloques de piedra por trabajo, 768 por ronda. Cuatro rondas alternan captura desactivada/activada. Se mide `System.nanoTime` alrededor de `WorkQueue.tick()` completo, incluyendo validaciones, callbacks/roturas nativas, captura y feedback síncrono. Preparación, generación de terreno y limpieza quedan fuera del intervalo.

El reloj de juego se adelanta por iteración para terminar el seguimiento de materiales asentados; no se duerme 50 ms. Usa FakePlayer y conexión de prueba. Cada ronda exige huella completa, botín exacto, cero pausas y presupuesto global de intentos. No hay umbral temporal que falle según la potencia del runner.

## Resultado observado (ms)

| Ronda | Bolsa | Ticks del planificador | Botín mundo / bolsa | Entidades | Total | p50 | p95 | Máximo |
|---:|---|---:|---:|---:|---:|---:|---:|---:|
| 0 | No | 47 | 768 / 0 | 768 | 46.106 | 0.078 | 6.522 | 7.804 |
| 1 | Sí | 47 | 0 / 768 | 0 | 51.055 | 0.046 | 7.086 | 10.953 |
| 2 | No | 47 | 768 / 0 | 768 | 47.801 | 0.045 | 7.269 | 7.893 |
| 3 | Sí | 47 | 0 / 768 | 0 | 49.544 | 0.088 | 7.351 | 7.580 |

Máximo observado: **128 intentos por tick** en las cuatro rondas, dentro del límite global configurado de 192. Este ensayo de ocho jugadores no satura ese límite. No se perdió ni duplicó material.

## Qué acredita y qué no

- Acredita finalización de 8 trabajos simultáneos y conservación exacta; captura impide añadir las 768 entidades de ítem por ronda al mundo. El objeto Java ItemEntity se construye antes del evento: no se afirma cero asignaciones.
- El almacenamiento cuesta CPU: en estas rondas el total medido con bolsa fue algo mayor. No afirmar que la bolsa hace la propia rotura más rápida.
- El beneficio esperado es evitar el coste **posterior** de actualizar/renderizar/sincronizar entidades. Ese coste no está medido aquí: no se tickean entidades ni se usa un cliente conectado por jugador.
- p50/p95 incluyen la fase de seguimiento con celdas vacías; no son percentiles de ticks de rotura exclusivamente. No deben usarse para esconder los máximos.
- Son tiempos del planificador, **no del tick completo de servidor**, ni TPS, FPS o latencia percibida. No incluyen envío real de paquetes/cliente, física de objetos persistentes, GC sostenido ni carga representativa de mods/listeners externos.
- Sólo dos muestras por variante en un mismo JVM/runner; orden, JIT y GC pueden influir. No es una estimación estadística de una mejora causal.

## Pendiente para un cierre de rendimiento general

Medir servidor con clientes reales, tráfico de sincronización, entidades persistentes y terreno/fluidos representativos; separar calentamiento de medición y repetir en mundos equivalentes. Evaluar bolsa con diversidad alta de materiales/capacidad cercana al límite, Fortuna alta y listeners de mods. No declarar «sin lag» ni rendimiento global resuelto por estas pruebas.

La comparación anterior de una sola firma (96 unidades / 96→0 entidades) y las regresiones Seda/Fortuna, overflow, inventario lleno y aislamiento NBT siguen aprobadas. Modelos/texturas no se cambian para obtener rendimiento.
