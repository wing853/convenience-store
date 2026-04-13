package com.tenco.service;

import com.tenco.dao.AdminDAO;
import com.tenco.dao.ProductDAO;
import com.tenco.dao.SalesDAO;
import com.tenco.dto.Admin;
import com.tenco.dto.Product;
import com.tenco.dto.Sales;
import com.tenco.dto.SalesToday;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class StoreService {
    private Integer currentId = null;
    AdminDAO adminDAO = new AdminDAO();
    Admin admin;
    ProductDAO productDAO = new ProductDAO();
    SalesDAO salesDAO = new SalesDAO();

    // 1. 관리자 로그인
    public void login(String adminId, String password) throws SQLException {
        if (adminId.trim().isEmpty()) {
            throw new SQLException("아이디를 입력하세요");
        }
        if (password.trim().isEmpty()) {
            throw new SQLException("비밀번호를 입력하세요");
        }
        admin = adminDAO.login(adminId, password);
        currentId = admin.getId();
    }

    // 2. 로그인 상태 확인
    public boolean isLogin() {
        if (currentId != null) {
            return true;
        } else {
            return false;
        }
    }

    // 3. 관리자 로그아웃
    public void logout() {
        if (isLogin()) {
            admin = null;
            currentId = null;
        }
    }

    // 4. 상품 목록 조회
    public List<Product> getProductList() throws SQLException {
        return productDAO.findAll();
    }

    // 5. 판매 처리
    public String processSale(String barcode, int quantity) throws SQLException {
        if (productDAO.findByBarcode(barcode) == null) {
            return ("상품이 존재 하지 않습니다");
        }

        Product product = productDAO.findByBarcode(barcode);
        if (isLowStock(product)) {
            return "재고가 부족합니다.";
        }

        if (isNearExpiry(product)) {
            return "유통기한이 입박했습니다";
        }

        if(!product.isActive()){
            return "판매 불가 상품입니다.";
        }

        if (salesDAO.processSale(product, quantity)) {
            return product.getName() + "이 " + quantity + "개 판매 되었습니다.";
        } else {
            return "판매가 완료 되지 않았습니다. 다시시도하십시요";
        }
    }

    // 6. 재고 부족 판별
    public boolean isLowStock(Product product) {
        return product.getStock() <= product.getMinStock();
    }

    // 7. 재고 부족 물품 리스트
    public List<Product> getLowStockProducts() throws SQLException {
        List<Product> products = productDAO.findAll();
        List<Product> lowStockList = new ArrayList<>();

       for(Product p : products){
           if(isLowStock(p)) {
               lowStockList.add(p);
           }
       }

        return lowStockList;
    }

    // 8. 유통기한 임박 판별
    public boolean isNearExpiry(Product product) throws SQLException {
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), product.getExpireDate());
        if (daysBetween <= 0) {
            productDAO.softDelete(product.getBarcode());
            return true;
        } else return daysBetween <= 2;
    }

    // 9. 유통기한 판별 제품 리스트
    public List<Product> getNearExpiry() throws SQLException {
        List<Product> products = productDAO.findAll();
        List<Product> nearProducts = new ArrayList<>();
        for(Product p : products) {
            if(isNearExpiry(p)) {
               nearProducts.add(p);
            }
        }
        return nearProducts;
    }

    // 10. 관리자 이름
    public String getCurrentAdminName() {
        return admin.getName();
    }

    // 11. 바코드 상품 조회
    public Product findByBarcode(String barcode) throws SQLException {
        return  productDAO.findByBarcode(barcode);
    }

    // 12. 상품 추가
    public void addProduct(Product product) throws SQLException {
        productDAO.insertProduct(product);
    }

    // 13. 상품 수정
    public void updateProduct(Product product) throws SQLException {
        productDAO.updateProduct(product);
    }

    // 14. 소프트 삭제
    public void softDelete(Product product) throws SQLException {
        productDAO.softDelete(product.getBarcode());
    }

    // 15. 매출 조회
    public List<SalesToday> getTodaySales() throws SQLException {
        return salesDAO.findTodaySales();
    }

    public List<Product> getProductListWithStatus() throws SQLException {
        List<Product> list = productDAO.findAll();

        for (Product p : list) {
            String status = "";

            if (p.getExpireDate() != null) {
                if (p.getExpireDate().isBefore(LocalDate.now())) {
                    status += "[유통기한 만료], [판매불가]";
                    p.setActive(false);
                } else if (p.getExpireDate().isBefore(LocalDate.now().plusDays(3))) {
                    status += "[유통기한 임박] ";
                }
            }

            if (p.getStock() <= p.getMinStock() && p.getStock() != 0) {
                if(status.trim().isEmpty()) {
                    status += "[재고 부족]";
                } else {
                    status += ", [재고 부족]";
                }
            }

            if(p.getStock() == 0 ){
                if(status.trim().isEmpty()) {
                    status += "[재고 없음],[판매불가]";
                    p.setActive(false);
                } else {
                    status += ", [재고 없음]";
                }
            }

            p.setStatus(status);
        }
        return list;
    }

    // 16. 소프트삭제 상태 변경
    public boolean changeStatus(String barcode) throws SQLException {
        return productDAO.changeStatus(barcode);
    }

    public void addStock(String barcode, int stock) throws SQLException {
        productDAO.addStock(barcode,stock);
    }
}
