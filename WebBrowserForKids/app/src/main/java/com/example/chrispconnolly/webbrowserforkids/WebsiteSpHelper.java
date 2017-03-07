package com.example.chrispconnolly.webbrowserforkids;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WebsiteSpHelper {
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSharedPreferencesEditor;
    static final String WEBSITES = "websites";
    static final String CURFEW_HOUR = "curfew_hour";
    static final String CURFEW_MINUTE = "curfew_minute";
    static final String TIME_LIMIT = "time_limit";
    static final String PASSCODE = "passcode";
    static final String PARENT_MODE = "parent_mode";
    static final String TIME_LEFT = "time_left";
    static final String DAY = "day";

    public WebsiteSpHelper(Context context){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mSharedPreferencesEditor = mSharedPreferences.edit();
    }

    public void insertWebsite(String website){
        String websites = mSharedPreferences.getString(WEBSITES, null);
        websites = (websites == null || websites.isEmpty()) ? website : websites + "," + website;
        mSharedPreferencesEditor.putString(WEBSITES, websites);
        mSharedPreferencesEditor.commit();
    }

    public int deleteWebsite(String website){
        String websitesString = mSharedPreferences.getString(WEBSITES, null);
        if(websitesString.contains("," + website))
            websitesString = websitesString.replace("," + website, "");
        else
            websitesString = websitesString.replace(website, "");
        mSharedPreferencesEditor.putString(WEBSITES, websitesString);
        mSharedPreferencesEditor.commit();
        return 1;
    }

    public String getWebsites(){
        return mSharedPreferences.getString(WEBSITES, null);
    }

    public String getCurfew(){
        return getFormattedTime(mSharedPreferences.getInt(CURFEW_HOUR, 23), mSharedPreferences.getInt(CURFEW_MINUTE, 59));
    }

    public int getCurfewHour(){
        return mSharedPreferences.getInt(CURFEW_HOUR, 12);
    }

    public int getCurfewMinute(){
        return mSharedPreferences.getInt(CURFEW_MINUTE, 0);
    }

    public void setCurfewHour(int hour){
        mSharedPreferencesEditor.putInt(CURFEW_HOUR, hour).commit();
    }

    public void setCurfewMinute(int minute){
        mSharedPreferencesEditor.putInt(CURFEW_MINUTE, minute).commit();
    }

    public long getTimeLimit(){
        return mSharedPreferences.getLong(TIME_LIMIT, 24);
    }

    public void setTimeLimit(long timeLimit){
        mSharedPreferencesEditor.putLong(TIME_LIMIT, timeLimit).commit();
    }

    public long getTimeLeft(){
        return mSharedPreferences.getLong(TIME_LEFT, getTimeLimit()*3600000);
    }

    public void setTimeLeft(long timeLeft){
        mSharedPreferencesEditor.putLong(TIME_LEFT, timeLeft).commit();
    }

    public String getParentPasscode(){
        return mSharedPreferences.getString(PASSCODE, null);
    }

    public void setParentPasscode(String passcode){
        mSharedPreferencesEditor.putString(PASSCODE, passcode).commit();
    }

    public boolean getParentMode(){
        return mSharedPreferences.getBoolean(PARENT_MODE, false);
    }

    public void toggleParentMode()
    {
        mSharedPreferencesEditor.putBoolean(PARENT_MODE, !getParentMode()).commit();
    }

    public Date getDay()
    {
        return new Date(mSharedPreferences.getLong("day", System.currentTimeMillis()));
    }

    public void updateDay(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        if(!simpleDateFormat.format(new Date(System.currentTimeMillis())).equals(simpleDateFormat.format(getDay()))) {
            mSharedPreferencesEditor.putLong(DAY, System.currentTimeMillis()).commit();
            mSharedPreferencesEditor.putLong(TIME_LEFT, getTimeLeft()).commit();
        }
    }

    private String getFormattedTime(int hour, int minute)
    {
        String padMinute = (minute < 10) ? "0" + minute : "" + minute;
        return hour + ":" + padMinute;
    }
}
