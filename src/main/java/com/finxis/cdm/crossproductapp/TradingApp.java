package com.finxis.cdm.crossproductapp;


import javax.management.JMException;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.*;
import java.awt.event.*;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.xml.parsers.ParserConfigurationException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TradingApp extends JFrame {

    public JFrame frame;
    private JPanel mainContainer;

    public JPanel pairoffPanel;

    public JTabbedPane tabbedPane;



    public JTextArea outputArea;
    JScrollPane scrollPane = null;

    public JFrame eout;

    public Integer labelWidth;
    public Integer labelHeight;

    public Integer columnWidth;

    public Integer fieldWidth;
    public Integer fieldHeight;

    public Integer btnWidth;

    public Integer btnHeight;

    public Integer mainFrameWidth = 1000;
    public Integer mainFrameHeight = 1000;
    public Integer tabPanelWidth = mainFrameWidth;
    public Integer tabPanelHeight = 50;

    public Integer actionPanelHeight = 60;

    private final static int CLEARED_SWAPS_TAB=0;

    private final static int OUTPUT_TAB= 1;
    private final static int SETTINGS_TAB= 2;


    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        String fileName = null;
        if (args.length > 0) fileName = args[0];


        TradingApp tradingApp = new TradingApp();
        SwingUtilities.invokeLater(() -> {
            try {
                tradingApp.buildTradingFrame();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


    }

        public void buildTradingFrame() throws SQLException, IOException {

            labelWidth = 150;
            labelHeight = 15;
            fieldWidth = 170;
            fieldHeight = 20;
            columnWidth = 15;
            btnWidth = 150;
            btnHeight = 30;
            mainFrameWidth = 1100;
            mainFrameHeight = 1000;
            tabPanelWidth = mainFrameWidth;
            tabPanelHeight = 50;
            actionPanelHeight = 60;



            IcmaRepoUtil ru = new IcmaRepoUtil();

            this.tabbedPane = new JTabbedPane();
            tabbedPane.setSize(tabPanelWidth, tabPanelHeight);
            frame = new JFrame("ICMA CDM Repo Demo");
            frame.setLayout(new FlowLayout(FlowLayout.LEFT));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(mainFrameWidth, mainFrameHeight);
            frame.setMinimumSize(getSize());
            frame.setMaximumSize(getSize());
            //frame.getContentPane().setBackground(new Color(150,36,33));


            JPanel logoPanel = new JPanel();
            try {
                Image image = ImageIO.read(getClass().getResource("/images/icma-logo.jfif"));
                frame.setIconImage(image);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            JPanel sysOutPanel = new JPanel(new BorderLayout());
            sysOutPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            //sysOutPanel.setSize( 800, 800 );

            outputArea = new JTextArea(40, 80);
            outputArea.setEditable(false);
            scrollPane = new JScrollPane(outputArea);
            scrollPane.setViewportView(outputArea);
            scrollPane.getPreferredSize();
            sysOutPanel.add(scrollPane, BorderLayout.CENTER);

            Border blackline = BorderFactory.createLineBorder(Color.black);
            mainContainer = new JPanel();
            mainContainer.setLayout(new FlowLayout(FlowLayout.LEFT));

            mainContainer.setSize(mainFrameWidth, mainFrameHeight);
            mainContainer.setMinimumSize(new Dimension(mainFrameWidth, mainFrameHeight));
            mainContainer.setPreferredSize(new Dimension(mainFrameWidth, mainFrameHeight));
            //mainContainer.setBackground(Color.GREEN);
            mainContainer.setBorder(blackline);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.anchor =GridBagConstraints.NORTHWEST;
            gbc.gridx = 0;


            JPanel settingsPanel = new JPanel();
            ClearedSwapApp csa = new ClearedSwapApp();

            JPanel clearedSwapTradePanel = csa.buildClearedSwapTicket(this);

            tabbedPane.insertTab("Cleared Swaps", null, clearedSwapTradePanel,
                    null, CLEARED_SWAPS_TAB);


            //tabbedPane.addTab("Funds", null, fundsTradePanel,null);

            tabbedPane.insertTab("Output", null, sysOutPanel,
                    null, OUTPUT_TAB);
            tabbedPane.insertTab("Settings", null, settingsPanel,
                    null, SETTINGS_TAB);

            tabbedPane.setVisible(true);

            //actionPanel.setVisible(true);

            mainContainer.add(tabbedPane);
            frame.add(mainContainer);

            frame.getContentPane().setVisible(true);
            frame.setResizable(true);
            frame.setVisible(true);

        }


        public void tradingAppAgent(){

        //Setup and connect to desktop agent

        }

}
