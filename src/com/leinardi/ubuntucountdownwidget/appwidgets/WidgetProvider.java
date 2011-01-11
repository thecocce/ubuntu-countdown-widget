/**
 *  Ubuntu Countdown Widget
 *  Copyright (C) 2011 Roberto Leinardi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package com.leinardi.ubuntucountdownwidget.appwidgets;

import com.leinardi.ubuntucountdownwidget.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class WidgetProvider extends AppWidgetProvider {
    private static final String TAG="UbuntuWidget";
    private static final String FORCE_WIDGET_UPDATE = "com.leinardi.ubuntucountdownwidget.FORCE_WIDGET_UPDATE";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(FORCE_WIDGET_UPDATE), 0);

        GregorianCalendar gcal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        Log.d(TAG, gcal.getTime().toLocaleString());
        gcal.set(Calendar.HOUR_OF_DAY, 0);
        gcal.set(Calendar.MINUTE, 0);
        gcal.set(Calendar.SECOND, 0);
        gcal.set(Calendar.MILLISECOND, 0);
        gcal.add(Calendar.DAY_OF_YEAR, 1);
        gcal.getTimeInMillis();
        Log.d(TAG, gcal.getTime().toLocaleString());

        alarmManager.cancel(pi);
        alarmManager.setRepeating(AlarmManager.RTC,
                gcal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pi);

        updateWidget(context);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if(FORCE_WIDGET_UPDATE.equals(action)){
            Log.d(TAG,"broadcast "+FORCE_WIDGET_UPDATE+" catched!");
            updateWidget(context);
        }

        super.onReceive(context, intent);
    }

    public void updateWidget(Context context){
        ComponentName thisWidget=null;
        if(this instanceof Widget1x1Provider){
            thisWidget = new ComponentName(context, Widget1x1Provider.class); 
            Log.d(TAG, "instanceof Widget1x1Provider");
        }else if(this instanceof Widget2x2Provider){
            thisWidget = new ComponentName(context, Widget2x2Provider.class);
            Log.d(TAG, "instanceof Widget2x2Provider");
        }else{
            // TODO Write a better log message
            Log.e(TAG, "instanceof ERROR !!!");
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        updateWidget(context, appWidgetManager, appWidgetIds);
    }

    public void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        Log.d(TAG, "updateWidget");
        GregorianCalendar today = new GregorianCalendar();
        Log.d(TAG, "today: " + today.getTime().toLocaleString());
        GregorianCalendar ubuntuReleaseDay = new GregorianCalendar(2011, Calendar.APRIL, 28);
        Log.d(TAG, "ubuntuReleaseDay: " + ubuntuReleaseDay.getTime().toLocaleString());

        long millisLeft = ubuntuReleaseDay.getTimeInMillis()-today.getTimeInMillis();
        // Only API Level 9 --> TimeUnit.MILLISECONDS.toHours(millisLeft);
        long hoursLeft = millisLeft / (1000 * 60 * 60); 

        long daysLeft = (long) Math.ceil(hoursLeft/24.0);

        Log.d(TAG, "millisLeft: " + millisLeft);
        Log.d(TAG, "hoursLeft: " + hoursLeft);
        Log.d(TAG, "daysLeft: " + daysLeft);

        RemoteViews views=null;

        if(this instanceof Widget1x1Provider){
            views=new RemoteViews(context.getPackageName(), R.layout.appwidget_1x1);   
            Log.d(TAG, "instanceof Widget1x1Provider");
        }else if(this instanceof Widget2x2Provider){
            views=new RemoteViews(context.getPackageName(), R.layout.appwidget_2x2);
            Log.d(TAG, "instanceof Widget2x2Provider");
        }else{
            // TODO Write a better log message
            Log.e(TAG, "instanceof ERROR !!!");
        }

        for(int appWidgetId : appWidgetIds){
            Log.d(TAG, "appWidgetId: " + appWidgetId);
            //            views.setViewVisibility(R.id.progress_bar, View.GONE);
            views.setViewVisibility(R.id.tv_footer, View.VISIBLE);

            if(millisLeft > DateUtils.DAY_IN_MILLIS){
                views.setViewVisibility(R.id.iv_header, View.VISIBLE);
                views.setViewVisibility(R.id.tv_release_big, View.GONE);
                views.setViewVisibility(R.id.iv_logo, View.GONE);
                views.setTextViewText(R.id.tv_counter, daysLeft+"");
                views.setViewVisibility(R.id.tv_counter, View.VISIBLE);
                views.setTextViewText(R.id.tv_footer, context.getString(R.string.days_left));
            }else if(millisLeft<0){
                views.setViewVisibility(R.id.iv_header, View.VISIBLE);
                views.setViewVisibility(R.id.iv_logo, View.GONE);
                views.setViewVisibility(R.id.tv_counter, View.GONE);
                views.setTextViewText(R.id.tv_release_big, context.getString(R.string.release_number));
                views.setViewVisibility(R.id.tv_release_big, View.VISIBLE);
                views.setTextViewText(R.id.tv_footer, context.getString(R.string.its_here));
            }else{
                views.setViewVisibility(R.id.iv_header, View.GONE);
                views.setViewVisibility(R.id.tv_counter, View.GONE);
                views.setViewVisibility(R.id.tv_release_big, View.GONE);
                views.setViewVisibility(R.id.iv_logo, View.VISIBLE);
                views.setTextViewText(R.id.tv_footer, context.getString(R.string.coming_soon));
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

}
