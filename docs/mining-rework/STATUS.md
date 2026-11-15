## Entrega verificada 5.10.456

- Fuente: `95b36f35908e32db1f630dfd6523cb2704da0da1`.
- CI: https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43674513875
- Prerelease: https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.456-43674513875
- JAR 553744 bytes; SHA256 `40700d0844c55e267ba41349c5022bb1969a15ca0d46e333df99b3cb557cbdfa`.
- Asset GitHub749778973 uploaded: tamaño y digest API coinciden con RELEASE.json.
- Marcadores reales de cliente: ALTERNATE_BINDINGS_REBOUND_OK e INSTRUCTION_BINDINGS_REBOUND_OK.
- Galería: `artifacts/picos-5.10.456/`, PDF13páginas, ZIP26imágenes con CRC correcto;
  procedencia vinculada a manifest CI y recursos exactos del commit.
- No certifica todas las animaciones, Ghouls ni una instalación Windows.

# 5.10.456 — CI completa correcta; prerelease de prueba publicado

WorldSafety.exchange conserva nivel, posición y herramienta originales desde
antes del primer permiso. Revalida ambas posiciones antes de escribir, entre
los dos callbacks de colocación y al confirmar. Si un listener mueve al actor o
revoca construcción, no confirma; revierte sólo escrituras que todavía le
pertenecen, sin deshacer el movimiento externo. Cuatro GameTests cubren cambios
en permisos, rotura, primera y segunda colocación.

Los plazos de ocho segundos de los contadores Curios y de Heartwood/Null Veil
compartidos usan la política validada PersistedCooldown: un plazo imposible no
bloquea la pasiva indefinidamente. Dos GameTests nuevos comprueban recuperación
y bloqueo de repetición real. CI 43674513875 completed/success: 110 JUnit sin
fallos/errores/omisiones y 340 GameTests; servidor empaquetado correcto, logs
validados, cliente con 32 escenas y 64 tooltips. Sin build local. La CI 455 inicial falló por tres pruebas que publicaban login Forge
con una conexión simulada: se extrajo restoreCooldowns, usada tanto por login
real como por pruebas; no se omitieron pruebas ni se relajaron los logs.

# 5.10.455 — recargas y referencias secundarias, pendiente de CI

Al reconectar se sincroniza sólo el tiempo restante de plazos válidos, no un
Integer.MAX_VALUE derivado de NBT corrupto. Tres JUnit y tres GameTests cubren
plazos válidos, inválidos y el mayor intervalo legítimo de principal/secundaria.
Las descripciones de secundarias también usan keybinds, no letras R/X fijas;
el cliente reasigna principal a O y secundaria a P para validar Eventide, además
del test de instrucciones principal existente. Sintonía Curios reserva20partículas
antes de emitir; saltarla no impide pasivas. Palimpsest: séptima adaptación de
contraste reproducible, gamma.84, alpha/silueta/original preservados.
454/run43672786521 pasó CI completa.455 debe repetirla:110JUnit/334GameTests
esperados; local25recursos/63Python/parser145Java/diffcheck correctos.

# 5.10.454 — ascenso sin recaptura; CI correcta

Icarian Lift detecta que el minero salió de su eje o fue teletransportado fuera
del progreso esperado. Termina sin seguir cortando ni arrastrarlo de vuelta;
respeta saltos normales con tolerancia vertical. Salir no provoca carga de chunks
ni consultas de congestión antes de parar. Mensaje ES/EN explica la detención.
Tres GameTests y cinco JUnit nuevos: esperados330GameTests/107JUnit. Mantiene
ascenso normal, barreras y fluidos bajo sus regresiones existentes. Local25recursos,
63Python/parser145Java/diffcheck correctos. Sin build local. CI43672786521
completed/success:107JUnit sin fallos/errores/omisiones y330GameTests.
Estos cambios quedan incluidos en456, cuya CI completa también pasó.

# Resultado actual: 5.10.453 verificada y publicada como prueba

- **Fuente:** `25fd933b670bae285c37b90059971e38ef1946ea`.
- **CI:** [43671030909](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43671030909), completed/success.
- **Java:** 102 JUnit, cero fallos/errores/omisiones; 327 GameTests completos.
- **Recursos:** 25 pruebas en CI. Herramientas Python: 63 pasan localmente con
  Pillow; en CI sin Pillow pasan 62 y se omite sólo el renderer opcional del PDF.
- **Runtime:** servidor empaquetado y logs de servidor/GameTests/cliente limpios.
  Cliente con 32 escenas, 64 comprobaciones de tooltip final y altura, controles
  al servidor y WorldShatter de 9.341 bloques. Marcador nuevo
  `INSTRUCTION_BINDINGS_REBOUND_OK` observado una vez tras reasignar activación a O.
- **JAR:** `special-pickaxes-1.20.1-5.10.453.jar`, 551.671 bytes, SHA-256
  `8d9a95ad465fec905eb1724e6c592640d64362bdbec45fd9566f9b07183a7967`.
  Recuperado íntegro mediante Checks; tamaño/hash/CRC correctos. Los trece modelos
  y recursos coinciden con sus fuentes y procedencia. Sin recetas, worldgen ni fixtures.
- **Publicación:** prerelease `test-5.10.453-43671030909`, asset 749676275 uploaded;
  tamaño y digest de API coinciden con el JAR verificado.
- **Galería:** `artifacts/picos-5.10.453/los-13-picos.pdf`, 13 páginas, 26 imágenes
  exactas, HTML, PROVENANCE y SHA256SUMS. ZIP con CRC correcto. Se inspeccionaron
  el retrato Icarus y SHIFT Fault Choir de esta revisión; no se certifican todas
  las animaciones ni el pack Ghouls/shaders/TPS.

