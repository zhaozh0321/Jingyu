package cn.jingyu.deepseek;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

/** One bounded Markdown model shared by the Word and PDF renderers. */
final class DocumentLayout {
 static final class Block {
  String kind,text; int level,start=1; boolean ordered; List<String[]> rows; String[] align;
  Block(String kind,String text){this.kind=kind;this.text=text;}
 }
 final List<Block> blocks=new ArrayList<>();
 final String title,subtitle,style,accent; final boolean landscape,numbers,toc; final float font;
 final int pageWidth,pageHeight,bodyWidth;
 DocumentLayout(JSONObject spec)throws Exception {
  title=ArtifactSkills.title(spec); style=ArtifactSkills.style(spec); accent=ArtifactSkills.accent(spec);
  JSONObject options=spec.optJSONObject("layout"); if(options==null)options=new JSONObject();
  subtitle=options.optString("subtitle").trim();landscape=options.optString("orientation").equals("landscape");
  numbers=options.optBoolean("pageNumbers",true);toc=options.optBoolean("toc",false);
  font=(float)Math.max(10,Math.min(14,options.optDouble("fontSize",11)));
  pageWidth=landscape?842:595;pageHeight=landscape?595:842;bodyWidth=pageWidth-104;
  parse(ArtifactFiles.withSources(spec).replace("\r\n","\n").replace('\r','\n'));
 }
 static String[] cells(String line){
  String s=line.trim();if(s.startsWith("|"))s=s.substring(1);if(s.endsWith("|")&&!s.endsWith("\\|"))s=s.substring(0,s.length()-1);
  List<String> cells=new ArrayList<>();StringBuilder cell=new StringBuilder();boolean code=false;
  for(int i=0;i<s.length();i++){char c=s.charAt(i);if(c=='\\'&&i+1<s.length()&&s.charAt(i+1)=='|'){cell.append('|');i++;}
   else if(c=='`'){code=!code;cell.append(c);}else if(c=='|'&&!code){cells.add(cell.toString().trim());cell.setLength(0);}else cell.append(c);}
  cells.add(cell.toString().trim());return cells.toArray(new String[0]);
 }
 static boolean separator(String line){String[] cells=cells(line);if(cells.length<2)return false;for(String c:cells)if(!c.matches(":?-{3,}:?"))return false;return true;}
 static boolean special(String line){String s=line.trim();return s.isEmpty()||s.matches("^(#{1,6}\\s|[-*+]\\s|[0-9]+[.)]\\s|>).*" )||s.startsWith("```")||s.startsWith("~~~")||s.startsWith("$$")||s.startsWith("\\[")||s.equals("<!-- pagebreak -->");}
 private void parse(String raw)throws Exception {
  String[] lines=raw.split("\n",-1);
  for(int i=0;i<lines.length;){String line=lines[i],s=line.trim();if(s.isEmpty()){i++;continue;}
   if(s.equals("<!-- pagebreak -->")){blocks.add(new Block("break",""));i++;continue;}
   if(s.startsWith("```")||s.startsWith("~~~")){String marker=s.substring(0,3);StringBuilder code=new StringBuilder();i++;while(i<lines.length&&!lines[i].trim().startsWith(marker)){if(code.length()>0)code.append('\n');code.append(lines[i++]);}if(i==lines.length)throw new IOException("代码围栏未闭合，请补齐后创建文件。");i++;blocks.add(new Block("code",code.toString()));continue;}
   if(s.startsWith("$$")||s.startsWith("\\[")){String end=s.startsWith("$$")?"$$":"\\]";StringBuilder formula=new StringBuilder(s.substring(2));i++;while(!formula.toString().trim().endsWith(end)&&i<lines.length)formula.append('\n').append(lines[i++]);String text=formula.toString().trim();if(!text.endsWith(end))throw new IOException("独立公式未闭合，请检查LaTeX。");blocks.add(new Block("math",text.substring(0,text.length()-2).trim()));continue;}
   if(i+1<lines.length&&line.contains("|")&&separator(lines[i+1])){Block t=new Block("table","");t.rows=new ArrayList<>();t.rows.add(cells(line));String[] rules=cells(lines[i+1]);int cols=t.rows.get(0).length;if(cols!=rules.length||cols>8)throw new IOException("文档表格须有一致的列数，最多8列；宽表可横向排版或使用Excel。");t.align=new String[cols];for(int c=0;c<cols;c++)t.align[c]=rules[c].endsWith(":")?(rules[c].startsWith(":")?"center":"right"):"left";
    i+=2;while(i<lines.length&&!lines[i].trim().isEmpty()&&lines[i].contains("|")){String[] row=cells(lines[i++]);if(row.length!=cols)throw new IOException("文档表格列数不一致，请补齐空单元格。");t.rows.add(row);}blocks.add(t);continue;}
   Matcher h=Pattern.compile("^\\s*(#{1,6})\\s+(.+)$").matcher(line);if(h.matches()){Block b=new Block("heading",h.group(2));b.level=h.group(1).length();blocks.add(b);i++;continue;}
   Matcher l=Pattern.compile("^(\\s*)([-*+]|[0-9]+[.)])\\s+(.*)$").matcher(line);if(l.matches()){Block b=new Block("list",l.group(3));b.level=Math.min(3,l.group(1).replace("\t","    ").length()/2);b.ordered=Character.isDigit(l.group(2).charAt(0));if(b.ordered)b.start=Integer.parseInt(l.group(2).replaceAll("[^0-9]",""));blocks.add(b);i++;continue;}
   if(s.startsWith(">")){StringBuilder quote=new StringBuilder(s.replaceFirst("^> ?",""));i++;while(i<lines.length&&lines[i].trim().startsWith(">"))quote.append('\n').append(lines[i++].trim().replaceFirst("^> ?",""));blocks.add(new Block("quote",quote.toString()));continue;}
   if(s.matches("[-*_]{3,}")){i++;continue;}
   StringBuilder p=new StringBuilder(line);i++;while(i<lines.length&&!special(lines[i])&&!(i+1<lines.length&&separator(lines[i+1]))){p.append('\n').append(lines[i++]);}blocks.add(new Block("paragraph",p.toString()));
  }
 }
 static int visualLength(String value){int width=0;for(int i=0;i<value.length();i++)width+=value.charAt(i)>255?2:1;return width;}
 static int[] widths(List<String[]> rows,int total){int cols=rows.get(0).length;double[] weights=new double[cols];double sum=0;for(int c=0;c<cols;c++){List<Integer> sizes=new ArrayList<>();for(String[] r:rows)sizes.add(Math.min(70,visualLength(r[c])));Collections.sort(sizes);int typical=sizes.get((sizes.size()-1)*3/4);weights[c]=Math.max(6,Math.sqrt(Math.max(1,typical))*3);sum+=weights[c];}int[] result=new int[cols];int used=0;for(int c=0;c<cols;c++){result[c]=(int)(total*weights[c]/sum);used+=result[c];}result[cols-1]+=total-used;return result;}
}
