package com.example.service;

import com.example.dto.*;

import java.util.List;

public interface SalesService {
    HeatmapDataDTO getHourlySalesHeatmap();
    List<DishRankDTO> getDishRanking();
    List<CategorySalesDTO> getCategorySales();
}