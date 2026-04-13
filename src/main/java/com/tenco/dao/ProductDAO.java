package com.tenco.dao;

import com.tenco.dto.Product;
import com.tenco.util.DBConnectionManager;

import java.sql.*;
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

    public boolean insertProduct(Product product) throws SQLException {
        String sql = """
                INSERT INTO product(barcode, name, category, price, cost, stock,expire_date)
                VALUES (?, ?, ?, ?, ?, ?,?)
                """;
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getBarcode());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getCategory());
            pstmt.setBigDecimal(4, product.getPrice());
            pstmt.setBigDecimal(5, product.getCost());
            pstmt.setInt(6, product.getStock());
            pstmt.setDate(7, Date.valueOf(product.getExpireDate()));
            int rowInsert = pstmt.executeUpdate();
            return rowInsert > 0;
        }
    } // end of insertProduct()

    // 4단계 - 상품 수정 (update) 가격, 제고, 유통기한
    public boolean updateProduct(Product product) throws SQLException {
        String sql = """
                UPDATE product
                SET price = ?,
                	stock = ?,
                    expire_date = ?
                WHERE barcode = ?
                """;
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, product.getPrice());
            pstmt.setInt(2, product.getStock());
            pstmt.setDate(3, Date.valueOf(product.getExpireDate()));
            pstmt.setString(4, product.getBarcode());
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
            pstmt.setString(1, barcode);
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

    // 소프트 삭제 변경
    public boolean changeStatus(String barcode) throws SQLException {
        String sql = """
                UPDATE product
                SET is_active = true
                WHERE barcode = ?
                AND is_active = false
                AND expire_date > curdate();
                """;
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, barcode);
            int rowAvailable = pstmt.executeUpdate();
            return rowAvailable > 0;
        }

    }

    public void addStock(String barcode, int stock) throws SQLException {
        String sql = """
                UPDATE product
                SET stock = stock + ?
                WHERE barcode = ?
                """;
        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, stock);
            pstmt.setString(2, barcode);
            pstmt.executeUpdate();

        }
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

} // end of class
