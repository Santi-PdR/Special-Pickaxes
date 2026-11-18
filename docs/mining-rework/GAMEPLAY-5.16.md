# 5.16 — recuperación sostenida, arquitectura y HUD

## Alcance implementado y validación completa aprobada

La petición global sigue siendo una reforma de unas 50 mejoras notorias, no una suma de tests/atajos/fotogramas. Esta tanda añade mecánicas y presentación funcional; no declara esa reforma completa.

### Ocaso: recuperación activa de botín

La secundaria conserva su selección de hasta 256 objetos libres/propios dentro de 48 bloques y su impulso inicial. Ahora mantiene una recuperación de hasta ocho segundos: vuelve a dirigir los objetos seleccionados hacia la posición actual del lanzador. No hace nuevos barridos de entidades cada tick, no teletransporta, no desactiva colisiones, no fuerza recogida y no cambia el propietario. No busca ruta alrededor de muros.

Cada intervención revalida el objeto, propietario, distancia, dimensión, stack de herramienta y permiso de claim. Un cambio durante el callback invalida esa intervención. Cancelar, cambiar de stack de herramienta, morir, cambiar de dimensión o desconectarse elimina el trabajo. Moverse más de un radio desde el origen también lo termina. La recogida y los retrasos de pickup siguen siendo de Minecraft.

### Iridio: recuperación sostenida sólo de sus menas

Misma recuperación finita, pero únicamente para drops que conservan la marca de mena de Iridio y la UUID del minero original. Conserva el límite de 96 objetivos y radio 48. No se convierte en un imán de todo el inventario tirado por otros.

Ambas recuperaciones comparten presupuestos: 32 objetivos por jugador/tick, 192 globales, 32 trabajos simultáneos, máximo 256 referencias por trabajo; turnos rotatorios y sin partículas de mantenimiento. La atracción no es un aura pasiva.

### Telar del Mundo: construcción arquitectónica pagada

- Con bloques completos, la calzada plana anterior permanece igual, incluso mirando arriba/abajo.
- Con escaleras vanilla secas, crea una escalera ascendente orientada al avance.
- Mirando más de 35 grados hacia abajo, las escaleras crean un descenso con orientación invertida.
- Hasta 64 piezas, una pieza pagada de la mano secundaria por posición. No usa reservas del inventario, duplica ni sobrescribe contenedores.
- Dos bloques de gálibo y límite de construcción verificados al planificar y otra vez al colocar. No salta obstáculos.
- El permiso especial de estado orientado se limita a escaleras vanilla de Telar, pagadas desde la mano secundaria: no se amplía la transmutación de Crisol a estados arbitrarios.

### HUD funcional de los trece picos

Panel compacto con dos enfriamientos y barras independientes (principal/secundaria), estado real de cola, pasos procesados/pendientes, recuperación de botín restante y dominio activo cuando corresponde. Usa NBT de presentación sincronizado por el servidor cada diez ticks, no intenciones ni coordenadas confiadas al cliente. No añade protocolo de red nuevo ni envía inventarios adicionales por cuenta propia. Se oculta en pantallas/menús, con F1, para espectadores o resoluciones GUI menores de 240 píxeles. Sus contadores no autorizan acciones y pueden tener hasta medio segundo de retardo.

## Pruebas añadidas

Geometría unitaria de 64 piezas para las cuatro direcciones y tres pendientes; límites y selección de pendiente. Diez GameTests: dirección sostenida, seguimiento del lanzador, cambio de stack, nuevo veto de claim, cambio de propietario, marca Iridio, callback que mueve el objeto/cancelación, ascenso, descenso, gálibo ante contenedor y telemetría autoritativa (algunos escenarios comparten test). No se cuentan como mejoras jugables.

## Estado final

Runtime `b904be503ed9a19cc9e34039f57d949abed56928`, [CI 44383199553](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44383199553): SUCCESS. **171 JUnit**, cero fallos/errores/omisiones; **410 GameTests** obligatorios; cliente gráfico (32 escenas/64 tooltips), servidor empaquetado y logs estrictos aprobados. Local: 131 pruebas Python sin fallos, una omitida por falta de Java; 25 pruebas de recursos correctas.

JAR `special-pickaxes-1.20.1-5.16.0.jar`: **793992 bytes**, SHA256 `623c2ca70156c84649fe2ef09b1e6763f21005195a7d662bd546c85c1a9c2caf`. Artefacto personal autenticado `14465283390`, evidencia completa `14465963235` (7 días), sin release pública. Evidencia recuperada temporalmente a `/tmp/sp516green`.

HUD inspeccionado en dos capturas reales del mismo run, sin retoques, a 640×360: `review/hud-palimpsest-5.16.0.jpg` y `review/hud-eventide-5.16.0.jpg`, originales `build/client-smoke/portrait-palimpsest.jpg` y `portrait-eventide.jpg`. Textos y barras caben, mira central despejada. Esta muestra muestra estado listo/inactivo, no certifica todas las escalas ni cada estado del HUD. Las nuevas capturas `hud-<escena>` existen en el artefacto completo/image-manifest; no fueron incluidas por el export compacto de Checks.

No hay texturas nuevas en 5.16: se conservan los dos originales Malum y once Mythic Metals ya integrados. No se declara compatibilidad Windows/Ghouls ni aprobación estética del usuario. El inventario acumulado explícito es `IMPROVEMENTS-47-AUDIT.md`: 39 identidades audiovisuales por pico y ocho cambios de arte/sistemas, **no 47 mecánicas distintas**.

## Primera validación

Run 44380776387: compilación Java y 171 JUnit sin fallos; 410 GameTests obligatorios aprobados. Falló el smoke gráfico: `palimpsest-vein` rechazó la activación y agotó su espera. La captura muestra la mena a la derecha, fuera de la mira, después del ciclo de inventario/manual de Robot. No se atribuye el fallo a la mecánica sin prueba adicional.

El fixture de cliente ahora exige un handshake explícito después de cerrar el manual: solicita restaurar una vez la pose original, espera un mensaje confirmado por el cliente y sólo entonces pulsa la tecla real de activación. No invoca la habilidad, no simula éxito, no reintenta activaciones ni cambia el raycast de producción. Se añade captura `hud-<escena>` en esa pose. El gate falla si no llega el acuse o corresponde a otra escena. Pruebas Python del orden actualizadas (131 locales, una omisión por Java ausente). La segunda validación completa pasó; no se cambió ninguna habilidad para resolver este fallo del fixture.
