package cn.jingyu.deepseek;

import org.json.*;
import java.util.*;

/** Small task-specific playbooks, loaded only for actual artifact requests. No extra API round. */
public final class ArtifactSkills {
 public static String guide(String format){
  String common="<artifact_skill>本次只交付可用的成品文件。先在内部核对本次用户的主题、用途、受众、章节或条目数量、长度、指定格式、单位、是否保留原文、样式与禁用事项，再一次性填写完整create_file参数。用户要求原样导出时禁止重新概括和改写。缺少决定性数据时只询问该数据，不编造数字、经历、引用、作者或日期。内容不能包含‘以下是文件内容’、使用教程、制作说明、占位符或虚假下载链接。title为简短内容标题，name为有意义的文件名，二者不能直接使用整句用户指令。必要的限制在聊天中说明，不塞入成品。skills只是写作与排版规范，不提供额外模型、浏览器或代码执行能力。";
  if(format.equals("xlsx")||format.equals("csv"))return common+"数据表技能：rows提供矩形二维数据，第一行为列名与单位，数字用JSON数值；编号、前导零用字符串。XLSX可用sheets数组创建1至12张工作表，每项含name、rows、可选columnFormats和chart；CSV仅单表rows。columnFormats逐列选择auto/text/number/integer/percent，percent数值以0.25表示25%。chart仅在用户需要时添加，type为bar或line，categoryColumn/valueColumn从0计数，仅绘制已有1至60条数值。图表原生可编辑，引用工作表真实数据。不得编造数据，不把文章塞进单元格，不写公式字符串冒充计算结果；先用calculate核算合计、均值或表达式再填数值。宏和Excel公式不执行。</artifact_skill>";
  if(format.equals("md")||format.equals("txt"))return common+"文本技能：保持内容完整与编码正确。Markdown使用标准标题、列表、表格和围栏；TXT尽量采用可直接阅读的纯文本。不添加不相关装饰。</artifact_skill>";
  return common+"文档技能：content使用标准Markdown：一个一级标题，二三级标题表达结构；自然段、列表、至多8列的清晰表格。title与一级标题一致。study用于学习讲义/解题，report用于摘要/报告，formal用于申请/正式材料，plain用于低装饰文字。style只选择一个模板，不用emoji、满篇加粗、分隔线和无意义封面。学习解题保留题号、已知条件、关键推导、下标、单位与最终结论；报告先结论后依据，避免空洞套话。数学用标准LaTeX的$...$行内式与独立行$$...$$，不用代码围栏包公式。layout支持orientation=portrait/landscape、fontSize=10至14、subtitle、pageNumbers、toc；toc生成章节目录链接，PDF目录为文字导航，不含自动页码。仅长文且用户需要时加目录，不随意加封面。需要明确分页时单独一行写<!-- pagebreak -->。横向页面适合宽表，表格保留左右冒号对齐标记。公式、代码、引用块、分级列表交给本地排版。正文保留原始题号，连续编号不自行重排。图片、复杂多栏、流程图、精确页数与品牌版式不在本版生成能力内，不能承诺实现。要求精确数据表时建议XLSX；要求精细视觉演示文稿时直说本版不提供PPT生成。交付前内部核对条目数、数据、引用、指定内容与格式，避免反复重写或额外联网。</artifact_skill>";
 }
 public static void validate(JSONObject spec)throws Exception {
  String format=spec.optString("format");if(!Arrays.asList(ArtifactFiles.FORMATS).contains(format))throw new java.io.IOException("2.0仅提供Word、PDF、Excel及文本/CSV导出；不再生成简单拼字的PPT或HTML。请选择支持的格式。");
  if(format.equals("xlsx")){SpreadsheetWriter.validate(spec);}
  else if(format.equals("csv")){if(spec.optJSONArray("sheets")!=null)throw new java.io.IOException("CSV只支持单表，请使用rows或改为XLSX。");JSONArray rows=ArtifactFiles.rows(spec);SpreadsheetWriter.validateRows(rows);spec.put("rows",rows);}
  else {if(spec.optString("content").trim().isEmpty())throw new java.io.IOException("缺少完整正文，文件没有创建。");if(format.equals("docx")||format.equals("pdf"))new DocumentLayout(spec);}
  JSONObject layout=spec.optJSONObject("layout");if(layout!=null){String orientation=layout.optString("orientation","portrait");if(!orientation.equals("portrait")&&!orientation.equals("landscape"))throw new java.io.IOException("页面方向必须是portrait或landscape。");double size=layout.optDouble("fontSize",11);if(!Double.isFinite(size)||size<10||size>14)throw new java.io.IOException("正文字号应为10至14磅。");if(layout.optString("subtitle").length()>180)throw new java.io.IOException("副标题最多180字符。");}
  if(spec.optString("content").matches("(?s).*\\[(?:此处填写|待补充正文|插入完整内容)\\].*"))throw new java.io.IOException("正文含占位符，请补充完整内容后再创建。");
 }
 public static String style(JSONObject spec){String value=spec.optString("style","report");return Arrays.asList("report","study","formal","plain").contains(value)?value:"report";}
 public static void checkRequest(JSONObject spec,String question)throws Exception {
  java.util.regex.Matcher title=java.util.regex.Pattern.compile("标题(?:为|是|叫|用)[：:\\s]*[“\"《]([^”\"》]{1,100})[”\"》]").matcher(question);
  if(title.find()&&!title.group(1).equals(title(spec)))throw new java.io.IOException("用户指定标题为“"+title.group(1)+"”，请完整保留该标题。");
  java.util.regex.Matcher count=java.util.regex.Pattern.compile("(?:共|一共|包含|生成|列出|整理|给我|分为)?\\s*([0-9]{1,3})\\s*(条记录|条数据|个部分|个章节)").matcher(question);
  if(count.find()){int expected=Integer.parseInt(count.group(1));String unit=count.group(2);if(expected<1)return;
   if((unit.equals("条记录")||unit.equals("条数据"))&&(spec.optString("format").equals("xlsx")||spec.optString("format").equals("csv"))){JSONArray data=spec.optJSONArray("rows");if(data==null&&spec.optJSONArray("sheets")!=null){JSONArray all=spec.getJSONArray("sheets");if(all.length()==1)data=all.getJSONObject(0).optJSONArray("rows");else throw new java.io.IOException("明确的条记录数量约束请使用单张数据表。");}if(data==null)data=ArtifactFiles.rows(spec);if(data.length()-1!=expected)throw new java.io.IOException("用户要求"+expected+unit+"（不含表头），当前有"+Math.max(0,data.length()-1)+"条，请补齐或删除多余数据。");}
   if((unit.equals("个部分")||unit.equals("个章节"))&&(spec.optString("format").equals("docx")||spec.optString("format").equals("pdf"))){java.util.regex.Matcher headings=java.util.regex.Pattern.compile("(?m)^## +").matcher(spec.optString("content"));int n=0;while(headings.find())n++;if(n!=expected)throw new java.io.IOException("用户要求"+expected+unit+"，请用且仅用"+expected+"个二级标题组织正文。");}
  }
 }
 public static String accent(JSONObject spec){String s=style(spec);return s.equals("study")?"276A52":s.equals("formal")?"1F2937":s.equals("plain")?"374151":"285568";}
 public static String title(JSONObject spec){String title=spec.optString("title").trim();if(title.isEmpty()){java.util.regex.Matcher h=java.util.regex.Pattern.compile("(?m)^# +(.+)$").matcher(spec.optString("content"));if(h.find())title=h.group(1).trim();}if(title.isEmpty())title=spec.optString("name","文档").replaceFirst("(?i)\\.(docx|pdf)$","");return title.length()>100?title.substring(0,100):title;}
}
