/*
 * Copyright (c) 2018 denua.
 */

package cn.denua.v2ex;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cn.denua.v2ex.base.App;
import cn.denua.v2ex.model.Account;
import cn.denua.v2ex.utils.TimeUtil;

/*
 * App 配置相关
 *
 * @user denua
 * @date 2018/10/18
 */
public class Config {

    private static HashMap<ConfigRefEnum, Serializable> CONFIG = new HashMap<>();

    private static Account sAccount;

    /**
     * 用于配置字体与UI缩放
     */
    private static Configuration mConfig;

    static final ArrayList<Tab> HOME_TAB_DEFAULT = new ArrayList<Tab>(){{
        add(new Tab(TabEnum.LATEST, "",TabEnum.LATEST.getTitle()));
        add(new Tab(TabEnum.HOT, "", TabEnum.HOT.getTitle()));
        add(new Tab(TabEnum.TAB, "tech","技 术"));
        add(new Tab(TabEnum.TAB, "creative","好 玩"));
    }};

    public static final ArrayList<Locale> LOCAL_LIST = new ArrayList<Locale>(){{
        add(Locale.CHINA);
        add(Locale.US);
        add(Locale.JAPAN);
    }};

    public static Account getAccount() {
        return sAccount;
    }

    public static void setAccount(Account account){
        sAccount = account;
    }

    /**
     * 初始化配置, 从 SharedPreferences 文件中读取配置
     * @param context the context
     */
    public static void init(Context context){

        if (mConfig == null){
            mConfig = context.getResources().getConfiguration();
        }
        loadConfig(context);
        restoreAccount();

        if (getConfig(ConfigRefEnum.CONFIG_AUTO_NIGHT_THEME)){
            String[] autoNightThemeTime =
                    ((String)getConfig(ConfigRefEnum.CONFIG_AUTO_NIGHT_TIME)).split("_");
            ToastUtils.showShort( autoNightThemeTime[0] + "-" + autoNightThemeTime[1]);
            if (autoNightThemeTime.length == 2 &&
                    TimeUtil.isNowBetweenTimeSpanOfDay(autoNightThemeTime[0], autoNightThemeTime[1])){
                ToastUtils.showShort("黑暗主题已启用, " + autoNightThemeTime[0] + "-" + autoNightThemeTime[1]);
                setConfig(ConfigRefEnum.CONFIG_THEME, "DarkTheme");
            }
        }

    }

    public static void saveState(Bundle bundle){

    }

    public static void restoreState(Bundle bundle){

    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T getConfig(ConfigRefEnum key, @Nullable T defaultValue){
        T result = (T) CONFIG.get(key);
        return result == null ? defaultValue : result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getConfig(ConfigRefEnum key){
        T result = (T) CONFIG.get(key);
        if (result == null){
            result = (T) key.getDefaultValue();
        }
        return result;
    }

    public static <T extends Serializable> void setConfig(ConfigRefEnum key, @Nullable T value){

        CONFIG.put(key, value);
    }

    public static Configuration getConfiguration(){
        return mConfig;
    }
    /**
     * 将用户信息 (Account) 以 json 的形式持久化
     *
     * @param context the context
     */
    public static void persistentAccount(Context context){

        SharedPreferences.Editor editor= context.getSharedPreferences(
                ConfigRefEnum.KEY_FILE_CONFIG_PREF.getKey(), Context.MODE_PRIVATE).edit();
        String gsonAccount = new Gson().toJson(sAccount);
        editor.putString(ConfigRefEnum.KEY_ACCOUNT.getKey(), gsonAccount);
        editor.apply();
    }

    /**
     * 从 SharedPreferences 文件中读取以 json 形式保存的用户信息
     */
    private static void restoreAccount() {
        try {
            SharedPreferences editor = App.getApplication().getSharedPreferences(
                    ConfigRefEnum.KEY_FILE_CONFIG_PREF.getKey(), Context.MODE_PRIVATE);
            sAccount = new Gson().fromJson(editor.getString(
                    ConfigRefEnum.KEY_ACCOUNT.getKey(),null), Account.class);
            if (sAccount == null){
                sAccount = new Account();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void loadConfig(Context context){

        SharedPreferences preferences = context.getSharedPreferences(
                Config.getConfig(ConfigRefEnum.CONFIG_PREFERENCE_SETTING_FILE),
                Context.MODE_PRIVATE);
        Map<String, ?> pref = preferences.getAll();
        for (ConfigRefEnum refEnum:ConfigRefEnum.values()){
            String key = refEnum.getKey();
            CONFIG.put(refEnum, pref.get(key) != null
                    ? (Serializable) pref.get(key)
                    : refEnum.getDefaultValue());
        }
        Set<String> homeTabs = preferences.getStringSet(
                ConfigRefEnum.CONFIG_HOME_TAB.getKey(), null);
        if (homeTabs == null){
            CONFIG.put(ConfigRefEnum.CONFIG_HOME_TAB, HOME_TAB_DEFAULT);
            return;
        }
        ArrayList<Tab> tabEnums = new ArrayList<>();
        for (String tab:homeTabs){
            if (tab.startsWith("tab")){
                Tab tabEnum = new Tab(TabEnum.TAB, tab.split("_")[1], tab.split("_")[2]);
                tabEnums.add(tabEnum);
            } else if (tab.startsWith("node")){
                Tab tabEnum = new Tab(TabEnum.NODE, tab.split("_")[1], tab.split("_")[2]);
                tabEnums.add(tabEnum);
            } else{
                TabEnum tabEnum = TabEnum.contains(tab)
                        ? TabEnum.valueOf(tab)
                        : TabEnum.findByDescriptor(tab);
                Tab tab1 = new Tab(tabEnum, null, tabEnum.getTitle());
                tabEnums.add(tab1);
            }
        }
        CONFIG.put(ConfigRefEnum.CONFIG_HOME_TAB, tabEnums);
        mConfig.fontScale =
                mConfig.fontScale * Float.valueOf(Config.getConfig(ConfigRefEnum.CONFIG_FONT_SCALE));
        mConfig.densityDpi = (int) (mConfig.densityDpi
                * Float.valueOf(Config.getConfig(ConfigRefEnum.CONFIG_UI_SCALE)));
    }
}

