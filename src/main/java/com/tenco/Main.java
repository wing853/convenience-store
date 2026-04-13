package com.tenco;

import com.tenco.view.StoreView;
import com.tenco.view.swing.StoreSwingView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

//        StoreView view = new StoreView();
//        view.start();
        SwingUtilities.invokeLater(() -> {
            new StoreSwingView().setVisible(true);
        });

    }
}