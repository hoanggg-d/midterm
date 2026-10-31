package com.example.habitmate;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habitList = new ArrayList<>();
    private OnHabitClickListener listener;

    // Danh sách bảng màu phối sẵn (Màu nền pastel - Màu chữ/chủ đạo tương ứng)
    private final int[][] colorPalette = {
            {Color.parseColor("#E8F5E9"), Color.parseColor("#2E7D32")}, // Xanh lá
            {Color.parseColor("#E3F2FD"), Color.parseColor("#1565C0")}, // Xanh dương
            {Color.parseColor("#FFF3E0"), Color.parseColor("#EF6C00")}, // Cam
            {Color.parseColor("#F3E5F5"), Color.parseColor("#6A1B9A")}, // Tím
            {Color.parseColor("#FFEBEE"), Color.parseColor("#C62828")}  // Đỏ hồng
    };

    // Trong phần định nghĩa Interface của Adapter, thêm phương thức onHabitLongClick:
    public interface OnHabitClickListener {
        void onHabitClick(Habit habit);
        void onHabitLongClick(Habit habit); // Thêm sự kiện nhấn giữ
    }

    public HabitAdapter(OnHabitClickListener listener) {
        this.listener = listener;
    }

    public void setHabits(List<Habit> habits) {
        this.habitList = habits;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit currentHabit = habitList.get(position);
        holder.tvTitle.setText(currentHabit.getTitle());
        holder.tvStreak.setText("🔥 " + currentHabit.getStreakDays() + " ngày");

        // Chọn màu sắc dựa theo vị trí item
        int colorIndex = Math.abs(position) % colorPalette.length;
        int backgroundColor = colorPalette[colorIndex][0];
        int primaryColor = colorPalette[colorIndex][1];

        // Đổi màu nền thẻ và màu chữ cùng tone
        holder.cardView.setCardBackgroundColor(backgroundColor);
        holder.tvTitle.setTextColor(primaryColor);
        holder.tvStreak.setTextColor(primaryColor);

        // Xử lý nút stick (Huy hiệu tích xanh)
        boolean isCompleted = currentHabit.getCurrentCount() >= currentHabit.getTargetCount();
        if (isCompleted) {
            // Đã hoàn thành: Đổi stick sang màu chủ đạo của Habit
            ImageViewCompat.setImageTintList(holder.ivCheckStatus, ColorStateList.valueOf(primaryColor));
        } else {
            // Chưa hoàn thành: Để stick màu TRẮNG
            ImageViewCompat.setImageTintList(holder.ivCheckStatus, ColorStateList.valueOf(Color.WHITE));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHabitClick(currentHabit);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHabitClick(currentHabit);
            }
        });

        // Xử lý sự kiện nhấn giữ vào item (xóa)
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onHabitLongClick(currentHabit);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvTitle, tvStreak;
        private ImageView ivCheckStatus;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewHabit);
            tvTitle = itemView.findViewById(R.id.tvHabitTitle);
            tvStreak = itemView.findViewById(R.id.tvStreak);
            ivCheckStatus = itemView.findViewById(R.id.ivCheckStatus);
        }
    }
}