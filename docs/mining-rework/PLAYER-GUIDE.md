# Rediseño 5.13.0 — build de prueba verificada

CI `44234259503` aprobada sobre `6aed2b07962f8511d86c4061a1b8e1181774c033`:
161 JUnit y 400 GameTests, cliente gráfico y servidor empaquetado.
Selector de Rompemundos con flechas/Tab, Inicio/Fin, vista previa y Enter para copiar.
Ver `INTERFACE-5.12.md` para los nombres y el alcance de la revisión visual.
Sin garantía de compatibilidad Windows/Ghouls o revisión de todas las animaciones.

Los13picos usan sprites originales de terceros (Mythic Metals), sin retoques ni
arte generado. Las partículas de equipamiento/Curios y bordes persistentes se
retiran. Solo una activación válida muestra una ráfaga de partículas custom.
El manual presenta tres bloques: principal, secundaria y controles actuales.

Corona de Stasis ya no extrae un solo bloque: Fractura Temporal abre un volumen
de11×11×11, extrayendo roca y mineral con drops normales. La cola evita ejecutar
1331mutaciones en un tick y conserva claims, bedrock y bloques construidos.
Axiom vacía ese volumen conservando menas; Crucible transmuta una esfera de
radio12; Choir abre una galería abovedada de9×7×48 (2832celdas); Hellspec una chimenea
de11×11×48 (5808celdas); ambas procesadas por turnos. Worldloom
construye un puente de64bloques pagados; Eventide atrae botín libre/propio en48
bloques; Worldbreaker tiene fractura nativa o secundaria copiada; Seam barre
15×15; Exodium baja hasta64. Icarus/Iridium conservan sus mecánicas principales.

Defaults de mundo nuevo:32intentos por jugador/tick,192globales, menas96,
secundaria mínima2segundos, primarias0.5segundos. Los mundos existentes conservan
los valores de su serverconfig: cambiarlo explícitamente si se quiere el nuevo
perfil; no borres configuraciones ajenas ni mundos. Las habilidades se pueden
cancelar con el control mostrado en el manual. Usa una copia de seguridad.

No hay publicación automática de binarios de estos assets personales; no es
una aprobación visual ni de compatibilidad con todo Ghouls.

## Historial

## Revisión 5.10.467 — CI completa correcta

Los campos no siguen entregando beneficios ni alterando proyectiles/drops
después de que otro mod invalide el contexto durante sus callbacks. En
particular Stasis corta entregas pendientes, Crucible respeta la nueva
propiedad/posición del proyectil y Eventide no carga desde copias de herramienta.
Worldloom vuelve a comprobar permisos en el ancla antes de devolver al objetivo.
Se conservan los beneficios ya autorizados: no hay rollback arbitrario de efectos.
El manual se recompone tras recargar idioma; Crucible tiene contraste adaptado
sin reemplazar el dibujo original. No se añade daño ni combate a secundarias.
126JUnit/382GameTests, cliente32escenas/64tooltips y servidor empaquetado verdes.
Esto es una prerelease de prueba, no aprobación del pack Ghouls completo.

## Historial de cambios (los pendientes inferiores son históricos)

## Revisión 5.10.463 — pendiente de CI

Las auras compartidas detienen entregas pendientes si un mod mueve al portador
o a un compañero durante un efecto. Los flashes de minería y varios contornos
respetan el presupuesto cosmético sin quitar sus beneficios. Las partículas
ya no revelan al portador invisible por comprobar la visibilidad al revés.
El manual vuelve a maquetar tras reinicializarse o cambiar la instancia de
idioma, incluso si el nombre del idioma no cambió. Crucible tiene una adaptación
reproducible de contraste conservando el dibujo original.

## Revisión 5.10.460 — CI completa correcta

Las roturas diferidas de Iridium sólo completan sus efectos si conservas la
herramienta y el mismo pico Curios: una copia nueva no hereda trabajo pendiente.
Si otro mod te mueve o quita la fuente al conceder Lode Surge, no se continúa
marcando drops ni compartiendo ese evento con compañeros desde otra posición.
Los efectos ya concedidos no se borran arbitrariamente. Interregnum recibe una
adaptación de contraste manteniendo el dibujo y la transparencia originales.
Daño reducido, potencia minera, cero recetas y secundarias mineras sin cambios.

