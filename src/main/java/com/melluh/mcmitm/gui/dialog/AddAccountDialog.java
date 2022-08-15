package com.melluh.mcmitm.gui.dialog;

import com.melluh.mcauth.MicrosoftAuthenticator.DeviceCode;
import com.melluh.mcauth.MicrosoftAuthenticator.PollingResult;
import com.melluh.mcauth.MicrosoftAuthenticator.PollingState;
import com.melluh.mcmitm.auth.AuthenticationHandler;
import com.melluh.mcmitm.gui.MainGui;
import org.tinylog.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AddAccountDialog extends JDialog {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final MainGui gui;
    private final DeviceCode deviceCode;

    private JLabel instructionsLabel;

    public AddAccountDialog(MainGui gui, DeviceCode deviceCode) {
        this.gui = gui;
        this.deviceCode = deviceCode;

        this.setTitle("Add account");
        this.setSize(350, 250);
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setModalityType(ModalityType.APPLICATION_MODAL);

        this.addComponents();
        this.startPolling();
    }

    private void startPolling() {
        executor.scheduleAtFixedRate(() -> {
            try {
                PollingResult result = AuthenticationHandler.MICROSOFT_AUTHENTICATOR.pollDeviceCode(deviceCode).get();
                if(result.state() == PollingState.DECLINED) {
                    this.closeWithError("Authentication was declined");
                    return;
                }

                if(result.state() == PollingState.EXPIRED) {
                    this.closeWithError("Authentication request expired");
                    return;
                }

                if(result.state() == PollingState.ACCEPTED) {
                    this.stopPolling();
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    instructionsLabel.setText("<html><div style='text-align: center;'><b>Please wait, logging you in...</b></div></html>");

                    AuthenticationHandler.getInstance().authenticate(result.token());

                    this.dispose();
                    AccountsDialog dialog = new AccountsDialog(gui);
                    dialog.setLocationRelativeTo(gui);
                    dialog.setVisible(true);
                }
            } catch (Throwable ex) {
                Logger.error(ex, "Failed to poll device code");
                this.stopPolling();
                this.dispose();
                gui.displayException(ex);
            }
        }, deviceCode.checkInterval(), deviceCode.checkInterval(), TimeUnit.SECONDS);
    }

    private void closeWithError(String message) {
        this.stopPolling();
        this.dispose();
        JOptionPane.showMessageDialog(gui, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void stopPolling() {
        if(!executor.isShutdown())
            executor.shutdown();
    }

    private void addComponents() {
        this.instructionsLabel = new JLabel("<html><div style='text-align: center;'>To authenticate, open <b>" + deviceCode.verificationUri() + "</b> and enter the following code:<br><br><b style='font-size: 14px;'>" + deviceCode.userCode() + "</b></div></html>");
        instructionsLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
        this.add(instructionsLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        this.add(buttonPanel, BorderLayout.SOUTH);

        JButton openButton = new JButton("Copy to clipboard and open in browser");
        openButton.setEnabled(Desktop.isDesktopSupported());
        buttonPanel.add(openButton);
        openButton.addActionListener(event -> {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(deviceCode.userCode()), null);
                Desktop.getDesktop().browse(URI.create(deviceCode.verificationUri()));
            } catch (IOException ex) {
                Logger.error(ex, "Failed to open browser");
            }
        });

        JButton backButton = new JButton("Cancel");
        buttonPanel.add(backButton);
        backButton.addActionListener(event -> {
            this.dispose();
            AccountsDialog dialog = new AccountsDialog(gui);
            dialog.setLocationRelativeTo(gui);
            dialog.setVisible(true);
        });
    }

}
