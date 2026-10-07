#!/usr/bin/env python3
"""Existing vanilla pickaxe bases; differentiation is glint, runtime aura and ability effects."""
from pathlib import Path
import json,struct,zlib,shutil
R=Path('src/main/resources')
IDS='palimpsest fault_choir eventide meridian paradox_crucible interregnum worldloom icarus hollow_axiom bifold_atlas worldbreaker chronicle keystone tessellator aegis lodestar seam_ripper causeway counterseal covenant'.split()
EN=['Palimpsest of the Last Dawn','Choir of Faults','The Night That Weighs','Meridian of Two Worlds','Paradox Crucible','Crown of the Interregnum','Loom of the First Wall','Tear of Icarus','The Hollow Axiom','Atlas of Two Shores']
ES=['Palimpsesto del Último Alba','Coro de las Fallas','La Noche que Pesa','Meridiano de Dos Mundos','Crisol de la Paradoja','Corona del Interregno','Bastidor de la Primera Muralla','Lágrima de Ícaro','El Axioma Hueco','Atlas de las Dos Orillas']
TIPS_EN=['Rebuild your remembered mining scars. Each placed block consumes an actual inventory block.',
'Replay your recorded excavation stencil at a new anchor. Only matching rock is removed.',
'Collapse a geological domain while bending hostile trajectories. No item magnet or damage explosion.',
'Link two loaded anchors in one dimension. Mining near the first echoes at the second.',
'Rephase inert geology one-for-one. Never creates ore, drops or experience.',
'Anchor kinetic stasis. Mining inside sustains the domain and grants ×8 local mining speed.',
'Weave a hollow shelter or bridge from actual inventory blocks. Hold the chosen rock in your offhand.',
'Ride a kinetic bore. Every section is excavated before your body may advance.',
'Excavate the geological matrix while preserving ore, machinery and optional structural ribs.',
'Exchange two geological regions in protected atomic pairs. No matter is duplicated.']
TIPS_ES=['Reconstruye tus huellas de minería. Cada bloque colocado consume un bloque real del inventario.',
'Reproduce tu plantilla de excavación en otro anclaje. Solo extrae roca coincidente.',
'Colapsa un dominio geológico y curva trayectorias hostiles. Sin imán ni explosión de daño.',
'Enlaza dos anclas cargadas de una dimensión. Minar cerca de la primera repercute en la segunda.',
'Transforma geología inerte uno a uno. Nunca crea minerales, drops ni experiencia.',
'Ancla una estasis cinética. Minar dentro la sostiene y multiplica ×8 la velocidad local.',
'Teje un refugio hueco o puente con bloques del inventario. Lleva la roca elegida en la mano secundaria.',
'Cabalga una perforación cinética. Cada sección se excava antes de permitir el paso del cuerpo.',
'Excava la matriz geológica preservando minerales, maquinaria y nervaduras opcionales.',
'Intercambia dos regiones geológicas por pares protegidos. No duplica materia.']
SEC_EN=['Erase history','Rotate stencil 90°','Reverse field polarity','Unlink anchors','Cycle stone / deepslate / basalt / obsidian','Release stasis and momentum','Switch shelter / bridge','Reverse bore direction','Toggle structural ribs','Clear anchor']
SEC_ES=['Borra el historial','Rota la plantilla 90°','Invierte la polaridad','Elimina el enlace','Alterna piedra / pizarra / basalto / obsidiana','Libera estasis e impulso','Alterna refugio / puente','Invierte la dirección','Alterna nervaduras estructurales','Elimina el ancla']
EN += ['The Worldbreaker','Chronicle of the Silent Stone','Keystone of the Impossible Arch','The Tessellator']
ES += ['El Rompemundos','Crónica de la Piedra Silente','Clave del Arco Imposible','El Teselador']
TIPS_EN += ['Analyze a selected world volume, then break, carve, rephase, transpose, record or restore.', 'Record a geological checkpoint; restore missing cells later with real materials.', 'Build a parabolic vault defined by your selected span, rise and depth.', 'Reproduce an existing geological blueprint in another region, paying for every placed block.']
TIPS_ES += ['Analiza un volumen; rompe, talla, transforma, intercambia, registra o restaura.', 'Registra un estado geológico; restaura después los huecos con materiales reales.', 'Construye una bóveda parabólica definida por la anchura, altura y profundidad elegidas.', 'Reproduce un plano geológico en otra región pagando cada bloque colocado.']
SEC_EN += ['Cycle operation when idle; cancel selection/job otherwise']*4
SEC_ES += ['Alterna operación sin selección; de lo contrario cancela selección/trabajo']*4
SEC_EN[0]='Cancel selection; memory is never erased by accident'
SEC_ES[0]='Cancela selección; nunca borra la memoria accidentalmente'
SEC_EN[9]='Cancel selection; sneak-left-click changes transformation'
SEC_ES[9]='Cancela selección; agachado + clic izquierdo cambia transformación'
EN += ['Aegis of the Horizon','Lodestar of the Returning Miner','Seam Ripper',"Pilgrim's Causeway",'Counterseal of the Bastion','Covenant of the Sealed Gallery']
ES += ['Égida del Horizonte','Astrolabio del Retorno','Sutura de los Estratos','Calzada del Peregrino','Contrafirma del Bastión','Pacto de la Galería Sellada']
TIPS_EN += ['Redirect incoming hostile projectiles without changing their ownership.', 'Retrace remembered footsteps through still-clear loaded space.', 'Peel a connected material interface, not the whole vein.', 'Paid footing follows your route over gaps; it does not grant flight.', 'Protect nearby terrain from explosions, not entities from damage.', 'Seal confirmed manual mining scars after you pass them, using inventory material.']
TIPS_ES += ['Redirige proyectiles hostiles sin cambiar su propietario.', 'Recorre de vuelta tus pasos por espacio aún despejado y cargado.', 'Retira una interfaz de materiales conectada, no la veta completa.', 'Un apoyo pagado sigue tus pasos sobre huecos; no concede vuelo.', 'Protege terreno cercano de explosiones, no a entidades del daño.', 'Sella huecos de minería manual cuando los dejas atrás, con material del inventario.']
SEC_EN += ['Change reflection / shear','Forget route','Change contact / exposed surface','Stop companion','Stop ward','Stop sealing']
SEC_ES += ['Alterna reflexión / desvío','Olvida la ruta','Alterna contacto / superficie expuesta','Detiene el acompañante','Detiene la protección','Detiene el sellado']
def js(path,obj):
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n')
def png(path,pixels,w,h):
 def chunk(k,d):return struct.pack('>I',len(d))+k+d+struct.pack('>I',zlib.crc32(k+d)&0xffffffff)
 raw=b''.join(b'\0'+bytes(sum(pixels[y*w:(y+1)*w],())) for y in range(h))
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True)
 p.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
