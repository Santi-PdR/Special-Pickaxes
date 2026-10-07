PLAY=[
('Aim at your old excavation and activate to repair it.','Apunta a tu excavación anterior y activa para repararla.'),
('Mine a shape, then aim elsewhere and replay it. Change mode to rotate.','Excava una forma; apunta a otro lugar y repítela. Cambia modo para rotarla.'),
('Aim and activate a gravitational quarry. Choose attraction or repulsion.','Apunta y activa una cantera gravitatoria. Elige atraer o repeler.'),
('Activate at A and B. Mining near A echoes at B. Secondary unlinks.','Activa sobre A y B. Minar cerca de A se repite en B. Secundaria desconecta.'),
('Choose the rock family, select two corners and confirm when ready.','Elige la roca, selecciona dos esquinas y confirma cuando esté listo.'),
('Aim and activate stasis. Mining inside sustains it. Secondary releases.','Apunta y activa estasis. Minar dentro la mantiene. Secundaria libera.'),
('Choose shelter or bridge. Hold building material in your offhand; aim and activate.','Elige refugio o puente. Lleva material en la mano secundaria; apunta y activa.'),
('Look along the tunnel and activate. Reverse mode bores backwards.','Mira hacia el túnel y activa. El modo inverso perfora hacia atrás.'),
('Aim at the matrix around ore. Activate to remove it, optionally leaving ribs.','Apunta a la matriz que rodea minerales. Activa para retirarla, con nervaduras opcionales.'),
('Select source and destination corners. Choose a transform, then confirm.','Selecciona esquinas de origen y destino. Elige transformación y confirma.'),
('Convergence creates a sanctuary inside a shattered ring. Other modes specialize in regional work.','Convergencia crea un santuario dentro de un anillo fracturado. Otros modos trabajan regiones.'),
('Record a selected area before excavation. Restore mode repairs its empty scars.','Registra una zona antes de excavar. Restaurar repara sus huecos.'),
('Aim at the base of your vault. Offhand selects its material.','Apunta a la base de tu bóveda. La mano secundaria elige el material.'),
('Select a blueprint and its destination, then confirm a paid copy.','Selecciona plano y destino; confirma una copia pagada.'),
('Aim and activate projectile redirection. Choose outward reflection or lateral shear.','Apunta y activa el desvío de proyectiles. Elige reflexión o desvío lateral.'),
('Activate to mark. Walk holding the relic; activate again to retrace.','Activa para marcar. Camina sosteniendo la reliquia; activa otra vez para regresar.'),
('Aim at A with B in your offhand to peel their contact seam.','Apunta a A con B en la mano secundaria para retirar su costura de contacto.'),
('Activate, then walk over a gap. Offhand selects the paid footing.','Activa y camina sobre un hueco. La mano secundaria elige los apoyos pagados.'),
('Activate a moving ward against terrain destruction. Secondary ends it.','Activa protección móvil contra destrucción del terreno. Secundaria la termina.'),
('Activate and mine forward. Holes behind you are filled using your chosen material.','Activa y excava hacia delante. Se rellenan los huecos que dejas atrás con tu material.')]
WARN=[
('Only your remembered scars; bring matching blocks.','Solo tus huellas recordadas; lleva bloques coincidentes.'),('The destination rock must match the original.','La roca de destino debe coincidir con la original.'),('Does not protect you from all damage.','No te protege de todo daño.'),('Both anchors must be in the same loaded dimension.','Ambas anclas deben estar en la misma dimensión cargada.'),('Only ordinary inert geology; no ore creation.','Solo geología inerte; no crea minerales.'),('Players and allies are not frozen.','No congela jugadores ni aliados.'),('Needs plain building blocks; occupied cells are skipped.','Necesita bloques simples; omite espacios ocupados.'),('Stops at obstructions; never passes through walls.','Se detiene ante obstáculos; nunca atraviesa paredes.'),('Leaves ores for you to harvest separately.','Deja los minerales para que los extraigas aparte.'),('The two regions must not overlap.','Las dos regiones no pueden solaparse.'),('Bring stone for the sanctuary. Protected cells remain untouched.','Lleva piedra para el santuario. No altera celdas protegidas.'),('A checkpoint is limited; restore needs matching materials.','El registro es limitado; restaurar necesita materiales coincidentes.'),('Builds above the aimed base; cannot replace occupied blocks.','Construye sobre la base apuntada; no reemplaza bloques ocupados.'),('No machines or inventories; every copied block is paid.','Sin máquinas ni inventarios; cada bloque copiado se paga.'),('Does not redirect player-owned shots or freeze enemies.','No desvía disparos de jugadores ni congela enemigos.'),('Only the last 128 footsteps; blocked segments stop the return.','Solo los últimos 128 pasos; un obstáculo detiene el regreso.'),('A and B must differ. Surface mode needs no offhand block.','A y B deben ser distintos. Superficie no necesita bloque secundario.'),('Same starting elevation; no flight or fast-fall rescue.','Misma cota inicial; sin vuelo ni rescate de caídas rápidas.'),('Entities still take damage. Huge explosions may be only partly blocked.','Las entidades reciben daño. Explosiones enormes pueden quedar protegidas solo en parte.'),('Only confirmed manual holes, behind you; never fills occupied bodies.','Solo huecos manuales confirmados detrás de ti; nunca rellena cuerpos ocupados.')]
def revise(d,spanish,ids):
 i=int(spanish)
 for n,id in enumerate(ids):d['play4.'+id]=PLAY[n][i];d['warning4.'+id]=WARN[n][i]
 for key,pair in {'what':('What it does','Qué hace'),'how':('How to use','Cómo usarlo'),'mode':('Current mode','Modo actual'),'cost':('Consumes','Qué consume'),'limits':('Keep in mind','Ten en cuenta'),'controls':('Controls · rebind in Options','Controles · reasigna en Opciones'),'wear':('Activation: %s durability. Mining wears the tool; building spends blocks.','Activar: %s de durabilidad. Minar desgasta; construir gasta bloques.')}.items():d['manual4.'+key]=pair[i]
 for key,pair in {'activate':('Activate ability','Activar habilidad'),'secondary':('Secondary ability','Habilidad secundaria'),'mode':('Change mode','Cambiar modo'),'select':('Enter / leave selection','Entrar / salir de selección'),'confirm':('Confirm','Confirmar'),'cancel':('Cancel','Cancelar'),'pause':('Pause / resume','Pausar / reanudar')}.items():d['key.specialpickaxes.'+key]=pair[i]
 d['key.categories.specialpickaxes']='Special Pickaxes'
 d['message.specialpickaxes.select_key']=('Use your Selection binding first.','Usa primero la tecla de Selección.')[i]
 d['mode.specialpickaxes.world_convergence']=('Convergence — forge a sanctuary','Convergencia — forjar un santuario')[i]
 d['message.specialpickaxes.armed']=('Select corners with left click. Cancel exits.','Marca esquinas con clic izquierdo. Cancelar sale.')[i]
 d['message.specialpickaxes.selection_ready']=('Analyzing your selection…','Analizando tu selección…')[i]
 d['status.specialpickaxes.ready']=('READY · Confirm to execute','LISTO · Confirma para ejecutar')[i]

 d['message.specialpickaxes.complete']=('Work complete.','Trabajo terminado.')[i]

 d['message.specialpickaxes.nothing_changed']=('Nothing changed. Check space and materials.','Sin cambios. Revisa espacio y materiales.')[i]
 d['message.specialpickaxes.partial']=('Finished; some occupied or protected cells were skipped.','Terminado; se omitieron celdas ocupadas o protegidas.')[i]
 d['identity.specialpickaxes.worldbreaker']=('Fracture a ring of terrain and forge its central sanctuary.','Fractura un anillo de terreno y forja su santuario central.')[i]

 d['message.specialpickaxes.cooldown']=('Ability cooling down.','Habilidad en enfriamiento.')[i]
 symbols={'attract':'↘','repel':'↗','record':'◇','restore':'↶','identity':'=','mirror_x':'↔','mirror_z':'↕','shelter':'⌂','bridge':'═','forward':'→','reverse':'←','stasis':'Ⅱ','reflect':'◈','shear':'∠','world_convergence':'✦'}
 for key in list(d):
  if key.startswith('mode.specialpickaxes.'):
   mode=key.split('.')[-1];d[key]=symbols.get(mode,'↻' if 'rotate' in mode else '•')+' '+d[key]

 d['message.specialpickaxes.not_ready']=('Finish the corners and wait for READY.','Completa las esquinas y espera a LISTO.')[i]

 d['message.specialpickaxes.queued']=('Ability activated.','Habilidad activada.')[i]
 d['message.specialpickaxes.volume_limit']=('Selection too large. Reduce its size.','Selección demasiado grande. Reduce su tamaño.')[i]

 d['message.specialpickaxes.transform']=('Transform: %s','Transformación: %s')[i]
