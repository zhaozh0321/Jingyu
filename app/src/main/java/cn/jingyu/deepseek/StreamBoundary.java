package cn.jingyu.deepseek;

import java.util.*;
import java.util.regex.*;

/** Raw-source offsets only. Completed blocks can be frozen without parsing an unfinished fence or formula. */
final class StreamBoundary {
 private static final Pattern FENCE=Pattern.compile("^ {0,3}(`{3,}|~{3,})(.*)$");
 private String previous="";private int lineStart,searchFrom,stable,fenceLength,inlineTicks;private char fence;private boolean dollarMath,bracketMath;
 void reset(){previous="";lineStart=searchFrom=stable=fenceLength=inlineTicks=0;fence=0;dollarMath=bracketMath=false;}
 boolean accepts(String raw){return raw.length()>=previous.length()&&raw.startsWith(previous);}
 List<Integer> append(String raw){if(!accepts(raw))throw new IllegalArgumentException("Source replaced; reset stream blocks first");List<Integer> ends=new ArrayList<>();int end;
  while((end=raw.indexOf('\n',searchFrom))>=0){String line=raw.substring(lineStart,end);if(line.endsWith("\r"))line=line.substring(0,line.length()-1);Matcher m=FENCE.matcher(line);
   if(fence!=0){if(m.matches()&&m.group(1).charAt(0)==fence&&m.group(1).length()>=fenceLength&&m.group(2).trim().isEmpty()){fence=0;fenceLength=0;}}
   else if(!dollarMath&&!bracketMath&&inlineTicks==0&&m.matches()&&(m.group(1).charAt(0)!='`'||m.group(2).indexOf('`')<0)){fence=m.group(1).charAt(0);fenceLength=m.group(1).length();}
   else{for(int i=0;i<line.length();i++){char c=line.charAt(i);if(c=='`'&&!dollarMath&&!bracketMath){int ticks=1;while(i+1<line.length()&&line.charAt(i+1)=='`'){ticks++;i++;}if(inlineTicks==0)inlineTicks=ticks;else if(inlineTicks==ticks)inlineTicks=0;continue;}if(inlineTicks!=0)continue;if(c=='\\'){if(i+1<line.length()){char n=line.charAt(++i);if(n=='['&&!dollarMath)bracketMath=true;else if(n==']'&&bracketMath)bracketMath=false;}continue;}if(!bracketMath&&c=='$'&&i+1<line.length()&&line.charAt(i+1)=='$'){dollarMath=!dollarMath;i++;}}}
   if(line.trim().isEmpty()&&fence==0&&!dollarMath&&!bracketMath&&inlineTicks==0){stable=end+1;ends.add(stable);}lineStart=searchFrom=end+1;
  }
  searchFrom=raw.length();previous=raw;return ends;
 }
 int stable(){return stable;}
}
