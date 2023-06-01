package com.pmso.product_management_system_original.dataaccess.dao;

import com.pmso.product_management_system_original.dataaccess.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductDao extends JpaRepository<Product, Long> {
    List<Product> findByCategory_Id(Long categoryId);
    List<Product> findByNomContainingIgnoreCase(String seatrch);
    Page<Product> findAll(Pageable pageable);
}
