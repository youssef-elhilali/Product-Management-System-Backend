package com.pmso.product_management_system_original.logic.api;

import com.pmso.product_management_system_original.dataaccess.entities.Product;
import com.pmso.product_management_system_original.to.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface ProductService {

    void addProduct(ProductDto productDto) throws IOException;
    ProductDto getProductDto(Product product);
    List<ProductDto> getProducts();
    void updateProduct(long id, ProductDto productDto) throws Exception;
    void deleteProduct(long id);
    int counting();
    ResponseEntity<Map<String, Object>> products(int pageNo, int pageSize, String search);
    ProductDto getProduct(Long id);
    void deleteUpdateProduct(long id) throws Exception;
}
