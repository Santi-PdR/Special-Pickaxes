# Special Pickaxes — mining rework

**5.10.439 — minería, guía y procedencia visual reforzadas; CI completo verificado.**
Worldloom activa sin bloque apuntado, paga sólo offhand y corta la pasarela ante
obstáculos/denegaciones/falta de material. Palimpsest prioriza las32menas naturales
más cercanas y comunica la coordenada de la primera. Los trabajos naturales
revalidan procedencia player-placed incluso durante callbacks de rotura. Firmas
alineadas con la mirada y nueva ficha de herramienta en manual/SHIFT. Diez
GameTests y tres JUnit añadidos; aún pendientes de ejecución Java en CI.

**5.10.431 — prueba descargable publicada; CI global correcto.**
Se conserva el gameplay validado en430. Tras CI completo exitoso en la rama de
sesión se publica un **prerelease de prueba** con JAR/RELEASE.json/SHA256SUMS.
No se marca como latest estable ni se atribuye aprobación visual. El instalador
PowerShell debe consultar releases (incluye prereleases), no `/releases/latest`.
No requiere gh/token porque el repositorio y los assets son públicos.
[Descarga de prueba 5.10.431](https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.431-43557399012).
[CI43557399012](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43557399012):
83JUnit y263GameTests pasados, JAR/servidor/logs/cliente correctos. Prerelease,
no aprobación visual completa ni instalación Windows comprobada.

**5.10.430 — picos, no secundarias de combate; CI global correcto.** X de los trece
picos sólo mina/construye/sondea/recoge drops o mueve al propio minero. Se retiran
repulsión, marcas, raíces, absorción de proyectiles, stasis pulse, erupción y ward
de las secundarias. Worldbreaker copia las versiones mineras. Daño base melee
reducido de 25 a 4 (antes de encantamientos/efectos externos), conservando tier5,
velocidad64 y durabilidad32768. Cada pico tiene una firma geométrica de activación
propia y acotada. Galería de 13 iconos reales + 13 capturas en mano generada desde este CI.
Las primarias y pasivas defensivas existentes no se eliminan en este cambio.
[CI 43551696313](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43551696313):
83 JUnit y 263 GameTests pasados, JAR/servidor/logs y cliente real correctos
(32 escenas, 64 tooltips, teclado-servidor). Sin instalación Windows ni aprobación
visual temporal completa. Ver [rediseño de secundarias](docs/mining-rework/MINING-ALTERNATES-430.md).

**5.10.428 — aterrizajes, Starfall y Lode Sense; CI global correcto.** Eventide
selecciona objetivos sin callbacks de permisos dentro del selector y revalida
origen, protección y objetivos antes del snare; evita reentrada. Icarus rechaza
caídas no finitas. Starfall, aterrizajes, Lode Sense y escaneos aliados recuperan
plazos imposibles manteniendo intervalos/potencia. Starfall valida bearer/nivel y
presupuesta flashes; Lode Sense revalida posiciones después de permisos. Once
GameTests y tres JUnit añadidos. En 427 pasó build/JUnit y 260/261 GameTests; el fixture antiguo de recarga no equipaba su ward. Corregido en 428: [CI 43548490641](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43548490641) global correcto, 81 JUnit y 261 GameTests pasados, JAR/servidor/logs y cliente real (32 escenas, 64 tooltips y controles). Sin aprobación visual completa ni instalación Windows.

**5.10.426 — defensa de Estasis y auras de mobs; CI global correcto.** La respuesta
compartida de Estasis evita reentrada y revalida daño, ward y atacante después de
callbacks de permisos. Recupera plazos imposibles sin alterar sus ocho segundos.
Las auras beneficiosas de Hellspec/Estasis/Worldbreaker sobre mobs usan audiencia
local y presupuesto global, igual que las demás. Gravitic Tether no acelera con
amplificadores negativos. Diez GameTests y tres JUnit nuevos. En 425 pasaron 78 JUnit y 250 GameTests, pero el escaneo estricto rechazó el error intencional de un fixture. En 426 se prueba denegación/reintento sin provocar excepciones. [CI 43546125974](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43546125974): 78 JUnit y 250 GameTests pasados, JAR/servidor/logs y cliente real correctos (32 escenas, 64 tooltips, controles). Sin aprobación visual completa ni instalación Windows.

**5.10.424 — minería observada, calor y resonancia; CI global correcto.** Hellspec
liga su caché térmica al origen/dimensión/tick y omite chunks descargados. Una
rotura observada no duplica la racha; su ventana usa ticks del servidor. Strata,
Estasis e Iridium recuperan plazos imposibles sin acelerar sus intervalos, y los
contadores corruptos no desbordan niveles. Las partículas de Iridium comparten
presupuesto global, sin condicionar los bonus/comida al renderizado. Ocho
GameTests y tres JUnit nuevos. [CI 43543547256](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43543547256): 75 JUnit y 240 GameTests pasados; servidor/logs/JAR y cliente real correctos (32 escenas, 64 tooltips). Sin aprobación visual completa ni instalación Windows.

