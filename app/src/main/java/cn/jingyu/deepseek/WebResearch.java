package cn.jingyu.deepseek;

import org.json.*;import java.util.*;import java.util.regex.*;import java.net.*;

/** Official search/fetch wire schema. Web material is bounded, attributed, and untrusted. */
public final class WebResearch {
 public static String clip(String text,int max){if(text.codePointCount(0,text.length())<=max)return text;return text.substring(0,text.offsetByCodePoints(0,max));}
 public static String safeURL(String url){try{URI uri=new URI(url);String scheme=uri.getScheme();return ("http".equalsIgnoreCase(scheme)||"https".equalsIgnoreCase(scheme))&&uri.getHost()!=null&&uri.getUserInfo()==null&&url.length()<=8192?uri.toASCIIString():"";}catch(Exception e){return "";}}
 public static JSONArray sources(JSONObject response,String fetchedURL)throws JSONException {JSONArray sources=new JSONArray();JSONArray results=response.optJSONArray("results");if(!fetchedURL.isEmpty()){if(!response.optString("content").trim().isEmpty())sources.put(new JSONObject().put("title",response.optString("title",fetchedURL)).put("url",fetchedURL).put("content",clip(response.optString("content"),16000)));return sources;}Set<String> seen=new HashSet<>();for(int i=0;results!=null&&i<results.length()&&sources.length()<6;i++){JSONObject r=results.optJSONObject(i);if(r==null)continue;String url=safeURL(r.optString("url"));if(url.isEmpty()||!seen.add(url))continue;sources.put(new JSONObject().put("title",clip(r.optString("title",url),180)).put("url",url).put("date",r.optString("date","")).put("summary",clip(r.optString("summary",r.optString("snippet")),1000)).put("content",clip(r.optString("content"),5000)));}return sources;}
}
