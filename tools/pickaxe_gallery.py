#!/usr/bin/env python3
"""Deliver genuine item sprites + genuine CI hand captures; no rendered/synthetic hand mockups."""
import argparse,base64,json,pathlib,shutil,zipfile,html
from PIL import Image
from mining_catalog import IDS
p=argparse.ArgumentParser();p.add_argument('captures',type=pathlib.Path);p.add_argument('output',type=pathlib.Path);a=p.parse_args()
a.output.mkdir(parents=True,exist_ok=True)
cards=[]
labels=['Palimpsest','Fault Choir','Eventide','Paradox Crucible','Interregnum','Worldloom','Icarus','Hollow Axiom','Worldbreaker','Exodium','Iridium','Hellspec','Seam Ripper']
for pick,label in zip(IDS,labels):
 model=json.loads(pathlib.Path('src/main/resources/assets/specialpickaxes/models/item',pick+'.json').read_text())
 texture=model['textures']['layer0'].split(':',1)[1]
 source=pathlib.Path('src/main/resources/assets/specialpickaxes/textures',texture+'.png')
 with Image.open(source) as icon:
  # Inventory sprite's first animation frame, nearest-neighbor enlargement; no repainting.
  size=icon.width;icon=icon.crop((0,0,size,min(size,icon.height))).convert('RGBA').resize((256,256),Image.Resampling.NEAREST)
  icon.save(a.output/(pick+'-icono.png'))
 hand=a.captures/('portrait-'+pick+'.jpg')
 if not hand.is_file():raise SystemExit('Missing genuine hand capture: '+str(hand))
 shutil.copyfile(hand,a.output/(pick+'-en-mano.jpg'))
 def uri(path,mime):return 'data:'+mime+';base64,'+base64.b64encode(path.read_bytes()).decode()
 cards.append(f'<section><h2>{html.escape(label)}</h2><div><figure><img class="icon" src="{uri(a.output/(pick+"-icono.png"),"image/png")}"><figcaption>Icono real · primer fotograma</figcaption></figure><figure><img class="hand" src="{uri(a.output/(pick+"-en-mano.jpg"),"image/jpeg")}"><figcaption>En mano · captura real de Minecraft / CI, reducida a 640×360</figcaption></figure></div></section>')
(a.output/'galeria.html').write_text('''<!doctype html><meta charset="utf-8"><title>Los 13 picos — iconos y en mano</title><style>body{background:#111822;color:#eff5ff;font:16px system-ui;margin:30px auto;max-width:1050px}h1{color:#6de5d5}section{background:#1c2735;padding:20px;border-radius:16px;margin:20px 0}section div{display:flex;align-items:center;flex-wrap:wrap;gap:20px}figure{margin:0}img{max-width:100%;border-radius:8px}.icon{width:200px;image-rendering:pixelated;background:#10151d}.hand{width:640px}figcaption{color:#b2c8db;margin-top:8px}</style><h1>Los 13 picos</h1><p>Iconos extraídos de los modelos reales del mod. Vistas en mano procedentes del cliente Minecraft de CI: no son maquetas ni imágenes generadas. Las capturas son en primera persona; no muestran todas las fases de las animaciones.</p>'''+''.join(cards),encoding='utf-8')
with zipfile.ZipFile(a.output/'picos-iconos-y-en-mano.zip','w',zipfile.ZIP_DEFLATED) as z:
 for path in sorted(a.output.iterdir()):
  if path.suffix in ('.png','.jpg','.html'):z.write(path,path.name)
print('Gallery: 13 real icons + 13 genuine hand captures')
