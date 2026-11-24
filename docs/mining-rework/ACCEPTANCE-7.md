# Reforma 7: cotejo de requisitos y evidencia

Fuente integrada probada: `488215bbb9e17c749e7bbc07d234318d243abacd`, Forge verification **45453539106 SUCCESS**: 217 JUnit sin fallos/errores/omisiones, 617 GameTests requeridos y cliente real. Las 617 pruebas no son 617 mecánicas. No se publicó el JAR ni se declara resuelto el rendimiento global.

## Requisitos funcionales

| Requisito | Implementación / evidencia | Alcance y límite |
|---|---|---|
| No modificar modelos/texturas ni originales Iridium/Strata | `tools/test_major_reform_contracts.py` compara los hashes congelados de `REFORM-6-FROZEN.json`; aprobado. | Los dos originales quedan protegidos, no se redefinen silenciosamente para homogeneizarlos. |
| Picos diferentes, sin un mismo megafrente para todos | Once huellas propias en `SignatureExcavation`; `everySignatureHasUniqueBoundedForwardFootprint` y `everyReworkedPrimaryUsesItsBoundedSignatureAndNativeLoot`. | Geometría, límites y entrada real comprobados. La utilidad/balance percibidos no se deducen del número de tests. El ejemplo 6×6 no se convierte en una obligación. |
| Curios no excava ni cambia el mundo | `CuriosEffectActive`, perfiles de efectos por fuente; `equippedEffectsNeverStartMiningOrFlight`, copia de cada perfil y activación física `CURIO_KEY_OK`. | Los efectos pueden mejorar acciones del jugador (Prisa/Salto), pero activar no programa roturas ni fuerza ascenso. Los hooks originales protegidos no se cambian. |
| Paradox no queda en selector ni bloquea velocidad manual | `realParadoxControlStartsWedgeWithoutSelectionOrSpeedLock`. | SELECT rechaza selección retirada; ACTIVATE inicia cuña, cancelar termina el trabajo. No se recupera transmutación. |
| Ícaro asciende físicamente y abre techo sólo durante vuelo sostenido | `IcarianFlight`, controles/lease de fuente y escenario cliente `ICARUS_CEILINGS_OK`. | No se sustituye por teletransporte. Activación Curios da efectos, no inicia este vuelo. |
| Sólo extractor resalta menas/drops apropiados | `WorldSafety.tagIridiumDrop`, `OreDropHighlight` y regresiones de loot no resaltado para Alba/copia/Curios. | No se añade brillo general de bloques. Partículas authored no equivalen a marcar entidades con Glowing. |
| Rompibles apropiados, arenisca/terracota; no líquidos, máquinas o irrompibles | Elegibilidad y cosechabilidad nativas en `NaturalExcavationProgram`/`WorldSafety`; regresión de once firmas sobre terracota, pruebas de barreras/fluidos/tiers. | El loot y la autorización siguen la ruta Forge nativa, no un reemplazo de loot tables. Compatibilidad universal con todos los mods no está demostrada. |
| Arena/grava que cae durante excavación | `signatureHarvestsRealGravelFallingAfterActivation` y seguimiento de celdas ya abiertas. | Seguimiento acotado en huella y tiempo; no escaneo infinito del mundo. |
| No detenerse por abundancia de loot | Presupuesto de cola, no backpressure por densidad; prueba con ocho trabajos y bolsa saturada. | Límites de trabajo/tiempo siguen existiendo; no son pausas por objetos. |
| Bolsa antes de añadir drops al mundo, sin pérdida/duplicación | Contexto de rotura nativa y captura opt-in LOWEST; pruebas emparejadas, Seda/Fortuna, overflow y ticks naturales. | El objeto Java ItemEntity se crea antes del evento. No se promete cero asignaciones ni control de listeners que actúen después. Datos/capacidades especiales siguen al mundo. |
| Capacidad y datos acotados; retirada segura | 64 tipos × 4096; UI de 26 materiales/página, hasta tres páginas, inventario lleno, cursor vacío, preferencia persistente y rechazo de vista obsoleta. | Iridium/Strata conservan ruta de drops original. Captura de habilidad, no imán ni captura manual universal. |
| Interfaz resumida y guía I, sin HUD añadido | Tooltip mínimo y guía; contratos de recursos/manual y cliente con controles reasignados. `POUCH_MENU_OK` físico. | Escenas/tooltips/capturas son validación, no habilidades nuevas. |

## Almacenamiento: cierre del último cambio

- Lista nueva por cada escritura aceptada: mantiene invalidación del menú vivo.
- Copia profunda únicamente de la entrada modificada; entradas ajenas compartidas **sólo para lectura interna**. Ninguna escritura de la API edita una entrada compartida in-place.
- Prototipos se deserializan desde copia de su NBT; entrada/salida, guardado/carga y preview no comparten estado mutable con el almacenamiento.
- Rechazos no copian la tabla entera. Búsqueda por registro no reconstruye prototipos de tipos distintos.
- Aprobados: snapshots previos intactos, 400 operaciones deterministas contra modelo de referencia, 64 tipos/tres páginas y compactación 3→2 con entrega exacta de los 12 tipos de la última página y otros 52 intactos.
- Mutación cruda de `materialPouchV1` por terceros fuera de la API no forma parte del contrato de invalidación. No se promete protección frente a cambios arbitrarios de NBT ajenos.

