package com.melluh.mcmitm.gui.auth;

import com.melluh.mcauth.MicrosoftAuthenticator.DeviceCode;
import com.melluh.mcauth.MicrosoftAuthenticator.PollingState;
import com.melluh.mcmitm.auth.AuthenticationHandler;
import com.melluh.mcmitm.gui.MainGui;
import org.tinylog.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AddAccountDialog extends JDialog {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final MainGui gui;
    private final DeviceCode deviceCode;

    public AddAccountDialog(MainGui gui, DeviceCode deviceCode) {
        this.gui = gui;
        this.deviceCode = deviceCode;

        this.setTitle("Add account");
        this.setSize(350, 250);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setModalityType(ModalityType.APPLICATION_MODAL);

        this.addComponents();
        this.startPolling();
    }

    private void startPolling() {
        executor.scheduleAtFixedRate(() -> {
            AuthenticationHandler.MICROSOFT_AUTHENTICATOR.pollDeviceCode(deviceCode).thenAccept(result -> {
                if(result.state() == PollingState.DECLINED) {
                    this.closeWithError("Authentication was declined");
                    return;
                }

                if(result.state() == PollingState.EXPIRED) {
                    this.closeWithError("Authentication request expired");
                    return;
                }

                if(result.state() == PollingState.ACCEPTED) {
                    Logger.info("Accepted! Token: {}", result.token().value());
                    this.stopPolling();
                    this.dispose();
                }
            });
        }, deviceCode.checkInterval(), deviceCode.checkInterval(), TimeUnit.SECONDS);
    }

    private void closeWithError(String message) {
        this.stopPolling();
        this.dispose();
        JOptionPane.showMessageDialog(gui, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void stopPolling() {
        executor.shutdownNow();
    }

    private void addComponents() {
        JLabel instructions = new JLabel("<html><div style='text-align: center;'>To authenticate, open <b>" + deviceCode.verificationUri() + "</b> and enter the following code:<br><br><b style='font-size: 14px;'>" + deviceCode.userCode() + "</b></div></html>");
        instructions.setBorder(new EmptyBorder(0, 10, 0, 10));
        this.add(instructions, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        this.add(buttonPanel, BorderLayout.SOUTH);

        JButton openButton = new JButton("Open in browser");
        openButton.setEnabled(Desktop.isDesktopSupported());
        buttonPanel.add(openButton);
        openButton.addActionListener(event -> {
            try {
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
