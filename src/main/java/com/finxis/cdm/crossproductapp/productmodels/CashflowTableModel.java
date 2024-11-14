package com.finxis.cdm.crossproductapp.productmodels;

import cdm.event.common.BusinessEvent;
import cdm.product.template.EconomicTerms;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
public class CashflowTableModel extends AbstractTableModel {

    private final static int RECORD_ID= 0;
    private final static int DATE_COL= 1;
    private final static int PAY_COL = 2;
    private final static int RECEIVE_COL = 3;
    private final static int NET_COL = 4;

    private final HashMap<Integer, CashflowModel> rowToCashflow;
    private final HashMap<String, Integer> idToRow;
    private final HashMap<String, CashflowModel> idToCashflow;

    private final String[] headers;

    public CashflowTableModel() {
        rowToCashflow= new HashMap<>();
        idToRow = new HashMap<>();
        idToCashflow = new HashMap<>();

        headers = new String[]
                {"Record ID", "Date", "Pay Amount", "Receive Amount", "Net Amount"};
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) { }

    public Class<String> getColumnClass(int columnIndex) {
        return String.class;
    }

    public int getRowCount() {
        return rowToCashflow.size();
    }

    public int getColumnCount() {
        return headers.length;
    }

    public String getColumnName(int columnIndex) {
        return headers[columnIndex];
    }

    public void removeRow(int rowIndex){
        rowToCashflow.remove(rowIndex);
    }



    public Object getValueAt(int rowIndex, int columnIndex) {
        CashflowModel cashflow = rowToCashflow.get(rowIndex);
        switch (columnIndex) {
            case RECORD_ID:
                return cashflow.getCashflowId();
            case DATE_COL:
                return cashflow.getCashflowDate();
            case PAY_COL:
                return cashflow.getPayAmount();
            case RECEIVE_COL:
                return cashflow.getReceiveAmount();
            case NET_COL:
                return cashflow.getNetAmount();

        }
        return "";
    }

    public void clearTable(){

            rowToCashflow.clear();
            idToRow.clear();
            idToCashflow.clear();
    }

    public void addCashflows(CashflowModel cashflow) throws JsonProcessingException {

        int row = rowToCashflow.size();

        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime CfzonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        String CfformattedDateTimeString = CfzonedDateTime.format(formatter);

        cashflow.cashflowId = "CFID:" + row+CfformattedDateTimeString;
        rowToCashflow.put(row, cashflow);
        idToRow.put(cashflow.cashflowId, row);
        idToCashflow.put(cashflow.cashflowId, cashflow);

        fireTableRowsInserted(row, row);
        fireTableDataChanged();
    }

}
