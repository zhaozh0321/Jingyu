package cn.jingyu.deepseek;
import org.json.*;
/** Regression tests for visible ordering and task-dependent request contracts. */
public final class ExperienceQualityTest {
 private static int checks;
 private static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;System.out.println("PASS "+name);}
 private static JSONObject user(String q)throws Exception{return new JSONObject().put("role","user").put("text",q);}
 private static JSONObject chat(String q)throws Exception{return new JSONObject().put("messages",new JSONArray().put(user(q)));}
 private static boolean has(JSONObject payload,String name)throws Exception{JSONArray all=payload.getJSONArray("tools");for(int i=0;i<all.length();i++)if(all.getJSONObject(i).getJSONObject("function").getString("name").equals(name))return true;return false;}
 public static void main(String[] args)throws Exception {
  JSONObject normal=Conversation.payload(chat("解释一下二阶系统的阻尼比"),new JSONObject());
  check(!normal.toString().contains("artifact_skill")&&!normal.toString().contains("personal_memory")&&!normal.toString().contains("file_tools"),"ordinary question has no artifact or memory rule blocks");
  check(!has(normal,"create_file")&&!has(normal,"remember_memory")&&!has(normal,"recall_conversation"),"ordinary question excludes large unrelated tool schemas");
  check(has(normal,"web_search")&&has(normal,"web_fetch")&&has(normal,"calculate"),"verification and local calculation remain available");
  JSONObject file=Conversation.payload(chat("生成一份Word讲义"),new JSONObject());
  check(file.toString().contains("artifact_skill")&&has(file,"create_file"),"explicit file request loads rules and executable tool together");
  JSONObject follow=chat("生成一份PDF讲义");follow.getJSONArray("messages").put(new JSONObject().put("role","assistant").put("status","complete").put("text","讲义内容"));follow.getJSONArray("messages").put(user("修改第二节，补充两个例子"));
  check(FileRequests.format(follow).equals("pdf")&&has(Conversation.payload(follow,new JSONObject()),"create_file"),"editing a previous document retains its output format");
  follow.getJSONArray("messages").put(user("不用生成文件，只解释原理"));check(FileRequests.format(follow).isEmpty(),"explicit no-file follow-up overrides previous artifact context");
  check(PromptPolicy.memoryNeeded("请记住：不接受二战",false),"explicit memory request works with automatic memory disabled");
  check(PromptPolicy.memoryNeeded("我喜欢先给答案，再给推导",true)&&!PromptPolicy.memoryNeeded("我喜欢先给答案，再给推导",false),"automatic memory respects the user's switch");
  check(PromptPolicy.memoryNeeded("我现在用什么手表",false),"saved-memory recall remains available with automatic writes disabled");
  check(!PromptPolicy.memoryNeeded("计算阻尼比",true),"routine math does not load memory write tools");
  check(has(Conversation.payload(chat("请记住：我是自动化学生"),new JSONObject()),"remember_memory"),"explicit remember exposes actual memory tool");
  int legacy=PromptPolicy.system().length()+AgentTools.definitions().toString().length();int lean=normal.getJSONArray("messages").getJSONObject(0).getString("content").length()+normal.getJSONArray("tools").toString().length();
  check(lean<legacy*.50,"ordinary request instruction and tool characters cut by more than half");
  System.out.println("REQUEST CHARACTERS "+legacy+" -> "+lean+" (not a tokenizer count)");
  TextReveal r=new TextReveal();r.begin("a","",0);String body="开头😀\n\n"+"连续回答内容。".repeat(1800);r.offer(body);String previous="";int frames=0;long now=0;
  while(r.pending()&&frames<500){r.advance(now+=32);String visible=r.visible();checkPrefix(visible,body,previous);previous=visible;frames++;}
  check(!r.pending()&&r.visible().equals(body),"burst response drains completely without lost or reordered characters");
  check(frames>10&&frames<160,"long burst appears progressively and catches up within five seconds");
  r.offer("替换后的回答");r.advance(now+=32);check("替换后的回答".startsWith(r.visible()),"tool repair replacing answer resets UI cursor safely");
  r.begin("b","已保存内容",now);check(r.visible().equals("已保存内容")&&!r.pending(),"reopening conversation does not replay saved text");
  r.offer("已保存内容，继续输出");r.advance(now+=32);check("已保存内容，继续输出".startsWith(r.visible()),"new streaming deltas append to visible saved prefix");
  System.out.println("EXPERIENCE TOTAL "+checks+" checks passed; burst frames="+frames);
 }
 private static void checkPrefix(String visible,String source,String previous){if(!source.startsWith(visible)||!visible.startsWith(previous))throw new AssertionError("display order");if(!visible.isEmpty()&&Character.isHighSurrogate(visible.charAt(visible.length()-1)))throw new AssertionError("split surrogate pair");}
}
