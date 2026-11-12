package com.example.service.impl;



import com.example.dto.*;
import com.example.entity.DishSales;
import com.example.entity.HourlySales;
import com.example.repository.DishSalesRepository;
import com.example.repository.HourlySalesRepository;
import com.example.service.SalesService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SalesServiceImpl implements SalesService {

    private final HourlySalesRepository hourlySalesRepository;
    private final DishSalesRepository dishSalesRepository;

    public SalesServiceImpl(HourlySalesRepository hourlySalesRepository,
                            DishSalesRepository dishSalesRepository) {
        this.hourlySalesRepository = hourlySalesRepository;
        this.dishSalesRepository = dishSalesRepository;
    }

    @Override
    public HeatmapDataDTO getHourlySalesHeatmap() {
        List<HourlySales> sales = hourlySalesRepository.findAllByOrderByDayOfWeekAscHourOfDayAsc();

        // 初始化热力图数据
        List<Integer> hours = IntStream.range(0, 24).boxed().collect(Collectors.toList());
        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

        //  使用 Double 作为内层 Map 的键类型
        Map<String, Map<Double, BigDecimal>> dayHourMap = new LinkedHashMap<>();
        days.forEach(day -> dayHourMap.put(day, new HashMap<>()));

        sales.forEach(sale -> {
            String day = sale.getDayOfWeek().trim();
            dayHourMap.computeIfPresent(day, (k, v) -> {
                // 直接使用 Double 类型的 hourOfDay
                v.put(sale.getHourOfDay(), sale.getTotalSales());
                return v;
            });
        });

        // 构建热力图数据点
        List<List<Object>> data = new ArrayList<>();
        for (int i = 0; i < days.size(); i++) {
            // 使用 Double 类型的 Map
            Map<Double, BigDecimal> hourSales = dayHourMap.get(days.get(i));
            for (int hour = 0; hour < 24; hour++) {
                //  使用 Double 类型的键获取值
                BigDecimal value = hourSales.getOrDefault((double) hour, BigDecimal.ZERO);
                data.add(Arrays.asList(hour, i, value));
            }
        }

        HeatmapDataDTO dto = new HeatmapDataDTO();
        dto.setHours(hours);
        dto.setDays(days);
        dto.setData(data);
        return dto;
    }

    @Override
    public List<DishRankDTO> getDishRanking() {
        return dishSalesRepository.findTop10Dishes() // 使用新方法
                .stream()
                .map(dish -> {
                    DishRankDTO dto = new DishRankDTO();
                    dto.setDishId(dish.getDishId());
                    dto.setDishName(dish.getDishName());
                    dto.setTotalSales(dish.getTotalSales());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CategorySalesDTO> getCategorySales() {
        // 将 Iterable 转换为 List 以获得 stream() 支持
        List<DishSales> allDishes = new ArrayList<>(dishSalesRepository.findAll());

        // 显式指定 Map.Entry 类型
        return allDishes.stream()
                .collect(Collectors.groupingBy(
                        DishSales::getCategoryName,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                DishSales::getTotalSales,
                                BigDecimal::add
                        )
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    CategorySalesDTO dto = new CategorySalesDTO();
                    dto.setCategory(entry.getKey());
                    dto.setSales(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}