## Cambios comprobados en esta tanda

1. La colocación pagada conserva el contexto creativo inicial, revalida antes de
   escribir y después del evento, y revierte su propia escritura si el modo cambia.
   Cuatro regresiones cubren ambas direcciones en claims y eventos de colocación.
2. Seam Ripper no admite un bloque marcado por un callback como origen de
   descubrimiento de más terreno. Se revalidan contexto, estado y procedencia.
3. Preview de hasta 24 puntos uniformes, incluyendo ambos extremos. Cinco JUnit
   cubren límites, planes pequeños, unicidad, espaciado y aritmética con int máximo.
4. Instrucciones de manual/SHIFT con keybinds para acción, modo y secundaria.
   El regenerador conserva placeholders; regresión recorre modos de los 13 picos.
5. Sexta adaptación de legibilidad: Icarus, gamma .82, alpha/silueta/original intactos.

452/run43669594248 pasó 102 JUnit y 326 GameTests antes del cambio de instrucciones.
El verificador histórico debe usar 326 para ese log: compararlo con el catálogo
actual de 327 lo rechaza correctamente. 453 repitió toda la CI con 327 completos.

No hubo checkout, build local, instalación Windows ni automatización de interfaz
local. La publicación de prueba sigue separada del gate visual estable.

## Historial (las notas «pendiente» siguientes preceden al resultado actual)

# 5.10.453 — instrucciones reasignables y legibilidad: pendiente de CI

Las instrucciones largas ya no fijan las letras R/C/X: se traducen con componentes
keybind para acción, modo y secundaria, en SHIFT y manual. El regenerador de
catálogo conserva el contrato. Cliente CI comprueba instrucciones con activación
reasignada a O, además del tooltip compacto. Una regresión recorre los modos de
los13picos; dos Python y una prueba de recursos comprueban traducciones.
Icarus usa una sexta adaptación reproducible de contraste (.82), conservando
alpha/silueta y el original; no se declara redibujo ni aprobación visual completa.
Local25recursos/63Python/parser143Java/diffcheck; esperados102JUnit/327GameTests.
452/run43669594248 ya completó CI correctamente;453 debe repetirla con cambios.

# 5.10.452 — previsualización completa: pendiente de CI

El muestreo de previews ahora incluye el principio y el final del plan y reparte
hasta 24 puntos uniformes. El stride anterior sólo mostraba la primera mitad de
planes de 25–47 pasos y omitía finales de chimeneas/caras mayores. No aumenta
paquetes ni toca bloques para dibujar. Cinco JUnit puros prueban orden, unicidad,
intervalos, casos pequeños e índices de tamaño Integer.MAX_VALUE sin overflow.
Conserva las transacciones y descubrimiento natural de451. Esperados102JUnit,
326GameTests. Local24recursos/61Python/parser143Java/diffcheck correctos.
No verificada todavía; última validación completa:450.

# 5.10.451 — construcción y descubrimiento: pendiente de CI

Una transacción de colocación pagada conserva el estado creativo inicial y rechaza
cambios durante claims o eventos de colocación. Evita material gratuito al pasar
a creativo y consultar el slot -1 al pasar de creativo a supervivencia. Cuatro
regresiones cubren ambas direcciones y ambas fases sin perder material.
El descubrimiento de Seam Ripper revalida actor y procedencia después del claim:
un bloque recién marcado no sirve de puente para descubrir terreno detrás.
Una regresión adicional; 326 GameTests y 97 JUnit esperados, no confirmados aún.
Local: 24 recursos, 61 Python con Pillow, parser de 141 Java y diffcheck correctos.
Sin build local ni automatización de interfaz local; última verificada: 450.

# Resultado actual: 5.10.450 verificada y publicada como prueba

- **Fuente:** `b1f495880c1debcc029422e3841120b52992e9a9`.
- **CI:** [43667431107](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43667431107), completed/success.
- **Pruebas Java:** 97 JUnit, cero fallos, errores u omisiones; 321 GameTests completos.
- **Recursos/herramientas:** 24 pruebas de recursos en CI; 61 Python correctas en
  venv local con Pillow. En el unittest de CI sin Pillow se omite únicamente el
  render opcional del PDF: 60 pasan y 1 se omite. No se confunde con JUnit.
- **Servidor/cliente:** servidor empaquetado y logs limpios. Cliente completo:
  32 escenas, 64 tooltips, 64 comprobaciones de viewport y del evento final,
  controles al servidor y WorldShatter de 9.341 bloques.
- **JAR:** `special-pickaxes-1.20.1-5.10.450.jar`, 550.032 bytes, SHA-256
  `ac50c5e88fbec79089708136b36997bd2adc7feb3f1e0ea35c8d71ce866c0fb5`.
  Recuperado íntegro mediante Checks; tamaño, hash y CRC correctos. Trece iconos
  y modelos coinciden con la procedencia exacta; sin recetas, worldgen ni fixtures.
- **Publicación:** prerelease `test-5.10.450-43667431107`, asset 749577195 uploaded;
  digest y tamaño de la API coinciden con el archivo verificado.
- **Galería:** `artifacts/picos-5.10.450/los-13-picos.pdf` (13 páginas), ZIP con
  26 imágenes exactas, HTML, procedencia y SHA256SUMS; CRC del ZIP correcto.
  Revisada vista comparativa de los 13 retratos reales. Esto no aprueba toda la
  animación temporal ni todo el pack Ghouls.

## Cambios de esta tanda