## Revisión 5.10.458 (incluye457; CI completa correcta)

Los cambios de posición o permisos efectuados por claims durante una pasiva
no redirigen su respuesta a una ubicación nueva. Cambiar de herramienta antes
de confirmar una rotura diferida de Iridium, o pasar a espectador, descarta esa
observación: no añade ritmo minero ni marca drops ajenos. Los enlaces visuales
entre aliados comparten el límite cosmético; perder el enlace por saturación no
elimina la protección. No cambia el daño, las recetas ni las secundarias.

# Guía del catálogo minero

Forge 1.20.1, Java 17. Catálogo de **13 picos**, sin recetas de fabricación.
Esta guía describe el comportamiento implementado; el resultado de cada revisión
se registra en `STATUS.md`. No certifica compatibilidad con el pack Ghouls completo.

## Controles predeterminados

Se pueden reasignar en Opciones → Controles → Special Pickaxes; los tooltips usan
la tecla configurada, no una letra fija. Las instrucciones largas del manual y
de SHIFT también siguen las teclas de acción, modo y secundaria reasignadas.

| Tecla | Acción |
|---|---|
| R | Activar la técnica principal; con trabajo activo pausa/reanuda, y cuando está LISTO confirma. |
| X | Técnica secundaria de utilidad minera; si hay trabajo/selección activa, lo cancela primero. No ataca ni controla criaturas/proyectiles. |
| C | Cambiar modo cuando el pico tiene varios. |
| I | Abrir el manual del pico en mano, o del equipado en Curios si la mano no lleva uno. |
| B | Armar selección regional, cuando el modo lo permite. |
| Clic izquierdo | Marcar esquinas durante la selección. |
| Enter | Confirmar una región cuando termina el análisis y aparece LISTO. |
| K | Pausar/reanudar trabajo; confirmar también exige conservar la herramienta original. |
| V | Cancelar el trabajo o selección. |

El tooltip compacto muestra tres líneas; **mantén SHIFT** para ampliar instrucciones,
costes y controles. Las pasivas, el perfil completo y la lista de modos están en
el manual desplazable, evitando desbordar la pantalla con SHIFT. El manual permite elegir qué
pico copia Worldbreaker. Las teclas R/X actúan sobre la herramienta en la mano
principal: Curios aplica pasivas, no añade un segundo conjunto de habilidades activas.

## Qué hace cada pico

| Pico | Técnica principal R | Secundaria X |
|---|---|---|
| Palimpsest | Sigue una veta conectada de mena. | Sondea hasta 32 menas naturales cercanas al objetivo, sin romperlas. |
| Fault Choir | Túnel 2×2 hasta 20 bloques hacia delante. | Galería arqueada de 3 de ancho, 3 de alto y 8 de largo; conserva esquinas superiores. |
| Eventide | Campo gravitatorio marcado; C cambia su polaridad. | Atrae hasta 64 drops cercanos no reservados a otro jugador, en radio 16. |
| Paradox Crucible | Transmuta geología natural en una región o por proximidad; C selecciona material. | Convierte una muestra natural al material seleccionado. |
| Interregnum | Dominio de estasis marcado o aura, según el modo. | Extracción de precisión de un bloque natural picable; no mueve ni activa un campo contra criaturas. |
| Worldloom | Excava una esfera de geología natural de radio 8. | Pasarela de hasta 9 bloques desde tus pies hacia delante, pagada con bloques inertes sin NBT de la mano secundaria. |
| Icarus | Perforación hacia delante. | Ascensor minero 1×2 que desplaza de forma segura al propio minero; no funciona montado o durmiendo. |
| Hollow Axiom | Excava roca en un volumen radial conservando las menas. | Despeja geología de las seis caras contiguas a la mena apuntada, preservando el núcleo. |
| Worldbreaker | Cortes direccionales, pozo vertical, cuña escalonada y selección regional; puede copiar otro pico desde el manual. | Ejecuta la utilidad minera del pico copiado. Necesita elegir origen primero. |
| Exodium | Lanza minera 13×9 hasta 48 bloques hacia delante. | Pozo octogonal de sección 5×5 hasta 32 bloques hacia abajo. |
| Iridium | Busca y extrae menas cargadas desde el centro de un volumen elipsoidal 29×15×29, por tandas. | Recupera hasta 96 drops de tus menas marcadas por Iridium, en radio 48. |
| Hellspec | Cantera de cinco pétalos de geología y menas naturales. | Chimenea 3×3 hasta 12 bloques por encima del objetivo. |
| Seam Ripper | Filón conectado de piedra natural del mismo material, hasta 256 bloques. | Barrido 7×7 de la cara de piedra natural apuntada; conserva menas y bloques colocados. |

