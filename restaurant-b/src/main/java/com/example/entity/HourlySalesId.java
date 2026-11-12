package com.example.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class HourlySalesId implements Serializable {
    private Double hourOfDay;
    private String dayOfWeek;

    // 必须有无参构造函数
    public HourlySalesId() {}

    public HourlySalesId(Double hourOfDay, String dayOfWeek) {
        this.hourOfDay = hourOfDay;
        this.dayOfWeek = dayOfWeek;
    }


    public Double getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(Double hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    // 必须重写equals和hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HourlySalesId that = (HourlySalesId) o;
        return Objects.equals(hourOfDay, that.hourOfDay) &&
                Objects.equals(dayOfWeek, that.dayOfWeek);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hourOfDay, dayOfWeek);
    }
}