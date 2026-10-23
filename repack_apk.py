from pathlib import Path
import zipfile,sys,os
apk,dex=map(Path,sys.argv[1:3])
tmp=apk.with_suffix('.repacked')
with zipfile.ZipFile(apk) as src, zipfile.ZipFile(tmp,'w') as dst:
 for item in src.infolist():
  if item.filename=='classes.dex':continue
  data=src.read(item);item.filename=item.filename.replace(chr(92),'/');dst.writestr(item,data)
 dst.write(dex,'classes.dex',compress_type=zipfile.ZIP_DEFLATED)
os.replace(tmp,apk)
