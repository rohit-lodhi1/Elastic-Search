package com.search.app.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

@Service
public class ProductServiceImpl implements IProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ElasticProductRepository elasticProductRepository;

	@Autowired
	private ElasticsearchClient elasticsearchClient;

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
	public ApiResponse fuzzySearch(String keyword, int pageNumber, int pageSize) {
		Supplier<Query> supplier = this.getFuzzySearchSupplier(keyword);
		try {
			SearchResponse<Product> response = elasticsearchClient.search(
					s -> s.index("products").query(supplier.get()).from(pageNumber * pageSize).size(pageSize),
					Product.class);
			List<Product> content = response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
			long totalElements = response.hits().total().value();
			return ApiResponse.builder()
					.body(new PageImpl<>(content, PageRequest.of(pageNumber, pageSize), totalElements)).build();
		} catch (ElasticsearchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ApiResponse autoSuggestSearching(String keyword) {
		Supplier<Query> supplier = this.getAutoSuggestSupplier(keyword);
		try {
			SearchResponse<Product> response = elasticsearchClient
					.search(s -> s.index("products").query(supplier.get()).from(0).size(10), Product.class);
			System.out.println(response.hits().toString());
			List<String> content = response.hits().hits().stream().map(e -> e.source().getName())
					.collect(Collectors.toList());
			return ApiResponse.builder().body(content).build();
		} catch (ElasticsearchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Supplier<Query> getAutoSuggestSupplier(String keyword) {
		var query = new MultiMatchQuery.Builder()
				.query(keyword)
				.fields("name", "description", "tags")
				.analyzer("standard")   // Simple ,Whitespace ,Keyword ,Language 
				.fuzziness("auto")      // "2" ,"3","4"
				.build();
		return () -> Query.of(q -> q.multiMatch(query));
	}

	// supplier for fuzzy search on fields of product
	public Supplier<Query> getFuzzySearchSupplier(String keyword) {
		var multiMatchQuery = new MultiMatchQuery.Builder().query(keyword) // The search keyword
				.fields("name", "description", "tags") // Fields to search on
				.fuzziness("AUTO") // Automatic fuzziness (you can adjust this as needed (e.g "2" ,"3","4"))
				.build();
		try {
			var rangeQuery = new RangeQuery.Builder().number(r -> r.field("price")
					.gte(Double.parseDouble(keyword) + Integer.MIN_VALUE).lte(Double.parseDouble(keyword) + 100))
					.build();
			return () -> Query.of(q -> q.range(rangeQuery));
		} catch (NumberFormatException ex) {

			return () -> Query.of(q -> q.multiMatch(multiMatchQuery));
		}
	}

	@Override
	// Search method for Elastic search with pagination, sorting, and filters
	public Page<Product> searchProducts(String keyword, String category, Double minPrice, Double maxPrice,
			ProductStatus status, List<String> tags, int page, int size, String sortBy, String sortOrder) {

		if (keyword != null && !keyword.isEmpty()) {
			return this.fuzzySearchKeyword(keyword, page, size);
		}

		QueryBuilders.bool();
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

	@Override
	public ApiResponse getAllProducts(int page, int size) {
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdDate"));

		return ApiResponse.builder()
				.body(this.elasticProductRepository.findAllByIsActiveAndIsDeleted(true, false, pageRequest)).build();
	}

}
