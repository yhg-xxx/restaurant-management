package com.example.controller;

import com.example.entity.TableEntity;
import com.example.repository.TableRepository;
import com.example.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final TableService tableService;

    @Autowired
    public TableController(TableService tableService, TableRepository tableRepository) {
        this.tableService = tableService;
    }

    // 模糊查询桌台
    @GetMapping("/search")
    public ResponseEntity<List<TableEntity>> searchTables(
            @RequestParam(required = false) String keyword) {

        // 处理空格和空值
        if (keyword != null) {
            keyword = keyword.trim();
        }

        List<TableEntity> tables = tableService.searchTables(keyword);
        return new ResponseEntity<>(tables, HttpStatus.OK);
    }

    // 获取所有桌台
    @GetMapping
    public ResponseEntity<List<TableEntity>> getAllTables() {
        List<TableEntity> tables = tableService.findAllTables();
        return new ResponseEntity<>(tables, HttpStatus.OK);
    }

    // 添加新桌台
    @PostMapping
    public ResponseEntity<TableEntity> addTable(@RequestBody TableEntity table) {
        TableEntity newTable = tableService.addTable(table);
        return new ResponseEntity<>(newTable, HttpStatus.CREATED);
    }

    // 更新桌台信息
    @PutMapping("/{id}")
    public ResponseEntity<TableEntity> updateTable(@PathVariable Integer id, @RequestBody TableEntity table) {
        if (!id.equals(table.getTableId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        TableEntity updatedTable = tableService.updateTable(table);
        return new ResponseEntity<>(updatedTable, HttpStatus.OK);
    }

    // 删除桌台
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Integer id) {
        tableService.deleteTable(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}