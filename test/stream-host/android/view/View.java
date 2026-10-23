package android.view;
public class View {
 public int width=400,height=600,top,intrinsicHeight=100,measures,lastWidthMode;
 public int getWidth(){return width;}public int getHeight(){return height;}public int getTop(){return top;}
 public int getMeasuredHeight(){return intrinsicHeight;}
 public void measure(int width,int height){measures++;lastWidthMode=width&0xc0000000;}
 public static final class MeasureSpec {public static final int EXACTLY=0x40000000,UNSPECIFIED=0;public static int makeMeasureSpec(int size,int mode){return size|mode;}}
}
