package cn.jingyu.deepseek;
import net.objecthunter.exp4j.ExpressionBuilder;import net.objecthunter.exp4j.function.Function;import java.io.*;import java.util.*;import org.json.*;

/** Bounded arithmetic parser and statistics. No eval, scripts, file or network access. */
final class LocalCalculator {
 static JSONObject calculate(JSONObject input)throws Exception {
  String expression=input.optString("expression").trim();JSONArray values=input.optJSONArray("values");JSONObject result=new JSONObject();
  if(!expression.isEmpty()){if(expression.length()>2000)throw new IOException("计算表达式最多2000字符。");try{double value=new ExpressionBuilder(expression).implicitMultiplication(false).function(new Function("ln",1){public double apply(double... x){return Math.log(x[0]);}}).build().evaluate();finite(value);result.put("value",value).put("expression",expression).put("engine","exp4j").put("precision","IEEE 754 double；三角函数采用弧度；ln为自然对数，log为自然对数，log10为常用对数");}catch(RuntimeException e){throw new IOException("表达式计算失败："+e.getMessage());}}
  if(values!=null){if(values.length()==0||values.length()>10000)throw new IOException("统计需要1至10000个数值。");double[] data=new double[values.length()];double sum=0,correction=0,mean=0,m2=0;for(int i=0;i<data.length;i++){Object o=values.opt(i);if(!(o instanceof Number))throw new IOException("统计数组仅接受JSON数字。");double x=((Number)o).doubleValue();finite(x);data[i]=x;double y=x-correction,t=sum+y;correction=(t-sum)-y;sum=t;double delta=x-mean;mean+=delta/(i+1);m2+=delta*(x-mean);}Arrays.sort(data);finite(sum);finite(mean);finite(m2);double median=data.length%2==1?data[data.length/2]:data[data.length/2-1]/2+data[data.length/2]/2;result.put("count",data.length).put("sum",sum).put("mean",mean).put("median",median).put("min",data[0]).put("max",data[data.length-1]).put("populationStdDev",Math.sqrt(Math.max(0,m2/data.length)));if(data.length>1)result.put("sampleStdDev",Math.sqrt(Math.max(0,m2/(data.length-1))));}
  if(result.length()==0)throw new IOException("请提供expression表达式或values数值数组。");return result;
 }
 private static void finite(double x)throws IOException{if(!Double.isFinite(x))throw new IOException("计算结果无定义或超出数值范围，请检查除数、开方和对数的定义域。");}
}
