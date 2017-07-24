package com.NUNO.demo.service;

import com.NUNO.demo.entity.Product;
import com.NUNO.demo.exception.ProductNotFoundException;
import com.NUNO.demo.model.ExternalProduct;
import com.NUNO.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private String resource = "https://product-service.herokuapp.com/api/v1/products";

    public void populateDbFromResource() {
        TestRestTemplate restTemplate = new TestRestTemplate("user", "pass");
        productRepository.save(
                Arrays.stream(restTemplate.getForObject(resource, ExternalProduct[].class))
                        .map(ExternalProduct::externalToVo)
                        .map(product -> product.voToEntity(null))
                        .filter(product -> productRepository.findByExternalId(product.getExternalId()) == null)
                        .collect(Collectors.toList())
        );
    }

    public Product getExistingProduct(String externalId) {
        return Optional.ofNullable(productRepository.findByExternalId(externalId))
                .orElseThrow(() -> new ProductNotFoundException(externalId));
    }


}
