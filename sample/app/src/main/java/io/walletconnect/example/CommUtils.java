package io.walletconnect.example;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * @Author k.k
 * @Date 2023/3/15
 * @Desc 类描述
 */
public class CommUtils {

    //当本应用位于后台时，则将它切换到最前端
    public static void setTopApp(Context context) {
        if (isRunningForeground(context)) {
            return;
        }
        //获取ActivityManager
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

        //获得当前运行的task(任务)
        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
            //找到本应用的 task，并将它切换到前台
            if (taskInfo.topActivity.getPackageName().equals(context.getPackageName())) {
                activityManager.moveTaskToFront(taskInfo.id, 0);
                break;
            }
        }
    }

    //判断本应用是否已经位于最前端：已经位于最前端时，返回 true；否则返回 false
    public static boolean isRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfoList) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
                return true;
            }
        }
        return false;
    }

    public static String getAppInfo(Context context) throws Exception {
        String curPackageName = "";
        PackageManager packageManager = context.getPackageManager();
        //获取所有安装的app
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
        for (PackageInfo info : installedPackages) {
            String packageName = info.packageName;//app包名
            ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
            String appName = (String) packageManager.getApplicationLabel(ai);//获取应用名称
            if("MetaMask".equals(appName)) {
                Log.d("getAppInfo", "appName:" + appName);
                Log.d("getAppInfo", "apppackageName:" + packageName);
                curPackageName = packageName;
                break;
            }
        }
        if(TextUtils.isEmpty(curPackageName)){
            Log.d("getAppInfo", "not found need APP!");
        }
        return curPackageName;
    }

    public static void openApp(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);
    }
}
