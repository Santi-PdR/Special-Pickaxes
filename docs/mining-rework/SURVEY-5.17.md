# 5.17 — guía temporal de sondeo de Alba Eterna

## Implementado, validación completa pendiente

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

Última build completamente verificada antes de esta tanda: 5.16.0. No se han integrado más texturas externas ni se afirma revisión visual de cada animación, todas las escalas, Windows/Ghouls o aprobación estética del usuario.
