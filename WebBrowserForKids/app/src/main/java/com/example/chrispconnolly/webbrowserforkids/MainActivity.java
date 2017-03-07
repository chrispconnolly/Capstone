package com.example.chrispconnolly.webbrowserforkids;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;

import java.net.URL;
import java.util.Calendar;

import icepick.Icepick;
import icepick.State;

public class MainActivity extends AppCompatActivity {
    WebView mWebView;
    WebsiteSpHelper mWebsiteSpHelper;
    GoogleAnalytics mGoogleAnalytics;
    long mTimeLimit;

    @State
    String mUrl;

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setContentView(R.layout.activity_main);

        if(mGoogleAnalytics == null) {
            mGoogleAnalytics = GoogleAnalytics.getInstance(this);
            mGoogleAnalytics.newTracker(R.xml.track_app).enableAutoActivityTracking(true);
        }

        mWebsiteSpHelper = new WebsiteSpHelper(this);
        mTimeLimit = mWebsiteSpHelper.getTimeLimit();
        new CountDownTimer(mWebsiteSpHelper.getTimeLeft(), 1000) {
            public void onTick(long remaining) {
                long timeLimit = mWebsiteSpHelper.getTimeLimit();
                if(mTimeLimit != timeLimit) {
                    remaining = timeLimit * 3600000;
                    mTimeLimit = timeLimit;
                    mWebsiteSpHelper.setTimeLeft(remaining);
                }
                mWebsiteSpHelper.updateDay();
                mWebsiteSpHelper.setTimeLeft(remaining);
                if(!isBeforeCurfew()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.past_curfew), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
            }
            public void onFinish() {
                mWebsiteSpHelper.updateDay();
                Toast.makeText(getApplicationContext(), getString(R.string.past_time_limit), Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        }.start();

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String urlString, Bitmap favicon) {
                String webHost = "";
                try {
                    String[] websites = mWebsiteSpHelper.getWebsites().split(",");
                    webHost = new URL(urlString).getHost();
                    String mobileWebHost = webHost.replace("m.", "www.");
                    for(String website : websites)
                        if(webHost.equals(website) || mobileWebHost.equals(website))
                            return;
                }
                catch(Exception exception){
                    view.stopLoading();
                    Toast.makeText(getApplicationContext(), getString(R.string.unable_to_load) + webHost, Toast.LENGTH_LONG).show();
                    return;
                }
                view.stopLoading();
                Toast.makeText(getApplicationContext(), getString(R.string.ask_parent) + webHost, Toast.LENGTH_LONG).show();
            }
        });

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mUrl = getIntent().getStringExtra("url");
        if(mUrl != null)
            mWebView.loadUrl("http://" + mUrl);
        else {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void startSettingsActivity(MenuItem menuItem){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    public void goBack(MenuItem menuItem){
        if (mWebView.canGoBack())
            mWebView.goBack();
    }

    private boolean isBeforeCurfew(){
        Calendar calendar = Calendar.getInstance();
        if(calendar.HOUR_OF_DAY > mWebsiteSpHelper.getCurfewHour() || (calendar.HOUR_OF_DAY == mWebsiteSpHelper.getCurfewHour() &&
                calendar.MINUTE > mWebsiteSpHelper.getCurfewMinute()))
            return false;
        return true;
    }
}
