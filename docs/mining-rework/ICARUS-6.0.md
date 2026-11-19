# Ícaro — reforma 6.0 alpha.2

Estado: implementación a validar en CI. No representa las otras diez identidades pendientes.

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

Pendiente comprobar resultados: JUnit de geometría/velocidad, GameTests de minería sin desplazamiento, impulso sin escritura de posición, techos/fluidos, retirada de herramienta, callback de teleport, copia/cancelación, Pluma temporal y variante pasiva. Smoke gráfico general comprueba principal; todavía no prueba un vuelo físico completo con un jugador humano ni el aspecto del ascenso en todos los FPS/modpacks.
