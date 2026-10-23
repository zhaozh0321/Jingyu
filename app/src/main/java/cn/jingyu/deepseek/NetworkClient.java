package cn.jingyu.deepseek;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Shared by the Android host and the live JVM integration test. */
public final class NetworkClient {
 public interface Listener { void event(String type,String data); }
 private final ExecutorService workers=Executors.newCachedThreadPool();
 private final ScheduledExecutorService timers=Executors.newSingleThreadScheduledExecutor();
 public final class Task {
  private final AtomicBoolean finished=new AtomicBoolean();
  private volatile HttpURLConnection connection;
  private volatile long lastActivity=System.nanoTime();
  private final long started=System.nanoTime();
  private final Listener listener;
  private volatile ScheduledFuture<?> deadline;
  private Task(Listener listener){this.listener=listener;}
  private void send(String type,String data){if(!finished.get())listener.event(type,data);}
  private void finish(String type,String data){if(finished.compareAndSet(false,true)){ScheduledFuture<?> timer=deadline;if(timer!=null)timer.cancel(false);listener.event(type,data);}}
  private void disconnect(){HttpURLConnection c=connection;if(c!=null)workers.execute(c::disconnect);}
  public void cancel(){finish("cancelled","");disconnect();}
 }
 public Task start(String method,String endpoint,String key,String body,Listener listener){
  final Task task=new Task(listener);
  if(!endpoint.equals("/chat/completions")&&!endpoint.equals("/models")&&!endpoint.equals("/user/balance")&&!endpoint.equals("/v1/tools/search")&&!endpoint.equals("/v1/tools/fetch"))throw new IllegalArgumentException("Unsupported endpoint");
  task.deadline=timers.scheduleAtFixedRate(()->{long now=System.nanoTime();if(now-task.lastActivity>TimeUnit.SECONDS.toNanos(45)||now-task.started>TimeUnit.MINUTES.toNanos(10)){task.finish("error","网络响应超时，请检查网络后重试（45 秒无数据）");task.disconnect();}},1,1,TimeUnit.SECONDS);
  workers.execute(()->{
   HttpURLConnection con=null;
   try {
    if(task.finished.get())return;
    task.send("stage","正在连接 DeepSeek…");
    con=(HttpURLConnection)new URL("https://api.deepseek.com"+endpoint).openConnection();task.connection=con;
    con.setConnectTimeout(15000);con.setReadTimeout(45000);con.setInstanceFollowRedirects(false);
    con.setRequestMethod(method);con.setRequestProperty("Authorization","Bearer "+key);
    con.setRequestProperty("Accept-Encoding","identity");con.setRequestProperty("Cache-Control","no-cache");
    if(body!=null){con.setDoOutput(true);con.setRequestProperty("Content-Type","application/json; charset=utf-8");con.setRequestProperty("Accept",endpoint.equals("/chat/completions")?"text/event-stream":"application/json");byte[] bytes=body.getBytes(StandardCharsets.UTF_8);con.setFixedLengthStreamingMode(bytes.length);try(OutputStream out=con.getOutputStream()){out.write(bytes);out.flush();}}
    int code=con.getResponseCode();task.lastActivity=System.nanoTime();
    if(code<200||code>=300)throw new IOException("HTTP "+code+" "+readError(con.getErrorStream()));
    if(body==null||endpoint.startsWith("/v1/tools/")){if(endpoint.equals("/user/balance")||endpoint.startsWith("/v1/tools/")){try(Reader reader=new InputStreamReader(con.getInputStream(),StandardCharsets.UTF_8)){StringBuilder result=new StringBuilder();char[] data=new char[2048];int count;while((count=reader.read(data))!=-1){if(result.length()+count>2000000)throw new IOException("余额响应过大");result.append(data,0,count);}task.finish(endpoint.equals("/user/balance")?"balance":"json",result.toString());}}else task.finish("test-success","DeepSeek 官方 API 连接正常");return;}
    boolean streaming=!body.contains("\"stream\":false");
    task.send("stage",streaming?"已连接，等待模型输出…":"已连接，等待完整回答（兼容模式）…");
    if(!streaming||(con.getContentType()!=null&&con.getContentType().contains("application/json"))){
     try(Reader reader=new InputStreamReader(con.getInputStream(),StandardCharsets.UTF_8)){StringBuilder response=new StringBuilder();char[] chars=new char[2048];int n;while(!task.finished.get()&&(n=reader.read(chars))!=-1){task.lastActivity=System.nanoTime();if(response.length()+n>4000000)throw new IOException("模型输出超过本机显示限制");response.append(chars,0,n);}if(!task.finished.get()){task.send("response",response.toString());task.finish("done","");}}return;
    }
    try(BufferedReader reader=new BufferedReader(new InputStreamReader(con.getInputStream(),StandardCharsets.UTF_8))){
     String line;
     while(!task.finished.get()&&(line=reader.readLine())!=null){
      task.lastActivity=System.nanoTime();
      if(line.startsWith("data:")){task.send("chunk",line+"\n");if(line.substring(5).trim().equals("[DONE]")){task.finish("done","");return;}}
     }
    }
    task.finish("done","");
   }catch(Exception e){task.finish("error",e.getClass().getSimpleName()+": "+String.valueOf(e.getMessage()));}
   finally{if(con!=null)con.disconnect();}
  });
  return task;
 }
 private static String readError(InputStream stream)throws IOException{if(stream==null)return "";try(Reader r=new InputStreamReader(stream,StandardCharsets.UTF_8)){char[] chars=new char[2048];int n=r.read(chars);return n<0?"":new String(chars,0,n);}}
 public void close(){workers.shutdownNow();timers.shutdownNow();}
}
