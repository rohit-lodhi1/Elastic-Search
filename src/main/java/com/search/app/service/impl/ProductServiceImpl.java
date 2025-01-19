package com.search.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.search.app.entity.Product;
import com.search.app.entity.ProductStatus;
import com.search.app.exception.ResourceNotFoundException;
import com.search.app.payload.ApiResponse;
import com.search.app.payload.ProductRequest;
import com.search.app.repository.elastic.ElasticProductRepository;
import com.search.app.repository.jpa.ProductRepository;
import com.search.app.service.IProductService;
import com.search.app.utils.MessageConstants;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;

@Service
public class ProductServiceImpl implements IProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ElasticProductRepository elasticProductRepository;

	@Override
	public ApiResponse createProduct(ProductRequest request) {
		Product product = this.productRequestToProduct(request);
		this.productRepository.save(product);
		this.elasticProductRepository.save(product);
		return ApiResponse.builder().status(HttpStatus.CREATED.value()).message(MessageConstants.PRODUCT_CREATE_SUCCESS)
				.build();
	}

	@Override
	public ApiResponse deleteProduct(String id) {
		Product product = this.productRepository.findByIdAndIsDeletedAndIsActive(id, false, true)
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.PRODUCT_NOT_FOUND));
		product.setIsDeleted(true);
		this.productRepository.save(product);
		this.elasticProductRepository.save(product);
		return ApiResponse.builder().message(MessageConstants.PRODUCT_DELETE_SUCCESS).status(HttpStatus.OK.value())
				.build();
	}

	@Override
	public ApiResponse updateProduct(ProductRequest request) {
		Product product = this.productRepository.findByIdAndIsDeletedAndIsActive(request.getId(), false, true)
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.PRODUCT_NOT_FOUND));
		product.setName(request.getName());
		product.setDescription(request.getDescription());
		product.setOwnerId(request.getOwnerId());
		product.setCreatedBy(request.getCreatedBy());
		product.setTags(request.getTags());
		product.setPrice(request.getPrice());
		this.productRepository.save(product);
		this.elasticProductRepository.save(product);
		return ApiResponse.builder().message(MessageConstants.PRODUCT_UPDATE_SUCCESS).status(HttpStatus.OK.value())
				.build();
	}

	@Override
	// Search method for Elasticsearch with pagination, sorting, and filters
	public Page<Product> searchProducts(String keyword, String category, Double minPrice, Double maxPrice,
			ProductStatus status, List<String> tags, int page, int size, String sortBy, String sortOrder) {

		if (keyword != null && !keyword.isEmpty()) {
			return this.fuzzySearchKeyword(keyword, page, size);
		}
		// Building the Pageable object for pagination and sorting
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy));

		if (sortOrder.equalsIgnoreCase("desc")) {
			pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
		}

		// Elasticsearch query with filters (if provided)
		if (category != null || minPrice != null || maxPrice != null || status != null || tags != null
				|| !keyword.isEmpty()) {
			return elasticProductRepository.searchWithFilters(keyword, category, minPrice, maxPrice, status, tags,
					pageRequest);
		} else {
//        	  return this.elasticProductRepository.searchByFuzzy(keyword, pageRequest);
			return new PageImpl<>(this.elasticProductRepository.searchByFuzzy(keyword));
		}
	}

	public Page<Product> fuzzySearchKeyword(String keyword, int pageNo, int pageSize) {
		PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.ASC, "name"));
		System.out.println(pageRequest + " " + pageNo + " " + pageSize);
//        return this.elasticProductRepository.searchByFuzzy(keyword, pageRequest);

		return new PageImpl<>(this.elasticProductRepository.searchByFuzzy(keyword));
	}

	public Product productRequestToProduct(ProductRequest request) {
		// Perform validation and all stuff
		return Product.builder().name(request.getName()).description(request.getDescription())
				.createdBy(request.getCreatedBy()).ownerId(request.getOwnerId()).price(request.getPrice())
				.tags(request.getTags()).build();
	}

}
