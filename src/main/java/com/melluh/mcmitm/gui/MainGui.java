package com.melluh.mcmitm.gui;

import com.melluh.mcmitm.MinecraftProxy;
import com.melluh.mcmitm.MinecraftProxy.ProxyState;
import com.melluh.mcmitm.auth.AuthenticationHandler;
import com.melluh.mcmitm.protocol.packet.Packet;
import com.melluh.mcmitm.util.Utils;
import org.tinylog.Logger;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MainGui extends JFrame {

    private final FixedTableModel tableModel = new FixedTableModel("Direction", "ID", "Name", "Length");

    private MinecraftProxy proxy;
    private TopPanel topPanel;

    public MainGui() {
        this.setTitle("mc-mitm");
        this.setSize(800, 600);
        //this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addComponents();
    }

    private void addComponents() {
        this.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        this.add(mainPanel);

        this.topPanel = new TopPanel(this);
        topPanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));
        this.add(topPanel, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new MatteBorder(0, 0, 0, 1, Color.BLACK));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // Automatically scroll to the bottom when a new packet gets added
        scrollPane.getVerticalScrollBar().addAdjustmentListener(event -> event.getAdjustable().setValue(event.getAdjustable().getMaximum()));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(new PacketInspectionPanel(), BorderLayout.EAST);
    }

    public void addPacket(Packet packet) {
        String[] values = new String[] {
                packet.getType().getDirection().getName(),
                packet.getType().getHexId(),
                packet.getType().getName(),
                Utils.formatLength(packet.getData().getLength())
        };
        tableModel.addRow(values);
    }

    public void startProxy(String targetIp, int targetPort, int listenPort) {
        if(proxy != null) {
            Logger.warn("Proxy already running");
            return;
        }

        try {
            this.proxy = new MinecraftProxy(this, listenPort, targetIp, targetPort);
            proxy.onStateChange(this::proxyStateChange);
            proxy.run();
        } catch (Throwable t) {
            Logger.error(t, "Failed to launch");
        }
    }

    private void proxyStateChange(ProxyState state) {
        topPanel.proxyStateChange(state);

        if(state == ProxyState.IDLE)
            this.proxy = null;
    }

    public void stopProxy() {
        if(proxy == null) {
            Logger.warn("Proxy isn't running");
            return;
        }

        proxy.stop();
    }

    public void displayException(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));

        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setText(stringWriter.toString());

        JScrollPane scrollPane = new JScrollPane(text);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        JPanel panel = new JPanel();
        panel.add(scrollPane);

        JOptionPane.showMessageDialog(this, panel, "An exception occurred", JOptionPane.ERROR_MESSAGE);
    }

    private static MainGui instance;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable t) {
            Logger.error(t, "Failed to set look and feel");
        }

        instance = new MainGui();
        instance.setLocationRelativeTo(null);
        instance.setVisible(true);

        AuthenticationHandler.getInstance().loadFromFile();
    }

    public static MainGui getInstance() {
        return instance;
    }

}
