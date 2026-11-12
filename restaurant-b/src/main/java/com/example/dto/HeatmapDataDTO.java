package com.example.dto;


import java.util.List;


public class HeatmapDataDTO {
    private List<Integer> hours;
    private List<String> days;
    private List<List<Object>> data;

    public HeatmapDataDTO(List<Integer> hours, List<String> days, List<List<Object>> data) {
        this.hours = hours;
        this.days = days;
        this.data = data;
    }

    public HeatmapDataDTO() {

    }

    public List<Integer> getHours() {
        return hours;
    }

    public void setHours(List<Integer> hours) {
        this.hours = hours;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    public List<List<Object>> getData() {
        return data;
    }

    public void setData(List<List<Object>> data) {
        this.data = data;
    }
}