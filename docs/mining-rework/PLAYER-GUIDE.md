# Guía del catálogo 5.1.0

Candidato de la rama `arena/74873685-special-pickaxes`; no es anuncio de release.
R activa, C cambia de modo, B arma una selección, Enter confirma, V cancela y K
pausa o reanuda. Las teclas se pueden reasignar en Controles. SHIFT sobre el pico
muestra el manual del modo seleccionado.

| Pico | Habilidad |
|---|---|
| Palimpsest | Apunta a una mena expuesta. Extrae hasta 192 bloques conectados de esa mena, con drops y Fortuna normales. Da Prisa V y Regeneración II por ocho segundos. El servidor puede cambiar el límite. |
| Choir of Faults | Mina una plantilla con este pico; apunta al inicio y actívalo mirando hacia el túnel. La plantilla rota al frente y solo rompe el estado de bloque que registró. |
| Eventide | Apunta y fija el campo. Cada cuatro bloques de piedra iguales extraídos carga un colapso controlado de hasta seis bloques en la dirección de la mirada. Tiene Prisa III dentro del campo. |
| Paradox Crucible | Selecciona dos esquinas con clic izquierdo y confirma. C transforma hacia piedra, pizarra, granito, diorita, andesita, tierra, basalto, obsidiana, calcita, toba, espeleotema o grava. No produce drops ni XP. |
| Crown of Stasis | Modo dominio: marca un cuadrado de 16×16 con velocidad minera ×8, Regeneración V, Absorción IV, Resistencia II, comida y Visión nocturna. Modo aura: beneficios menores te siguen y no congela enemigos. |
| Loom of the First Quarry | Apunta a roca y activa. Excava una esfera de geología natural de radio 8, distribuida en ticks; preserva menas, máquinas y bloques colocados. |
| Tear of Icarus | Elige una perforación hacia delante normal o amplia. Cada sección se valida antes de minar y avanzar; agua, lava, fluidos de mods, claims y barreras detienen el recorrido. No recibe daño de caída durante el trabajo. |
| Hollow Axiom | Apunta a una zona cargada y activa. Marca para ti hasta 32 menas ya existentes dentro de una elipse de 17×9×17; da Prisa III y Visión nocturna durante diez segundos. No rompe bloques. |
| Worldbreaker | CARVE, FRACTURE, CLEAVE, CORE DRILL y WORLD SHATTER son cortes direccionales. REGION BREAK selecciona dos esquinas, analiza y extrae solo bloques picables. Core Drill evita el daño de caída hasta terminar. |
| Exodium Starfall | Dispara un frente de minería de 13×9 hasta 48 bloques. La cola distribuye el trabajo por ticks y se detiene ante toda barrera. |
| Iridium Lodebreaker | Escanea una elipse de 17×9×17 en chunks cargados; prioriza cercanía y extrae hasta 128 menas vanilla o de mods. |
| Hellspec Infernal Bloom | Apunta al centro y extrae menas y geología natural en una cavidad elíptica de siete bloques. Da Resistencia al fuego y Prisa IV; nunca mina fluidos. |

## Seguridad y rendimiento

Todos los trabajos usan harvesting nativo y durabilidad normal. Las operaciones
revalidan permisos, alcance, chunk cargado, fluidos, block entities y estado del
bloque. No se cargan chunks artificialmente. La congestión de drops pausa el
trabajo para que el servidor no acumule entidades sin límite. El scheduler limita
el trabajo por jugador y globalmente. Los bloques geológicos colocados por
jugadores quedan protegidos desde que esta versión registra su colocación; no se
puede recuperar el historial de colocaciones anteriores.
