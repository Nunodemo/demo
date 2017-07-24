package com.NUNO.demo.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "package")
@NoArgsConstructor
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    @Getter
    private Long id;

    @Column(name = "name")
    @Getter
    @Setter
    private String name;

    @Column(name = "description")
    @Getter
    @Setter
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "package_product",
            joinColumns =
            @JoinColumn(name = "package_id", referencedColumnName = "package_id"),
            inverseJoinColumns =
            @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    )
    @Getter
    @Setter
    private List<Product> productList;

}