1. Tickets de limpieza de procedencia: recolocar un bloque invalida observaciones
   antiguas sin borrar la marca nueva; sólo se mantienen tickets pendientes.
2. Forge EntityMultiPlaceEvent: cada snapshot válido del mismo mundo se registra,
   con exclusiones de fluidos y contenedores; no se fuerzan chunks.
3. Lode Sense: se revalidan permisos, actor y stack exacto después de callbacks.
4. Contornos de selección y trazas comparten presupuesto visual. Agotarlo no
   cancela la selección ni la minería autorizada.
5. Eventide: quinta adaptación de legibilidad reproducible, sin cambiar alpha,
   silueta ni el original preservado.
6. Controles: recuperación ante inputTick corrupto por overflow; una reanudación
   denegada ya no devuelve éxito ni emite su señal de confirmación.
7. Evidencia Checks: selección por intento/digest, rechazo de exportaciones
   incompletas nuevas sin recurrir a una antigua; compatible con historial.

449 falló por AuthenticationUnavailableException del servicio externo Minecraft,
aunque completó las escenas y las pruebas de servidor. No se ignoró el escáner:
450 repitió la validación completa y sus logs están limpios. Se conservan las
notas históricas siguientes para no reescribir ese fallo como éxito.

No hubo checkout, build local, instalación Windows ni automatización de interfaz
local. La publicación es de prueba; el gate visual de versión estable sigue separado.

## Historial (las notas «pendiente» siguientes son anteriores al resultado actual)

# 5.10.450 — validación repetida y evidencias por intento, pendiente de CI

449/run43666174413 pasó97JUnit(cero fallos/errores/skips),321GameTests y
servidor empaquetado. Cliente completó escenas/controles/WorldShatter9341pero
logs detectaron AuthenticationUnavailableException al contactar servicio externo
Minecraft. No se ignora el error ni se certifica449verde. Se repite CI íntegro.

Exportación Checks identifica intento; recuperación selecciona un conjunto completo
por intento/digest, nunca mezcla chunks ni cae a evidencia vieja si el intento más
reciente está incompleto. Compatible con exports históricos, siete regresiones
Python y límite de128chunks. Local24recursos/61Python pasan conPillow; sinPillow
60pasan/1skip(rendereropcional). Runtimegameplayidéntico449,versión450.

# 5.10.449 — colocación múltiple Forge, pendiente de CI

EntityMultiPlaceEvent hereda EntityPlaceEvent, pero getPos sólo cubre la primera
celda. El rastreador ahora recorre snapshots del mismo nivel, sin cargar chunks,
con las mismas exclusiones de fluidos/blockentities. Protege construcciones de
colocación múltiple frente a minería de terreno natural. Tres GameTests cubren
todas las celdas/cancelación/exclusiones.321GameTests y97JUnit esperados.
Conserva447/448. Local24recursos/54Python/parser141Java/diffcheck correctos.
Aún no verificada;446permanece la última confirmada.

# 5.10.448 — controles recuperables, pendiente de CI

El throttle de inputs rechaza diferencias negativas por overflow: inputTick
Long.MIN_VALUE no puede bloquear controles indefinidamente. Cinco JUnit puros
cubren límites/rollback/extremos. Pausa/reanudación ya propaga un rechazo real
por chunks/drops envezde informar éxito y emitir cue. Dos GameTests nuevos.
Conserva cambios447: procedencia con tickets,passivepull,Eventide y contornos.
Esperados97JUnit/318GameTests. Local24recursos/54Python/parser141Java correctos.
No buildlocal ni lanzamiento de interfaz local.446sigue última verificada.

# 5.10.447 — nueva tanda integral pendiente de CI

La limpieza diferida de procedencia usa tickets: una colocación/recolocación
invalida el break anterior, sin borrar la protección del bloque nuevo. El mapa
transitorio sólo conserva posiciones con limpieza pendiente; no se serializa.
Lode Sense revalida mayBuild/actor y stack exacto tras claims. Wireframes y trazas
comparten presupuesto cosmético sin impedir selección. Eventide obtiene adaptación
RGB reproducible 32px, conserva alpha/silueta/original. Siete regresiones nuevas,
316GameTests esperados. Local24recursos/54Python/parser139Java correctos.
Sin buildlocal; compilar/servidor/cliente/publicación pendientes de CI.

# Resultado actual — 5.10.446 verificada y publicada como prueba

Fuente `63057f66abb6acd765f2dc5172c3f6fdd02a7c92`.
CI [43663630681](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43663630681)
completed/success. Evidencia Checks recuperada con manifiesto verificado.

-92JUnit,0fallos,0errores,0skips;309GameTests completos.
-Servidor empaquetado y escaneo de logs de servidor/GameTests/cliente limpios.
-32escenas,64tooltips normales/SHIFT,64marcadores viewportfit/stackevent;
 controles de teclado al servidor yWorldShatter9341completados.
-24recursos enCI;54Python envenvlocal conPillow. El render PDF opcional se
 omite enel unittestCI sinPillow;53restantes pasan. No se confunde conJUnit.
-JAR548113bytes,SHA256`ebf010a1ca26379eaed0f4428323e73485a57bf5757bbd24661a7522ed3c0bb0`;
 JARrecuperado íntegro,CRC/hashOK,sinrecetas,worldgen,clases/tags de fixtures.
-Release`test-5.10.446-43663630681`,prerelease=true,asset749449808uploaded;
 tamaño/digestAPI coinciden. No se afirma descargaCDN desdeWindows.