**5.10.423 — CI global exitoso: 72 JUnit, 232 GameTests, servidor, logs y cliente real.** Los casos
negativos y de conteo limitan el permiso del fixture a su propio objetivo, para
no aceptar entidades/drops ajenos que puedan quedar de otros lotes. No se quitan
aserciones de propiedad, movimiento, fuerza o cooldown. Los CI 421/422 compilaron
y pasaron los 72 JUnit, pero fallaron en un GameTest distinto de selección espacial;
no se consideran verdes. La ejecución de 423 pasó los 232 GameTests, 32 escenas,
64 tooltips, controles y World Shatter (9341 bloques). Evidencia recuperada y
verificada. Solo tres capturas inspeccionadas: publicación binaria omitida por
falta de aprobación visual completa, sin instalación Windows.

**5.10.422 — guardas en la ruta real de activación; pendiente de CI.** La guarda
antirreentrada cubre también el primer evento del alternativo y comparte la
protección con el primario. Ambos rechazan herramientas separadas y revalidan la
mano después de permisos; la recuperación de cooldowns inválidos llega a la ruta
usada por los controles. Se preservan las habilidades copiadas de Worldbreaker.
Cinco GameTests adicionales: 232 en fuente, aún pendientes de CI propio.

**5.10.421 — tanda amplia de combate, memorias y animación; pendiente de CI.**
Palimpsest, Choir y Null Ward revalidan cada objetivo tras callbacks; los predicados
de selección ya no emiten eventos. Choir conserva resonancias externas y Null Ward
comparte presupuesto de partículas sin limitar su capacidad de detener proyectiles.
Los alternativos rechazan herramientas separadas, tipos incorrectos y activaciones
recursivas, y recuperan cooldowns inválidos. Memorias futuras/mal tipadas no se
restauran; los snapshots recuperan capacidad caducada sin barrer cada admisión.
Siete partículas animadas nacen transparentes y comparten fundido suavizado, sin
cambiar sprites, tamaño o vida. Nueve GameTests y ocho JUnit nuevos: 227 GameTests
en fuente. Sin cambios de tier, fuerza, radios ni recetas.

**5.10.420 — recuperación de cooldowns persistentes; build/JUnit, 218 GameTests, JAR, servidor y logs correctos en CI; cliente pendiente.** Dawn
Mending, Heartwood, comida de Worldloom y Null Veil descartan plazos imposibles
fuera de su propio intervalo. Las curaciones usan el reloj común del servidor,
no uno distinto por dimensión. No cambia la cadencia normal ni permite pulsos
repetidos en el mismo tick. Cuatro JUnit y tres GameTests añadidos; 218 GameTests
pasados por el CI de esta fuente; cliente/global todavía pendientes.

**5.10.419 — desplazamientos y recolocación seguros; 215 GameTests y servidor correctos en CI; cliente pendiente.** La ruta
usa un origen fijo y aborta si un callback cambia posición, dimensión, herramienta
o estado de montura/sueño. Recomprueba colisión después del permiso y rechaza
coordenadas no finitas antes de consultar el mundo. Un dominio cancelado o
sustituido durante el permiso ya no puede recolocarse como si siguiera activo.
Cuatro regresiones nuevas; 215 GameTests en fuente.

**5.10.418 — atribución de Magma Brand y erupción; CI global exitoso, evidencia recuperada, sin aprobación visual completa ni instalación.** Una marca
reemplazada externamente no se convierte en daño propio ni se elimina al perder
permisos el antiguo caster. Otro portador no puede robar una marca viva. El pulso
revalida ownership después de callbacks; la erupción comprueba cada objetivo
antes y después del permiso, y solo informa/visualiza objetivos afectados. Cuatro
regresiones adicionales; 211 GameTests en fuente. Daño, radio y límites intactos.

**5.10.417 — cola de trabajo resistente a cancelaciones; pendiente de CI.** Los
callbacks pueden cancelar, sustituir o pausar un trabajo sin que la cola continúe
sus pasos viejos, borre el sustituto o lo vuelva a encolar. Una guarda separada
impide ticks recursivos durante preparación y mutación, incluso tras limpiar la
cola. Cambios de dimensión/vida/herramienta detienen el turno. Cinco regresiones
nuevas; 207 GameTests en fuente. Presupuestos y potencia no cambian.

**5.10.416 — Estasis revalida cada fijación; CI global exitoso, sin aprobación visual completa ni instalación.** Tanto el dominio
como el pulso comprueban objetivo, alianzas, dimensión, herramienta y claims antes
de volver a fijar una entidad, incluido el permiso del punto de retorno. La
liberación restaura velocidad acotada sin teletransportar, y no escribe movimiento
en otra dimensión. Cancelar el pulso desde un callback no reanuda su efecto ni
invalida la iteración. Cuatro regresiones nuevas; 202 GameTests en fuente.

