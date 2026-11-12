package com.example.service;

import com.example.entity.TableEntity;
import java.util.List;

public interface TableService {
    List<TableEntity> findAllTables();
    TableEntity addTable(TableEntity table);
    void deleteTable(Integer id);
    TableEntity updateTable(TableEntity table);
    List<TableEntity> searchTables(String keyword); // 修改为返回列表
}