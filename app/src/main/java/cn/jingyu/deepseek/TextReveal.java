package cn.jingyu.deepseek;

/** UI-only cursor. Network, persistence and model context always retain the full answer. */
final class TextReveal {
 private String id="",source="";private int cursor;private long last;
 void begin(String value,String text,long now){id=value;source=text;cursor=text.length();last=now;}
 boolean matches(String value){return id.equals(value);}
 void offer(String text){if(cursor>text.length()||!text.regionMatches(0,source,0,Math.min(cursor,source.length())))cursor=0;source=text;}
 void finish(){cursor=source.length();}
 boolean pending(){return cursor<source.length();}
 String visible(){return source.substring(0,cursor);}
 void advance(long now){long elapsed=Math.max(1,Math.min(64,now-last));last=now;int remaining=source.length()-cursor;if(remaining<=0)return;int step=Math.max(3,(int)Math.ceil(elapsed*Math.max(.22,remaining/900.0)));int next=Math.min(source.length(),cursor+Math.min(step,768));if(next<source.length()&&next>0&&Character.isHighSurrogate(source.charAt(next-1))&&Character.isLowSurrogate(source.charAt(next)))next++;cursor=next;}
}
