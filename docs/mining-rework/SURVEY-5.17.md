# 5.17 — guía temporal de sondeo de Alba Eterna

## Implementado y validado

El sondeo conserva su búsqueda activada de hasta 32 menas naturales alrededor del bloque apuntado. Ahora publica una **guía de diez segundos al depósito más cercano al centro sondeado**: dirección cardinal, distancia al jugador y desnivel en el HUD. Al moverse el jugador cambia la lectura, no el depósito elegido. Una barra muestra la duración restante. Se cuenta como una nueva mejora funcional, no una mejora por campo del HUD.

No es un radar global, un escáner pasivo ni un contorno a través de las paredes. No hay partículas ambientales nuevas, rayos X, excavación automática ni búsquedas periódicas de otras menas. Conserva radio/capacidad y protecciones del sondeo anterior. Las coordenadas del depósito ya se comunicaban antes por chat; la nueva guía hace utilizable esa información sin copiarla a mano.

### Ciclo de vida

- La guía pertenece al jugador que lanzó el sondeo y al depósito autorizado; otro jugador no hereda su HUD al recibir el pico.
- Repetir la secundaria cancela la guía, incluso si no hay cola de trabajo. Después puede lanzarse otro sondeo cuando termine su cooldown normal.
- Caduca tras 200 ticks. La validación cada diez ticks sólo comprueba el depósito almacenado y **no renueva la duración**.
- Se retira si la mena desaparece/cambia, está marcada como colocada, se descarga, queda a más de 48 bloques, cambia la dimensión, cambia el dueño del stack o un claim deja de permitirla. El cambio puede tardar hasta medio segundo en sincronizarse.
- Al volver a seleccionar un pico guardado se respeta el plazo original, no empieza una guía nueva.
- Rompemundos puede usar la guía al copiar la secundaria de Alba.
- Publicación y actualización revalidan herramienta, actor, posición del depósito y contexto después de callbacks. Guardas de reentrada evitan recursión de los permisos.

### Presentación

Datos de presentación NBT del servidor mediante la sincronización de inventario ya existente. El cliente sólo calcula la lectura; nunca devuelve estas coordenadas como autoridad de minería. El decodificador rechaza otra dimensión/dueño, posiciones lejanas/no finitas y plazos imposibles o desbordados. Mantiene ocultación en menús/F1/espectador y GUI pequeñas. Manual EN/ES conciso, sin atajos nuevos ni relleno.

### Validación añadida

- Cuatro tests Java puros de ocho rumbos, verticalidad, movimiento del observador, caducidad, overflow, dimensión/dueño y datos inválidos.
- Ocho GameTests de secundaria real, cancelación por la acción registrada, caducidad sin renovación, retirada por claim/minado, transferencia, copia de Rompemundos, reentrada y chunks ausentes/callback que mueve al actor.
- El smoke gráfico pulsa la secundaria real de Alba después de confirmar la pose, espera a que el cliente haya leído una guía válida, captura `survey-guide.png/.jpg` y sólo entonces pulsa la primaria real. No se simula activación ni éxito. La captura se incluye en la prioridad del export compacto de evidencia.
- Local: 132 pruebas Python sin fallos, una omitida por falta de Java; 25 pruebas de recursos correctas. Son contratos locales, no sustituyen Forge/cliente real.

## Validación final

Runtime `fc1fff5481e7fff576fb87c1d46fa035b7999dcc`, [CI 44390344841](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44390344841): SUCCESS. **175 JUnit**, cero fallos/errores/omisiones; **418 GameTests** obligatorios; cliente gráfico (32 escenas/64 tooltips), servidor empaquetado y logs estrictos aprobados. Local: 132 pruebas Python sin fallos, una omitida por falta de Java; 25 pruebas de recursos correctas.

El log real confirma `CLIENT_INPUT_SERVER_RESULT_ALT_SKILL accepted=true` y `SURVEY_GUIDE_RENDERED_palimpsest-vein` antes de la captura y de la primaria. La activación/captura no se ha sustituido por una llamada directa de prueba.

Captura real inspeccionada: `review/survey-guide-5.17.0-es.jpg`, 1280×720 (GUI 640×360), copia exacta de `build/client-smoke/survey-guide.jpg` del mismo run. Lectura visible: `Sondeo N · 6m · Y+3`, con tiempo restante; línea y barra caben sin cubrir la mira ni los dos cooldowns. Inspección de una escena en español, no todas las direcciones/escalas. Hash de la captura: `0b9ba34d688fc8d7586adfd2a5bec2e95aeedcb30448ec3ea7341c2863fd7b68`.

JAR `special-pickaxes-1.20.1-5.17.0.jar`: **801313 bytes**, SHA256 `ff7567bce1be636232f037d5c5e4ca05fa64c07b94553f8548faa0e0550779fc`. Artefacto personal autenticado `14468589157`, evidencia completa `14468394369` (7 días), sin release pública. Evidencia temporal `/tmp/sp517green`.

Esta es una nueva mejora funcional, adicional al inventario histórico de 47 hasta 5.16; no se cuentan los tres datos del HUD, pruebas o screenshots como mejoras separadas. No se han integrado más texturas externas ni se afirma revisión visual de cada animación, todas las escalas, Windows/Ghouls o aprobación estética del usuario.
