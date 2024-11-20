package com.finxis.cdm.crossproductapp.productmodels;

import cdm.event.common.BusinessEvent;
import cdm.product.template.EconomicTerms;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;

import javax.swing.table.AbstractTableModel;
import java.util.HashMap;

public class ClearedSwapTradeTableModel extends AbstractTableModel {

    private final static int TRADE_ID_COL = 0;
    private final static int TRADE_DATE_COL = 1;
    private final static int PAYER_COL = 2;
    private final static int RECEIVER_COL = 3;
    private final static int AMOUNT_COL = 4;
    private final static int MATURITY_DATE_COL = 5;
    private final static int FIXED_RATE_COL = 6;
    private final static int FLOATING_RATE_COL = 7;

    private final HashMap<Integer, ClearedSwapModel> rowToTrade;
    private final HashMap<String, Integer> idToRow;
    private final HashMap<String, ClearedSwapModel> idToTrade;

    private final String[] headers;

    public ClearedSwapTradeTableModel() {
        rowToTrade = new HashMap<>();
        idToRow = new HashMap<>();
        idToTrade = new HashMap<>();

        headers = new String[]
                  {"Trade ID", "Date", "Fixed Party", "Floating Party", "Amount", "Maturity Date", "Fixed Rate", "Floating Rate"};
    }


    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void addTrade(String businessEventStr) throws JsonProcessingException {

        //CdmTradeElements cdmTradeElements = new CdmTradeElements();

        ObjectMapper rosettaObjectMapper = RosettaObjectMapper.getNewRosettaObjectMapper();
        BusinessEvent businessEvent = new BusinessEvent.BusinessEventBuilderImpl();
        businessEvent = rosettaObjectMapper.readValue(businessEventStr, businessEvent.getClass());

        cdm.event.common.Trade cdmTrade = businessEvent.getAfter().get(0).getTrade();

        EconomicTerms ecomonicTerms = cdmTrade.getTradableProduct().getProduct().getContractualProduct().getEconomicTerms();
        String tradeIdentifier = cdmTrade.getTradeIdentifier().get(0).getAssignedIdentifier().get(0).getIdentifier().getValue().toString();

        ClearedSwapModel trade = new ClearedSwapModel();
        trade.dealId = tradeIdentifier;
        trade.tradeDate= cdmTrade.getTradeDate().getValue().toString();
        trade.fixedRatePayer = cdmTrade.getParty().get(1).getPartyId().get(0).getIdentifier().getValue().toString();
        trade.floatingRatePayer = cdmTrade.getParty().get(2).getPartyId().get(0).getIdentifier().getValue().toString();

        trade.fixedRate= cdmTrade.getTradableProduct().getProduct().getContractualProduct().getEconomicTerms().getPayout().getInterestRatePayout().get(1).getCashflowRepresentation().getPaymentCalculationPeriod().get(0).getCalculationPeriod().get(0).getFixedRate().toString();
        trade.floatingRateReference=cdmTrade.getTradableProduct().getProduct().getContractualProduct().getEconomicTerms().getPayout().getInterestRatePayout().get(0).getRateSpecification().getFloatingRate().getInitialRate().getValue().toString();

        trade.maturityDate=cdmTrade.getTradableProduct().getProduct().getContractualProduct().getEconomicTerms().getTerminationDate().getAdjustableDate().getAdjustedDate().getValue().toString();
        trade.quantity=cdmTrade.getTradableProduct().getTradeLot().get(0).getPriceQuantity().get(0).getQuantity().get(0).getValue().getValue().toString();


        int row = rowToTrade.size();

        rowToTrade.put(row, trade);
        idToRow.put(trade.dealId, row);
        idToTrade.put(trade.dealId, trade);

        fireTableRowsInserted(row, row);
    }

    public void addCdsTrade(String businessEventStr) throws JsonProcessingException {

        //CdmTradeElements cdmTradeElements = new CdmTradeElements();

        ObjectMapper rosettaObjectMapper = RosettaObjectMapper.getNewRosettaObjectMapper();
        BusinessEvent businessEvent = new BusinessEvent.BusinessEventBuilderImpl();
        businessEvent = rosettaObjectMapper.readValue(businessEventStr, businessEvent.getClass());

        cdm.event.common.Trade cdmTrade = businessEvent.getAfter().get(0).getTrade();

        EconomicTerms ecomonicTerms = cdmTrade.getTradableProduct().getProduct().getContractualProduct().getEconomicTerms();
        String tradeIdentifier = cdmTrade.getTradeIdentifier().get(0).getAssignedIdentifier().get(0).getIdentifier().getValue().toString();

        ClearedSwapModel trade = new ClearedSwapModel();
        trade.dealId = tradeIdentifier;
        trade.tradeDate= cdmTrade.getTradeDate().getValue().toString();
        trade.fixedRatePayer = cdmTrade.getParty().get(1).getPartyId().get(0).getIdentifier().getValue().toString();
        trade.floatingRatePayer = cdmTrade.getParty().get(2).getPartyId().get(0).getIdentifier().getValue().toString();

        trade.fixedRate= cdmTrade.getTradableProduct().getProduct().getContractualProduct().getEconomicTerms().getPayout().getInterestRatePayout().get(0).getCashflowRepresentation().getPaymentCalculationPeriod().get(0).getCalculationPeriod().get(0).getFixedRate().toString();
        trade.floatingRateReference="";

        trade.maturityDate=cdmTrade.getTradableProduct().getProduct().getContractualProduct().getEconomicTerms().getTerminationDate().getAdjustableDate().getAdjustedDate().getValue().toString();
        trade.quantity=cdmTrade.getTradableProduct().getTradeLot().get(0).getPriceQuantity().get(0).getQuantity().get(0).getValue().getValue().toString();


        int row = rowToTrade.size();

        rowToTrade.put(row, trade);
        idToRow.put(trade.dealId, row);
        idToTrade.put(trade.dealId, trade);

        fireTableRowsInserted(row, row);
    }

    public void updateTrade(ClearedSwapModel trade, String id) {


    }

    public void replaceTrade(ClearedSwapModel clearedSwapModel, String originalID) {

        Integer row = idToRow.get(originalID);


        fireTableRowsUpdated(row, row);
    }

    public void addID(ClearedSwapModel trade, String newID) {
        idToTrade.put(newID, trade);
    }

    public ClearedSwapModel getTrade(String id) {
        return idToTrade.get(id);
    }

    public ClearedSwapModel getTrade(int row) {
        return rowToTrade.get(row);
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) { }

    public Class<String> getColumnClass(int columnIndex) {
        return String.class;
    }

    public int getRowCount() {
        return rowToTrade.size();
    }

    public int getColumnCount() {
        return headers.length;
    }

    public String getColumnName(int columnIndex) {
        return headers[columnIndex];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ClearedSwapModel trade = rowToTrade.get(rowIndex);
        switch (columnIndex) {
            case TRADE_ID_COL:
                return trade.getDealId();
            case TRADE_DATE_COL:
                return trade.getTradeDate();
            case PAYER_COL:
                return trade.getFixedRatePayer();
            case RECEIVER_COL:;
                return trade.getFloatingRatePayer();
            case AMOUNT_COL:
                return trade.getQuantity();
            case MATURITY_DATE_COL:
                return trade.getMaturityDate();
            case FIXED_RATE_COL:
                return trade.getFixedRate();
            case FLOATING_RATE_COL :
                return trade.getFloatingRateReference();
        }
        return "";
    }
}
