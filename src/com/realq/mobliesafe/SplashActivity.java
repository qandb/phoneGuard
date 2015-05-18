package com.realq.mobliesafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParserException;

import com.realq.mobliesafe.domain.UpdateInfo;
import com.realq.mobliesafe.engine.UpdateInfoParser;
import com.realq.mobliesafe.utils.DownLoadUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {
	private TextView tv_splash_version;
	private ProgressDialog pd;

	private SharedPreferences sp;
	
	private UpdateInfo info;
	private static final int GET_INFO_SUCCESS = 10;
	private static final int SERVER_ERROR = 11;
	private static final int SERVER_URL_ERROR = 12;
	private static final int PROTOCOL_ERROR = 13;
	private static final int IO_ERROR = 14;
	private static final int XML_PARSE_ERROR = 15;
	private static final int DOWNLOAD_SUCCESS = 16;
	private static final int DOWNLOAD_ERROR = 17;
    private static final int NO_NEED_CHK_UPDATE = 18;
	
	protected static final String TAG = "SplashActivity";
	private long startTime;
	private long endTime;
	private RelativeLayout r1_splash;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg){
			switch(msg.what){
			case XML_PARSE_ERROR:
				Toast.makeText(getApplicationContext(), "XML 解析错误", 1).show();
				loadMainUI();
				break;
			case IO_ERROR:
				Toast.makeText(getApplicationContext(), "I/O 错误", 1).show();
				loadMainUI();
				break;
			case PROTOCOL_ERROR:
				Toast.makeText(getApplicationContext(), "协议不支持", 1).show();
				loadMainUI();
				break;
			case SERVER_URL_ERROR:
				Toast.makeText(getApplicationContext(), "服务器路径不正确", 1).show();
				break;
			case SERVER_ERROR:
				Toast.makeText(getApplicationContext(), "服务器内部异常", 1).show();
				loadMainUI();
				break;
			case GET_INFO_SUCCESS:
				String serverversion = info.getVersion();
				String currentversion = getVersion();
				Log.d(TAG,"serverversion:"+serverversion+" currentversion"+currentversion);
				if(currentversion.equals(serverversion)){
					Log.d(TAG,"版本号相同进入主界面");
					loadMainUI();
				}else{
					Log.d(TAG,"版本号不相同，升级对话框");
					showUpdateDialog();
				}
				break;
			case DOWNLOAD_SUCCESS:
				File file = (File)msg.obj;
				installApk(file);
				break;
			case DOWNLOAD_ERROR:
				Toast.makeText(getApplicationContext(), "下载数据异常", 1).show();
				loadMainUI();
				break;
                case NO_NEED_CHK_UPDATE:
                    loadMainUI();
                    break;
			};
		}
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("版本号："+getVersion());
		
		r1_splash = (RelativeLayout) findViewById(R.id.r1_splash);
		AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
		aa.setDuration(2000);
		r1_splash.startAnimation(aa);

		sp = getSharedPreferences("config",MODE_PRIVATE);
		boolean autoupdate = sp.getBoolean("autoupdate",true);
		//连接服务器，获取服务器上的配置信息
		if(autoupdate) {
			new Thread(new CheckVersionTask()) {
			}.start();
		}else{
            handler.sendEmptyMessageDelayed(NO_NEED_CHK_UPDATE,2000);
        }
	}
	
	private class CheckVersionTask implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			startTime = System.currentTimeMillis();
			Message msg = Message.obtain();
			try{
				String serverurl = getResources().getString(R.string.serverurl);
				URL url = new URL(serverurl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5000);
				int code = conn.getResponseCode();
				Log.d(TAG,"serverurl:"+serverurl+" result:"+code);
				if(code == 200){
					InputStream is = conn.getInputStream();
					info = UpdateInfoParser.getUpdateInfo(is);
					endTime = System.currentTimeMillis();
					long resulttime = endTime - startTime;
					if(resulttime<2000){
						try{
							Thread.sleep(2000-resulttime);
						}catch(InterruptedException e){
							e.printStackTrace();
						}
					}
					msg.what = GET_INFO_SUCCESS;
					handler.sendMessage(msg);
				}else{
					msg.what = SERVER_ERROR;
					handler.sendMessage(msg);
					endTime = System.currentTimeMillis();
					long resulttime = endTime - startTime;
					if(resulttime<2000){
						try{
							Thread.sleep(2000-resulttime);
						}catch(InterruptedException e){
							e.printStackTrace();
						}
					}
				}
				
			}catch(MalformedURLException e){
				e.printStackTrace();
				msg.what = SERVER_URL_ERROR;
				handler.sendMessage(msg);
			}catch(ProtocolException e){
				e.printStackTrace();
				msg.what = PROTOCOL_ERROR;
				handler.sendMessage(msg);
			}catch(IOException e){
				e.printStackTrace();
				msg.what = IO_ERROR;
				handler.sendMessage(msg);
			}catch(XmlPullParserException e){
				e.printStackTrace();
				msg.what = XML_PARSE_ERROR;
				handler.sendMessage(msg);
			}
		}
		
	}

	protected void showUpdateDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(getResources().getDrawable(R.drawable.notification));
		builder.setTitle("升级提示");
		builder.setMessage(info.getDescription());
		pd = new ProgressDialog(SplashActivity.this);
		pd.setMessage("正在下载");
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
		builder.setPositiveButton("升级", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
					pd.show();
					new Thread(){
						public void run(){
							String path = info.getApkurl();
							String filename = DownLoadUtil.getFileName(path);
							File file = new File(Environment.getExternalStorageDirectory(),filename);
							file = DownLoadUtil.getFile(path, file.getAbsolutePath(), pd);
							if(file != null){
								Message msg = Message.obtain();
								msg.what = DOWNLOAD_SUCCESS;
								msg.obj = file;
								handler.sendMessage(msg);
							}else{
								Message msg = Message.obtain();
								msg.what = DOWNLOAD_ERROR;
								msg.obj = file;
								handler.sendMessage(msg);
							}
							pd.dismiss();
						}
					}.start();
				}else{
					Toast.makeText(getApplicationContext(), "sd卡不可用", 1).show();
					loadMainUI();
				}
			}
			
		});
		
		builder.setNegativeButton("取消", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				loadMainUI();
			}
			
		});
		builder.create().show();
	}

	private String getVersion() {
		// TODO Auto-generated method stub
		PackageManager pm = this.getPackageManager();
		
		try{
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			return info.versionName;
			
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}
	
	protected void installApk(File file){
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		startActivity(intent);
	}
	
	private void loadMainUI(){
		Intent intent = new Intent(this,MainActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
