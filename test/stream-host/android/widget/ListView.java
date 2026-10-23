package android.widget;
import android.view.View;
public class ListView extends View {
 public int first,paddingTop=12,paddingBottom=20,position=-1,offset,selections;public View child;
 public int getPaddingTop(){return paddingTop;}public int getPaddingBottom(){return paddingBottom;}public int getPaddingLeft(){return 0;}public int getPaddingRight(){return 0;}
 public int getFirstVisiblePosition(){return first;}public int getChildCount(){return child==null?0:1;}public View getChildAt(int i){return i==0?child:null;}
 public void setSelectionFromTop(int p,int y){position=p;offset=y;selections++;if(child!=null&&first==p)child.top=paddingTop+y;}
}
