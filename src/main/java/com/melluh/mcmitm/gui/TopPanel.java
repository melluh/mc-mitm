package com.melluh.mcmitm.gui;

import com.melluh.mcmitm.MinecraftProxy.ProxyState;
import com.melluh.mcmitm.gui.auth.AccountsDialog;

import javax.swing.*;
import java.awt.*;

public class TopPanel extends JPanel {

    private final MainGui gui;

    private JButton startButton;
    private ProxyState proxyState = ProxyState.IDLE;

    public TopPanel(MainGui gui) {
        this.gui = gui;
        this.setLayout(new BorderLayout());
        this.addComponents();
    }

    private void addComponents() {
        JPanel firstRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.add(firstRow, BorderLayout.PAGE_START);
        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.add(secondRow, BorderLayout.PAGE_END);

        firstRow.add(new JLabel("Target IP:"));
        JTextField targetIpField = new JTextField("127.0.0.1");
        targetIpField.setColumns(15);
        firstRow.add(targetIpField);

        firstRow.add(new JLabel("Target port:"));
        JTextField targetPortField = new JTextField("25565");
        targetPortField.setColumns(5);
        firstRow.add(targetPortField);

        firstRow.add(new JLabel("Listen port:"));
        JTextField listenPortField = new JTextField("25570");
        listenPortField.setColumns(5);
        firstRow.add(listenPortField);

        firstRow.add(new JLabel("Version:"));
        JComboBox<String> versionDropdown = new JComboBox<>();
        versionDropdown.addItem("Auto-detect");
        versionDropdown.addItem("760 (1.19.1/1.19.2)");
        versionDropdown.addItem("759 (1.19)");
        firstRow.add(versionDropdown);

        this.startButton = new JButton("Start proxy");
        secondRow.add(startButton);
        startButton.addActionListener(event -> {
            if(proxyState == ProxyState.IDLE) {
                String targetIp = targetIpField.getText();
                int targetPort = Integer.parseInt(targetPortField.getText());
                int listenPort = Integer.parseInt(listenPortField.getText());
                // TODO: version dropdown

                gui.startProxy(targetIp, targetPort, listenPort);
            } else if(proxyState == ProxyState.RUNNING) {
                gui.stopProxy();
            }
        });

        JButton accountsButton = new JButton("Manage accounts");
        secondRow.add(accountsButton);
        accountsButton.addActionListener(event -> {
            AccountsDialog dialog = new AccountsDialog(gui);
            dialog.setLocationRelativeTo(gui);
            dialog.setVisible(true);
        });

        JButton filterButton = new JButton("Filter packets");
        secondRow.add(filterButton);
    }

    public void proxyStateChange(ProxyState state) {
        this.proxyState = state;
        switch(state) {
            case IDLE -> startButton.setText("Start proxy");
            case RUNNING -> startButton.setText("Stop proxy");
        }
    }

}
