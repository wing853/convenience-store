package com.tenco.view;

import com.tenco.dto.Product;
import com.tenco.dto.SalesToday;
import com.tenco.service.StoreService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;

public class StoreView {
    // 실행
    private final StoreService service = new StoreService();
    private final Scanner scanner = new Scanner(System.in);
    private final int MINSTOCK = 5;

    public void start() {
        while (true) {
            printMenu();
            int choice = readInt("선택:");
            try {
                switch (choice) {
                    case 0:
                        System.out.println("프로그램 종료");
                        return;
                    case 1:
                        login();
                        break;
                    case 2:
                        logout();
                        break;
                    case 3:
                        getProductList();
                        break;
                    case 4:
                        checkStock();
                        break;
                    case 5:
                        saleProduct();
                        break;
                    case 6:
                        getNearExpiry();
                        break;
                    case 7:
                        addProduct();
                        break;
                    case 8:
                        updateProduct();
                        break;
                    case 9:
                        softDelete();
                        break;
                    case 10:
                        getTodaySales();
                        break;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void printMenu() {
        System.out.println("===== 편의점 재고 관리 시스템=====");
        System.out.println("0. 종료");
        System.out.println("1. 로그인");
        System.out.println("2. 로그아웃");
        System.out.println("3. 상품 목록");
        System.out.println("4. 재고 부족 물품 조회");
        System.out.println("5. 상품 판매");
        System.out.println("6. 유통기한 임박 조회");
        System.out.println("7. 상품 추가");
        System.out.println("8. 상품 수정");
        System.out.println("9. 상품 삭제");
        System.out.println("10. 매출 조회");
    }

    // 로그인 시스템
    public void login() throws SQLException {
        if (service.isLogin() == false) {
            System.out.print("아이디를입력하세요: ");
            String adminId = scanner.nextLine().trim();
            System.out.print("비밀번호를 입력하세요:");
            String password = scanner.nextLine().trim();
            service.login(adminId, password);
        } else {
            System.out.println("현재 로그인 상태입니다.");
        }
    }

    public void logout() {
        service.logout();
    }

    public void getProductList() throws SQLException {
        List<Product> products = service.getProductList();
        for (Product p : products) {
            System.out.println(p);
        }
    }

    //재고 판단
    public void checkStock() throws SQLException {
        List<Product> lowStockList = service.getLowStockProducts();
        for (Product p : lowStockList) {
            System.out.println(p);
        }
        System.out.println("현재 재고 부족 물품: " + lowStockList.size() + "개");
    }

    // 유통기한 기간 판단
    public void getNearExpiry() throws SQLException {
        int past = 0;
        int near = 0;
        List<Product> nearExpiryProducts = service.getNearExpiry();
        for (Product p : nearExpiryProducts) {
            long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), p.getExpireDate());
            if (daysBetween <= 0) {
                System.out.println(p.getName() + "은 유통기한이 지났습니다. 폐기 바랍니다");
                past++;
            } else {
                System.out.println(p.getName() + "은 유통기한이 입박했습니다.");
                near++;
            }
        }
        System.out.println("유통기한 임박 제품: " + near + "개");
        System.out.println("유통기한 지난 제품: " + past + "개");
    }

    // 상품 판매
    public void saleProduct() throws SQLException {
        System.out.print("바코드 입력: ");
        String barcode = scanner.next().trim();
        System.out.println("개수 입력: ");
        int quantity = scanner.nextInt();
        Product product = service.findByBarcode(barcode);
        if (product != null) {
            service.processSale(barcode, quantity);
            System.out.println(product.getName() + "상품이 " + quantity + "개 판매되었습니다");
        } else {
            System.out.println("상품 판매가 완료 되지 않았습니다.");
        }
    }

    // 상품 추가
    public void addProduct() throws SQLException {
        if (!service.isLogin()) {
            System.out.println("관리자만 이용 가능합니다. 로그인을 먼저 해주세요");
            login();
        }

        Product product = new Product();
        // 바코드
        while (true) {
            System.out.print("바코드: ");
            String barcode = scanner.nextLine().trim();

            if (barcode.isEmpty()) {
                System.out.println("바코드는 필수입니다.");
                continue;
            }
            product.setBarcode(barcode);
            break;
        }

        // 상품명
        while (true) {
            System.out.print("상품명: ");
            String name = scanner.nextLine().trim();

            if (name.isEmpty()) {
                System.out.println("상품명은 필수입니다.");
                continue;
            }

            product.setName(name);
            break;
        }

        // 카테고리
        while (true) {
            System.out.print("카테고리: ");
            String category = scanner.nextLine().trim();

            if (category.isEmpty()) {
                System.out.println("카테고리는 필수입니다.");
                continue;
            }

            product.setCategory(category);
            break;
        }

        // 가격
        while (true) {
            try {
                System.out.print("가격: ");
                BigDecimal price = new BigDecimal(scanner.nextLine().trim());

                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("가격은 0보다 커야 합니다.");
                    continue;
                }

                product.setPrice(price);
                break;
            } catch (Exception e) {
                System.out.println("숫자로 입력하세요.");
            }
        }

        // 원가
        while (true) {
            try {
                System.out.print("원가: ");
                BigDecimal cost = new BigDecimal(scanner.nextLine().trim());

                if (cost.compareTo(BigDecimal.ZERO) < 0) {
                    System.out.println("원가는 0 이상이어야 합니다.");
                    continue;
                }

                product.setCost(cost);
                break;
            } catch (Exception e) {
                System.out.println("숫자로 입력하세요.");
            }
        }

        // 재고
        while (true) {
            try {
                System.out.print("재고수량: ");
                int stock = Integer.parseInt(scanner.nextLine().trim());

                if (stock < 0) {
                    System.out.println("재고는 0 이상이어야 합니다.");
                    continue;
                }

                product.setStock(stock);
                break;
            } catch (Exception e) {
                System.out.println("숫자로 입력하세요.");
            }
        }

        // 최소재고 (옵션)
        while (true) {
            try {
                System.out.print("최소재고: ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    product.setMinStock(MINSTOCK);
                } else {
                    int minStock = Integer.parseInt(input);
                    if (minStock < 0) {
                        System.out.println("최소재고는 0 이상이어야 합니다.");
                        continue;
                    }
                    product.setMinStock(minStock);
                }
                break;
            } catch (Exception e) {
                System.out.println("숫자로 입력하세요.");
            }
        }

        // 유통기한
        while (true) {
            try {
                System.out.print("유통기한 (yyyy-MM-dd, 공백이면 미설정): ");
                String input = scanner.nextLine().trim();

                // 입력 안 했을 때 (기본값)
                if (input.isEmpty()) {
                    product.setExpireDate(LocalDate.of(9999, 12, 31));
                    break;
                }

                LocalDate date = LocalDate.parse(input);

                if (date.isBefore(LocalDate.now())) {
                    System.out.println("유통기한이 이미 지났습니다.");
                    continue;
                }

                product.setExpireDate(date);
                break;

            } catch (Exception e) {
                System.out.println("날짜 형식이 올바르지 않습니다.");
            }
        }

        // 판매여부
        while (true) {
            System.out.print("판매여부 (true/false): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (!input.equals("true") && !input.equals("false")) {
                System.out.println("true 또는 false로 입력하세요.");
                continue;
            }

            product.setActive(Boolean.parseBoolean(input));
            break;
        }
        service.addProduct(product);
        System.out.println(product.getName() + "상품이" + product.getName() + "개 추가 되었습니다");
    }

    // 상품 수정
    public void updateProduct() throws SQLException {
        if (!service.isLogin()) {
            System.out.println("관리자만 이용 가능합니다. 로그인을 먼저 해주세요");
            login();
        }

        System.out.print("바코드를 입력하세요: ");
        String barcode = scanner.nextLine().trim();
        Product product = service.findByBarcode(barcode);

        System.out.println("수정 전 정보");
        System.out.println("상품명: " + product.getName());
        System.out.println("상품가격: " + product.getPrice());
        System.out.println("유통기한: " + product.getExpireDate());
        System.out.println("수량: " + product.getStock());

        service.updateProduct(product);
        System.out.println(product.getName() + "의 상품 수정 정보 입력");
        System.out.print("가격: ");
        BigDecimal rePrice = BigDecimal.valueOf(scanner.nextInt());
        if (rePrice == null || rePrice.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("가격 정보에 이상이 있어 변경되지 않았습니다.");
        } else {
            product.setPrice(rePrice);
        }
        System.out.print("유통기한: ");
        String reDate = scanner.next().trim();

        if (!reDate.isEmpty()) {
            try {
                LocalDate newDate = LocalDate.parse(reDate);

                if (newDate.isBefore(LocalDate.now())) {
                    System.out.println("유통기한이 이미 지났습니다. 수정 안됨.");
                } else {
                    product.setExpireDate(newDate);
                }

            } catch (Exception e) {
                System.out.println("날짜 형식이 올바르지 않습니다. (변경 안됨)");
            }
        }

        System.out.print("수량: ");
        int reStock = scanner.nextInt();
        try {
            if (reStock < 0) {
                System.out.println("0이상의 값을 입력하지않아 수량이 변경되지 않았습니다");
            } else if (reStock == 0) {
                System.out.println("모든 상품이 판매되어 판매 중지 합니다");
                product.setStock(reStock);
                product.setActive(false);
            } else {
                product.setStock(reStock);
            }
        } catch (Exception e) {
            System.out.println("숫자를 입력하지 않아 수정되지 않았습니다");
        }

        service.updateProduct(product);
        System.out.println("수정 후 목록");
        System.out.println("상품명: " + product.getName());
        System.out.println("상품가격: " + product.getPrice());
        System.out.println("유통기한: " + product.getExpireDate());
        System.out.println("수량: " + product.getStock());

        System.out.println("상품이 수정 되었습니다.");
    }

    // 소프트 삭제
    public void softDelete() throws SQLException {
        if (!service.isLogin()) {
            System.out.println("관리자만 이용 가능합니다. 로그인을 먼저 해주세요");
            login();
        }

        List<Product> products = service.getProductList();
        for (Product p : products) {
            if (p.getStock() <= 0 || ChronoUnit.DAYS.between(LocalDate.now(), p.getExpireDate()) <= 0) {
                System.out.println(p.getName() + "상품 품절 이거나 유통기한이 지난제품을 소프트 삭제합니다.");
                service.softDelete(p);
            }
        }
    }

    // 날짜별 판매 매출 정보
    public void getTodaySales() throws SQLException {
        if (!service.isLogin()) {
            System.out.println("관리자만 이용 가능합니다. 로그인을 먼저 해주세요");
            login();
        }
        List<SalesToday> todaySales = service.getTodaySales();
        for (SalesToday st : todaySales) {
            System.out.println("날짜: " + st.getSoldAt() + " | 품목: " + st.getCategory()
                    + " | 총 판매수량: " + st.getCount() + " | 총 판매액: " + st.getTotalPrice()
                    + " | 순이익: " + st.getProfit());
        }
    }

    // 숫자입력
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }

    }
}
