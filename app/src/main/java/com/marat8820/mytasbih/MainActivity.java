package com.marat8820.mytasbih;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {
    static final String PREFS = "MyTasbihPrefs";
    static final String CHANNEL_ID = "mytasbih_reminders";
    private final int cream = Color.rgb(250, 247, 239);
    private final int green = Color.rgb(35, 91, 66);
    private final int darkGreen = Color.rgb(24, 66, 48);
    private final int gold = Color.rgb(190, 151, 75);
    private final int ink = Color.rgb(55, 61, 52);

    private SharedPreferences prefs;
    private TextView countView, targetView, remainingView, progressView, zikrView, arabicView, reminderView;
    private int count, target;
    private String zikrName, zikrArabic;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        count = prefs.getInt("count", 0);
        target = prefs.getInt("target", 33);
        zikrName = prefs.getString("zikr_name", "Субханаллах");
        zikrArabic = prefs.getString("zikr_arabic", "سُبْحَانَ اللَّهِ");
        ReminderReceiver.createChannel(this);
        askNotificationPermission();
        buildUi();
        updateUi();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 701);
        }
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(cream);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(18), dp(22), dp(24));
        scroll.addView(root);

        TextView arabicTitle = text("مسبحتي", 32, green, true);
        arabicTitle.setTypeface(Typeface.SERIF, Typeface.BOLD);
        root.addView(arabicTitle, lp(-1, -2));

        TextView title = text("MY TASBIH", 22, green, true);
        title.setLetterSpacing(0.18f);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title, lp(-1, -2));

        TextView subtitle = text("Your daily dhikr companion", 14, ink, false);
        subtitle.setPadding(0, dp(5), 0, dp(22));
        root.addView(subtitle, lp(-1, -2));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(18), dp(22), dp(18), dp(20));
        card.setBackground(bg(Color.WHITE, dp(22), Color.rgb(232, 222, 198)));
        root.addView(card, lp(-1, -2));

        TextView label = text("YOUR DAILY DHIKR", 12, gold, true);
        label.setLetterSpacing(0.12f);
        card.addView(label, lp(-1, -2));

        zikrView = text(zikrName, 21, green, true);
        zikrView.setPadding(0, dp(10), 0, dp(2));
        card.addView(zikrView, lp(-1, -2));

        arabicView = text(zikrArabic, 25, ink, false);
        arabicView.setTypeface(Typeface.SERIF);
        arabicView.setPadding(0, dp(2), 0, dp(20));
        card.addView(arabicView, lp(-1, -2));

        targetView = text("", 13, ink, false);
        card.addView(targetView, lp(-1, -2));

        countView = text("0", 68, darkGreen, true);
        countView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(countView, lp(-1, -2));

        remainingView = text("", 14, Color.rgb(113, 117, 105), false);
        remainingView.setPadding(0, 0, 0, dp(12));
        card.addView(remainingView, lp(-1, -2));

        progressView = text("", 17, gold, false);
        progressView.setPadding(0, 0, 0, dp(18));
        card.addView(progressView, lp(-1, -2));

        Button countButton = button("＋  СЧИТАТЬ ЗИКР", green, Color.WHITE, 78);
        countButton.setTextSize(18);
        countButton.setOnClickListener(v -> {
            count++;
            save();
            if (target > 0 && count == target)
                Toast.makeText(this, "Цель достигнута! Ма ша Аллах!", Toast.LENGTH_LONG).show();
        });
        card.addView(countButton, lp(-1, dp(78)));

        Button changeZikr = button("Выбрать зикр", Color.rgb(239, 234, 220), green, 48);
        changeZikr.setOnClickListener(v -> chooseZikr());
        LinearLayout.LayoutParams p = lp(-1, dp(48)); p.topMargin = dp(14);
        card.addView(changeZikr, p);

        Button reset = button("↻  Сбросить счётчик", Color.WHITE, green, 48);
        reset.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Сбросить счётчик?")
                .setMessage("Текущий результат будет обнулён.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сбросить", (d, w) -> { count = 0; save(); })
                .show());
        addAction(root, reset);

        Button targetButton = button("◎  Изменить цель", Color.WHITE, green, 48);
        targetButton.setOnClickListener(v -> chooseTarget());
        addAction(root, targetButton);

        Button reminder = button("◷  Добавить напоминание", Color.WHITE, green, 48);
        reminder.setOnClickListener(v -> chooseReminder());
        addAction(root, reminder);

        Button cancelReminder = button("Отключить напоминание", Color.WHITE, green, 48);
        cancelReminder.setOnClickListener(v -> cancelReminder());
        addAction(root, cancelReminder);

        reminderView = text("", 13, ink, false);
        reminderView.setPadding(0, dp(12), 0, 0);
        root.addView(reminderView, lp(-1, -2));

        setContentView(scroll);
        updateReminderText();
    }

    private void addAction(LinearLayout root, Button button) {
        LinearLayout.LayoutParams p = lp(-1, dp(48));
        p.topMargin = dp(9);
        root.addView(button, p);
    }

    private void chooseZikr() {
        String[] names = {"Субханаллах", "Альхамдулиллях", "Аллаху Акбар", "Астагфируллах"};
        String[] arabic = {"سُبْحَانَ اللَّهِ", "الْحَمْدُ لِلَّهِ", "اللَّهُ أَكْبَرُ", "أَسْتَغْفِرُ اللَّهَ"};
        new AlertDialog.Builder(this).setTitle("Выберите зикр").setItems(names, (d, which) -> {
            zikrName = names[which];
            zikrArabic = arabic[which];
            save();
            buildUi();
            updateUi();
        }).show();
    }

    private void chooseTarget() {
        String[] labels = {"33", "99", "100", "500", "1000", "Без цели"};
        int[] values = {33, 99, 100, 500, 1000, 0};
        new AlertDialog.Builder(this).setTitle("Цель зикра").setItems(labels, (d, which) -> {
            target = values[which];
            save();
        }).show();
    }

    private void chooseReminder() {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            Calendar next = Calendar.getInstance();
            next.set(Calendar.HOUR_OF_DAY, hour);
            next.set(Calendar.MINUTE, minute);
            next.set(Calendar.SECOND, 0);
            next.set(Calendar.MILLISECOND, 0);
            if (next.getTimeInMillis() <= System.currentTimeMillis()) next.add(Calendar.DAY_OF_YEAR, 1);

            int id = prefs.getInt("next_reminder_id", 1000);
            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("reminder_id", id);
            PendingIntent pi = PendingIntent.getBroadcast(this, id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());
            AlarmManager alarms = (AlarmManager) getSystemService(ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= 31 && alarms != null && !alarms.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Разрешить точные напоминания?")
                        .setMessage("Android может попросить разрешение на точные будильники. Если не разрешить, напоминание может прийти с задержкой.")
                        .setNegativeButton("Продолжить без точного времени", (d, w) ->
                                scheduleInexact(alarms, next.getTimeInMillis(), pi, id, hour, minute))
                        .setPositiveButton("Открыть настройки", (d, w) -> {
                            Intent settings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(settings);
                            scheduleInexact(alarms, next.getTimeInMillis(), pi, id, hour, minute);
                        }).show();
            } else if (alarms != null) {
                try {
                    if (Build.VERSION.SDK_INT >= 23)
                        alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
                    else alarms.setExact(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
                    storeReminder(id, hour, minute);
                } catch (SecurityException ex) {
                    scheduleInexact(alarms, next.getTimeInMillis(), pi, id, hour, minute);
                }
            }
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
    }

    private void scheduleInexact(AlarmManager alarms, long time, PendingIntent pi, int id, int hour, int minute) {
        if (alarms != null) {
            alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pi);
            storeReminder(id, hour, minute);
        }
    }

    private void storeReminder(int id, int hour, int minute) {
        prefs.edit().putInt("next_reminder_id", id + 1)
                .putInt("reminder_hour_" + id, hour).putInt("reminder_minute_" + id, minute)
                .putBoolean("reminder_enabled_" + id, true).apply();
        updateReminderText();
        Toast.makeText(this, String.format(Locale.getDefault(), "Напоминание установлено: %02d:%02d", hour, minute),
                Toast.LENGTH_LONG).show();
    }

    private void cancelReminder() {
        int id = prefs.getInt("next_reminder_id", 1000) - 1;
        if (id < 1000) {
            Toast.makeText(this, "Нет сохранённых напоминаний для отключения", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());
        AlarmManager alarms = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarms != null) alarms.cancel(pi);
        prefs.edit().putBoolean("reminder_enabled_" + id, false).apply();
        updateReminderText();
        Toast.makeText(this, "Последнее напоминание отключено", Toast.LENGTH_SHORT).show();
    }

    private void updateReminderText() {
        if (reminderView == null) return;
        int id = prefs.getInt("next_reminder_id", 1000) - 1;
        if (id >= 1000 && prefs.getBoolean("reminder_enabled_" + id, false)) {
            reminderView.setText(String.format(Locale.getDefault(), "Последнее напоминание: %02d:%02d",
                    prefs.getInt("reminder_hour_" + id, 20), prefs.getInt("reminder_minute_" + id, 0)));
        } else reminderView.setText("Напоминания не настроены");
    }

    private int immutableFlag() {
        return Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0;
    }

    private void save() {
        prefs.edit().putInt("count", count).putInt("target", target)
                .putString("zikr_name", zikrName).putString("zikr_arabic", zikrArabic).apply();
        updateUi();
    }

    private void updateUi() {
        if (countView == null) return;
        countView.setText(String.valueOf(count));
        targetView.setText("ЦЕЛЬ  •  " + (target == 0 ? "∞" : target));
        remainingView.setText(target == 0 ? "Продолжайте в своём темпе" : Math.max(0, target - count) + " осталось");
        int filled = target == 0 ? count % 10 : Math.min(10, (int)(count * 10.0 / Math.max(1, target)));
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < 10; i++) dots.append(i < filled ? "● " : "○ ");
        progressView.setText(dots.toString().trim());
        zikrView.setText(zikrName);
        arabicView.setText(zikrArabic);
    }

    private TextView text(String value, int size, int color, boolean centered) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextColor(color); t.setTextSize(size);
        t.setGravity(centered ? Gravity.CENTER : Gravity.CENTER);
        return t;
    }

    private Button button(String value, int background, int foreground, int heightDp) {
        Button b = new Button(this);
        b.setText(value); b.setAllCaps(false); b.setTextColor(foreground);
        b.setTextSize(15); b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(background); shape.setCornerRadius(dp(15));
        shape.setStroke(dp(1), background == green ? gold : Color.rgb(230, 224, 207));
        b.setBackground(shape);
        return b;
    }

    private GradientDrawable bg(int color, int radius, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius); d.setStroke(dp(1), stroke);
        return d;
    }

    private LinearLayout.LayoutParams lp(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
