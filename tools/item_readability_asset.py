#!/usr/bin/env python3
"""Non-destructive readability variants: original silhouette, alpha, hue family and pixel grid.
No downloaded/generated replacement art. Original reference textures remain unchanged.
"""
from pathlib import Path
import json,struct,zlib,hashlib
from dawn_bloom_asset import write_png

ROOT=Path('src/main/resources/assets/specialpickaxes')
VARIANTS={'fault_choir':.72,'worldloom':.78,'hollow_axiom':.78}


def read_rgba(path):
    data=Path(path).read_bytes()
    if data[:8]!=b'\x89PNG\r\n\x1a\n':raise ValueError('Not PNG')
    width,height,depth,color,compression,filtering,interlace=struct.unpack('>IIBBBBB',data[16:29])
    if (depth,color,compression,filtering,interlace)!=(8,6,0,0,0):raise ValueError('Expected non-interlaced 8-bit RGBA')
    offset=8;compressed=bytearray()
    while offset<len(data):
        size=struct.unpack('>I',data[offset:offset+4])[0];kind=data[offset+4:offset+8];payload=data[offset+8:offset+8+size]
        if kind==b'IDAT':compressed.extend(payload)
        offset+=size+12
    raw=zlib.decompress(compressed);stride=width*4;previous=bytearray(stride);rows=[]
    if len(raw)!=(stride+1)*height:raise ValueError('Unexpected PNG scanline size')
    for y in range(height):
        start=y*(stride+1);mode=raw[start];row=bytearray(raw[start+1:start+1+stride])
        for i in range(stride):
            left=row[i-4] if i>=4 else 0;above=previous[i];corner=previous[i-4] if i>=4 else 0
            if mode==0:predict=0
            elif mode==1:predict=left
            elif mode==2:predict=above
            elif mode==3:predict=(left+above)//2
            elif mode==4:
                p=left+above-corner;pa,pb,pc=abs(p-left),abs(p-above),abs(p-corner)
                predict=left if pa<=pb and pa<=pc else above if pb<=pc else corner
            else:raise ValueError('Unknown PNG filter')
            row[i]=(row[i]+predict)&255
        rows.append([tuple(row[i:i+4]) for i in range(0,stride,4)]);previous=row
    return rows


def readable(rows,gamma):
    if not 0<gamma<=1:raise ValueError('Gamma must brighten without inversion')
    def adjust(pixel):
        r,g,b,a=pixel
        return tuple(round(255*((channel/255)**gamma)) for channel in (r,g,b))+(a,) if a else pixel
    # A 2x nearest-neighbor grid gives a standard 32px item without rounding its silhouette.
    return [[adjust(pixel) for pixel in row for _ in range(2)] for row in rows for _ in range(2)]


def main():
    manifest=Path('docs/mining-rework/ASSET-PROVENANCE.json');records=json.loads(manifest.read_text())
    for name,gamma in VARIANTS.items():
        rows=readable(read_rgba(ROOT/'textures/item'/f'{name}.png'),gamma)
        path=ROOT/'textures/item'/f'{name}_v2.png';write_png(path,rows,len(rows[0]),len(rows))
        model=ROOT/'models/item'/f'{name}.json';data=json.loads(model.read_text());data['textures']['layer0']='specialpickaxes:item/'+name+'_v2';model.write_text(json.dumps(data,indent=2)+'\n')
        record=next(record for record in records if record['artifact']==name)
        record.update(runtime_texture=path.name,runtime_sha256=hashlib.sha256(path.read_bytes()).hexdigest(),
                      adaptation='Non-destructive contrast/readability adaptation of the existing reference sprite. Original source retained unchanged; silhouette and alpha preserved on a 2x nearest-neighbor grid. Reproducible with tools/item_readability_asset.py.')
    manifest.write_text(json.dumps(records,indent=2,ensure_ascii=False)+'\n')


if __name__=='__main__':main()
