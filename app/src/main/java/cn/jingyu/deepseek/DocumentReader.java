package cn.jingyu.deepseek;

import android.content.Context;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.graphics.*;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.json.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;

public final class DocumentReader {
 public static String name(Context context,Uri uri){String name=uri.getLastPathSegment()==null?"附件":uri.getLastPathSegment();try(Cursor c=context.getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){if(c!=null&&c.moveToFirst())name=c.getString(0);}catch(Exception ignored){}return name;}
 public static byte[] bytes(Context context,Uri uri,int max)throws IOException{try(InputStream in=context.getContentResolver().openInputStream(uri)){if(in==null)throw new IOException("无法打开文件");return read(in,max);}}
 private static byte[] read(InputStream in,int max)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(out.size()+n>max)throw new IOException("文件过大，请拆分后上传");out.write(buffer,0,n);}return out.toByteArray();}
 public static List<JSONObject> parse(Context context,Uri uri)throws Exception{
  String name=name(context,uri),lower=name.toLowerCase(Locale.ROOT);byte[] data=bytes(context,uri,10*1024*1024);List<JSONObject> files=new ArrayList<>();String mime=context.getContentResolver().getType(uri);
  if(lower.matches(".*\\.(jpg|jpeg|png|webp|gif)$")||(mime!=null&&mime.startsWith("image/"))){BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,options);options.inJustDecodeBounds=false;options.inSampleSize=1;while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>2400)options.inSampleSize*=2;Bitmap bitmap=BitmapFactory.decodeByteArray(data,0,data.length,options);if(bitmap==null)throw new IOException("无法读取图片，请转为 JPG 或 PNG");files.add(image(name,bitmap));return files;}
  if(lower.endsWith(".pdf")){File temporary=File.createTempFile("pdf-",".pdf",context.getCacheDir());try{try(FileOutputStream out=new FileOutputStream(temporary)){out.write(data);}try(ParcelFileDescriptor fd=ParcelFileDescriptor.open(temporary,ParcelFileDescriptor.MODE_READ_ONLY);PdfRenderer pdf=new PdfRenderer(fd)){if(pdf.getPageCount()>8)throw new IOException("PDF 最多 8 页，请拆分后上传。页面会作为图片交给 Flash 识别。");for(int i=0;i<pdf.getPageCount();i++){try(PdfRenderer.Page page=pdf.openPage(i)){float scale=Math.min(2f,1440f/Math.max(page.getWidth(),page.getHeight()));Bitmap bitmap=Bitmap.createBitmap(Math.max(1,(int)(page.getWidth()*scale)),Math.max(1,(int)(page.getHeight()*scale)),Bitmap.Config.ARGB_8888);bitmap.eraseColor(Color.WHITE);page.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);files.add(image(name+" · 第 "+(i+1)+" 页",bitmap));}}}return files;}finally{temporary.delete();}}
  String text;
  if(lower.endsWith(".docx")||lower.endsWith(".xlsx"))text=office(data,lower.endsWith(".xlsx"));
  else if(lower.matches(".*\\.(txt|md|csv|tsv|json|log|py|c|cpp|h|java|js|ts|html|css|xml|yaml|yml)$")){try{text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(data)).toString();}catch(CharacterCodingException e){text=Charset.forName("GB18030").decode(ByteBuffer.wrap(data)).toString();}}
  else throw new IOException("支持图片、PDF、DOCX、XLSX 和文本文件");
  if(text.indexOf('\0')>=0||text.trim().isEmpty())throw new IOException("文件中没有可读文字");if(text.length()>60000)throw new IOException("文档文字超过 6 万字符，请拆分后上传");files.add(new JSONObject().put("name",name).put("kind","text").put("text",text));return files;
 }
 private static JSONObject image(String name,Bitmap bitmap)throws JSONException{int size=Math.max(bitmap.getWidth(),bitmap.getHeight());if(size>1600){Bitmap scaled=Bitmap.createScaledBitmap(bitmap,Math.max(1,bitmap.getWidth()*1600/size),Math.max(1,bitmap.getHeight()*1600/size),true);bitmap.recycle();bitmap=scaled;}ByteArrayOutputStream out=new ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.JPEG,86,out);bitmap.recycle();return new JSONObject().put("name",name).put("kind","image").put("data","data:image/jpeg;base64,"+Base64.encodeToString(out.toByteArray(),Base64.NO_WRAP));}
 private static String office(byte[] data,boolean spreadsheet)throws Exception{
  Map<String,byte[]> entries=new TreeMap<>();int expanded=0;
  try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(data))){ZipEntry entry;while((entry=zip.getNextEntry())!=null){String name=entry.getName().replace('\\','/');while(name.startsWith("/"))name=name.substring(1);if((!spreadsheet&&name.equals("word/document.xml"))||(spreadsheet&&(name.equals("xl/sharedStrings.xml")||(name.startsWith("xl/worksheets/")&&name.endsWith(".xml"))))){byte[] value=read(zip,8*1024*1024-expanded);expanded+=value.length;entries.put(name,value);}}}
  if(!spreadsheet){byte[] doc=entries.get("word/document.xml");if(doc==null)throw new IOException("Word 文件格式不正确");return xmlText(doc,"t",true);}
  List<String> shared=new ArrayList<>();byte[] strings=entries.get("xl/sharedStrings.xml");if(strings!=null){XmlPullParser parser=parser(strings);StringBuilder value=new StringBuilder();int event;while((event=parser.next())!=XmlPullParser.END_DOCUMENT){if(event==XmlPullParser.START_TAG&&parser.getName().equals("si"))value.setLength(0);else if(event==XmlPullParser.START_TAG&&parser.getName().equals("t"))value.append(parser.nextText());else if(event==XmlPullParser.END_TAG&&parser.getName().equals("si"))shared.add(value.toString());}}
  StringBuilder result=new StringBuilder();for(Map.Entry<String,byte[]> entry:entries.entrySet()){if(!entry.getKey().contains("worksheets/"))continue;result.append("\n[工作表 ").append(entry.getKey()).append("]\n");XmlPullParser parser=parser(entry.getValue());String type="",cell="";int event;while((event=parser.next())!=XmlPullParser.END_DOCUMENT){if(event==XmlPullParser.START_TAG&&parser.getName().equals("c")){type=parser.getAttributeValue(null,"t");cell=parser.getAttributeValue(null,"r");}else if(event==XmlPullParser.START_TAG&&(parser.getName().equals("v")||parser.getName().equals("t"))){String value=parser.nextText();if("s".equals(type)){int index=Integer.parseInt(value);value=index>=0&&index<shared.size()?shared.get(index):value;}result.append(cell).append(": ").append(value).append('\t');}else if(event==XmlPullParser.END_TAG&&parser.getName().equals("row"))result.append('\n');if(result.length()>60000)throw new IOException("表格文字过多，请拆分后上传");}}return result.toString();
 }
 private static XmlPullParser parser(byte[] data)throws Exception{XmlPullParser parser=Xml.newPullParser();parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,true);parser.setInput(new ByteArrayInputStream(data),"UTF-8");return parser;}
 private static String xmlText(byte[] data,String tag,boolean paragraphs)throws Exception{XmlPullParser parser=parser(data);StringBuilder result=new StringBuilder();int event;while((event=parser.next())!=XmlPullParser.END_DOCUMENT){if(event==XmlPullParser.START_TAG&&parser.getName().equals(tag))result.append(parser.nextText());else if(paragraphs&&event==XmlPullParser.END_TAG&&parser.getName().equals("p"))result.append('\n');if(result.length()>60000)throw new IOException("文档文字过多，请拆分后上传");}return result.toString();}
}