**5.10.415 — cierre de la auditoría de atracción de drops; pendiente de CI.**
El alternativo de Eventide también respeta la reserva vanilla de recogida y los
claims por drop, revalida posición/dimensión/mano/propiedad tras callbacks y no
gasta cooldown si todos los objetivos están protegidos. Mantiene fuerza, radio y
límite de objetivos. Una regresión adicional eleva la fuente a 198 GameTests.

**5.10.414:** CI global exitoso: build/JUnit, 197 GameTests, servidor empaquetado,
logs y cliente real (32 escenas/64 tooltips; 9341 bloques retirados). Evidencia
Checks recuperada y verificada. Solo tres capturas revisadas aquí; publicación
binaria omitida por falta de aprobación visual de esa fuente. No valida cambios posteriores.

**5.10.414 — propiedad real de drops y callbacks; pendiente de CI.** Corrige la
confusión entre `ItemEntity.getOwner()` (lanzador) y el UUID vanilla `Owner`
(reserva de recogida). Iridium y el colapso de Eventide respetan la reserva real;
Eventide comprueba claims por drop y revalida después del evento. Los permisos
recomprueban estado/dimensión/distancia del jugador después de callbacks sin
emitir un segundo evento. Selecciones, raíces y campos toleran cancelación
reentrante. Siete GameTests nuevos, todavía no ejecutados.

**Resultado de 5.10.412:** build y 60 JUnit correctos; GameTests fallaron en el
fixture de propiedad de Iridium, revelando la confusión anterior. No pasó el gate
global ni llegó a validar cliente. Evidencia Checks recuperada y verificada.

**5.10.413 — protección adicional de efectos; pendiente de CI.** Las pasivas
registran la potencia y duración concedidas, además de su marca de propiedad.
Un reemplazo externo más largo o de distinta potencia, incluso con las mismas
opciones visuales, no se renueva ni se elimina al quitar el pico. Los efectos
vetados al concederse no crean una marca de propiedad. Tres GameTests nuevos
cubren renovación, visión nocturna, absorción y efectos de combate. Un reemplazo
exactamente idéntico no puede distinguirse solo por estos datos.

**5.10.412 — auditoría en desarrollo; pendiente de su propio CI.** Iridium respeta
propiedad vanilla y marcas previas de otros jugadores, consulta claims tanto en
atracción pasiva como activa y revalida el drop después de callbacks. Sus
esquirlas y estelas comparten un límite de 32 por tick del servidor y el
presupuesto global de auras. Las observaciones mineras usan una ventana de dos
ticks del servidor. Worldloom comprueba permisos antes de cada captura o pin y
rechaza objetivos de otras dimensiones. Ícaro contabiliza sus dos partículas de
remate; Estasis reduce el brillo móvil según la calidad elegida sin ocultar su
contorno. Se refuerzan el escaneo de excepciones Java y la exportación/extracción
de evidencia de CI, conservando diagnósticos y declarando imágenes omitidas.

**5.10.411 — build/JUnit, GameTests, servidor y cliente comprobados en CI; ejecución global fallida al exportar evidencia, sin aprobación visual.** La selección de entidades
usa un colector top-k acotado, sin duplicados, siempre ordenado y con desempate
por ID; las listas pequeñas ya no saltan la ordenación. Los dominios no vuelven
a consultar JEG si su tipo ya es un proyectil vanilla y respetan el presupuesto
total incluso con límites impares. Se reparan compuestos NBT de tipos inválidos
y se invalidan anclas fechadas en el futuro. El manual abre por pulsación física,
no por repeticiones acumuladas; el scroll y el selector resisten desbordamientos
numéricos. Las operaciones revalidan mano y barreras tras los callbacks Forge,
antes de escribir bloques. Se añaden 17 casos JUnit y tres GameTests de regresión.

**5.10.410 — en desarrollo, pendiente de CI y revisión en cliente.** Unifica el
presupuesto de partículas de vínculos aliados con el tick del servidor, incluso
si difiere de la hora de una dimensión. Reserva del Alba usa un sol propio y
tres motas (4 partículas en vez de 9), sin cambiar comida ni cadencia. La copia
de Worldbreaker rechaza jugadores muertos, eliminados o espectadores. Un pulso
de Magma Brand cancelado por otro mod conserva la invulnerabilidad previa sin
pisar un valor distinto establecido por su listener. Nuevas pruebas cubren
presupuesto, copia, reserva y veto Forge del daño.

