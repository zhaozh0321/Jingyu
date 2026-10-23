package cn.jingyu.deepseek;

import java.io.*;import java.nio.file.*;import java.security.*;import java.util.*;import java.util.zip.*;import org.json.*;

/** Host regression checks on real production writers and request builders, with upstream org.json. */
public final class HostQualityTest {
 private static int checks;
 private static void check(boolean value,String name){if(!value)throw new AssertionError(name);checks++;System.out.println("PASS "+name);}
 private interface Action{void run()throws Exception;}
 private static void rejects(Action a,String label)throws Exception{try{a.run();throw new AssertionError(label);}catch(IOException expected){check(true,label);}}
 private static JSONObject user(String text)throws Exception{return new JSONObject().put("id","u").put("role","user").put("text",text).put("status","complete");}
 private static JSONObject chat(String text)throws Exception{return new JSONObject().put("title","文档测试").put("messages",new JSONArray().put(user(text)));}
 private static String xml(File file,String entry)throws Exception{try(ZipFile z=new ZipFile(file);InputStream in=z.getInputStream(z.getEntry(entry))){return new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}}
 public static void main(String[] argv)throws Exception{
  checks=0;
  File out=new File(argv[0]);out.mkdirs();
  check(FileRequests.infer("帮我做一份控制系统word文档").equals("docx"),"Chinese adjacent Word request is routed");
  check(FileRequests.infer("把上述内容导出为PDF").equals("pdf"),"PDF conversion request is routed");
  check(FileRequests.infer("生成Excel表格，保留单位").equals("xlsx"),"Excel generation is routed");
  check(FileRequests.infer("为什么你不能生成word文件").isEmpty(),"capability complaint does not force a new file");
  check(FileRequests.infer("解释word和pdf的区别").isEmpty(),"comparison question does not generate a file");
  check(FileRequests.infer("不要生成Word，直接回答").isEmpty(),"negative output constraint is respected");
  check(FileRequests.infer("普通问题：1+1是多少").isEmpty(),"normal chat has no document routing");
  check(FileRequests.document("我无法直接生成docx，可以复制HTML另存").isEmpty(),"file refusal cannot be packaged as document content");
  check(FileRequests.infer("不要改写正文导出word").equals("docx"),"content-preservation constraint does not suppress file export");
  rejects(()->ArtifactSkills.checkRequest(new JSONObject().put("format","docx").put("title","错误标题").put("content","body"),"生成Word，标题为“用户指定的标题”"),"explicit requested title is enforced before file creation");
  rejects(()->ArtifactSkills.checkRequest(new JSONObject().put("format","xlsx").put("rows",new JSONArray().put(new JSONArray().put("表头")).put(new JSONArray().put("数据"))),"生成3条记录的Excel"),"explicit data-record count is enforced excluding header");
  rejects(()->ArtifactSkills.checkRequest(new JSONObject().put("format","docx").put("content","## 唯一章节\n内容"),"分为3个部分生成Word"),"explicit document section count is enforced");
  JSONObject settings=new JSONObject().put("chats",new JSONArray()).put("streaming",false);Conversation.recover(settings);check(settings.optBoolean("streaming"),"2.0 migrates old non-streaming default once");settings.put("streaming",false);Conversation.recover(settings);check(!settings.optBoolean("streaming"),"later explicit streaming choice is preserved");
  JSONObject doc=Conversation.payload(chat("请生成一份学习讲义Word文档"),new JSONObject());
  check(doc.optBoolean("stream"),"new request defaults to progressive output");
  check(doc.getJSONObject("tool_choice").getJSONObject("function").getString("name").equals("create_file"),"quick file request selects installed file tool");
  check(doc.toString().contains("artifact_skill")&&doc.toString().contains("原样导出"),"file requests load task-specific quality and fidelity guide");
  check(Conversation.payload(chat("请生成一份Word文档"),new JSONObject().put("effort","high")).optString("tool_choice").equals("auto"),"thinking mode uses compatible automatic tool selection");
  check(Conversation.payload(chat("联网核实最新消息并生成Word"),new JSONObject()).optString("tool_choice").equals("auto"),"research task can search before file creation");
  check(!Conversation.payload(chat("你好"),new JSONObject()).toString().contains("<artifact_skill>"),"ordinary chat avoids loading document skill tokens");
  JSONArray history=new JSONArray().put(user("第一问")).put(new JSONObject().put("role","assistant").put("status","complete").put("text","上一条回答保留完整。\n\n数值是12.50，单位为rad/s。").put("protocol",new JSONArray().put(new JSONObject().put("role","assistant").put("content","上一条回答保留完整。\n\n数值是12.50，单位为rad/s。").put("reasoning_content","unnecessary-old-reasoning")))).put(user("将上一条回答导出为Word，不要改写"));JSONObject conversation=new JSONObject().put("title","原样导出").put("messages",history);
  check(!Conversation.payload(conversation,new JSONObject()).toString().contains("unnecessary-old-reasoning"),"previous-turn reasoning is excluded from subsequent payload");
  JSONObject local=FileRequests.existing(conversation,"docx");check(local!=null&&local.getString("content").equals(history.getJSONObject(1).getString("text")),"existing answer export preserves exact wording and digits without API");
  JSONObject definitions=AgentTools.definitions().getJSONObject(2).getJSONObject("function");String formats=definitions.getJSONObject("parameters").getJSONObject("properties").getJSONObject("format").toString();check(!formats.contains("pptx")&&!formats.contains("html")&&formats.contains("docx"),"unpolished presentation and HTML tools are absent");
  rejects(()->ArtifactSkills.validate(new JSONObject().put("format","pptx").put("content","text")),"retired format is rejected instead of silently substituted");
  rejects(()->ArtifactSkills.validate(new JSONObject().put("format","docx").put("content","")),"blank document cannot produce a success card");
  rejects(()->ArtifactSkills.validate(new JSONObject().put("format","xlsx").put("rows",new JSONArray().put(new JSONArray().put("a").put("b")).put(new JSONArray().put(1)))),"ragged spreadsheet data is rejected");
  rejects(()->ArtifactSkills.validate(new JSONObject().put("format","xlsx").put("content","这是一整篇文章，没有数据表。")),"article cannot be disguised as a one-cell Excel sheet");
  String math=OfficeMath.xml("G(s)=\\frac{100}{s^2+14s+100}, \\omega_n=10, \\zeta=0.7");
  check(math.contains("<m:f>")&&math.contains("<m:sSup>")&&math.contains("<m:sSub>"),"formula becomes editable fractions superscripts and subscripts");
  rejects(()->OfficeMath.xml("\\frac{1}{s"),"malformed formula produces a clear error");
  rejects(()->OfficeMath.xml("\\unsupported{x}"),"unsupported Word math is not shown as fake rendered LaTeX");
  String content="# 二阶系统响应分析\n\n从系统参数出发，判断稳定性、阻尼类型与阶跃响应。\n\n## 1. 已知条件\n\n系统的标准传递函数为：\n\n$$G(s)=\\frac{100}{s^2+14s+100}$$\n\n| 参数 | 数值 | 说明 |\n| --- | ---: | --- |\n| 自然频率 | 10 rad/s | 保留单位 |\n| 阻尼比 | 0.7 | 无量纲 |\n\n## 2. 推导与判断\n\n- 比较分母系数，得到 $\\omega_n=10$。\n- 由 $2\\zeta\\omega_n=14$，得到 $\\zeta=0.7$。\n- 系统属于**欠阻尼**系统，极点实部为负。\n\n## 3. 结论\n\n系统稳定。分析时需区分**自然频率**与*阻尼振荡频率*，不能省略单位。\n\n参考：[控制系统公开资料](https://example.org/control)\n";
  JSONObject spec=new JSONObject().put("format","docx").put("name","二阶系统响应分析").put("title","二阶系统响应分析").put("style","study").put("content",content);ArtifactSkills.validate(spec);File word=new File(out,"study.docx");try(OutputStream stream=new FileOutputStream(word)){DocxWriter.write(stream,spec);}
  String body=xml(word,"word/document.xml");check(body.contains("<w:tblHeader/>")&&body.contains("<w:tblGrid>"),"Word uses real sized tables and repeating headers");
  check(body.contains("<m:oMath>")&&!body.contains("\\frac"),"Word embeds actual math rather than visible TeX commands");
  check(body.contains("<w:numPr>")&&body.contains("<w:i/>"),"Word has real lists and styled emphasis");
  check(xml(word,"word/_rels/document.xml.rels").contains("TargetMode=\"External\""),"Word hyperlinks use actual relationships");
  check(xml(word,"word/styles.xml").contains("outlineLvl")&&!xml(word,"word/styles.xml").contains("SourceFile"),"Word headings use semantic document styles");
  check(body.contains("10 rad/s")&&body.contains("0.7"),"numbers and units survive the production document writer");
  check(!body.contains("鲸语 · 文档"),"document has no unsolicited app branding");
  for(String style:new String[]{"report","formal","plain"}){JSONObject sample=new JSONObject(spec.toString()).put("style",style);try(OutputStream stream=new FileOutputStream(new File(out,style+".docx"))){DocxWriter.write(stream,sample);}}
  JSONObject sheet=new JSONObject().put("format","xlsx").put("rows",new JSONArray().put(new JSONArray().put("参数").put("数值").put("单位")).put(new JSONArray().put("自然频率").put(10.5).put("rad/s")).put(new JSONArray().put("原样文本").put("=1+2").put("不执行")));ArtifactSkills.validate(sheet);File excel=new File(out,"data.xlsx");try(OutputStream stream=new FileOutputStream(excel)){ArtifactFiles.xlsx(stream,sheet);}
  String sx=xml(excel,"xl/worksheets/sheet1.xml");check(sx.contains("<v>10.5</v>")&&sx.contains("<t xml:space=\"preserve\">=1+2</t>")&&!sx.contains("<f>"),"Excel preserves numeric cells and keeps formula-looking strings inert");
  check(sx.contains("autoFilter")&&sx.contains("customWidth")&&sx.contains("state=\"frozen\""),"Excel includes filter sized columns and frozen header");
  check(ArtifactFiles.csv(new JSONArray().put(new JSONArray().put("=1+2").put("-12.5"))).contains("'=1+2"),"CSV protects formula-like text while preserving negative numbers");
  byte[] cert=java.nio.file.Files.readAllBytes(java.nio.file.Path.of(argv[1]));check(AppIntegrity.trusted(cert),"configured public signing certificate passes integrity check");check(!AppIntegrity.trusted("re-signed-package".getBytes()),"unrelated signing certificate is rejected");
  System.out.println("TOTAL "+checks+" checks passed");
 }
}
