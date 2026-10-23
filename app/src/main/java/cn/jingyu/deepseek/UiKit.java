package cn.jingyu.deepseek;

import android.graphics.*;
import android.graphics.drawable.*;
import android.content.res.ColorStateList;

/** Small, local vector icons and touch feedback; no image or font downloads. */
public final class UiKit {
 public static Drawable surface(int fill,int stroke,float radius,int ripple){GradientDrawable shape=new GradientDrawable();shape.setColor(fill);shape.setCornerRadius(radius);if(stroke!=0)shape.setStroke(1,stroke);GradientDrawable mask=new GradientDrawable();mask.setColor(Color.WHITE);mask.setCornerRadius(radius);return new RippleDrawable(ColorStateList.valueOf(ripple),shape,mask);}
 public static final class Icon extends Drawable {
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final String name;private final int color,size;
  public Icon(String name,int color,int size){this.name=name;this.color=color;this.size=size;}
  private void line(Canvas c,float a,float b,float d,float e){c.drawLine(a,b,d,e,p);}private void path(Canvas c,float...xy){Path v=new Path();v.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)v.lineTo(xy[i],xy[i+1]);c.drawPath(v,p);}
  @Override public void draw(Canvas original){Canvas c=original;int save=c.save();Rect bounds=getBounds();c.translate(bounds.left,bounds.top);c.scale(bounds.width()/24f,bounds.height()/24f);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
   switch(name){
    case "back":path(c,15,5,8,12,15,19);break;
    case "profile":c.drawCircle(12,8,4,p);c.drawArc(5,13,19,26,190,160,false,p);break;
    case "memory":c.drawRoundRect(4,4,20,20,4,4,p);line(c,8,8,16,8);line(c,8,12,16,12);line(c,8,16,13,16);break;
    case "summary":path(c,5,4,19,4,19,20,5,20,5,4);line(c,8,8,16,8);line(c,8,12,16,12);line(c,8,16,12,16);break;
    case "menu":line(c,4,7,20,7);line(c,4,12,16,12);line(c,4,17,20,17);break;
    case "plus":line(c,12,5,12,19);line(c,5,12,19,12);break;
    case "send":line(c,12,19,12,5);path(c,6,11,12,5,18,11);break;
    case "stop":p.setStyle(Paint.Style.FILL);c.drawRoundRect(7,7,17,17,2,2,p);break;
    case "close":line(c,6,6,18,18);line(c,18,6,6,18);break;
    case "down":path(c,7,10,12,15,17,10);break;
    case "next":path(c,9,6,15,12,9,18);break;
    case "globe":c.drawCircle(12,12,9,p);c.drawOval(8,3,16,21,p);line(c,3,12,21,12);break;
    case "search":c.drawCircle(10.5f,10.5f,6,p);line(c,15,15,20,20);break;
    case "copy":c.drawRoundRect(8,8,20,21,2,2,p);path(c,15,4,5,4,4,5,4,15);break;
    case "retry":c.drawArc(4,4,20,20,45,290,false,p);path(c,4,4,4,10,10,10);break;
    case "more":p.setStyle(Paint.Style.FILL);for(int i=0;i<3;i++)c.drawCircle(5+7*i,12,1.35f,p);break;
    case "file":path(c,14,3,5,3,5,21,19,21,19,8,14,3,14,8,19,8);line(c,8,12,16,12);line(c,8,16,14,16);break;
    case "image":c.drawRoundRect(3,4,21,20,3,3,p);c.drawCircle(8,9,1.3f,p);path(c,4,18,10,12,14,16,17,13,21,17);break;
    case "chat":path(c,5,4,19,4,21,6,21,15,19,17,10,17,5,21,5,17,3,15,3,6,5,4);line(c,7,9,17,9);line(c,7,13,13,13);break;
    case "edit":path(c,5,16,16,5,20,9,9,20,4,21,5,16);line(c,13,8,17,12);break;
    case "export":path(c,5,13,5,20,19,20,19,13);line(c,12,16,12,3);path(c,7,8,12,3,17,8);break;
    case "import":path(c,5,15,5,21,19,21,19,15);line(c,12,3,12,16);path(c,7,11,12,16,17,11);break;
    case "shield":path(c,12,3,20,6,19,14,16,19,12,22,8,19,5,14,4,6,12,3);path(c,8,12,11,15,16,9);break;
    case "sun":c.drawCircle(12,12,4,p);for(int i=0;i<8;i++){double a=i*Math.PI/4;line(c,12+(float)Math.cos(a)*7,12+(float)Math.sin(a)*7,12+(float)Math.cos(a)*9,12+(float)Math.sin(a)*9);}break;
    case "spark":path(c,12,3,14.5f,9.5f,21,12,14.5f,14.5f,12,21,9.5f,14.5f,3,12,9.5f,9.5f,12,3);break;
    case "key":c.drawCircle(8,9,4,p);path(c,11,12,20,21,22,19,19,16,17,18);break;
    case "settings":c.drawCircle(12,12,3.2f,p);Path cog=new Path();for(int i=0;i<32;i++){double a=i*Math.PI/16;float radius=(i%4==0||i%4==3)?9:7.3f;float x=12+(float)Math.cos(a)*radius,y=12+(float)Math.sin(a)*radius;if(i==0)cog.moveTo(x,y);else cog.lineTo(x,y);}cog.close();c.drawPath(cog,p);break;
    case "wave":Path wave=new Path();wave.moveTo(3,10);wave.cubicTo(6,5,9,6,12,10);wave.cubicTo(15,14,18,15,21,10);c.drawPath(wave,p);wave.reset();wave.moveTo(3,16);wave.cubicTo(6,11,9,12,12,16);wave.cubicTo(15,20,18,21,21,16);c.drawPath(wave,p);break;
    default:c.drawCircle(12,12,8,p);
   }c.restoreToCount(save);
  }
  @Override public void setAlpha(int alpha){p.setAlpha(alpha);}@Override public void setColorFilter(ColorFilter filter){p.setColorFilter(filter);}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}@Override public int getIntrinsicWidth(){return size;}@Override public int getIntrinsicHeight(){return size;}
 }
}
