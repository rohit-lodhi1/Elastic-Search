package com.search.app.payload;

import java.util.List;

import com.search.app.entity.ProductStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
	
	private String id;

	private String name;

	private String description;

	private Double price;

	private String createdBy;

	private String ownerId;

	private List<String> tags;
}
