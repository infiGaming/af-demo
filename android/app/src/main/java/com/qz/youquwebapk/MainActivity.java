package com.qz.youquwebapk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.google.gson.Gson;
import ygy.bet.R;

public class MainActivity extends AppCompatActivity {

    public static MainActivity s_appActivity = null;
    private static ClipboardManager pasteclip = null;
    static public Dialog loadingDialog = null;
    private final String TAG = "JS:MainActivity";
    private Boolean hasEnterCustomTabs = false;  //是否已经打开过customtabs
    //
    private final String prodPath = "";
    //

     private JSONObject prodConfigJson = null;
    private static String m_appsflyer_id = "";

    private static String[] browserPackageNameArr = {
        // "com.android.browser", //小米、华为 、OPPO、vivo、魅族、努比亚、一加、中兴自带默认浏览器 
        "com.huawei.browser",
        "com.android.chrome",  //Google Chrome
        "org.mozilla.firefox", //Mozilla Firefox
        "com.microsoft.emmx",  //Microsoft Edge
        "com.opera.mini.native", //Opera Mini
        "com.opera.browser",   //Opera
        "com.UCMobile.intl",   //UC Browser
        "com.sec.android.app.sbrowser", //Samsung Internet Browser
        "com.brave.browser", //Brave Browser
        "com.cloudmosa.puffinFree", //Puffin Browser
        "mobi.mgeek.TunnyBrowser", //Dolphin Browser
        "com.mx.browser",   //Maxthon Browser
        "com.tencent.mtt", //QQ Browser
        "com.ksmobile.cb", //CM Browser
        "com.apusapps.browser", //APUS Browser
        "com.transsion.phoenix", //Phoenix Browser
        "com.yandex.browser",  //Yandex Browser
        "mark.via.gp",   //Via Browser
        "com.alohamobile.browser", //Aloha Browser
        "com.kiwibrowser.browser", //Kiwi Browser
        "com.baidu.browser.inter", //DU Browser
        "com.ghostery.android.ghostery" //Ghostery Privacy Browser
};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        s_appActivity = this;
        pasteclip = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        setContentView(R.layout.activity_main);
        //
//        createLoadingDialog();

        //本地写死prod
        {
            try {
                prodConfigJson = new JSONObject();
                prodConfigJson.put("gameUrl","https://www.baidu.com");
                prodConfigJson.put("afDevKey","");
                //
                final String afKey = getProdAfKey();
                if(afKey == null || afKey == ""){
                    afInitCallback("");
                }else{
                    afInit(afKey); //goto afInitCallback()
                }
            } catch (JSONException e) {

            }
        }

