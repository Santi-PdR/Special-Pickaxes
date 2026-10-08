# Guía del catálogo 5.6.0

R activa la habilidad principal de minería; X la técnica secundaria; C cambia de modo; I abre el
Manual con la textura, pasiva y habilidades. Curios solo aplica las pasivas del pico equipado; no tiene botones propios. B arma una selección regional,
los clics izquierdos fijan esquinas, Enter confirma solo cuando aparece LISTO,
V cancela y K pausa/reanuda. R también reanuda un trabajo pausado por drops.

Todos los picos tienen técnica principal R, técnica alternativa X y una pasiva.
El tooltip resume esas tres líneas; abre el Manual con I para ver instrucciones.
Al equiparlos en Curios solo se aplican sus pasivas.

| Pico | Habilidades principales y alternativas |
|---|---|
| Palimpsest | Extrae vetas conectadas; X activa una onda de empuje contra hostiles cercanos. |
| Choir of Faults | Abre un túnel seguro 2×2 de 20 bloques hacia delante; X abre una perforación corta y segura 2×2. |
| Eventide — La Noche que Pesa | Mantiene el campo gravitatorio; X atrae drops sueltos cercanos. |
| Paradox Crucible | Selecciona una región y transmuta geología, incluida calcita y obsidiana; X desvía proyectiles hostiles cercanos. |
| Crown of Stasis | Alterna dominio marcado de 16×16 y aura; X traslada el dominio conservando su tiempo o detiene amenazas en modo Aura. Los compañeros reciben sus beneficios dentro del borde. |
| Loom of the First Quarry | Excava una esfera de geología natural de radio 8; X atrae un objetivo hostil hacia ti. |
| Tear of Icarus | Perfora hacia delante; X empuja al objetivo que miras; Caída lenta es pasiva en el aire. Sus efectos usan partículas doradas, de encantamiento y vara de End. |
| Hollow Axiom | Excava una malla radial de roca natural y conserva las menas; X pliega un objetivo hostil hacia ti. |
| Worldbreaker | Conserva sus cortes y selección regional; el Manual permite elegir por icono qué técnicas R/X copiar. |
| Exodium Starfall | Lanza minera de 13×9 hasta 48 bloques; X te pliega detrás de un objetivo visible, en espacio seguro. |
| Iridium Lodebreaker | Busca menas de Forge y vanilla en un volumen 29×15×29 por tandas, extrae hasta 192, ilumina sus drops y X los atrae hacia ti. |
| Hellspec Infernal Bloom | Excava geología y menas; la resistencia al fuego es pasiva y X convierte lava fuente apuntada en obsidiana. |

R/X corresponden al pico cuando lo usas en la mano. Al equiparlo en Curios conserva sus pasivas, incluida Visión nocturna, sin habilidades activas adicionales. No se aplica Fuerza. Curios es opcional.

Las excavaciones se procesan en el scheduler global, verifican cada bloque y
pausan ante congestión de drops. No cargan chunks ni atraviesan fluidos, máquinas,
bedrock, claims o celdas protegidas. El modo regional baja a cero la velocidad de
rotura mientras eliges esquinas, para evitar romper bloques por accidente.
