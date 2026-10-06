#!/usr/bin/env python3
"""Deterministic original animated 64px artifact silhouettes. No image/animation runtime dependency."""
from pathlib import Path
import json,struct,zlib,math,shutil
R=Path('src/main/resources')
IDS='palimpsest fault_choir eventide meridian paradox_crucible interregnum worldloom icarus hollow_axiom bifold_atlas'.split()
EN=['Palimpsest of the Last Dawn','Choir of Faults','The Night That Weighs','Meridian of Two Worlds','Paradox Crucible','Crown of the Interregnum','Loom of the First Wall','Tear of Icarus','The Hollow Axiom','Atlas of Two Shores']
ES=['Palimpsesto del Último Alba','Coro de las Fallas','La Noche que Pesa','Meridiano de Dos Mundos','Crisol de la Paradoja','Corona del Interregno','Bastidor de la Primera Muralla','Lágrima de Ícaro','El Axioma Hueco','Atlas de las Dos Orillas']
COLORS=[0xffd783,0x46ede0,0xa67aff,0x68b9ff,0xff8648,0x8ae5ff,0x64ffa9,0xffc057,0xd9ffbd,0xff7b99]
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
def js(path,obj):
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n')
def png(path,pixels,w,h):
 def chunk(k,d):return struct.pack('>I',len(d))+k+d+struct.pack('>I',zlib.crc32(k+d)&0xffffffff)
 raw=b''.join(b'\0'+bytes(sum(pixels[y*w:(y+1)*w],())) for y in range(h))
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True)
 p.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
def art(index,frame):
 pix=[(0,0,0,0)]*4096;rgb=COLORS[index];c=((rgb>>16)&255,(rgb>>8)&255,rgb&255);dark=(19,19,35);white=(245,246,231);metal=(103,110,129)
 def dot(x,y,col):
  x,y=int(x),int(y)
  if 0<=x<64 and 0<=y<64:pix[y*64+x]=(*col,255)
 def disk(x,y,r,col):
  for a in range(int(x-r),int(x+r+1)):
   for b in range(int(y-r),int(y+r+1)):
    if (a-x)**2+(b-y)**2<=r*r:dot(a,b,col)
 def line(a,b,col,width=1):
  steps=max(abs(b[0]-a[0]),abs(b[1]-a[1]),1)
  for i in range(int(steps)+1):disk(round(a[0]+(b[0]-a[0])*i/steps),round(a[1]+(b[1]-a[1])*i/steps),width,col)
 def stroke(a,b,col=c,w=1):line(a,b,dark,w+1);line(a,b,col,w)
 def ring(x,y,r,col=c,w=1,start=0,end=360):
  for a in range(start,end,2):
   a=math.radians(a);disk(round(x+math.cos(a)*r),round(y+math.sin(a)*r),w,col)
 def chain(points,col=c,w=1):
  for a,b in zip(points,points[1:]):stroke(a,b,col,w)
 if index==0:
  stroke((13,57),(35,30),metal,2);ring(37,23,15,dark,3);ring(37,23,14,c,1);ring(37,23,10,white,1)
  ring(27,20,19,white,2,105,280)
  for n in range(12):
   a=n*math.pi/6;disk(37+math.cos(a)*12,23+math.sin(a)*12,1,c)
  a=frame*math.pi/2;stroke((37,23),(round(37+8*math.cos(a)),round(23+8*math.sin(a))),white)
 elif index==1:
  stroke((21,57),(32,32),metal,2)
  for x in [15,31,47]:chain([(x-6,22),(x,7),(x+4,23),(32,36)],c,1)
  for y in [23,27,31]:line((16,y),(46,y),white if (y+frame)%4==0 else metal,0)
 elif index==2:
  chain([(29,58),(34,45),(27,37),(32,28)],metal,2);disk(32,22,14,dark);ring(32,22,16,c,2);ring(32,22,11,metal,1)
  for n in range(4):
   a=(n+frame/4)*math.pi/2;disk(32+math.cos(a)*21,22+math.sin(a)*19,2,c)
  ring(32,22,6,(65,33,88),1)
 elif index==3:
  stroke((32,58),(32,20),white,2);chain([(5,24),(21,9),(32,23),(43,9),(59,24)],c,2)
  chain([(6,26),(22,36),(32,23),(42,36),(58,26)],(246,195,75),2);ring(32,23,7,white,1)
  stroke((32,23),(32+(-8 if frame%2 else 8),23),c)
 elif index==4:
  stroke((31,58),(32,33),(173,94,61),3);chain([(13,14),(23,6),(43,6),(52,16),(46,36),(20,36),(13,14)],metal,2)
  for x in [21,31,42]:stroke((x,12),(x,33),c,1)
  disk(32,23,6,c);disk(32,21-frame,3,white);chain([(13,13),(7,23),(16,27)],c,2)
 elif index==5:
  stroke((32,58),(32,38),metal,2);chain([(12,25),(9,8),(21,17),(25,5),(32,15),(40,5),(44,17),(55,8),(51,25),(12,25)],c,1)
  chain([(22,29),(42,29),(22,44),(42,44),(22,29)],white,1);disk(32,33+frame*2,2,c)
 elif index==6:
  stroke((33,58),(33,35),metal,3);chain([(12,9),(50,9),(50,39),(12,39),(12,9)],c,2)
  for x in range(18,49,6):line((x,13),(x,35),metal,0)
  stroke((16,20+frame*3),(46,20+frame*3),white,1);chain([(10,10),(6,20),(10,30)],white,1)
 elif index==7:
  stroke((14,57),(41,17),metal,3);chain([(37,26),(10,17),(28,11),(49,15),(41,35),(58,45),(42,40)],(228,129,65),2)
  chain([(35,24),(21,28),(9,44),(31,37),(39,31)],c,2);disk(40,21,5,white);disk(40,21,3,c)
  for n in range(4):disk(27-n*3-frame,38+n*3,1,c)
 elif index==8:
  stroke((29,59),(30,37),metal,2)
  chain([(15,20),(42,20),(42,45),(15,45),(15,20),(25,9),(52,9),(52,34),(42,45)],white,1)
  stroke((42,20),(52,9),white);stroke((15,45),(25,34),white)
  chain([(29,22),(37,30),(29,38),(21,30),(29,22)],c,1);disk(29,30,1+frame%2,c)
 elif index==9:
  stroke((32,58),(32,23),metal,3)
  chain([(30,20),(24,8),(6,15),(11,34),(26,30),(30,20)],white,2)
  chain([(34,20),(40,8),(58,15),(53,34),(38,30),(34,20)],c,2)
  for x in [15,45]:stroke((x,17),(x+3,28),metal)
  disk(32,24,4,dark);disk(32,24,2+frame%2,c)
 # Small distinct animated floating runes, not a palette-recoloured shared silhouette.
 for n in range(3):
  x=6+(index*7+n*17)%52;y=45+(n*5+frame*2)%13;dot(x,y,c);dot(x+1,y,white)
 return pix
