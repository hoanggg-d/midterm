package com.example.habitmate;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppDatabase database;
    private HabitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Khởi tạo thanh lịch chọn ngày tháng ở phía trên
        setupDateStrip();

        // Khởi tạo Adapter và xử lý sự kiện bấm vào thẻ thói quen
        adapter = new HabitAdapter(new HabitAdapter.OnHabitClickListener() {
            @Override
            public void onHabitClick(Habit habit) {
                // Chạm 1 lần để hoàn thành
                if (habit.getCurrentCount() < habit.getTargetCount()) {
                    habit.setCurrentCount(habit.getTargetCount());
                    habit.setStreakDays(habit.getStreakDays() + 1);

                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.habitDao().updateHabit(habit);
                    });
                }
            }

            @Override
            public void onHabitLongClick(Habit habit) {
                // Nhấn giữ để hiển thị Dialog xác nhận xóa
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Xóa thói quen")
                        .setMessage("Bạn có chắc chắn muốn xóa thói quen \"" + habit.getTitle() + "\" không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                database.habitDao().deleteHabit(habit);
                            });
                            Toast.makeText(MainActivity.this, "Đã xóa thói quen", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);

        // Khởi tạo Database
        database = AppDatabase.getDatabase(this);

        // Lắng nghe dữ liệu thay đổi từ Room Database theo thời gian thực
        database.habitDao().getAllHabits().observe(this, habits -> {
            adapter.setHabits(habits);
        });

        // Xử lý nút thêm thói quen mới (+)
        FloatingActionButton fabAdd = findViewById(R.id.fabAddHabit);
        fabAdd.setOnClickListener(v -> showAddHabitDialog());
    }

    // Thiết lập thanh chọn ngày ngang (Calendar Strip)
    private void setupDateStrip() {
        LinearLayout dateContainer = findViewById(R.id.layoutDateContainer);
        if (dateContainer == null) return;
        dateContainer.removeAllViews();

        Calendar calendar = Calendar.getInstance();
        // Tua lại 7 ngày trước ngày hiện tại để xem lịch sử
        calendar.add(Calendar.DAY_OF_YEAR, -7);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EE", Locale.ENGLISH); // MON, TUE...
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.getDefault()); // Ngày trong tháng

        for (int i = 0; i < 20; i++) {
            final Calendar itemCalendar = (Calendar) calendar.clone();
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_date_box, dateContainer, false);

            TextView tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            TextView tvDayOfMonth = itemView.findViewById(R.id.tvDayOfMonth);
            View container = itemView.findViewById(R.id.dateItemContainer);

            String dayStr = dayFormat.format(itemCalendar.getTime()).toUpperCase();
            String dateStr = dateFormat.format(itemCalendar.getTime());

            tvDayOfWeek.setText(dayStr);
            tvDayOfMonth.setText(dateStr);

            // Kiểm tra xem có phải là ngày hôm nay không để làm nổi bật sẵn
            boolean isToday = isSameDay(itemCalendar, Calendar.getInstance());
            if (isToday) {
                container.setBackgroundResource(R.drawable.bg_date_selected);
            }

            // Sự kiện khi bấm vào một ngày cụ thể trên thanh lịch
            itemView.setOnClickListener(v -> {
                highlightSelectedDate(dateContainer, container);
            });

            dateContainer.addView(itemView);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void highlightSelectedDate(LinearLayout container, View selectedCard) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            View box = child.findViewById(R.id.dateItemContainer);
            if (box != null) {
                box.setBackgroundResource(R.drawable.bg_date_normal);
            }
        }
        selectedCard.setBackgroundResource(R.drawable.bg_date_selected);
    }

    // Hàm hiển thị Dialog nhập thói quen mới
    private void showAddHabitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_habit, null);
        builder.setView(dialogView);

        EditText etHabitName = dialogView.findViewById(R.id.etHabitName);

        builder.setTitle("Thêm thói quen mới")
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String title = etHabitName.getText().toString().trim();

                    if (!title.isEmpty()) {
                        Habit newHabit = new Habit(title, 1, 0);

                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            database.habitDao().insertHabit(newHabit);
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "Vui lòng nhập tên thói quen", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}