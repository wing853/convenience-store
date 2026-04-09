package com.tenco.util;

// DB 접근해서 Connection 객체를 생성하는 역할의 클래스이다.
// 싱글톤 패턴: 프로그램 전체에서 단 하나의 인스턴스만을 존재하게하는 기법


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnectionManager {
    // static: 클래스 레벨에서 하나만 유지하는 커넥션 풀
    private static final HikariDataSource dataSource;

    // static 초기화 블록: 클래스가 메모리에 로딩될때 단 한번만 실행을 보장
    static {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://localhost:3306/convenience_store");
        config.setUsername(System.getenv("DB_USER"));
        config.setPassword(System.getenv("DB_PASSWORD"));

        config.setMaximumPoolSize(10); // 동시에 유지할 연결 최대 개수
        config.setMinimumIdle(3); // 확보할 최소 유후 커넥션 수
        config.setConnectionTimeout(10000); // 커넥션 할당 대기시간 제한(5초)
        config.setIdleTimeout(600000); // 유후 커넥션 유지 시간(10분)

        dataSource = new HikariDataSource(config);
    }

    // 사용자가 호출하는 메서드 생성: 풀에서 커넥션 객체 하나를 빌려온다.
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // 애플리케이션 종료 시 커넥션 풀 완전히 닫는다.
    public static void close() {
        if(dataSource!= null && !dataSource.isClosed()){
            dataSource.close();
        }
    }

    // 테스트 코드 작성
//    public static void main(String[] args) {
//        try {
//            DBConnectionManager.getConnection();
//            Thread.sleep(100000);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
