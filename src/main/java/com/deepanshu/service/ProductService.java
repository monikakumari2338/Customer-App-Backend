package com.deepanshu.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.deepanshu.exception.ProductException;
import com.deepanshu.modal.Product;
import com.deepanshu.request.CreateProductRequest;

public interface ProductService {
    public Product createProduct(CreateProductRequest req) throws ProductException;

    public String deleteProduct(Long productId) throws ProductException;

    public Product updateProduct(Long productId, Product product) throws ProductException;

    public List<Product> getAllProducts();

    public Product findProductById(Long id) throws ProductException;

    public List<Product> findProductByCategory(String category);

    public List<Product> searchProduct(String query);

    public Page<Product> getAllProduct(String category, List<String> colors, List<String> sizes, Integer minPrice, Integer maxPrice, Integer minDiscount, String sort, String stock, Integer pageNumber, Integer pageSize,
                                       String country, String wearType, String fabric, String sleeves, String fit, String materialCare, String productCode, String seller);

    public List<Product> findProductsByPincode(String pincode);

    Product getProductById(Long newProductId);

    Product save(Product product);
}
