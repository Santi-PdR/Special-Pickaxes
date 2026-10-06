#!/usr/bin/env python3
"""Original deterministic pixel art and data; no third-party image dependencies."""
from pathlib import Path
import json, struct, zlib
R=Path('src/main/resources')
ITEMS={
 'overdrive':('Overdrive','Overdrive',(255,191,45),'redstone_block','diamond', 'Right-click: a brief mining burst up to 10×. Costs durability.', 'Clic derecho: ráfaga de minería de hasta 10×. Consume durabilidad.'),
 'excavator':('Excavator','Excavador',(220,159,84),'iron_block','diamond', 'Mine a 3×3 plane. Sneak to mine a single block.', 'Mina un plano de 3×3. Agáchate para minar un solo bloque.'),
 'vein_miner':('Vein Miner','Vein Miner',(76,213,111),'emerald','diamond','Mine connected ores of the same type, within server limits.', 'Mina minerales conectados del mismo tipo, con límites del servidor.'),
 'inferno':('Inferno','Inferno',(255,89,34),'blaze_powder','iron_ingot','Smelts valid drops using furnace recipes. Silk Touch takes priority.', 'Funde drops mediante recetas de horno. Toque de Seda tiene prioridad.'),
 'magnetic':('Magnetic','Magnetic',(239,84,122),'redstone','iron_ingot','Attracts nearby available items in sight. Sneak to disable.', 'Atrae objetos disponibles y visibles. Agáchate para desactivar.'),
 'scanner':('Scanner','Scanner',(64,234,216),'amethyst_shard','iron_ingot','Right-click: a limited local ore scan; shows nearest relative coordinates.', 'Clic derecho: escaneo local limitado; muestra coordenadas relativas del más cercano.'),
 'storm':('Storm','Storm',(102,166,255),'lightning_rod','diamond','Right-click: shock and slow a limited number of visible monsters.', 'Clic derecho: daña y ralentiza un número limitado de monstruos visibles.'),
 'void':('Void','Void',(148,84,208),'nether_star','netherite_ingot','Right-click: set/return to a temporary anchor. Sneak-use clears it. Cannot cross walls.', 'Clic derecho: fija/regresa a un ancla temporal. Agachado la elimina. No atraviesa paredes.'),
 'ender':('Ender','Ender',(173,228,117),'ender_pearl','diamond','Right-click: short blink along your view, stopping before obstacles.', 'Clic derecho: salto corto hacia donde miras, hasta el primer obstáculo.'),
 'explosive':('Explosive','Explosive',(249,103,109),'tnt','diamond','Right-click a block: bounded mining blast. No fire or entity damage.', 'Clic derecho sobre un bloque: minería explosiva limitada. Sin fuego ni daño a entidades.')}
def js(path,data):
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
def png(path,pixels,w=16,h=16):
 def chunk(k,d):return struct.pack('>I',len(d))+k+d+struct.pack('>I',zlib.crc32(k+d)&0xffffffff)
 raw=b''.join(b'\0'+bytes(sum(pixels[y*w:(y+1)*w],())) for y in range(h))
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True)
 p.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
langs=[{'itemGroup.specialpickaxes':'Special Pickaxes','effect.specialpickaxes.overdrive':'Overdrive',
 'tooltip.specialpickaxes.controls':'Use key: activate • Sneak: precise mining / magnet off',
 'message.specialpickaxes.overdrive':'Overdrive: ×%s', 'message.specialpickaxes.scan_empty':'No nearby ore detected.',
 'message.specialpickaxes.scan':'%s signals • nearest: %s • Δx %s, Δy %s, Δz %s',
 'message.specialpickaxes.anchor_set':'Anchor set. Use again after cooldown to return.',
 'message.specialpickaxes.anchor_cleared':'Anchor cleared.', 'message.specialpickaxes.anchor_blocked':'Return blocked, out of range or in another dimension.',
 'message.specialpickaxes.anchor_return':'Returned to anchor.'},
 {'itemGroup.specialpickaxes':'Picos especiales','effect.specialpickaxes.overdrive':'Overdrive',
 'tooltip.specialpickaxes.controls':'Tecla Usar: activar • Agacharse: minería precisa / imán apagado',
 'message.specialpickaxes.overdrive':'Overdrive: ×%s', 'message.specialpickaxes.scan_empty':'No se detectan minerales cercanos.',
 'message.specialpickaxes.scan':'%s señales • más cercano: %s • Δx %s, Δy %s, Δz %s',
 'message.specialpickaxes.anchor_set':'Ancla fijada. Usa de nuevo tras el cooldown para regresar.',
 'message.specialpickaxes.anchor_cleared':'Ancla eliminada.', 'message.specialpickaxes.anchor_blocked':'Regreso bloqueado, fuera de alcance o en otra dimensión.',
 'message.specialpickaxes.anchor_return':'Has regresado al ancla.'}]
