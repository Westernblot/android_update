package me.plugin.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

/**
 * 自动更新插件 wangsheng
 * 
 * @author Administrator
 * 
 */
public class WsAutoUpdate extends CordovaPlugin {

	private ProgressDialog pBar;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		// TODO Auto-generated method stub

		if (action.equalsIgnoreCase("update")) {

			// // 传递参数:1,最新版本号2,最新apk下载地址3,更新说明
			System.out.println("args==" + args);

			String serverVersion = args.getString(0);
			String serverUrl = args.getString(1);
			String serverDescription = args.getString(2);

			System.out.println("serverVersion==" + serverVersion);
			System.out.println("serverUrl==" + serverUrl);
			System.out.println("serverDescription==" + serverDescription);

			String version = this.getVersionName(cordova.getActivity());// 获取APP当前版本号

			if (!serverVersion.equals(version)) {
				showUpdateDialog(serverUrl, serverDescription);
			}
			callbackContext.success("success");
			return true;
		}

		callbackContext.error("error");
		return false;
	}

	/**
	 * 显示升级对话框
	 * 
	 * @param serverDescription2
	 * @param serverUrl
	 */
	protected void showUpdateDialog(final String serverUrl,
			String serverDescription) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new Builder(cordova.getActivity());
		builder.setTitle("提示升级");
		builder.setMessage(serverDescription);
		builder.setPositiveButton("立即升级", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// 下载apk检查
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// sd卡空间充足, 下载apk
					downFile(serverUrl);

				} else {
					Toast.makeText(cordova.getActivity(), "sd卡空间不足！", 0).show();
					return;
				}
			}
		});

		builder.setNegativeButton("下次再说", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.show();
	}

	/**
	 * [获取应用程序版本名称信息]
	 * 
	 * @param context
	 * @return 当前应用的版本名称
	 */
	public static String getVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			return packageInfo.versionName;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	// =========================================分隔线===========================================

	/**
	 * 下载apk 程序
	 */
	public void downFile(String serverUrl) {
		final String url = serverUrl;
		pBar = new ProgressDialog(cordova.getActivity()); // 进度条，在下载的时候实时更新进度，提高用户友好度
		pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pBar.setTitle("正在下载");
		pBar.setMessage("请稍候...");
		pBar.setProgress(0);
		pBar.show();
		new Thread() {
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					int length = (int) entity.getContentLength(); // 获取文件大小
					pBar.setMax(length); // 设置进度条的总长度
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					if (is != null) {
						File file = new File(
								Environment.getExternalStorageDirectory(),
								"Test.apk");
						fileOutputStream = new FileOutputStream(file);
						byte[] buf = new byte[10]; // 这个是缓冲区，即一次读取10个比特，我弄的小了点，因为在本地，所以数值太大一
													// 下就下载完了，看不出progressbar的效果。
						int ch = -1;
						int process = 0;
						while ((ch = is.read(buf)) != -1) {
							fileOutputStream.write(buf, 0, ch);
							process += ch;
							pBar.setProgress(process); // 这里就是关键的实时更新进度了！
						}

					}
					fileOutputStream.flush();
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}

					// 更新UI线程
					cordova.getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							pBar.cancel();
							update();
						}
					});

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}.start();
	}

	// 安装文件，一般固定写法
	void update() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 安装完成后，显示 完成/打开的提示页
		intent.setDataAndType(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), "Test.apk")),
				"application/vnd.android.package-archive");
		cordova.getActivity().startActivity(intent);
	}

}
