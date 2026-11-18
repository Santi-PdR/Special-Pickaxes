#!/usr/bin/env python3
"""Author custom spell-particle sprites only. NEVER generates, edits or recolors pickaxe item art."""
import math
from pathlib import Path
import json
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
PALETTE = {'palimpsest':0xffd783,'choir':0x46ede0,'eventide':0xa67aff,'crucible':0xff8648,
           'interregnum':0x8ae5ff,'worldloom':0x64ffa9,'icarus':0xffc057,'axiom':0xd9ffbd,
           'worldbreaker':0xf1bf65,'exodium':0xff6ba5,'iridium':0x74d7d0,'hellspec':0xf35a39,'seam_ripper':0xf5ac79}
S = 4

def frame(kind, rgb, phase, alternate=False):
    ink = Image.new('RGBA',(32*S,32*S))
    draw = ImageDraw.Draw(ink)
    color = ((rgb>>16)&255,(rgb>>8)&255,rgb&255,190+phase*20)
    r = 7.5+phase*.65
    def point(x,y):return ((16+x)*S,(16+y)*S)
    def line(points,width=1):draw.line([point(x,y) for x,y in points],fill=color,width=max(1,int(width*S)),joint='curve')
    def orbit(radius,start=0,end=360,width=1):draw.arc([(16-radius)*S,(16-radius)*S,(16+radius)*S,(16+radius)*S],start,end,fill=color,width=max(1,int(width*S)))
    def star(n,outer,inner,rotation=-math.pi/2):
        pts=[]
        for j in range(n*2):
            a=rotation+j*math.pi/n;rr=outer if j%2==0 else inner;pts.append((math.cos(a)*rr,math.sin(a)*rr))
        line(pts+[pts[0]],1)
    if alternate:
        # Secondary glyphs depict the action: shafts, planes, intake funnels and erupted columns.
        if kind=='palimpsest':
            for y in (-6,0,6):line([(-9,y+2),(0,y-2),(9,y+2)],1.3)
        elif kind=='choir':
            for x in (-7,0,7):line([(x,-10),(x+3,-5),(x,-1),(x+3,4),(x,10)],1.2)
        elif kind=='eventide':
            orbit(10,20,160);orbit(10,200,340)
            for side in (-1,1):line([(side*10,0),(side*3,0),(side*6,-3)]);line([(side*3,0),(side*6,3)])
        elif kind=='crucible':
            line([(-10,-7),(10,-7),(7,7),(-7,7),(-10,-7)],1.2)
            for x in (-3,3):line([(x,-7),(x,7)])
            line([(-8,0),(8,0)])
        elif kind=='interregnum':
            for side in (-1,1):line([(side*10,-8),(side*6,-8),(side*6,8),(side*10,8)],1.5)
            star(4,4,1)
        elif kind=='worldloom':
            for y in (-6,0,6):line([(-10,y),(10,y)],1.4)
            line([(-6,-10),(-6,10)]);line([(6,-10),(6,10)])
        elif kind=='icarus':
            line([(-7,2),(0,-7),(7,2)],1.6);line([(-7,8),(0,-1),(7,8)],1.6)
        elif kind=='axiom':
            for sign in (-1,1):line([(sign*10,-9),(sign*5,-4),(sign*7,0),(sign*3,5),(sign*5,10)],1.3)
            line([(0,-7),(0,7)])
        elif kind=='worldbreaker':
            star(8,11,6);star(4,4,1)
        elif kind=='exodium':
            line([(-9,-8),(0,4),(9,-8)],1.4);line([(-6,4),(0,11),(6,4)],1.4)
            orbit(10,0,180)
        elif kind=='iridium':
            for x,y in ((-7,-5),(7,-5),(0,8)):
                line([(x,y-3),(x+3,y),(x,y+3),(x-3,y),(x,y-3)])
                line([(x*.6,y*.6),(0,0)])
        elif kind=='hellspec':
            for x in (-7,0,7):line([(x,9),(x-2,1),(x+1,-8),(x+3,1),(x,9)],1.2)
        else:
            line([(-10,-8),(10,-8),(10,8),(-10,8),(-10,-8)])
            for x in (-5,0,5):line([(x,-8),(x-3,8)],1.2)
    elif kind=='palimpsest':
        orbit(r,180,360);line([(-r,1),(r,1)]);star(4,4,1.2)
        for a in range(200,350,30):line([(math.cos(math.radians(a))*r,math.sin(math.radians(a))*r),(math.cos(math.radians(a))*(r+3),math.sin(math.radians(a))*(r+3))])
    elif kind=='choir':
        for k in (-1,0,1):line([(x,math.sin(x*.45+phase*.4)*2+k*4) for x in range(-10,11)],1)
    elif kind=='eventide':
        orbit(r,30+phase*15,310+phase*15,1.4);orbit(r*.45,180,470,1)
        line([(-2,0),(2,0)]);line([(0,-2),(0,2)])
    elif kind=='crucible':
        star(3,r,4,math.pi/2);star(3,r,4,-math.pi/2);orbit(3)
    elif kind=='interregnum':
        line([(-6,-9),(6,-9),(-5,9),(5,9)]);line([(-6,9),(6,9),(-5,-9),(5,-9)]);orbit(r+2,20,150);orbit(r+2,200,330)
    elif kind=='worldloom':
        line([(0,10),(0,-9)],1.5)
        for sign in (-1,1):
            for y in (-5,1,6):line([(0,y),(sign*7,y-4),(sign*4,y-6),(0,y-2)])
    elif kind=='icarus':
        line([(0,9),(0,-8)],1.2)
        for side in (-1,1):
            for k in range(4):line([(0,5-k*3),(side*(10-k*1.5),-4-k*1.8),(side*(5-k*.5),4-k*3)])
    elif kind=='axiom':
        for a in range(0,360,90):orbit(r,a+10,a+70,1.6)
        line([(-4,0),(0,4),(4,0),(0,-4),(-4,0)])
    elif kind=='worldbreaker':
        line([(-2,-11),(3,-5),(-2,-2),(4,2),(-3,11)],1.8)
        line([(-9,-5),(-4,-2),(-7,3)],1.2);line([(10,-3),(6,2),(8,8)],1.2)
    elif kind=='exodium':
        star(4,r,2.4);line([(-8,-11),(-3,-4)],1.5);line([(-11,-6),(-5,-2)],1)
    elif kind=='iridium':
        line([(0,-11),(7,-3),(5,7),(0,11),(-5,7),(-7,-3),(0,-11)],1.3)
        line([(-7,-3),(7,-3),(0,11),(0,-11),(-5,7),(5,7)])
    elif kind=='hellspec':
        for j in range(5):
            a=j*math.tau/5-math.pi/2
            line([(0,0),(math.cos(a-.3)*6,math.sin(a-.3)*6),(math.cos(a)*11,math.sin(a)*11),(math.cos(a+.3)*6,math.sin(a+.3)*6),(0,0)],1.3)
        orbit(2)
    else:
        for shift in (-5,0,5):line([(-10,shift+3),(-3,shift+1),(0,shift-2),(10,shift-3)],1.2)
    glow=ink.filter(ImageFilter.GaussianBlur(1.1*S))
    glow=Image.blend(Image.new('RGBA',ink.size),glow,.7)
    image=Image.alpha_composite(glow,ink)
    # A soft white center provides readable emission without a solid square background.
    core=Image.new('RGBA',ink.size);cd=ImageDraw.Draw(core)
    cd.ellipse([15*S,15*S,17*S,17*S],fill=(255,250,235,210))
    return Image.alpha_composite(image,core.filter(ImageFilter.GaussianBlur(.4*S))).resize((32,32),Image.Resampling.LANCZOS)

def main():
    textures=ROOT/'src/main/resources/assets/specialpickaxes/textures/particle/signature'
    particles=ROOT/'src/main/resources/assets/specialpickaxes/particles'
    textures.mkdir(parents=True,exist_ok=True)
    for kind,rgb in PALETTE.items():
        for alternate in (False,True):
            name=kind+('_alternate' if alternate else '')
            for phase in range(4):frame(kind,rgb,phase,alternate).save(textures/f'{name}_{phase}.png')
            (particles/f'{name}_signature.json').write_text(json.dumps({'textures':[f'specialpickaxes:signature/{name}_{n}' for n in range(4)]},indent=2)+'\n')
    print('Authored 26 primary/secondary particle glyphs, 4 frames each; item textures untouched.')
if __name__=='__main__':main()
