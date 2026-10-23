package cn.jingyu.deepseek;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

/** Shared, theme-aware dialogs for confirmations, menus and single-choice controls. */
final class JingyuDialog extends AlertDialog {
 private final Builder spec;
 private final java.util.Map<Integer,Button> buttons=new java.util.HashMap<>();
 private final int ink,muted,accent,card,soft,line;
 private int dp(float n){return (int)(n*getContext().getResources().getDisplayMetrics().density+.5f);}
 private JingyuDialog(Builder s){super(s.context,s.dark?R.style.JingyuDialogDark:R.style.JingyuDialogLight);spec=s;ink=s.dark?0xffe1ebe4:0xff223a31;muted=s.dark?0xffa4b6aa:0xff5f7265;accent=s.dark?0xff93cbb0:0xff276a52;card=s.dark?0xff1a2821:0xfff7f9f6;soft=s.dark?0xff263d31:0xffe8f0e8;line=s.dark?0xff344a3d:0xffdfe8de;setCancelable(s.cancelable);setCanceledOnTouchOutside(s.cancelable);}
 private LinearLayout column(){LinearLayout v=new LinearLayout(getContext());v.setOrientation(1);return v;}
 private TextView text(CharSequence value,int size,int color){TextView t=new TextView(getContext());t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setLineSpacing(dp(4),1);return t;}
 @Override protected void onCreate(Bundle saved){
  super.onCreate(saved);Window window=getWindow();
  LinearLayout root=column();root.setPadding(dp(24),dp(24),dp(24),dp(20));root.setBackground(UiKit.surface(card,line,dp(26),0x12276a52));
  if(spec.title!=null){TextView heading=text(spec.title,20,ink);heading.setTypeface(Typeface.create("sans-serif-medium",0));root.addView(heading,new LinearLayout.LayoutParams(-1,-2));}
  LinearLayout content=column();
  if(spec.message!=null){TextView message=text(spec.message,15,muted);message.setTextIsSelectable(true);content.addView(message,new LinearLayout.LayoutParams(-1,-2));}
  if(spec.view!=null){if(spec.view.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)spec.view.getParent()).removeView(spec.view);content.addView(spec.view,new LinearLayout.LayoutParams(-1,-2));}
  if(spec.items!=null){for(int i=0;i<spec.items.length;i++){final int index=i;LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(14),dp(6),dp(12),dp(6));row.setMinimumHeight(dp(52));boolean selected=spec.selected==i;row.setBackground(UiKit.surface(selected?soft:Color.TRANSPARENT,selected?line:0,dp(14),0x22276a52));TextView label=text(spec.items[i],15,ink);row.addView(label,new LinearLayout.LayoutParams(0,-2,1));if(spec.single){RadioButton radio=new RadioButton(getContext());radio.setButtonTintList(android.content.res.ColorStateList.valueOf(accent));radio.setChecked(selected);radio.setClickable(false);radio.setFocusable(false);radio.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);row.addView(radio,new LinearLayout.LayoutParams(dp(32),dp(40)));}row.setFocusable(true);row.setContentDescription(spec.items[i]+(selected?"，已选中":""));row.setOnClickListener(v->{if(spec.itemClick!=null)spec.itemClick.onClick(this,index);if(!spec.single)dismiss();});LinearLayout.LayoutParams item=new LinearLayout.LayoutParams(-1,-2);item.bottomMargin=dp(4);content.addView(row,item);}}
  ScrollView scroll=new ScrollView(getContext());scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);scroll.addView(content);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,0,1);cp.topMargin=spec.title==null?0:dp(16);root.addView(scroll,cp);
  if(spec.positive!=null||spec.negative!=null||spec.neutral!=null){LinearLayout actions=new LinearLayout(getContext());actions.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);addAction(actions,BUTTON_NEUTRAL,spec.neutral,spec.neutralClick,false);addAction(actions,BUTTON_NEGATIVE,spec.negative,spec.negativeClick,false);addAction(actions,BUTTON_POSITIVE,spec.positive,spec.positiveClick,true);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.topMargin=dp(20);root.addView(actions,ap);}
  setContentView(root);window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams attrs=window.getAttributes();attrs.dimAmount=.32f;window.setAttributes(attrs);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
  int width=Math.min(getContext().getResources().getDisplayMetrics().widthPixels-dp(40),dp(420));root.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));int maxHeight=(int)(getContext().getResources().getDisplayMetrics().heightPixels*.80f);int desired=Math.min(maxHeight,root.getMeasuredHeight()+content.getMeasuredHeight());
  // A weighted scroll area expands to its content, bounded by the available screen height.
  cp.height=-2;cp.weight=0;root.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));desired=Math.min(maxHeight,root.getMeasuredHeight());if(root.getMeasuredHeight()>maxHeight){cp.height=0;cp.weight=1;}
  window.setLayout(width,desired);window.setGravity(Gravity.CENTER);
 }
 private void addAction(LinearLayout row,int which,CharSequence label,DialogInterface.OnClickListener listener,boolean primary){if(label==null)return;Button b=new Button(getContext());b.setText(label);b.setAllCaps(false);b.setTextSize(14);b.setTypeface(Typeface.create("sans-serif-medium",0));boolean destructive=primary&&label.toString().matches(".*(删除|清空|移除|遗忘).*");int fill=destructive?(spec.dark?0xffa65343:0xffb35e49):0xff276a52;b.setTextColor(primary?Color.WHITE:accent);b.setPadding(dp(14),0,dp(14),0);b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(dp(46));b.setMinimumHeight(dp(46));b.setStateListAnimator(null);b.setBackground(UiKit.surface(primary?fill:soft,0,dp(14),0x22ffffff));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,-2,1);if(row.getChildCount()>0)bp.leftMargin=dp(8);row.addView(b,bp);buttons.put(which,b);b.setOnClickListener(v->{if(listener!=null)listener.onClick(this,which);dismiss();});}
 @Override public Button getButton(int which){return buttons.get(which);}
 static final class Builder extends AlertDialog.Builder {
  final Context context;final boolean dark;CharSequence title,message,positive,negative,neutral;View view;CharSequence[] items;boolean single,cancelable=true;int selected=-1;DialogInterface.OnClickListener positiveClick,negativeClick,neutralClick,itemClick;
  Builder(Context context,boolean dark){super(context,dark?R.style.JingyuDialogDark:R.style.JingyuDialogLight);this.context=context;this.dark=dark;}
  @Override public Builder setTitle(CharSequence s){title=s;return this;}
  @Override public Builder setMessage(CharSequence s){message=s;return this;}
  @Override public Builder setView(View v){view=v;return this;}
  @Override public Builder setCancelable(boolean value){cancelable=value;return this;}
  @Override public Builder setPositiveButton(CharSequence s,DialogInterface.OnClickListener c){positive=s;positiveClick=c;return this;}
  @Override public Builder setNegativeButton(CharSequence s,DialogInterface.OnClickListener c){negative=s;negativeClick=c;return this;}
  @Override public Builder setNeutralButton(CharSequence s,DialogInterface.OnClickListener c){neutral=s;neutralClick=c;return this;}
  @Override public Builder setItems(CharSequence[] s,DialogInterface.OnClickListener c){items=s;itemClick=c;return this;}
  @Override public Builder setSingleChoiceItems(CharSequence[] s,int checked,DialogInterface.OnClickListener c){items=s;selected=checked;single=true;itemClick=c;return this;}
  @Override public AlertDialog create(){return new JingyuDialog(this);}
  @Override public AlertDialog show(){AlertDialog d=create();d.show();return d;}
 }
}
