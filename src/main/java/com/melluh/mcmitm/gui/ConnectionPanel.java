package com.melluh.mcmitm.gui;

import com.melluh.mcmitm.MinecraftProxy.ProxyState;

import javax.swing.*;
import java.awt.*;

public class ConnectionPanel extends JPanel {

    private final MainGui gui;

    private JButton startButton;
    private ProxyState proxyState = ProxyState.IDLE;

    public ConnectionPanel(MainGui gui) {
        this.gui = gui;
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.addComponents();
    }

    private void addComponents() {
        this.add(new JLabel("Target IP:"));
        JTextField targetIpField = new JTextField("127.0.0.1");
        targetIpField.setColumns(15);
        this.add(targetIpField);

        this.add(new JLabel("Target port:"));
        JTextField targetPortField = new JTextField("25565");
        targetPortField.setColumns(5);
        this.add(targetPortField);

        this.add(new JLabel("Listen port:"));
        JTextField listenPortField = new JTextField("25570");
        listenPortField.setColumns(5);
        this.add(listenPortField);

        this.add(new JLabel("Version:"));
        JComboBox<String> versionDropdown = new JComboBox<>();
        versionDropdown.addItem("Auto-detect");
        versionDropdown.addItem("760 (1.19.1/1.19.2)");
        versionDropdown.addItem("759 (1.19)");
        this.add(versionDropdown);

        this.startButton = new JButton("Start proxy");
        this.add(startButton);
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
    }

    public void proxyStateChange(ProxyState state) {
        this.proxyState = state;
        switch(state) {
            case IDLE -> startButton.setText("Start proxy");
            case RUNNING -> startButton.setText("Stop proxy");
        }
    }

}
