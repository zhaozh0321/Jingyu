package android.view;
import java.util.*;
/** Deterministic display clock for exercising production coalescer behavior on the host. */
public final class Choreographer {
 public interface FrameCallback {void doFrame(long nanos);}
 private static final Choreographer INSTANCE=new Choreographer();private final List<FrameCallback> pending=new ArrayList<>();
 public static Choreographer getInstance(){return INSTANCE;}
 public void postFrameCallback(FrameCallback c){pending.add(c);}
 public void removeFrameCallback(FrameCallback c){pending.remove(c);}
 public int pendingCount(){return pending.size();}
 public void frame(){List<FrameCallback> frame=new ArrayList<>(pending);pending.clear();for(FrameCallback c:frame)c.doFrame(16000000L);}
}
