"""Gradle may exit zero after a Forge launch failure: require actual complete execution."""
import pathlib,re,sys
expected=sum(len(re.findall(r'@GameTest\(',p.read_text())) for p in pathlib.Path('src/main/java/io/github/santipdr/specialpickaxes/test').glob('*GameTests.java'))
text=pathlib.Path(sys.argv[1]).read_text()
counts=re.findall(r'(\d+) tests? (?:passed|done)',text,re.I)
if str(expected) not in counts:raise SystemExit(f'Missing completed GameTest summary for {expected} tests; observed {counts}')
if re.search(r'\b[1-9]\d* (?:required )?tests? failed',text,re.I):raise SystemExit('GameTests reported failures')
print(f'GAMETEST_EXECUTION_CONFIRMED: {expected}')
