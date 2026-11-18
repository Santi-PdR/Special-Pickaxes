# Inventario auditable — 47 cambios visuales y funcionales de 5.14 a 5.16

**No son 47 mecánicas nuevas.** Se cuentan identidades primarias/secundarias y timbres por pico porque la petición pidió explícitamente partículas propias por pico. No se cuentan fotogramas, tests, commits, correcciones de CI, límites defensivos ni atajos como mejoras. Tampoco se reciclan los veinte cambios pequeños de 5.13. Esta separación permite juzgar el alcance sin inflar los sistemas jugables.

39 cambios son audiovisuales por pico; los ocho restantes son dos sustituciones de arte, dos sistemas de impacto/remate, dos recuperaciones jugables, arquitectura y un HUD funcional. Es un inventario cercano a cincuenta con predominio audiovisual, no una afirmación de reforma de cincuenta grandes sistemas en todos los aspectos.

## 1–13 · Identidad primaria propia (5.14.1)

| # | Pico | Cambio |
|---|---|---|
| 1 | Alba Eterna | Firma propia: amanecer y estrella; secuencia y trayectoria registradas. |
| 2 | Coro Sísmico | Firma propia: ondas sísmicas; secuencia y trayectoria registradas. |
| 3 | Ocaso Gravitatorio | Firma propia: órbitas; secuencia y trayectoria registradas. |
| 4 | Crisol Alquímico | Firma propia: sello alquímico; secuencia y trayectoria registradas. |
| 5 | Corona del Tiempo | Firma propia: reloj de arena; secuencia y trayectoria registradas. |
| 6 | Telar del Mundo | Firma propia: rama con hojas; secuencia y trayectoria registradas. |
| 7 | Ícaro | Firma propia: alas; secuencia y trayectoria registradas. |
| 8 | Axioma del Vacío | Firma propia: anillo fracturado; secuencia y trayectoria registradas. |
| 9 | Rompemundos | Firma propia: fractura; secuencia y trayectoria registradas. |
| 10 | Exodio Estelar | Firma propia: estrella con estela; secuencia y trayectoria registradas. |
| 11 | Iridio Rompevetas | Firma propia: cristal facetado; secuencia y trayectoria registradas. |
| 12 | Flor Infernal | Firma propia: flor de cinco puntas; secuencia y trayectoria registradas. |
| 13 | Desgarraestratos | Firma propia: cortes paralelos; secuencia y trayectoria registradas. |

## 14–26 · Identidad secundaria propia (5.14.1)

| # | Pico | Cambio |
|---|---|---|
| 14 | Alba Eterna | Firma secundaria: estratos en chevrón; trayectoria y silueta distintas de su primaria. |
| 15 | Coro Sísmico | Firma secundaria: columnas quebradas; trayectoria y silueta distintas de su primaria. |
| 16 | Ocaso Gravitatorio | Firma secundaria: embudo de atracción; trayectoria y silueta distintas de su primaria. |
| 17 | Crisol Alquímico | Firma secundaria: matriz de transmutación; trayectoria y silueta distintas de su primaria. |
| 18 | Corona del Tiempo | Firma secundaria: corchetes de suspensión; trayectoria y silueta distintas de su primaria. |
| 19 | Telar del Mundo | Firma secundaria: entramado; trayectoria y silueta distintas de su primaria. |
| 20 | Ícaro | Firma secundaria: chevrones de ascenso; trayectoria y silueta distintas de su primaria. |
| 21 | Axioma del Vacío | Firma secundaria: grieta doble; trayectoria y silueta distintas de su primaria. |
| 22 | Rompemundos | Firma secundaria: estrella múltiple; trayectoria y silueta distintas de su primaria. |
| 23 | Exodio Estelar | Firma secundaria: cono descendente; trayectoria y silueta distintas de su primaria. |
| 24 | Iridio Rompevetas | Firma secundaria: cristales convergentes; trayectoria y silueta distintas de su primaria. |
| 25 | Flor Infernal | Firma secundaria: columnas de fuego; trayectoria y silueta distintas de su primaria. |
| 26 | Desgarraestratos | Firma secundaria: plano cortado; trayectoria y silueta distintas de su primaria. |

