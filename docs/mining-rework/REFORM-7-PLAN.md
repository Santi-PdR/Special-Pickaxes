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

## Tercer paso: bolsa y firmas sostenidas (pendiente Forge)

- Menú vanilla de retirada, 26 entradas/página y flecha de página. Agacharse + clic derecho en aire abre y habilita la bolsa. Clic retira hasta un stack; derecho una unidad. No acepta depósitos ni transacciones vanilla de cursor: sólo retirada servidor-autoritativa, con misma instancia de pico en mano. Inventario lleno devuelve remanente a la bolsa.
- Captura opt-in del loot de roturas nativas de habilidades antes de añadir entidades al mundo (el objeto Java ya fue creado). Hook LOWEST ignora eventos ya cancelados. Tags/capacidades/exceso siguen su ruta de drops; no aspira loot ajeno. Iridium/Strata excluidos para conservar originales. Pendiente pruebas de interacciones con otros listeners LOWEST y medición de sincronización; no se promete compatibilidad universal ni captura manual todavía.
- Once picos rediseñados entran en SignatureExcavation con huellas diferentes (incluidos cuña Paradox, línea precisa Interregnum, alas Ícaro). Secundaria Ícaro conserva ascenso. Se elimina selección regional primaria de Paradox para no dejar bloqueo manual; Worldbreaker copia la nueva firma.
- Límites 256/384 celdas e inspecciones 16/tick por trabajo dentro del scheduler global. Eso NO prueba ausencia de lag. Pendiente seguimiento de caída en nuevas huellas y ajuste de potencia.
- Migrada fixture real de tecla Curios a terreno intacto/efecto/cero desgaste. Nuevas pruebas de geometrías y captura de bolsa. Las pruebas antiguas que exigían megavolúmenes y huellas conectadas deben migrarse, no ocultarse. No hay release.

## Correcciones y migración de contrato

CI `44988825841` falló compilación por inferencia de tipos en Comparator.comparingInt; corregido con lambda BlockPos explícita en 8568557. No se atribuye a infraestructura. Revalidación postpermiso de elegibilidad/procedencia añadida.

Las nuevas firmas revisitan sólo celdas minadas durante 40 ticks, con límite de 64 roturas adicionales y deadline total de 160 ticks; cada inspección consume el presupuesto de 16/tick existente. No expande su huella durante seguimiento. Pendientes GameTests de caída en estas rutas.

Textos en tres idiomas migrados de megavolúmenes/aftershocks Curios a firmas acotadas y efectos. Contratos documentales actualizados para rechazar las cifras retiradas y describir la nueva mecánica; no se mantienen promesas antiguas sólo para aprobar tests. Cliente Worldloom ahora exige loot nativo presente (no 256+ objetos como requisito de usabilidad) y Stasis rechaza el dominio retirado. Esto NO acredita aún el rendimiento ni termina migración de todos los GameTests anteriores.

Pruebas añadidas de menú: inventario lleno devuelve todos los materiales; extracción repetida y flecha de página no duplican stacks visuales ni ocupan cursor. Regresión de seguimiento de grava ya asentada en celda minada; todavía falta física real de caída para nuevas huellas. Versión separada `7.0.0-dev.1`: no es alpha.60 ni reemplazo publicado de alpha.59.

Entrada semántica corregida: RelicControl.ACTIVATE evita el selector regional para firmas nuevas; SELECT no rearma regiones retiradas. Manual/fixture dejan de ofrecer/preparar esquinas para esas firmas. Los subsistemas regionales conservan pruebas aisladas de seguridad mientras se retira código legado, pero no deben exponerse como habilidades nuevas.

Las regresiones del componente legado ConnectedExcavation ahora lo invocan directamente (en vez de exigir que los picos nuevos lo usen). Se añade una regresión separada de entrada primaria real para las once firmas: excava toda su huella, conserva mena fuera de ella. La nueva planificación respeta además JOB_LIMIT del servidor. No se elimina cobertura de seguridad del motor legado para conseguir un verde artificial.

