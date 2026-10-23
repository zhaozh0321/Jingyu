package cn.jingyu.deepseek;

import android.content.Context;
import android.security.keystore.*;
import android.util.AtomicFile;
import android.util.Base64;
import org.json.JSONObject;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.KeyStore;
import java.io.*;
import java.nio.charset.StandardCharsets;

public final class LocalStore {
 private final Context context;
 private final AtomicFile history;
 public LocalStore(Context context){this.context=context;history=new AtomicFile(new File(context.getFilesDir(),"history.json"));}
 public synchronized JSONObject load()throws Exception{
  if(!history.getBaseFile().exists())return new JSONObject();
  try(InputStream in=history.openRead();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);return new JSONObject(out.toString("UTF-8"));}
 }
 public synchronized void save(String value)throws IOException{
  FileOutputStream stream=null;
  try{stream=history.startWrite();stream.write(value.getBytes(StandardCharsets.UTF_8));history.finishWrite(stream);}catch(IOException error){if(stream!=null)history.failWrite(stream);throw error;}
 }
 public String key()throws Exception{
  String value=context.getSharedPreferences("vault",0).getString("key","");if(value.isEmpty())return "";
  JSONObject data=new JSONObject(value);KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,(javax.crypto.SecretKey)store.getKey("jingyu_api",null),new GCMParameterSpec(128,Base64.decode(data.getString("iv"),Base64.NO_WRAP)));
  return new String(cipher.doFinal(Base64.decode(data.getString("data"),Base64.NO_WRAP)),StandardCharsets.UTF_8);
 }
 public synchronized void key(String text)throws Exception{
  if(text.isEmpty()){if(!context.getSharedPreferences("vault",0).edit().remove("key").commit())throw new IOException("密钥清除失败");return;}
  KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);
  if(!store.containsAlias("jingyu_api")){KeyGenerator generator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");generator.init(new KeyGenParameterSpec.Builder("jingyu_api",KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());generator.generateKey();}
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,(javax.crypto.SecretKey)store.getKey("jingyu_api",null));JSONObject data=new JSONObject().put("iv",Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP)).put("data",Base64.encodeToString(cipher.doFinal(text.getBytes(StandardCharsets.UTF_8)),Base64.NO_WRAP));
  if(!context.getSharedPreferences("vault",0).edit().putString("key",data.toString()).commit())throw new IOException("密钥保存失败");
 }
}
