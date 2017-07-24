package com.NUNO.demo.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Builder
@Getter
public class ForeignExchange {

    private String base;

    private String date;

    private Map<String, BigDecimal> rates;

}