CI `44989689469` (fc4a7aa) compiló y ejecutó GameTests, pero FALLÓ con 16 regresiones. Varias exigían las habilidades retiradas (dominio/selección/megafrente). Se migran las de recuperación de cooldown y control semántico a trabajos nuevos sin dominio; las de seguridad del motor DomainFields/selecciones ahora lo invocan directamente. No se borran sus comprobaciones de permisos, conservación o liberación. La prueba de copia sísmica reveló además que el helper síncrono sólo avanzaba el reloj de ConnectedExcavation: ampliado a SignatureExcavation para poder terminar su asentamiento temporal. Se espera nueva validación; no declarar verde completo.

Perfilado opt-in `-Dspecialpickaxes.profileMining=true`: cada firma completada registra candidatos, inspecciones, roturas y tiempo acumulado/máximo de las transacciones nativas (incluye callbacks síncronos, no mide frame time ni tick total). Desactivado por defecto. Esta instrumentación todavía no es un resultado de rendimiento. Captura de bolsa revalida además mundo, posición y vida del actor después de listeners de spawn.

Perfil transaccional activado únicamente en el JVM de GameTests para recoger muestras reales en CI; no se activa en el mod del jugador. Registro identifica bolsa habilitada/deshabilitada. No extrapolar estas muestras sintéticas a FPS, TPS sostenido o hardware del usuario; el siguiente cierre debe incluir evidencia y limitaciones, no sólo límites teóricos.

CI `44990904730` (36118b2) FALLÓ con cinco pruebas de selección antigua; no es un SUCCESS parcial. Migradas a la API del componente regional preservando comprobaciones de propiedad, duplicados, pausa, análisis, drops y barreras. Añadido GameTest del contrato real nuevo de Paradox: SELECT rechaza región retirada, ACTIVATE abre cuña y rompe el objetivo sin bloqueo de velocidad, CANCEL inmediato termina trabajo. Revalidación completa pendiente.

CI integrado `44991966806` (e9408c5): diagnóstico Checks confirma **596 GameTests requeridos aprobados**, pero cliente FALLÓ tras 32 escenas activadas y la exportación completa también falló. No hay SUCCESS global. Descargas de logs/artefactos devuelven EOF en esta red; se obtuvo diagnóstico por Checks. La fixture final aún exigía 4096+ roturas y un punto fuera de la nueva brecha: contrato imposible con la reforma acotada. Migrada a huella exacta de Worldbreaker, volumen <=256 y pruebas de terreno externo/barreras intactas; escenario de relleno reducido de 51450 a 2028 bloques (no fabricar lag desde la propia fixture). La causa exacta del fallo de cliente todavía requiere revalidación: no atribuirla sólo a esa aserción sin log. Diagnósticos ampliados con primer error y log runtime para evitar que la cola de Gradle oculte el motivo.

Nueva fase de cliente para bolsa: interacción física Robot agacharse+clic derecho mirando al aire, comprobación servidor de menú y contenido, captura `pouch-menu.jpg`, cierre físico con Escape y marcador obligatorio POUCH_MENU_OK. No se simula openMenu desde la fixture; el almacenamiento de 17 diamantes es sólo preparación del escenario. Pendiente ejecución y revisión.

Bolsa: la vista muestra el total agregado en el nombre de cada material, no sólo el stack parcial visible. Clic derecho sobre la flecha alterna captura; la preferencia se conserva al reabrir. Primera apertura habilita captura sólo si aún no existe elección. Prueba de menú ampliada para preferencia persistente.

Añadida regresión física nueva de firma: recinto de bedrock sella agua/grava externas, la habilidad abre la celda, y sólo después se crea FallingBlockEntity de grava cuatro bloques encima. Comprueba aterrizaje/cosecha, drop nativo grava o pedernal y ausencia de entidad aún cayendo. Pendiente Forge; no se confunde con la prueba anterior de estado ya asentado.