**5.10.409 — en desarrollo, pendiente de CI y revisión en cliente.** Corrige la
clasificación de tags `ore_rates/*` para que no inventen menas ni unan vetas;
limpia también el caché de familias al recargar tags. La respuesta de Estasis
usa un cristal animado y nueve partículas bajo el presupuesto compartido.
Hellspec sustituye chispas genéricas por sus flares propios en el aviso de
Infernal Bloom y reutiliza el pulso de marca con audiencia local en los blancos
de la erupción, sin cambiar daño, alcance o recarga. Todos los recursos de
compatibilidad de prueba quedan excluidos del JAR. El CI añade pruebas del
verificador de GameTests y de reproducción del sprite del Alba, y se verifican
los recursos de todas las partículas registradas.

Guía de extensión del terreno: [integración por datapacks](docs/mining-rework/DATAPACK-INTEGRATION.md).

La **5.10.408** añade un sol animado de cuatro fotogramas al pulso de Remiendo del Alba de Palimpsest (una sola partícula propia por curación real, con generador determinista en `tools/dawn_bloom_asset.py`) y un tag de bloques `specialpickaxes:geology` para que los packs declaren terreno seguro sin actualizar el mod; las piezas fabricadas (losas, escaleras, muros, puertas, lámparas) ya no cuentan como geología aunque su nombre contenga roca. La inferencia por nombre de otros mods ahora exige segmentos completos (`lodestone` o `milestone` no coinciden) y sigue excluyendo menas, máquinas y fluidos antes de cualquier regla de nombre.

La **5.10.407** cambia los iconos de Exodium, Iridium, Hellspec y Worldbreaker por sprites pixel-art 32×32 dibujados para este mod; conserva las referencias y su procedencia.

La **5.10.406** amplía Barrido de Falla de Sutura de los Estratos a una cara 7×7 (hasta 49 bloques naturales); mantiene los filtros de seguridad y comprueba en GameTest los bloques del borde y los exteriores.

La **5.10.405** añade a Starfall de Exodium un núcleo estelar animado de Blockbench en los impactos de minería; cada avance de la técnica envía una sola partícula propia en vez del par genérico anterior.

La **5.10.404** lleva el sigilo animado de raíces al pulso de captura activo de Worldloom; sustituye cuatro fragmentos genéricos por una sola partícula cada 10 ticks, sin cambiar el aviso de 4 s ni la inmovilización.

La **5.10.403** cambia las raíces genéricas de Worldloom por cuatro sigilos pixel-art animados de Blockbench al atrapar enemigos con la pasiva de Curios; conserva el anillo bajo los pies, recorta la emisión a cuatro sprites y respeta la audiencia local.

La **5.10.402** conecta la pluma animada de Blockbench al Planeo Icariano de Curios: alas y rebote de aterrizaje muestran plumas propias sin subir la cantidad de partículas ni su frecuencia.

La **5.10.401** rehace en Blockbench la pluma animada de Ícaro como cuatro cuadros de 32×32 con transparencias, sincronizados con los 16 ticks de vida de la partícula.

La **5.10.400** corrige las instrucciones del Crisol: `C` selecciona entre sus 12 salidas, incluidas calcita, obsidiana y toba. Se añade una prueba de calcita como bloque de entrada y el mensaje de rechazo explica qué geología se acepta y qué protecciones la omiten.

La **5.10.399** rediseña en Blockbench la esquirla de Stasis como un cristal cian con un aro violeta orbital. La hoja animada de cuatro cuadros conserva transparencia y el ciclo completo de un segundo.

La **5.10.398** añade a Eventide una onda gravitatoria pasiva al aterrizar tras una caída fuerte: frena hasta tres enemigos cercanos durante tres segundos, con enfriamiento de diez segundos y sin daño adicional.

La **5.10.397** amplía Eco de Falla y Raíces de Cantera a mobs sin `Enemy` que ya estén atacando al portador o a un aliado; no cambia su alcance ni hace barridos extra.

La **5.10.396** hace que Eco de Falla y Raíces de Cantera también reconozcan enemigos de otros mods que implementan `Enemy` sin heredar de `Monster`; se conservan los filtros de equipos, PvP y zonas protegidas.

La **5.10.395** refuerza la Guardia de veta: el golpe protegido ahora ancla al portador y a sus aliados contra empujones durante cuatro segundos; dos marcas de cristal a los pies señalan el efecto sin mantener una nube de partículas.

La **5.10.394** lleva el flare animado de Blockbench a los cinco respiraderos de Erupción Magmática; reduce las llamas vanilla por respiradero para mantener el mismo coste de partículas.

La **5.10.393** sustituye el fuego genérico del pulso de Marca de Magma por una llamarada de cuatro pétalos diseñada y animada en Blockbench; sigue siendo un único destello por pulso, enviado solo a jugadores cercanos.

La **5.10.392** reutiliza el fragmento de veta animado también como destello central de la Guardia de Veta cuando Iridium protege al portador o a un aliado; aparece una vez por segundo dentro del presupuesto de aura y mantiene el mismo radio de audiencia local.

