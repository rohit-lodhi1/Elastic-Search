package com.search.app.entity;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Document;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Document(indexName = "products") // Elasticsearch Index Mapping
public class Product extends AuditData {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	private String name;

	private String description;

	private Double price;

	private String createdBy;

	private String ownerId;

	private String inventoryId;

	@ElementCollection
	@CollectionTable(joinColumns = @JoinColumn(name = "product_id"))
	private List<String> tags;

	@Enumerated(EnumType.STRING)
	private ProductStatus status;

}