# These directories contain only project-generated assets; remove legacy recipes/models and never touch references.
for folder in ['assets/specialpickaxes','data/specialpickaxes/recipes','data/specialpickaxes/advancements/recipes','data/specialpickaxes/loot_modifiers','data/forge/loot_modifiers']:
 p=R/folder
 if p.exists():shutil.rmtree(p)
BASES=['diamond','diamond','netherite','diamond','golden','diamond','iron','netherite','netherite','netherite','netherite','golden','iron','diamond','diamond','netherite','iron','iron','netherite','netherite']
for index,id in enumerate(IDS):
 js(Path(f'assets/specialpickaxes/models/item/{id}.json'),{'parent':'minecraft:item/handheld','textures':{'layer0':'minecraft:item/'+BASES[index]+'_pickaxe'}})
langs=[]
for spanish in [False,True]:
 lang={'itemGroup.specialpickaxes':'Special Pickaxes — Artefactos' if spanish else 'Special Pickaxes — Artifacts',
 'effect.specialpickaxes.dominion':'Interregno' if spanish else 'Interregnum',
 'tooltip.specialpickaxes.controls':'Usar: primaria • Agachado: secundaria/cancelar • Sin recetas' if spanish else 'Use: primary • Sneak-use: secondary/cancel • No recipes',
 'message.specialpickaxes.cancelled':'Operación cancelada.' if spanish else 'Operation cancelled.',
 'message.specialpickaxes.mode':'Estado secundario %s.' if spanish else 'Secondary state %s.',
 'message.specialpickaxes.no_target':'Sin operación válida: revisa objetivo, memoria, anclas o material.' if spanish else 'No valid operation: check target, memory, anchors or material.',
 'message.specialpickaxes.queued':'%s pasos en cola. Cambia de herramienta para detener.' if spanish else '%s steps queued. Switch tools to stop.',
 'message.specialpickaxes.working':'Trabajo pendiente: %s • Agacharse + usar para cancelar' if spanish else 'Work remaining: %s • Sneak-use to cancel',
 'message.specialpickaxes.anchor_a':'Primera ancla fijada. Apunta a la segunda.' if spanish else 'First anchor set. Aim at the second.',
 'message.specialpickaxes.linked':'Enlace activo. Minar cerca de A repercute en B.' if spanish else 'Link active. Mining near A echoes at B.',
 'message.specialpickaxes.unknown':'Artefacto desconocido.' if spanish else 'Unknown artifact.',
 'message.specialpickaxes.granted':'Entregado %s a %s.' if spanish else 'Granted %s to %s.'}
 for i,id in enumerate(IDS):
  lang['item.specialpickaxes.'+id]=(ES if spanish else EN)[i]
  lang['tooltip.specialpickaxes.'+id]=(TIPS_ES if spanish else TIPS_EN)[i]
  lang['tooltip.specialpickaxes.secondary.'+id]=('Agachado: ' if spanish else 'Sneak-use: ')+(SEC_ES if spanish else SEC_EN)[i]
 langs.append(lang)
for code,lang in zip(['en_us','es_es'],langs):js(Path('assets/specialpickaxes/lang')/(code+'.json'),lang)
pixels=[(0,0,0,0)]*256
for y in range(2,14):
 for x in range(3,13):
  if x in (3,12) or y in (2,13) or abs(x-7.5)==abs(y-7.5):pixels[y*16+x]=(138,229,255,255)
png(Path('assets/specialpickaxes/textures/mob_effect/dominion.png'),pixels,16,16)
js(Path('data/minecraft/tags/items/pickaxes.json'),{'replace':False,'values':['specialpickaxes:'+id for id in IDS]})

for tag in ['axes','shovels']:
 js(Path('data/minecraft/tags/items')/(tag+'.json'),{'replace':False,'values':['specialpickaxes:'+id for id in IDS]})
# Shared UX translations are authored separately so resource regeneration is reproducible.
from ux_strings import enrich
from ux31_strings import revise
for code in ['en_us','es_es']:
 path=Path('assets/specialpickaxes/lang')/(code+'.json');data=json.loads((R/path).read_text());enrich(data,code=='es_es');revise(data,code=='es_es',IDS);js(path,data)
