package com.finxis.cdm.crossproductapp.util;

import javax.swing.*;
import java.awt.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.xml.parsers.ParserConfigurationException;
import javax.swing.JComponent;

public class LabelAndTextPanel extends JPanel{
    Integer labelWidth = 150;
    Integer  labelHeight = 15;
    Integer  fieldWidth = 170;
    Integer  fieldHeight = 20;

    public JLabel panelLabel;

    public JTextField panelTextField;

    public JPanel panel;

    public LabelAndTextPanel(){

        String label = "";
        String text = "";

        Border blackline, raisedetched, loweredetched,
                raisedbevel, loweredbevel, empty;

        blackline = BorderFactory.createLineBorder(Color.black);
        empty = BorderFactory.createEmptyBorder();

        panel = new JPanel(new GridBagLayout());

        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        //lxp.setBorder(blackline);
        panel.setOpaque(true);

        panelLabel = new JLabel(label, JLabel.LEFT);
        panelLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        panelLabel.setOpaque(true);

        panelTextField = new JTextField();
        panelTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelTextField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        panelTextField.setBackground(Color.WHITE);

        //ltxt.setOpaque(false);

        panelLabel.setText(label);
        panelTextField .setText(text);

        panel.add(panelLabel,createGbc(0, 0));
        panel.add(panelTextField ,createGbc(1, 0));

        panelLabel.setVisible(true);
        panel.setVisible(true);

    }

    public LabelAndTextPanel(String label, String text){
        Border blackline, raisedetched, loweredetched,
                raisedbevel, loweredbevel, empty;

        blackline = BorderFactory.createLineBorder(Color.black);
        empty = BorderFactory.createEmptyBorder();

        panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.setOpaque(true);

        panelLabel = new JLabel(label, JLabel.LEFT);
        panelLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        panelLabel.setOpaque(true);

        panelTextField = new JTextField();
        panelTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelTextField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        panelTextField.setBackground(Color.WHITE);

        panelLabel.setText(label);
        panelTextField .setText(text);

        panel.add(panelLabel,createGbc(0, 0));
        panel.add(panelTextField ,createGbc(1, 0));

        panelLabel.setVisible(true);
        panel.setVisible(true);

    }
    public JPanel createLabelAndTextPanel(String label, String text) {

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
        panelLabel = lb;

        JTextField ltxt = new JTextField();
        ltxt.setAlignmentX(Component.LEFT_ALIGNMENT);
        ltxt.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        ltxt.setBackground(Color.WHITE);
        panelTextField = ltxt;

        //ltxt.setOpaque(false);

        lb.setText(label);
        ltxt.setText(text);
        lxp.add(lb,createGbc(0, 0));
        lxp.add(ltxt,createGbc(1, 0));

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

}
