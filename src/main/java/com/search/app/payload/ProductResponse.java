package com.search.app.payload;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.search.app.entity.ProductStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponse {
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
