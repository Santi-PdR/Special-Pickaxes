"""Final mining roster and mode-specific player text; no texture synthesis."""
import json
from pathlib import Path
IDS='palimpsest fault_choir eventide paradox_crucible interregnum worldloom icarus hollow_axiom worldbreaker'.split()
MODES={
'palimpsest':{'restore':('Rebuild your actual excavation with real blocks.','Reconstruye tu excavación real con bloques reales.','Mine first with this pick, then point near the scar. Missing blocks are paid from inventory; occupied cells stay intact.','Mina primero con este pico y apunta cerca de la huella. Los huecos se reconstruyen pagando del inventario; nada ocupado se sustituye.')},
'fault_choir':{m:(f'Replay your excavation: {en}.',f'Repite tu excavación: {es}.',f'Mine a template with this pick. Aim at a new anchor and replay it with {en}; only matching original material is mined.',f'Mina una plantilla con este pico. Apunta a otro anclaje y repítela con {es}; solo extrae el material original coincidente.') for m,en,es in [('rotate0','original orientation','orientación original'),('rotate90','90° rotation','rotación de 90°'),('rotate180','180° rotation','rotación de 180°'),('rotate270','270° rotation','rotación de 270°'),('mirror_x','X reflection','reflexión X'),('mirror_z','Z reflection','reflexión Z')]},
'eventide':{m:(f'Excavate a flattened quarry {en}.',f'Excava una cantera achatada {es}.',f'Aim at geology. Mining advances {en}, keeping normal drops and preserving ores and machines.',f'Apunta a geología. La extracción avanza {es}, con drops normales y sin tocar minerales ni máquinas.') for m,en,es in [('gravity_in','from the rim inward','del borde al centro'),('gravity_out','from the center outward','del centro al borde')]},
'paradox_crucible':{
'regional':('Change existing geology in a selected region.','Cambia geología existente en una región.','Choose material before the second corner. Arm selection, left-click two different corners, wait for analysis, then confirm. Offhand stone, deepslate, basalt or obsidian chooses the result; empty hand means stone.','Elige material antes de la segunda esquina. Arma la selección, marca dos esquinas distintas, espera el análisis y confirma. Piedra, pizarra, basalto u obsidiana en la secundaria eligen el resultado; vacía significa piedra.'),
'proximity':('Change nearby geology in a sphere, without corners.','Cambia geología cercana en esfera, sin esquinas.','Activate once to transform nearby natural matrix. Offhand stone, deepslate, basalt or obsidian chooses the result. No ore, loot or experience is created.','Activa una vez para transformar la matriz natural cercana. Piedra, pizarra, basalto u obsidiana en la secundaria eligen el resultado. No genera menas, drops ni experiencia.')},
'interregnum':{'stasis':('Mine at extreme speed inside local stasis.','Mina a velocidad extrema dentro de una estasis local.','Aim at your worksite and activate. Mine inside the field to sustain it; nearby interruptions are slowed. Cancel releases the field.','Apunta al frente de trabajo y activa. Minar dentro sostiene el campo; ralentiza las interrupciones cercanas. Cancelar libera el campo.')},
'worldloom':{m:(f'Build a paid {en} facing your direction.',f'Construye {es} pagando materiales.',f'Hold a supported building block in your offhand. Aim at the base; the preview follows your facing. Activate to place the {en} in sequence, consuming inventory blocks.',f'Pon un bloque de construcción compatible en la secundaria. Apunta a la base; la vista previa sigue tu orientación. Activa para colocar {es} en secuencia, consumiendo bloques del inventario.') for m,en,es in [('shelter','shelter','un refugio'),('bridge','bridge','un puente'),('wall','wall','una pared')]},
'icarus':{m:(f'Drill and advance {en} through safe sections.',f'Perfora y avanza {es} por secciones seguras.',f'Activate once. The bore runs {en} along your view axis, clearing each section before moving you. Any barrier stops the route.',f'Activa una vez. Perfora {es} sobre el eje de la mirada; despeja cada sección antes de moverte. Cualquier barrera detiene la ruta.') for m,en,es in [('forward','forward','hacia delante'),('reverse','backward','hacia atrás')]},
'hollow_axiom':{'selective':('Peel rock around existing ores; keep the ores intact.','Desmonta roca alrededor de menas existentes; conserva las menas.','Aim at a deposit and activate once. Removes connected matrix up to three blocks from actual ores in a bounded ellipsoid, working outward. No ore outlines or generated resources.','Apunta a un yacimiento y activa una vez. Retira matriz conectada hasta tres bloques de menas reales dentro de un elipsoide, desde el núcleo hacia fuera. No muestra menas ocultas ni genera recursos.')},
'worldbreaker':{m:(en,es,how,como) for m,en,es,how,como in [
('carve','CARVE: drive a long rounded mining tunnel.','CARVE: abre un túnel minero largo y redondeado.','Aim at the cutting face. Activate once to excavate successive rounded sections along your view axis.','Apunta al frente de corte. Activa una vez para excavar secciones redondeadas sucesivas sobre el eje de la mirada.'),
('fracture','FRACTURE: split a widening mining fan.','FRACTURE: abre una fractura minera en abanico.','Aim and activate. A low fan widens as the cut advances, rather than removing a cuboid.','Apunta y activa. Un abanico bajo se ensancha al avanzar el corte; no vacía un cuboide.'),
('cleave','CLEAVE: shear a broad diamond-shaped face.','CLEAVE: secciona un frente amplio en rombo.','Aim and activate for a wide, shallow diamond cut. Suitable for opening a quarry face.','Apunta y activa para un corte ancho, poco profundo y en rombo. Abre el frente de una cantera.'),
('core_drill','CORE DRILL: sink a deep vertical shaft.','CORE DRILL: hunde un pozo vertical profundo.','Activate to drill downward below you. The shaft terminates at the first barrier; watch your footing.','Activa para perforar verticalmente bajo tus pies. El pozo termina ante la primera barrera; cuida dónde pisas.'),
('world_shatter','WORLD SHATTER: excavate a vast terraced wedge.','WORLD SHATTER: excava una enorme cuña escalonada.','Aim and activate once. Successive sections widen and deepen into a large directional excavation. Real harvesting is spread across ticks, not one server-freezing explosion.','Apunta y activa una vez. Las secciones se ensanchan y profundizan en una gran excavación direccional. La extracción real se distribuye por ticks; no es una explosión que congele el servidor.')]} }

