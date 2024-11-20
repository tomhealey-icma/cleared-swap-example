package com.finxis.cdm.crossproductapp;

//import cdm.base.staticdata.common.functions.IntToString;
import cdm.event.common.BusinessEvent;
import cdm.event.common.Trade;
import cdm.event.common.TradeState;
import cdm.event.workflow.WorkflowStep;
import cdm.product.asset.CashflowRepresentation;
import cdm.product.common.schedule.CalculationPeriod;
import cdm.product.common.schedule.PaymentCalculationPeriod;
import com.finxis.cdm.crossproductapp.productlifecycle.ClearedSwapLifecycle;
import com.finxis.cdm.crossproductapp.productmodels.CashflowModel;
import com.finxis.cdm.crossproductapp.productmodels.CashflowTableModel;
import com.finxis.cdm.crossproductapp.productmodels.ClearedSwapModel;
import com.finxis.cdm.crossproductapp.productmodels.ClearedSwapTradeTableModel;
import com.finxis.cdm.crossproductapp.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.rosetta.model.metafields.MetaFields;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

import static java.lang.Double.parseDouble;

public class ClearedSwapApp implements ActionListener  {


    private String tradeStateStr = null;
    private String beforeTradeStateStr = null;
    private String afterTradeStateStr = null;
    private JPanel panel;

    private JPanel clearedSwapContainer;
    private JPanel tc;
    private JPanel c1;
    private JPanel c2;
    private JPanel c3;
    private BlotterTable swapTradeBlotter = new BlotterTable();

    private BlotterTable swapCashflowBlotter= new BlotterTable();

    public JTabbedPane clearedSwapTabbedPane;

    Map<String, String> cdmMap = new HashMap<>();

    public Integer labelWidth;
    public Integer labelHeight;

    public Integer btnWidth;

    public Integer btnHeight;
    public Integer columnWidth;

    public Integer fieldWidth;
    public Integer fieldHeight;

    public JPanel tradeDate;

    private JComboBox issuerLEIField;
    private JTextField statusField;

    public String ETformattedDateTimeString;

    public DateTimeFormatter formatter;
    public ZonedDateTime ETzonedDateTime;
    private JButton newTradeBtn;
    private JButton bookTradeBtn;
    private JButton cancelBtn;
    private JButton closoutBtn;

    private JButton settleBtn;

    private JButton resetBtn;
    private JButton increaseBtn;
    private JButton allocationBtn;
    private JButton novationBtn;
    private JButton fullTerminationBtn;
    private JButton partialTerminationBtn;

    private JButton chooseFileBtn;

    private JPanel actionPanel;

    private TradingApp tradingApp;

    private JTextArea outputArea;
    private String defaultLocalTimeZone = "UTC";

    public ZonedDateTime TDzonedDateTime;

    public String TDformattedDateTimeString;

    public LabelAndSelectorPanel firmLsp;
    public LabelAndTextPanel settlementDateLxp;
    public LabelAndTextPanel effectiveDateLxp;
    public LabelAndTextPanel maturityDateLxp;
    public LabelAndTextPanel dealIdLxp;
    public LabelAndTextPanel  quantityLxp;
    public LabelAndTextPanel  fixedRateLxp;

    public LabelAndTextPanel  initialReferenceRateLxp;
    public LabelAndTextPanel  fixedMarketValueLxp;
    public LabelAndTextPanel  floatSpreadLxp;

    public LabelAndTextPanel floatMarketValueLxp;
    public LabelAndTextPanel settlementAmountLxp;
    public LabelAndTextPanel statusLxp;
    public LabelAndSelectorPanel centralCounterpartyLsp;
    public LabelAndSelectorPanel executingBrokerLsp;
    public LabelAndSelectorPanel clearingBrokerLsp;

    public LabelAndSelectorPanel customerAccountLsp;



    public JComboBox firmLEIField;
    public JComboBox fixedRatePartyField;

    public JComboBox floatingRatePartyField;
    public JComboBox ccpLEIField;
    public JComboBox clearingBrokerLEIField;
    public JComboBox executingBrokerLEIField;
    public JComboBox customerAccountField;

    public JComboBox currencyField;

    public JComboBox businessDayField;

    public JTextField tradeIdField;

    public JTextField tradeUTIField;

    public JTextField tradeDateField;

    public JTextField executionTimeField;

    public JComboBox floatingRatePayFreqField;

    public JComboBox fixedRatePayFreqField;

    public ClearedSwapModel clearedSwapModel = new ClearedSwapModel();
    public ClearedSwapTradeTableModel clearedSwapTradeTableModel = new ClearedSwapTradeTableModel();
    public CashflowTableModel cashflowTableModel = new CashflowTableModel();