Diagnóstico ampliado de `44993466586` (3c63fcd) confirma causa concreta del fallo de render: `ClientTooltipValidation` detectó que el texto de alternativa usaba la tecla primaria (O) en vez de alternativa reasignada (P), y la excepción se repetía en overlay. Corregidos textos `%3$s` para alternativa y `%1$s` para pausa/cancelación en los tres idiomas; test exige prefijo alternativo y ambas teclas resueltas. Checks también confirma 596 GameTests aprobados y muestras de perfil transaccional, pero el run NO tiene aprobación de cliente. No llamar fallo infra ni dar por terminado el mod.

Manual/tooltip ya no anuncian los modos materiales ni el control MODE para las firmas rediseñadas; la copia de Worldbreaker muestra el pico fuente sin un modo material retirado. Catálogo interno de modos se conserva por compatibilidad de datos y pruebas de motores legados, no como repertorio de habilidades nuevas. Etiqueta de cabecera cambia de minería frontal común a habilidad propia.

## Validación integrada confirmada

696e19c / CI 44995369324 SUCCESS: 216 JUnit, 597 GameTests y cliente real completo, incluidos controles Curios sin minería, apertura/cierre físico de bolsa, techos Ícaro, 96 bloques exactos Worldbreaker y loot Worldloom sin recoger. Evidencia revisada; no es aprobación universal de modpacks ni ausencia de lag. Ver STATUS para hash/tamaño y perfil sintético.

Seguimiento sólo de tests: mismo volumen Worldbreaker con bolsa desactivada y activada, conservación exacta world+stored y recuento de entidades. Prioridad explícita para captura de menú de bolsa en Checks. Pendiente CI; no sustituir JAR probado por recompilado de este seguimiento aunque producción sea idéntica.

Optimización de lectura de bolsa: las consultas internas ya no clonan toda la lista NBT por cada slot/contador del menú. El acceso es privado y sólo de lectura; inserción/extracción siguen copiando antes de escribir y los ItemStack retornados se deserializan por separado. Regresión nueva comprueba que mutar preview/retirada no modifica prototipo almacenado y que consultar no cambia NBT. Reduce asignaciones evitables, no acredita una cifra de TPS/FPS; requiere CI nuevo porque cambia producción.

Comparación confirmada en a3ef516 / CI 44997012546 SUCCESS, 598 GameTests: misma huella nativa de 96 bloques de piedra, sin bolsa 96 cobblestone / 96 entidades; con bolsa 96 cobblestone almacenadas / 0 entidades. Conservación exacta, sin usar aspiración posterior. Esto mide entidades añadidas al mundo, no elimina la construcción previa de ItemEntity ni demuestra FPS/TPS. Captura pouch-menu.jpg revisada: contenido correcto, título demasiado largo. Se acorta a «Bolsa de materiales» / «Material pouch» sin tocar modelos ni texturas. Cambio NBT de ed12cfe sigue pendiente validación por separado.

Cobertura adicional pendiente: rotura nativa de mena de diamante con bolsa y Seda I / Fortuna III; exige mena exacta con Seda, diamantes en rango nativo con Fortuna, agotamiento tras retirada y cero drops duplicados en el mundo. No compara tiradas aleatorias independientes como si fueran equivalentes.

Cierre de esta comprobación: 4897ed9 / CI 44999329270 SUCCESS, **216 JUnit y 600 GameTests**, servidor y cliente real completos. Seda/Fortuna y aislamiento de lecturas NBT aprobados; comparación 96 unidades conservadas / 96→0 entidades repetida. Título corto revisado en captura real de d1947fd. STATUS actualizado con hash/tamaño probados y límites; no publicación ni declaración de reforma global completa (identidad Curios/rendimiento representativo siguen abiertos).

## Diferenciación de activaciones Curios (pendiente CI)

