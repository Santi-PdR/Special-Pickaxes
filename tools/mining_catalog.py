"""Current playable mining roster and canonical mode text; no texture synthesis."""
import json
from pathlib import Path

IDS=['palimpsest', 'fault_choir', 'eventide', 'paradox_crucible', 'interregnum', 'worldloom', 'icarus', 'hollow_axiom', 'worldbreaker', 'exodium', 'iridium', 'hellspec', 'seam_ripper']
MODES={'palimpsest': {'vein': ('VEIN ECHO: harvest the connected ore vein.',
                         'ECO DE VETA: extrae la veta conectada.',
                         'Aim at an exposed ore and activate. It searches a 12-block radius and mines up to '
                         '256 connected blocks by default, respecting Fortune, protections and normal drops.',
                         'Apunta a una mena expuesta y activa. Busca en un radio de 12 bloques y extrae '
                         'hasta 256 bloques conectados por defecto; respeta Fortuna, protecciones y drops '
                         'normales.')},
 'fault_choir': {'forward': ('RESONANT TUNNEL: 2×2 bore, 20 blocks forward.',
                             'TÚNEL RESONANTE: perforación 2×2 de 20 bloques.',
                             'Aim forward and activate. The tunnel follows your view; it never flips behind '
                             'you or replays distant blocks.',
                             'Apunta al frente y activa. El túnel avanza hacia donde miras, sin girar 180° '
                             'ni reproducir zonas lejanas.')},
 'eventide': {'gravity_in': ('GRAVITY WELL: pull hostile mobs and projectiles into the marked field.',
                             'POZO GRAVITATORIO: atrae hostiles y proyectiles al campo marcado.',
                             'Aim at a block and press R to anchor an 8-block field. Hostile mobs and '
                             'projectiles are pulled toward its center while you mine faster inside. C '
                             'switches to outward gravity. The field never breaks extra blocks.',
                             'Apunta a un bloque y pulsa R para anclar un campo de 8 bloques. Atrae enemigos '
                             'y proyectiles hostiles al centro y te da más velocidad minera dentro. C cambia '
                             'a gravedad hacia fuera. El campo no rompe bloques extra.'),
              'gravity_out': ('GRAVITY SURGE: push hostile mobs and projectiles out of the marked field.',
                              'IMPULSO GRAVITATORIO: repele hostiles y proyectiles del campo marcado.',
                              'Aim at a block and press R to anchor an 8-block field. Hostile mobs and '
                              'projectiles are pushed away from its center while you mine faster inside. C '
                              'switches to inward gravity. The field never breaks extra blocks.',
                              'Apunta a un bloque y pulsa R para anclar un campo de 8 bloques. Aleja '
                              'enemigos y proyectiles hostiles del centro y te da más velocidad minera '
                              'dentro. C cambia a gravedad hacia dentro. El campo no rompe bloques extra.')},
 'paradox_crucible': {'stone': ('TRANSFORM: Stone',
                                'TRANSFORMAR: Piedra',
                                'Select two corners, wait for analysis and confirm. Only natural geology in '
                                'the region changes to Stone.',
                                'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                                'geología natural de la región a piedra.'),
                      'deepslate': ('TRANSFORM: Deepslate',
                                    'TRANSFORMAR: Pizarra profunda',
                                    'Select two corners, wait for analysis and confirm. Only natural geology '
                                    'in the region changes to Deepslate.',
                                    'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                                    'geología natural de la región a pizarra profunda.'),
                      'granite': ('TRANSFORM: Granite',
                                  'TRANSFORMAR: Granito',
                                  'Select two corners, wait for analysis and confirm. Only natural geology '
                                  'in the region changes to Granite.',
                                  'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                                  'geología natural de la región a granito.'),
                      'diorite': ('TRANSFORM: Diorite',
                                  'TRANSFORMAR: Diorita',
                                  'Select two corners, wait for analysis and confirm. Only natural geology '
                                  'in the region changes to Diorite.',
                                  'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                                  'geología natural de la región a diorita.'),
                      'andesite': ('TRANSFORM: Andesite',
                                   'TRANSFORMAR: Andesita',
                                   'Select two corners, wait for analysis and confirm. Only natural geology '
                                   'in the region changes to Andesite.',
                                   'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                                   'geología natural de la región a andesita.'),
                      'dirt': ('TRANSFORM: Dirt',
                               'TRANSFORMAR: Tierra',
                               'Select two corners, wait for analysis and confirm. Only natural geology in '
                               'the region changes to Dirt.',
                               'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                               'geología natural de la región a tierra.'),
                      'basalt': ('TRANSFORM: Basalt',
                                 'TRANSFORMAR: Basalto',
                                 'Select two corners, wait for analysis and confirm. Only natural geology in '
                                 'the region changes to Basalt.',
                                 'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                                 'geología natural de la región a basalto.'),
                      'obsidian': ('TRANSFORM: Obsidian',
                                   'TRANSFORMAR: Obsidiana',
                                   'Select two corners, wait for analysis and confirm. Only natural geology '
                                   'in the region changes to Obsidian.',
                                   'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                                   'geología natural de la región a obsidiana.'),
                      'calcite': ('TRANSMUTE: Calcite',
                                  'TRANSFORMAR: Calcita',
                                  'Select two corners, wait for the analysis, then confirm. Changes only '
                                  'natural geology in the region to calcite.',
                                  'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                                  'geología natural de la región a calcita.'),
                      'tuff': ('TRANSMUTE: Tuff',
                               'TRANSFORMAR: Toba',
                               'Select two corners, wait for the analysis, then confirm. Changes only '
                               'natural geology in the region to tuff.',
                               'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                               'geología natural de la región a toba.'),
                      'dripstone': ('TRANSMUTE: Dripstone',
                                    'TRANSFORMAR: Espeleotema',
                                    'Select two corners, wait for the analysis, then confirm. Changes only '
                                    'natural geology in the region to dripstone.',
                                    'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                                    'geología natural de la región a espeleotema.'),
                      'gravel': ('TRANSMUTE: Gravel',
                                 'TRANSFORMAR: Grava',
                                 'Select two corners, wait for the analysis, then confirm. Changes only '
                                 'natural geology in the region to gravel.',
                                 'Selecciona dos esquinas, espera el análisis y confirma. Solo cambia la '
                                 'geología natural de la región a grava.')},
 'interregnum': {'domain': ('DOMAIN: a marked 16×16×8 Stasis field that supports nearby teammates.',
                            'DOMINIO: campo Stasis marcado de 16×16×8 que ayuda a compañeros.',
                            'Aim at a location and activate. Its visible border marks the 16×16×8 field. '
                            'Inside, you gain mining speed ×8, Regeneration V, Absorption IV, Resistance II, '
                            'food restoration and Night Vision. Up to 16 nearby teammates gain Regeneration '
                            'II, Haste III, food restoration and Night Vision.',
                            'Apunta al sitio y activa. El borde visible marca el campo de 16×16×8. Dentro '
                            'recibes velocidad minera ×8, Regeneración V, Absorción IV, Resistencia II, '
                            'recuperación de comida y Visión nocturna. Hasta 16 compañeros cercanos reciben '
                            'Regeneración II, Prisa III, recuperación de comida y Visión nocturna.'),
                 'aura': ('AURA: carry Stasis with you.',
                          'AURA: lleva Stasis contigo.',
                          'Activate without aiming at a block. The small aura follows only you and grants ×8 '
                          'mining speed, Night Vision and one food point every five seconds. It does not '
                          'freeze mobs.',
                          'Activa sin apuntar a un bloque. El aura pequeña te sigue solo a ti y da velocidad '
                          'minera ×8, Visión nocturna y un punto de comida cada cinco segundos. No congela '
                          'criaturas.')},
 'worldloom': {'quarry': ('CANOPY BORE: a marked 5×5 passage, 16 blocks ahead.',
                          'TALADRO DEL DOSEL: túnel marcado de 5×5, 16 bloques hacia delante.',
                          'Aim at natural stone and press R. The green outline marks the 5×5 tunnel ends; '
                          'the pick clears up to 16 blocks straight ahead. Ores, fluids, placed blocks, '
                          'machines and protected cells stay safe.',
                          'Apunta a piedra natural y pulsa R. El contorno verde marca los extremos del túnel '
                          'de 5×5; el pico excava hasta 16 bloques en línea recta. Conserva menas, fluidos, '
                          'bloques colocados, máquinas y zonas protegidas.')},
 'icarus': {'forward': ('ANGELIC BORE: safe forward travel through a 3×3 tunnel.',
                        'PERFORACIÓN ANGELICAL: viaje seguro por un túnel 3×3.',
                        'Activate to bore safely in scheduled slices. End Rod and enchant particles trace '
                        'the route. Use X for Icarian Lift.',
                        'Activa para perforar por secciones seguras. Partículas de vara de End y '
                        'encantamiento trazan la ruta. Usa X para el Ascenso de Ícaro.'),
            'wide': ('WIDE BORE: controlled 5×5 advance.',
                     'PERFORACIÓN ANCHA: avance controlado 5×5.',
                     'The same safe forward route with a 5×5 cross-section. It never reverses and will not '
                     'enter water, lava or modded fluids.',
                     'La misma ruta segura hacia delante, con sección 5×5. Nunca retrocede ni entra en agua, '
                     'lava o fluidos de mods.')},
 'hollow_axiom': {'hollow_pulse': ('NULLCUT: excavates the marked outer shell and preserves its core.',
                                   'CORTE NULO: excava la capa exterior marcada y conserva el núcleo.',
                                   'Aim at natural stone and press R. A dense cyan-and-violet outline shows '
                                   'the shell that will be mined and the smaller core that will remain. '
                                   'Ores, placed blocks, machines, fluids and protected cells are preserved.',
                                   'Apunta a piedra natural y pulsa R. Un contorno cian y violeta muestra la '
                                   'capa que se minará y el núcleo pequeño que permanecerá. Conserva menas, '
                                   'bloques colocados, máquinas, fluidos y zonas protegidas.')},
 'worldbreaker': {'carve': ('CARVE: drive a long rounded mining tunnel.',
                            'CARVE: abre un túnel minero largo y redondeado.',
                            'Aim at the cutting face. Activate once to excavate successive rounded sections '
                            'along your view axis.',
                            'Apunta al frente de corte. Activa una vez para excavar secciones redondeadas '
                            'sucesivas sobre el eje de la mirada.'),
                  'fracture': ('FRACTURE: split a widening mining fan.',
                               'FRACTURE: abre una fractura minera en abanico.',
                               'Aim and activate. A low fan widens as the cut advances, rather than removing '
                               'a cuboid.',
                               'Apunta y activa. Un abanico bajo se ensancha al avanzar el corte; no vacía '
                               'un cuboide.'),
                  'cleave': ('CLEAVE: cut a smooth oval face.',
                             'CLEAVE: corta una cara ovalada y lisa.',
                             'Aim and activate to carve a broad, rounded oval opening, not a sharp diamond. '
                             'The scheduler checks each slice and stops at barriers.',
                             'Apunta y activa para excavar una abertura ovalada y redondeada, no un rombo '
                             'afilado. El planificador revisa cada sección y se detiene ante barreras.'),
                  'core_drill': ('CORE DRILL: sink a deep vertical shaft.',
                                 'CORE DRILL: hunde un pozo vertical profundo.',
                                 'Activate to drill vertically below your feet. Slow Falling and fall-damage '
                                 'protection last until it ends; the shaft stops at the first barrier.',
                                 'Activa para perforar verticalmente bajo tus pies. Recibes caída lenta y '
                                 'protección contra daño por caída hasta que termine; el pozo para ante la '
                                 'primera barrera.'),
                  'world_shatter': ('WORLD SHATTER: excavate a vast terraced wedge.',
                                    'WORLD SHATTER: excava una enorme cuña escalonada.',
                                    'Aim and activate once. Successive sections widen and deepen into a '
                                    'large directional excavation. Real harvesting is spread across ticks, '
                                    'not one server-freezing explosion.',
                                    'Apunta y activa una vez. Las secciones se ensanchan y profundizan en '
                                    'una gran excavación direccional. La extracción real se distribuye por '
                                    'ticks; no es una explosión que congele el servidor.'),
                  'region_break': ('REGION BREAK: mine a selected cuboid safely.',
                                   'ROTURA REGIONAL: mina una selección cuboidal con seguridad.',
                                   'Choose this mode, select two corners, wait for analysis and confirm. '
                                   'Only blocks your pick can harvest are mined; fluids, bedrock, '
                                   'containers, claims and other barriers stay intact.',
                                   'Elige este modo, marca dos esquinas, espera el análisis y confirma. Solo '
                                   'extrae bloques que el pico pueda picar; fluidos, bedrock, contenedores, '
                                   'reclamos y otras barreras quedan intactas.')},
 'exodium': {'starfall': ('STARFALL LANCE: massive, safe excavation.',
                          'LANZA ESTELAR: perforación masiva y segura.',
                          'Opens a 13×9 gallery up to 48 blocks ahead. Work is spread across ticks and stops '
                          'at any barrier.',
                          'Abre una galería de 13×9 hasta 48 bloques frente a ti. La extracción se reparte '
                          'por ticks y se detiene antes de cualquier barrera.')},
 'iridium': {'orefall': ('OREFALL: center-out lode extraction.',
                         'LLUVIA DE MENAS: extracción de veta desde el centro.',
                         'Mines loaded, harvestable ores in a 29×15×29 ellipsoid around your target, nearest '
                         'positions first. Each ore is mined as soon as it is found, up to the server work '
                         'limit. Drops from ores mined with this pick glow; X recalls them. It never loads '
                         'chunks or crosses protections.',
                         'Extrae menas picables cargadas dentro de una elipse 29×15×29 alrededor del '
                         'objetivo, empezando por las posiciones cercanas. Mina cada mena al encontrarla, '
                         'hasta el límite de trabajo del servidor. Los drops de estas menas brillan y X los '
                         'atrae. No carga chunks ni atraviesa protecciones.')},
 'hellspec': {'hellforge': ('INFERNAL BLOOM: a five-petal quarry.',
                            'FLOR INFERNAL: una cantera de cinco pétalos.',
                            'Aim at natural geology or an ore and press R. An orange outline marks one '
                            'central chamber and four side petals before excavation begins. Player-placed '
                            'blocks, machines and protected cells stay safe.',
                            'Apunta a geología natural o una mena y pulsa R. Un contorno naranja marca una '
                            'cámara central y cuatro pétalos laterales antes de excavar. Protege bloques '
                            'colocados, máquinas y zonas protegidas.')},
 'seam_ripper': {'connected': ('CONNECTED SEAM: mine a same-material geological vein.',
                               'VETA CONECTADA: extrae un filón geológico del mismo material.',
                               'Aim at natural stone and press R. The pickaxe follows only connected blocks '
                               'of that exact material, up to 256. Ores, placed blocks, fluids and machines '
                               'are preserved.',
                               'Apunta a piedra natural y pulsa R. El pico sigue solo bloques conectados de '
                               'ese material exacto, hasta 256. Conserva menas, bloques colocados, fluidos y '
                               'máquinas.')}}

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
