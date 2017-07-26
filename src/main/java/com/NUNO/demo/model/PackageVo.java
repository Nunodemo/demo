package com.NUNO.demo.model;

import com.NUNO.demo.api.generated.dto.PackageRequest;
import com.NUNO.demo.api.generated.dto.PackageResponse;
import com.NUNO.demo.api.generated.dto.ProductResponse;
import com.NUNO.demo.entity.Package;
import com.NUNO.demo.entity.Product;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Builder
@Getter
public class PackageVo {

    private Long id;

    private String name;

    private String description;

    private ImmutableList<ProductVo> productList;

    public static PackageVo requestToVo(PackageRequest in) {
        return PackageVo.builder()
                .name(in.getName())
                .description(in.getDescription())
                .productList(
                        ImmutableList.copyOf(
                                in.getProducts().stream()
                                        .map(ProductVo::requestToVo)
                                        .collect(Collectors.<ProductVo>toList()))
                )
                .build();
    }

    public static PackageVo entityToVo(Package in) {
        return PackageVo.builder()
                .id(in.getId())
                .name(in.getName())
                .description(in.getDescription())
                .productList(
                        ImmutableList.copyOf(
                                in.getProductList().stream()
                                        .map(ProductVo::entityToVo)
                                        .collect(Collectors.<ProductVo>toList()))
                )
                .build();
    }

    public Package voToEntity(Package out) {
        if (out == null) {
            out = new Package();
        }
        out.setName(this.name);
        out.setDescription(this.description);

        out.setProductList(
                this.getProductList().stream().map(productVo -> productVo.voToEntity(null)
                ).collect(Collectors.<Product>toList())
        );

        return out;
    }

    public PackageResponse voToResponse() {

        return new PackageResponse().id(this.id)
                .name(this.name)
                .description(this.description)
                .price(this.productList.stream()
                        .map(ProductVo::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .products(this.productList.stream()
                        .map(ProductVo::voToResponse)
                        .collect(Collectors.<ProductResponse>toList()));
    }
}