Sólo once perfiles rediseñados; Iridium/Strata quedan exactamente con su activación Prisa I anterior, sin tocar sus hooks originales. Cada perfil dura 10 s con cooldown existente de 10 s, sin partículas de poción, sin trabajo minero ni movimiento forzado:
- Palimpsest: Prisa II; Choir: Resistencia III.
- Eventide: visión nocturna y respiración acuática; Paradox: Prisa IV y velocidad II.
- Interregnum: Absorción IV; Worldloom: Regeneración III.
- Ícaro: salto III y caída lenta (saltar sigue siendo una acción del jugador, no ascenso automático).
- Axiom: invisibilidad vanilla, no oculta equipo ni crea dominio.
- Exodium: velocidad III; Hellspec: resistencia al fuego y resistencia I.
- Worldbreaker: absorción II y resistencia II, o perfil de la fuente copiada.

La diferencia se basa en bonificaciones reales y niveles, no en contar textos/pruebas como habilidades. Textos breves sincronizados en tres idiomas. Regresiones exigen perfil exacto y no repetido entre los once, duración/límites/partículas, ausencia de cambios de terreno/desgaste/movimiento, copia de cada fuente y conservación de efecto externo más fuerte. Cada bonus revalida equipo tras callbacks Forge. No se afirma aún balance o rendimiento global.

Validación de perfiles confirmada: b479316 / CI 45032768317 **SUCCESS**. Evidencia descargada: 216 JUnit sin fallos/omisiones, **602 GameTests**, cliente real completo con CURIO_KEY_OK y POUCH_MENU_OK. Los once perfiles y copia aprueban; originales quedan Prisa I como antes de esta tanda. SHA/tamaño actualizados en STATUS. No publicación; los verdes no acreditan balance ni rendimiento global con modpacks.

## Ensayo de carga del planificador (pendiente CI)

Nuevo GameTest de ocho firmas Worldbreaker simultáneas en posiciones disjuntas. Cuatro rondas alternan captura apagada/encendida. Mide wall-clock alrededor de WorkQueue.tick completo (validación, permisos/roturas nativas, almacenamiento y feedback síncrono), reporta total/p50/p95/máximo y máximo de intentos; exige presupuesto global, fin completo sin pausas por drops y conservación exacta del botín. Limpia jobs/drops y restaura reloj entre rondas. Sin umbral temporal dependiente del hardware; no transforma una ejecución rápida en prueba automática de FPS.

Alcance limitado explícito: microbenchmark síncrono con FakePlayer, no tick entero del servidor ni física/ticking de entidades, envío real de paquetes, cliente/FPS, GC sostenido o modpack. Alternar rondas hace visibles diferencias de orden/JIT, no las elimina. No inferir causalidad de tiempos únicos ni prometer rendimiento universal.

Ensayo confirmado 4704717 / CI 45039589537 SUCCESS: 216 JUnit, **603 GameTests** y cliente real. Cuatro rondas de ocho firmas/768 bloques completan en 47 ticks sin pausas ni pérdida/duplicación; 128 intentos/tick como máximo, captura reduce 768→0 entidades. La bolsa tuvo algo más de coste inmediato acumulado que sin captura; no afirmar mejora de TPS/FPS ni ignorar ese coste. Tabla/método/limitaciones en PERFORMANCE-7.md. No cambia producción en esta tanda ni se publica JAR.

## Saturación de bolsa (pendiente Forge)

Inserción ahora retrasa la copia completa de la lista NBT hasta aceptar material; rechazar por entrada/tipos llenos no clona datos que luego descarta. Se conserva copy-on-write en operaciones aceptadas. Nueva regresión llena 64 tipos × 4096, rechaza 256 stacks alternando tipo existente lleno/nuevo, y ejecuta firma nativa con bolsa saturada: exige terminar, todo overflow al mundo y todas las entradas intactas. No cambia capacidad, drops, modelos ni texturas; no hay estimación de FPS por esta optimización de asignaciones.

