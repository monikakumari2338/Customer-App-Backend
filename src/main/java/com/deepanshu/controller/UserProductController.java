package com.deepanshu.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.deepanshu.exception.ProductException;
import com.deepanshu.modal.Product;
import com.deepanshu.service.ProductService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "https://localhost:8081")
public class UserProductController {

    private ProductService productService;

    public UserProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping("/products")
    public ResponseEntity<Page<Product>> findProductByCategoryHandler(@RequestParam String category,
                                                                      @RequestParam List<String> color, @RequestParam List<String> size, @RequestParam Integer minPrice,
                                                                      @RequestParam Integer maxPrice, @RequestParam Integer minDiscount, @RequestParam String sort,
                                                                      @RequestParam String stock, @RequestParam Integer pageNumber, @RequestParam Integer pageSize,
                                                                      @RequestParam String country,
                                                                      @RequestParam String wearType,
                                                                      @RequestParam String fabric,
                                                                      @RequestParam String sleeves,
                                                                      @RequestParam String fit,
                                                                      @RequestParam String materialCare,
                                                                      @RequestParam String productCode,
                                                                      @RequestParam String seller) {


        Page<Product> res = productService.getAllProduct(category, color, size, minPrice, maxPrice, minDiscount, sort, stock, pageNumber, pageSize,
                country, wearType, fabric, sleeves, fit, materialCare, productCode, seller);

        System.out.println("complete products");
        return new ResponseEntity<>(res, HttpStatus.ACCEPTED);

    }


    @GetMapping("/products/id/{productId}")
    public ResponseEntity<Product> findProductByIdHandler(@PathVariable Long productId) throws ProductException {

        Product product = productService.findProductById(productId);

        return new ResponseEntity<Product>(product, HttpStatus.ACCEPTED);
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProductHandler(@RequestParam String q) {

        List<Product> products = productService.searchProduct(q);

        return new ResponseEntity<List<Product>>(products, HttpStatus.OK);

    }
}
