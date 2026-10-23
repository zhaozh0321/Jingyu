package cn.jingyu.deepseek;

import org.json.*;
import java.util.*;
import java.util.regex.*;

/** Routes explicit document requests to the installed native writers. */
public final class FileRequests {
 public static String infer(String text){
  String q=text.toLowerCase(Locale.ROOT).replaceAll("\\s+"," ");
  if(q.matches("(?s).*(为什么|为啥|怎么会|原理|区别|是否支持|能否支持).*")||q.matches("(?s).*(不要|不需要|不用|别)(?:再|给我|帮我|另行)?(?:生成|制作|导出|转换|创建|提供|做成)?(?:任何|一个|一份|为|成)?\\s*(word|docx|pdf|excel|xlsx|ppt|文件|文档).*"))return "";
  if(!Pattern.compile("生成|制作|做[一份个成]|导出|转换|转[为成]|整理|写[一份个]|给[我个份]|发[我个份]|保存|下载|create|generate|export|convert",Pattern.CASE_INSENSITIVE).matcher(q).find())return "";
  if(Pattern.compile("docx|word").matcher(q).find())return "docx";
  if(Pattern.compile("pdf").matcher(q).find())return "pdf";
  if(Pattern.compile("xlsx|excel").matcher(q).find())return "xlsx";
  if(Pattern.compile("pptx?|powerpoint|幻灯片|演示文稿").matcher(q).find())return "pptx";
  if(Pattern.compile("csv").matcher(q).find())return "csv";
  if(Pattern.compile("html").matcher(q).find())return "html";
  if(Pattern.compile("markdown|md").matcher(q).find())return "md";
  if(Pattern.compile("txt|纯文本").matcher(q).find())return "txt";
  if(q.matches("(?s).*(生成|制作|做一份|导出|给我)[^，。；;]{0,18}(文件|文档).*"))return "docx";
  return "";
 }
 public static JSONObject user(JSONObject chat){JSONArray all=chat.optJSONArray("messages");for(int i=all==null?-1:all.length()-1;i>=0;i--){JSONObject m=all.optJSONObject(i);if(m!=null&&m.optString("role").equals("user"))return m;}return new JSONObject();}
 public static String format(JSONObject chat){JSONObject user=user(chat);String selected=user.optString("preferredOutput");if(Arrays.asList(ArtifactFiles.FORMATS).contains(selected))return selected;String question=user.optString("text"),direct=infer(question);if(!direct.isEmpty())return direct;
  if(question.matches("(?is).*(不要|不需要|不用|别).{0,8}(文件|文档|word|pdf|excel).*"))return "";
  if(question.matches("(?is).*(记忆|记住|忘记|遗忘).*"))return "";
  if(!question.matches("(?is)\\s*(继续|调整|修改|改成|改为|增加|补充|删掉|删除|重做|重新|完善|还是|不对|太长|太短|标题|字号|行距|排版|表格|章节|换成|可以|好的|确认).*"))return "";
  JSONArray history=chat.optJSONArray("messages");boolean passed=false;for(int i=history==null?-1:history.length()-1;i>=0;i--){JSONObject m=history.optJSONObject(i);if(m==user){passed=true;continue;}if(!passed||m==null)continue;JSONArray files=m.optJSONArray("artifacts");if(files!=null&&files.length()>0){String format=files.optJSONObject(files.length()-1).optString("format");if(Arrays.asList(ArtifactFiles.FORMATS).contains(format))return format;}if(m.optString("role").equals("user")){String format=m.optString("preferredOutput");return Arrays.asList(ArtifactFiles.FORMATS).contains(format)?format:infer(m.optString("text"));}}
  return "";
 }
 public static boolean research(String text){return Pattern.compile("联网|检索|搜索|核实|最新|今天|实时|本周|近期|新闻|天气|价格|search|https?://",Pattern.CASE_INSENSITIVE).matcher(text).find();}
 public static boolean calculation(String text){return Pattern.compile("计算|统计|合计|平均|均值|标准差|图表|数据分析|calculate|chart",Pattern.CASE_INSENSITIVE).matcher(text).find();}
 public static boolean fileOnly(String text){return !Pattern.compile("(?:另外|同时|并且|并|再)(?:请|给我|帮我|在聊天中|在这里)?[^，。；]{0,8}(回答|解释|总结|说明|列出|分析)").matcher(text).find();}
 public static boolean refusal(String value){return Pattern.compile("(无法|不能|不支持|没法|没有.{0,8}(能力|工具|权限|环境)).{0,28}(生成|创建|提供|附加|发送|文件|附件|docx)|复制.{0,30}(word|html)|保存为.{0,20}html",Pattern.CASE_INSENSITIVE).matcher(value).find();}
 public static String document(String raw){
  Matcher html=Pattern.compile("(?is)```(?:html)?\\s*((?:<!doctype|<html)[\\s\\S]*?)```").matcher(raw);
  if(html.find())return android.text.Html.fromHtml(html.group(1),android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim();
  if(refusal(raw))return "";
  Matcher fenced=Pattern.compile("(?s)^\\s*```(?:markdown|md|text|txt)?\\s*\\n(.*?)\\n```\\s*$").matcher(raw);
  return fenced.matches()?fenced.group(1).trim():raw.trim();
 }
 public static JSONObject existing(JSONObject chat,String format)throws Exception {
  JSONObject user=user(chat);String question=user.optString("text");JSONArray files=user.optJSONArray("files");
  if(format.isEmpty()||question.length()>180||files!=null&&files.length()>0||!question.matches("(?s).*(上一[条次]|上面|上述|以上|刚才|这些内容|这[条份段个].*(回答|内容)|现有回答).*"))return null;
  JSONArray all=chat.optJSONArray("messages");boolean passed=false;
  for(int i=all.length()-1;i>=0;i--){JSONObject m=all.getJSONObject(i);if(m==user){passed=true;continue;}if(passed&&m.optString("role").equals("assistant")&&m.optString("status").equals("complete")){String body=document(m.optString("text"));if(body.isEmpty())return null;return new JSONObject().put("format",format).put("name",chat.optString("title","鲸语文档")).put("content",body);}}
  return null;
 }
}
