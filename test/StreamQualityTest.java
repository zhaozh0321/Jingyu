package cn.jingyu.deepseek;
import java.util.*;import java.util.concurrent.atomic.AtomicInteger;import android.view.*;import android.widget.ListView;

public final class StreamQualityTest {
 private static int checks;
 private static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;System.out.println("PASS "+name);}
 private static List<Integer> pieces(String raw,int step){StreamBoundary scanner=new StreamBoundary();List<Integer> all=new ArrayList<>();for(int end=1;end<raw.length();end+=step)all.addAll(scanner.append(raw.substring(0,end)));all.addAll(scanner.append(raw));return all;}
 public static void main(String[] args){
  AtomicInteger executions=new AtomicInteger();Choreographer clock=Choreographer.getInstance();SmoothStream.FrameCoalescer frames=new SmoothStream.FrameCoalescer(executions::incrementAndGet);
  for(int i=0;i<1000;i++)frames.request();check(clock.pendingCount()==1,"1000 incoming refresh requests coalesce into one display callback");clock.frame();check(executions.get()==1&&clock.pendingCount()==0,"idle callback neither redraws again nor keeps a timer alive");
  frames.request();frames.cancel();clock.frame();check(executions.get()==1,"leaving screen cancels pending frame work");
  AtomicInteger count=new AtomicInteger();SmoothStream.FrameCoalescer[] drain={null};drain[0]=new SmoothStream.FrameCoalescer(()->{if(count.incrementAndGet()<3)drain[0].request();});drain[0].request();clock.frame();check(count.get()==1&&clock.pendingCount()==1,"a reveal backlog can request only the following frame");clock.frame();clock.frame();check(count.get()==3&&clock.pendingCount()==0,"drained content ends the display loop");
  ListView list=new ListView();View item=new View();list.child=item;list.first=7;item.height=200;item.intrinsicHeight=1800;SmoothStream.stick(list,7,item);
  check(item.measures==1&&item.lastWidthMode==View.MeasureSpec.EXACTLY,"bottom anchor measures the changed row at its actual width");
  check(list.selections==1&&list.offset==-1232,"three-screen answer uses one bottom offset based on new measured height");
  SmoothStream.stick(list,7,item);check(list.selections==1,"unchanged bottom position makes no extra selection jump");
  item.intrinsicHeight=1900;SmoothStream.stick(list,7,item);check(list.selections==2&&list.offset==-1332,"growth advances directly to the new bottom without a top-alignment step");
  check(SmoothStream.visibleItem(list,4)==null&&SmoothStream.visibleItem(list,7)==item,"history scrolling excludes the offscreen active row");
  list.height=0;SmoothStream.stick(list,7,item);check(list.selections==2,"unmeasured viewport does not issue a bogus selection");
  String doc="## 标题😀\n\n**粗体**和[链接](https://example.org)\n\n下一段，保留所有字符。";
  check(pieces(doc,1).equals(pieces(doc,1000)),"one-code-unit deltas produce the same raw block boundaries as a complete response");
  List<Integer> cuts=pieces(doc,2);StringBuilder recovered=new StringBuilder();int from=0;for(int end:cuts){recovered.append(doc,from,end);from=end;}recovered.append(doc,from,doc.length());check(recovered.toString().equals(doc),"raw source offsets preserve Markdown markers links Chinese and emoji exactly");
  String code="前文\n\n````java\nfirst\n\n```\nstill code\n\n````\n\n结束";cuts=pieces(code,1);check(cuts.size()==2&&cuts.get(1)==code.indexOf("结束"),"blank lines and short closing fences do not prematurely freeze code");
  String tilde="~~~text\n\n```\n\n~~~\n\n尾巴";cuts=pieces(tilde,3);check(cuts.size()==1&&cuts.get(0)==tilde.indexOf("尾巴"),"mixed fence markers remain within the original fenced block");
  String math="$$\na^2\n\n+b^2\n$$\n\n余文";cuts=pieces(math,1);check(cuts.size()==1&&cuts.get(0)==math.indexOf("余文"),"display math retains internal blank lines until its delimiter closes");
  String bracket="\\[\nx+1\n\n=y\n\\]\n\n余文";cuts=pieces(bracket,2);check(cuts.size()==1&&cuts.get(0)==bracket.indexOf("余文"),"bracket-delimited display formulas use the same safe boundary rule");
  String inline="写作 `$$` 和 ``反引号`例子``。\n\n下一段";cuts=pieces(inline,1);check(cuts.size()==1&&cuts.get(0)==inline.indexOf("下一段"),"literal math delimiters inside inline code do not lock the document");
  String escaped="转义 \\$\\$ 不是公式。\r\n\r\n下一段";cuts=pieces(escaped,1);check(cuts.size()==1&&cuts.get(0)==escaped.indexOf("下一段"),"escaped dollar signs and Windows line endings stay intact");
  StreamBoundary scanner=new StreamBoundary();scanner.append("原始内容\n\n");check(scanner.append("原始内容\n\n").isEmpty(),"duplicate source adds no block work");check(!scanner.accepts("被替换的内容"),"tool repair can detect source replacement instead of retaining stale spans");scanner.reset();check(scanner.append("新段落\n\n").equals(Arrays.asList(5)),"reset starts raw positions from zero for a repaired answer");
  System.out.println("STREAM TOTAL "+checks+" scheduling, anchoring and source-boundary checks passed");
 }
}
