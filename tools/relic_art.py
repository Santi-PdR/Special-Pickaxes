"""Authored 32-pixel silhouettes, material ramps and shared leather fittings. No AI imagery or randomness."""
HEADS=[
[(4,13),(5,7),(10,3),(19,2),(25,6),(28,12),(24,10),(21,6),(14,5),(10,8),(9,14)],
[(3,12),(6,3),(9,10),(13,2),(16,10),(21,3),(25,5),(27,13),(22,11),(18,14),(8,14)],
[(4,9),(8,4),(17,2),(25,5),(29,11),(24,15),(17,17),(8,15)],
[(2,11),(9,4),(15,7),(20,2),(29,7),(26,14),(21,10),(16,15),(10,10),(5,16)],
[(5,4),(22,3),(28,8),(26,16),(20,18),(10,16),(4,11)],
[(3,5),(8,8),(11,2),(16,7),(22,2),(24,8),(29,4),(27,15),(7,16)],
[(3,4),(25,4),(29,9),(25,16),(6,16),(3,12)],
[(2,11),(14,3),(28,4),(23,9),(29,17),(18,14),(12,18),(13,11)],
[(5,3),(24,3),(28,8),(24,18),(9,17),(3,10)],
[(2,7),(11,2),(16,7),(22,2),(30,7),(27,17),(20,14),(16,10),(11,16),(5,17)],
[(1,9),(8,2),(15,5),(21,1),(30,5),(29,12),(24,9),(21,17),(12,16),(8,10),(3,15)],
[(4,5),(11,2),(22,4),(27,9),(24,17),(18,19),(8,16),(3,11)],
[(2,13),(6,5),(12,2),(22,3),(28,10),(27,16),(22,11),(18,8),(12,8),(8,14)],
[(2,6),(10,3),(16,7),(22,3),(29,6),(27,14),(20,17),(15,12),(9,17),(3,14)]]
METALS=[(204,191,155),(110,174,165),(77,70,97),(168,180,198),(182,107,64),(139,169,198),(123,154,124),(189,125,74),(171,183,154),(179,155,168),(183,145,77),(151,135,173),(154,178,165),(169,132,153)]
def art(index,frame,colors):
 grid={};edge=(20,24,33);metal=METALS[index];rgb=colors[index];core=((rgb>>16)&255,(rgb>>8)&255,rgb&255)
 def line(x0,y0,x1,y1,color,width=1):
  n=max(abs(x1-x0),abs(y1-y0),1)
  for i in range(n+1):
   x=round(x0+(x1-x0)*i/n);y=round(y0+(y1-y0)*i/n)
   for a in range(-width,width+1):
    for b in range(-width,width+1):grid[x+a,y+b]=color
 line(8,28,17,11,edge,2);line(8,28,17,11,(90,61,48),1)
 for x,y in [(9,26),(11,22),(13,18)]:line(x-1,y-1,x+1,y,(163,117,72),0)
 poly=HEADS[index]
 inside=set()
 for y in range(1,20):
  for x in range(1,31):
   hit=False;j=len(poly)-1
   for i,(xi,yi) in enumerate(poly):
    xj,yj=poly[j]
    if (yi>y)!=(yj>y) and x<(xj-xi)*(y-yi)/(yj-yi)+xi:hit=not hit
    j=i
   if hit:inside.add((x,y))
 for x,y in inside:
  border=any((x+a,y+b)not in inside for a,b in [(1,0),(-1,0),(0,1),(0,-1)])
  light=(x,y-2)not in inside or (x-2,y)not in inside
  grid[x,y]=edge if border else tuple(max(0,min(255,c+(35 if light else -18 if y>12 else 0)))for c in metal)
 # Deliberately placed engravings, no floating random symbols.
 for x in range(9,24,3):
  if (x,8)in inside:grid[x,8]=tuple(max(0,c-55)for c in metal)
 for y in range(8,15):
  for x in range(12,19):
   d=abs(x-15)+abs(y-11)
   if d<=3:grid[x,y]=edge if d==3 else tuple(min(255,max(0,c+(18 if d==0 and frame%3==0 else -30 if y>11 else 0)))for c in core)
 # Distinct negative spaces: ring, hourglass, loom, cage, book and paired plates.
 if index in [0,2,5,6,8,9,11,13]:
  for x,y in [(8,10),(9,10),(22,10),(23,10)]:
   if (x,y)in inside:grid.pop((x,y),None)
 if index==10:line(23,4,25,7,(242,220,167),0)
 result=[]
 for y in range(64):
  for x in range(64):
   c=grid.get((x//2,y//2));result.append((*c,255)if c else(0,0,0,0))
 return result