        //图片全屏显示
        // ImageView imageView = findViewById(R.id.imageView);
        // imageView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            // 活动的窗口现在具有焦点，执行相应的操作
            Log.d(TAG, "[MainActivity onWindowFocusChanged]:hasWindowFocus: " + hasWindowFocus);
            if(hasEnterCustomTabs){
                startOpenWeb();
            }
        } else {
            // 活动的窗口现在失去了焦点，执行相应的操作
            Log.d(TAG, "[MainActivity onWindowFocusChanged]:hasWindowFocus: " + hasWindowFocus);
        }
    }

    //获取到远程prod config之后的处理
    private void getProdConfigCallback(String prodJsonStr){
        Log.d(TAG, "getProdConfigCallback: " + prodJsonStr);
        //
        try {
            prodConfigJson = new JSONObject(prodJsonStr);
            //
            final String afKey = getProdAfKey();
            if(afKey == null || afKey == ""){
                afInitCallback("");
            }else{
                afInit(afKey); //goto afInitCallback()
            }
        } catch (JSONException e) {
//            e.printStackTrace();
        }
    }

    //goto afInitCallback()
    public void afInit(final String afKey) {
        Log.d(TAG, "afInit:key: " + afKey);
        //
        s_appActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //appsflyer
//                AppsFlyerLib.getInstance().setDebugLog(true);

                AppsFlyerConversionListener appsFlyerConversionListener = new AppsFlyerConversionListener() {
                    @Override
                    public void onConversionDataSuccess(Map<String, Object> map) {
                        Log.d("LOG_TAG", "[AppsFlyerConversionListener]:onConversionDataSuccess: " + new Gson().toJson((map)));
                    }

                    @Override
                    public void onConversionDataFail(String s) {
                        Log.d("LOG_TAG", "[AppsFlyerConversionListener]:onConversionDataFail: " + s);
                    }

                    @Override
                    public void onAppOpenAttribution(Map<String, String> map) {
                        Log.d("LOG_TAG", "[AppsFlyerConversionListener]:onAppOpenAttribution: " + new Gson().toJson((map)));
                    }

                    @Override
                    public void onAttributionFailure(String s) {
                        Log.d("LOG_TAG", "[AppsFlyerConversionListener]:onAttributionFailure: " + s);
                    }
                };

                AppsFlyerLib.getInstance().init(afKey, appsFlyerConversionListener, s_appActivity);
                AppsFlyerLib.getInstance().start(s_appActivity, afKey, new AppsFlyerRequestListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "AppsFlyer Launch sent successfully, got 200 response code from server");
                    }
                    @Override
                    public void onError(int i, @NonNull String s) {
                        Log.d(TAG, "AppsFlyer Launch failed to be sent:\n" +
                                "Error code: " + i + "\n"
                                + "Error description: " + s);
                    }
                });
                //
                final String appsflyer_id = AppsFlyerLib.getInstance().getAppsFlyerUID(s_appActivity);
                s_appActivity.afInitCallback(appsflyer_id);
            }
        });
    }

    private void afInitCallback(String afKey){
        m_appsflyer_id = afKey;
        //
        startOpenWeb();
    }

    private void startOpenWeb(){
        final String phoneInfo = getPhoneInfo();
        final String deviceIdStr = getDeviceId(phoneInfo);
        //
        final String prodGameUrl = getProdGameUrl();
        //
        final String deviceId = deviceIdStr;
        final String bundleId = getResources().getString(R.string.app_package_name);
        final String appsflyer_id = m_appsflyer_id;
        final String advertising_id = deviceIdStr;
        String inviteCode = getInviteCodeFromPasteboard();
        if(inviteCode == ""){
            inviteCode = getProdInviteCode();
        }
        //
        final String webUrl = getWebUrl(prodGameUrl,deviceId,bundleId,appsflyer_id,advertising_id,inviteCode);
        openCustomTab(webUrl);
        hasEnterCustomTabs = true;
        //
//        closeDialog();
    }

    private void openCustomTab(String url) {
        String customTabsSelectBrowser = getDeviceBrowserPackageName();
        Log.d("JS:" + TAG,"CustomTabs openCustomTab: url:" + url + " , this.customTabsSelectBrowser: " + customTabsSelectBrowser);
        //
        if (customTabsSelectBrowser != null) {
            Log.d("JS:" + TAG, "CustomTabs openCustomTab: to app broswer");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.outWidth = 24;
            options.outHeight = 24;
            options.inScaled = true; //already default, just for illustration - ie scale to screen density (dp)
            //
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(false)
                    .setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK)
                    .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                    .setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.bg_null_1,options))
                    .build();
            //
            // customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.intent.setPackage(customTabsSelectBrowser);
            customTabsIntent.launchUrl(this, Uri.parse(url));
        } else {
            //外跳手机浏览器打开
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Log.d("JS:" + TAG, "CustomTabs openCustomTab: out to system broswer");
            startActivity(intent);
        }
    }

    public static void createLoadingDialog(){
        s_appActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                LayoutInflater inflater = LayoutInflater.from(s_appActivity);
                View v = inflater.inflate(R.layout.dialog_loading, null);// 得到加载view
                LinearLayout layout = (LinearLayout) v
                        .findViewById(R.id.dialog_loading_view);// 加载布局
//        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
//        tipTextView.setText(msg);// 设置加载信息

                loadingDialog = new Dialog(s_appActivity, R.style.MyDialogStyle);// 创建自定义样式dialog
                loadingDialog.setCancelable(false); // 是否可以按“返回键”消失
                loadingDialog.setCanceledOnTouchOutside(false); // 点击加载框以外的区域
                loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
                /**
                 *将显示Dialog的方法封装在这里面
                 */
                Window window = loadingDialog.getWindow();
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setGravity(Gravity.CENTER);
                window.setAttributes(lp);
                window.setWindowAnimations(R.style.PopWindowAnimStyle);
                loadingDialog.show();
            }
        });
    }

    /**
     * 关闭dialog
     *
     */
    public static void closeDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    //---------------------------------------------//

     private String getWebUrl(final String prodGameUrl, final String deviceId, final String bundleId, final String appsflyer_id, final String advertising_id, final String inviteCode) {
        String weburl = prodGameUrl;
        try {
            String firstPre = (prodGameUrl.contains("?") ? "&" : "?");
            weburl += (firstPre + "deviceId=" + URLEncoder.encode(deviceId, "UTF-8"));
            weburl += ("&bundleId=" + URLEncoder.encode(bundleId, "UTF-8"));
            if (appsflyer_id != null && appsflyer_id != "") {
                weburl += ("&appsflyer_id=" + URLEncoder.encode(appsflyer_id, "UTF-8"));
            }
            weburl += ("&advertising_id=" + URLEncoder.encode(advertising_id, "UTF-8"));
            //
            if (inviteCode != "") {
                weburl += ("&inviteCode=" + URLEncoder.encode(inviteCode, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
        }
        return weburl;
    }

    private String getInviteCodeFromPasteboard(){
        final String pasteStr = getPasteboard();
        //
        int index1 = pasteStr.indexOf("inviteCode=");
        if(index1 != -1){
            int index2 = pasteStr.indexOf("&",index1);
            if(index2 != -1){
                return pasteStr.substring(index1 + 11,index2);
            }else{
                return pasteStr.substring(index1 + 11,pasteStr.length());
            }
        }
        return "";
    }

    //数据接口：webUrl地址
    private String getProdGameUrl(){
        try {
            String gameUrl = prodConfigJson.getString("gameUrl");
            return gameUrl;
        } catch (JSONException e) {
//            e.printStackTrace();
            return "";
        }
    }
    private String getProdAfKey(){
        try {
            String afDevKey = prodConfigJson.getString("afDevKey");
            return afDevKey;
        } catch (JSONException e) {
//            e.printStackTrace();
            return "";
        }
    }
    private String getProdInviteCode(){
        try {
            String inviteCode = prodConfigJson.getString("inviteCode");
            return inviteCode;
        } catch (JSONException e) {
//            e.printStackTrace();
            return "";
        }
    }

    //---------------------------------------------//

    public String getPhoneInfo()
    {
        String deviceID = "";
        String androidID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        String serial = Build.SERIAL;
        deviceID = androidID + serial;

        JSONObject res = new JSONObject();
        try {
            res.put("a",deviceID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        res.put("b",appsflyer_id);

        final String phoneInfo = res.toString();
        return phoneInfo;
//        return deviceID+","+appsflyer_id+","+language+","+timezone+","+vpn;
    }

    public String getDeviceId(String phoneInfo){
        try {
            JSONObject phoneInfoJson = new JSONObject(phoneInfo);
            final String deviceID = phoneInfoJson.getString("a");
            final String deviceIDMD5 = MD5Utils.md5(deviceID);
            return deviceIDMD5;
        } catch (JSONException e) {
//            e.printStackTrace();
            return "";
        }
    }

    public static void setPasteboard(final String text)
    {
        if(pasteclip != null && text != null && text != ""){
            pasteclip.setText(text);
        }
    }

    public static String getPasteboard()
    {
        if(pasteclip != null && pasteclip.getText() != null){
            String str = pasteclip.getText().toString();
            return str;
        }
        return "";
    }

    // 检查指定的包名是否已安装在设备上
    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private String getDeviceBrowserPackageName() {
        if (isHuaweiDevice()) {
            // "com.android.browser", //小米、华为 、OPPO、vivo、魅族、努比亚、一加、中兴自带默认浏览器
            if (this.isPackageInstalled("com.android.browser", getPackageManager())) {
                return "com.android.browser";
            }
        }
        //
        for (int i = 0; i < browserPackageNameArr.length; i++) {
            if (this.isPackageInstalled(browserPackageNameArr[i], getPackageManager())) {
                return browserPackageNameArr[i];
            }
        }
        return null;
    }

    private boolean isHuaweiDevice() {
        if (Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {
            return true;
        } else if (Build.BRAND.equalsIgnoreCase("HUAWEI")) {
            return true;
        } else if (Build.MODEL.startsWith("HUAWEI")) {
            return true;
        }
        return false;
    }

}

class AESUtil {

    private static final String IV_PARAMETER = "60fc7353-3301-4d";
    private static final String ALGORITHM = "AES/CBC/PKCS7Padding";
    private static final String CHARSET = "UTF-8";

    public static String decrypt(String cipherText, String key, String iv) throws Exception {
        byte[] encryptedData = Base64.decode(cipherText, Base64.DEFAULT);
        byte[] keyBytes = key.getBytes("UTF-8");
        byte[] ivBytes = iv.getBytes("UTF-8");
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(encryptedData);
        return new String(decrypted, "UTF-8");
    }
}

class MD5Utils {
    public static String md5(String input) {
        try {
            // 获取 MessageDigest 实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 将输入字符串转换为字节数组，并进行加密
            byte[] messageDigest = md.digest(input.getBytes());
            // 将加密后的字节数组转换为字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}