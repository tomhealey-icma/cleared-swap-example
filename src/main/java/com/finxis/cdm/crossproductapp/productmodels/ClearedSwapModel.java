package com.finxis.cdm.crossproductapp.productmodels;

import javax.swing.*;

public class ClearedSwapModel {

    //Firm
    public String firm;

    //TradeDate
    public String tradeDate;

    //Settlement Date
    public String settlementDate;

    //Effective Date
    public String effectiveDate;

    //Maturity Date
    public String maturityDate;

    //Open Close
    public String transactionType;

    //Customer Account
    public String customerAccount;

    //CCP
    public String centralCounterparty;

    //Deal ID
    public String dealId;

    //Clearing Broker
    public String clearingBroker;

    //Executing Broker
    public String executingBroker;

    public String commissions;

    public String closeOutIndicator;

    //Quantity
    public String quantity;

    //Settlement Amount
    public String settlementAmount;

    //Pay or Receive Payment
    public String paymentDirection;

    //Fixed Rate Payer
    public String fixedRatePayer;

    //Fixed Rate
    public String fixedRate;

    public String fixedRatePaymentDates;

    public String fixedRateDayCountFraction;

    //Floating Rate Payer
    public String floatingRatePayer;

    //Reference Rate
    public String floatingRateReference;

    //Maturity Type
    public String maturityType;

    //Spread
    public String spread;

    public String floatingRatePaymentDates;

    public String floatingRateDayCountFraction;

    public String payFrequency;

    public String floatingRatePayFreq;
    public String fixedRatePayFreq;

    public String dealReference;

    public String currency;


    public String getDealId(){
        return dealId;

    }

    public String getTradeDate(){
        return tradeDate;

    }
    public String getFixedRatePayer(){
        return fixedRatePayer;

    }
    public String getFloatingRatePayer(){
        return floatingRatePayer;

    }
    public String getQuantity(){
        return quantity;

    }
    public String getFixedRate(){
        return fixedRate;

    }
    public String getFloatingRateReference(){
        return floatingRateReference;

    }

    public String getMaturityDate(){
        return maturityDate;

    }

}
