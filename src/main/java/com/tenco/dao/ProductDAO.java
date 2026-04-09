package com.tenco.dao;

import com.tenco.dto.Product;
import com.tenco.util.DBConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {


    public List<Product> findAll() throws SQLException {
        List<Product> productList = new ArrayList<>();
        String sql = """
                SELECT * FROM product;
                """;
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                productList.add(setProduct(rs));
            }

        }
        return productList;
    } // end of findAll

    public Product findByBarcode(String barcode) throws SQLException {
        String sql = """
                SELECT * FROM product WHERE barcode = ?;
                """;
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
        ) {
            pstmt.setString(1, barcode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return setProduct(rs);
                } else {
                    throw new SQLException("바코드에 해당하는 상품이 존재하지 않습니다");
                }
            }
        }
    } // end of findByBarcode

    public Boolean insertProduct(String barcode, String name, String category,
                              BigDecimal price, BigDecimal cost, int stock) throws SQLException {
        String sql = """
                INSERT INTO product(barcode, name, category, price, cost, stock)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnectionManager.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1,barcode);
            pstmt.setString(2,name);
            pstmt.setString(3,category);
            pstmt.setBigDecimal(4,price);
            pstmt.setBigDecimal(5,cost);
            pstmt.setInt(6,stock);
            int rowInsert = pstmt.executeUpdate();
            return rowInsert > 0;
        }
    } // end of insertProduct()

    // 4단계 - 상품 수정 (update) 가격, 제고, 유통기한
    public boolean updateProduct(BigDecimal price, int stock,
                                 LocalDate date, String barcode) throws SQLException {
        String sql = """
                UPDATE product
                SET price = ?,
                	stock = ?,
                    expire_date = ?
                WHERE barcode = ?
                """;
        try (Connection connection = DBConnectionManager.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBigDecimal(1,price);
            pstmt.setInt(2, stock);
            pstmt.setDate(3, Date.valueOf(date));
            pstmt.setString(4,barcode);
            int rowUpdate = pstmt.executeUpdate();
            return rowUpdate > 0;
        }
    }

    // 5단계 - 소프트 삭제 (delete)
    public boolean softDelete(String barcode) throws SQLException {
        String sql = """
                UPDATE product
                SET is_active = false
                WHERE barcode = ?
                """;
        try (Connection connection = DBConnectionManager.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1,barcode);
            int rowAvailable = pstmt.executeUpdate();
            return rowAvailable > 0;
        }

    }
    // 6단계 - 재고 부족 상품 조회 (findLowStock)
    public List<Product> findLowStock() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = """
                SELECT * FROM product
                WHERE stock <= min_stock
                """;
        try (Connection connection = DBConnectionManager.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(setProduct(rs));
                }

            }
        }
        return products;
    }

    private Product setProduct(ResultSet rs) throws SQLException {
        Product product = Product.builder()
                .id(rs.getInt("id"))
                .barcode(rs.getString("barcode"))
                .name(rs.getString("name"))
                .category(rs.getString("category"))
                .price(rs.getBigDecimal("price"))
                .cost(rs.getBigDecimal("cost"))
                .stock(rs.getInt("stock"))
                .minStock(rs.getInt("min_stock"))
                .expireDate(rs.getDate("expire_date").toLocalDate())
                .isActive(rs.getBoolean("is_active"))
                .build();
        return product;
    }

    // 테스트 코드
    public static void main(String[] args) {
        ProductDAO productDAO = new ProductDAO();
        try {
            System.out.println(productDAO.findAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

} // end of class
