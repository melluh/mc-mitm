package com.melluh.mcmitm.gui.dialog;

import com.melluh.mcmitm.Settings;
import com.melluh.mcmitm.gui.MarginLabel;
import com.melluh.mcmitm.gui.MainGui;
import com.melluh.mcmitm.protocol.PacketType;
import com.melluh.mcmitm.protocol.ProtocolCodec;
import com.melluh.mcmitm.protocol.ProtocolCodec.ProtocolStateCodec;
import com.melluh.mcmitm.protocol.ProtocolVersions;
import org.tinylog.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PacketFilterDialog extends JDialog {

    private final DefaultTableModel tableModel = new DefaultTableModel(null, new String[] { "Captured?", "ID", "Name" }) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            super.setValueAt(value, row, column);

            if(column == 0) {
                PacketType packetType = currentTypes.get(row);
                String configName = packetType.getState().name() + "/" + packetType.getName();
                boolean enabled = (boolean) value;

                Settings settings = Settings.getInstance();
                if(enabled)
                    settings.getFilteredPackets().remove(configName);
                else
                    settings.getFilteredPackets().add(configName);
                settings.save();
            }
        }
    };

    private final List<PacketType> currentTypes = new ArrayList<>();
    private JTextField searchBox;
    private ProtocolCodec selectedCodec;

    public PacketFilterDialog(MainGui gui) {
        this.setTitle("Configure packet filters");
        this.setSize(500, 400);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.addComponents();
    }

    private void selectVersion(ProtocolCodec codec) {
        this.selectedCodec = codec;
        this.updateShown(searchBox.getText());
    }

    private void updateShown(String searchQuery) {
        tableModel.getDataVector().removeAllElements();
        tableModel.fireTableDataChanged();

        Settings settings = Settings.getInstance();
        currentTypes.clear();
        for(PacketType packetType : selectedCodec.getPacketTypes()) {
            if(!packetType.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                continue;
            currentTypes.add(packetType);
            boolean enabled = !settings.isFiltered(packetType);
            tableModel.addRow(new Object[] { enabled, packetType.getHexId(), packetType.getName() });
        }
    }

    private void addComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.add(topPanel, BorderLayout.NORTH);

        topPanel.add(new JLabel("Version:"));
        JComboBox<String> versionSelector = new JComboBox<>();
        topPanel.add(versionSelector);

        List<ProtocolCodec> versions = ProtocolVersions.getAll();
        versions.forEach(version -> versionSelector.addItem(version.getDisplayName()));
        versionSelector.addActionListener(event -> {
            ProtocolCodec codec = versions.get(versionSelector.getSelectedIndex());
            this.selectVersion(codec);
        });

        topPanel.add(new MarginLabel("Search:"));
        this.searchBox = new JTextField();
        topPanel.add(searchBox);
        searchBox.setColumns(20);
        searchBox.addCaretListener(event -> this.updateShown(searchBox.getText()));

        JTable table = new JTable(tableModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                if(column == 0)
                    return Boolean.class;
                return String.class;
            }
        };
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(1).setPreferredWidth(40);
        columnModel.getColumn(2).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane, BorderLayout.CENTER);

        // TODO: automatically select last version joined with
        this.selectVersion(versions.get(0));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.add(bottomPanel, BorderLayout.SOUTH);

        JButton doneButton = new JButton("Done");
        bottomPanel.add(doneButton);
        doneButton.addActionListener(event -> this.dispose());
    }

}