-Galería`artifacts/picos-5.10.446/`:PDF13páginas,26imágenes originales,
 HTML,PROVENANCE,SHA256SUMS yZIPCRCcorrecto. Íconos ligados acommit exacto.
 Capturas reales, no equivalen a aprobación de animación completa.

Inspeccionadas capturasSHIFTWorldbreaker446 ySeamRipper446. Worldbreaker ya
muestra nombre/qué/cómo/alternativa/coste/tecla manual sinclipping visible enesa
captura. No se aprueba automáticamente todo el catálogo niGhouls/shaders/TPS.

Fallos441/443/444 por tagfixture requerido encliente no se reescriben como éxito;
445 corrige referencia opcional.446 añade referenciaSHIFT legible con gatealtura.
No hubo checkout,buildlocal,instalaciónWindows ni automatización de interfaz local.

## Historial de desarrollo (las notas «pendiente» siguientes son históricas)

# 5.10.446 — SHIFT legible, pendiente de CI

Inspección de la captura real444worldbreaker-carve: SHIFT recuperó detalles pero
la ficha completa se salía por arriba. Separa referencia rápida de12líneas lógicas
(qué/cómo/alternativa/coste/tecla de manual) y manual completo desplazable, que
conserva pasivas/perfil/modos. Cliente CI ahora calcula wrapping a320px y exige
que tooltip completo quepa enel viewport capturado. No seoculta clipping
suprimiendo la prueba. Una regresión recorre13picos.309GameTests esperados.
24recursos/54Python local, parser139Java; compilación/runtime pendientes.

# 5.10.445 — corrección de tag de fixture, pendiente de CI

444/run43662073543 falló al escanear logs del cliente: needs_higher_tool exigía
superior_ore aunque testOres no registra el bloque enrunClient. No se silencia el
escáner: referencia opcional como las demás fixtures; regresión recorre tags de
fixtures y exige optional enreferencias ajenas. Los GameTests conservan asserts
de membership/tier para que optional no oculte una fixture ausente enservidor.
444 sí pasó92JUnit(sinfallos/errores/skips),308GameTests,servidor empaquetado;
cliente llegó a32escenas/64tooltips/WorldShatter9341pero no se certifica verde.
445mantiene las mejoras runtime y debe repetir todaCI.

# 5.10.444 — pendiente de CI

Mantenimiento integral: el actualizador del catálogo ya no elimina iconos_v2,
fuentes preservadas ni metadatos de animación. Regresión con árbol desechable.
Guía de13picos actualizada: elimina secundarias obsoletas de combate y explica
controles, durabilidad, permisos y alcance real de las comprobaciones.
La prueba multivariante de revocación sólo declara éxito al terminar las4familias.
Exportador PDF/ZIP comprobado con fixtures temporales:13páginas,26imágenes,
bytes originales, hashes yCRC; no son fotos de juego ni se entregan como tales.
Local:23recursos y54Python pasan envenvconPillow (sinPillow:53pasan/1skip),
parser139Java/diffcheckOK.308GameTests/92JUnit esperados pendientes deCI.

# 5.10.443 — revisión integral pendiente de CI

Cierra la ventana entre WorkProgram.next y apply: no aplica pasos con actor
invalidado. Secundarias mineras rechazan permisos revocados en callbacks;
Starfall y Seam Ripper comparten la captura de contexto del resto de utilidades.
308GameTests esperados,92JUnit esperados (no confirmados en esta revisión).
Local:23recursos,52Python,parser139Java,diffcheck. Galería reproducible con
`tools/gallery_deliverable.py`, sin arrancar juego ni automatizar interfaz local.
No entregar443como verificada sinCI;439sigue siendo la última confirmada.

# 5.10.442 — pendiente de CI, no entregar como verificada

Mensajes ES/EN de secundaria sin objetivo incluyen una orientación específica
por pico; cooldown principal/secundario muestra segundos restantes y el rechazo
por durabilidad explica el mínimo necesario. Reanudación y ejecución de colas
revalidan actor, herramienta exacta y permiso de construcción también después
de callbacks. Tres regresiones adicionales:305GameTests esperados, no comprobados.
Local:22recursos,49Python y parser139Java pasan; no se ejecutó build local.

# Estado actual — 5.10.441: segunda fase pendiente de CI

441: tiers explícitos Forge respetados aunque la mena se reconozca por nombre;
regresiones mantienen tier5/velocidad64+ en bloques vanilla y menas extranjeras.
SeamRipper32px: adaptación de contraste reproducible, alpha/silueta/fuente intactos.
Bursts/previews y pulsos de trabajo de las13familias cubiertos por reservas para
más rutas mineras (no se afirma que todo feedback legado use el mismo presupuesto).
Paleta de bursts reutilizada; saturación visual nunca cancela minería.
Cinco GameTests adicionales,302esperados;92JUnit esperados. Local22recursos,
49Python, parser139Java ydiffcheck pasados. Java/cliente pendientes, sin buildlocal.

# Estado actual — 5.10.440: primera fase pendiente de CI

440: snapshot de actor/herramienta y posición/stack exactos de drops tras permisos
para Eventide/Iridium. Movimiento de drops finito y acotado, incluso con
velocidades inválidas/extremas de otros mods. Tooltip SHIFT reconstruido también
en ItemTooltipEvent; CI ahora valida getTooltipLines completo, no sólo el builder.
Diez GameTests nuevos (297 esperados), seis JUnit nuevos (92 esperados).
Local:22recursos/49Python/parser139Java/diffcheck. Sin build local.

# Estado actual — 5.10.439: CI completo y prerelease de prueba verificados

## Resultado verificado — 5.10.439

