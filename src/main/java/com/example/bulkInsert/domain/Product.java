package com.example.bulkInsert.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String Title;

    @Column
    private Integer price;

    @Column
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column
    private Integer starRating;

    @Column
    private Integer discountRate;

    @Column
    private LocalDateTime discountStart;

    @Column
    private LocalDateTime discountEnd;

    @Column
    private LocalDateTime createAt;
}