for index,(id,(en,es,color,ingredient,material,tip_en,tip_es)) in enumerate(ITEMS.items()):
 for lang,name,tip in zip(langs,(en,es),(tip_en,tip_es)):
  lang['item.specialpickaxes.'+id]=name;lang['tooltip.specialpickaxes.'+id]=tip
 pixels=[(0,0,0,0)]*256
 def dot(x,y,c):
  if 0<=x<16 and 0<=y<16:pixels[y*16+x]=(*c,255)
 # Outlined diagonal handle, arched blade and individually placed gemstone highlights.
 shape={(x,y) for x,y in [(i,15-i) for i in range(2,11)]}
 head={(x,y) for x in range(4,13) for y in range(2,4)}|{(12,4),(13,4),(13,5),(13,6),(4,4),(3,4)}
 for x,y in shape|head:
  for dx,dy in [(0,0),(1,0),(-1,0),(0,1),(0,-1)]:dot(x+dx,y+dy,(24,26,40))
 for x,y in shape:dot(x,y,(139,95,64));dot(x+1,y,(190,146,83))
 for x,y in head:dot(x,y,color if y%2 else tuple(min(255,c+38) for c in color))
 dot(8,5,color);dot(9,4,(245,248,255));dot(5+index%6,2,(255,255,255))
 if id in ('storm','overdrive'):dot(11,7,color);dot(10,8,color);dot(11,9,color)
 if id in ('void','ender','scanner'):dot(5,6,color);dot(4,7,color)
 png(Path('assets/specialpickaxes/textures/item')/(id+'.png'),pixels)
 js(Path('assets/specialpickaxes/models/item')/(id+'.json'),{'parent':'minecraft:item/handheld','textures':{'layer0':'specialpickaxes:item/'+id}})
 js(Path('data/specialpickaxes/recipes')/(id+'.json'),{'type':'minecraft:crafting_shaped','pattern':['MMM',' C ',' S '],
  'key':{'M':{'item':'minecraft:'+material},'C':{'item':'minecraft:'+ingredient},'S':{'item':'minecraft:stick'}},'result':{'item':'specialpickaxes:'+id}})
 js(Path('data/specialpickaxes/advancements/recipes')/(id+'.json'),{'parent':'minecraft:recipes/root',
 'criteria':{'has_material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['minecraft:'+ingredient]}]}},
 'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'specialpickaxes:'+id}}},
 'requirements':[['has_material','has_recipe']],'rewards':{'recipes':['specialpickaxes:'+id]}})
for name,lang in zip(['en_us','es_es'],langs):js(Path('assets/specialpickaxes/lang')/(name+'.json'),lang)
pixels=[(0,0,0,0)]*256
for y in range(2,14):
 for x in range(4,12):
  if abs(x-(10-y//2))<2:pixels[y*16+x]=(255,202,51,255)
png(Path('assets/specialpickaxes/textures/mob_effect/overdrive.png'),pixels)
js(Path('data/forge/loot_modifiers/global_loot_modifiers.json'),{'replace':False,'entries':['specialpickaxes:auto_smelt']})
js(Path('data/specialpickaxes/loot_modifiers/auto_smelt.json'),{'type':'specialpickaxes:auto_smelt','conditions':[]})