La **5.10.391** convierte el cristal animado de Blockbench en el destello de la pasiva Curios de Iridium: cada drop de mena se marca con un fragmento pixel-art de ocho cuadros, enviado solo al portador y limitado al presupuesto existente de 32 destellos por tick. La hoja de juego se genera directamente en Blockbench y el aura reemplaza cinco chispas genéricas por una partícula animada.

La **5.10.390** reconoce el depósito `terramity:prismatic_crystal_cluster` como mena para vetas, Orefall y protección de minerales; deja excluido el bloque de almacenamiento `prismatic_crystal_block`. La **5.10.389** sustituye el destello genérico del ascenso de Icarus por una pluma pixel-art animada de ocho fotogramas, modelada y animada en Blockbench; la pluma sube en espiral con las corrientes cian y solo se envía a la audiencia cercana. El `.bbmodel` fuente y la hoja de fotogramas de Blockbench se guardan en `artwork/blockbench/`. La **5.10.388** reutiliza la partícula de ascenso de Icarian Lift en vez de reconstruir su color y tamaño durante cada pulso; conserva el movimiento helicoidal y el envío local. La **5.10.387** añade un cristal animado de cuatro fotogramas, creado en Blockbench, al aura pasiva de Stasis en Curios. Aparece una vez por segundo dentro del alcance local y del presupuesto existente de partículas; su fuente `.bbmodel` está en `artwork/blockbench/`. La **5.10.386** limita las partículas de Phase Shear, Null Mark, Dawn Reserve, Stasis Crush, Fault Resonance, Gravitic Tether y Crucible Mantle a audiencias locales con presupuesto por tick; los golpes y pulsos conservan sus efectos y geometría. La **5.10.385** envía el destello de aterrizaje de Icarus solo al portador y hasta tres jugadores cercanos, con el presupuesto compartido de partículas de las demás auras de Curios. La **5.10.384** aplica la Visión Nocturna anunciada al sostener un pico, además de cuando se lleva en Curios, y la retira al guardar el último sin borrar el efecto del servidor. La **5.10.383** completa las fichas de Curios en el manual: Palimpsest, Hollow Axiom, Exodium, Iridium y Seam Ripper ahora explican también su Foco Estratal y la racha de velocidad minera que reciben al llevarlos en ese slot, sin alargar el tooltip corto del objeto. La **5.10.382** amplía la geología reconocida por el Crisol para incluir los bloques de moonstone, fairystone e igneostone de Terramity; deja fuera sus lámparas y dispensadores. La **5.10.381** amplía la compatibilidad geológica con rocas de nombres comunes como gabbro y mudstone, sin generar drops, y aclara las instrucciones de Eventide.

La **5.10.380** hace visible el frenado icario con las alas recogidas y orientadas hacia abajo; la señal también se muestra a los aliados que reciben el planeo.

La **5.10.379** añade un freno aéreo al Planeo icario: mantén Agacharse para reducir el impulso horizontal y controlar el descenso.

La **5.10.378** dibuja también las alas del Planeo icario en los compañeros que reciben el efecto por Curios, aunque no lleven Ícaro equipado. La animación se reduce según la calidad de partículas y mantiene ambas alas visibles.

La **5.10.377** permite que Tear of Icarus comparta su planeo con hasta dos compañeros aéreos cercanos cuando su portador también está en el aire. La pasiva de Curios no requiere tecla y muestra un breve hilo dorado al enlazar.

La **5.10.376** extiende la Guardia de Veta de Iridium a hasta tres compañeros cercanos cuando el portador recibe un golpe; el arco de cristal aparece una vez y solo para los participantes del vínculo.

La **5.10.375** añade señales breves y privadas cuando Exodium o Hellspec comparten sus protecciones con un aliado cercano. El arco aparece solo al recibir la protección, no en cada refresco del aura.

Forge **1.20.1 / Java 17**. La versión instalada para pruebas es **5.10.406**: trece picos tier V con dos técnicas al usarlos y pasivas propias. En Curios conservan solo sus pasivas; no necesitan teclas extra. El manual reúne las instrucciones completas y los objetos mantienen descripciones breves. Las excavaciones se procesan por ticks, vuelven a validar cada bloque y respetan menas, fluidos, máquinas, zonas protegidas y bloques colocados. Las búsquedas de entidades, drops y jugadores tienen límites para mantener el coste del servidor controlado. Curios es opcional.

