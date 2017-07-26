package com.NUNO.demo.model;

import com.NUNO.demo.api.generated.dto.ProductRequest;
import com.NUNO.demo.api.generated.dto.ProductResponse;
import com.NUNO.demo.entity.Product;


import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class ProductVo {

    private Long id;

    private String externalId;

    private String name;

    private BigDecimal price;

    public static ProductVo requestToVo(ProductRequest in) {
        return ProductVo.builder().externalId(in.getId()).build();
    }

    public static ProductVo entityToVo(Product in) {
        return ProductVo.builder()
                .id(in.getId())
                .name(in.getName())
                .price(in.getUsdPrice())
                .externalId(in.getExternalId())
                .build();
    }

    public Product voToEntity(Product out) {
        if (out == null) {
            out = new Product();
        }
        out.setName(this.getName());
        out.setExternalId(this.getExternalId());
        out.setUsdPrice(this.getPrice());
        return out;
    }

    public ProductResponse voToResponse() {
        ProductResponse out = new ProductResponse();
        out.setName(this.getName());
        out.setId(this.getExternalId());
        out.setPrice(this.getPrice());
        return out;
    }
}

