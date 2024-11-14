package com.finxis.cdm.crossproductapp;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class BlotterTable extends JTable {


    @Override
    public boolean isCellEditable(int row, int column){
        return false;
    }

    Map<Integer, Color> rowColor = new HashMap<Integer, Color>();

    public BlotterTable()
    {
        super();
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
    {
        Component c = super.prepareRenderer(renderer, row, column);

        if (!isRowSelected(row))
        {
            Color color = rowColor.get( row );
            c.setBackground(color == null ? getBackground() : color);
        }

        return c;
    }

    public void setRowColor(int row, Color color)
    {
        rowColor.put(row, color);
    }

}
