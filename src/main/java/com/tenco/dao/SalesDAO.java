package com.tenco.dao;

import com.tenco.dto.Product;
import com.tenco.dto.Sales;
import com.tenco.dto.SalesToday;
import com.tenco.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {

    //    판매 처리 (processSale)
    public boolean processSale(Product product, int quantity) throws SQLException {
        Connection connection = null;
        try {
            connection = DBConnectionManager.getConnection();
            connection.setAutoCommit(false);
            // 1. 상품이 존재하지 않고 소프트 삭제가 되어있다 -> 판매불가
            if (quantity <= 0) {
                throw new SQLException("상품개수가 0개 이하일수 없습니다");
            }
            String checkSql = """
                    SELECT * FROM product WHERE barcode = ?;
                    """;
            try (PreparedStatement checkPstmt = connection.prepareStatement(checkSql)) {
                checkPstmt.setString(1, product.getBarcode());
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next() == false) {
                        throw new SQLException("존재하지 않는 상품입니다.");
                    }
                    if (rs.getBoolean("is_active") == false) {
                        throw new SQLException("판매 중단 상품이거나 품절 상품입니다");
                    }
                    if (rs.getInt("stock") <= quantity ||
                            rs.getInt("stock") <= rs.getInt("min_stock")) {
                        throw new SQLException("재고가 부족합니다");
                    }
                }
            }
            // 2. 상품 판매
            String saleSql = """
                    INSERT INTO sales(product_id, quantity, unit_price, sold_at)
                    VALUES(?, ?, ?, ?)
                    """;
            try (PreparedStatement salePstmt = connection.prepareStatement(saleSql)) {
                salePstmt.setInt(1, product.getId());
                salePstmt.setInt(2, quantity);
                salePstmt.setBigDecimal(3, product.getPrice());
                salePstmt.setDate(4, Date.valueOf(LocalDate.now()));
                salePstmt.executeUpdate();
            }
            // 3. 판매 개수만큼 상품 목록 개수 업데이트 -> 추후 추가 만약 상품 개수가 0개가 되었다면 soft삭제
            String updateSql = """
                        UPDATE product
                        SET stock = stock - ?
                        WHERE id = ?
                    """;
            try (PreparedStatement updatePstmt = connection.prepareStatement(updateSql)) {
                updatePstmt.setInt(1, quantity);
                updatePstmt.setInt(2, product.getId());
                updatePstmt.executeUpdate();
            }
            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            System.out.println("오류발생: " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    //    오늘 매출 조회 (findTodaySales)
    public List<SalesToday> findTodaySales() throws SQLException {
        List<SalesToday> salesList = new ArrayList<>();
        String sql = """
                SELECT
                    DATE(s.sold_at) AS sold_at,
                    p.category,
                    SUM(s.quantity) AS sales_count,
                    SUM(s.quantity * s.unit_price) total_price,
                    SUM(s.quantity * (s.unit_price - p.cost)) AS profit
                FROM sales s
                JOIN product p ON s.product_id = p.id
                GROUP BY DATE (s.sold_at),p.category
                ORDER BY sold_at;
                """;
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                SalesToday sales = SalesToday.builder()
                        .soldAt(rs.getDate("sold_at").toLocalDate())
                        .category(rs.getString("category"))
                        .count(rs.getInt("sales_count"))
                        .totalPrice(rs.getBigDecimal("total_price"))
                        .profit(rs.getBigDecimal("profit"))
                        .build();
                salesList.add(sales);
            }
        }
        return salesList;
    }


}
