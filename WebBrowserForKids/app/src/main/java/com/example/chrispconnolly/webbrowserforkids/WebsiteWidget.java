package com.example.chrispconnolly.webbrowserforkids;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class WebsiteWidget extends AppWidgetProvider {
    WebsiteSpHelper mWebsiteSpHelper;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        mWebsiteSpHelper = new WebsiteSpHelper(context);
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, WebsiteWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.website_widget);

            long timeLeft = mWebsiteSpHelper.getTimeLeft();
            long hours = MILLISECONDS.toHours(timeLeft);
            long minutes = MILLISECONDS.toMinutes(timeLeft) - hours*60;
            String timeLeftString = String.format(context.getString(R.string.time_left) +
                    "  %02d:%02d", hours, minutes);

            views.setTextViewText(R.id.widget_textview, timeLeftString);
            views.setOnClickPendingIntent(R.id.widget_textview, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
