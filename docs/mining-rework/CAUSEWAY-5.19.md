# 5.19.0 — Telar: calzadas y escalinatas de tres bloques

**Estado: implementación pendiente de CI; no usar resultados anteriores como validación.**

Secundaria de Telar agachado: calzada de tres bloques de ancho, hasta 64 filas / 192 celdas. Sin agacharse conserva el ancho de un bloque. Material desde la mano secundaria; bloques completos planos, escaleras vanilla ascendentes o descendentes mirando abajo. Cada celda consume una unidad en supervivencia. No se extraen materiales del inventario principal.

Se comprueba toda la fila durante la planificación: un obstáculo lateral o techo excluye esa fila y termina el trazado. La ejecución sigue siendo incremental, no una transacción de filas: si se agotan materiales, cambia un claim o aparece una obstrucción, puede quedar la última fila incompleta. Centro primero; detención al primer fallo, sin saltar a filas posteriores ni colocar gratuitamente.

Las calzadas anchas exigen dos bloques de gálibo. Se revalida durante la transacción de colocación y después de los callbacks de Forge; si un callback añade techo, se revierte únicamente el suelo tentativo y no se cobra. Sin sobrescribir construcciones, máquinas o fluidos ni cargar chunks ausentes. Los checks y presupuestos de WorkQueue y WorldSafety continúan activos.

La copia de Telar en Rompemundos conserva la misma técnica. Sin cambio de IDs/NBT de habilidades, sin nuevo arte de picos ni partículas permanentes. Una mejora funcional de arquitectura, no seis mejoras por sus tests.

Pruebas nuevas: geometría 4 orientaciones × 3 pendientes × 64 filas; GameTests para calzada, ascenso, descenso, obstáculo lateral, techo sobrevenido y techo instalado por callback. No se certifica aquí Windows, Ghouls ni todos los modpacks o escalas de GUI.
