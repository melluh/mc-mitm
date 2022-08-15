package com.melluh.mcmitm.gui;


import com.melluh.mcmitm.MinecraftProxy.ProxyState;
import com.melluh.mcmitm.gui.auth.AccountsDialog;
import com.melluh.mcmitm.gui.dialog.PacketFilterDialog;
import com.melluh.mcmitm.protocol.ProtocolCodec;
import com.melluh.mcmitm.protocol.ProtocolVersions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class TopPanel extends JPanel {

    private final MainGui gui;
    private JButton startButton;

    public TopPanel(MainGui gui) {
        this.gui = gui;
        this.setLayout(new BorderLayout());
        this.addComponents();
    }

    private void addComponents() {
        JPanel firstRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        firstRow.setBorder(new EmptyBorder(5, 5, 0, 5));
        this.add(firstRow, BorderLayout.PAGE_START);
        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        secondRow.setBorder(new EmptyBorder(0, 5, 5, 5));
        this.add(secondRow, BorderLayout.PAGE_END);

        firstRow.add(new JLabel("Target IP:"));
        JTextField targetIpField = new JTextField("127.0.0.1");
        targetIpField.setColumns(15);
        firstRow.add(targetIpField);

        firstRow.add(new MarginLabel("Target port:"));
        JTextField targetPortField = new JTextField("25565");
        targetPortField.setColumns(5);
        firstRow.add(targetPortField);

        firstRow.add(new MarginLabel("Listen port:"));
        JTextField listenPortField = new JTextField("25570");
        listenPortField.setColumns(5);
        firstRow.add(listenPortField);

        firstRow.add(new MarginLabel("Version:"));
        JComboBox<String> versionSelector = new JComboBox<>();
        versionSelector.addItem("Auto-detect");
        firstRow.add(versionSelector);
        List<ProtocolCodec> versions = ProtocolVersions.getAll();
        versions.forEach(version -> versionSelector.addItem(version.getDisplayName()));

        this.startButton = new JButton("Start proxy");
        secondRow.add(startButton);
        startButton.addActionListener(event -> {
            ProxyState proxyState = gui.getProxyState();
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

        });

        JButton filterButton = new JButton("Filter packets");
        secondRow.add(filterButton);
        filterButton.addActionListener(event -> {
        });
    }

    public void proxyStateChange(ProxyState state) {
        switch(state) {
            case IDLE -> startButton.setText("Start proxy");
            case RUNNING -> startButton.setText("Stop proxy");
        }
    }

}
