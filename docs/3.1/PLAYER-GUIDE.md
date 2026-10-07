# Guía de los veinte artefactos — 3.1

Todos son pico, hacha y pala. Solo creativo, comandos de administrador o recompensas
externas: sin recetas, minerales ni generación de mundo. Usa el JAR tanto en cliente
como servidor. Se preservan los IDs anteriores y se descartan los datos de energía
obsoletos al leer el estado; no existe ninguna mecánica de carga.

**Usar** es clic derecho. **Agachado + usar** cambia modo cuando no hay trabajo,
salvo las acciones explícitas de liberar/desconectar que siguen. Durante trabajos
largos, usar pausa/reanuda y agachado + usar cancela. Cambiar herramienta, muerte,
dimensión, desconexión o caducidad cancelan las operaciones temporales.

El tooltip normal muestra identidad, modo y aviso de SHIFT. **Mantener SHIFT**
expande ese mismo tooltip, no abre una GUI. Incluye controles específicos, costes,
límites, ejemplo, advertencias y niveles numéricos de encantamientos.

## Interacciones directas

| ID | Usar | Agachado + usar / condición distintiva |
|---|---|---|
| `palimpsest` | Reconstruye tus huellas dentro de 16 del punto apuntado; si no hay objetivo, alrededor del jugador. Paga cada bloque. | Un solo modo. Las huellas se registran tras confirmar minería manual, no al golpear. |
| `fault_choir` | Repite una partitura de excavación registrada sobre el nuevo objetivo. Exige estados coincidentes. | Rota 0/90/180/270°. |
| `eventide` | Ancla un dominio y excava su matriz geológica. Modifica trayectorias de hostiles/proyectiles. | Alterna atraer/repeler cuando no hay trabajo. |
| `meridian` | Marca punto A y luego B. La minería manual a 8 de A se repite en B, con estado esperado. | Desconecta. No selecciona volúmenes. |
| `interregnum` | Ancla estasis local de mobs hostiles/proyectiles y concede regla minera x8 dentro. | Libera. Minar dentro prolonga su duración; no es un recurso acumulable. |
| `worldloom` | Construye refugio o puente predefinido de 48 bloques, pagando el material secundario. | Alterna refugio/puente. |
| `icarus` | Perfora y avanza físicamente por el camino despejado, longitud 24 por defecto. | Invierte dirección. Un obstáculo impide avanzar; no hay noclip. |
| `hollow_axiom` | Sustrae matriz inerte conservando minerales. | Alterna sustracción o nervaduras estructurales. |
| `keystone` | Desde una base apuntada levanta bóveda parabólica orientada, ancho 9, fondo 5, elevación 4. | Alterna apoyos. Material secundario simple; piedra por defecto. Sin esquinas. |
| `aegis` | Ancla protección de trayectorias; usar otra vez detiene. No congela mobs. | Alterna reflexión hacia afuera/desvío lateral. Excluye disparos de jugadores y aliados, flechas incrustadas y proyectiles inmóviles. No cambia daño ni propietario. |
| `lodestar` | Marca una ruta; camina sosteniéndolo; usar otra vez recorre los pasos en orden inverso y consume la ruta. | Olvida. Guarda últimas 128 posiciones, desplazando el ancla al llenarse. Cada tramo debe seguir despejado/cargado y medir como máximo 8. No excava ni atraviesa paredes. |
| `seam_ripper` | Apunta a material A; el bloque secundario elige B. Extrae solo la interfaz conectada de A adyacente a B. | Alterna contacto/superficie expuesta al aire. Hasta 512 posiciones examinadas y 256 extraídas, no una veta completa. |
| `causeway` | Activa/desactiva apoyos pagados bajo tus pasos y justo delante. | Detiene. A la cota inicial, máximo dos candidatos cada 5 ticks; no eleva, vuela ni rescata caídas rápidas. |
| `counterseal` | Activa/desactiva protección móvil del terreno ante explosiones. | Detiene. No da invulnerabilidad, no cambia la lista de entidades dañadas y no genera explosiones. |
| `covenant` | Activa/desactiva sellado de huecos de minería manual confirmada. | Detiene. Hasta 128 pendientes; revisa cuatro cada 5 ticks, detrás y a más de 2 bloques. Relleno pagado, solo aire y sin encerrar actores. |

