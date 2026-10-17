package com.marat8820.mytasbih;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;

// После перезагрузки Android восстанавливает последнее активное напоминание.
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        int id = prefs.getInt("next_reminder_id", 1000) - 1;
        if (id < 1000 || !prefs.getBoolean("reminder_enabled_" + id, false)) return;

        int hour = prefs.getInt("reminder_hour_" + id, 20);
        int minute = prefs.getInt("reminder_minute_" + id, 0);
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) next.add(Calendar.DAY_OF_YEAR, 1);

        Intent alarmIntent = new Intent(context, ReminderReceiver.class);
        alarmIntent.putExtra("reminder_id", id);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getBroadcast(context, id, alarmIntent, flags);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) {
            if (Build.VERSION.SDK_INT >= 23) alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    next.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
            else alarms.setRepeating(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);
        }
    }
}
