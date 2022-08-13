package com.melluh.mcmitm.gui;

import javax.swing.table.DefaultTableModel;

public class FixedTableModel extends DefaultTableModel {

    public FixedTableModel(String... columns) {
        super(null, columns);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
