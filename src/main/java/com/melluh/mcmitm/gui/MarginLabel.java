package com.melluh.mcmitm.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MarginLabel extends JLabel {

    public MarginLabel(String text) {
        super(text);
        this.setBorder(new EmptyBorder(0, 10, 0, 0));
    }

}
