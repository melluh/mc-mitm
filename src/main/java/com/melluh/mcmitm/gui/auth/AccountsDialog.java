package com.melluh.mcmitm.gui.auth;

import com.melluh.mcmitm.auth.Account;
import com.melluh.mcmitm.auth.AuthenticationHandler;
import com.melluh.mcmitm.gui.FixedTableModel;
import com.melluh.mcmitm.gui.MainGui;
import org.tinylog.Logger;

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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 0, 10), scrollPane.getBorder()));
        this.add(scrollPane, BorderLayout.CENTER);

        for(Account account : AuthenticationHandler.getInstance().getAccounts()) {
            tableModel.addRow(new String[] { account.getGameProfile().username(), account.getGameProfile().uuid().toString() });
        }

        JPanel buttonPanel = new JPanel();
        this.add(buttonPanel, BorderLayout.SOUTH);

        JButton addButton = new JButton("Add account");
        buttonPanel.add(addButton);
        addButton.addActionListener(event -> {
            addButton.setEnabled(false);
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            AuthenticationHandler.MICROSOFT_AUTHENTICATOR.getDeviceCode().thenAccept(deviceCode -> {
                this.dispose();
                AddAccountDialog dialog = new AddAccountDialog(gui, deviceCode);
                dialog.setLocationRelativeTo(gui);
                dialog.setVisible(true);
            });
        });

        JButton removeButton = new JButton("Remove account");
        removeButton.setEnabled(false);
        removeButton.addActionListener(event -> {
            int row = table.getSelectedRow();
            AuthenticationHandler authHandler = AuthenticationHandler.getInstance();
            Account account = authHandler.getAccounts().get(row);
            authHandler.removeAccount(account);
            tableModel.removeRow(row);
            table.clearSelection();
            removeButton.setEnabled(false);
        });
        buttonPanel.add(removeButton);

        table.getSelectionModel().addListSelectionListener(event -> {
            SwingUtilities.invokeLater(() -> removeButton.setEnabled(true));
        });

        JButton backButton = new JButton("Close");
        buttonPanel.add(backButton);
        backButton.addActionListener(event -> this.dispose());
    }

}
