/**
 * Copyright 2014 Zhenguo Jin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.worthed.util;

import java.io.File;

import java.io.FileFilter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;

/**
 * APP工具类
 * 
 * @author jingle1267@163.com
 * 
 * @description APP相关信息工具类。获取版本信息
 * 
 */
public class AppUtils {

	private static final boolean DEBUG = true;
	private static final String TAG = "AppUtils";

	/**
	 * 得到软件版本号
	 * 
	 * @param c
	 * @return
	 */
	public static int getVerCode(Context c) {
		int verCode = -1;
		try {
			String packageName = c.getPackageName();
			verCode = c.getPackageManager().getPackageInfo(packageName, 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
		}
		return verCode;
	}

	/**
	 * 得到软件显示版本信息
	 * 
	 * @param c
	 * @return
	 */
	public static String getVerName(Context c) {
		String verName = "";
		try {
			String packageName = c.getPackageName();
			verName = c.getPackageManager().getPackageInfo(packageName, 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
		}
		return verName;
	}

	/**
	 * 安装apk
	 * 
	 * @param context
	 * @param file
	 */
	public static void installApk(Context context, File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	/**
	 * 安装apk
	 * 
	 * @param context
	 * @param file
	 */
	public static void installApk(Context context, Uri file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(file, "application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	/**
	 * 卸载apk
	 * 
	 * @param context
	 * @param packageName
	 */
	public static void uninstallApk(Context context, String packageName) {
		Intent intent = new Intent(Intent.ACTION_DELETE);
		Uri packageURI = Uri.parse("package:" + packageName);
		intent.setData(packageURI);
		context.startActivity(intent);
	}

	/**
	 * 检测服务是否运行
	 * 
	 * @param ctx
	 * @param className
	 * @return
	 */
	public static boolean isServiceRunning(Context ctx, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> servicesList = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		Iterator<RunningServiceInfo> l = servicesList.iterator();
		while (l.hasNext()) {
			RunningServiceInfo si = (RunningServiceInfo) l.next();
			if (className.equals(si.service.getClassName())) {
				isRunning = true;
			}
		}
		return isRunning;
	}

	/**
	 * 停止运行服务
	 * 
	 * @param ctx
	 * @param className
	 * @return
	 */
	public static boolean stopRunningService(Context ctx, String className) {
		Intent intent_service = null;
		boolean ret = false;
		try {
			intent_service = new Intent(ctx, Class.forName(className));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (intent_service != null) {
			ret = ctx.stopService(intent_service);
		}
		return ret;
	}

	/**
	 * 得到CPU核心数
	 * 
	 * @return
	 */
	public static int getNumCores() {
		try {
			File dir = new File("/sys/devices/system/cpu/");
			File[] files = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (Pattern.matches("cpu[0-9]", pathname.getName())) {
						return true;
					}
					return false;
				}
			});
			return files.length;
		} catch (Exception e) {
			return 1;
		}
	}

	/**
	 * whether this process is named with processName
	 * 
	 * @param context
	 * @param processName
	 * @return <ul>
	 *         return whether this process is named with processName
	 *         <li>if context is null, return false</li>
	 *         <li>if {@link ActivityManager#getRunningAppProcesses()} is null,
	 *         return false</li>
	 *         <li>if one process of
	 *         {@link ActivityManager#getRunningAppProcesses()} is equal to
	 *         processName, return true, otherwise return false</li>
	 *         </ul>
	 */
	public static boolean isNamedProcess(Context context, String processName) {
		if (context == null || TextUtils.isEmpty(processName)) {
			return false;
		}

		int pid = android.os.Process.myPid();
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processInfoList = manager
				.getRunningAppProcesses();
		if (processInfoList == null) {
			return true;
		}

		for (RunningAppProcessInfo processInfo : manager
				.getRunningAppProcesses()) {
			if (processInfo.pid == pid
					&& processName.equalsIgnoreCase(processInfo.processName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * whether application is in background
	 * <ul>
	 * <li>need use permission android.permission.GET_TASKS in Manifest.xml</li>
	 * </ul>
	 * 
	 * @param context
	 * @return if application is in background return true, otherwise return
	 *         false
	 */
	public static boolean isApplicationInBackground(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskList = am.getRunningTasks(1);
		if (taskList != null && !taskList.isEmpty()) {
			ComponentName topActivity = taskList.get(0).topActivity;
			if (topActivity != null
					&& !topActivity.getPackageName().equals(
							context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取应用签名
	 * 
	 * @param context
	 * @param pkgName
	 */
	public static String getSign(Context context, String pkgName) {
		try {
			PackageInfo pis = context.getPackageManager().getPackageInfo(
					pkgName, PackageManager.GET_SIGNATURES);
			return hexdigest(pis.signatures[0].toByteArray());
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 将签名字符串转换成需要的32位签名
	 * 
	 * @param paramArrayOfByte
	 * @return
	 */
	private static String hexdigest(byte[] paramArrayOfByte) {
		final char[] hexDigits = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97,
				98, 99, 100, 101, 102 };
		try {
			MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
			localMessageDigest.update(paramArrayOfByte);
			byte[] arrayOfByte = localMessageDigest.digest();
			char[] arrayOfChar = new char[32];
			for (int i = 0, j = 0;; i++, j++) {
				if (i >= 16) {
					return new String(arrayOfChar);
				}
				int k = arrayOfByte[i];
				arrayOfChar[j] = hexDigits[(0xF & k >>> 4)];
				arrayOfChar[++j] = hexDigits[(k & 0xF)];
			}
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * 清理后台进程与服务
	 * 
	 * @param cxt
	 *            应用上下文对象context
	 * @return 被清理的数量
	 */
	public static int gc(Context cxt) {
		long i = getDeviceUsableMemory(cxt);
		int count = 0; // 清理掉的进程数
		ActivityManager am = (ActivityManager) cxt
				.getSystemService(Context.ACTIVITY_SERVICE);
		// 获取正在运行的service列表
		List<RunningServiceInfo> serviceList = am.getRunningServices(100);
		if (serviceList != null)
			for (RunningServiceInfo service : serviceList) {
				if (service.pid == android.os.Process.myPid())
					continue;
				try {
					android.os.Process.killProcess(service.pid);
					count++;
				} catch (Exception e) {
					e.getStackTrace();
					continue;
				}
			}

		// 获取正在运行的进程列表
		List<RunningAppProcessInfo> processList = am.getRunningAppProcesses();
		if (processList != null)
			for (RunningAppProcessInfo process : processList) {
				// 一般数值大于RunningAppProcessInfo.IMPORTANCE_SERVICE的进程都长时间没用或者空进程了
				// 一般数值大于RunningAppProcessInfo.IMPORTANCE_VISIBLE的进程都是非可见进程，也就是在后台运行着
				if (process.importance > RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
					// pkgList 得到该进程下运行的包名
					String[] pkgList = process.pkgList;
					for (String pkgName : pkgList) {
						if (DEBUG) {
							LogUtils.d(TAG, "======正在杀死包名：" + pkgName);
						}
						try {
							am.killBackgroundProcesses(pkgName);
							count++;
						} catch (Exception e) { // 防止意外发生
							e.getStackTrace();
							continue;
						}
					}
				}
			}
		if (DEBUG) {
			LogUtils.d(TAG, "清理了" + (getDeviceUsableMemory(cxt) - i) + "M内存");
		}
		return count;
	}

	/**
	 * 获取设备的可用内存大小
	 * 
	 * @param cxt
	 *            应用上下文对象context
	 * @return 当前内存大小
	 */
	public static int getDeviceUsableMemory(Context cxt) {
		ActivityManager am = (ActivityManager) cxt
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		// 返回当前系统的可用内存
		return (int) (mi.availMem / (1024 * 1024));
	}

	/**
	 * 获取系统中所有的应用
	 * 
	 * @param context
	 * @return
	 */
	public static List<PackageInfo> getAllApps(Context context) {

		List<PackageInfo> apps = new ArrayList<PackageInfo>();
		PackageManager pManager = context.getPackageManager();
		List<PackageInfo> paklist = pManager.getInstalledPackages(0);
		for (int i = 0; i < paklist.size(); i++) {
			PackageInfo pak = (PackageInfo) paklist.get(i);
			if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				// customs applications
				apps.add(pak);
			}
		}
		return apps;
	}
	
	/**
     * 获取手机系统SDK版本
     * 
     * @return 如API 17 则返回 17
     */
    public static int getSDKVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }
	
}
