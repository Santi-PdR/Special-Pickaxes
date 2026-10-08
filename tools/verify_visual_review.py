"""Release approval must reference a successful run of the identical runtime/test/tooling source."""
import json,pathlib,re,subprocess
from mining_catalog import IDS,MODES
review=json.loads(pathlib.Path('docs/mining-rework/VISUAL-REVIEW.json').read_text())
sha=review['source_commit'];assert re.fullmatch('[0-9a-f]{40}',sha)
run=json.loads(subprocess.check_output(['gh','run','view',str(review['run_id']),'--json','headSha,conclusion'],text=True))
assert run['conclusion']=='success' and run['headSha']==sha,'Reviewed run was not successful for that commit'
subprocess.run(['git','fetch','--no-tags','origin',sha],check=True)
subprocess.run(['git','diff','--exit-code',sha,'HEAD','--','src','tools','.github','build.gradle','gradle.properties','gradle','settings.gradle'],check=True)
expected={f'ux-{id}-{mode}.png' for id in IDS for mode in MODES[id]}|{f'ux-shift-{id}-{mode}.png' for id in IDS for mode in MODES[id]}
expected|={'inventory.png','gallery.png','supreme-executing.png','supreme-complete.png'}
assert expected<=set(review['reviewed_images']),'Incomplete visual review'
assert review['approved'] is True
print('VISUAL_REVIEW_APPROVED_FOR_IDENTICAL_SOURCE',sha)
