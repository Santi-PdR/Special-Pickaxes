from pathlib import Path
import gzip,struct
s=lambda x:struct.pack('>H',len(x.encode()))+x.encode()
def tag(t,n,v):return bytes([t])+s(n)+v
def integer(n,v):return tag(3,n,struct.pack('>i',v))
def li(n,t,values):return tag(9,n,bytes([t])+struct.pack('>i',len(values))+b''.join(values))
root=integer('DataVersion',3465)+li('size',3,[struct.pack('>i',12)]*3)+li('palette',10,[tag(8,'Name',s('minecraft:air'))+b'\0'])+li('blocks',10,[])+li('entities',10,[])+b'\0'
p=Path('src/main/resources/data/specialpickaxes/structures/empty.nbt');p.parent.mkdir(parents=True,exist_ok=True)
p.write_bytes(gzip.compress(b'\x0a\0\0'+root,mtime=0))
