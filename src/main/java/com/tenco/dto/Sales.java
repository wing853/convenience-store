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
public class Sales {
    private int id;
    private int productId;
    private String productName; // JOIN시 사용
    private int quantity;
    private BigDecimal unitPrice;
    private LocalDate soldAt;

}
