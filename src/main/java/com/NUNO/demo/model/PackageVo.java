package com.NUNO.demo.model;

import com.NUNO.demo.entity.Package;
import com.NUNO.demo.entity.Product;
import com.google.common.collect.ImmutableList;
import io.swagger.model.PackageRequest;
import io.swagger.model.PackageResponse;
import io.swagger.model.ProductResponse;
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
        out.setName(this.getName());
        out.setDescription(this.getDescription());

        out.setProductList(
                this.getProductList().stream().map((ProductVo productVo) -> {
                    return productVo.voToEntity(null);
                }).collect(Collectors.<Product>toList())
        );

        return out;
    }

    public PackageResponse voToResponse() {
        PackageResponse out = new PackageResponse();
        out.setId(this.getId());
        out.setName(this.getName());
        out.setDescription(this.getDescription());
        out.setPrice(this.getProductList().stream()
                .map(ProductVo::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        out.setProducts(this.getProductList().stream()
                .map(ProductVo::voToResponse)
                .collect(Collectors.<ProductResponse>toList())
        );
        return out;
    }
}

