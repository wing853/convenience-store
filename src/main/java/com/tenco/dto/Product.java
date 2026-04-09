package com.tenco.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

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
