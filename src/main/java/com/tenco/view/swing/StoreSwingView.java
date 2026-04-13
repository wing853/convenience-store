package com.tenco.view.swing;

import com.tenco.dto.Product;
import com.tenco.dto.SalesToday;
import com.tenco.service.StoreService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StoreSwingView extends JFrame {

    private final StoreService service = new StoreService();

    private JTable table;
    private DefaultTableModel model;

    // 🔥 선택된 상품 바코드
    private String selectedBarcode;

    public StoreSwingView() {
        setTitle("편의점 재고 관리 시스템");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ===== 상단 =====
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(30, 30, 30));

        JButton loginBtn = new JButton("로그인");
        JButton logoutBtn = new JButton("로그아웃");
        JButton loadBtn = new JButton("상품 조회");

        styleButton(loginBtn);
        styleButton(logoutBtn);
        styleButton(loadBtn);

        topPanel.add(loginBtn);
        topPanel.add(logoutBtn);
        topPanel.add(loadBtn);

        add(topPanel, BorderLayout.NORTH);

        // ===== 테이블 =====
        String[] columns = {"바코드", "상품명", "가격", "재고", "유통기한", "상태"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);

        table.setRowHeight(25);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 15));

        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        // 🔥 선택 이벤트 (핵심)
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;

            selectedBarcode = model.getValueAt(row, 0).toString();
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== 오른쪽 버튼 =====
        JPanel rightPanel = new JPanel(new GridLayout(10, 1, 10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton stockBtn = new JButton("재고 부족");
        JButton stockAdd = new JButton("재고 추가");
        JButton expiryBtn = new JButton("유통기한 임박");
        JButton saleBtn = new JButton("판매");
        JButton addBtn = new JButton("상품 추가");
        JButton updateBtn = new JButton("상품 수정");
        JButton deleteBtn = new JButton("삭제");
        JButton changeStatusBtn = new JButton("판매 가능 변경");
        JButton salesBtn = new JButton("매출 조회");

        JButton[] buttons = {
                stockBtn, stockAdd, expiryBtn, saleBtn,
                addBtn, updateBtn, deleteBtn,
                changeStatusBtn, salesBtn
        };

        for (JButton btn : buttons) {
            styleButton(btn);
            rightPanel.add(btn);
        }

        add(rightPanel, BorderLayout.EAST);

        // ===== 이벤트 =====

        loginBtn.addActionListener(e -> {
            if (service.isLogin()) {
                JOptionPane.showMessageDialog(this, "이미 로그인 중입니다.");
                return;
            }
            login();
        });

        logoutBtn.addActionListener(e -> {
            if (!service.isLogin()) {
                JOptionPane.showMessageDialog(this, "로그인되어 있지 않습니다.");
                return;
            }
            service.logout();
            JOptionPane.showMessageDialog(this, "로그아웃 완료");
        });

        loadBtn.addActionListener(e -> loadProducts());
        stockBtn.addActionListener(e -> checkStock());
        stockAdd.addActionListener(e -> addStock());
        expiryBtn.addActionListener(e -> checkExpiry());
        saleBtn.addActionListener(e -> saleProduct());

        addBtn.addActionListener(e -> requireLogin(this::addProduct));
        updateBtn.addActionListener(e -> requireLogin(this::updateProduct));
        deleteBtn.addActionListener(e -> requireLogin(this::softDelete));
        changeStatusBtn.addActionListener(e -> requireLogin(this::changeStatus));
        salesBtn.addActionListener(e -> requireLogin(this::getSales));
    }

    // ===== 상태 컬러 =====
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (value != null) {
                String status = value.toString();

                if (status.contains("만료") || status.contains("불가")) {
                    c.setForeground(Color.RED);
                } else if (status.contains("임박")) {
                    c.setForeground(new Color(255, 140, 0));
                } else if (status.contains("부족")) {
                    c.setForeground(Color.BLUE);
                } else {
                    c.setForeground(Color.BLACK);
                }
            }

            return c;
        }
    }

    // ===== 버튼 스타일 =====
    private void styleButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
    }

    // ===== 공통 =====
    private void clearTable() {
        model.setRowCount(0);
    }

    private void showProducts(List<Product> list) {
        clearTable();

        for (Product p : list) {
            model.addRow(new Object[]{
                    p.getBarcode(),
                    p.getName(),
                    p.getPrice(),
                    p.getStock(),
                    p.getExpireDate(),
                    p.getStatus()
            });
        }
    }

    private void requireLogin(Runnable action) {
        if (!service.isLogin()) {
            JOptionPane.showMessageDialog(this, "로그인이 필요합니다.");
            login();
            return;
        }
        action.run();
    }

    // ===== 기능 =====

    private void login() {
        JTextField idField = new JTextField();
        JPasswordField pwField = new JPasswordField();

        int option = JOptionPane.showConfirmDialog(this,
                new Object[]{"아이디:", idField, "비밀번호:", pwField},
                "로그인", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                service.login(idField.getText(), new String(pwField.getPassword()));
                JOptionPane.showMessageDialog(this, "로그인 성공");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "로그인 실패");
            }
        }
    }

    private void loadProducts() {
        try {
            showProducts(service.getProductListWithStatus());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "상품 조회 실패");
        }
    }

    private void checkStock() {
        try {
            showProducts(service.getLowStockProducts());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "재고 조회 실패");
        }
    }

    private void checkExpiry() {
        try {
            showProducts(service.getNearExpiry());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "유통기한 조회 실패");
        }
    }

    // ===== 선택 기반 판매 =====
    private void saleProduct() {
        if (selectedBarcode == null) {
            JOptionPane.showMessageDialog(this, "상품을 선택하세요.");
            return;
        }

        try {
            Product p = service.findByBarcode(selectedBarcode);

            if (p.getExpireDate().isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "유통기한이 지난 상품입니다.");
                return;
            }

            String qtyStr = JOptionPane.showInputDialog("수량");
            if (qtyStr == null) return;

            int qty = Integer.parseInt(qtyStr);

            if (!p.isActive()) {
                JOptionPane.showMessageDialog(this, "판매 불가 상품입니다.");
                return;
            }

            if (p.getStock() < qty) {
                JOptionPane.showMessageDialog(this, "재고 부족");
                return;
            }

            service.processSale(selectedBarcode, qty);
            JOptionPane.showMessageDialog(this, "판매 완료");

            loadProducts();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "판매 실패");
        }
    }

    private void addProduct() {
        try {
            Product p = new Product();

            p.setBarcode(JOptionPane.showInputDialog("바코드"));
            p.setName(JOptionPane.showInputDialog("상품명"));
            p.setCategory(JOptionPane.showInputDialog("카테고리"));

            p.setPrice(new BigDecimal(JOptionPane.showInputDialog("가격")));
            p.setCost(new BigDecimal(JOptionPane.showInputDialog("원가")));
            p.setStock(Integer.parseInt(JOptionPane.showInputDialog("재고")));

            String date = JOptionPane.showInputDialog("유통기한 (yyyy-MM-dd)");

            if (date == null || date.isEmpty()) {
                p.setExpireDate(LocalDate.of(9999, 12, 31));
            } else {
                p.setExpireDate(LocalDate.parse(date));
            }

            p.setActive(true);

            service.addProduct(p);
            JOptionPane.showMessageDialog(this, "상품 추가 완료");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "추가 실패");
        }
    }

    private void updateProduct() {
        if (selectedBarcode == null) {
            JOptionPane.showMessageDialog(this, "상품을 선택하세요.");
            return;
        }

        try {
            Product p = service.findByBarcode(selectedBarcode);

            String price = JOptionPane.showInputDialog("가격");
            String stock = JOptionPane.showInputDialog("재고");
            String date = JOptionPane.showInputDialog("유통기한");

            if (price != null) p.setPrice(new BigDecimal(price));
            if (stock != null) p.setStock(Integer.parseInt(stock));
            if (date != null) p.setExpireDate(LocalDate.parse(date));

            service.updateProduct(p);

            JOptionPane.showMessageDialog(this, "수정 완료");
            loadProducts();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "수정 실패");
        }
    }

    private void softDelete() {
        try {
            for (Product p : service.getProductList()) {
                if (p.getStock() <= 0 ||
                        p.getExpireDate().isBefore(LocalDate.now())) {
                    service.softDelete(p);
                }
            }
            JOptionPane.showMessageDialog(this, "삭제 완료");
            loadProducts();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "삭제 실패");
        }
    }

    private void changeStatus() {

        int result = JOptionPane.showConfirmDialog(
                this,
                "모든 조건 만족 상품을 판매 가능 상태로 변경하시겠습니까?",
                "전체 상태 변경",
                JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) return;

        try {

            int successCount = 0;

            List<Product> list = service.getProductList();

            for (Product p : list) {

                boolean changed = service.changeStatus(p.getBarcode());

                if (changed) {
                    successCount++;
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    "상태 변경 완료\n변경된 상품 수: " + successCount
            );

            loadProducts();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "전체 상태 변경 실패");
        }
    }

    private void getSales() {
        try {
            List<SalesToday> list = service.getTodaySales();

            StringBuilder sb = new StringBuilder();

            for (SalesToday s : list) {
                sb.append("날짜: ").append(s.getSoldAt())
                        .append(" 카테고리: ").append(s.getCategory())
                        .append(" 판매량: ").append(s.getCount())
                        .append(" 매출: ").append(s.getTotalPrice())
                        .append(" 이익: ").append(s.getProfit())
                        .append("\n");
            }

            JOptionPane.showMessageDialog(this, sb.toString());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "조회 실패");
        }
    }

    private void addStock() {
        if (selectedBarcode == null) {
            JOptionPane.showMessageDialog(this, "상품을 선택하세요.");
            return;
        }

        try {
            Product p = service.findByBarcode(selectedBarcode);

            String input = JOptionPane.showInputDialog(
                    this,
                    "추가할 재고 수량을 입력하세요"
            );

            if (input == null) return; // 취소

            int stock = Integer.parseInt(input);

            if (stock <= 0) {
                JOptionPane.showMessageDialog(this, "1 이상 입력하세요.");
                return;
            }

            service.addStock(selectedBarcode, stock);

            JOptionPane.showMessageDialog(this, "재고 추가 완료");

            loadProducts(); // 테이블 갱신

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "숫자만 입력하세요.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "재고 추가 실패");
        }
    }


}