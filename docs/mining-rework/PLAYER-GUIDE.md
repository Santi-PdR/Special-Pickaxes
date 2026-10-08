# Guía del catálogo 5.2.0

R activa la habilidad principal; X la alternativa; C cambia de modo; I abre el
Manual con la textura, pasiva y habilidades. H/J activan las dos habilidades
Curios si tienes un pico equipado en ese slot. B arma una selección regional,
los clics izquierdos fijan esquinas, Enter confirma solo cuando aparece LISTO,
V cancela y K pausa/reanuda. R también reanuda un trabajo pausado por drops.

Todos los picos dan Visión nocturna y una pasiva propia mientras los sostienes
en la mano principal o los llevas en Curios.

| Pico | Habilidades principales y alternativas |
|---|---|
| Palimpsest | Extrae vetas conectadas; X activa Prisa V y Regeneración III. |
| Choir of Faults | Abre un túnel seguro 2×2 de 20 bloques hacia delante; X protege y da visión nocturna a aliados cercanos. |
| Eventide — La Noche que Pesa | Mantiene el campo gravitatorio; X aplica telepatía nocturna y Brillo a hostiles cercanos. |
| Paradox Crucible | Selecciona una región y transmuta geología, incluida calcita y obsidiana; X resalta geología natural cercana. |
| Crown of Stasis | Alterna dominio marcado de 16×16 y aura; el dominio también ayuda a compañeros dentro de sus límites visibles. |
| Loom of the First Quarry | Excava una esfera de geología natural; X activa Pacto Silvestre para curar y alimentar aliados cercanos. |
| Tear of Icarus | Perfora hacia delante; X te impulsa como un ala y evita caídas. Sus efectos usan partículas doradas, de encantamiento y vara de End. |
| Hollow Axiom | Pulso Hueco atrae hostiles hacia un punto, los ralentiza y los revela; X pliega al jugador hasta 18 bloques por espacios libres. |
| Worldbreaker | Conserva sus cortes y selección regional; en el Manual puedes elegir, por icono, qué habilidad alternativa copiar y activarla con X. |
| Exodium Starfall | Lanza minera de 13×9 hasta 48 bloques; X da velocidad, Prisa y Caída lenta. |
| Iridium Lodebreaker | Busca menas de Forge y vanilla en un volumen 29×15×29 por tandas, extrae hasta 192 y hace brillar los drops. |
| Hellspec Infernal Bloom | Mantiene su aspecto y efectos; X apunta a lava para activar Manto Magmático. |

Las habilidades Curios H/J son adicionales al ataque principal y a X; cada pico
tiene dos poderes distintos al llevarlo en el slot `pickaxe`. Curios es opcional.

Las excavaciones se procesan en el scheduler global, verifican cada bloque y
pausan ante congestión de drops. No cargan chunks ni atraviesan fluidos, máquinas,
bedrock, claims o celdas protegidas. El modo regional baja a cero la velocidad de
rotura mientras eliges esquinas, para evitar romper bloques por accidente.
