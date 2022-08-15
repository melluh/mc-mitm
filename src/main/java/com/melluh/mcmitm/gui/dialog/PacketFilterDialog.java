package com.melluh.mcmitm.gui.dialog;

import com.melluh.mcmitm.gui.MarginLabel;
import com.melluh.mcmitm.gui.MainGui;
import com.melluh.mcmitm.protocol.PacketType;
import com.melluh.mcmitm.protocol.ProtocolCodec;
import com.melluh.mcmitm.protocol.ProtocolCodec.ProtocolStateCodec;
import com.melluh.mcmitm.protocol.ProtocolVersions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class PacketFilterDialog extends JDialog {

    private final DefaultTableModel tableModel = new DefaultTableModel(0, 3) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }
    };

    private final MainGui gui;

    public PacketFilterDialog(MainGui gui) {
        this.gui = gui;

        this.setTitle("Configure packet filters");
        this.setSize(500, 400);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.addComponents();
    }

    private void selectVersion(ProtocolCodec codec) {
        tableModel.getDataVector().removeAllElements();

        for(ProtocolStateCodec stateCodec : codec.getStateCodecs()) {
            for(PacketType packetType : stateCodec.getPacketTypes()) {
                tableModel.addRow(new Object[]{true, stateCodec.getState().getDisplayName(), packetType.getHexId() + ": " + packetType.getName()});
            }
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
        JTextField searchBox = new JTextField();
        topPanel.add(searchBox);
        searchBox.setColumns(20);

        JTable table = new JTable(tableModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                if(column == 0)
                    return Boolean.class;
                return String.class;
            }
        };
        table.getTableHeader().setUI(null); // hide the header
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(15);
        columnModel.getColumn(1).setPreferredWidth(30);
        columnModel.getColumn(2).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new MatteBorder(1, 0, 0, 0, Color.BLACK));
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