CI 45047874969 / 9225de6 falló antes de compilar: Maven Central devolvió HTTP 429 al resolver slf4j-simple 1.7.30. No es un resultado de las nuevas pruebas. El diagnóstico Checks conserva el error. Reintento por API no autorizado para la integración (403 actions rerun); este seguimiento documental dispara una verificación nueva sin cambiar dependencias ni producción. Último verde completo sigue siendo 4704717 / 45039589537 (603 GameTests).

Saturación confirmada: 047fd43 / CI 45048228736 SUCCESS. **216 JUnit, 604 GameTests**, cliente real completo. Marcador POUCH_SATURATED confirma 64 tipos/262144 unidades intactas, 256 rechazos y 96 drops nativos de overflow al mundo tras terminar firma. El fallo Maven 429 anterior no se ocultó ni se contó como test aprobado; el nuevo run valida el cambio. STATUS actualizado con JAR probado no publicado. Pendientes generales de rendimiento real/red y balance siguen abiertos.

## Ticks posteriores de loot (pendiente CI)

Nuevo ensayo `PersistentLootLoadGameTests`: cuatro rondas ABBA (sin/con/con/sin captura) con la misma firma de 96 bloques. Tras completar excavación, deja avanzar 70 ticks naturales de nivel, descarta primeros 20 y mide START→END de LevelTickEvent. Cuenta ítems y unidades tras caída/fusión natural; exige al menos 40 muestras y tickCount>=20 de entidades sobrevivientes sin bolsa, para no medir entidades en chunks inactivos. Fuerza temporalmente 3×3 chunks de ambos escenarios y restaura sólo los que el ensayo forzó. Piso bedrock contiene física; no teletransporta/congela/tickea manualmente los drops. Limpieza de listener/chunks/jobs/entidades en éxito y fallo.

Es tiempo de nivel real tras excavación, no del tick completo de servidor ni de red/clientes reales, y GameTestServer puede avanzar sin esperar 50 ms por tick. No afirmar FPS/TPS ni causalidad estadística de cuatro rondas; publicar también entidades remanentes porque se fusionan naturalmente. Producción/modelos/texturas sin cambios en esta tanda.

Corrección de texto detectada durante auditoría: restablecer copia todavía anunciaba «seis modos» retirados de Worldbreaker. Las dos frases de ayuda/confirmación ahora dicen habilidades propias en los tres idiomas. Regresión local rechaza esa promesa obsoleta. Sin cambio de mecánica/modelos/texturas.

CI 45051599686 (a3d6436) FALLÓ en nueva prueba: conservación tras ticks naturales, no compilación/infra. La fixture sólo tenía piso y no aislaba fluidos del mundo de pruebas; no se atribuye aún toda la diferencia a ello sin conteos. Se sustituye por cámara seca cerrada de bedrock/aire fuera de huella, manteniendo caída/fusión nativas. Diagnóstico registra cantidades iniciales/finales antes de aserción. Ningún resultado temporal de ese run se considera validado. Pendiente revalidación, sin tocar captura productiva para esconder la pérdida observada.

69d49e2 / CI 45052885056 FALLÓ: conteos nuevos muestran 96 entidades al inicio y 198 unidades/130 entidades al final sin bolsa. No es «loot perdido» ni prueba de duplicación de captura (está apagada). La consulta incluía exterior/techo del recinto donde pueden caer materiales de fixtures previas al nivel de trabajo +100. Se acota al interior seco y se eleva la escena +128, dentro de límites de construcción; no se cambia cantidad esperada (96), no se filtran drops por UUID para esconder duplicación. Diagnóstico conserva ambas cantidades; pendiente revalidación antes de interpretar tiempos.

