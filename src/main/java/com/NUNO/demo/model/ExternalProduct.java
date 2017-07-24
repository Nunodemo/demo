package com.NUNO.demo.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class ExternalProduct {

    private String id;

    private String name;

    private BigDecimal usdPrice;

    public ProductVo externalToVo() {
        return ProductVo.builder()
                .name(this.name)
                .price(this.usdPrice)
                .externalId(this.id)
                .build();
    }

}

