package com.example.habitmate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

public class HabitWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new HabitRemoteViewsFactory(this.getApplicationContext());
    }
}

class HabitRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private List<Habit> habitList = new ArrayList<>();

    public HabitRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDataSetChanged() {
        // Lấy dữ liệu từ Room Database (Đổi từ getInstance thành getDatabase cho khớp)
        AppDatabase db = AppDatabase.getDatabase(context);
        habitList = db.habitDao().getAllHabitsSync();
    }

    @Override
    public void onDestroy() {
        habitList.clear();
    }

    @Override
    public int getCount() {
        return habitList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position < 0 || position >= habitList.size()) return null;

        Habit habit = habitList.get(position);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item_habit);

        views.setTextViewText(R.id.tvWidgetTitle, habit.getTitle());
        views.setTextViewText(R.id.tvWidgetStreak, "🔥 " + habit.getStreakDays() + " ngày");

        boolean isCompleted = habit.getCurrentCount() >= habit.getTargetCount();
        if (isCompleted) {
            views.setInt(R.id.ivWidgetCheck, "setColorFilter", Color.parseColor("#2E7D32"));
        } else {
            views.setInt(R.id.ivWidgetCheck, "setColorFilter", Color.parseColor("#CCCCCC"));
        }

        // Cấu hình fillInIntent kèm theo dữ liệu định danh (Uri) để truyền ID chính xác
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("EXTRA_HABIT_ID", habit.getId());

        // BỔ SUNG DÒNG NÀY ĐỂ ANDROID KHÔNG BỊ NUỐT MẤT EXTRA:
        fillInIntent.setData(android.net.Uri.parse(fillInIntent.toUri(Intent.URI_INTENT_SCHEME)));

        views.setOnClickFillInIntent(R.id.widgetItemContainer, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < habitList.size()) {
            return habitList.get(position).getId(); // Trả về ID thật của Habit trong database
        }
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}