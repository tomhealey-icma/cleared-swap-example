package com.finxis.cdm.crossproductapp.productmodels;

public class CashflowModel {

    public String cashflowId;
    public String cashflowDate;
    public String payAmount;
    public String receiveAmount;
    public String netAmount;


    public String getCashflowId() {
        return cashflowId;
    }

    public String getCashflowDate() {
        return cashflowDate;
    }
    public String getPayAmount() {
        return payAmount;
    }

    public String getReceiveAmount() {
        return receiveAmount;
    }

    public String getNetAmount() {
        return netAmount;
    }

    public void setCashflowId(String cashflowId) {
        this.cashflowId = cashflowId;
    }

    public void setCashflowDate(String cashflowDate) {
        this.cashflowDate = cashflowDate;
    }

    public void setPayAmount(String payAmount) {
        this.payAmount = payAmount;
    }

    public void setReceiveAmount(String receiveAmount) {
        this.receiveAmount = receiveAmount;
    }

    public void setNetAmount(String netAmount) {
        this.netAmount = netAmount;
    }
}
