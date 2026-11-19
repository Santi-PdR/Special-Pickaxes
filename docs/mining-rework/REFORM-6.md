# Reforma mayor 6.0 — contrato de implementación

## Estado real

**En desarrollo; no es todavía la reforma jugable completa.** El usuario sustituyó el enfoque incremental 5.19. La calzada y el sondeo de 5.x no son el destino de la reforma. No publicar esta alpha como final ni reutilizar la certificación de 5.18 para ella.

Primera base implementada: eliminación del renderer HUD; SHIFT no expande tooltips; fallos/cancelaciones breves; catálogo completo `es_uy` junto a `es_es` y `en_us`; manual con tarjeta explícita de comportamiento Curios y render del modelo real aumentado. La tarjeta describe los pasivos actuales, no promete todavía las nuevas variantes. La copia de habilidades de mano no se presenta como copia de pasivos si no existe en runtime.

[Ícaro reformado y validado](ICARUS-6.0.md): alas de fractura, ascenso por velocidad sin TP, Pluma temporal, variante aérea Curios y copia en mano. No quedan el ascensor ni sus anclas de TP.

[Alba reformada y validada](DAWNBREAKER-6.0.md): rayos de ocho brazos, nova de radio 12, Glowing de drops nativos y Curios, defensa solar del 65%. Sondeo y guía de mineral retirados.

[Coro reformado y validado](FAULTSONG-6.0.md): abanico sísmico, cascada de cuatro fallas, réplica Curios de seis cicatrices, copia en mano y guard de slot durante callbacks. La guía muestra ahora el detalle equipado real de los trece picos.

[Ocaso reformado y validado](EVENTIDE-6.0.md): eclipse horizontal/creciente vertical, horizonte anular y réplica Curios tras una mena, sin imán como poder principal. Copia en mano respeta el modo seleccionado.

Pendiente: reemplazo de las otras 7 identidades, nuevo sistema de activación Curios/copia, rediseño visual completo del manual, nuevos VFX por habilidad, pruebas de esas mecánicas y publicación final con shell directo. Las otras habilidades de 5.x siguen presentes en esta base salvo los cambios de interfaz; **la transmutación y construcción antiguas todavía no se han retirado**.

## Invariantes obligatorios

- Modelos/texturas de los ítems inmutables, hashes en `REFORM-6-FROZEN.json`. El render de la GUI reutiliza el ítem, nunca inventa arte nuevo.
- Iridium Lodebreaker y Strata Ripper conservan activas, secundarias y pasivos. Congelar clases dedicadas no basta: conservar además pruebas de comportamiento al cambiar dispatch común.
- Sin HUD persistente, ni tooltips largos al pulsar SHIFT. Explicaciones en I, incluidas mano, Curios, disparador, alcance/duración y controles reales.
- Curios opcional, slot `pickaxe` para los 13. Ninguna activación desde inventario común. Validar dueño, slot, cooldown de servidor, cambio de dimensión, muerte y retirada. Selección de copia también desde el ítem equipado, no sólo mano.
- VFX personalizados por acción, sin sustituir iconos aprobados. No auras cosméticas perpetuas. Los dominios activos sí deben delimitar su alcance y anunciar fin.
- Claims/protecciones, contenedores, chunks ausentes, presupuestos globales, drops y durabilidad siguen siendo límites de seguridad aunque el poder sea OP.
- ES/EN completos, también `es_uy`. Evitar cadenas de error con listas de condiciones o ayuda larga en actionbar.

## Dirección de las 11 identidades (Ícaro, Alba, Coro y Ocaso implementados)

| Identidad | Poderes en mano | Variante Curios |
| --- | --- | --- |
| Dawnbreaker / Alba — implementado | Ocho rayos de 24 bloques y nova de radio 12, Glowing de drops, ningún radar | Botín nuevo brillante, curación/velocidad y defensa solar del 65% cada 8 s; ver DAWNBREAKER-6.0.md |
| Faultsong / Coro — implementado | Abanico de 32 de alcance hasta 49 de ancho y cascada de cuatro fallas verticales | Réplica sísmica de 99 celdas tras seis cicatrices naturales; recarga 8 s y lease de slot; ver FAULTSONG-6.0.md |
| Eventide / Ocaso — implementado | Eclipse horizontal/creciente vertical de radio 18, horizonte anular de radio 15–24 | Anillo de 312 celdas tras una mena natural, cada 8 s, con lease de slot; ver EVENTIDE-6.0.md |
| Paradox / Crisol | Ruptura diferida y detonación de fracturas almacenadas, nunca transmutación | Eco diferido de bloques minados |
| Stasis / Corona | Área de ruptura suspendida y liberación simultánea | Ventana temporal defensiva; dominio activo con contorno y caducidad visibles |
| Worldloom / Telar | Desmontar estratos de un gesto y barrido de superficie, sin construcción de puentes | Réplica de barrido al completar una racha |
| Icarus — implementado | Alas de fractura sin mover al jugador; impulso vertical progresivo y Pluma de 6 s | Pluma aérea añadida al planeo sin impulso automático; ver ICARUS-6.0.md |
| Void / Axioma | Implosión de roca y corte de vacío penetrante y destructivo | Fractura de vacío desde el bloque minado |
| Exodium | Lluvia estelar dirigida y cráteres encadenados | Réplicas estelares sobre minería |
| Hellspec | Erupción infernal y frente de ruptura volcánica | Contraerupción al recibir daño y pulso minero térmico |
| Worldbreaker | Cataclismo propio y segunda ruptura suprema, además de copiar | Variante suprema propia más imitación real de la variante Curios seleccionada |

