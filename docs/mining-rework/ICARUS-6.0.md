# Ícaro — reforma 6.0 alpha.2

Estado: implementación validada en CI. No representa las otras diez identidades pendientes.

## Poderes nuevos

- **Principal: alas de fractura.** Dos superficies ascendentes y simétricas de roca/menas naturales, 32 bloques de largo, hasta 37 de envergadura o 49 en modo ancho. Geometría propia: 3820/5740 candidatos, no un túnel rectangular. Usa el presupuesto de WorkQueue, no consulta ni carga chunks ausentes, conserva procedencia de bloques colocados, claims, contenedores, fluidos y tiers. No desplaza al jugador.
- **Secundaria: ascenso de plumas.** Durante 32 ticks, aplica velocidad vertical progresiva de 0,16 a 0,70 bloques/tick. No llama a `setPos`, `teleport` ni `WorldSafety.move`; el movimiento se produce por la física normal con paquetes de velocidad. No excava el techo: hay que abrir primero el espacio con la principal. Comprueba el recorrido de colisión por incrementos de 0,1 bloques y revalida tras callbacks. El impulso cesa al alcanzar 14 bloques por encima del punto de inicio; la inercia y la física posteriores no son una promesa de altitud exacta.
- **Pluma icaria.** Efecto temporal que reduce 80% el daño de caída y permanece 120 ticks desde el último impulso válido. No modifica encantamientos de armadura ni permisos permanentes de vuelo. No es el encantamiento vanilla Feather Falling: es una protección temporal propia con icono y nombre localizados.
- **Curios:** en el aire añade Pluma icaria al planeo existente, sin iniciar impulso automáticamente. El planeo conserva frenado y reparto a aliados. Cuando también está activo el multiplicador de planeo, ambos modificadores de caída se combinan. La copia equipada de Worldbreaker sigue pendiente de su reforma específica; la copia en mano sí usa estos nuevos poderes.
- **VFX:** doble hélice de firmas de plumas custom durante el ascenso, 8 partículas cada tres ticks, dentro del presupuesto compartido de AbilityVfx; nunca una emisión permanente por llevar el objeto.

La referencia del menú I y EN/ES/es_uy describen estos poderes. No se cambió ningún modelo/textura de los picos, ni las clases dedicadas congeladas de Iridium/Strata.

## Ciclo de vida y seguridad

32 vuelos simultáneos como máximo. Cambio de herramienta o copia, muerte, logout, dimensión, montura, sueño, élitros, desplazamiento externo grande, final del tiempo o colisión terminan el impulso. R/X pueden cancelar, sin borrar la protección temporal. Modo también detiene el impulso. No reinicia el impulso al caminar, minar o recibir un tick de Curios. Los callbacks que cambian al actor invalidan la operación; no se sobrescribe su movimiento. El avance sólo se procesa una vez por tick y no admite reentrada.

## Pruebas

Los tests antiguos del ascensor que verificaban teletransporte desde las teclas se sustituyeron por expectativas explícitas del rework. Se retiraron por completo `IcarianLiftProgram`, su ancla `IcarianShaft` y las cinco pruebas unitarias de esa mecánica descartada. Sus tres regresiones de mundo fueron sustituidas por controles de cancelación, no acumulación y revocación de claims del impulso nuevo. También se eliminó el paso Move de las geometrías direccionales antiguas de Ícaro: no queda teletransporte de minería en ese motor.

Validación final: fuente `98f5c06c900461b16cca18d1a177c3d178adf231`, [CI 44442689638](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44442689638) SUCCESS. **177 JUnit**, cero fallos/errores/omisiones; **436 GameTests obligatorios**; cliente 32 escenas/64 tooltips y servidor empaquetado aprobados. El total unitario baja respecto a alpha.1 porque se retiraron cinco pruebas del ancla de TP descartada y se añadieron tres pruebas nuevas de geometría/velocidad; no hay tests omitidos en Java. Local: 138 Python (una omisión por falta de Java), 25 recursos, escaneo de secretos y diff correctos.

Prueba real adicional: cliente en supervivencia, cambio a tercera persona, pulsación física X mediante Robot y paquete normal. El servidor observa movimiento por física antes de dar éxito: `ICARUS_FLIGHT_OK rise=14.215452604234258 maxStep=1.3076000118255706`. El último valor es el máximo salto entre muestras de posición del servidor (puede agrupar paquetes), no la velocidad vertical impuesta por tick, acotada a 0,70. El límite de 14 corta el impulso: la inercia permite sobrepasarlo ligeramente. Se exige recorrido mayor que 3 y muestras menores que 2,5, además del efecto temporal al terminar. El fixture sólo teletransporta al preparar/restaurar escenarios, nunca durante el vuelo evaluado.

Captura ES real `review/icarus-ascent-6.0.0-alpha.2-es.jpg`, 1280×720: Ícaro equipado, personaje ascendiendo, plumas custom visibles bajo los pies y ausencia del HUD retirado. Inspeccionada; un fotograma no equivale a aprobación de toda la animación ni de todos los FPS/modpacks/idiomas. SHA256 `dbd3ea48acc40ed5f3535a9887029914d2da2171adc0ab09e80c625326126d1b`.

Historial: el primer fallo fue una aserción antigua del planeo que aún esperaba 50% de reducción en vez de la nueva combinación de 90%; se actualizó únicamente esa expectativa de Ícaro. Otro run falló en la regresión intermitente del ascensor TP descartado; se eliminó el sistema viejo completo y se sustituyeron sus regresiones por las de cancelación/duplicación/claims nuevas, no se mantiene una prueba de un poder retirado como requisito del rework. `44439517571` ya pasó toda la batería y el vuelo real, pero su captura tardía mostraba el escenario siguiente. La captura se movió a la misma invocación Robot que pulsa X (620 ms después), y el run final volvió a pasar y produjo la imagen correcta.

JAR de alpha incompleta, no reforma final: `special-pickaxes-1.20.1-6.0.0-alpha.2.jar`, **824530 bytes**, SHA256 `941f79ecb3cdde57c816d872740533fc82cfa25b8951a355c0066bae3fb1bbe0`. No se publicó como release final.
