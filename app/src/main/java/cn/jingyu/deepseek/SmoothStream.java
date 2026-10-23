package cn.jingyu.deepseek;

import android.view.*;
import android.widget.ListView;

/** Adapted from the supplied SmoothStream patch: UI-thread frame merging and single-pass bottom anchoring. */
final class SmoothStream {
 private SmoothStream(){}
 static final class FrameCoalescer {
  private final Runnable task;private final Choreographer clock;private boolean scheduled;
  private final Choreographer.FrameCallback callback;
  FrameCoalescer(Runnable task){this.task=task;clock=Choreographer.getInstance();callback=frame->{scheduled=false;this.task.run();};}
  void request(){if(!scheduled){scheduled=true;clock.postFrameCallback(callback);}}
  void cancel(){if(scheduled){scheduled=false;clock.removeFrameCallback(callback);}}
 }
 static View visibleItem(ListView list,int position){if(list==null)return null;int index=position-list.getFirstVisiblePosition();return index>=0&&index<list.getChildCount()?list.getChildAt(index):null;}
 static int bottomOffset(int viewport,int height){return Math.min(0,viewport-height);}
 static void stick(ListView list,int position,View item){if(list==null||item==null)return;int viewport=list.getHeight()-list.getPaddingTop()-list.getPaddingBottom();int width=list.getWidth()-list.getPaddingLeft()-list.getPaddingRight();if(viewport<=0||width<=0)return;
  item.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
  int offset=bottomOffset(viewport,item.getMeasuredHeight());
  // Skip a redundant selection when this row is already at the computed position.
  if(visibleItem(list,position)!=item||Math.abs(item.getTop()-list.getPaddingTop()-offset)>1)list.setSelectionFromTop(position,offset);
 }
}
