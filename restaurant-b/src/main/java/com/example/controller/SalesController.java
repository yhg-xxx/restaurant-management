package com.example.controller;

import com.example.dto.*;
import com.example.service.SalesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @GetMapping("/heatmap")
    public HeatmapDataDTO getHeatmapData() {
        return salesService.getHourlySalesHeatmap();
    }

    @GetMapping("/dishes")
    public List<DishRankDTO> getDishRanking() {
        return salesService.getDishRanking();
    }

    @GetMapping("/categories")
    public List<CategorySalesDTO> getCategorySales() {
        return salesService.getCategorySales();
    }
}