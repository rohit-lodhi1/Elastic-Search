package com.search.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.search.app.entity.Product;
import com.search.app.entity.ProductStatus;
import com.search.app.payload.ApiResponse;
import com.search.app.payload.ProductRequest;
import com.search.app.service.IProductService;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

	@Autowired
	private IProductService productService;

	@PostMapping("/create")
	public ResponseEntity<ApiResponse> createProduct(@RequestBody ProductRequest request) {
		return new ResponseEntity<>(this.productService.createProduct(request), HttpStatus.CREATED);
	}

	@PutMapping("/update")
	public ResponseEntity<ApiResponse> updateProduct(@RequestBody ProductRequest request) {
		return new ResponseEntity<>(this.productService.updateProduct(request), HttpStatus.OK);
	}

	@DeleteMapping("/delete")
	public ResponseEntity<ApiResponse> deleteProduct(@RequestParam("id") String id) {
		return new ResponseEntity<>(this.productService.deleteProduct(id), HttpStatus.OK);
	}
	
	@GetMapping("/findAll")
	public ResponseEntity<ApiResponse> getAllProducts(
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size){
		return ResponseEntity.ok(this.productService.getAllProducts(page,size));
	}

	@GetMapping("/fuzzy/search")
	public ResponseEntity<ApiResponse> fuzzySearch(
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,@RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
          return ResponseEntity.ok(this.productService.fuzzySearch(keyword,pageNumber,pageSize));
	}

	// pending......
	@GetMapping("/search")
	public ResponseEntity<Page<Product>> searchProducts(
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "minPrice", required = false) Double minPrice,
			@RequestParam(value = "maxPrice", required = false) Double maxPrice,
			@RequestParam(value = "status", required = false) ProductStatus status,
			@RequestParam(value = "tags", required = false) List<String> tags,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size,
			@RequestParam(value = "sortBy", required = false, defaultValue = "name") String sortBy,
			@RequestParam(value = "sortOrder", required = false, defaultValue = "asc") String sortOrder) {
		// Use the service method to get paginated products based on filters
		Page<Product> products = productService.searchProducts(keyword, category, minPrice, maxPrice, status, tags,
				page, size, sortBy, sortOrder);

		return ResponseEntity.ok(products);
	}
}
