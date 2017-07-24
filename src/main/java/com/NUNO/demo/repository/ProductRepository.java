package com.NUNO.demo.repository;

import com.NUNO.demo.entity.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {

    Product findByExternalId(String externalId);
}
