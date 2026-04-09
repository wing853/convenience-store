package com.tenco.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Product {
    private int id;
    private String barcode;
    private String name;
    private String category;
    private BigDecimal price;
    private BigDecimal cost;
    private int stock;
    private int minStock;
    private LocalDate expireDate;
    private boolean isActive;
}