package com.marz.snapprefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XModuleResources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.marz.snapprefs.Util.FileUtils;
import com.marz.snapprefs.Util.XposedUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static de.robv.android.xposed.XposedHelpers.setStaticIntField;


public class HookMethods implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {


    public static final String SNAPCHAT_PACKAGE_NAME = "com.snapchat.android";
    // Modes for saving Snapchats
    public static final int SAVE_AUTO = 0;
    public static final int SAVE_S2S = 1;
    public static final int DO_NOT_SAVE = 2;
    // Length of toasts
    public static final int TOAST_LENGTH_SHORT = 0;
    public static final int TOAST_LENGTH_LONG = 1;
    // Minimum timer duration disabled
    public static final int TIMER_MINIMUM_DISABLED = 0;
    private static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();
    // Preferences and their default values
    public static int mModeSnapImage = SAVE_AUTO;
    public static int mModeSnapVideo = SAVE_AUTO;
    public static int mModeStoryImage = SAVE_AUTO;
    public static int mModeStoryVideo = SAVE_AUTO;
    public static int mToastLength = TOAST_LENGTH_LONG;
    public static int mTimerMinimum = TIMER_MINIMUM_DISABLED;
    public static boolean mCustomFilterBoolean = false;
    public static boolean mPaintTools = true;
    public static boolean mMultiFilterBoolean = true;
    public static int mCustomFilterType;
    public static boolean mTimerUnlimited = true;
    public static boolean mHideTimerStory = false;
    public static boolean mLoopingVids = true;
    public static boolean mHideTimer = false;
    public static boolean mToastEnabled = true;
    public static boolean mVibrationEnabled = true;
    public static String mSavePath = "";
    public static String mCustomFilterLocation = "";
    public static String mConfirmationID = "";
    public static String mDeviceID = "";

    public static String vibrationPattern = ""; //change vibration pattern
    public static boolean mHideLive = false;
    public static boolean mHideDiscover = false;
    public static boolean mHidePeople = false;
    public static String peopleToHide = "";
    public static boolean mShowFavorites = false;
    public static String favoritesList = "";
    public static boolean mShowFavsOnTop = false;
    public static boolean mVibrateChange = false;

    public static boolean mSaveSentSnaps = false;
    public static boolean mSortByCategory = true;
    public static boolean mSortByUsername = true;
    public static boolean mDebugging = true;
    public static boolean mOverlays = false;
    public static boolean mSpeed = false;
    public static boolean mWeather = false;
    public static boolean mDiscoverSnap = false;
    public static boolean mDiscoverUI = false;
    public static boolean mCustomSticker = false;
    public static boolean mReplay = false;
    public static boolean mStealth = false;
    public static boolean mTyping = false;
    public static int mLicense = 0;
    static XSharedPreferences prefs;
    static XSharedPreferences license;
    static boolean selectStory;
    static boolean selectVenue;
    static boolean txtcolours;
    static boolean bgcolours;
    static boolean size;
    static boolean transparency;
    static boolean rainbow;
    static boolean bg_transparency;
    static boolean txtstyle;
    static boolean txtgravity;
    static boolean debug;
    static EditText editText;
    static XModuleResources modRes;
    static Activity SnapContext;
    static Context context;
    static int counter = 0;
    private static XModuleResources mResources;
    private static int snapchatVersion;
    private static String MODULE_PATH = null;
    private static boolean fullCaption;
    private static boolean selectAll;
    private static boolean hideBf;
    private static boolean hideRecent;
    private static boolean shouldAddGhost;
    private static boolean mColours;
    private static boolean mLocation;
    private static boolean mTimerCounter;
    private static boolean mChatAutoSave;
    private static boolean mChatImageSave;
    private static InitPackageResourcesParam resParam;
    Class CaptionEditText;
    boolean latest = false;

    public static int px(float f) {
        return Math.round((f * SnapContext.getResources().getDisplayMetrics().density));
    }

    public static int pxC(float f, Context ctx) {
        return Math.round((f * ctx.getResources().getDisplayMetrics().density));
    }