    public JPanel buildClearedSwapTicket(TradingApp tradingApp) throws SQLException, IOException {

        labelWidth = tradingApp.labelWidth;
        labelHeight = tradingApp.labelHeight;
        fieldWidth = tradingApp.fieldWidth;
        fieldHeight = tradingApp.fieldHeight;
        columnWidth = tradingApp.columnWidth;
        btnWidth = tradingApp.btnWidth;
        btnHeight = tradingApp.btnHeight;
        Integer mainFrameWidth = tradingApp.mainFrameWidth;
        Integer mainFrameHeight = tradingApp.mainFrameHeight;
        Integer tabPanelWidth = mainFrameWidth;
        Integer tabPaneHeight = tradingApp.tabPanelHeight;
        Integer actionPanelWidth = mainFrameWidth-25;
        Integer actionPanelHeight = tradingApp.actionPanelHeight;

        IcmaRepoUtil ru = new IcmaRepoUtil();
        this.tradingApp = tradingApp;

        Border blackline = BorderFactory.createLineBorder(Color.black);
        clearedSwapContainer = new JPanel();
        clearedSwapContainer.setLayout(new FlowLayout(FlowLayout.LEFT));
        clearedSwapContainer.setSize(mainFrameWidth, mainFrameHeight);
        clearedSwapContainer.setMinimumSize(new Dimension(mainFrameWidth, mainFrameHeight));
        clearedSwapContainer.setPreferredSize(new Dimension(mainFrameWidth, mainFrameHeight));
        //repoContainer.setBackground(Color.WHITE);
        clearedSwapContainer.setBorder(blackline);

        JPanel csp = new JPanel();

        tc = new JPanel();
        GridLayout mainPanel = new GridLayout(1, 3);
        mainPanel.setHgap(10);
        mainPanel.setVgap(0);
        tc.setLayout(mainPanel);



        GridLayout colPanel = new GridLayout(20, 1);
        colPanel.setHgap(20);
        colPanel.setVgap(0);
        LabelAndTextPanel lxp = new LabelAndTextPanel();
        LabelAndSelectorPanel lsp = new LabelAndSelectorPanel();

        c1 = new JPanel();
        //c1.setBackground(Color.BLUE);
        c1.setLayout(colPanel);

        c2 = new JPanel();
        //c2.setBackground(Color.GREEN);
        c2.setLayout(colPanel);

        c3 = new JPanel();
        //c3.setBackground(Color.ORANGE);
        c3.setLayout(colPanel);

        //Action Button Panel
        actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        GridBagConstraints apgbc = new GridBagConstraints();
        apgbc.gridy = 0;
        apgbc.gridx = 0;
        apgbc.anchor = GridBagConstraints.SOUTHWEST;

        actionPanel.setSize( actionPanelWidth, actionPanelHeight);
        actionPanel.setMinimumSize(new Dimension( actionPanelWidth, actionPanelHeight));
        actionPanel.setPreferredSize(new Dimension( actionPanelWidth, actionPanelHeight));
        actionPanel.setBorder(blackline);
        //actionPanel.setBackground(Color.GREEN);
        this.tradingApp = tradingApp;
        this.outputArea = tradingApp.outputArea;

        this.clearedSwapTabbedPane = new JTabbedPane();
        clearedSwapTabbedPane.setSize(tabPanelWidth, tabPaneHeight);
        clearedSwapTabbedPane.addTab("Trade", null, tc, null);
        clearedSwapContainer.add(clearedSwapTabbedPane);

        //Firm
        JPanel firmLEIPanel = new JPanel(new GridBagLayout());
        JLabel firmLEILabel = new JLabel("Firm", JLabel.LEFT);
        firmLEILabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        firmLEIField = new JComboBox();
        ru.addParties(firmLEIField);

        firmLEIField.setAlignmentX(Component.LEFT_ALIGNMENT);
        firmLEIField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        firmLEIPanel.add(firmLEILabel);
        firmLEIPanel.add(firmLEIField);
        c1.add(firmLEIPanel);
        firmLEIField.setSelectedIndex(1);



        //Trade Date
        JPanel tradeDatePanel = new JPanel(new GridBagLayout());
        JLabel tradeDateLabel = new JLabel("Trade Date", JLabel.LEFT);
        tradeDateLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        tradeDateField = new JTextField(labelHeight);
        LocalDateTime localDateTime = LocalDateTime.now();
        TDzonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of(this.defaultLocalTimeZone));
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        TDformattedDateTimeString = TDzonedDateTime.format(formatter);

        tradeDateField.setText(TDformattedDateTimeString);
        tradeDatePanel.add(tradeDateLabel);
        tradeDatePanel.add(tradeDateField);
        c1.add(tradeDatePanel);

        //Settlement Date
        settlementDateLxp = new LabelAndTextPanel();
        JPanel settlementDatePanel = settlementDateLxp.createLabelAndTextPanel("Settlement Date","");
        c1.add(settlementDatePanel);

        //Effective Date
        effectiveDateLxp = new LabelAndTextPanel();
        JPanel effectiveDatePanel = effectiveDateLxp.createLabelAndTextPanel("Effective Date","");
        c1.add(effectiveDatePanel);

        //Maturity Date
        maturityDateLxp = new LabelAndTextPanel();
        JPanel maturityDatePanel = maturityDateLxp.createLabelAndTextPanel("Maturity Date","");
        c1.add(maturityDatePanel);

        //Open Close
        String[] opencloseChoices = {"Open", "Close"};
        JPanel openClosePanel = lsp.createLabelAndSelectorPanel("Open/Close", opencloseChoices);
        c1.add(openClosePanel);

        //Customer Account
        JPanel customerAccountPanel = new JPanel(new GridBagLayout());
        JLabel customerAccountLabel = new JLabel("Customer Account", JLabel.LEFT);
        customerAccountLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        customerAccountField = new JComboBox();
        ru.addCustomerAccounts(customerAccountField);

        customerAccountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerAccountField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        customerAccountPanel.add(customerAccountLabel);
        customerAccountPanel.add(customerAccountField);
        c1.add(customerAccountPanel);
        customerAccountField.setSelectedIndex(1);


        //CCP
        JPanel ccpLEIPanel = new JPanel(new GridBagLayout());
        JLabel ccpLEILabel = new JLabel("CCP", JLabel.LEFT);
        ccpLEILabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        ccpLEIField = new JComboBox();
        ru.addCcps(ccpLEIField);

        ccpLEIField.setAlignmentX(Component.LEFT_ALIGNMENT);
        ccpLEIField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        ccpLEIPanel.add(ccpLEILabel);
        ccpLEIPanel.add(ccpLEIField);
        c1.add(ccpLEIPanel);
        ccpLEIField.setSelectedIndex(1);

        //Deal ID
        dealIdLxp = new LabelAndTextPanel();
        JPanel dealID = dealIdLxp.createLabelAndTextPanel("Deal ID","");
        dealIdLxp.panelTextField.setText("USIX1876567US9");
        c1.add(dealID);


        //Clearing Broker
        JPanel clearingBrokerPanel = new JPanel(new GridBagLayout());
        JLabel clearingBrokerLabel = new JLabel("Clearing Broker", JLabel.LEFT);
        clearingBrokerLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        clearingBrokerLEIField = new JComboBox();
        ru.addClearingBrokers(clearingBrokerLEIField);