## 27–39 · Sonido secundario reconocible (5.14.1)

Son sonidos existentes de Minecraft, no audio original ni importado. Cada secundaria usa un sonido diferente del primario de su pico.

| # | Pico | Timbre secundario |
|---|---|---|
| 27 | Alba Eterna | Resonancia de amatista. |
| 28 | Coro Sísmico | Rotura de pizarra profunda. |
| 29 | Ocaso Gravitatorio | Teletransporte. |
| 30 | Crisol Alquímico | Extinción de lava. |
| 31 | Corona del Tiempo | Desactivación de baliza. |
| 32 | Telar del Mundo | Colocación de azalea. |
| 33 | Ícaro | Aleteo de dragón. |
| 34 | Axioma del Vacío | Agotamiento de ancla. |
| 35 | Rompemundos | Impacto de yunque. |
| 36 | Exodio Estelar | Lanzamiento de cohete. |
| 37 | Iridio Rompevetas | Recogida de experiencia. |
| 38 | Flor Infernal | Disparo de blaze. |
| 39 | Desgarraestratos | Afiladora. |

## 40–47 · Arte, ejecución, mecánicas y presentación funcional

| # | Versión | Cambio |
|---|---|---|
| 40 | 5.15.0 | Alba Eterna: original Spellweaving de Malum, importado sin modificaciones. |
| 41 | 5.15.0 | Axioma del Vacío: original Soul Stained Steel de Malum, importado sin modificaciones. |
| 42 | 5.15.0 | Impactos custom en el frente de excavación durante trabajo activo, con geometría por estilo. Se cuenta como un sistema, no trece. |
| 43 | 5.15.0 | Remate custom al completar trabajos, no en cancelaciones. Se cuenta como un sistema, no trece. |
| 44 | 5.16.0 | Ocaso: recuperación sostenida de botín hasta ocho segundos, en lugar de un impulso aislado. |
| 45 | 5.16.0 | Iridio: retorno sostenido de sus menas marcadas, siguiendo al lanzador y conservando procedencia. |
| 46 | 5.16.0 | Telar: construcción de escaleras pagadas ascendentes/descendentes con orientación y gálibo correctos. Se cuenta como una mejora, no dos. |
| 47 | 5.16.0 | HUD de los trece picos: dos cooldowns, estado/progreso de cola, recuperación de botín y dominio. Se cuenta como un sistema, no una mejora por indicador. |

## Evidencia y límites

- 5.14.1: runtime `8722f0c4b59a419da6637cc1c444be06a83d52f0`, CI 44367188726 aprobada; 166 JUnit/400 GameTests, cliente y servidor empaquetado.
- 5.15.0: runtime `a527ffbfd83931c57af9266e0865ef3eca3e60d1`, CI 44375291707 aprobada; 168 JUnit/400 GameTests, cliente y servidor empaquetado; dos capturas reales de arte inspeccionadas.
- 5.16.0: runtime `b904be503ed9a19cc9e34039f57d949abed56928`, CI 44383199553 aprobada: 171 JUnit, 410 GameTests, cliente gráfico y servidor empaquetado. El candidato anterior pasó Java/GameTests pero falló el smoke gráfico con una escena desalineada; no se promovió. HUD inspeccionado en dos capturas reales a 640×360.
- Los campos que no ejecutan cola no usan impactos/remates de cola. No se afirma revisión visual de todas las animaciones, aprobación estética del usuario, compatibilidad Windows/Ghouls ni permiso público de redistribución de arte externo.
- Se mantienen once texturas Mythic Metals antiguas; no se cuentan como nuevas. Ver procedencia exacta y derechos en ASSET-PROVENANCE.json y los avisos incluidos.
