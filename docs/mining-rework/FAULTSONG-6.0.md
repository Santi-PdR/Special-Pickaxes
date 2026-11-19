# Coro / Faultsong — alpha.4, validación pendiente

Arte de los picos inalterado; Iridium y Strata conservan sus clases dedicadas.

## Identidad nueva

- **Abanico sísmico**, primaria: 32 bloques desde el bloque natural apuntado, de 5 a 49 de ancho, 5 de alto. 5310 celdas candidatas, avance por frentes desde su centro. No depende de memorias ni excava un túnel de sección constante.
- **Cascada de fallas**, secundaria: cuatro fracturas verticales de 41 de ancho y 25 de alto, separadas por 6 bloques. 4100 celdas candidatas; preserva los espacios entre fallas. La secundaria repetida cancela el trabajo.
- **Curios — réplica de minería**: seis cicatrices naturales confirmadas, con pausas de hasta cinco segundos, producen una cruz sísmica de 99 celdas (17 bloques por brazo, tres de alto). Recarga de ocho segundos. Conserva Enfoque y apoyo minero aliado; no sustituye su identidad por una mejora de velocidad.
- Worldbreaker copia en mano ambas geometrías nuevas. La copia global de variantes Curios sigue pendiente.
- Partículas custom existentes del Coro formando un peine sísmico sobre el frente activo; no se modifican texturas ni modelos ni se agrega una aura de equipamiento.
- Textos `en_us`, `es_es`, `es_uy`, catálogo de modos Java y catálogo de escenas Python sincronizados en `fault_fan`.

## Límites y seguridad

Cada inspección consume presupuesto del scheduler compartido y respeta su límite configurable; las cantidades son candidatos, no garantía de drops. Minería nativa con herramienta realmente sostenida, durabilidad, claims, chunks cargados, procedencia, inventarios y fluidos. Conserva la contrapresión de drops.

Las réplicas requieren la misma instancia en el mismo slot durante toda la ejecución. Un guard de transacción scoped revalida esa fuente después de callbacks de habilidad y dentro del evento nativo de rotura, sin cambiar las rutas no equipadas. Retirar o reemplazar el Curios corta su réplica. No procesa roturas de la propia cola, ni bloques colocados, ni minas que no se confirman como aire. Mapa de rachas acotado a 256 dueños y limpiado con el ciclo de observaciones; no hay escaneo mineral.

Las geometrías históricas de galería se conservan como utilidades internas con pruebas de seguridad, pero ya no son las habilidades públicas de Coro. No presentar sus pruebas antiguas como pruebas de la nueva identidad.

## Comprobación

Pendiente de CI: compilación/JUnit, GameTests nuevos de mano/copia/Curios/callbacks, regresiones generales, cliente real y servidor empaquetado. Python y recursos locales comprobados antes del push. No publicar como reforma completa ni afirmar aprobación estética sin una captura de la acción inspeccionada.