La **5.10.372** permite arrastrar el tirador de desplazamiento del Manual para recorrer rápidamente las fichas largas; el área de agarre es más amplia y se mantiene separada del pie y el selector. La **5.10.371** completa las señales de las pasivas Curios compartidas: Crucible deja un hilo breve de brasas al otorgar su manto térmico. Solo se muestra al aplicar la protección a un aliado nuevo. La **5.10.370** añade un hilo de pizarra cuando Worldbreaker comparte su Égida y una sutura violeta al compartir el Velo Nulo de Axiom; cada señal aparece una sola vez por nuevo aliado y solo la ven el portador y quien recibe la pasiva. La **5.10.369** hace que el Foco Estratal avance solo al romper geología natural confirmada: menas, máquinas, fluidos y bloques colocados no aumentan la racha ni disparan su pulso minero. La **5.10.368** pule el Manual de reliquias: ajusta títulos largos al ancho de sus tarjetas, evita que el nombre del pico se cruce con la indicación superior y permite recorrer el contenido con AvPág/RePág e Inicio/Fin. La **5.10.367** omite los barridos predictivos de proyectiles de Exodium mientras Manto Estelar está recargando. La intercepción exacta en el impacto sigue activa; al terminar la recarga, los barridos vuelven con el mismo ritmo escalonado entre jugadores. La **5.10.366** optimiza el Retorno de Veta de Iridium: conserva solo los drops elegibles más cercanos, con un límite de 4096 entidades inspeccionadas, en vez de materializar y ordenar miles de candidatos. La **5.10.365** evita que tooltips de inventarios y overlays de terceros cubran el Manual de reliquias, estiliza los bordes de sus tarjetas y deja de dibujar tarjetas fuera del área visible. Además, el Core Drill ahora tiene pruebas explícitas de protección contra caída durante la pausa, después de acabar la perforación y tras aterrizar. La **5.10.345** muestra el hilo visual de Choir solo cuando un aliado obtiene Foco Estratal, en vez de volver a dibujarlo en cada revisión del aura. La **5.10.344** cachea también las familias de tags de veta; la búsqueda conectada evita volver a crear streams de tags para cada bloque vecino y limpia ese caché junto con la clasificación de menas al recargar tags. La **5.10.343** cachea por bloque la clasificación de menas para evitar recorrer las mismas tags una y otra vez durante las excavaciones y consultas de geología; los cambios de tags al recargar datapacks invalidan el caché. La **5.10.342** aclara las descripciones de Worldloom: especifica los dos puntos de comida restaurados y el contraataque de raíces compartido. La **5.10.341** extiende la respuesta de Worldloom a cada aliado que recibe Renovación de Duramen: reduce un golpe hostil en 10% y sujeta al atacante con raíces durante 3 s. Cada aliado tiene su propio enfriamiento de 8 s; la respuesta respeta PvP y zonas protegidas. La versión **5.10.340** hace que los buffs del dominio de Stasis respeten las pociones externas: el dominio solo renueva sus propios efectos y al cerrarse retira únicamente su propio Dominion. La **5.10.339** amplía Sutura de los Estratos: sigue bloques naturales iguales unidos por caras, aristas o esquinas, con los límites existentes de 512 posiciones examinadas y 256 bloques minados. Conserva menas, bloques colocados y carga de chunks bajo demanda. La **5.10.338** añade un vínculo corto de enredaderas y destellos cuando Worldloom comparte su renovación con un compañero nuevo. La curva se reutiliza con elevación distinta para la señal gravitatoria de Eventide; ambas se muestran solo al portador y al aliado, una vez al recibir el efecto. La **5.10.337** muestra con un hilo gravitatorio curvo cuándo Eventide comparte su caída controlada con cada aliado nuevo; aparece una sola vez al entrar al aura. La **5.10.336** comparte la caída controlada de Eventide y su reducción de daño por caída con hasta tres compañeros a seis bloques. La **5.10.335** hace visible el polvo estelar de la guardia de Exodium también para compañeros cercanos que la reciben. Conserva cuatro partículas por órbita y solo añade una chispa central ocasional al recargarse. La **5.10.334** muestra un anillo pequeño de brasas para el portador y jugadores cercanos mientras el Caparazón Ígneo da su bonus de minería cerca del calor. La **5.10.333** extendió ese caparazón —protección contra fuego y lava más 35 % de velocidad de minería junto a fuentes de calor— a hasta tres compañeros a seis bloques. El rastreo de compañeros para esta última aura corre cada 20 ticks; todas las partículas están limitadas a una audiencia local.

R activa, confirma, pausa o reanuda; X usa la técnica secundaria; C cambia el modo disponible e I abre el manual. Worldbreaker permite elegir visualmente otro pico desde el manual y muestra cuál está copiando. El sonido de habilidades se limita a jugadores cercanos y tiene enfriamiento por emisor y oyente.

