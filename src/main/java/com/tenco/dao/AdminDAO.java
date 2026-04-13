package com.tenco.dao;

import com.tenco.dto.Admin;
import com.tenco.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {
    public Admin login(String adminId, String password) throws SQLException {
        String sql = """
                SELECT * FROM admins
                WHERE admin_id = ? AND password = ?
                """;

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setString(1, adminId);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Admin admin = Admin.builder()
                            .id(rs.getInt("id"))
                            .adminId(rs.getString("admin_id"))
                            .name(rs.getString("name"))
                            .build();
                    return admin;
                } else {
                    throw new SQLException("관리자 아이디 혹은 비밀번호를 확인하세요");
                }
            }
        }
    }

}