Recinto aislado validado: 8b2b193 / CI 45054553819 SUCCESS, **216 JUnit, 605 GameTests y cliente real**. Las cuatro rondas conservan 96 unidades: sin bolsa 96→24/25 entidades tras física/fusión, con bolsa 0. Tabla íntegra en PERFORMANCE-7.md. Los tiempos muestran deriva fuerte y última ronda sin bolsa más rápida que ambas con bolsa: no atribuir mejora TPS/FPS. Se documentan límites y fallos anteriores, no se maquillan. 179 pruebas Python locales (1 omitida), 25 recursos. Sin publicación ni cambio de modelos/texturas.

## Búsqueda de material sin reconstrucciones ajenas (pendiente CI)

Inserción consulta primero el ID guardado mediante ResourceLocation/registro de ítems, igual que ItemStack.of; sólo deserializa el prototipo cuando coincide el tipo. Conserva comprobación completa ItemStack.isSameItemSameTags, límites y copy-on-write. No es comparación textual de IDs: se preservan IDs sin namespace de datos antiguos. Evita reconstruir hasta 63 stacks/capacidades ajenas por inserción en tabla diversa, sin caché persistente ni invalidación adicional. Nueva regresión llena 64 tipos, hace coincidir último con ID legacy, conserva otras 63 entradas y retira cantidad exacta una vez. No hay porcentaje de TPS/FPS atribuido a este cambio; requiere verde nuevo.

Búsqueda optimizada confirmada: 75b9189 / CI 45058964029 SUCCESS. **216 JUnit, 606 GameTests y cliente real**, incluidos conservación posterior a física, saturación/overflow, Seda/Fortuna, aislamiento NBT y nueva coincidencia final/ID legacy. Evidencia descargada y STATUS actualizado con hash/tamaño probados. 179 Python locales (1 omitida), 25 recursos; sin publicación ni cambios de modelos/texturas/originales.

## Menú vivo y retirada de vista obsoleta (pendiente CI)

La vista de bolsa abierta ahora detecta cambios del contenido/preferencia al hacer broadcast vanilla y refresca sin exigir clic. Usa identidad opaca de la lista NBT reemplazada por las escrituras copy-on-write existentes; sin revisión persistida, caché de materiales global ni hash/copia de 64 tipos cada tick. Si no cambia contenido, no reconstruye stacks de exposición; cambios de NBT ajenos no invalidan la vista. Una escritura cruda in-place de terceros a materialPouchV1 no está cubierta por ese token: no se promete compatibilidad universal fuera de la API de almacenamiento.

Clic que detecta almacenamiento cambiado desde la vista servidor sólo refresca, no retira otro material que ocupó el índice. Esto no sustituye el protocolo vanilla de stateId ni pretende resolver toda carrera de latencia cliente. Regresiones nuevas: 20 broadcasts sin reconstrucción, material/cantidad añadidos visibles sin clic, preferencia actualizada y slot desplazado no retira el nuevo tipo hasta segundo clic explícito. Menú sigue de sólo retirada, cursor vacío. Sin cambios a modelos/texturas/originales.

Menú vivo confirmado: 01055ce / CI 45061754599 SUCCESS. Evidencia descargada: **216 JUnit, 608 GameTests, servidor y cliente real** con POUCH_MENU_OK. Dos regresiones nuevas aprobadas de actualización sin clic/idle estable y rechazo de retirada con lista desplazada; conserva pruebas anteriores de saturación, física, Seda/Fortuna, deserialización y overflow. STATUS actualizado con fuente/hash/tamaño probados, sin publicación ni cambios de modelos/texturas/originales. No atribuir TPS/FPS a esta corrección de interfaz.

## Saltar feedback retirado en firmas (pendiente CI)

