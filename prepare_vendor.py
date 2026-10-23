from pathlib import Path
import zipfile, shutil, xml.etree.ElementTree as E
root=Path(__file__).resolve().parent
resources=E.Element('resources')
for aar in (root/'vendor').glob('*.aar'):
 with zipfile.ZipFile(aar) as z:
  (root/'vendor'/(aar.stem+'.jar')).write_bytes(z.read('classes.jar'))
  for name in z.namelist():
   if name.startswith('assets/') and not name.endswith('/'):
    target=root/'app/src/main'/name;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(z.read(name))
  if 'res/values/values.xml' in z.namelist():
   for item in E.fromstring(z.read('res/values/values.xml')): resources.append(item)
E.ElementTree(resources).write(root/'app/src/main/res/values/vendor.xml',encoding='utf-8',xml_declaration=True)