Los valores finales y las geometrías deben definirse con pruebas y presupuesto, no copiar la misma esfera cambiando el color. Salvo Ícaro, Alba, Coro, Ocaso, Paradox, Stasis y Worldloom, los nombres de esta tabla son dirección de diseño, no contenido terminado.

## Validación requerida antes de entrega final

Pruebas por identidad para mano y slot; copia nativa/externa en ambos contextos; cancelación, retirada, cooldown y claims; no TP de Ícaro; no radar de Alba; no transmutación de Paradox; intactos Iridium/Strata; cliente real ES/EN/es_uy, tooltips iguales con SHIFT, manual desplazable y sin recortes. Servidor con JAR empaquetado y release directa verificada antes de entregar el shell.

## Verificación de esta primera base (no de las habilidades pendientes)

Fuente `5d0c8491d72f13de1fd6b702267f75b90d08af39`, [CI 44433168671](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44433168671): **SUCCESS**. 179 JUnit sin fallos/errores/omisiones, 426 GameTests obligatorios, cliente 32 escenas/64 tooltips, servidor con JAR empaquetado y logs estrictos aprobados. 137 Python locales (una omisión sin Java) y 25 recursos correctos.

Primer run `44429871556` falló por un import Component retirado durante la limpieza del HUD. Se restauró; `44430258038` pasó completamente. La inspección posterior descubrió numeración 03 duplicada en el manual; se corrigió y la ejecución final anterior volvió a pasar. No se relajaron las pruebas de habilidades para ocultar fallos; sólo se actualizaron los contratos de tooltip y captura del HUD que el usuario pidió retirar.

Capturas auténticas ES de la ejecución final, 1280×720, inspeccionadas: manual con tarjetas 01–04, Curios y modelo real; tooltip SHIFT compacto sin referencia larga. No equivalen a revisión visual en todos los idiomas/escalas. Archivos:

- `review/manual-curios-6.0.0-alpha.1-es.jpg`: SHA256 `5bb9cdd1d5195fff396bb85a07c80ad39b56e0f93eb0e604083739d713ee935f`.
- `review/tooltip-shift-6.0.0-alpha.1-es.jpg`: SHA256 `4dd52370cae1c64179efea009bfd7b5539e2281f7d28ca7d4d838ad9e346cb60`.

## Paradox alpha.6 validado

Fuente `20e2a1124560a1b1f21c024da83fcf496b2c1bb6`, CI `44515980736` SUCCESS: 182 JUnit, 462 GameTests, cliente 32 escenas/64 tooltips y servidor empaquetado. Fractura regional, retícula causal, ecos Curios y copia en mano implementados sin transmutación pública. Seis identidades y copia Curios global siguen pendientes. [Evidencia y límites](PARADOX-6.0.md).

## Stasis alpha.9 validado

Runtime `0227115609126603e9237ec2bb9d2eeee80429e6`, CI `44549253542`: SUCCESS, 189 JUnit / 470 GameTests / cliente y servidor. Dominio ampliado 33×17×33, bordes y reloj visibles, secundaria reloj de arena, corte Curios y copia en mano con modo correcto. Captura activa inspeccionada. [Alcance y límites](STASIS-6.0.md). Quedan cinco identidades y copia Curios global.

## Worldloom alpha.12 validado

Fuente `dde36814a8a0cef56166e509fd42e63d2e9ebe6c`, CI `44609459397`: SUCCESS, 195 JUnit / 479 GameTests / cliente y servidor. Cantera escalonada, cosecha expuesta sin construcción y raíces Curios con lease. Cantera real y guía ES inspeccionadas; límites en [WORLDLOOM-6.0.md](WORLDLOOM-6.0.md). Quedan Axiom, Exodium, Hellspec, Worldbreaker y copia Curios global.
