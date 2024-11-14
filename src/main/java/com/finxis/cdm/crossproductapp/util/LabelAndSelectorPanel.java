package com.finxis.cdm.crossproductapp.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LabelAndSelectorPanel extends JPanel{
    Integer labelWidth = 150;
    Integer  labelHeight = 15;
    Integer  fieldWidth = 170;
    Integer  fieldHeight = 20;

    public JComboBox lcb;
    public JPanel lxp;
    public JLabel lb;

    public LabelAndSelectorPanel() {



    }
    public LabelAndSelectorPanel(String label, JComboBox jcBox) {

        Border blackline, raisedetched, loweredetched,
                raisedbevel, loweredbevel, empty;

        blackline = BorderFactory.createLineBorder(Color.black);
        empty = BorderFactory.createEmptyBorder();

        lxp = new JPanel(new GridBagLayout());
        lxp.setBorder(new EmptyBorder(0, 0, 0, 0));
        //lxp.setBorder(blackline);
        lxp.setOpaque(true);

        lb = new JLabel(label, JLabel.LEFT);
        lb.setPreferredSize(new Dimension(labelWidth, labelHeight));
        lb.setOpaque(true);

        lcb = jcBox;
        lcb.setAlignmentX(Component.LEFT_ALIGNMENT);
        lcb.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        lcb.setBackground(Color.WHITE);
        lcb.setOpaque(false);

        lb.setText(label);
        lxp.add(lb,createGbc(0, 0));
        lxp.add(lcb,createGbc(1, 0));

        lb.setVisible(true);
        lxp.setVisible(true);

    }




    public LabelAndSelectorPanel(String label, String[] selectList) {

        Border blackline, raisedetched, loweredetched,
                raisedbevel, loweredbevel, empty;

        blackline = BorderFactory.createLineBorder(Color.black);
        empty = BorderFactory.createEmptyBorder();

        lxp = new JPanel(new GridBagLayout());
        lxp.setBorder(new EmptyBorder(0, 0, 0, 0));
        //lxp.setBorder(blackline);
        lxp.setOpaque(true);

        lb= new JLabel(label, JLabel.LEFT);
        lb.setPreferredSize(new Dimension(labelWidth, labelHeight));
        lb.setOpaque(true);

        JComboBox lcb = new JComboBox<String>(selectList);
        lcb.setAlignmentX(Component.LEFT_ALIGNMENT);
        lcb.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        lcb.setBackground(Color.WHITE);
        lcb.setOpaque(false);

        lb.setText(label);
        lxp.add(lb,createGbc(0, 0));
        lxp.add(lcb,createGbc(1, 0));

        lb.setVisible(true);
        lxp.setVisible(true);

    }

    public JPanel createLabelAndSelectorPanel(String label, String[] selectList) {

        Border blackline, raisedetched, loweredetched,
                raisedbevel, loweredbevel, empty;

        blackline = BorderFactory.createLineBorder(Color.black);
        empty = BorderFactory.createEmptyBorder();

        JPanel lxp = new JPanel(new GridBagLayout());


        lxp.setBorder(new EmptyBorder(0, 0, 0, 0));
        //lxp.setBorder(blackline);
        lxp.setOpaque(true);

        JLabel lb= new JLabel(label, JLabel.LEFT);
        lb.setPreferredSize(new Dimension(labelWidth, labelHeight));
        lb.setOpaque(true);

        JComboBox lcb = new JComboBox<String>(selectList);
        lcb.setAlignmentX(Component.LEFT_ALIGNMENT);
        lcb.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        lcb.setBackground(Color.WHITE);
        lcb.setOpaque(false);

        lb.setText(label);
        lxp.add(lb,createGbc(0, 0));
        lxp.add(lcb,createGbc(1, 0));

        lb.setVisible(true);
        lxp.setVisible(true);

        return lxp;

    }

    public JPanel createLabelAndSelectorPanel(String label, JComboBox jcBox ) {

        Border blackline, raisedetched, loweredetched,
                raisedbevel, loweredbevel, empty;

        blackline = BorderFactory.createLineBorder(Color.black);
        empty = BorderFactory.createEmptyBorder();

        JPanel lxp = new JPanel(new GridBagLayout());


        lxp.setBorder(new EmptyBorder(0, 0, 0, 0));
        //lxp.setBorder(blackline);
        lxp.setOpaque(true);

        JLabel lb= new JLabel(label, JLabel.LEFT);
        lb.setPreferredSize(new Dimension(labelWidth, labelHeight));
        lb.setOpaque(true);

        JComboBox lcb = jcBox;
        lcb.setAlignmentX(Component.LEFT_ALIGNMENT);
        lcb.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        lcb.setBackground(Color.WHITE);
        lcb.setOpaque(false);

        lb.setText(label);
        lxp.add(lb,createGbc(0, 0));
        lxp.add(lcb,createGbc(1, 0));

        return lxp;

    }

    private static GridBagConstraints createGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0;
        gbc.weighty = 0;

        gbc.fill = GridBagConstraints.HORIZONTAL;
        int gap = 0;
        gbc.insets = new Insets(gap, gap + 0 * gap * x, gap, gap);

        return gbc;
    }

    public String getText(){return lcb.getSelectedItem().toString();}

    public CItem getItem(){return (CItem) lcb.getSelectedItem();}

}
