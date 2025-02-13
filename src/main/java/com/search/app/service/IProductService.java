package com.search.app.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.search.app.entity.Product;
import com.search.app.entity.ProductStatus;
import com.search.app.payload.ProductRequest;
import com.search.app.payload.ApiResponse;

public interface IProductService {

	public ApiResponse createProduct(ProductRequest request);

	// Search method for Elasticsearch with pagination, sorting, and filters
    public Page<Product> searchProducts(String keyword, String category, Double minPrice, Double maxPrice,
                                        ProductStatus status, List<String> tags, int page, int size,
                                        String sortBy, String sortOrder); 

	public ApiResponse deleteProduct(String id);

	public ApiResponse updateProduct(ProductRequest request);

	public ApiResponse fuzzySearch(String keyword,int pageNumber,int pageSize);

	public ApiResponse getAllProducts(int page, int size);

	public ApiResponse autoSuggestSearching(String keyword);
}
