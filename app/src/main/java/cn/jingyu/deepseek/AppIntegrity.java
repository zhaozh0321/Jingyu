package cn.jingyu.deepseek;

import android.content.Context;
import android.content.pm.*;
import android.os.Build;
import java.security.*;

/** Basic re-signing detection; this is not a claim of unbreakable DRM. */
public final class AppIntegrity {
 private static final String EXPECTED=BuildConfig.SIGNING_CERT_SHA256;
 public static boolean trusted(byte[] certificate)throws Exception {
  byte[] digest=MessageDigest.getInstance("SHA-256").digest(certificate);StringBuilder hex=new StringBuilder();for(byte b:digest)hex.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return MessageDigest.isEqual(EXPECTED.getBytes(java.nio.charset.StandardCharsets.US_ASCII),hex.toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII));
 }
 public static void verify(Context context)throws Exception {
  if(BuildConfig.DEBUG)return;
  PackageManager pm=context.getPackageManager();android.content.pm.Signature[] signatures;
  if(Build.VERSION.SDK_INT>=28){PackageInfo info=pm.getPackageInfo(context.getPackageName(),PackageManager.GET_SIGNING_CERTIFICATES);signatures=info.signingInfo==null?null:info.signingInfo.getApkContentsSigners();}
  else signatures=pm.getPackageInfo(context.getPackageName(),PackageManager.GET_SIGNATURES).signatures;
  if(signatures==null||signatures.length!=1||!trusted(signatures[0].toByteArray()))throw new SecurityException("安装包签名不匹配，请安装原版鲸语。");
 }
}
