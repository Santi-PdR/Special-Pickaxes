# 5.16 — recuperación sostenida, arquitectura y HUD

## Alcance implementado (validación pendiente)

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

## Estado

Pendientes compilación, tests Java/GameTests, cliente gráfico, servidor empaquetado e inspección del nuevo HUD. La última versión completamente verificada al comenzar esta tanda es 5.15.0. No hay texturas nuevas en 5.16: se conservan los dos originales Malum y once Mythic Metals ya integrados. No se declara compatibilidad Windows/Ghouls ni aprobación estética del usuario.
