package com.example.service.impl;


import com.example.entity.TableEntity;
import com.example.repository.TableRepository;
import com.example.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TableServiceImpl implements TableService {

    private final TableRepository tableRepository;

    @Autowired
    public TableServiceImpl(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    @Override
    public List<TableEntity> findAllTables() {
        return tableRepository.findAll();
    }

    @Override
    public TableEntity addTable(TableEntity table) {
        return tableRepository.save(table);
    }

    @Override
    @Transactional
    public void deleteTable(Integer id) {
        tableRepository.deleteById(id);
    }

    @Override
    public TableEntity updateTable(TableEntity table) {
        // 确保ID存在
        if (table.getTableId() == null) {
            throw new IllegalArgumentException("Table ID cannot be null for update");
        }

        // 检查是否存在
        if (!tableRepository.existsById(table.getTableId())) {
            throw new IllegalArgumentException("Table not found with ID: " + table.getTableId());
        }

        return tableRepository.save(table);
    }

    @Override
    public List<TableEntity> searchTables(String keyword) {
        // 如果关键字为空，返回所有桌台
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Searching all tables");
            return tableRepository.findAll();
        }

        System.out.println("Searching tables with keyword: " + keyword);
        List<TableEntity> results = tableRepository.findByTableNumberContaining(keyword);
        System.out.println("Found " + results.size() + " results");
        return results;
    }


}