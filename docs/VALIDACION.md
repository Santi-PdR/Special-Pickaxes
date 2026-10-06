# Validación 3.0.0

La autoridad es el run de Forge verification del commit del JAR, no un informe
histórico. Manifiesto, RELEASE.json y SHA256SUMS deben coincidir.

Se conservan los 23 GameTests de 2.0 y se amplía su registro a 14 herramientas.
Nuevas pruebas: herramienta triple, niveles altos, selecciones y confirmación,
pausa/reanudación, intercambio rotado, checkpoint pagado, dos regiones de 50.000
celdas simultáneas, cancelación, cambio de herramienta, logout/muerte/dimensión,
chunk ausente, cambios después del análisis, entidades de bloque, protecciones,
plano pagado y bóveda geométrica.

JUnit: traversal y geometría originales, tamaños inclusivos pequeños/medianos/
grandes, saturación de coordenadas, biyección de los seis transformadores,
incompatibilidad y cálculos de encantamientos altos.

Cliente: Minecraft/Forge real bajo Xvfb, no imágenes simuladas. Galería de 14
artefactos, fixture de servidor que recorre cada herramienta, selección/preview,
modos, tooltip renderizado con la API real, Efficiency 1000 y captura en tercera
persona. La fixture conduce el estado de servidor; **no es una prueba automatizada
de todas las pulsaciones físicas ni de todos los poderes en multiplayer**.

Servidor: instalación limpia de Forge con el JAR reobfuscado y arranque/guardado/
parada. Recursos: JSON, traducciones, modelos/sprites distintos, animaciones, tags,
ausencia de recetas y worldgen. Logs y diff pasan gates estrictos.

Límites explícitos: no se encontró `test-1` en el sandbox. No se accede a `siege`.
El JAR se entrega listo para instalar en test-1, pero no se confunde esa prueba
pendiente con CI. Tampoco se certifican claims externos, los 122 mods de Siege,
latencia multiplayer ni todos los mods que alteran loot o encantamientos.
Los tests de dos trabajos grandes verifican presupuesto/cursor, no constituyen
un benchmark completo de TPS para una instancia con muchos jugadores.
