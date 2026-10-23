package cn.jingyu.deepseek;
import android.app.Application;
/** Owns the active request across Activity recreation, with no Activity reference. */
public final class JingyuApp extends Application {
 private ChatSession session;
 public synchronized ChatSession session(){if(session==null)session=new ChatSession(this);return session;}
}
