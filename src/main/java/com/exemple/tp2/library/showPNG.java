package com.exemple.tp2.library;

import javax.swing.*;
import java.awt.*;

public class showPNG extends JFrame {
    public void createDiagramFrame(String filename) {
        JPanel panel = new JPanel();
        panel.setSize(500, 640);
        panel.setBackground(Color.CYAN);
        ImageIcon icon = new ImageIcon(filename);
        JLabel label = new JLabel();
        label.setIcon(icon);
        panel.add(label);
        this.getContentPane().add(panel);
        this.setVisible(true);
    }
}