La versión 5.10.275 mantiene la animación de las auras de Curios cada cuatro ticks, pero actualiza la selección de jugadores cercanos cada ocho para reducir búsquedas de entidades e inventarios sin retrasar la animación visible. La versión 5.10.274 reparte las comprobaciones de Curios en diez grupos estables, con la misma frecuencia por jugador pero evitando una ráfaga global cada diez ticks. Las pasivas y auras de aliados conservan su ritmo. La versión 5.10.273 diferencia las rachas del Foco Estratal con un corte azul, verde azulado y dorado; al llegar al máximo, remata con una espiral ascendente breve. La frecuencia y el máximo de partículas por bloque se mantienen. La versión 5.10.272 comparte Foco Estratal I con hasta tres aliados cercanos cuando Choir of Faults se lleva en Curios, con búsqueda limitada a una vez cada 20 ticks. La versión 5.10.271 amplía Phase Shear del Crucible: reduce el ataque un 25 % y el movimiento un 15 %, con un corte cruzado visible al impacto. La versión 5.10.270 añade cuatro columnas de enredaderas al cierre de la trampa del Telar, sin cambiar sus cuatro segundos de aviso ni su límite de partículas. La 5.10.269 evita consumir el pulso de Renovación de Duramen cuando otra habilidad bloquea la curación; tampoco muestra partículas hasta curar de verdad. La 5.10.268 solo consume la recarga y muestra el pulso de Remiendo del Alba cuando la curación se aplica de verdad; una curación bloqueada conserva el siguiente pulso. La 5.10.267 dibuja tres cortes luminosos cuando Desgarro de Estrato reduce la armadura del atacante; la animación solo aparece al activar la pasiva. La 5.10.266 dibuja el Velo Nulo como una espiral ascendente al limpiar un efecto dañino, sin animación continua ni cambios a su recarga. La 5.10.265 conserva las picadas deliberadas con élitros mientras la pasiva icaria controla las caídas normales. La 5.10.264 anima la recarga de Manto Estelar con un halo compacto y violeta que se expande y vuelve a brillar al recuperarse la intercepción, sin aumentar el ritmo de partículas. La 5.10.263 recarga la intercepción en 1,5 s, manteniendo el barrido de trayectorias cada tres ticks y la reducción de daño de los disparos que atraviesan el velo. La 5.10.262 hace visible el cierre de las raíces del Telar a los jugadores cercanos y reparte cuatro puntos de anclaje alrededor del objetivo sin aumentar la frecuencia del efecto. La 5.10.261 amplía el alcance predictivo del velo de Exodium a 10 bloques y reduce sus barridos de proyectiles a una vez cada tres ticks. La versión 5.10.260 añade un halo breve al alcanzar la tercera carga del Foco Estratal; la 5.10.259 confirmó roturas reales antes de avanzar la racha o mostrar el pulso de Estasis. La apariencia de las animaciones requiere validación visual dentro del cliente.
El análisis del Paradox Crucible usa ahora las mismas restricciones de seguridad que la transformación real y omite los bloques que ya coinciden con el material seleccionado; así no promete cambios que luego fallarán ni reporta falsamente “Nothing changed”.
La habilidad radial del Crucible ahora filtra materiales iguales al resultado y comprueba las mismas protecciones que el modo de selección antes de intentar transmutar.

El Crucible informa específicamente cuando no encuentra geología segura para transformar y qué tipo de bloque y modo revisar.

Los campos de Stasis y Eventide ahora consultan directamente criaturas hostiles y proyectiles; entidades irrelevantes como drops no consumen el límite de búsqueda.

En la instancia ghouls, las vetas mundiales de Terramity `brimslag` y `stellar_remnant`, y `undead_revamp2:bostrokestone`, cuentan como menas; `igneostone` y los bloques naturales de dripstone de Undead Revamped participan en túneles y transmutación geológica.

## Catálogo

Palimpsest · Choir of Faults · Eventide · Paradox Crucible · Crown of Stasis ·
Loom of the First Quarry · Tear of Icarus · Hollow Axiom · Worldbreaker ·
Exodium Starfall · Iridium Lodebreaker · Hellspec Infernal Bloom.

- **Palimpsest:** extracción segura de vetas conectadas con un límite configurable; su pasiva ayuda a minar y recuperarte.
- **Choir:** túnel resonante fijo 2×2, 20 bloques hacia delante; ya no repite una plantilla vieja.
- **Eventide:** campo gravitatorio que ordena derrumbes controlados al minar piedra.
- **Crucible:** transforma doce materiales geológicos, ahora también calcita y obsidiana; respeta menas, cofres, fluidos y bloques colocados.
- **Stasis:** dominio 16×16 con borde visible, ayuda a compañeros dentro o te sigue como aura; X traslada el dominio conservando su tiempo.
- **Worldloom:** túnel recto de 5×5 por 16 bloques; raíces atrapan enemigos cercanos.
- **Icarus:** perforación hacia delante y ascensor de un bloque de ancho por dos de alto que sube por el techo de terreno.
- **Axiom:** excava una malla radial de geología natural y conserva las menas; no detecta vetas. Su pliegue secundario sirve para combate.
- **Worldbreaker:** varios cortes, rotura regional y selección visual de otro pico para copiar con R/X.
- **Exodium:** galería minera de 13×9 hasta 48 bloques; X abre un pozo octogonal 5×5 hacia abajo.
- **Iridium:** mina menas cargadas desde el centro hacia afuera, a medida que las encuentra; prioriza las cercanas, ilumina sus drops y omite chunks descargados y zonas protegidas. X atrae hasta 96 drops propios.
- **Hellspec:** excava una flor de cinco pétalos; su X lanza una onda de magma que empuja, quema y marca enemigos sin alterar bloques ni líquidos.
- **Sutura de los Estratos:** R pela la cara conectada entre dos materiales o la superficie expuesta; X pela el lado B opuesto sin excavar el interior.
  Su rastreo también se reparte por la cola global de ticks; no hace el sondeo completo en una sola activación.