    static void refreshPreferences() {
        prefs = new XSharedPreferences(new File(
                Environment.getDataDirectory(), "data/"
                + PACKAGE_NAME + "/shared_prefs/" + PACKAGE_NAME
                + "_preferences" + ".xml"));
        prefs.reload();
        prefs.makeWorldReadable();
        fullCaption = prefs.getBoolean("pref_key_fulltext", false);
        selectAll = prefs.getBoolean("pref_key_selectall", false);
        selectStory = prefs.getBoolean("pref_key_selectstory", false);
        selectVenue = prefs.getBoolean("pref_key_selectvenue", false);
        hideBf = prefs.getBoolean("pref_key_hidebf", false);
        hideRecent = prefs.getBoolean("pref_key_hiderecent", false);
        txtcolours = prefs.getBoolean("pref_key_txtcolour", false);
        bgcolours = prefs.getBoolean("pref_key_bgcolour", false);
        size = prefs.getBoolean("pref_key_size", false);
        transparency = prefs.getBoolean("pref_key_size", false);
        rainbow = prefs.getBoolean("pref_key_rainbow", false);
        bg_transparency = prefs.getBoolean("pref_key_bg_transparency", false);
        txtstyle = prefs.getBoolean("pref_key_txtstyle", false);
        txtgravity = prefs.getBoolean("pref_key_txtgravity", false);
        mPaintTools = prefs.getBoolean("pref_key_paint_checkbox", mPaintTools);
        mTimerCounter = prefs.getBoolean("pref_key_timercounter", true);
        mChatAutoSave = prefs.getBoolean("pref_key_save_chat_text", true);
        mChatImageSave = prefs.getBoolean("pref_key_save_chat_image", true);
        mCustomFilterBoolean = prefs.getBoolean("pref_key_custom_filter_checkbox", mCustomFilterBoolean);
        mMultiFilterBoolean = prefs.getBoolean("pref_key_multi_filter_checkbox", mMultiFilterBoolean);
        mCustomFilterLocation = Environment.getExternalStorageDirectory().toString() + "/Snapprefs/Filters";
        mCustomFilterType = prefs.getInt("pref_key_filter_type", 0);
        mSpeed = prefs.getBoolean("pref_key_speed", false);
        mWeather = prefs.getBoolean("pref_key_weather", false);
        mLocation = prefs.getBoolean("pref_key_location", false);
        mDiscoverSnap = prefs.getBoolean("pref_key_discover", false);
        mDiscoverUI = prefs.getBoolean("pref_key_discover_ui", false);
        mCustomSticker = prefs.getBoolean("pref_key_sticker", false);
        mReplay = prefs.getBoolean("pref_key_replay", false);
        mStealth = prefs.getBoolean("pref_key_viewed", false);
        mTyping = prefs.getBoolean("pref_key_typing", false);
        mConfirmationID = prefs.getString("confirmation_id", "");
        debug = prefs.getBoolean("pref_key_debug", false);
        mDeviceID = prefs.getString("device_id", null);
        mLicense = prefs.getInt(mDeviceID, 0);

        mVibrateChange = prefs.getBoolean("pref_key_vibrate_change", false);
        vibrationPattern = prefs.getString("pref_key_vibrate_set", "");
        String read = FileUtils.readFromSDFile("storysettings").replaceAll("\n", "");
        if(!read.equals("0")) {
            String settings[] = read.split("#");
            mHideDiscover = Boolean.parseBoolean(settings[0]);
            mHideLive = Boolean.parseBoolean(settings[1]);
            mHidePeople = Boolean.parseBoolean(settings[2]);
            mShowFavorites = Boolean.parseBoolean(settings[3]);
            peopleToHide = settings[4];
            favoritesList = settings[5];
            mShowFavsOnTop = Boolean.parseBoolean(settings[6]);
        }

        //SAVING

        mModeSnapImage = prefs.getInt("pref_key_snaps_images", mModeSnapImage);
        mModeSnapVideo = prefs.getInt("pref_key_snaps_videos", mModeSnapVideo);
        mModeStoryImage = prefs.getInt("pref_key_stories_images", mModeStoryImage);
        mModeStoryVideo = prefs.getInt("pref_key_stories_videos", mModeStoryVideo);
        mTimerMinimum = prefs.getInt("pref_key_timer_minimum", mTimerMinimum);
        mToastEnabled = prefs.getBoolean("pref_key_toasts_checkbox", mToastEnabled);
        mVibrationEnabled = prefs.getBoolean("pref_key_vibration_checkbox", mVibrationEnabled);
        mToastLength = prefs.getInt("pref_key_toasts_duration", mToastLength);
        mSavePath = prefs.getString("pref_key_save_location", mSavePath);
        mSaveSentSnaps = prefs.getBoolean("pref_key_save_sent_snaps", mSaveSentSnaps);
        mSortByCategory = prefs.getBoolean("pref_key_sort_files_mode", mSortByCategory);
        mSortByUsername = prefs.getBoolean("pref_key_sort_files_username", mSortByUsername);
        mDebugging = prefs.getBoolean("pref_key_debug_mode", mDebugging);
        mOverlays = prefs.getBoolean("pref_key_overlay", mOverlays);
        mTimerUnlimited = prefs.getBoolean("pref_key_timer_unlimited", mTimerUnlimited);
        mHideTimerStory = prefs.getBoolean("pref_key_timer_story_hide", mHideTimerStory);
        mLoopingVids = prefs.getBoolean("pref_key_looping_video", mLoopingVids);
        mHideTimer = prefs.getBoolean("pref_key_timer_hide", mHideTimer);


        //SHARING

        Common.ROTATION_MODE = Integer.parseInt(prefs.getString("pref_rotation", Integer.toString(Common.ROTATION_MODE)));
        Common.ADJUST_METHOD = Integer.parseInt(prefs.getString("pref_adjustment", Integer.toString(Common.ADJUST_METHOD)));
        Common.CAPTION_UNLIMITED_VANILLA = prefs.getBoolean("pref_caption_unlimited_vanilla", Common.CAPTION_UNLIMITED_VANILLA);
        Common.CAPTION_UNLIMITED_FAT = prefs.getBoolean("pref_caption_unlimited_fat", Common.CAPTION_UNLIMITED_FAT);
        Common.DEBUGGING = prefs.getBoolean("pref_debug", Common.DEBUGGING);
        Common.CHECK_SIZE = !prefs.getBoolean("pref_size_disabled", !Common.CHECK_SIZE);
        Common.TIMBER = prefs.getBoolean("pref_timber", Common.TIMBER);

        if (txtcolours == true || bgcolours == true || size == true || rainbow == true || bg_transparency == true || txtstyle == true || txtgravity == true) {
            mColours = true;
        } else {
            mColours = false;
        }

        if (mSpeed || mColours || mLocation || mWeather) {
            shouldAddGhost = true;
        } else {
            shouldAddGhost = false;
        }
    }