        clearingBrokerLEIField.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearingBrokerLEIField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        clearingBrokerPanel.add(clearingBrokerLabel);
        clearingBrokerPanel.add(clearingBrokerLEIField);
        c1.add(clearingBrokerPanel);
        clearingBrokerLEIField.setSelectedIndex(1);


        //Executing Broker
        JPanel executingBrokerPanel = new JPanel(new GridBagLayout());
        JLabel executingBrokerLabel = new JLabel("Executing Broker", JLabel.LEFT);
        executingBrokerLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        executingBrokerLEIField = new JComboBox();
        ru.addClearingBrokers(executingBrokerLEIField);

        executingBrokerLEIField.setAlignmentX(Component.LEFT_ALIGNMENT);
        executingBrokerLEIField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        executingBrokerPanel.add(executingBrokerLabel);
        executingBrokerPanel.add(executingBrokerLEIField);
        c1.add(executingBrokerPanel);
        executingBrokerLEIField.setSelectedIndex(1);

        //Quantity
        quantityLxp = new LabelAndTextPanel();
        JPanel quantity = quantityLxp.createLabelAndTextPanel("Quantity","");
        quantityLxp.panelTextField.setText("10,000,000");
        c1.add(quantity);

        //Currency
        String[] currencychoices = {"USD", "GBP", "EUR", "AUD"};
        JPanel currencyPanel = new JPanel(new GridBagLayout());
        JLabel currencyLabel = new JLabel("Currency", JLabel.LEFT);
        currencyLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));

        currencyField = new JComboBox<String>(currencychoices);
        currencyField.setAlignmentX(Component.LEFT_ALIGNMENT);
        currencyField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        currencyPanel.add(currencyLabel);
        currencyPanel.add(currencyField);
        c1.add(currencyPanel);

        //Business Day
        String[] businessDaychoices = {"None", "Following", "Modified Following", "Preceding", "Modified Preceding", "Nearest", "FRN"};
        JPanel businessDayPanel = new JPanel(new GridBagLayout());
        JLabel businessDayLabel = new JLabel("Business Day", JLabel.LEFT);
        businessDayLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));

        businessDayField = new JComboBox<String>(businessDaychoices);
        businessDayField.setAlignmentX(Component.LEFT_ALIGNMENT);
        businessDayField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        businessDayPanel.add(businessDayLabel);
        businessDayPanel.add(businessDayField);
        c1.add(businessDayPanel);

        //Status
        statusLxp = new LabelAndTextPanel();
        JPanel statusPanel = statusLxp.createLabelAndTextPanel("Status","");
        c1.add(statusPanel);

        //Execution Time
        JPanel executionTimePanel = new JPanel(new GridBagLayout());
        JLabel executionTimeLabel = new JLabel("Execution Time", JLabel.LEFT);
        executionTimeLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        executionTimeField = new JTextField(labelHeight);

        DateTimeFormatter ETformatter = DateTimeFormatter.ofPattern("hh:mm:ss");
        ETzonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of(this.defaultLocalTimeZone));
        ETformattedDateTimeString = ETzonedDateTime.format(ETformatter);
        executionTimeField.setText(ETformattedDateTimeString);
        executionTimePanel.add(executionTimeLabel);
        executionTimePanel.add(executionTimeField);
        c1.add(executionTimePanel);

        //UTI
        JPanel tradeUTIPanel = new JPanel(new GridBagLayout());
        JLabel tradeUTILabel = new JLabel("Trade UTI", JLabel.LEFT);
        tradeUTILabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        tradeUTIField = new JTextField(labelHeight);
        //USI
        String tdt = tradeDateField.getText().trim().toString();
        String tradeUTIStr = "USI"+ tdt.trim().replaceAll("-", "") + executionTimeField.getText().trim().replaceAll("\\s", "");;
        tradeUTIField.setText(tradeUTIStr);

        tradeUTIPanel.add(tradeUTILabel);
        tradeUTIPanel.add(tradeUTIField);
        c1.add(tradeUTIPanel);



        //Settlement Amount
        settlementAmountLxp = new LabelAndTextPanel();
        JPanel settlementAmountPanel = settlementAmountLxp.createLabelAndTextPanel("Settlement Amount","");
        c1.add(settlementAmountPanel);

        //Pay or Receive Payment
        String[] directionchoices = {"Pay", "Receive"};
        JPanel settlementPayReceivePanel = lsp.createLabelAndSelectorPanel("Pay/Receive", directionchoices);
        c1.add(settlementPayReceivePanel);

        //Fixed Rate Field
        JPanel fixedRatePartyPanel = new JPanel(new GridBagLayout());
        JLabel fixedRatePartyLabel = new JLabel("Fixed Party", JLabel.LEFT);
        fixedRatePartyLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        fixedRatePartyField = new JComboBox();
        ru.addParties(fixedRatePartyField);

        fixedRatePartyField.setAlignmentX(Component.LEFT_ALIGNMENT);
        fixedRatePartyField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        fixedRatePartyPanel.add(fixedRatePartyLabel);
        fixedRatePartyPanel.add(fixedRatePartyField);
        c2.add(fixedRatePartyPanel);
        fixedRatePartyField.setSelectedIndex(1);

        //Fixed Rate
        fixedRateLxp = new LabelAndTextPanel();
        JPanel fixedRatePanel = fixedRateLxp.createLabelAndTextPanel("Fixed Rate","");
        fixedRateLxp.panelTextField.setText("4.55");
        c2.add(fixedRatePanel);

        //Fixed Rate Pay Freq
        String[] fixedRatePayFreqchoices = {"Annual", "Semi-Annual", "Quarterly", "Monthly"};
        JPanel fixedRatePayFreqPanel = new JPanel(new GridBagLayout());
        JLabel fixedRatePayFreqLabel = new JLabel("Pay Frequency", JLabel.LEFT);
        fixedRatePayFreqLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));

        fixedRatePayFreqField = new JComboBox<String>(fixedRatePayFreqchoices);
        fixedRatePayFreqField.setAlignmentX(Component.LEFT_ALIGNMENT);
        fixedRatePayFreqField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        fixedRatePayFreqPanel.add(fixedRatePayFreqLabel);
        fixedRatePayFreqPanel.add(fixedRatePayFreqField);
        c2.add(fixedRatePayFreqPanel);

        //Day Count
        String[] dayCountChoice = {"ACT/365", "ACT/360", "30/360"};
        JPanel dayCountPanel = lsp.createLabelAndSelectorPanel("Day Count", dayCountChoice);
        c2.add(dayCountPanel);


        //Market Value
        fixedMarketValueLxp = new LabelAndTextPanel();
        JPanel fixedMarketValuePanel = fixedMarketValueLxp.createLabelAndTextPanel("Market Value","");
        c2.add(fixedMarketValuePanel);


        //Floating Field
        JPanel floatingRatePartyPanel = new JPanel(new GridBagLayout());
        JLabel floatingRatePartyLabel = new JLabel("Floating Rate Party", JLabel.LEFT);
        floatingRatePartyLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
        floatingRatePartyField = new JComboBox();
        ru.addParties(floatingRatePartyField);

        floatingRatePartyField.setAlignmentX(Component.LEFT_ALIGNMENT);
        floatingRatePartyField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        floatingRatePartyPanel.add(floatingRatePartyLabel);
        floatingRatePartyPanel.add(floatingRatePartyField);
        c3.add(floatingRatePartyPanel);
        floatingRatePartyField.setSelectedIndex(2);

        //Reference Rate
        String[] rateReference = {"USD_SOFR", "EURIBOR_SWAP"};
        JPanel rateReferencePanel = lsp.createLabelAndSelectorPanel("Rate Reference", rateReference);
        c3.add(rateReferencePanel);

        //Initial Reference Rate
        initialReferenceRateLxp = new LabelAndTextPanel();
        JPanel initialReferenceRatePanel = initialReferenceRateLxp.createLabelAndTextPanel("Initial Rate","");
        initialReferenceRateLxp.panelTextField.setText("4.83");
        c3.add(initialReferenceRatePanel);

        //Spread
        floatSpreadLxp = new LabelAndTextPanel();
        JPanel spreadPanel = floatSpreadLxp.createLabelAndTextPanel("Spread(bps)","");
        floatSpreadLxp.panelTextField.setText("50");
        c3.add(spreadPanel);

        //Fixed Rate Pay Freq
        String[] floatingRatePayFreqchoices = {"Annual", "Semi-Annual", "Quarterly", "Monthly"};
        JPanel floatingRatePayFreqPanel = new JPanel(new GridBagLayout());
        JLabel floatingRatePayFreqLabel = new JLabel("Pay Frequency", JLabel.LEFT);
        floatingRatePayFreqLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));

        floatingRatePayFreqField = new JComboBox<String>(floatingRatePayFreqchoices);
        floatingRatePayFreqField.setAlignmentX(Component.LEFT_ALIGNMENT);
        floatingRatePayFreqField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        floatingRatePayFreqPanel.add(floatingRatePayFreqLabel);
        floatingRatePayFreqPanel.add(floatingRatePayFreqField);
        c3.add(floatingRatePayFreqPanel);


        //Day Count
        String[] floatDayCountChoice = {"ACT/365", "ACT/360", "30/360"};
        JPanel floatDayCountPanel = lsp.createLabelAndSelectorPanel("Day Count", floatDayCountChoice);
        c3.add(floatDayCountPanel);


        //Market Value
        floatMarketValueLxp = new LabelAndTextPanel();
        JPanel floatMarketValuePanel = floatMarketValueLxp.createLabelAndTextPanel("Market Value","");
        c3.add(floatMarketValuePanel);

        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.gridy = 1;
        gbc3.gridx = 0;
        gbc3.fill = GridBagConstraints.HORIZONTAL;
        gbc3.gridwidth = 3;

        GridBagConstraints gbc4 = new GridBagConstraints();
        gbc4.gridy = 2;
        gbc4.gridx = 0;
        gbc4.fill = GridBagConstraints.HORIZONTAL;
        gbc4.gridwidth = 3;


        JScrollPane swapTradeBlotterPanel = new JScrollPane(this.swapTradeBlotter);
        this.swapTradeBlotter.setModel(this.clearedSwapTradeTableModel);
        swapTradeBlotterPanel.setPreferredSize(new Dimension(mainFrameWidth-50,200));
        swapTradeBlotterPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollPane swapCashflowBlotterPanel= new JScrollPane(this.swapCashflowBlotter);
        this.swapCashflowBlotter.setModel(this.cashflowTableModel);
        swapCashflowBlotterPanel.setPreferredSize(new Dimension(mainFrameWidth-50,200));
        swapCashflowBlotterPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);



        Integer btnCount = 6;
        Integer actionBtnPanelWidth = btnCount  * btnWidth;
        Integer actionBtnPanelHeight = actionPanelHeight-10;

        JPanel actionBtnPanel = new JPanel(new GridLayout(2, 6));

        actionBtnPanel.setSize(actionBtnPanelWidth, actionBtnPanelHeight);
        actionBtnPanel.setMinimumSize(new Dimension(actionBtnPanelWidth, actionBtnPanelHeight));
        actionBtnPanel.setMaximumSize(new Dimension(actionBtnPanelWidth, actionBtnPanelHeight));
        actionBtnPanel.setPreferredSize(new Dimension(actionBtnPanelWidth, actionBtnPanelHeight));

        newTradeBtn = new JButton("New Trade");
        newTradeBtn.addActionListener(this);
        newTradeBtn.setPreferredSize(new Dimension(btnWidth, btnHeight));
        newTradeBtn.setMaximumSize(new Dimension(btnWidth, btnHeight));
        actionBtnPanel.add(newTradeBtn);

        bookTradeBtn = new JButton("Book/Modify Trade");
        bookTradeBtn.addActionListener(this);
        bookTradeBtn.setPreferredSize(new Dimension(btnWidth, btnHeight));
        bookTradeBtn.setMaximumSize(new Dimension(btnWidth, btnHeight));
        actionBtnPanel.add(bookTradeBtn);

        settleBtn = new JButton("Settle");
        settleBtn.addActionListener(this);
        settleBtn.setPreferredSize(new Dimension(btnWidth, btnHeight));
        settleBtn.setMaximumSize(new Dimension(btnWidth, btnHeight));
        actionBtnPanel.add(settleBtn);

        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(this);
        cancelBtn.setPreferredSize(new Dimension(btnWidth, btnHeight));
        cancelBtn.setMaximumSize(new Dimension(btnWidth, btnHeight));
        actionBtnPanel.add(cancelBtn);


        resetBtn = new JButton("Reset");
        resetBtn.addActionListener(this);
        resetBtn.setPreferredSize(new Dimension(btnWidth, btnHeight));
        resetBtn.setMaximumSize(new Dimension(btnWidth, btnHeight));
        actionBtnPanel.add(resetBtn);

        closoutBtn = new JButton("Closeout");
        closoutBtn.addActionListener(this);
        closoutBtn.setPreferredSize(new Dimension(btnWidth, btnHeight));
        closoutBtn.setMaximumSize(new Dimension(btnWidth, btnHeight));
        actionBtnPanel.add(closoutBtn);

        fullTerminationBtn = new JButton("Terminate");
        fullTerminationBtn.addActionListener(this);
        fullTerminationBtn.setPreferredSize(new Dimension(btnWidth, btnHeight));
        fullTerminationBtn.setMaximumSize(new Dimension(btnWidth, btnHeight));
        actionBtnPanel.add(fullTerminationBtn);

        chooseFileBtn = new JButton("Choose File");
        chooseFileBtn.addActionListener(this);
        chooseFileBtn.setPreferredSize(new Dimension(btnWidth, btnHeight));
        chooseFileBtn.setMaximumSize(new Dimension(btnWidth, btnHeight));
        actionBtnPanel.add(chooseFileBtn);


        actionPanel.add(actionBtnPanel);
        actionBtnPanel.setVisible(true);
        actionPanel.setVisible(true);

        tc.add(c1);
        tc.add(c2);
        tc.add(c3);
        clearedSwapContainer.add(swapTradeBlotterPanel,gbc3);
        clearedSwapContainer.add(swapCashflowBlotterPanel,gbc4);
        tc.setVisible( true );

        clearedSwapContainer.add(actionPanel);
        clearedSwapContainer.setVisible(true);

        loadNewTrade();


        return clearedSwapContainer;

    }

    @Override
    public void actionPerformed(ActionEvent ae) {

        IcmaRepoUtil ru = new IcmaRepoUtil();

        if (ae.getSource() == this.newTradeBtn) {
            JOptionPane.showMessageDialog(this.clearedSwapContainer, "Create New Trade", "Alert", JOptionPane.INFORMATION_MESSAGE);
            try {
                this.loadNewTrade();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } else if (ae.getSource() == this.bookTradeBtn) {
            JOptionPane.showMessageDialog(this.clearedSwapContainer, "Book/Modify Trade", "Alert", JOptionPane.INFORMATION_MESSAGE);
            try {
                String source = "app";
                ClearedSwapModel csm = new ClearedSwapModel();
                csm = clearedSwapModelMapper(source);

                String businessEvent = this.bookTrade(source, csm);
                this.statusLxp.panelTextField.setText("EXECUTED");
                System.out.println(businessEvent);
                outputArea.setText(businessEvent);

                ru = new IcmaRepoUtil();
                this.tradeStateStr = ru.getAfterTradeState(businessEvent);
                this.afterTradeStateStr = this.tradeStateStr;


                DateTimeFormatter eventDateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
                LocalDateTime localDateTime = LocalDateTime.now();
                String eventDateTime = localDateTime.format(eventDateFormat);

                ru.writeEventToFile("irs-execution-business-event", eventDateTime, businessEvent);

                clearedSwapTradeTableModel.addTrade(businessEvent);

                updateCashflowTableFromCdmTrade(businessEvent);


            } catch (IOException ignored) {
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }  else if (ae.getSource() == this.cancelBtn) {
            JOptionPane.showMessageDialog(this.clearedSwapContainer, "Cancel", "Alert", JOptionPane.INFORMATION_MESSAGE);
            try {
                String source = "app";
                ClearedSwapModel csm = new ClearedSwapModel();
                csm = clearedSwapModelMapper(source);

                String businessEvent = this.cancelClearedSwap();
                this.statusLxp.panelTextField.setText("CANCELED");
                System.out.println(businessEvent);
                //outputArea.setText(businessEvent);

                ru = new IcmaRepoUtil();
                //this.tradeStateStr = ru.getAfterTradeState(businessEvent);
                //this.afterTradeStateStr = this.tradeStateStr;


                DateTimeFormatter eventDateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
                LocalDateTime localDateTime = LocalDateTime.now();
                String eventDateTime = localDateTime.format(eventDateFormat);

                ru.writeEventToFile("cancel-business-event", eventDateTime, businessEvent);

                //this.loadTrades();

            } catch (IOException ignored) {
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (ae.getSource() == this.settleBtn) {
            JOptionPane.showMessageDialog(this.clearedSwapContainer, "Settle", "Alert", JOptionPane.INFORMATION_MESSAGE);
            try {
                String source = "app";
                ClearedSwapModel csm = new ClearedSwapModel();
                csm = clearedSwapModelMapper(source);

                String businessEvent = this.settleClearedSwap();
                this.statusLxp.panelTextField.setText("SETTLE");
                System.out.println(businessEvent);
                //outputArea.setText(businessEvent);

                ru = new IcmaRepoUtil();
                //this.tradeStateStr = ru.getAfterTradeState(businessEvent);
                //this.afterTradeStateStr = this.tradeStateStr;


                DateTimeFormatter eventDateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
                LocalDateTime localDateTime = LocalDateTime.now();
                String eventDateTime = localDateTime.format(eventDateFormat);

                ru.writeEventToFile("settle-business-event", eventDateTime, businessEvent);

                //this.loadTrades();

            } catch (IOException ignored) {
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }  else if (ae.getSource() == this.resetBtn) {
            JOptionPane.showMessageDialog(this.clearedSwapContainer, "Reset", "Alert", JOptionPane.INFORMATION_MESSAGE);
            try {
                String source = "app";
                ClearedSwapModel csm = new ClearedSwapModel();
                csm = clearedSwapModelMapper(source);

                String businessEvent = this.resetClearedSwap();
                this.statusLxp.panelTextField.setText("RESET");
                System.out.println(businessEvent);
                //outputArea.setText(businessEvent);

                ru = new IcmaRepoUtil();
                //this.tradeStateStr = ru.getAfterTradeState(businessEvent);
                //this.afterTradeStateStr = this.tradeStateStr;


                DateTimeFormatter eventDateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
                LocalDateTime localDateTime = LocalDateTime.now();
                String eventDateTime = localDateTime.format(eventDateFormat);

                ru.writeEventToFile("reset-business-event", eventDateTime, businessEvent);

                //this.loadTrades();

            } catch (IOException ignored) {
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else if (ae.getSource() == this.closoutBtn) {
            JOptionPane.showMessageDialog(this.clearedSwapContainer, "Close Out", "Alert", JOptionPane.INFORMATION_MESSAGE);
            try {
                String source = "app";
                ClearedSwapModel csm = new ClearedSwapModel();
                csm = clearedSwapModelMapper(source);

                String businessEvent = this.closeoutClearedSwap();
                this.statusLxp.panelTextField.setText("CLOSEOUT");
                System.out.println(businessEvent);
                //outputArea.setText(businessEvent);

                ru = new IcmaRepoUtil();
                //this.tradeStateStr = ru.getAfterTradeState(businessEvent);
                //this.afterTradeStateStr = this.tradeStateStr;


                DateTimeFormatter eventDateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
                LocalDateTime localDateTime = LocalDateTime.now();
                String eventDateTime = localDateTime.format(eventDateFormat);

                ru.writeEventToFile("irs-execution-business-event", eventDateTime, businessEvent);

                //this.loadTrades();

            } catch (IOException ignored) {
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (ae.getSource() == this.fullTerminationBtn) {
            JOptionPane.showMessageDialog(this.clearedSwapContainer, "Full Termination", "Alert", JOptionPane.INFORMATION_MESSAGE);
            try {
                String source = "app";
                ClearedSwapModel csm = new ClearedSwapModel();
                csm = clearedSwapModelMapper(source);

                String businessEvent = this.clearedSwapFullTermination();
                this.statusLxp.panelTextField.setText("TERMINATION");
                System.out.println(businessEvent);
                //outputArea.setText(businessEvent);

                ru = new IcmaRepoUtil();
                //this.tradeStateStr = ru.getAfterTradeState(businessEvent);
                //this.afterTradeStateStr = this.tradeStateStr;


                DateTimeFormatter eventDateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
                LocalDateTime localDateTime = LocalDateTime.now();
                String eventDateTime = localDateTime.format(eventDateFormat);

                ru.writeEventToFile("fulltermination-business-event", eventDateTime, businessEvent);

                //this.loadTrades();

            } catch (IOException ignored) {
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }  else if (ae.getSource() == chooseFileBtn) {
            JOptionPane.showMessageDialog(clearedSwapContainer, "Select File", "Alert", JOptionPane.INFORMATION_MESSAGE);

            JFileChooser fc  = new JFileChooser();
            File cd = new File("./upload");
            fc.setCurrentDirectory(cd);

            WorkflowStep wfs = WorkflowStep.builder();

            int returnVal = fc.showDialog(clearedSwapContainer, "Attach");
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File loadFile = fc.getSelectedFile();
                String name = loadFile.getName();
                int lastIndexOf = name.lastIndexOf(".");
                if (lastIndexOf == -1) {
                    JOptionPane.showMessageDialog(clearedSwapContainer, "Select File", "Alert", JOptionPane.INFORMATION_MESSAGE);
                }
                String fileType = name.substring(lastIndexOf);
                //This is where a real application would open the file.
                System.out.println("Opening: " + loadFile.getName());
                //Convert XML file to object

                if(fileType.equals(".xml")) {
                    LoadCmeSwaplNewTrade lxml = new LoadCmeSwaplNewTrade();
                    wfs = lxml.createNewSwapTradeFromFpml(loadFile);
                }else if (fileType.equals(".json")){
                    LoadJsonNewTrade ljson = new LoadJsonNewTrade();
                    try {
                        wfs = ljson.createNewTradeFromJsontoXml(loadFile);
                    } catch (ParserConfigurationException e) {
                        throw new RuntimeException(e);
                    }
                }else if (fileType.equals(".csv")){
                    LoadCsvNewTrade lcsv = new LoadCsvNewTrade();
                    try {
                        wfs = lcsv.createClearedSwapTradeFromCsv1(loadFile);
                        lcsv.createClearedSwapTradeFromCsv2(loadFile);
                    } catch (ParserConfigurationException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (SAXException e) {
                        throw new RuntimeException(e);
                    }

                }else{
                    JOptionPane.showMessageDialog(clearedSwapContainer, "Cannot load file type " + fileType , "Alert", JOptionPane.INFORMATION_MESSAGE);

                }


                String businessEvent = null;
                try {
                    businessEvent = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(wfs.getBusinessEvent());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                DateTimeFormatter eventDateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
                LocalDateTime localDateTime = LocalDateTime.now();
                String eventDateTime = localDateTime.format(eventDateFormat);

                try {
                    ru.writeEventToFile("irs-execution-business-event", eventDateTime, businessEvent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try {
                    clearedSwapTradeTableModel.addTrade(businessEvent);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }


            }
        }
    }

    public void loadNewTrade() throws IOException, SQLException {


        //Trade Date

        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime startDateTime = ZonedDateTime.of(localDateTime, ZoneId.of(this.defaultLocalTimeZone));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDateTimeString = startDateTime.format(formatter);
        tradeDateField.setText(formattedDateTimeString);

        ZonedDateTime settlementDateTime = startDateTime.plusDays(2);
        String settlementDateTimeString = settlementDateTime.format(formatter);


        settlementDateLxp.panelTextField.setText(settlementDateTimeString);

        effectiveDateLxp.panelTextField.setText(settlementDateTimeString);

        ZonedDateTime maturityDateTime = settlementDateTime.plusYears(5);
        String maturityDateTimeString = maturityDateTime.format(formatter);
        maturityDateLxp.panelTextField.setText(maturityDateTimeString);

        quantityLxp.panelTextField.setText("10,000,000");
        fixedRateLxp.panelTextField.setText("4.5");

        String fixedRate = fixedRateLxp.panelTextField.getText().trim().toString();
        String notional = quantityLxp.panelTextField.getText().trim().toString().replaceAll(",","");

        createFixedRateCashflows(settlementDateTimeString,maturityDateTimeString,notional, fixedRate);

        //Reset trade states to null
        this.afterTradeStateStr = null;
        this.beforeTradeStateStr = null;
        this.tradeStateStr = null;

    }

    public String bookTrade(String source, ClearedSwapModel csm) throws IOException, SQLException {


        String businessEvent=null;

        ClearedSwapLifecycle clearedSwapLifecycle = new ClearedSwapLifecycle();
        businessEvent = clearedSwapLifecycle.bookNewTrade(csm);

        return businessEvent;
    }

    public String cancelClearedSwap() throws IOException, SQLException {

        if (this.afterTradeStateStr == null) {
            JOptionPane.showMessageDialog(tradingApp, "No active trade, please book trade before attempting to terminate.", "Alert", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        String eventResult = null;
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime eventDate = ZonedDateTime.of(localDateTime, ZoneId.of(this.defaultLocalTimeZone));

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String eventDateStr = eventDate.format(formatter);
        String effectiveDateStr = eventDate.format(formatter);

        ClearedSwapLifecycle clearedSwapLifecycle = new ClearedSwapLifecycle();
        eventResult = clearedSwapLifecycle.cancelClearedSwap(this.afterTradeStateStr, eventDateStr, effectiveDateStr);

        return eventResult;
    }

    public String resetClearedSwap() throws IOException, SQLException {

        if (this.afterTradeStateStr == null) {
            JOptionPane.showMessageDialog(tradingApp, "No active trade, please book trade before attempting to terminate.", "Alert", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        String eventResult = null;
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime eventDate = ZonedDateTime.of(localDateTime, ZoneId.of(this.defaultLocalTimeZone));

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String eventDateStr = eventDate.format(formatter);
        String effectiveDateStr = eventDate.format(formatter);

        ClearedSwapLifecycle clearedSwapLifecycle = new ClearedSwapLifecycle();
        eventResult = clearedSwapLifecycle.createResetEvent(this.afterTradeStateStr, eventDateStr, eventDateStr, effectiveDateStr);

        return eventResult;
    }

    public String settleClearedSwap() throws IOException, SQLException {


        if (this.afterTradeStateStr == null) {
            JOptionPane.showMessageDialog(tradingApp, "No active trade, please book trade before attempting to terminate.", "Alert", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        String eventResult = null;

        return eventResult;
    }


    public String clearedSwapFullTermination() throws IOException, SQLException {


        if (this.afterTradeStateStr == null) {
            JOptionPane.showMessageDialog(tradingApp, "No active trade, please book trade before attempting to terminate.", "Alert", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        String eventResult = null;

        return eventResult;
    }

    public ClearedSwapModel clearedSwapModelMapper(String source){


        ClearedSwapModel csm = new ClearedSwapModel();

        csm.tradeDate = tradeDateField.getText().trim().toString();
        csm.settlementDate = settlementDateLxp.panelTextField.getText().trim().toString();
        csm.effectiveDate = effectiveDateLxp.panelTextField.getText().trim().toString();
        csm.maturityDate = maturityDateLxp.panelTextField.getText().trim().toString();
        csm.fixedRate = fixedRateLxp.panelTextField.getText().trim().toString();
        csm.spread = floatSpreadLxp.panelTextField.getText().trim().toString();
        csm.quantity = quantityLxp.panelTextField.getText().trim().toString().replaceAll(",", "");
        csm.dealId = tradeUTIField.getText().trim().toString();
        csm.currency = this.firmLEIField.getSelectedItem().toString();
        csm.floatingRateReference = initialReferenceRateLxp.panelTextField.getText().trim().toString();

        CItem item = (CItem) this.firmLEIField.getSelectedItem();
        String itemNameStr = item.getClabel();
        String itemCValue = item.getCValue();
        csm.firm = itemCValue;

        CItem cbItem = (CItem) this.clearingBrokerLEIField.getSelectedItem();
        itemNameStr = cbItem.getClabel();
        itemCValue = cbItem.getCValue();
        csm.clearingBroker = itemCValue;

        CItem ebItem = (CItem) this.executingBrokerLEIField.getSelectedItem();
        itemNameStr = ebItem.getClabel();
        itemCValue = ebItem.getCValue();
        csm.executingBroker = itemCValue;

        CItem caItem = (CItem) this.customerAccountField.getSelectedItem();
        itemNameStr = caItem.getClabel();
        itemCValue = caItem.getCValue();
        csm.customerAccount = itemCValue;

        CItem ccpItem = (CItem) this.ccpLEIField.getSelectedItem();
        itemNameStr = ccpItem.getClabel();
        itemCValue = ccpItem.getCValue();
        csm.centralCounterparty = itemCValue;

        CItem fixedRatePayer = (CItem) this.fixedRatePartyField.getSelectedItem();
        itemNameStr = fixedRatePayer.getClabel();
        itemCValue = fixedRatePayer.getCValue();
        csm.fixedRatePayer = itemCValue;

        CItem floatingRatePayer = (CItem) this.floatingRatePartyField.getSelectedItem();
        itemNameStr = floatingRatePayer.getClabel();
        itemCValue = floatingRatePayer.getCValue();
        csm.floatingRatePayer = itemCValue;


        return csm;

    }

    public String closeoutClearedSwap() throws IOException, SQLException {

        if (this.afterTradeStateStr == null) {
            JOptionPane.showMessageDialog(tradingApp, "No active trade, please book trade before attempting to terminate.", "Alert", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        String eventResult = null;
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime eventDate = ZonedDateTime.of(localDateTime, ZoneId.of(this.defaultLocalTimeZone));

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String eventDateStr = eventDate.format(formatter);
        String effectiveDateStr = eventDate.format(formatter);

        ClearedSwapLifecycle clearedSwapLifecycle = new ClearedSwapLifecycle();
        eventResult = clearedSwapLifecycle.cancelClearedSwap(this.afterTradeStateStr, eventDateStr, effectiveDateStr);

        return eventResult;
    }

        public void updateCashflowTableFromCdmTrade(String businessEventStr ) throws JsonProcessingException {

            cashflowTableModel.clearTable();
            IcmaRepoUtil ru = new IcmaRepoUtil();
            CdmUtil cdmUtil = new CdmUtil();

            ObjectMapper rosettaObjectMapper = RosettaObjectMapper.getNewRosettaObjectMapper();
            BusinessEvent businessEventObj = new BusinessEvent.BusinessEventBuilderImpl();
            BusinessEvent businessEvent  = rosettaObjectMapper.readValue(businessEventStr, businessEventObj.getClass());

            Trade trade  = businessEvent.getAfter().get(0).getTrade();

            List< ? extends PaymentCalculationPeriod> paymentCalculationPeriodList = new ArrayList<PaymentCalculationPeriod>();

            paymentCalculationPeriodList = trade.getTradableProduct().getProduct().getContractualProduct().getEconomicTerms().getPayout().getInterestRatePayout()
                    .get(1).getCashflowRepresentation().getPaymentCalculationPeriod();

            ZonedDateTime startDate = null;
            ZonedDateTime endDate = null;
            String startDateStr=null;
            String endDateStr=null;
            String paymentDateStr=null;
            Long dateDiff= (long)0;
            Double notional = 0.0;
            Double rate = 0.0;
            Double interest = 0.0;

            DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


            for (int i=0;i<paymentCalculationPeriodList.size();i++){

                startDateStr = paymentCalculationPeriodList.get(i).getCalculationPeriod().get(0).getAdjustedStartDate().toString() + "T00:00:00.000+00:00";;
                endDateStr = paymentCalculationPeriodList.get(i).getCalculationPeriod().get(0).getAdjustedEndDate().toString() + "T00:00:00.000+00:00";;

                startDate = ZonedDateTime.parse(startDateStr, formatter);
                endDate = ZonedDateTime.parse(endDateStr, formatter);
                paymentDateStr = shortDateFormatter.format(endDate);

                dateDiff = endDate.until(startDate, ChronoUnit.DAYS);

                rate = paymentCalculationPeriodList.get(i).getCalculationPeriod().get(0).getFixedRate().doubleValue();

                notional = paymentCalculationPeriodList.get(i).getCalculationPeriod().get(0).getNotionalAmount().doubleValue();

                interest = rate/100*dateDiff/365*notional;

                Double net = interest - 0.0;
                DecimalFormat df = new DecimalFormat("#.00");
                String interestStr = df.format(interest).toString();
                String netStr = df.format(net).toString();

                CashflowModel cashflowModel = new CashflowModel();
                cashflowModel.setCashflowDate(paymentDateStr);
                cashflowModel.setPayAmount(interestStr);
                cashflowModel.setReceiveAmount("0");
                cashflowModel.setNetAmount(netStr);
                cashflowTableModel.addCashflows(cashflowModel);

            }


        }

    public void createFixedRateCashflows (String effectiveDateStr,
                                                        String terminationDateStr,
                                                        String notionalAmount,
                                                        String fixedRate
    ) throws JsonProcessingException {

        cashflowTableModel.clearTable();
        IcmaRepoUtil ru = new IcmaRepoUtil();
        CdmUtil cdmUtil = new CdmUtil();

        ZonedDateTime startDate = null;
        ZonedDateTime endDate = null;
        String startDateStr = null;
        String endDateStr = null;
        String paymentDateStr=null;
        Long dateDiff = (long) 0;
        Double notional = 0.0;
        Double rate = 0.0;
        Double interest = 0.0;


        effectiveDateStr = effectiveDateStr + "T00:00:00.000+00:00";
        terminationDateStr = terminationDateStr + "T00:00:00.000+00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZonedDateTime effectiveDateTime = ZonedDateTime.parse(effectiveDateStr, formatter);
        ZonedDateTime terminationDateTime = ZonedDateTime.parse(terminationDateStr, formatter);

        ZonedDateTime previousDateTime = effectiveDateTime;
        ZonedDateTime nextDateTime = effectiveDateTime;
        ZonedDateTime paymentDate = effectiveDateTime;

        rate = parseDouble(fixedRate);
        notional = parseDouble(notionalAmount);

        Integer i  =0;

        while (nextDateTime.isBefore(terminationDateTime) || nextDateTime.isEqual(terminationDateTime)) {

            paymentDate = nextDateTime;

            startDate = previousDateTime;
            endDate = nextDateTime;
            paymentDateStr = shortDateFormatter.format(endDate);

            dateDiff = endDate.until(startDate, ChronoUnit.DAYS);
            interest = rate / 100 * dateDiff / 365 * notional;

            Double net = interest - 0.0;
            DecimalFormat df = new DecimalFormat("#.00");
            String interestStr = df.format(interest).toString();
            String netStr = df.format(net).toString();

            CashflowModel cashflowModel = new CashflowModel();
            cashflowModel.setCashflowDate(paymentDateStr);
            cashflowModel.setPayAmount(interestStr);
            cashflowModel.setReceiveAmount("0");
            cashflowModel.setNetAmount(netStr);
            cashflowTableModel.addCashflows(cashflowModel);

            previousDateTime = nextDateTime;
            nextDateTime = nextDateTime.plusYears(1);
            i++;

        }
    }

}
