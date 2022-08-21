package com.melluh.mcmitm.gui.auth;


import com.melluh.mcauth.MicrosoftAuthenticator.DeviceCode;
import com.melluh.mcmitm.auth.Account;
import com.melluh.mcmitm.auth.AuthenticationHandler;
import com.melluh.mcmitm.gui.FixedTableModel;
import com.melluh.mcmitm.gui.MainGui;
import org.tinylog.Logger;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.concurrent.ExecutionException;


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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 0, 10), scrollPane.getBorder()));
        this.add(scrollPane, BorderLayout.CENTER);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(250);

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

            try {
                DeviceCode deviceCode = AuthenticationHandler.MICROSOFT_AUTHENTICATOR.getDeviceCode().get();
                this.dispose();
                AddAccountDialog dialog = new AddAccountDialog(gui, deviceCode);
                dialog.setLocationRelativeTo(gui);
                dialog.setVisible(true);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.error(ex, "Failed to get device code");
                gui.displayException(ex);
                this.dispose();
            }
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
            if(event.getValueIsAdjusting())
                SwingUtilities.invokeLater(() -> removeButton.setEnabled(true));
        });

        JButton backButton = new JButton("Close");
        buttonPanel.add(backButton);
        backButton.addActionListener(event -> this.dispose());
    }

}
