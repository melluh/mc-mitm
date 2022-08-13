package com.melluh.mcmitm.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PacketInspectionPanel extends JPanel {

    public PacketInspectionPanel() {
        this.setLayout(new BorderLayout());

        JLabel nameField = new JLabel("<html><b>ClientboundUpdateAttributesPacket</b></html>");
        nameField.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.add(nameField, BorderLayout.NORTH);

        // this.add(new JButton("Manage accounts"), BorderLayout.PAGE_START);
    }

}
