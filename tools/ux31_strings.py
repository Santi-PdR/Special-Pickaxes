"""3.1 player text: short identities, per-artifact controls and SHIFT-only detail."""
IDENTITIES=[
('Repair the scars you left behind.','Repara las huellas que dejaste atrás.'),
('Replay your personal excavation score.','Repite tu partitura de excavación.'),
('Bend hostile trajectories while quarrying.','Curva trayectorias hostiles mientras excavas.'),
('Synchronize work between two anchors.','Sincroniza trabajo entre dos anclas.'),
('Rephase the material of a selected region.','Transforma el material de una región.'),
('Impose kinetic stasis and local mining rules.','Impone estasis y reglas locales de minería.'),
('Raise useful structures from your inventory.','Levanta estructuras útiles con tu inventario.'),
('Ride a bore through a safely cleared path.','Recorre una perforación despejada y segura.'),
('Subtract the matrix, preserve the valuable core.','Sustrae la matriz; conserva su núcleo valioso.'),
('Exchange two regions without copying matter.','Intercambia regiones sin copiar materia.'),
('A supreme tool for world-scale intervention.','Una herramienta suprema de intervención mundial.'),
('Keep and restore a geological checkpoint.','Guarda y restaura un estado geológico.'),
('Raise a parabolic vault where you aim.','Levanta una bóveda parabólica donde apuntas.'),
('Reproduce a structure, paying for its material.','Reproduce estructuras pagando sus materiales.'),
('Turn incoming projectiles away from danger.','Desvía los proyectiles que se aproximan.'),
('Find your way back through remembered footsteps.','Regresa siguiendo tus pasos recordados.'),
('Separate two materials along their contact seam.','Separa materiales siguiendo su costura de contacto.'),
('A paid path follows your steps over gaps.','Un camino pagado sigue tus pasos sobre huecos.'),
('Keep explosions from destroying nearby terrain.','Impide que explosiones destruyan terreno cercano.'),
('Seal mined passages after you leave them behind.','Sella pasos excavados cuando los dejas atrás.')]
CONTROLS=[
('Aim at a scar and Use: restore nearby memories. No corners.','Apunta a una huella y usa: restaura recuerdos cercanos. Sin esquinas.'),
('Aim and Use: replay. Sneak-use: rotate 90 degrees.','Apunta y usa: repite. Agachado + usar: rota 90 grados.'),
('Aim and Use: anchor the field and quarry. Sneak-use: polarity, or cancel active work.','Apunta y usa: ancla el dominio y excava. Agachado + usar: polaridad, o cancelar trabajo activo.'),
('Use on A, then B. Mine near A; work echoes at B. Sneak-use: unlink.','Usa sobre A y después B. Mina cerca de A para repetir en B. Agachado + usar: desconecta.'),
('Use: arm two corners. Sneak-use when idle: choose rock family.','Usar: activa dos esquinas. Agachado + usar sin selección: elige familia de roca.'),
('Aim and Use: stasis. Sneak-use: release. Mine inside to sustain it.','Apunta y usa: estasis. Agachado + usar: libera. Minar dentro prolonga el dominio.'),
('Aim and Use: build. Offhand: material. Sneak-use: shelter/bridge.','Apunta y usa: construye. Mano secundaria: material. Agachado + usar: refugio/puente.'),
('Use: launch a physical bore. Sneak-use: reverse direction.','Usar: inicia una perforación física. Agachado + usar: invierte dirección.'),
('Aim and Use: subtract geology. Sneak-use: preserve/remove ribs.','Apunta y usa: sustrae geología. Agachado + usar: conserva/elimina nervaduras.'),
('Use: arm four corners. Sneak-left: transform. Analyze, then confirm.','Usar: activa cuatro esquinas. Agachado + izquierdo: transforma. Analiza y confirma.'),
('Sneak-use: mode. CARVE and RESTORE are direct. BREAK/REPHASE/TRANSPOSE/RECORD use regions.','Agachado + usar: modo. CARVE y RESTORE son directos. BREAK/REPHASE/TRANSPOSE/RECORD usan regiones.'),
('Use: arm two corners. Sneak-use when idle: record/restore.','Usar: activa dos esquinas. Agachado + usar sin selección: registrar/restaurar.'),
('Aim at the base and Use: build the vault. Offhand: material. Sneak-use: supports.','Apunta a la base y usa: construye la bóveda. Mano secundaria: material. Agachado + usar: apoyos.'),
('Use: arm source and target corners. Sneak-left: mirror/rotate.','Usar: activa esquinas de origen y destino. Agachado + izquierdo: espejo/rotación.'),
('Aim and Use: redirect projectiles; Use again: stop. Sneak-use: reflect/shear.','Apunta y usa: desvía proyectiles; usar otra vez: detiene. Agachado + usar: reflexión/desvío lateral.'),
('Use: mark your route. Walk; Use again: retrace and consume the route. Sneak-use: forget.','Usar: marca la ruta. Camina; usar otra vez: regresa y consume la ruta. Agachado + usar: olvida.'),
('Aim at material A; offhand block chooses adjacent B. Use peels the seam. Sneak-use: exposed surface.','Apunta al material A; el bloque secundario elige el B adyacente. Usar retira la costura. Agachado + usar: superficie expuesta.'),
('Use: start/stop automatic footing. Carry plain offhand material. Sneak-use stops.','Usar: inicia/detiene apoyos automáticos. Lleva material simple secundario. Agachado + usar detiene.'),
('Use: start/stop the moving terrain ward. Sneak-use stops. Entity damage remains.','Usar: inicia/detiene la protección móvil del terreno. Agachado + usar detiene. El daño a entidades permanece.'),
('Use: start/stop sealing. Mine manually and move past holes. Offhand: filling material.','Usar: inicia/detiene el sellado. Mina manualmente y avanza más allá de los huecos. Mano secundaria: relleno.')]
EXAMPLES=[
('Repair a tunnel wall using actual stone.','Repara una pared del túnel con piedra real.'),('Repeat a staircase in another matching wall.','Repite una escalera en otra pared coincidente.'),('Clear a fighting space and divert hostile shots.','Despeja una zona y desvía disparos hostiles.'),('Work two quarry fronts at once.','Trabaja dos frentes de cantera a la vez.'),('Convert a selected stone wall to obsidian.','Convierte una pared seleccionada en obsidiana.'),('Mine while nearby hostiles remain in stasis.','Mina mientras los hostiles cercanos quedan en estasis.'),('Build a bridge without drawing a region.','Construye un puente sin dibujar una región.'),('Advance through a tunnel without noclip.','Avanza por un túnel sin atravesar paredes.'),('Expose ore by removing its stone matrix.','Expón minerales retirando su matriz de piedra.'),('Swap two 9 x 3 walls.','Intercambia dos paredes de 9 x 3.'),('Use direct cleavage now, a regional program later.','Usa ahora un corte directo; después, un programa regional.'),('Checkpoint a facade before demolition.','Registra una fachada antes de desmontarla.'),('Create an archway from a single aimed base.','Crea un arco apuntando a una sola base.'),('Repeat a facade with paid bricks.','Repite una fachada con ladrillos pagados.'),('Deflect skeleton arrows without freezing mobs.','Desvía flechas de esqueletos sin congelar mobs.'),('Retrace a winding mining route, if it is still clear.','Regresa por una ruta minera sinuosa si sigue despejada.'),('Peel stone touching deepslate, leaving the interior.','Retira piedra en contacto con pizarra sin vaciar su interior.'),('Walk over a short gap as supports appear below you.','Cruza un hueco mientras aparecen apoyos bajo tus pasos.'),('Preserve a quarry facade near a TNT blast.','Conserva una fachada cerca de una explosión de TNT.'),('Close a passage behind yourself with real cobblestone.','Cierra un paso detrás de ti con adoquín real.')]
MODES={
'reflect':('Reflect incoming projectiles','Reflejar proyectiles'), 'shear':('Shear trajectories sideways','Desviar trayectorias lateralmente'),
'return_path':('Mark / retrace footsteps','Marcar / recorrer pasos'), 'interface':('Material contact seam','Costura entre materiales'),
'exposed_surface':('Exposed skin only','Solo superficie expuesta'), 'footbridge':('Following footing','Apoyos en movimiento'),
'blast_ward':('Terrain blast ward','Protección del terreno'), 'seal_wake':('Seal the mined wake','Sellar la estela minera')}
def revise(data,spanish,ids):
 i=int(spanish)
 for key in list(data):
  if 'energy' in key or key in ['tooltip.specialpickaxes.state','hud.specialpickaxes.status']:del data[key]
 for n,id in enumerate(ids):
  data['identity.specialpickaxes.'+id]=IDENTITIES[n][i]
  data['controls.specialpickaxes.'+id]=CONTROLS[n][i]
  data['example.specialpickaxes.'+id]=('Ejemplo: 'if spanish else 'Example: ')+EXAMPLES[n][i]
  data['tooltip.specialpickaxes.secondary.'+id]=CONTROLS[n][i]
 for key,pair in MODES.items():data['mode.specialpickaxes.'+key]=pair[i]
 updates={
 'manual.specialpickaxes.shift_hint':('Hold SHIFT for details','Mantén SHIFT para más información'),
 'manual.specialpickaxes.heading':('SHIFT — Advanced information','SHIFT — Información avanzada'),
 'manual.specialpickaxes.cancel':('Sneak-use cancels active work. Large jobs: Use pauses/resumes. Tool change cancels.','Agachado + usar cancela trabajo activo. Trabajos largos: usar pausa/reanuda. Cambiar herramienta cancela.'),
 'manual.specialpickaxes.cost':('Activation wear: %s. Cooldown: %s ticks. Placement always pays real materials.','Desgaste de activación: %s. Cooldown: %s ticks. Las colocaciones pagan materiales reales.'),
 'manual.specialpickaxes.enchantments':('Efficiency / Fortune / Silk Touch / Unbreaking / Mending; huge levels supported within execution budgets.','Efficiency / Fortune / Silk Touch / Unbreaking / Mending; niveles enormes con presupuestos de ejecución.'),
 'manual.specialpickaxes.region_limits':('Default cap: 262,144 cells; loaded chunks only. Protected/incompatible cells are skipped. No inventories or fluids.','Límite por defecto: 262.144 celdas; solo chunks cargados. Se omiten celdas protegidas/incompatibles. Sin inventarios ni fluidos.'),
 'manual.specialpickaxes.direct_limits':('Bounded work, loaded chunks and protection checks. No wall traversal, unpaid blocks or inventory copying.','Trabajo acotado, chunks cargados y protecciones. Sin atravesar paredes, bloques gratuitos ni copiar inventarios.'),
 'message.specialpickaxes.trail_marked':('Route marked. Walk, then Use to return.','Ruta marcada. Camina y usa para regresar.'),
 'message.specialpickaxes.companion_active':('Companion active. Use again or sneak-use to stop.','Acompañante activo. Usa otra vez o agachado + usar para detener.'),
 'tooltip.specialpickaxes.keystone':('Raise an oriented 9-wide, 5-deep parabolic vault from the aimed base, using inventory blocks.','Levanta una bóveda parabólica orientada, de 9 de ancho y 5 de fondo, desde la base apuntada y pagando bloques.'),
 'tooltip.specialpickaxes.palimpsest':('Restore your recorded scars within 16 blocks of the aim point, using inventory material. Memories survive sessions.','Restaura tus huellas registradas a 16 bloques del punto apuntado pagando material. La memoria sobrevive sesiones.'),
 'mode.specialpickaxes.world_carve':('CARVE — direct directional cleavage','CARVE — corte direccional directo'),
 'mode.specialpickaxes.world_restore':('RESTORE — direct local checkpoint repair','RESTORE — reparación local directa'),
 }
 for key,pair in updates.items():data[key]=pair[i]
