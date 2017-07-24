package com.NUNO.demo.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product")
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    @Getter
    private Long id;


    @Column(name = "external_id", unique = true)
    @Getter
    @Setter
    private String externalId;

    @Column(name = "name")
    @Getter
    @Setter
    private String name;

    @Column(name = "usd_price")
    @Getter
    @Setter
    private BigDecimal usdPrice;


}

