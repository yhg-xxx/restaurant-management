package com.example.repository;

import com.example.entity.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Integer> {
    // 使用原生 SQL 查询
    @Query(value = "SELECT * FROM tables WHERE LOWER(table_number) LIKE LOWER(CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    List<TableEntity> findByTableNumberContaining(@Param("keyword") String keyword);
}