Fuente `f07795818b39ce3d7172d7c175cb06a8992bff10`;
[CI43562331609](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43562331609)
completed/success. 86JUnit, 0fallos/errores/omitidos; 287GameTests completos.
Servidor empaquetado y logs limpios; cliente32escenas/64tooltips, teclas reales
activar/cancelar y WorldShatter9341bloques verificados. 49Python y22recursos
pasados en el workflow. No build local ni instalación Windows.

[Prerelease de prueba](https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.439-43562331609),
no latest estable ni aprobación visual completa. JAR `special-pickaxes-1.20.1-5.10.439.jar`:
541982bytes; SHA256 `37069f2693c3cbe3eb2cd7c9b65ff17dd4e266fdc9fae6d43416d5ea3b1e5c44`.
Asset746466840 uploaded y digest API coincidente. Bytes recuperados por Checks,
CRC/commit/hash y13modelos/texturas runtime verificados; no afirmar descarga CDN.

Cambios de esta tanda: pasarela sin raycast y pago sólo offhand, detención ante
obstáculo/claim/material agotado; sondeo nearest32natural con coordenadas;
revalidación de actor/procedencia/permiso en minería nativa, radiales, IcarianLift,
SeamRipper y rutas naturales; transmutación/pago con rollback tras callbacks;
firmas orientadas; tres adaptaciones de legibilidad sin cambiar alpha/silueta;
manual/SHIFT coherentes con copia, teclas reasignadas, costes y scroll/nombres.
Conserva13picos, daño base4, tier5, velocidad de tier64 y32768usos; cero recetas.

Fotos439: `artifacts/picos-5.10.439/` (ignorado por Git): PDF13páginas,
ZIP26fotos+HTML+PDF+vista general+PROVENANCE.json. Capturas genuine del cliente
439, hashes/commit/run verificados. No son imágenes generadas ni prueba de todas
las fases de animación. Los registros «pendiente» que siguen son históricos.


433 (pendiente CI): tres adaptaciones reproducibles de legibilidad, sin cambiar
silueta/alpha ni archivos originales. Manual respeta teclas reasignadas, invalida
caché de idioma/controles y evita desbordar nombres largos. Pasarela/transmutación
revalidan actor, permisos y procedencia tras eventos externos; rollback y pago
protegidos. Cuatro GameTests nuevos (277 esperados), cinco Python nuevos (33
pasados localmente); 22 validaciones de recursos y parser de 136 Java pasados.
No build local. Resultados Java/cliente todavía pendientes; no aprobación visual.

429: nuevas secundarias mineras para siete familias, sin interacciones de combate
en X; conserva utilidades de las restantes y copia Worldbreaker. Daño melee base
25→4; mineríaOP conservada. Trece firmas visuales propias, traducciones ES/EN y
galería genuine icono+mano en preparación. 263 GameTests/83JUnit esperados, pendientes.