    static void logging(String message) {
        if (mDebugging == true)
            XposedBridge.log(message);
    }

    public static void printStackTraces() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            Logger.log("Class name :: " + element.getClassName() + "  || method name :: " + element.getMethodName());
        }
    }

    public boolean postData() {

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://snapprefs.com/checkversion.php");


        try {
            // Add your data
            List nameValuePairs = new ArrayList(2);
            nameValuePairs.add(new BasicNameValuePair("version", "1.5.0"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            InputStream is = response.getEntity().getContent();
            BufferedInputStream bis = new BufferedInputStream(is);
            final ByteArrayBuffer baf = new ByteArrayBuffer(20);

            int current = 0;

            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            String text = new String(baf.toByteArray());
            String status = null;
            String error_msg = null;
            try {

                JSONObject obj = new JSONObject(text);
                status = obj.getString("status");
                error_msg = obj.getString("error_msg");
                if (status.equals("0") && !error_msg.isEmpty()) {
                    latest = true;
                }
                if (status.equals("1") && error_msg.isEmpty()) {
                    //Toast.makeText(SnapContext, "New version available, update NOW from the Xposed repo.", Toast.LENGTH_SHORT).show();
                    latest = false;
                }
            } catch (Throwable t) {
                Log.e("Snapprefs", "Could not parse malformed JSON: \"" + text + "\"");
                latest = false;
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            //saveIntPreference("license_status", 0);
            latest = false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //saveIntPreference("license_status", 0);
            latest = false;
        }
        return latest;
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        mResources = XModuleResources.createInstance(startupParam.modulePath, null);
        //refreshPreferences();
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(Common.PACKAGE_SNAP))
            return;
        mSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs";
        mCustomFilterLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs/Filters";
        refreshPreferences();
        resParam = resparam;
        modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
        if (shouldAddGhost) {
            addGhost(resparam);
        }
        addSnapprefsBtn(resparam);
        //if (mCustomFilterType == 0) {
            fullScreenFilter(resparam);
        //}
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(Common.PACKAGE_SNAP))
            return;
        try {
            mSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs";
            mCustomFilterLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs/Filters";
            XposedUtils.log("----------------- SNAPPREFS HOOKED -----------------", false);
            Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
            context = (Context) callMethod(activityThread, "getSystemContext");

            PackageInfo piSnapChat = context.getPackageManager().getPackageInfo(lpparam.packageName, 0);
            XposedUtils.log("SnapChat Version: " + piSnapChat.versionName + " (" + piSnapChat.versionCode + ")", false);
            XposedUtils.log("SnapPrefs Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")", false);
            if (!Obfuscator.isSupported(piSnapChat.versionCode)) {
                Logger.log("This Snapchat version is unsupported", true, true);
                Toast.makeText(context, "This Snapchat version is unsupported", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            XposedUtils.log("Exception while trying to get version info", e);
            return;
        }
        findAndHookMethod("android.app.Application", lpparam.classLoader, "attach", Context.class, new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        refreshPreferences();
        printSettings();
        if (mLicense == 1 || mLicense == 2) {

            if (mReplay == true) {
                //Premium.initReplay(lpparam, modRes, SnapContext);
            }
            if (mTyping == true) {
                Premium.initTyping(lpparam, modRes, SnapContext);
            }
            if (mStealth == true && mLicense == 2) {
                Premium.initViewed(lpparam, modRes, SnapContext);
            }
        }


                Class<?> storiesfrag = findClass("com.snapchat.android.fragments.stories.StoriesFragment", lpparam.classLoader);
                findAndHookMethod(storiesfrag, "D", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if(mHideDiscover || mHideLive || mHidePeople || mShowFavorites) {
                            ArrayList f = (ArrayList) getObjectField(param.thisObject, "f");

                            for (int i = f.size() - 1; i >= 0; i--) {
                                Object o = f.get(i);
                                String hotlinebling = o.toString();
                                if (hotlinebling.substring(0, 2).contains("JM")) {
                                    String username = callMethod(o, "i").toString();//I think these are the recent stories
                                    //Logger.log(username);
                                    if (mHidePeople) {
                                        String[] people = peopleToHide.split(",");
                                        for (String person : people) {
                                            if (username.equals(person)) {
                                                Logger.log("removing " + username);
                                                f.remove(i);
                                            }
                                        }
                                    }
                                    if (mShowFavorites) {
                                        String[] favs = favoritesList.split(",");
                                        Set<String> favorites = new HashSet<String>(Arrays.asList(favs));
                                        Logger.log(favorites.toString());
                                        if (!favorites.contains(username)) {
                                            if (!mShowFavsOnTop) {
                                                Logger.log("removing " + username);
                                                f.remove(i);
                                                //f.add(0, b);
                                            }
                                        }else{
                                            if (mShowFavsOnTop) {
                                                Logger.log("removing " + username);
                                                Object b = f.remove(i);
                                                f.add(0, b);
                                            }
                                        }
                                    }
                                }
                                //else if(hotlinebling.substring(0,2).contains("Jy"))       //All stories, but idk how to get the friend username since no method.
                                //    Logger.log(getObjectField(, ));
                                else if (hotlinebling.substring(0, 2).contains("Ps") && mHideLive)
                                    f.remove(i); //this is a live story
                                else if (hotlinebling.substring(0, 2).contains("Fl") && mHideDiscover)
                                    f.remove(i); //discover
                            }

                            setObjectField(param.thisObject, "f", f);
                        }
                    }
                });

                Class<?> WR = findClass("WR", lpparam.classLoader);
                findAndHookConstructor(WR, String.class, boolean.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[1] = true;
                    }
                });

                findAndHookMethod("ai$d", lpparam.classLoader, "a", long[].class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if(mVibrateChange) {
                            long[] test;
                            String[] sa = vibrationPattern.split(",");
                            test = new long[sa.length + 1];
                            test[0] = 0;
                            for (int i = 1; i < sa.length; i++)
                                test[i] = Long.parseLong(sa[i - 1]);
                            param.args[0] = test;
                            Logger.log("Successfully set the vibration pattern!");
                            Logger.log(vibrationPattern);
                            Logger.log(Arrays.toString(test));
                        }
                    }
                });
        /*findAndHookMethod("com.snapchat.android.Timber", lpparam.classLoader, "c", String.class, String.class, Object[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("TIMBER: " + param.args[0] + " : " + param.args[1], true);
            }
        });*/
        for (String s: Obfuscator.ROOTDETECTOR_METHODS) {
            findAndHookMethod(Obfuscator.ROOTDETECTOR_CLASS, lpparam.classLoader, s, XC_MethodReplacement.returnConstant(false));
            Logger.log("ROOTCHECK: " + s, true);
        }
        findAndHookMethod("adq", lpparam.classLoader, "h", String.class, XC_MethodReplacement.DO_NOTHING);
        findAndHookMethod("android.media.MediaRecorder", lpparam.classLoader, "setMaxDuration", int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = 120000;
                    }
                });

        final Class<?> receivedSnapClass = findClass(Obfuscator.save.RECEIVEDSNAP_CLASS, lpparam.classLoader);
        try{
            XposedHelpers.setStaticIntField(receivedSnapClass, "SECOND_MAX_VIDEO_DURATION", 99999);
            Logger.log("SECOND_MAX_VIDEO_DURATION set over 10", true);
		} catch (Throwable t){
			Logger.log("SECOND_MAX_VIDEO_DURATION set over 10 failed :(",true);
			Logger.log(t.toString());
        } /*For viewing longer videos?*/

        XC_MethodHook initHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                SnapContext = (Activity) param.thisObject;
                prefs.reload();
                refreshPreferences();
                //SNAPPREFS
                Saving.initSaving(lpparam, mResources, SnapContext);
                Lens.initLens(lpparam, mResources, SnapContext);
                File vfilters = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs/VisualFilters/xpro_map.png");
                if(vfilters.exists()){
                    VisualFilters.initVisualFilters(lpparam);
                } else {
                    Toast.makeText(context, "VisualFilter files are missing, download them!", Toast.LENGTH_SHORT).show();
                }
                if (mMultiFilterBoolean == true){
                    MultiFilter.initMultiFilter(lpparam, mResources, SnapContext);
                }
                if (mDiscoverSnap == true) {
                    DataSaving.blockDsnap(lpparam);
                }
                if (mDiscoverUI == true) {
                    DataSaving.blockFromUi(lpparam);
                }
                if (mSpeed == true) {
                    Spoofing.initSpeed(lpparam, SnapContext);
                }
                if (mLocation == true) {
                    Spoofing.initLocation(lpparam, SnapContext);
                }
                if (mWeather == true) {
                    Spoofing.initWeather(lpparam, SnapContext);
                }
                if (mPaintTools == true){
                    PaintTools.initPaint(lpparam, mResources);
                }
                if (mTimerCounter == true){
                    Misc.initTimer(lpparam, mResources);
                }
                if (mChatAutoSave == true){
                    Chat.initTextSave(lpparam, mResources);
                }
                if (mChatImageSave == true){
                    Chat.initImageSave(lpparam, mResources);
                }
                getEditText(lpparam);
                findAndHookMethod(Obfuscator.save.SCREENSHOTDETECTOR_CLASS, lpparam.classLoader, Obfuscator.save.SCREENSHOTDETECTOR_RUN, LinkedHashMap.class, XC_MethodReplacement.DO_NOTHING);
                findAndHookMethod(Obfuscator.save.SNAPSTATEMESSAGE_CLASS, lpparam.classLoader, Obfuscator.save.SNAPSTATEMESSAGE_SETSCREENSHOTCOUNT, Long.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = 0L;
                        Logger.log("StateBuilder.setScreenshotCount set to 0L", true);
                    }
                });
                if (mCustomSticker == true) {
                    Stickers.initStickers(lpparam, modRes, SnapContext);
                }
            }
        };

        findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, initHook);
        findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onResume", initHook);

        // VanillaCaptionEditText was moved from an inner-class to a separate class in 8.1.0
        String vanillaCaptionEditTextClassName = "com.snapchat.android.ui.caption.VanillaCaptionEditText";
        hookAllConstructors(findClass(vanillaCaptionEditTextClassName, lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Common.CAPTION_UNLIMITED_VANILLA) {
                    XposedUtils.log("Unlimited vanilla captions");
                    EditText vanillaCaptionEditText = (EditText) param.thisObject;
                    // Set single lines mode to false
                    vanillaCaptionEditText.setSingleLine(false);

                    // Remove actionDone IME option, by only setting flagNoExtractUi
                    vanillaCaptionEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    // Remove listener hiding keyboard when enter is pressed by setting the listener to null
                    vanillaCaptionEditText.setOnEditorActionListener(null);
                    // Remove listener for cutting of text when the first line is full by setting the text change listeners list to null
                    setObjectField(vanillaCaptionEditText, "mListeners", null);
                }
            }
        });

        // FatCaptionEditText was moved from an inner-class to a separate class in 8.1.0
        String fatCaptionEditTextClassName = "com.snapchat.android.ui.caption.FatCaptionEditText";
        hookAllConstructors(findClass(fatCaptionEditTextClassName, lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Common.CAPTION_UNLIMITED_FAT) {
                    XposedUtils.log("Unlimited fat captions");
                    EditText fatCaptionEditText = (EditText) param.thisObject;
                    // Remove InputFilter with character limit
                    fatCaptionEditText.setFilters(new InputFilter[0]);

                    // Remove actionDone IME option, by only setting flagNoExtractUi
                    fatCaptionEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    // Remove listener hiding keyboard when enter is pressed by setting the listener to null
                    fatCaptionEditText.setOnEditorActionListener(null);
                    // Remove listener for removing new lines by setting the text change listeners list to null
                    setObjectField(fatCaptionEditText, "mListeners", null);
                }
            }
        });
        //SNAPSHARE
        Sharing.initSharing(lpparam, mResources);
        //SNAPPREFS
        if (hideBf == true) {
            findAndHookMethod("com.snapchat.android.model.Friend", lpparam.classLoader, Obfuscator.FRIENDS_BF, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param)
                        throws Throwable {
                    //logging("Snap Prefs: Removing Best-friends");
                    return false;
                }
            });
        }
		/*if (hideRecent == true){
        findAndHookMethod(Common.Class_Friend, lpparam.classLoader, Common.Method_Recent, new XC_MethodReplacement(){
		@Override
		protected Object replaceHookedMethod(MethodHookParam param)
				throws Throwable {
			logging("Snap Prefs: Removing Recents");
			return false;
        }
		});
		}*/
        if (mCustomFilterBoolean == true) {
            addFilter(lpparam);
        }
        if (selectAll == true) {
            HookSendList.initSelectAll(lpparam);
        }


            }
        });
    }


    private void addFilter(LoadPackageParam lpparam) {
        //Replaces the batteryfilter with our custom one
        findAndHookMethod(ImageView.class, "setImageResource", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);
                    ImageView iv = (ImageView) param.thisObject;
                    int resId = (Integer) param.args[0];
                    if (iv != null)
                        if (iv.getContext().getPackageName().equals("com.snapchat.android"))
                            if (resId == iv.getContext().getResources().getIdentifier("camera_batteryfilter_full", "drawable", "com.snapchat.android"))
                                if (mCustomFilterLocation == null) {
                                    iv.setImageDrawable(modRes.getDrawable(R.drawable.custom_filter_1));
                                    Logger.log("Replaced batteryfilter from R.drawable", true);
                                } else {
                                    if (mCustomFilterType == 0) {
                                        iv.setImageDrawable(Drawable.createFromPath(mCustomFilterLocation + "/fullscreen_filter.png"));
                                        //iv.setImageDrawable(modRes.getDrawable(R.drawable.imsafe));
                                    } else if (mCustomFilterType == 1) {
                                        //iv.setImageDrawable(modRes.getDrawable(R.drawable.imsafe));
                                        iv.setImageDrawable(Drawable.createFromPath(mCustomFilterLocation + "/banner_filter.png"));
                                    }
                                    Logger.log("Replaced batteryfilter from " + mCustomFilterLocation + " Type: " + mCustomFilterType, true);
                                }
                    //else if (resId == iv.getContext().getResources().getIdentifier("camera_batteryfilter_empty", "drawable", "com.snapchat.android"))
                    //    iv.setImageDrawable(modRes.getDrawable(R.drawable.custom_filter_1)); quick switch to a 2nd filter?
                } catch (Throwable t) {
                    XposedBridge.log(t);
                }
            }
        });
        //Used to emulate the battery status as being FULL -> above 90%
        final Class<?> batteryInfoProviderEnum = findClass("com.snapchat.android.location.smartFilterProviders.BatteryInfoProvider$BatteryLevel", lpparam.classLoader);
        findAndHookMethod(Obfuscator.spoofing.BATTERY_FILTER, lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object battery = getStaticObjectField(batteryInfoProviderEnum, "FULL_BATTERY");
                param.setResult(battery);
            }
        });
    }

    private void printSettings() {

        Logger.log("\nTo see the advanced output enable debugging mode in the Support tab", true);

        logging("\n~~~~~~~~~~~~ SNAPPREFS SETTINGS");
        logging("FullCaption: " + fullCaption);
        logging("SelectAll: " + selectAll);
        logging("SelectStory: " + selectStory);
        logging("SelectVenue: " + selectVenue);
        logging("HideBF: " + hideBf);
        logging("HideRecent: " + hideRecent);
        logging("ShouldAddGhost: " + shouldAddGhost);
        logging("TxtColours: " + txtcolours);
        logging("BgColours: " + bgcolours);
        logging("Size: " + size);
        logging("Transparency: " + transparency);
        logging("Rainbow: " + rainbow);
        logging("Background Transparency: " + bg_transparency);
        logging("TextStyle: " + txtstyle);
        logging("TextGravity: " + txtgravity);
        logging("mTimerCounter: " + mTimerCounter);
        logging("mChatAutoSave: " + mChatAutoSave);
        logging("mChatImageSave: " + mChatImageSave);
        logging("mPaintTools: " + mPaintTools);
        logging("CustomFilters: " + mCustomFilterBoolean);
        logging("MultiFilters: " + mMultiFilterBoolean);
        logging("CustomFiltersLocation: " + mCustomFilterLocation);
        logging("CustomFilterType: " + mCustomFilterType);
        logging("mSpeed: " + mSpeed);
        logging("mWeather: " + mWeather);
        logging("mLocation: " + mLocation);
        logging("mDiscoverSnap: " + mDiscoverSnap);
        logging("mDiscoverUI: " + mDiscoverUI);
        logging("mCustomSticker: " + mCustomSticker);
        logging("mReplay: " + mReplay);
        logging("mStealth: " + mStealth);
        logging("mTyping: " + mTyping);
        logging("mColours: " + mColours);
        logging("*****Debugging: " + debug + " *****");
        logging("mLicense: " + mLicense);
        logging("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Logger.setDebuggingEnabled(mDebugging);

        logging("----------------------- SAVING SETTINGS -----------------------");
        logging("Preferences have changed:");
        String[] saveModes = {"SAVE_AUTO", "SAVE_S2S", "DO_NOT_SAVE"};
        logging("~ mModeSnapImage: " + saveModes[mModeSnapImage]);
        logging("~ mModeSnapVideo: " + saveModes[mModeSnapVideo]);
        logging("~ mModeStoryImage: " + saveModes[mModeStoryImage]);
        logging("~ mModeStoryVideo: " + saveModes[mModeStoryVideo]);
        logging("~ mOverlays: " + mOverlays);
        logging("~ mTimerMinimum: " + mTimerMinimum);
        logging("~ mToastEnabled: " + mToastEnabled);
        logging("~ mVibrationEnabled: " + mVibrationEnabled);
        logging("~ mToastLength: " + mToastLength);
        logging("~ mSavePath: " + mSavePath);
        logging("~ mSaveSentSnaps: " + mSaveSentSnaps);
        logging("~ mSortByCategory: " + mSortByCategory);
        logging("~ mSortByUsername: " + mSortByUsername);
        logging("~ mTimerUnlimited: " + mTimerUnlimited);
        logging("~ mHideTimerStory: " + mHideTimerStory);
        logging("~ mLoopingVids: " + mLoopingVids);
        logging("~ mHideTimer: " + mHideTimer);
    }

    public void getEditText(LoadPackageParam lpparam) {
        this.CaptionEditText = XposedHelpers.findClass("com.snapchat.android.ui.caption.CaptionEditText", lpparam.classLoader);
        XposedBridge.hookAllConstructors(this.CaptionEditText, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws PackageManager.NameNotFoundException {
                refreshPreferences();
                editText = (EditText) param.thisObject;
            }
        });
    }

    public void fullScreenFilter(InitPackageResourcesParam resparam) {
        resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "battery_view", new XC_LayoutInflated() {
            LinearLayout.LayoutParams batteryLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                View battery = (View) liparam.view.findViewById(liparam.res.getIdentifier("battery_icon", "id", "com.snapchat.android"));
                battery.setLayoutParams(batteryLayoutParams);
                battery.setPadding(0, 0, 0, 0);
                Logger.log("fullScreenFilter", true);
            }
        });
    }

    public void addSnapprefsBtn(InitPackageResourcesParam resparam){
        try {
            resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "stories", new XC_LayoutInflated() {
                public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                    final RelativeLayout relativeLayout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("top_panel", "id", Common.PACKAGE_SNAP));
                    final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("myfriends_action_bar_search_button", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    layoutParams.topMargin = px(8.0f);
                    layoutParams.rightMargin = px(115.0f);
                    final ImageView spbtn = new ImageView(SnapContext);
                    //spbtn.setBackgroundColor(Color.GREEN);
                    spbtn.setImageDrawable(mResources.getDrawable(R.drawable.logo));
                    //spbtn.setScaleX((float) 0.75);
                    //spbtn.setScaleY((float) 0.75);
                    Logger.log("Adding Snapprefs button to story section");
                    final Context myContext = SnapContext.createPackageContext("com.marz.snapprefs", Context.CONTEXT_IGNORE_SECURITY);

                    spbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Dialogs.MainDialog(SnapContext, editText);
                            //logging("SnapPrefs: Displaying MainDialog");
                            AlertDialog.Builder alert = new AlertDialog.Builder(SnapContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            try {
                                View layout = LayoutInflater.from(myContext).inflate(R.layout.newdialog_layout, null);
                                alert.setView(layout);

                                Button editFavs = (Button) layout.findViewById(R.id.button3);
                                Button editBlocked = (Button) layout.findViewById(R.id.button4);
                                CheckBox removeLive = (CheckBox) layout.findViewById(R.id.checkBox3);
                                CheckBox removeDiscover = (CheckBox) layout.findViewById(R.id.checkBox4);
                                CheckBox showFavsOnTop = (CheckBox) layout.findViewById(R.id.checkBox5);
                                final CheckBox enableBlocked = (CheckBox) layout.findViewById(R.id.checkBox2);
                                final CheckBox enableFavorites = (CheckBox) layout.findViewById(R.id.checkBox);

                                if (mHideLive)
                                    removeLive.setChecked(true);
                                if (mHideDiscover)
                                    removeDiscover.setChecked(true);
                                if (mHidePeople)
                                    enableBlocked.setChecked(true);
                                if (mShowFavorites)
                                    enableFavorites.setChecked(true);
                                if (mShowFavsOnTop)
                                    showFavsOnTop.setChecked(true);

                                editFavs.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        AlertDialog.Builder alert = new AlertDialog.Builder(SnapContext);
                                        View layout = LayoutInflater.from(myContext).inflate(R.layout.edit_favs_dialog, null);
                                        alert.setView(layout);
                                        final String existingNames = favoritesList;
                                        final EditText names = (EditText) layout.findViewById(R.id.editText);
                                        alert.setTitle("Favorite Usernames");
                                        Logger.log(existingNames);
                                        if (!existingNames.equals("") && existingNames != null && existingNames.length() > 0) {
                                            names.setText(existingNames);
                                        }
                                        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                String newpat = names.getText().toString();
                                                if (!newpat.equals(existingNames)) {
                                                    //remove spaces
                                                    newpat = newpat.replaceAll(" ", "");
                                                    favoritesList = newpat;
                                                    //pref.setSummary(newpat);
                                                }
                                            }
                                        });
                                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                        AlertDialog alertDialog = alert.create();

                                        // show it
                                        alertDialog.show();
                                    }
                                });

                                editBlocked.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        AlertDialog.Builder alert = new AlertDialog.Builder(SnapContext);
                                        View layout = LayoutInflater.from(myContext).inflate(R.layout.edit_blocked_dialog, null);
                                        alert.setView(layout);
                                        final String existingNames = peopleToHide;
                                        final EditText names = (EditText) layout.findViewById(R.id.editText);
                                        alert.setTitle("Blocked Story Usernames");
                                        Logger.log(existingNames);
                                        if (!existingNames.equals("") && existingNames != null && existingNames.length() > 0) {
                                            names.setText(existingNames);
                                        }
                                        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                String newpat = names.getText().toString();
                                                if (!newpat.equals(existingNames)) {
                                                    //remove spaces
                                                    newpat = newpat.replaceAll(" ", "");
                                                    peopleToHide = newpat;
                                                    //pref.setSummary(newpat);
                                                }
                                            }
                                        });
                                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                        AlertDialog alertDialog = alert.create();

                                        // show it
                                        alertDialog.show();
                                    }
                                });

                                removeLive.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        if (((CheckBox) v).isChecked()) {
                                            mHideLive = true;
                                        }else {
                                            mHideLive = false;
                                        }
                                    }
                                });

                                removeDiscover.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        if (((CheckBox) v).isChecked()) {
                                            mHideDiscover = true;
                                        }else {
                                            mHideDiscover = false;
                                        }
                                    }
                                });

                                enableBlocked.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        if (((CheckBox) v).isChecked()) {
                                            mHidePeople = true;
                                            if(enableFavorites.isChecked()) {
                                                enableFavorites.setChecked(false);
                                                mShowFavorites = false;
                                                Toast.makeText(SnapContext, "Whitelist and Blacklist cannot be on at the same time", Toast.LENGTH_SHORT).show();
                                            }
                                        }else {
                                            mHidePeople = false;
                                        }
                                    }
                                });

                                enableFavorites.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        if (((CheckBox) v).isChecked()) {
                                            mShowFavorites = true;
                                            if(enableBlocked.isChecked()) {
                                                enableBlocked.setChecked(false);
                                                mHidePeople = false;
                                                Toast.makeText(SnapContext, "Whitelist and Blacklist cannot be on at the same time", Toast.LENGTH_SHORT).show();
                                            }
                                        }else {
                                            mShowFavorites = false;
                                        }
                                    }
                                });

                                showFavsOnTop.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        if (((CheckBox) v).isChecked()) {
                                            mShowFavsOnTop = true;
                                        }else {
                                            mShowFavsOnTop = false;
                                        }
                                    }
                                });

                            } catch (Exception e) {
                                Logger.log("could not inflate view");
                            }
                            //alert.setTitle("Snapprefs Story Settings");
                            alert.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    FileUtils.writeToSDFile(mHideDiscover+"#"+mHideLive+"#"+mHidePeople+"#"+mShowFavorites+"#"+peopleToHide+"#"+favoritesList+"#"+mShowFavsOnTop, "storysettings");
                                    Toast.makeText(SnapContext, "Pull to refresh to view changes", Toast.LENGTH_SHORT).show();
                                }
                            });
                            AlertDialog alertDialog = alert.create();
                            alertDialog.show();
                        }
                    });

                    SnapContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            relativeLayout.addView(spbtn, layoutParams);
                        }
                    });
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void addGhost(InitPackageResourcesParam resparam) {
        resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "snap_preview", new XC_LayoutInflated() {
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                final RelativeLayout relativeLayout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_header", "id", Common.PACKAGE_SNAP)).getParent();
                final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.topMargin = px(45.0f);
                layoutParams.leftMargin = px(10.0f);
                final ImageButton ghost = new ImageButton(SnapContext);
                ghost.setBackgroundColor(0);
                ghost.setImageDrawable(mResources.getDrawable(R.drawable.triangle));
                ghost.setScaleX((float) 0.75);
                ghost.setScaleY((float) 0.75);
                ghost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialogs.MainDialog(SnapContext, editText);
                        logging("SnapPrefs: Displaying MainDialog");
                    }
                });
                final RelativeLayout.LayoutParams paramsSpeed = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                paramsSpeed.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                paramsSpeed.topMargin = px(90.0f);
                paramsSpeed.leftMargin = px(10.0f);
                final ImageButton speed = new ImageButton(SnapContext);
                speed.setBackgroundColor(0);
                speed.setImageDrawable(mResources.getDrawable(R.drawable.speed));
                speed.setScaleX((float) 0.4);
                speed.setScaleY((float) 0.4);
                speed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialogs.SpeedDialog(SnapContext);
                        logging("SnapPrefs: Displaying SpeedDialog");
                    }
                });
                final RelativeLayout.LayoutParams paramsWeather = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                paramsWeather.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                paramsWeather.topMargin = px(180.0f);
                paramsWeather.leftMargin = px(10.0f);
                final ImageButton weather = new ImageButton(SnapContext);
                weather.setBackgroundColor(0);
                weather.setImageDrawable(mResources.getDrawable(R.drawable.weather));
                weather.setScaleX((float) 0.4);
                weather.setScaleY((float) 0.4);
                weather.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialogs.WeatherDialog(SnapContext);
                        logging("SnapPrefs: Displaying WeatherDialog");
                    }
                });
                final RelativeLayout.LayoutParams paramsLocation = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                paramsLocation.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                paramsLocation.topMargin = px(135.0f);
                paramsLocation.leftMargin = px(10.0f);
                final ImageButton location = new ImageButton(SnapContext);
                location.setBackgroundColor(0);
                location.setImageDrawable(mResources.getDrawable(R.drawable.location));
                location.setScaleX((float) 0.4);
                location.setScaleY((float) 0.4);
                location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.marz.snapprefs", "com.marz.snapprefs.MapsActivity"));
                        SnapContext.startActivity(intent);
                        logging("SnapPrefs: Displaying Map");
                    }
                });
                SnapContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mColours == true) {
                            relativeLayout.addView(ghost, layoutParams);
                        }
                        if (mSpeed == true) {
                            relativeLayout.addView(speed, paramsSpeed);
                        }
                        if (mLocation == true) {
                            relativeLayout.addView(location, paramsLocation);
                        }
                        if (mWeather == true) {
                            relativeLayout.addView(weather, paramsWeather);
                        }
                    }
                });
            }
        });
    }
}