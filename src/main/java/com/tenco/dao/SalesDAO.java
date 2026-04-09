package com.tenco.dao;

import com.tenco.dto.Product;
import com.tenco.dto.Sales;
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
            if(quantity <= 0){
                throw new SQLException("상품개수가 0개 이하일수 없습니다");
            }
            String checkSql = """
                    SELECT * FROM product WHERE barcode = ?;
                    """;
            try (PreparedStatement checkPstmt = connection.prepareStatement(checkSql)) {
                checkPstmt.setString(1,product.getBarcode());
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if(rs.next() == false) {
                        throw new SQLException("존재하지 않는 상품입니다.");
                    }
                    if(rs.getBoolean("is_active") == false) {
                        throw new SQLException("판매 중단 상품이거나 품절 상품입니다");
                    }
                    if (rs.getInt("stock") <= quantity){
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
                salePstmt.setInt(2,quantity);
                salePstmt.setBigDecimal(3,product.getPrice());
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
                updatePstmt.setInt(1,quantity);
                updatePstmt.setInt(2,product.getId());
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
    public List<Sales> findTodaySales() throws SQLException {
        List<Sales> salesList = new ArrayList<>();
        String sql = """
                SELECT
                	s.id,
                    s.product_id,
                    p.name,
                    s.quantity,
                    s.unit_price,
                    s.sold_at,
                    SUM(s.quantity * s.unit_price) total
                FROM sales s
                JOIN product p on s.product_id = p.id
                GROUP BY s.id;
                """;
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Sales sales = Sales.builder()
                        .id(rs.getInt("id"))
                        .productId(rs.getInt("product_id"))
                        .productName(rs.getString("name"))
                        .quantity(rs.getInt("quantity"))
                        .unitPrice(rs.getBigDecimal("total"))
                        .soldAt(rs.getDate("sold_at").toLocalDate())
                        .build();
                salesList.add(sales);
            }
        }
        return salesList;
    }

    // 테스트 코드
    public static void main(String[] args) throws SQLException {
        SalesDAO sales = new SalesDAO();
        ProductDAO productDAO = new ProductDAO();
        Product product = productDAO.findByBarcode("8801234560006");
        try {
            sales.processSale(product,1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
