package com.melluh.mcmitm.gui.auth;

import com.melluh.mcauth.MicrosoftAuthenticator;
import com.melluh.mcmitm.auth.AuthenticationHandler;
import com.melluh.mcmitm.gui.FixedTableModel;
import com.melluh.mcmitm.gui.MainGui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AccountsDialog extends JDialog {

    private final DefaultTableModel tableModel = new FixedTableModel("Name", "UUID");
    private final MainGui gui;

    public AccountsDialog(MainGui gui) {
        this.gui = gui;

        this.setTitle("Accounts");
        this.setSize(450, 250);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.addComponents();
    }

    private void addComponents() {
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 0, 10), scrollPane.getBorder()));
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        this.add(buttonPanel, BorderLayout.SOUTH);

        JButton addButton = new JButton("Add account");
        buttonPanel.add(addButton);
        addButton.addActionListener(event -> {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            AuthenticationHandler.MICROSOFT_AUTHENTICATOR.getDeviceCode().thenAccept(deviceCode -> {
                this.dispose();
                AddAccountDialog dialog = new AddAccountDialog(gui, deviceCode);
                dialog.setLocationRelativeTo(gui);
                dialog.setVisible(true);
            });
        });

        JButton backButton = new JButton("Close");
        buttonPanel.add(backButton);
        backButton.addActionListener(event -> this.dispose());
    }

}
