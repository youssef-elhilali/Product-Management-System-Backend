package com.pmso.product_management_system_original.logic.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.pmso.product_management_system_original.dataaccess.dao.ProductDao;
import com.pmso.product_management_system_original.dataaccess.entities.Category;
import com.pmso.product_management_system_original.dataaccess.entities.Product;
import com.pmso.product_management_system_original.exceptions.ResourceNotFoundException;
import com.pmso.product_management_system_original.logic.api.ProductService;
import com.pmso.product_management_system_original.producers.ProductProducer;
import com.pmso.product_management_system_original.to.ProductDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private ProductProducer productProducer;

    @Override
    public void addProduct(ProductDto productDto) throws IOException {
        Product product = new Product();
        product.setNom(productDto.getNom());
        product.setDescription(productDto.getDescription());
        product.setDateCreation(new Date());

        product.setImage(saveImage(productDto.getImage()));

        product.setSupprimer(false);
        product.setSlug(productDto.getNom().replace(" ", "-"));
        product.setDateModification(new Date());
        product.setCategory(new Category(productDto.getCategoryId()));
        this.productDao.save(product);
        this.productProducer.send(product);
    }

    @Override
    public ProductDto getProductDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setNom(product.getNom());
        productDto.setDescription(product.getDescription());
        productDto.setDateCreation(product.getDateCreation());
        productDto.setImagePath(product.getImage());
        productDto.setSupprimer(product.getSupprimer());
        productDto.setSlug(product.getSlug());
        productDto.setDateModification(product.getDateModification());
        if (product.getCategory() != null) {
            productDto.setCategoryId(product.getCategory().getId());
        }
        return productDto;
    }

    @Override
    public List<ProductDto> getProducts() {
        List<Product> products = this.productDao.findAll();
        List<ProductDto> productDtos = new ArrayList<>();
        for (Product product: products) {
            productDtos.add(this.getProductDto(product));
        }
        return productDtos;
    }

    @Override
    public ProductDto getProduct(Long id) {
        ProductDto productDto = new ProductDto();
        Product product = this.productDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
        productDto.setNom(product.getNom());
        productDto.setDescription(product.getDescription());
        productDto.setSlug(product.getSlug());
        productDto.setImagePath(product.getImage());
        productDto.setCategoryId(product.getCategory().getId());
        productDto.setDateCreation(product.getDateCreation());
        productDto.setDateModification(product.getDateModification());
        return productDto;
    }

    @Override
    public ResponseEntity<Map<String, Object>> products(int pageNo, int pageSize, String search) {
            Pageable pageable = PageRequest.of(pageNo, pageSize);

            if (search != null && !search.isBlank()) {
                List<Product> productsList;
                productsList = this.productDao.findByNomContainingIgnoreCase(search);
                List<ProductDto> productDtos = new ArrayList<>();
                for (Product product : productsList) {
                    if (!product.getSupprimer())
                        productDtos.add(this.getProductDto(product));
                }
                Map<String, Object> res = new HashMap<>();
                res.put("products", productsList);
                return ResponseEntity.ok(res);
            } else {
                Page<Product> productsPage;
                List<Product> productsList;
                productsPage = this.productDao.findAll(pageable);
                productsList = productsPage.getContent();
                List<ProductDto> productDtos = new ArrayList<>();
                for (Product product : productsList) {
                    if (!product.getSupprimer())
                        productDtos.add(this.getProductDto(product));
                }
                Map<String, Object> res = new HashMap<>();
                res.put("products", productsList);
                res.put("currentPage", productsPage.getNumber());
                res.put("totalItems", productsPage.getTotalElements());
                res.put("totalPages", productsPage.getTotalPages());
                return ResponseEntity.ok(res);
            }
    }

    @Override
    public void updateProduct(long id, ProductDto productDto) throws Exception {
        Product product = this.productDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
        String slug;
        if (productDto.getNom() != null){
            product.setNom(productDto.getNom());
            slug = productDto.getNom().replace(" ", "-");
        } else {
            product.setNom(product.getNom());
            slug = product.getNom().replace(" ", "-");
        }
        if (productDto.getDescription() != null){
            product.setDescription(productDto.getDescription());
        } else {
            product.setDescription(product.getDescription());
        }
        product.setImage(saveImage(productDto.getImage()));

        product.setId(product.getId());
        product.setDateCreation(product.getDateCreation());
        product.setSupprimer(product.getSupprimer());
        product.setSlug(slug);
        product.setDateModification(new Date());
        product.setCategory(product.getCategory());
        this.productDao.save(product);
        this.productProducer.send(product);
    }

    private String saveImage(MultipartFile image) throws IOException {
        String imageName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());
        amazonS3.putObject(new PutObjectRequest(
                bucketName, imageName, image.getInputStream(), metadata)
        );
        return "https://" + bucketName + ".s3.amazonaws.com/" + imageName;
    }

    @Override
    public void deleteProduct(long id) {
        this.productDao.deleteById(id);
    }

    @Override
    public int counting() {
        List<Product> product = this.productDao.findAll();
        return product.size();
    }

    @Override
    public void deleteUpdateProduct(long id) {
        Product product = this.productDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
        product.setSupprimer(true);
        product.setDateModification(new Date());
        this.productDao.save(product);
        this.productProducer.send(product);
    }

}
