# 5.10.430 — herramientas antes que armas

Pedido explícito: mejorar efectos/habilidades, secundarias sin criaturas y daño menor.
No se añadieron recetas ni se redujo tier/velocidad/durabilidad de minería.

Daño melee sin encantamientos: 25 → **4 puntos** (dos corazones). Encantamientos,
efectos y firmas de golpe preexistentes todavía pueden añadir resultados: no es
un cap universal al daño final. Primarias conservan sus mecánicas; se añade una
firma visual de herramienta a cada activación primaria/secundaria, dieciséis puntos
con forma/color propios, presupuesto global y audiencia local. Las secundarias ya
no atacan, empujan, frenan, queman, marcan criaturas ni interceptan proyectiles.
Las primarias de campo y pasivas defensivas existentes no se eliminan aquí.

| Pico | Secundaria X actual |
|---|---|
| Palimpsest | Sondeo: hasta 32 menas cargadas en cubo de radio6 alrededor del objetivo, sin destruir bloques. Las partículas de menas enterradas no equivalen a un render x-ray garantizado. |
| Fault Choir | Galería de arco 3 ancho ×3 alto ×8 largo; esquinas superiores conservadas. |
| Eventide | Imán de hasta64 drops propios/sin reserva en radio16; no criaturas ni proyectiles. |
| Paradox Crucible | Muestra: un bloque natural al material del modo actual. |
| Interregnum | Extracción de precisión de un bloque de geología/mena, sin pulso stasis ni traslado de campo. |
| Worldloom | Pasarela de hasta9 bloques hacia delante, pagando material inerte de offhand. Requiere apuntar a un bloque al activar. |
| Icarus | Ascensor minero de 1×2, con movimiento seguro del propio minero. |
| Hollow Axiom | Exposición: despeja geología de seis caras contiguas a una mena; conserva el núcleo. |
| Worldbreaker | Copia la utilidad minera del pico seleccionado, nunca las antiguas técnicas de combate. |
| Exodium | Pozo octogonal 5×5 hasta32 hacia abajo. |
| Iridium | Recogida de hasta96 drops de sus propias menas en radio48. |
| Hellspec | Chimenea 3×3 hasta12 sobre el objetivo; sin erupción/daño/fuego a criaturas. |
| Seam Ripper | Barrido de una cara natural 7×7, hasta49 bloques. |

Nuevas operaciones de bloques usan WorkQueue/WorldSafety y respetan claims,
chunks cargados, herramientas/tier, naturales/playerplaced y drops normales.
Pasarela no crea materiales gratis. Siete contratos GameTest de antiguas acciones
fueron actualizados, tres tests de erupción reemplazados por no-interacción, y se
mantienen pruebas independientes de pasivas/lifecycle que no son secundarias.

## Resultado comprobado

Fuente `5f7f8443d1b6a16a80e8859d156f0d47399e9f49`,
[run43551696313](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/43551696313),
completed/success. 83 JUnit, cero fallos/errores/skips; 263 GameTests pasados,
incluyendo utilidad completada (romper, rephase y pago real). JAR/servidor/logs y
cliente real 32escenas/64tooltips, controles y WorldShatter9341 correctos.
429 falló dos JUnit nuevos por bootstrap SoundEvents; 430 separó geometría pura y
pasó el conjunto. No se reescribe ese fallo como éxito.

## Fotos entregadas

`/home/user/entrega-picos-5.10.430`: galeria.html, galeria-picos.pdf (13 páginas),
vista-general.jpg y picos-iconos-y-en-mano.zip con los 26 archivos individuales.
Iconos extraídos del layer0 del modelo actual, primer frame si hay animación,
ampliados nearest-neighbor. Capturas en mano provenientes del cliente real de CI,
reducidas a640×360; no son renders generados desde el icono. Todas las trece se
inspeccionaron en una vista general comparativa; esto no aprueba toda la galería
ni la animación temporal. Algunas muestran efectos/glint y estado copiado del
pico tal como existían en ese frame. No se afirma una sesión instalada en Windows.

BundleChecks54 recuperado/verificado `/tmp/sp-ci430`, con13portraits. Artefacto
forge-validation14097126723. Manifiesto JAR536128bytes, SHA256
`5d4fc9bed26e922ef139235c17fe1f7d6a55f63d9c4279b214f67bc56613de9a`.
Manifiesto verificado, no JAR descargado/instalado. Publicación binaria skipped.