Las secundarias no tienen acciones de combate. Las **primarias de campo, pasivas
defensivas y firmas de golpe preexistentes** no se han eliminado: no confundas la
restricción de X con una eliminación global de toda interacción con criaturas.

## Coste y potencia minera

- Daño base melee sin encantamientos: **4 puntos**, no los 25 anteriores. No es un
  límite universal al daño final de encantamientos, otros mods o efectos.
- Tier V, durabilidad base 32.768 y velocidad minera base 64; los perfiles de cada
  pico aumentan la velocidad en sus materiales especializados.
- Las menas extranjeras pueden reconocerse aun sin tags vanilla de herramienta.
  La revisión 441 y posteriores respetan requisitos explícitos de tiers superiores
  registrados en Forge; esto no sustituye una prueba con cada mod instalado.
- Por defecto, activar cuesta 2 de durabilidad y la principal recarga 1 segundo.
  La secundaria recarga al menos 5 segundos. Configuración y encantamientos pueden
  modificar costes o trabajo; SHIFT/manual muestran el perfil sincronizado.
- Una activación necesita dejar **al menos un punto de durabilidad**. Si no alcanza,
  repara el pico. Una secundaria rechazada por falta de operación no consume su
  durabilidad de activación ni su enfriamiento.

## Si una técnica no funciona

1. Conserva el pico original en la mano principal. Cambiarlo por una copia con el
   mismo NBT no permite reanudar su trabajo anterior.
2. Revisa objetivo, modo y material. Eventide necesita drops elegibles; Iridium
   necesita tus drops marcados; Worldloom requiere material en la otra mano y la
   primera celda de suelo libre; Worldbreaker necesita un origen seleccionado.
3. Espera el enfriamiento indicado. Repara si no queda durabilidad suficiente.
4. Para regiones, espera el análisis y confirma; no basta con marcar esquinas.
5. Si hay congestión de drops, recógelos y reanuda. Si falta un chunk, vuelve a
   cargarlo normalmente; las habilidades no fuerzan su carga.
6. Revisa permisos del servidor, modo de juego y límites de trabajos activos.
   Un rechazo no autoriza a saltarse un claim ni a continuar tras un cambio de permisos.

Las operaciones de área se reparten por ticks y comprueban cada bloque. Las rutas
se detienen ante sus barreras; las operaciones protegen fluidos, bedrock, máquinas
y celdas no autorizadas. Los modos regionales reducen la velocidad manual durante
selección/preparación/ejecución para evitar roturas accidentales, no la ponen a cero.
El seguimiento de bloques colocados no reconstruye el historial anterior a la
instalación del mod: no puede identificar toda construcción antigua.

Los efectos y sonidos son cosméticos limitados. Si se agota el presupuesto de
partículas, puede omitirse una señal sin cancelar una excavación autorizada. Una
captura fija de CI no demuestra la animación completa ni una medición de TPS.

## Galería vinculada a una versión

`tools/fetch_ci_evidence.py` recupera la evidencia con manifiesto de un run.
`tools/gallery_deliverable.py <carpeta-client-smoke> <salida-vacía>` valida los
13 retratos, el commit y los iconos exactos antes de crear el PDF de 13 páginas,
26 imágenes individuales, HTML, procedencia y ZIP con hashes. Requiere Pillow;
no compila el mod, no arranca el juego y no controla la interfaz del usuario.

Las colocaciones pagadas rechazan cambios de creativo/supervivencia durante un
callback de permisos o colocación: no dejan material gratis ni gastan un pago
antiguo. Los previews usan hasta 24 muestras distribuidas por todo el plan,
incluido su extremo final, sin aumentar el presupuesto de partículas.

El ascensor de Icarus se detiene si sales de su eje o un mod te teletransporta
fuera del avance esperado: no continúa cortando ni te arrastra a tu posición
anterior. Las partículas de sintonía Curios pueden omitirse por presupuesto sin
impedir las pasivas. Al reconectar sólo se sincronizan recargas con plazos válidos.
