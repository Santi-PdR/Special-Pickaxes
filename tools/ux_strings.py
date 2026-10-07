PAIRS={
'ux.specialpickaxes.intro_region':('Relic: Use arms; left clicks select; Use analyzes, then confirms. Details in the tooltip.','Reliquia: usar activa; clics izquierdos seleccionan; usar analiza y luego confirma. Detalles en el tooltip.'),
'ux.specialpickaxes.cost.paid':('One plain inventory block per placement. Activation wear: %s.','Un bloque simple del inventario por colocación. Desgaste al activar: %s.'),
'ux.specialpickaxes.cost.matter':('Conserves matter; no generated loot. Activation wear: %s.','Conserva materia; no genera drops. Desgaste al activar: %s.'),
'ux.specialpickaxes.cost.mining':('Activation wear: %s; mining also uses normal durability.','Desgaste al activar: %s; minar consume además durabilidad normal.'),
'ux.specialpickaxes.link':('Link distance %s blocks · rotation %s°','Enlace de %s bloques · rotación %s°'),
'ux.specialpickaxes.volumes':('SOURCE %s · TARGET %s','ORIGEN %s · DESTINO %s'),
'message.specialpickaxes.volume_limit':('Volume %s exceeds configured limit %s.','El volumen %s supera el límite configurado %s.'),
'message.specialpickaxes.overlap':('INVALID · Source and target overlap. No operation queued.','INVÁLIDO · Origen y destino se solapan. No se encola la operación.'),
'message.specialpickaxes.incompatible':('Incompatible dimensions: %s -> %s. A 90° turn swaps width and depth.','Dimensiones incompatibles: %s -> %s. Un giro de 90° intercambia ancho y profundidad.'),
'message.specialpickaxes.backpressure':('Queue full. Wait for another operation to finish.','Cola llena. Espera que termine otra operación.'),
'ux.specialpickaxes.mode':('MODE: %s','MODO: %s'),
'ux.specialpickaxes.region_controls':('Use: arm selection. Left click: corners. Sneak-left: mode/transform. Use: analyze; use at READY: execute.','Usar: activar selección. Clic izquierdo: esquinas. Agachado + izquierdo: modo/transformación. Usar: analizar; usar en LISTO: ejecutar.'),
'ux.specialpickaxes.cancel_help':('While working: Use pauses/resumes; sneak-use cancels. Switching tools cancels work.','Trabajando: usar pausa/reanuda; agachado + usar cancela. Cambiar herramienta cancela el trabajo.'),
'ux.specialpickaxes.resources':('Paid construction/restore: plain inventory blocks. No recipes. No unloaded chunks or machines.','Construcción/restauración: bloques simples del inventario. Sin recetas. No procesa chunks descargados ni máquinas.'),
'ux.specialpickaxes.history':('MEMORIES %s · OLDEST %sm · LAST SCAR %sm','RECUERDOS %s · MÁS ANTIGUO %sm · ÚLTIMA HUELLA %sm'),
'message.specialpickaxes.armed':('SELECTING · Left click corner A, then B. Sneak-use cancels.','SELECCIONANDO · Clic izquierdo en esquina A y luego B. Agachado + usar cancela.'),
'message.specialpickaxes.corner':('Corner %s selected. Continue with left click.','Esquina %s seleccionada. Continúa con clic izquierdo.'),
'message.specialpickaxes.selection_ready':('READY · All corners selected. Use to analyze before committing.','LISTO · Esquinas seleccionadas. Usar analiza antes de ejecutar.'),
'message.specialpickaxes.selection_invalid':('INVALID · Check size, transform dimensions, overlap, range and permissions.','INVÁLIDO · Revisa tamaño, dimensiones transformadas, solapamiento, alcance y permisos.'),
'message.specialpickaxes.need_corners':('Corners %s / %s · Left click selects the next corner.','Esquinas %s / %s · Clic izquierdo selecciona la siguiente.'),
'message.specialpickaxes.transform':('TRANSFORM: %s · Observe the source-to-target line.','TRANSFORMACIÓN: %s · Observa la conexión origen-destino.'),
'message.specialpickaxes.named_mode':('MODE: %s','MODO: %s'),
'message.specialpickaxes.unlinked':('DISCONNECTED · Use to choose a new ANCHOR A.','DESCONECTADO · Usar elige una nueva ANCLA A.'),
'message.specialpickaxes.released':('Field released.','Dominio liberado.'),
'message.specialpickaxes.progress':('%s · %s / %s · eligible/successful %s · Use: pause/confirm','%s · %s / %s · aptos/exitosos %s · Usar: pausa/confirma'),
'message.specialpickaxes.chunk_pause':('PAUSED · Next chunk is unloaded. Load it, then Use to resume.','PAUSADO · El siguiente chunk está descargado. Cárgalo y usa para reanudar.'),
'message.specialpickaxes.complete':('COMPLETE · %s successful; %s skipped.','COMPLETO · %s exitosos; %s omitidos.'),
}
MODES={
'restore':('Restore missing scars','Restaurar huecos registrados'),'record':('Record checkpoint','Registrar estado'),
'rotate0':('Stencil 0°','Plantilla 0°'),'rotate90':('Rotate 90°','Rotar 90°'),'rotate180':('Rotate 180°','Rotar 180°'),'rotate270':('Rotate 270°','Rotar 270°'),
'attract':('Attraction','Atracción'),'repel':('Repulsion','Repulsión'),'link':('Synchronized spatial work','Trabajo espacial sincronizado'),
'stone':('Rephase: stone','Transformar: piedra'),'deepslate':('Rephase: deepslate','Transformar: pizarra'),'basalt':('Rephase: basalt','Transformar: basalto'),'obsidian':('Rephase: obsidian','Transformar: obsidiana'),
'stasis':('Kinetic stasis','Estasis cinética'),'shelter':('Hollow shelter','Refugio hueco'),'bridge':('Bridge','Puente'),
'forward':('Forward bore','Perforación frontal'),'reverse':('Reverse bore','Perforación inversa'),'ribs':('Preserve ribs','Conservar nervaduras'),'subtract':('Geological subtraction','Sustracción geológica'),
'identity':('Direct correspondence','Correspondencia directa'),'mirror_x':('Mirror X','Espejo X'),'mirror_z':('Mirror Z','Espejo Z'),
'world_break':('WORLD BREAK','RUPTURA MUNDIAL'),'world_carve':('WORLD CARVE · elliptical Z tunnel','TALLADO · túnel elíptico Z'),
'world_rephase':('WORLD REPHASE · offhand rock','TRANSFORMACIÓN · roca secundaria'),'world_transpose':('WORLD TRANSPOSE','TRANSPOSICIÓN MUNDIAL'),
'world_restore':('WORLD RESTORE','RESTAURACIÓN MUNDIAL'),'world_record':('WORLD RECORD','REGISTRO MUNDIAL'),
'vault':('Parabolic vault','Bóveda parabólica'),'supported_vault':('Vault with pier ribs','Bóveda con nervios de apoyo')}
STATES={'selected':('SELECTION READY · Use to analyze','SELECCIÓN LISTA · Usar analiza'),'domain':('DOMAIN ACTIVE','DOMINIO ACTIVO'),'linked':('LINK ACTIVE · mine near A','ENLACE ACTIVO · mina cerca de A'),'anchor_b':('ANCHOR A set · aim B and Use','ANCLA A fijada · apunta B y usa'),'idle':('IDLE','INACTIVO'),'selecting':('SELECTING','SELECCIONANDO'),'ready':('READY · Use to confirm','LISTO · Usar confirma'),'preparing':('PREPARING','PREPARANDO'),'executing':('EXECUTING','EJECUTANDO'),'paused':('PAUSED · Use to resume','PAUSADO · Usar reanuda'),'':('IDLE','INACTIVO')}
def enrich(data,spanish):
 for key,pair in PAIRS.items():data[key]=pair[int(spanish)]
 for key,pair in MODES.items():data['mode.specialpickaxes.'+key]=pair[int(spanish)]
 for key,pair in STATES.items():data['status.specialpickaxes.'+key]=pair[int(spanish)]
