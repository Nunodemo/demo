package com.NUNO.demo.service;

import com.NUNO.demo.model.ForeignExchange;
import com.NUNO.demo.model.PackageVo;
import com.NUNO.demo.model.ProductVo;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForeignExchangeService {

    private String resource = "http://api.fixer.io/latest?symbols=USD,";

    public PackageVo convertCurrency(PackageVo packageVo, String currency) {
        TestRestTemplate restTemplate = new TestRestTemplate("user", "pass");
        ForeignExchange exchange = restTemplate.getForObject(resource + currency, ForeignExchange.class);

        return PackageVo.builder()
                .id(packageVo.getId())
                .name(packageVo.getName())
                .description(packageVo.getDescription())
                .productList(
                        ImmutableList.copyOf(
                                packageVo.getProductList().stream()
                                        .map(productVo -> {
                                                    BigDecimal price = productVo.getPrice();
                                                    if (!price.equals(new BigDecimal(0))) {
                                                        price = currency.equals("EUR") ?
                                                                price.divide(exchange.getRates().get("USD"), MathContext.DECIMAL64) :
                                                                price.divide(exchange.getRates().get("USD"), MathContext.DECIMAL64).multiply(exchange.getRates().get(currency));
                                                    }

                                                    price = price.setScale(2, RoundingMode.CEILING);

                                                    return ProductVo.builder().id(productVo.getId())
                                                            .externalId(productVo.getExternalId())
                                                            .name(productVo.getName())
                                                            .price(price)
                                                            .build();
                                                }
                                        )
                                        .collect(Collectors.<ProductVo>toList()))
                )
                .build();
    }

}