WorkQueue conserva impactos/finales visibles de AbilityVfx y evita sólo para SignatureExcavation la rama antigua cada cuatro ticks: burst/starfall/rephase antiguos llaman AbilityVfx.retired, que no emite, pero reservaban presupuesto y hacían trabajo de preparación. No se cambia esa ruta en Iridium/Strata ni otros programas legados. Nueva regresión ejecuta las once firmas con tickCount múltiplo de cuatro y comprueba objetivo roto, job terminado y 4095 unidades restantes del presupuesto legacy tras cebarlo con 1/4096. Contrato estático preserva impactos visibles anteriores y ramas originales. No se eliminan partículas visibles ni se atribuye un porcentaje de TPS/FPS.

Feedback retirado validado: 1581305 / CI 45069864077 SUCCESS. Evidencia descargada confirma **216 JUnit, 609 GameTests y cliente real**. Las once firmas completan sin consumir el allowance de aura legacy; contratos de impactos visibles y ramas originales conservados. 180 Python locales (1 omitida), 25 recursos. STATUS actualizado con fuente/hash/tamaño probados. No afirmar ahorro de paquetes de una rama que ya emitía cero, ni porcentaje de FPS/TPS. Sin publicación ni cambios de modelos/texturas/originales.

## Activación parcial de Curios y cooldown (pendiente CI)

Los once perfiles rediseñados reservan cooldown antes de aplicar el primer efecto, porque MobEffectEvent.Added puede revocar equipo o retaggear una copia después de otorgar el primer bonus. Antes retornaba false sin cooldown, permitiendo repetir ese beneficio parcial al reequipar. Se siguen revalidando fuente/vida entre efectos; no se borra indiscriminadamente el primer efecto (podría fusionarse con uno ajeno). Iridium/Strata conservan orden previo por protección explícita. Regresiones simulan revocación Hellspec y retag de copia Ícaro: sin segundo bonus, cooldown de fuente original registrado, sin retry gratis y retry normal tras expirar. No cambia perfiles/niveles/duración ni añade minería Curios.

Cooldown parcial confirmado: 3e42870 / CI 45399274218 SUCCESS. Evidencia descargada: **216 JUnit, 611 GameTests y cliente real**, con CURIO_KEY_OK/POUCH_MENU_OK. Revocación y retag durante primer bonus conservan cooldown fuente, impiden segundo efecto/retry gratis y permiten retry tras expirar. 180 Python locales (1 omitida), 25 recursos. STATUS actualizado con fuente/hash/tamaño exactos probados. No se publica JAR ni se cambian originales, modelos o texturas. Esta es una corrección de activación/abuso, no una mejora de FPS/TPS ni una nueva habilidad.

## Escritura estructural de bolsa (pendiente CI)

Las escrituras aceptadas reemplazan la lista como antes (mantiene token de menú), pero reutilizan referencias a entradas no modificadas y clonan sólo la entrada cuya cantidad cambia. Añadir/quitar tipos no clona entradas ajenas. Invariante privado: ninguna entrada compartida se modifica in-place; toda modificación reemplaza entrada/lista. Deserialización de prototipos recibe copia del NBT del ítem para evitar que normalización/capacidades de ItemStack.of muten el prototipo guardado. No se comparte almacenamiento con stacks retornados o guardados/cargados.

Regresiones nuevas: identidad de entradas ajenas reutilizada, snapshots previos intactos tras inserción/reinserción/extracción parcial/remoción, checkpoint guardado/cargado independiente y 400 operaciones deterministas comparadas con modelo de referencia completo (cantidades, orden, capacidad, overflow, cursor no implicado). Se conserva cobertura de menú vivo. Es reducción estructural de copias profundas, no medición de TPS/FPS ni nueva habilidad; no cambia capacidad/formato NBT/modelos/texturas/originales.

Cobertura de aceptación adicional del cambio estructural: menú real servidor con 64 tipos/3 páginas, mapeo de los 64 índices, slots vacíos finales, retirada de los 12 tipos de última página, compactación 3→2 páginas con clamp y wrap, entrega exacta una vez y otras 52 entradas intactas. Pendiente mismo seguimiento Forge; no es una habilidad adicional ni una afirmación de rendimiento.

