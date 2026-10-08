#!/usr/bin/env python3
"""Dependency-free class-file inspection: method bytecode and resolved constant operands."""
import struct, zipfile, pathlib, hashlib, json
class Reader:
 def __init__(self,b): self.b=b; self.i=0
 def take(self,n): v=self.b[self.i:self.i+n]; self.i+=n; return v
 def u1(self): return self.take(1)[0]
 def u2(self): return struct.unpack('>H',self.take(2))[0]
 def u4(self): return struct.unpack('>I',self.take(4))[0]
def inspect(b):
 r=Reader(b); r.take(8); cp=[None]; n=r.u2()
 while len(cp)<n:
  t=r.u1()
  if t==1: v=r.take(r.u2()).decode('utf8','replace')
  elif t in (3,4): v=str(struct.unpack('>i' if t==3 else '>f',r.take(4))[0])
  elif t in (5,6): v=str(struct.unpack('>q' if t==5 else '>d',r.take(8))[0])
  elif t in (7,8,16,19,20): v=(r.u2(),)
  elif t in (9,10,11,12,17,18): v=(r.u2(),r.u2())
  elif t==15: v=(r.u1(),r.u2())
  else: raise ValueError(t)
  cp.append((t,v))
  if t in (5,6): cp.append(None)
 def resolve(i):
  if not i or i>=len(cp) or cp[i] is None: return '?'
  t,v=cp[i]
  if isinstance(v,str): return v
  if t in (17,18): return 'bootstrap:'+str(v[0])+' '+resolve(v[1])
  if t==15: return 'handle:'+str(v[0])+' '+resolve(v[1])
  return ' '.join(resolve(k) for k in v)
 out=[]; r.take(6); r.take(r.u2()*2)
 for section in ('FIELD','METHOD'):
  for _ in range(r.u2()):
   access,name,desc=r.u2(),r.u2(),r.u2(); out.append(f'{section} {resolve(name)} {resolve(desc)}')
   for _ in range(r.u2()):
    an=r.u2(); data=r.take(r.u4())
    if resolve(an)=='Code':
     c=Reader(data); c.take(4); code=c.take(c.u4()); out.append('  bytecode '+code.hex())
     # Constant operands in executable instructions (not arbitrary byte scanning).
     p=0
     while p<len(code):
      at=p; op=code[p]; p+=1; operand=None
      if op==0xaa:
       p=(p+3)&~3; low,high=struct.unpack('>ii',code[p+4:p+12]); p+=12+4*(high-low+1)
      elif op==0xab:
       p=(p+3)&~3; count=struct.unpack('>i',code[p+4:p+8])[0]; p+=8+8*count
      elif op==0xc4: p+=5 if code[p]==0x84 else 3
      else:
       size= (1 if op in [0x10,0x12,0xbc,0xa9] or 0x15<=op<=0x19 or 0x36<=op<=0x3a else
              2 if op in [0x11,0x13,0x14,0x84,0xbb,0xbd,0xc0,0xc1,0xc6,0xc7] or 0x99<=op<=0xa8 or 0xb2<=op<=0xb8 else
              3 if op==0xc5 else 4 if op in [0xb9,0xba,0xc8,0xc9] else 0)
       if op==0x12: operand=code[p]
       elif op in [0x13,0x14,0xbb,0xbd,0xc0,0xc1,0xc5] or 0xb2<=op<=0xba: operand=int.from_bytes(code[p:p+2],'big')
       p+=size
      if operand: out.append(f'  {at:04x} op={op:02x} {resolve(operand)}')
 return '\n'.join(out)
for path in sorted(pathlib.Path('referencias').glob('*/*.jar')):
 z=zipfile.ZipFile(path); out=['# '+str(path),'SHA256 '+hashlib.sha256(path.read_bytes()).hexdigest()]
 for name in z.namelist():
  if name.endswith('.class'): out += ['\n## '+name,inspect(z.read(name))]
  elif name.endswith(('.json','.toml')): out+=['\n## '+name,z.read(name).decode('utf8','replace')]
 out+=['\n## All entries','\n'.join(z.namelist())]
 pathlib.Path('docs/audit/'+path.parent.name+'.txt').write_text('\n'.join(line.rstrip() for line in '\n'.join(out).splitlines()).rstrip()+'\n')
