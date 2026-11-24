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

## Seguimiento de capacidad llena

Fuente `047fd430ac74124b9cb4e434ada8805cc7d0df97`, CI **45048228736 SUCCESS**, 604 GameTests. Bolsa con 64 tipos/262144 unidades rechaza 256 stacks alternando entrada llena/tipo nuevo; después una firma completa deja sus 96 unidades de overflow en el mundo sin tocar contenido guardado ni pausar minería. Inserciones rechazadas ya no copian la lista completa NBT; las aceptadas siguen copy-on-write. Es una eliminación verificable de asignaciones innecesarias, no una medida de mejora de TPS/FPS. La tabla de tiempos anterior corresponde a la fuente y condiciones indicadas, no se renombra como benchmark de este cambio.

## Ticks naturales posteriores: resultado y limitaciones

Fuente `8b2b19388befb9fc50f8233228ba6e15e954791b`, CI **45054553819 SUCCESS**, 216 JUnit/605 GameTests y cliente real. `PersistentLootLoadGameTests` excava 96 bloques antes de medir, contiene física en recinto seco a altura aislada y fuerza temporalmente 3×3 chunks (restaura sólo sus tickets nuevos). Deja avanzar 70 ticks de nivel naturales, descarta primeros 20 y recoge 50 intervalos START→END de LevelTickEvent. No congela ni actualiza manualmente entidades; exige tickCount>=20 de ítems sobrevivientes sin bolsa. Orden ABBA.

| Ronda | Bolsa | Entidades iniciales → finales | Unidades mundo / bolsa | Muestras | Nivel total ms | p50 ms | p95 ms | Máximo ms |
|---:|---|---:|---:|---:|---:|---:|---:|---:|
| 0 | No | 96 → 24 | 96 / 0 | 50 | 2660.430 | 52.187 | 79.219 | 96.275 |
| 1 | Sí | 0 → 0 | 0 / 96 | 50 | 2116.435 | 40.897 | 54.309 | 63.935 |
| 2 | Sí | 0 → 0 | 0 / 96 | 50 | 1239.439 | 24.014 | 31.067 | 37.353 |
| 3 | No | 96 → 25 | 96 / 0 | 50 | 1120.671 | 21.498 | 29.055 | 31.523 |

**Conclusión funcional:** las 96 unidades se conservan tras caída/fusión natural; captura evita añadir entidades, mientras sin captura quedan 24–25 entidades incluso después de fusionarse.

**No hay conclusión causal de rendimiento:** existe una deriva grande durante las cuatro rondas. La última ronda sin bolsa fue más rápida que ambas con bolsa. No calcular ni anunciar un porcentaje de mejora seleccionando la primera ronda lenta. El calentamiento/background del mundo domina o confunde la comparación; no se ha aislado cuál de ellos ni su magnitud. Hace falta preacondicionamiento estable, más réplicas y captura de perfiles de servidor para atribuir diferencias.

Estos intervalos incluyen todo el tick de **ese nivel**, no sólo entidades, ni todo el tick de servidor. Pueden incluir trabajo de fondo de chunks/iluminación y otros subsistemas; la preparación está fuera del intervalo pero su trabajo diferido puede continuar. GameTestServer puede avanzar sin esperar 50 ms entre ticks. No hay jugadores conectados ni tráfico/renderizado real. No traducir estas cifras a TPS/FPS del usuario.

### Ensayos fallidos conservados en el historial

45051599686 falló conservación en la fixture inicial. 45052885056 instrumentó 96 entidades iniciales pero 198 unidades finales/130 entidades sin bolsa: el volumen consultado incluía exterior/techo, al nivel de trabajo de otras fixtures. No se contaron esos runs como evidencia aprobada ni se ajustó la cantidad esperada. La versión validada separa verticalmente la escena, cuenta sólo interior y mantiene la aserción exacta de 96 unidades. No filtra drops por UUID para esconder una discrepancia.

## Seguimiento de búsqueda en bolsa diversa

Fuente `75b9189012109ffcfd2cd6671122800c41fc9c19`, CI **45058964029 SUCCESS**, 606 GameTests. Inserción resuelve primero el ID por registro y sólo deserializa el prototipo del tipo coincidente; evita reconstruir stacks/capacidades de tipos distintos (hasta 63 entradas ajenas en una bolsa de 64 tipos). No añade caché persistente y conserva IDs antiguos sin namespace y validación completa de tags del candidato. Regresión de coincidencia final, otras entradas intactas, retirada exacta, saturación y física aprobadas. Esto describe trabajo evitado por código, no una medida causal de TPS/FPS; no se reutilizan tiempos anteriores como si midieran este cambio.

