package com.search.app.repository.elastic;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.search.app.entity.Product;
import com.search.app.entity.ProductStatus;

public interface ElasticProductRepository extends ElasticsearchRepository<Product, String> {

	public Optional<Product> findByIdAndIsDeletedAndIsActive(String id,Boolean isDeleted,Boolean isActive);
	
	 // Custom search query that allows searching with multiple filters like price range, status, tags, etc.
    @Query("{ \"bool\": { \"must\": [ " +
            "{ \"multi_match\": { \"query\": \"?0\", \"fields\": [\"name^3\", \"description^2\"] } }, " + // fuzzy search on name and description
            "], \"filter\": [ " +
            "{ \"range\": { \"price\": { \"gte\": \"?1\", \"lte\": \"?2\" } } }, " + // filter by price range
            "{ \"term\": { \"status\": \"?3\" } }, " + // filter by status
            "{ \"terms\": { \"tags\": \"?4\" } } " + // filter by tags
            "] } }")
    Page<Product> searchWithFilters(String keyword,String category, Double minPrice, Double maxPrice, ProductStatus status, List<String> tags, Pageable pageable);

 
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name\", \"description\", \"tags\", \"createdBy\", \"ownerId\"], \"fuzziness\": \"AUTO\"}}")
    Page<Product> searchByFuzzyKeyword(String keyword, Pageable pageable);
    
    @Query("{\"bool\": {\"must\": [" +
    		        "{\"term\": {\"isDeleted\": {\"value\": false}}}," +
    		        "{\"term\": {\"isActive\": {\"value\": true}}}" +
    		        "]," 
    		+ "\"should\": [" +
            "{\"wildcard\": {\"name\": {\"value\": \"*?0*\"}}}," +
             "{\"wildcard\": {\"description\": {\"value\": \"*?0*\"}}},"+
             "{\"wildcard\": {\"tags\": {\"value\": \"*?0*\"}}},"+
            "{\"fuzzy\": {\"name\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\"}}}," +
            "{\"fuzzy\": {\"description\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\"}}},	" +
            "{\"fuzzy\": {\"tags\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\"}}}" +
            
            "]}}")
    List<Product> searchByFuzzy(String keyword); 
    
    @Query("{\"wildcard\": {\"name\": {\"value\": \"*?0*\"}}}")
    Page<Product> findByNameContaining(String keyword,Pageable pageable);
}
