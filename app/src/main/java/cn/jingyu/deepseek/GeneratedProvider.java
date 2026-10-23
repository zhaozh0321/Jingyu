package cn.jingyu.deepseek;
import android.content.*;import android.database.*;import android.net.Uri;import android.os.ParcelFileDescriptor;import android.provider.OpenableColumns;import java.io.*;

/** Read-only, temporary-grant access to a single generated artifact, never private history or keys. */
public final class GeneratedProvider extends ContentProvider {
 public boolean onCreate(){return true;}
 private File file(Uri uri)throws FileNotFoundException {try{if(!(getContext().getPackageName()+".generated").equals(uri.getAuthority())||uri.getPathSegments().size()!=1)throw new IOException();return ArtifactFiles.resolve(getContext(),uri.getLastPathSegment());}catch(Exception e){throw new FileNotFoundException("文件不可用");}}
 public String getType(Uri uri){String id=uri.getLastPathSegment();return ArtifactFiles.mime(id==null?"":id.substring(id.lastIndexOf('.')+1));}
 public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException {if(!"r".equals(mode))throw new FileNotFoundException("只读文件");return ParcelFileDescriptor.open(file(uri),ParcelFileDescriptor.MODE_READ_ONLY);}
 public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){try{File file=file(uri);String[] cols=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;MatrixCursor result=new MatrixCursor(cols);Object[] row=new Object[cols.length];for(int i=0;i<cols.length;i++)if(OpenableColumns.DISPLAY_NAME.equals(cols[i])){String name=uri.getQueryParameter("name");row[i]=name==null?file.getName():name;}else if(OpenableColumns.SIZE.equals(cols[i]))row[i]=file.length();result.addRow(row);return result;}catch(Exception e){return null;}}
 public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
}