## Seguimiento de vista abierta

Fuente `01055ce440a5e5140f62f21e8034656d5177c262`, CI **45061754599 SUCCESS**, 608 GameTests. La vista abierta usa identidad de la lista reemplazada por las escrituras copy-on-write para invalidarse en broadcast. Regresión prueba 20 broadcasts sin reconstruir stacks de exposición, NBT ajeno sin invalidación, actualización tras inserciones y preferencia modificada. No hay revisión persistida ni hash/copia de 64 entradas por tick para detectar cambios. Los broadcasts vanilla y la sincronización de inventario siguen existiendo; no se afirma eliminación de paquetes ni mejora medida de TPS/FPS.

## Seguimiento de feedback retirado en cola

Fuente `15813054185683b56829ccbc0da67ea21f8a615e`, CI **45069864077 SUCCESS**, 609 GameTests. Sólo SignatureExcavation omite la rama de feedback legacy cada cuatro ticks, que terminaba en emisores no-op pero seguía reservando allowance/preparando datos. AbilityVfx conserva impactos/finales visibles y las rutas de originales/otros programas no se modifican. Regresión ejecuta once firmas en tickCount múltiplo de cuatro: completan y conservan las 4095 unidades de allowance legacy tras reservar 1/4096 al inicio. Es trabajo cosmético innecesario evitado, no ahorro demostrado de paquetes (esa rama ya no emitía) ni de FPS/TPS.

## Seguimiento de copia estructural de entradas

Fuente `77a46709994c89ccc64ea3e5a244ea0672ab6cf0`, CI **45407719792 SUCCESS**, 614 GameTests. Las escrituras aceptadas crean una lista de referencias nueva y clonan sólo la entrada modificada; añadir/eliminar tipos no clona profundamente las entradas ajenas. Éstas son de sólo lectura dentro de la API: toda modificación posterior vuelve a clonar entrada/lista. Los prototipos se deserializan desde copia de su NBT para aislar normalización y capacidades. Se mantiene token de invalidación del menú y formato persistido.

Las regresiones comprueban referencias ajenas reutilizadas, snapshots previos intactos, 400 operaciones contra modelo independiente, guardado/carga sin alias y tres páginas con compactación/retirada exacta. Es una reducción de copias profundas demostrada por la estructura y sus invariantes, no un porcentaje medido de FPS/TPS. Las copias del inventario/sincronización vanilla siguen existiendo; los tiempos históricos anteriores no se presentan como benchmark de este cambio.

## Payload nativo de bolsa (no benchmark de red)

Fuente b3737d8 / CI 45420018800 SUCCESS. Codec real ClientboundContainerSetSlotPacket: vacío 25 bytes; 64 tipos/262144 unidades 3912 bytes; misma bolsa tras retirar una unidad 3912 bytes. Framing/NBT exactos y snapshots independientes aprobados. No incluye framing externo, compresión, TCP ni acumulación de envíos por tick; no extrapolar ancho de banda o latencia. Confirma coste de tabla completa al cambiar un contador, no deltas.

Lectura única por slot (3ba3f77) aprobada en CI45438613414 SUCCESS:216JUnit/616GameTests/cliente real con bolsa viva10frames20/5. Evita segunda reconstrucción del prototipo al refrescar cada slot visible; total4096/icono64 y seis corrupciones sin reescritura aprobados. Sin benchmark causal ni ahorro de tráfico atribuido. 184Python(1omitida),25recursos,secret-scan528 locales aprobados.

Refresco incremental c0bf7e5 validado: CI45444309796 SUCCESS,216JUnit/617GameTests/cliente32escenas64tooltips,bolsa viva10renders20/5 y cierre físico. Se conserva identidad de displays no afectados, se reconstruyen desplazados/cambiados, y se vacían eliminados; paginación y protección de clic obsoleto aprobadas. Coste evitado de reconstrucción servidor, no benchmark causal de FPS/TPS ni ahorro de red. Local184Python(1omitida),25recursos,secret-scan528 PASS.

Base yaw por ráfaga aprobada en488215b/CI45453539106SUCCESS:217JUnit/617GameTests/cliente32escenas64tooltips y bolsa viva. Equivalencia bit a bit de44512transformaciones, sin modificar cantidad/tráfico de partículas ni habilidades originales.184Python(1omitida),25recursos,secret-scan528PASS locales. Ahorro de preparación geométrica, no benchmarkFPS/TPS; rendimiento causal global y balance siguen pendientes.