431 fuente `47e535100fbb348e809e7a68927819f3f83700b2`,
[run43557399012](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43557399012)
completed/success. 83JUnit/263GameTests pasados, cliente32escenas/64tooltips,
teclado-servidor yWorldShatter9341. Prerelease público
[test-5.10.431-43557399012](https://github.com/Santi-PdR/Special-Pickaxes/releases/tag/test-5.10.431-43557399012)
con JAR/RELEASE.json/SHA256SUMS, no marcado latest estable. Sin aprobaciónvisual
completa. JAR536128bytes,SHA256
`6b388089413289bc53253529a31d2d6e2d8f5f1ea070584bcd20835cfa32ae46`.
Bytes recuperados por Checks y estructura/hash verificados; digest público del
asset746333222 coincide. CDN de release bloqueado en este sandbox; no afirmar
una descarga directa CDN completada ni instalación en Windows.

**430 fuente `5f7f8443d1b6a16a80e8859d156f0d47399e9f49`,
[run43551696313](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43551696313): completed/success.**
83 JUnit cero fallos/errores/skips;263GameTests pasados. JAR/servidor/logs y
cliente32escenas/64tooltips, teclado y WorldShatter9341. Bundle54fragmentos
recuperado/verificado con13capturas de mano. GaleríaPDF/HTML/ZIP en
`/home/user/entrega-picos-5.10.430`; trecepares revisados en vista general, no
aprobación temporal completa. Publishskipped, no instalación Windows.
Detalle: [MINING-ALTERNATES-430.md](MINING-ALTERNATES-430.md).

**Fuente `7904bf8a6be920e4b6227620cb3addd5a4a10e20`,
[run 43548490641](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43548490641): completed/success.**
81 JUnit sin fallos/errores/skips y All 261 required tests passed. JAR/servidor/logs
correctos; cliente real 32 escenas/64 tooltips, teclado-servidor y World Shatter
9341. Bundle Checks54 recuperado/verificado `/tmp/sp-ci428`; logs GameTest y cliente
reescaneados localmente, limpios. Sin inspección visual de esta galería, no aprobación
visual completa/temporal. Publicación binaria skipped; no instalación Windows.
Artifact forge-validation 14095304337 (40237972 bytes). RELEASE.json verificado:
JAR533981 bytes, SHA256 `32fc53ed93f7a6003554d276faa94d63bfe9c6f5cb09704d35fe49c64046de03`.
Se verificó el manifiesto del bundle, no una descarga/instalación del JAR.

427: aterrizajes Eventide protegidos frente a reentrada/cambios durante permisos;
Icarus valida caídas finitas; Starfall/Lode Sense/escaneos aliados recuperan plazos
imposibles. Flashes Starfall y ripple Eventide tienen presupuesto global. Once
GameTests (261 totales) y tres JUnit nuevos, ejecutados y pasados en 428.

427 `aa73158` / run43547927879 completed/failure: 81 JUnit pasados,
260/261 GameTests. Falló únicamente exodiumStarfallCatchRechargesAfterThirtyTicks,
que llamaba a tryCatchIncoming sin aplicar STARFALL_WARD. 428 añade la protección
al fixture y conserva la comprobación en producción; CI propio exitoso indicado arriba.

426: Estasis compartida protegida frente a reentrada, eventos inválidos y cambios
en callbacks; recuperación de cooldown y arrastre Gravitic acotado. Tres auras
beneficiosas sobre mobs ahora respetan audiencia/coste global. Diez GameTests
(250 totales) y tres JUnit añadidos: ejecutados y pasados.

**Fuente `4262a10c219873d49abba6a53509ed352b5a3c45`,
[run 43546125974](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43546125974): completed/success.**
78 JUnit, cero fallos/errores/skips; All 250 required tests passed. JAR/servidor/logs
success; cliente real 32 escenas/64 tooltips, teclado-servidor activate/cancel y
World Shatter 9341. Bundle 56 fragmentos recuperado/verificado `/tmp/sp-ci426`.
Sólo una captura 426 inspeccionada, no aprobación visual completa/temporal.
Publicación binaria skipped; no instalación Windows. Artefacto forge-validation
14094209314 (43789979 bytes). Manifiesto de JAR 532425 bytes, SHA256
`18f357ac5b551b744c626461bfaed64f3647f3fb04ba7f5843c8abfd8ddc32f0`.
Se verificó el manifiesto del bundle, no una descarga/instalación del JAR.

425 / `a9016dd` / run 43545570252: failure global. Build y 78 JUnit OK;
250 GameTests pasados. Escaneo de logs rechazó el error deliberado emitido por
Forge al lanzar la excepción del fixture179. Servidor empaquetado y cliente
omitidos. En 426 se conserva el escaneo sin excepciones/allowlists y se sustituye
el fixture por denegación/reintento; no cambia el código de gameplay de 425.

## Evidencia verificada el 2026-11-13

Rama de sesión: `arena/cec54523-special-pickaxes`. No instalación Windows ni
control de la PC. Las validaciones de una fuente no certifican cambios posteriores.

| Versión | Fuente / run | Resultado comprobado |
|---|---|---|
| 5.10.411 | `e2c3699` / [43443796300](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43443796300) | Build/JUnit, 182 GameTests, servidor y cliente completos. Falló después al exportar evidencia. Sin aprobación visual/global. |
| 5.10.412 | `3cd8c71` / [43447050583](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43447050583) | Build y 60 JUnit OK; 187 GameTests, uno fallido en fixture de propiedad Iridium. Bundle Checks recuperado y verificado. Cliente omitido. |
| 5.10.413 | `460a5af` / [43447481028](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43447481028) | Build y 60 JUnit OK; 190 GameTests, mismo único fallo. Bundle recuperado y verificado. Cliente omitido. |
| 5.10.414 | `1bbd645` / [43448248860](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43448248860) | CI global success: build/JUnit, 197 GameTests, servidor, logs, cliente y exportación de evidencia. 32 escenas/64 tooltips, 9341 bloques. Publicación binaria omitida; sin revisión visual completa. |
| 5.10.415 | `74c4481` / [43449061170](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43449061170) | CI global success confirmado. Sin inferir aprobación visual ni instalación. |
| 5.10.416 | `3e4eecb` / [43450387194](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43450387194) | CI global success; evidencia cliente recuperada/verificada (32 escenas, 64 tooltips y World Shatter). Sin aprobación visual completa ni instalación. |
| 5.10.417 | `5ec7288` / [43452383160](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43452383160) | Failure cliente por autenticación devolviendo HTML; bundle recuperado. Build/JUnit, 207 GameTests, servidor y logs pasaron. |
| 5.10.418 | `cb2c5be` / [43452644898](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43452644898) | CI global success, evidencia cliente recuperada/verificada. Sin aprobación visual completa ni instalación. |
| 5.10.419 | `4e6e60b` / [43537824437](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43537824437) | Build/JUnit, 215 GameTests, JAR, servidor y logs success; cliente en curso. |
| 5.10.420 | `0bade7a` / [43537968640](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43537968640) | Build/JUnit, 218 GameTests, JAR, servidor y logs success; cliente en curso. |

5.10.424 mejora caché térmica sin cargar chunks, deduplicación/clock de roturas,
recuperación y overflow de rachas, y presupuesto cosmético de Iridium. Ocho
GameTests y tres JUnit añadidos. Fuente `328077af660e9873578d100d5417924af0314cb8`,
[run 43543547256](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43543547256)
completed/success: 75 JUnit sin fallos/errores/skips y 240 GameTests pasados.
JAR/servidor/logs y cliente 32 escenas/64 tooltips, teclado-servidor y World Shatter
9341 correctos. Bundle de 55 fragmentos recuperado/verificado en `/tmp/sp-ci424`.
No revisión visual completa de esta galería; publicación binaria skipped, no instalación Windows.
Artefacto `forge-validation` 14092744566. Manifiesto: JAR 532550 bytes, SHA256
`1c32c52fa672b9e3ef72c1d8f70669f43afefe64e7357c79ed5a22f5bcb8615c`.
El manifiesto se verificó dentro del bundle; no se descargó/instaló el JAR aquí.

**Fuente `72b7b8b432913b3d78ba906920b5c7e88edce2ae`: [run 43540141390](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43540141390) completed/success.**
72 JUnit sin fallos/errores/skips, 232 GameTests completos, JAR/servidor/logs y
cliente real (32 escenas, 64 tooltips, controles y 9341 bloques World Shatter).
Bundle Checks recuperado/verificado en `/tmp/sp-ci423`; tres capturas revisadas,
no galería completa. Publicación binaria skipped; no instalación Windows.

421/422: build y 72 JUnit correctos, runs globales fallidos en un GameTest espacial
distinto cada uno. Bundles recuperados. 423 aísla los fixtures sin borrar sus
aserciones de gameplay; 232 GameTests ya pasados por el CI de esta nueva fuente.

5.10.422 extiende guardas/revalidación a la ruta real de controles, recupera sus
cooldowns y prueba Worldbreaker copiado. Cinco GameTests adicionales; total 232
en fuente, ya pasados por el CI de 423, que conserva estos cambios.

5.10.421 reúne cambios de combate, reentrada de alternativos, memorias tipadas y
caducidad, presupuestos cosméticos y fundido de siete partículas. Añade nueve
GameTests y ocho JUnit; 227 GameTests al añadirse; los cambios se conservan en 423, cuyo CI pasó 232.

5.10.420 añade cuatro JUnit y tres GameTests de recuperación de cooldowns
persistentes sin cambiar cadencia normal. 218 GameTests pasados
en su propio CI; cliente y resultado global aún pendientes.

5.10.419 añade cuatro regresiones de movimiento/recolocación; 215 GameTests
pasados en su propio CI; cliente y resultado global aún pendientes.

5.10.418 añade cuatro regresiones de Hellspec: atribución, reemplazos externos y
objetivos movidos en callbacks; total 211 GameTests ya pasados por el CI de esa fuente.

5.10.417 añade cinco regresiones de cancelación/sustitución/pausa y reentrada del
scheduler; 207 GameTests pasados en su propio CI.

5.10.414 corrige la confusión entre lanzador de `ItemEntity` y reserva vanilla de
recogida, refuerza permisos tras callbacks y tolera cancelación reentrante de
raíces/campos/selecciones. 5.10.415 extiende la corrección al alternativo Eventide.
Tier OP, herramienta correcta al minar y cero recetas conservados. Validación
local: 22 pruebas de recursos, 28 Python y parser de 124 archivos Java; no build local.

## Registro histórico: 5.0.0

# Rework minero — 5.0.0 validado y publicado

Se mantiene el diseño de nueve artefactos de 5.0.0. La fuente, revisión visual y reproducción binaria están aprobadas. El JAR final está publicado.
Los resultados de un commit anterior no certifican sus correcciones posteriores.

## Correcciones verificables

- Nueve herramientas combinadas; 23 modos; retiradas las expectativas de los once
  artefactos eliminados. Referencias y ocho texturas archivadas conservadas.
- Modos y manuales específicos, etiquetas españolas de Worldloom, README actual,
  migración `miningSchema=5` que no convierte modos antiguos de Worldbreaker/Crisol
  en operaciones nuevas. Eliminado el texto de construcción de costes genéricos.
- Selección con esquinas distintas, propiedad de herramienta y modo; paquetes con
  UUID y secuencia; latch físico y guardas contra reentrada de activación/scheduler.
- Bedrock, fluidos source/flowing y fluido Forge externo, claims, bloques con
  entidad, fronteras, chunks y barreras de rutas cubiertos por pruebas nativas.
- Rotura nativa revalida el objetivo tras callbacks Forge. Las reversiones de
  colocación, transmutación e intercambio no sobrescriben barreras o máquinas
  introducidas por otro listener. Dos regresiones nuevas cubren estos casos.
- Worldloom: tres estructuras pagadas y orientadas. Crisol: regional y radial.
  Axiom no muestra candidatos ocultos antes de excavar. Estasis libera velocidad
  acotada, sin aumentarla. Niveles altos de encantamiento conservados y probados.

## Evidencia reciente — no mezclar identidades de build

| Fuente | Run de Actions | Resultado observado |
|---|---|---|
| `9fc9542` | `37681746690` | Éxito completo. 55 GameTests; cliente con 23 activaciones y 46 tooltips. WORLD SHATTER retiró 9341 bloques reales y conservó la barrera. Se inspeccionaron sus dos capturas supremas, no toda su galería. No aprobado para publicación. |
| `44577ce` | `37682889162` | Run completo exitoso; anterior al endurecimiento de rollback. No certifica el candidato actual. |
| `a5c2554` | `37683328797` | Build Java 17 y los 57 GameTests aprobados; servidor empaquetado superó su etapa. Cliente falló antes de producir capturas: timeout de la ventana temprana de Forge. Run fallido, no publicable. |
| `0c7233659d3b695da9211575f3356d81a1aeb1a8` | `37685753507` | Éxito completo: 20 JUnit, 57 GameTests, 8 pruebas de recursos, servidor empaquetado, 23 modos y 46 tooltips; 9341 bloques en la escena suprema. Deshabilita únicamente el splash temprano de Forge en la configuración desechable de CI para evitar el fallo anterior. Conserva la ventana real, capturas y todas las aserciones del cliente. |

El conjunto actual contiene 20 JUnit, 57 GameTests y 8 pruebas Python de recursos.
Las ocho pruebas Python y el chequeo de diff pasaron localmente; Java se ejecuta
con Java 17 en Actions, no se afirma una ejecución Java local.

## Galería y publicación

La galería exige inventario, vista de los nueve items, 23 normales, 23 SHIFT,
cambios de modo, ejecuciones, controles físicos y escena suprema. El cliente usa
Xvfb/renderizado por software. La escena suprema tiene visión nocturna VANILLA
concedida exclusivamente por el fixture de prueba y cámara en primera persona;
no es una habilidad del artefacto ni se incluye el fixture en el JAR distribuible.
Los JPG de revisión proceden del mismo frame Robot que los PNG, vinculados por
`image-manifest.json` y hashes; no son imágenes generadas ni recreaciones.

Se revisaron las 95 copias JPG, con hashes contrastados: 23 normales, 23 SHIFT,
23 ejecuciones, 14 cambios de modo y 12 vistas adicionales. Los tooltips se
inspeccionaron en planchas de sus regiones completas; las escenas, en planchas
y vistas completas de inventario, galería y suprema. `VISUAL-REVIEW.json` registra
la procedencia y las limitaciones visuales: túneles oscuros de Ícaro/CORE DRILL,
ancla elevada de Worldloom y cancelación de seguridad ante la barrera suprema.
La publicación reconstruyó el commit exacto y comprobó SHA-256 idéntico al JAR
ya sometido a la suite completa. El workflow `37687885085` terminó exitosamente;
la descarga recuperada también pasó comparación binaria, ZIP/Java 17, catálogo
y escáner de secretos. No se publica un JAR provisional.

## Limitaciones pendientes de pruebas reales

No se usó ni modificó el repositorio siege, ni se accedió a test-1. No se probó el
modpack completo, shaders, LOD, configuraciones reales de claims ni TPS
multijugador. Las pruebas de Forge/claims no equivalen a certificar SecurityCraft,
Create, ToolLeveling, Better Combat, Relics o las configuraciones del servidor real.
La protección frente a callbacks cubre los casos descritos, no una garantía ante
modificaciones arbitrarias del mundo por cualquier otro mod.

## Identidad de la entrega final

- Fuente: `0c7233659d3b695da9211575f3356d81a1aeb1a8`. Run de suite: `37685753507`.
- JAR: `special-pickaxes-1.20.1-5.0.0.jar`, 186554 bytes.
- SHA-256 esperado: `c4b7a6f2b3041532cd4dff9823b5e8adbfc317b169153491f69f916fb0635857`.
- Publicación: run `37687885085`, aprobado. Commit de aprobación: `fb6dd2d064a9a986dd0f8259438922e5bce660f0`.
- Artefacto: [special-pickaxes-5.0.0-verified](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/37687885085/artifacts/11512342063).
- Copia local: `artifacts/5.0.0/special-pickaxes-1.20.1-5.0.0.jar`.
- Galería autocontenida: `artifacts/5.0.0/GALERIA.html` (95 capturas y hashes).
- Los commits posteriores de documentación no cambian la identidad de la fuente compilada.

429 run43551389102: failure JUnit por bootstrap de ArtifactKind en los dos nuevos tests de geometría; 81/83 pasados. 430 separa geometría pura y añade verificación de trabajo completado. CI propio pendiente.

434 pendiente CI: contexto de rotura nativa captura nivel/posición/permisos;
propaga naturalOnly a Seam Ripper, radiales, Icarian Lift y rutas naturales
Worldloom/Exodium. Tres regresiones adicionales (280 GameTests esperados).
432 terminó con 86 JUnit pasados y 272/273 GameTests: falló la comprobación
combinada de la pasarela sin apuntar. Fixture ahora despeja columna vertical y
separa las aserciones de raycast/activación. No se publicó432. 433 sigue en CI.

435 pendiente CI: SHIFT y manual comparten identidad/modo copiado; mantienen
pasivas propias. Tooltips no anuncian cambio de modo inexistente en picos de un
solo modo o copias. Costes usan teclas reasignadas y aritmética long; caché y
manual consumen la misma tarjeta de coste. Cinco regresiones nuevas (285
GameTests esperados). La rotura captura actor antes, no después, del callback
de permisos. Validadores estáticos pasados, Java/cliente aún pendientes.

436 pendiente CI: galería atada a commit/run/manifest y hashes de retratos;
verificación de bytes exactos y procedencia de modelos/texturas/animaciones del
JAR. Dieciséis pruebas Python nuevas (49 pasadas localmente), sin build local.
433 completó86JUnit y276/277GameTests: sólo falló el test combinado de pasarela
anterior a separar/despejarfixture; sus cuatro nuevas regresiones de pago/Crisol
pasaron. Ningún JAR432/433 publicado. 434/435 todavía en CI.

437 pendiente CI: Barrido de Falla reutiliza la cara del único raycast que evita
chunks descargados, sin segundo clip de24bloques; conserva procedencia natural
también tras preview y valida actor tras callbacks. Dos regresiones nuevas:
287GameTests esperados. 49Python/22recursos/parser136Java/diffcheck locales OK.

438 pendiente CI: estabiliza la regresión histórica de cancelación reentrante de
Worldloom. En435 falló sólo ese test (284/285GameTests), no los nuevos tests de
guía/contexto. El fixture ahora espera la cadencia real del mundo en lugar de
suponer un offset entre GameTest y ticks; slimes suspendidos fuera de paredes.
Sin cambiar la habilidad ni relajar sus aserciones. 287GameTests esperados.

439 pendiente CI: la espera en438 no estabilizó el fixture histórico (286/287
GameTests; sólo falló126). Regresión reentrante ahora síncrona: presta un instante
del reloj alineado, ejecuta tick/restablece reloj/listener/bindings en finally.
Evita ticks de entidades entre setup y aserción, sin cambiar la habilidad ni
reducir las dos vinculaciones/assertcalls==1. 86JUnit observados pasados en437.
