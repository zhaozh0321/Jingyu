package cn.jingyu.deepseek;

import java.util.*;import java.util.regex.*;import org.commonmark.node.*;import org.commonmark.parser.Parser;

/** Reuse bundled CommonMark's inline AST instead of parsing nested emphasis with regex. */
final class DocumentInline {
 static final class Run {String text,link="";boolean bold,italic,code,math;Run(String text){this.text=text;}}
 private static final Parser PARSER=Parser.builder().build();
 private final List<Run> runs=new ArrayList<>();private final List<String> formulas=new ArrayList<>();private final String marker;
 private DocumentInline(String raw){String prefix="JINGYUMATH";while(raw.contains(prefix))prefix+="X";marker=prefix;}
 static List<Run> parse(String raw){DocumentInline p=new DocumentInline(raw);String protectedText=p.protect(raw);p.walk(PARSER.parse(protectedText),false,false,"");return p.runs;}
 private String protect(String raw){StringBuilder out=new StringBuilder();for(int i=0;i<raw.length();){char c=raw.charAt(i);if(c=='`'){int count=1;while(i+count<raw.length()&&raw.charAt(i+count)=='`')count++;String delimiter=raw.substring(i,i+count);int end=raw.indexOf(delimiter,i+count);if(end>=0){out.append(raw,i,end+count);i=end+count;continue;}}
  boolean dollar=c=='$'&&(i==0||raw.charAt(i-1)!='\\');boolean slash=c=='\\'&&i+1<raw.length()&&raw.charAt(i+1)=='(';if(dollar||slash){int length=dollar?1:2;String endMarker=dollar?"$":"\\)";int end=raw.indexOf(endMarker,i+length);if(end>i+length){String math=raw.substring(i+length,end);if(!math.contains("\n")){int index=formulas.size();formulas.add(math);out.append(marker).append(index).append("END");i=end+length;continue;}}}out.append(c);i++;}return out.toString();}
 private void text(String value,boolean bold,boolean italic,String link,boolean code){Matcher m=Pattern.compile(Pattern.quote(marker)+"([0-9]+)END").matcher(value);int start=0;while(m.find()){if(m.start()>start)add(value.substring(start,m.start()),bold,italic,link,code,false);int n=Integer.parseInt(m.group(1));add(formulas.get(n),bold,italic,link,false,true);start=m.end();}if(start<value.length())add(value.substring(start),bold,italic,link,code,false);}
 private void add(String value,boolean bold,boolean italic,String link,boolean code,boolean math){Run r=new Run(value);r.bold=bold;r.italic=italic;r.link=link;r.code=code;r.math=math;runs.add(r);}
 private void walk(Node node,boolean bold,boolean italic,String link){if(node instanceof Text){text(((Text)node).getLiteral(),bold,italic,link,false);return;}if(node instanceof Code){add(((Code)node).getLiteral(),bold,italic,link,true,false);return;}if(node instanceof SoftLineBreak||node instanceof HardLineBreak){add("\n",bold,italic,link,false,false);return;}if(node instanceof HtmlInline){text(((HtmlInline)node).getLiteral(),bold,italic,link,false);return;}if(node instanceof StrongEmphasis)bold=true;if(node instanceof Emphasis)italic=true;if(node instanceof Link){String url=((Link)node).getDestination();if(url.matches("(?i)https?://.*"))link=url;}
  for(Node child=node.getFirstChild();child!=null;child=child.getNext())walk(child,bold,italic,link);
 }
}