Escritura estructural y paginación confirmadas: 77a4670 / CI 45407719792 SUCCESS. Evidencia descargada: **216 JUnit, 614 GameTests y cliente real**, CURIO_KEY_OK/POUCH_MENU_OK/ICARUS_CEILINGS_OK. Tres regresiones nuevas aprueban snapshots, modelo de 400 operaciones y 64 tipos/tres páginas/compactación. STATUS actualizado con hash/tamaño exactos probados. Cotejo global en ACCEPTANCE-7.md distingue requisitos implementados de evidencia aún faltante (rendimiento end-to-end, balance y compatibilidad universal). No se publica JAR ni se cambia arte/originales.

## Menú vivo en cliente real (pendiente CI)

La fase física de bolsa mantiene abierta la GUI tras capturar sus 17 diamantes. Sólo entonces el servidor inserta 3 diamantes/5 lingotes mediante PickaxePouch, sin escribir slots visuales. Un probe CLIENT-only lee slots reales en ScreenEvent.Render.Post, exige 20/5 y nombres agregados durante 10 frames estables, y confirma atómicamente por archivo efímero. Servidor espera esa confirmación antes de pedir nueva captura y Escape físicos; verifica cantidades/cursor al cerrar. El cliente no altera slots ni simula paquetes de actualización. Marcadores y captura live son obligatorios para aprobar client_smoke.

Cubre sincronización nativa del servidor integrado y renderizado del menú abierto, no sólo estado servidor. La conexión integrada puede ser local/in-memory: NO es benchmark TCP, conteo de bytes, latencia externa ni FPS/TPS. Código de fixture excluido del JAR. Cuatro contratos Python complementan, no sustituyen, el cliente real. No se cuenta como nueva habilidad ni como optimización productiva.

Sincronización/render integrado confirmados en 577582c / CI 45415875250 SUCCESS: 216 JUnit/614 GameTests y cliente real con POUCH_LIVE_RENDER_OK (10 renders estables, 20 diamantes/5 lingotes), POUCH_MENU_UPDATED y cierre POUCH_MENU_OK. Capturas reales inicial/live revisadas: 17→20 diamantes y nuevo slot con 5 lingotes. 184 Python locales (1 omitida), 25 recursos.

Seguimiento adicional pendiente: codec real de ClientboundContainerSetSlotPacket con bolsa de 64 tipos/262144 unidades. Exige framing y NBT exactos tras write/read, snapshot de paquete encolado independiente de cambios posteriores, modificación de un contador de ancho fijo e independencia de copia decodificada. Registra bytes vacíos/llenos/cambio, sin llamarlo prueba TCP/latencia/ancho de banda ni atribuir FPS/TPS. Fixture excluida del JAR.

Resultado codec aa825b5 confirmado en b3737d8 / CI45420018800 SUCCESS: 216JUnit/615GameTests/cliente real. Payload25→3912bytes lleno;3912bytes al cambiar cantidad. Snapshots y aislamiento aprobados. Primer run45419913390 falló dependenciaMaven429, no aserción.

Siguiente optimización implementada, pendiente CI: menú lee una SlotView validada/detached por slot en lugar de deserializar preview y volver a hacerlo en amount. Mantiene rechazo de datos inválidos, copia del prototipo y total completo independiente de stack visible. Nueva regresión menú4096→stack64/nombre4096, seis corrupciones rechazadas sin reescribir datos, recuperación y cursor vacío. No cache global ni cambio de red/capacidad; no se atribuye FPS/TPS.

Lectura única por slot (3ba3f77) aprobada en CI45438613414 SUCCESS:216JUnit/616GameTests/cliente real con bolsa viva10frames20/5. Evita segunda reconstrucción del prototipo al refrescar cada slot visible; total4096/icono64 y seis corrupciones sin reescritura aprobados. Sin benchmark causal ni ahorro de tráfico atribuido. 184Python(1omitida),25recursos,secret-scan528 locales aprobados.