Los tres acompañantes finales duran 400 ticks por defecto y exigen sostener su
herramienta. Aegis y Counterseal tienen radio máximo 8, reducido si lo requiere
la configuración de radio. El escaneo de Counterseal se limita por explosión a
4096 bloques, 16384 comparaciones con propietarios y 1024 comprobaciones de permiso;
**una megadetonación puede quedar protegida solo parcialmente**.

## Regiones, solo cuando hacen falta

| Artefacto / modo | Esquinas | Propósito |
|---|---:|---|
| `bifold_atlas` | 4 | Permuta dos volúmenes separados; conserva materia, no da loot. |
| `paradox_crucible` | 2 | Transforma geología a piedra, pizarra, basalto u obsidiana. |
| `chronicle` RECORD / RESTORE | 2 | Registra un checkpoint deliberado o reconstruye sus huellas pagando. |
| `tessellator` | 4 | Copia un plano de estados inertes, pagando materiales; no copia inventarios/NBT. |
| `worldbreaker` BREAK / REPHASE / RECORD | 2 | Minería, transformación o registro regional. |
| `worldbreaker` TRANSPOSE | 4 | Intercambio regional transformado. |
| `worldbreaker` CARVE | **ninguna** | Corte direccional estacionario; longitud 48 por defecto, tope 64. No transporta al jugador. |
| `worldbreaker` RESTORE | **ninguna** | Restauración pagada del checkpoint dentro de 16 del punto apuntado. |

Usar activa la selección; izquierdo marca esquinas. Agachado + izquierdo rota modo
o transformación. Usar analiza y muestra LISTO; otro usar confirma. El análisis
no cambia bloques. Las regiones deben estar cargadas, no solaparse cuando hay dos
y tener dimensiones compatibles con la transformación. Se conservan pausa,
reanudación, previsualización, contadores y cancelación.

## Costes, encantamientos y límites

- Desgaste de activación por defecto: **4**, cooldown **40 ticks**; el tooltip
  recibe los valores reales del servidor al sostener la herramienta. Mending
  rebaja activaciones de Palimpsest, Chronicle y Tessellator. Minar consume además
  durabilidad nativa. Unbreaking reduce desgaste; Mending repara con XP real.
- Cada colocación consume un bloque simple real en supervivencia. No se consumen
  pilas con NBT personalizado. Creativo sigue su excepción habitual.
- Efficiency acelera minería y aumenta presupuesto por trabajo de forma acotada.
  Fortune conserva comportamiento nativo hasta **4096** y crece logarítmicamente
  por encima; Silk Touch se respeta. Niveles **1000+** se leen sin truncación a
  byte/short; el mod no puede recuperar un valor ya truncado por otro mod/API.
- Por defecto: 24 intentos/jugador/tick, 192 globales, techo encantado 128;
  4096 pasos directos, 32 trabajos, selección máxima 262144 celdas, tether 512.
  Una región grande se recorre por ticks, no se materializa como un trabajo directo.
- Memorias/checkpoints: **4096 registros / 24 horas** por defecto. Seleccionar una
  región mayor NO amplía automáticamente la memoria; quedan los registros que caben.
- Se revalidan estados y permisos al aplicar. Forge harvest/loot/break/place y
  `AbilityUseEvent` siguen siendo la frontera de integración. Sin cargar chunks,
  copiar block entities, inventarios, fluidos o romper bloques irrompibles.
- Los intercambios son atómicos por pareja, no una transacción global con undo.
  Los snapshots regionales están acotados por trabajo, sin cuota agregada de RAM.
  No se promete copiar arquitectura arbitraria con propiedades/NBT.

## Identidad visual

Modelos `minecraft:item/handheld` y texturas existentes de picos de diamante,
netherita, hierro u oro. No se redistribuyen PNG de Minecraft ni de referencias.
Glint permanente, pequeños halos animados de cabeza, partículas de impacto y
operación, colores y sonidos identifican los artefactos. El halo es cosmético
local; las operaciones producen feedback de servidor. Sin librería de animación
externa ni los sprites experimentales de 3.0.