# These directories contain only project-generated assets; remove legacy recipes/models and never touch references.
for folder in ['assets/specialpickaxes','data/specialpickaxes/recipes','data/specialpickaxes/advancements/recipes','data/specialpickaxes/loot_modifiers','data/forge/loot_modifiers']:
 p=R/folder
 if p.exists():shutil.rmtree(p)
for index,id in enumerate(IDS):
 pixels=[]
 for frame in range(4):pixels.extend(art(index,frame))
 png(Path(f'assets/specialpickaxes/textures/item/{id}.png'),pixels,64,256)
 js(Path(f'assets/specialpickaxes/textures/item/{id}.png.mcmeta'),{'animation':{'frametime':6,'interpolate':False}})
 js(Path(f'assets/specialpickaxes/models/item/{id}.json'),{'parent':'minecraft:item/handheld','textures':{'layer0':'specialpickaxes:item/'+id},'display':{'gui':{'rotation':[0,0,(-8 if index%2 else 8)],'scale':[0.9,0.9,0.9]}}})
langs=[]
for spanish in [False,True]:
 lang={'itemGroup.specialpickaxes':'Siege — Artefactos' if spanish else 'Siege — Artifacts',
 'effect.specialpickaxes.dominion':'Interregno' if spanish else 'Interregnum',
 'tooltip.specialpickaxes.controls':'Usar: primaria • Agachado: secundaria/cancelar • Sin recetas' if spanish else 'Use: primary • Sneak-use: secondary/cancel • No recipes',
 'tooltip.specialpickaxes.state':'Energía %s/256 • Memoria %s • Modo %s' if spanish else 'Energy %s/256 • Memory %s • Mode %s',
 'message.specialpickaxes.cancelled':'Operación cancelada.' if spanish else 'Operation cancelled.',
 'message.specialpickaxes.mode':'Estado secundario %s.' if spanish else 'Secondary state %s.',
 'message.specialpickaxes.no_target':'Sin operación válida: revisa objetivo, memoria, anclas o material.' if spanish else 'No valid operation: check target, memory, anchors or material.',
 'message.specialpickaxes.queued':'%s pasos en cola. Cambia de herramienta para detener.' if spanish else '%s steps queued. Switch tools to stop.',
 'message.specialpickaxes.working':'Trabajo pendiente: %s • Agacharse + usar para cancelar' if spanish else 'Work remaining: %s • Sneak-use to cancel',
 'message.specialpickaxes.anchor_a':'Primera ancla fijada. Apunta a la segunda.' if spanish else 'First anchor set. Aim at the second.',
 'message.specialpickaxes.linked':'Enlace activo. Minar cerca de A repercute en B.' if spanish else 'Link active. Mining near A echoes at B.',
 'message.specialpickaxes.unknown':'Artefacto desconocido.' if spanish else 'Unknown artifact.',
 'message.specialpickaxes.granted':'Entregado %s a %s.' if spanish else 'Granted %s to %s.',
 'hud.specialpickaxes.status':'Energía %s/256 • M%s • %s pasos' if spanish else 'Energy %s/256 • M%s • %s steps'}
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
