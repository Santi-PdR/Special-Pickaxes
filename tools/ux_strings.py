PAIRS={
'ux.specialpickaxes.mode':('MODE: %s','MODO: %s'),
'ux.specialpickaxes.controls':('Use: preview; use again at the same target: confirm. Sneak-use: mode / cancel.','Usar: previsualizar; repetir sobre el mismo objetivo: confirmar. Agachado + usar: modo / cancelar.'),
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
'message.specialpickaxes.released':('Stasis released.','Estasis liberada.'),
'message.specialpickaxes.confirm_again':('PREVIEW · Use again here to confirm; sneak-use cancels.','PREVIEW · Repite usar aquí para confirmar; agachado + usar cancela.'),
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
STATES={'idle':('IDLE','INACTIVO'),'selecting':('SELECTING','SELECCIONANDO'),'ready':('READY · Use to confirm','LISTO · Usar confirma'),'preparing':('PREPARING','PREPARANDO'),'executing':('EXECUTING','EJECUTANDO'),'paused':('PAUSED · Use to resume','PAUSADO · Usar reanuda'),'':('IDLE','INACTIVO')}
def enrich(data,spanish):
 for key,pair in PAIRS.items():data[key]=pair[int(spanish)]
 for key,pair in MODES.items():data['mode.specialpickaxes.'+key]=pair[int(spanish)]
 for key,pair in STATES.items():data['status.specialpickaxes.'+key]=pair[int(spanish)]
