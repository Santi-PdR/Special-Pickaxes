# Revisión amplia — 50 puntos de trabajo

No son 50 mejoras terminadas. Este inventario separa los **20 cambios aplicados en el primer lote** de **30 propuestas pendientes**. Este primer lote quedó validado por CI `44234259503` sobre `6aed2b07962f8511d86c4061a1b8e1181774c033`: 161 JUnit sin fallos/errores/omitidos, 400 GameTests, cliente gráfico y servidor empaquetado. Localmente: 125 Python y 25 recursos. La validación automatizada no equivale a haber ejercitado físicamente todas las teclas en todas las escalas. Las propuestas requieren revisión antes de implementarse y no prometen interfaces ni funciones inexistentes.

## Primer lote: 20 cambios aplicados

1. Tab recorre el selector de copias sin activar una copia.
2. Mayús+Tab permite recorrerlo hacia atrás.
3. Inicio lleva al primer pico cuando el selector tiene foco.
4. Fin lleva al último pico cuando el selector tiene foco.
5. Retroceso abandona el foco del selector sin cerrar el manual.
6. El foco usa un borde completo, no solo dos líneas horizontales.
7. El pie muestra el pico enfocado como vista previa, incluso sin espacio para la etiqueta lateral.
8. La opción nativa tiene una vista previa explícita de «Habilidades propias».
9. Los IDs de copia inválidos, retirados o autorreferentes vuelven a la presentación nativa, sin mostrar claves de traducción inventadas.
10. El hover respeta los huecos no clicables entre iconos.
11. Las etiquetas laterales largas usan puntos suspensivos en vez de un recorte silencioso.
12. El manual de un Rompemundos que no está en la mano explica que debe sostenerse para elegir copia.
13. La tarjeta de controles explica navegación, confirmación y salida del selector.
14. El caché de tarjetas incluye la disponibilidad del selector al cambiar de herramienta.
15. La entrada de teclado termina un arrastre de scrollbar anterior.
16. La rueda también termina el arrastre anterior para no mezclar posiciones.
17. La huella de contenido se construye con StringBuilder, sin concatenaciones crecientes por acción.
18. Catálogo de modos inmutable: consultas de cantidad/clave sin crear arrays y API histórica con copia defensiva.
19. La selección regional copiada consulta el modo guardado en la herramienta, no el modo del otro pico en los datos del jugador.
20. El fixture de salida del ascensor de Ícaro prepara su piedra como natural explícitamente y separa los tres fallos posibles en aserciones diagnósticas. El fixture se elevó fuera del terreno compartido y ahora comprueba el presupuesto y la segunda celda antes de salir. Pasó la suite completa sin cambiar la lógica del ascensor ni debilitar sus aserciones finales.

Pruebas añadidas: contrato de orden/compatibilidad de todos los modos, resistencia a mutación externa y modos enteros extremos; comprobaciones de navegación sin paquetes, ayuda localizada y caché de contexto. Las pruebas no se cuentan como mejoras independientes para completar artificialmente 50.

## Pendientes: 30 puntos para evaluar e implementar por lotes

21. Desplazamiento horizontal del selector mediante arrastre.
22. Indicadores claros de más opciones fuera del área visible.
23. Confirmación visual de la copia solo tras respuesta del servidor.
24. Mostrar el modo de la copia en el propio selector sin saturarlo.
25. Anunciar el foco del selector mediante el narrador de Minecraft.
26. Revisar contraste de todos los colores de habilidad sobre el fondo del manual.
27. Evaluar tamaño de texto configurable sin romper el diseño compacto.
28. Capturas gráficas reales en inglés, además de español.
29. Capturas reales en más escalas de GUI.
30. Capturas de pantallas estrechas con contenido desbordado.
31. Mostrar nombres completos recortados de forma accesible sin tapar tarjetas.
32. Revisar descripciones secundarias para todos los modos copiados.
33. Revisar claridad de duraciones y recargas en tooltips.
34. Evaluar un indicador opcional y discreto de progreso de excavación.
35. Diferenciar mejor pausa manual, chunks y presión de drops.
36. Explicar mejor análisis y confirmación de regiones sin añadir relleno.
37. Revisar volumen y cadencia de sonidos de confirmación/cancelación.
38. Auditar textos antiguos de pasivas para eliminar promesas cosméticas retiradas.
39. Medir asignaciones y tiempo de render del manual con un benchmark real.
40. Medir excavaciones en escenarios vacíos, densos y protegidos.
41. Ampliar las pruebas de sondeo junto a límites de chunks descargados.
42. Ampliar pruebas de equidad del planificador con varios jugadores.
43. Añadir pruebas de paquetes con datos extremos/malformados.
44. Extender las pruebas de replay y secuencias en reconexión.
45. Probar cambio de dimensión durante una solicitud de copia.
46. Probar sustitución de herramienta entre elección y respuesta de copia.
47. Verificar que los rechazos del servidor no se muestran como éxito ni provocan reintentos automáticos.
48. Ampliar pruebas de persistencia de procedencia de bloques al recargar.
49. Ejercitar recarga de recursos/idioma con el selector enfocado.
50. Auditar aislamiento de los fixtures de mundo que dejan bloques, entidades o trabajos fuera de su estructura.
