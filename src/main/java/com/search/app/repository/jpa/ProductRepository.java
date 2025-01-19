package com.search.app.repository.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.search.app.entity.Product;

public interface ProductRepository extends JpaRepository<Product, String>{

	public Optional<Product> findByIdAndIsDeletedAndIsActive(String id,Boolean isDeleted,Boolean isActive);
}
