package com.melluh.mcmitm.gui;

import com.melluh.mcmitm.MinecraftProxy;
import com.melluh.mcmitm.MinecraftProxy.ProxyState;
import com.melluh.mcmitm.protocol.ProtocolVersions;
import com.melluh.mcmitm.protocol.packet.Packet;
import com.melluh.mcmitm.util.Utils;
import org.tinylog.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MainGui extends JFrame {

    private static final String[] TABLE_COLUMNS = { "Direction", "ID", "Name", "Length" };
    private final DefaultTableModel tableModel = new DefaultTableModel(null, TABLE_COLUMNS) {
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return this.getValueAt(0, columnIndex).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private MinecraftProxy proxy;
    private ConnectionPanel connectionPanel;

    public MainGui() {
        this.setTitle("mc-mitm");
        this.setSize(800, 600);
        this.setMinimumSize(new Dimension(700, 300));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addComponents();
    }

    private void addComponents() {
        this.setLayout(new BorderLayout());

        this.connectionPanel = new ConnectionPanel(this);
        this.add(connectionPanel, BorderLayout.PAGE_START);

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        // Always show the vertical scroll bar
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // Automatically scroll to the bottom when a new packet gets added
        scrollPane.getVerticalScrollBar().addAdjustmentListener(event -> event.getAdjustable().setValue(event.getAdjustable().getMaximum()));
        this.add(scrollPane);
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
        connectionPanel.proxyStateChange(state);

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
        text.setCaretPosition(0);

        JPanel panel = new JPanel();
        panel.add(scrollPane);

        JOptionPane.showMessageDialog(this, panel, "An exception occurred", JOptionPane.ERROR_MESSAGE);
    }

    public ProxyState getProxyState() {
        return proxy != null ? proxy.getState() : ProxyState.IDLE;
    }

    private static MainGui instance;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Logger.error(ex, "Failed to set look and feel");
        }

        ProtocolVersions.loadAll();

        instance = new MainGui();
        instance.setLocationRelativeTo(null);
        instance.setVisible(true);
    }

    public static MainGui getInstance() {
        return instance;
    }

}