## Lo que aún NO puede marcarse como acreditado

1. **Mejora causal de FPS/TPS y tráfico real.** Los resultados completos de `PERFORMANCE-7.md` muestran conservación/reducción de entidades, pero los tiempos posteriores tienen deriva fuerte y la última ronda sin bolsa fue más rápida. No seleccionar sólo las muestras favorables. Falta comparación con calentamiento estable, clientes reales y perfiles de red/servidor repetidos.
2. **Balance y utilidad percibida de las once habilidades.** Las formas/bonificaciones son diferentes y funcionales; eso no demuestra por sí solo que el repertorio resulte suficientemente variado/OP/útil en una sesión normal.
3. **Compatibilidad universal de modpacks.** Se preservan callbacks/loot nativos y las regresiones de seguridad, pero no se han probado todos los listeners de loot/equipo de terceros ni hardware del usuario.

Estos límites no se ocultan detrás de un número de pruebas. No publicar alpha.60 ni vender el estado como reforma global cerrada por estar verde. `STATUS.md` fija fuente, tamaño y SHA256 exactos del JAR probado y no publicado.

### Actualización visible con bolsa abierta (577582c)

Cliente real aprobado: captura inicial con 17 diamantes, inserción servidor mediante API de 3 diamantes/5 lingotes, observación de slots tras render durante 10 frames estables y captura final revisada con 20 diamantes/5 lingotes. Sin pulsar slots ni cerrar la GUI entre capturas; sólo después se pulsa Escape y se comprueba conservación/cursor en servidor. Marcadores POUCH_LIVE_RENDER_OK, POUCH_MENU_UPDATED y POUCH_MENU_OK obligatorios. Conexión integrada/interna: no acredita TCP, latencia, bytes de red ni FPS/TPS. El ensayo adicional de codec aa825b5 está aprobado en b3737d8 y no sustituye este cliente.

### Codec nativo de inventario aprobado

CI 45420018800 SUCCESS: 216 JUnit, 615 GameTests requeridos y cliente real 32 escenas/64 tooltips; render vivo de bolsa repetido. ClientboundContainerSetSlotPacket.write/read conserva framing/NBT, 64 tipos y 262144 unidades, snapshot previo tras mutar la bolsa e independencia de copia decodificada. Registro POUCH_SLOT_CODEC: vacío 25 bytes, lleno 3912 bytes, cambio de una cantidad 3912 bytes (262143 unidades). Son bytes del payload de ese paquete, no tráfico TCP/comprimido ni latencia. La tabla completa sigue enviándose en cada actualización del stack; no es sincronización delta. Primera ejecución 45419913390 bloqueada por Maven HTTP429, sin atribuirlo al mod.

### Lectura única por slot aprobada

Fuente 3ba3f77 / CI45438613414 SUCCESS: 216JUnit sin fallos/errores/omisiones, 616GameTests requeridos, cliente32escenas/64tooltips y POUCH_LIVE_RENDER_OK10frames20/5, cierrePOUCH_MENU_OK. SlotView entrega stack separado y cantidad tras una única validación/deserialización; el menú deja de llamar preview+amount para el mismo slot. Lecturas públicas conservan validación; cantidades inválidas se descartan antes de construir el prototipo. Regresión aprobada de4096unidades/icono64/nombre4096, cero/sobrecapacidad/aire/noapilable/tagajeno/lista65 rechazados sin retirada ni reescritura, y restauración válida. No son deltas de red ni una mejora FPS/TPS medida.

### Refresco incremental aprobado

Fuente c0bf7e5 / CI45444309796 SUCCESS: 216JUnit sin fallos/errores/omisiones, 617GameTests requeridos, cliente32escenas/64tooltips y bolsa viva20/5 durante10renders estables, cierre físicoPOUCH_MENU_OK. Tokens de entrada copy-on-write permiten conservar displays detached de materiales no modificados en su slot. Aprobados cambio de una cantidad sin reconstruir el oro, toggle sin reconstruir materiales, append y compactación con limpieza del tail, paginación3→2 y clic obsoleto. El token global se sigue renovando para protección de clics. No se promete protección de mutaciones NBT crudas in-place ni reducción de paquetes/FPS/TPS; sincronización vanilla del stack aún envía tabla completa.

### Base yaw reutilizada en VFX, coordenadas preservadas

488215b / CI45453539106 SUCCESS:217JUnit sin fallos/errores/omisiones,617GameTests y cliente32escenas/64tooltips,bolsa viva10renders20/5. Cast y feedback de excavación calculan forward/right una vez por ráfaga, en lugar de por partícula. Prueba aprobada compara bits de las coordenadas con la fórmula anterior:13formas×2variantes×16puntos×107direcciones. Conserva orden de operaciones, fallback vertical/cero, formas, cantidades, presupuestos y destinatarios; no cambia habilidades/modelos/texturas. No se afirma ahorro de paquetes ni una mejora FPS/TPS medida.
