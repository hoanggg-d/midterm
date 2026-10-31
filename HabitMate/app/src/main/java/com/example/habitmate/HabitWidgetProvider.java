package com.example.habitmate;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class HabitWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_habit_2x2);

            // Gắn Adapter Service cho ListView
            Intent intentService = new Intent(context, HabitWidgetService.class);
            views.setRemoteAdapter(R.id.widgetListView, intentService);

            // Template PendingIntent khi bấm vào item trong danh sách widget
            Intent clickIntent = new Intent(context, HabitWidgetProvider.class);
            clickIntent.setAction("ACTION_COMPLETE_HABIT");
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(
                    context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            views.setPendingIntentTemplate(R.id.widgetListView, clickPendingIntent);

            // Nút Refresh
            Intent refreshIntent = new Intent(context, HabitWidgetProvider.class);
            refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                    context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.ivWidgetRefresh, refreshPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetListView);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("ACTION_COMPLETE_HABIT".equals(intent.getAction())) {
            int habitId = intent.getIntExtra("EXTRA_HABIT_ID", -1);
            if (habitId != -1) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    AppDatabase db = AppDatabase.getDatabase(context);
                    Habit habit = db.habitDao().getHabitById(habitId);
                    if (habit != null && habit.getCurrentCount() < habit.getTargetCount()) {
                        habit.setCurrentCount(habit.getTargetCount());
                        habit.setStreakDays(habit.getStreakDays() + 1);
                        db.habitDao().updateHabit(habit);

                        // Làm mới lại dữ liệu hiển thị trên Widget ngay lập tức
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                                new android.content.ComponentName(context, HabitWidgetProvider.class));
                        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widgetListView);
                    }
                });
            }
        }
        super.onReceive(context, intent);
    }
}