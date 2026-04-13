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

public class SalesToday {
    private LocalDate soldAt;
    private String category;
    private int count;
    private BigDecimal totalPrice;
    private BigDecimal profit;
}