def apply():
 root=Path('src/main/resources')
 for lang in ['en_us','es_es']:
  path=root/f'assets/specialpickaxes/lang/{lang}.json';d=json.loads(path.read_text());es=lang=='es_es'
  for artifact,modes in MODES.items():
   for mode,text in modes.items():
    d[f'mining.identity.{artifact}.{mode}']=text[1 if es else 0]
    d[f'mining.how.{artifact}.{mode}']=text[3 if es else 2]
    labels={'shelter':'REFUGIO','bridge':'PUENTE','wall':'PARED','regional':'REGIONAL','proximity':'PROXIMIDAD','stasis':'ESTASIS','forward':'ADELANTE','reverse':'ATRÁS','restore':'RECONSTRUIR','selective':'SELECTIVO','gravity_in':'HACIA EL CENTRO','gravity_out':'HACIA EL BORDE','rotate0':'ORIGINAL','rotate90':'90°','rotate180':'180°','rotate270':'270°','mirror_x':'REFLEXIÓN X','mirror_z':'REFLEXIÓN Z'}
    d[f'mode.specialpickaxes.{mode}']=labels.get(mode,mode.replace('_',' ').upper()) if es else mode.replace('_',' ').upper()
  d['manual4.wear']='Desgaste al activar: %s; enfriamiento %s s.' if es else 'Activation wear: %s; cooldown %s s.'
  d['mining.limits']=('Nunca toca bedrock, fluidos, máquinas ni bloques protegidos. Las rutas direccionales se detienen ante barreras. Pausa o cancela con tus teclas configuradas.' if es else 'Never edits bedrock, fluids, machines or protected blocks. Directional routes stop at barriers. Pause or cancel with your configured keys.')
  d['message.specialpickaxes.drop_pause']='Pausa por acumulación de drops. Recógelos y reanuda.' if es else 'Paused for drop congestion. Collect drops, then resume.'
  d['message.specialpickaxes.duplicate_corner']='Esa esquina ya está marcada.' if es else 'That corner is already marked.'
  path.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
 for tag in ['pickaxes','axes','shovels']:
  (root/f'data/minecraft/tags/items/{tag}.json').write_text(json.dumps({'replace':False,'values':['specialpickaxes:'+id for id in IDS]},indent=2)+'\n')
 for folder in ['models/item','textures/item']:
  for path in (root/f'assets/specialpickaxes/{folder}').iterdir():
   if path.name.split('.')[0] not in IDS:path.unlink()
if __name__=='__main__':apply()
