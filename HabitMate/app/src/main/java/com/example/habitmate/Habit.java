package com.example.habitmate;

import androidx.room.Entity;
import androidx.room.Ignore; // Nhớ import thư viện này
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private int targetCount;
    private int currentCount;
    private int streakDays;

    // 1. Constructor chính dành cho Room Database (giữ nguyên constructor nhiều tham số nhất)
    public Habit(int id, String title, int targetCount, int currentCount, int streakDays) {
        this.id = id;
        this.title = title;
        this.targetCount = targetCount;
        this.currentCount = currentCount;
        this.streakDays = streakDays;
    }

    // 2. Constructor phụ dùng khi tạo thói quen mới trong MainActivity -> Phải có @Ignore để Room bỏ qua
    @Ignore
    public Habit(String title, int targetCount, int currentCount) {
        this.title = title;
        this.targetCount = targetCount;
        this.currentCount = currentCount;
        this.streakDays = 0;
    }

    // --- Các Getter và Setter bên dưới giữ nguyên ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getTargetCount() { return targetCount; }
    public void setTargetCount(int targetCount) { this.targetCount = targetCount; }

    public int getCurrentCount() { return currentCount; }
    public void setCurrentCount(int currentCount) { this.currentCount = currentCount; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }
}