Toda minería se agenda por ticks y vuelve a validar cada bloque. No carga chunks,
no procesa fluids, bedrock, máquinas ni celdas protegidas, y pausa ante congestión
de drops. Los tags Forge y comunes amplían la compatibilidad con menas y geología de mods.
Los bloques geológicos colocados por jugadores desde esta versión quedan protegidos
de las transformaciones masivas del Crucible y de la cantera Hellspec.

La versión 5.10.8 depura las marcas de bloques colocados al verificar las roturas
al final del tick. El borrado y el barrido gradual están limitados por tick, no
cargan chunks y mantienen la protección si otro mod cancela el evento de rotura.

## Referencia de nivel

La instancia `ghouls` trae Terramity 0.9.8. Su Exodium Pickaxe es tier IV, velocidad
20, durabilidad 8.124 y encantabilidad 50. Los picos Special Pickaxes usan tier V,
velocidad base 64 o mayor, durabilidad 32.768 y encantabilidad 50; la habilidad de
herramienta permite extraer bloques picables y menas con tags de Forge.

Exodium, Iridium y Hellspec usan los sprites de Terramity con permiso del
propietario. La procedencia y SHA-256 están en
[TEXTURE_CREDITS.md](src/main/resources/assets/specialpickaxes/TEXTURE_CREDITS.md)
y [ASSET-PROVENANCE.json](docs/mining-rework/ASSET-PROVENANCE.json).

## Jugar y administrar

R activa la minería; X usa la técnica secundaria; C cambia de modo; I abre el
Manual. Worldbreaker usa R/X para las técnicas copiadas desde el Manual.
B selecciona regiones; Enter confirma solo cuando el análisis está listo; V
cancela; K pausa/reanuda (R también reanuda una pausa por drops). Los controles
son reasignables. Curios solo aplica las pasivas del pico equipado.

```text
/specialpickaxes grant <jugador> <id>
```

IDs: `palimpsest`, `fault_choir`, `eventide`, `paradox_crucible`, `interregnum`,
`worldloom`, `icarus`, `hollow_axiom`, `worldbreaker`, `exodium`, `iridium`,
`hellspec`.

- [Guía del jugador](docs/mining-rework/PLAYER-GUIDE.md)
- [Compatibilidad](docs/mining-rework/COMPATIBILITY.md)
- [Evidencia histórica de la versión 5.0.0](docs/mining-rework/STATUS.md)
- [Assets archivados](asset-library/README.md)

La versión 5.10.127 hace que el Pozo Estelar de Exodium tenga una sección biselada propia, contorno limitado al usuario y un pulso de partículas durante la perforación. La prueba de geometría verifica que sus 21 bloques por sección no se confundan con el túnel cuadrado de Worldloom. El inventario y el validador de recursos ahora cubren los trece picos jugables y solo las cuatro teclas realmente registradas. La procedencia visual incluye un archivo compacto de las tres texturas autorizadas de Terramity para que la revisión funcione también fuera de la instancia local. La versión 5.10.126 añade las variantes superior e inferior del pilar de dripstone de Undead Revamped a la geología reconocida para minería y transmutación. La versión 5.10.125 amplía el reconocimiento de geología de mods mediante nombres de registro habituales (piedras, basaltos, granitos, pizarras, tobas, calcitas, obsidianas, espeleotemas, gravas, tierras y arcillas). Las menas, los líquidos y los bloques con entidad quedan excluidos; el resultado se recalcula tras cada actualización de tags. El icono aprobado de Curios permanece intacto.

La versión 5.10.125 reconoce también `undead_revamp2:bostroxore`, una mena mundial de Undead Revamped que no usa sufijo ni tag convencional; sus drops `rawbostrox` ahora entran en la extracción de Iridium y su iluminación.

La versión 5.10.125 evita inferir geología a partir de sufijos fusionados como `lodestone`: solo usa tokens geológicos separados o IDs de worldgen aprobados, y mantiene la lista explícita para geología vanilla.

### Verificación final de esta tanda

[CI43562331609](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43562331609):
86JUnit +287GameTests; cliente32escenas/64tooltips, servidor y logs limpios.
[Descarga de prueba5.10.439](https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.439-43562331609).
No es aprobación estable ni instalación en Windows. Detalles/hashes en
`docs/mining-rework/STATUS.md`.
