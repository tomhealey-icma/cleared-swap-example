package com.finxis.cdm.crossproductapp.util;


import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

import cdm.event.workflow.Workflow;
import cdm.event.workflow.WorkflowStep;
import com.finxis.cdm.crossproductapp.productmodels.CdsCsvModel;
import com.finxis.cdm.crossproductapp.productmodels.ClearedCdsSwapModel;
import com.finxis.cdm.crossproductapp.productmodels.ClearedSwapModel;
import com.finxis.cdm.crossproductapp.productmodels.IrsCsvModel;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.opencsv.exceptions.CsvValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLCreators {
    // Protected Properties

    protected static DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    protected static DocumentBuilder domBuilder = null;

    static {
        try {
            domBuilder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    Document newDoc = domBuilder.newDocument();

    public XMLCreators() throws ParserConfigurationException {

        domFactory = DocumentBuilderFactory.newInstance();
        domBuilder = domFactory.newDocumentBuilder();

        Document newDoc = domBuilder.newDocument();

    }

    public Document convertFile(File csvFile, String xmlFileName,
                                char delimiter) {

        int rowsCount = -1;
        BufferedReader csvReader;
        try {

            //String rootStr = "CdmNewTradeWorkflow xmlns=\"urn:icma:xsd:ICMARepoNewTrade\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:icma:xsd:ICMARepoNewTrade icma-cdm-repo-trade.xsd\"";
            String rootStr = "CdmNewTradeWorkflow";

            Element rootElement = newDoc.createElement(rootStr);
            newDoc.appendChild(rootElement);
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "urn:icma:xsd:ICMARepoNewTrade");
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            rootElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "urn:icma:xsd:ICMARepoNewTrade icma-cdm-repo-trade.xsd");


            String xmlName = "CdmNewTradeWorkflow";
            // Read csv file
            csvReader = new BufferedReader(new FileReader(csvFile));

            //** Now using the OpenCSV **//
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(delimiter)
                    .build();

            CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile))
                    .withCSVParser(parser)
                    .build();


            //CSVReader reader = new CSVReader(csvReader);
            String[] nextLine;
            int line = 0;
            List<String> headers = new ArrayList<String>(5);
            while ((nextLine = reader.readNext()) != null) {

                if (line == 0) { // Header row
                    for (String col : nextLine) {
                        headers.add(col);
                    }
                } else { // Data row
                    //Element rowElement = newDoc.createElement("Workflow");
                    //rootElement.appendChild(rowElement);

                    int col = 0;
                    for (String value : nextLine) {
                        String header = headers.get(col).replaceAll("[\\t\\p{Zs}\\u0020]", "_");
                        //header = headers.get(col).replaceAll("[\\/]", "><");

                        if (header.contains("/")) {
                            String[] tokens = header.split("\\/");
                            System.out.println("Header:" + header);

                            addNode(rootElement, header, value);

                        }

                        col++;

                    }
                }
                line++;
            }
            //** End of CSV parsing**//

            FileWriter writer = null;

            try {

                writer = new FileWriter(new File(xmlFileName));


                TransformerFactory tranFactory = TransformerFactory.newInstance();
                Transformer aTransformer = tranFactory.newTransformer();
                aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
                aTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
                aTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                Source src = new DOMSource(newDoc);

                Result result = new StreamResult(writer);
                aTransformer.transform(src, result);
                System.out.println(src);

                writer.flush();

            } catch (Exception exp) {
                exp.printStackTrace();
            } finally {
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }

            // Output to console for testing
            // Resultt result = new StreamResult(System.out);

        } catch (IOException exp) {
            System.err.println(exp.toString());
        } catch (Exception exp) {
            System.err.println(exp.toString());
        }
        return newDoc;
        // "XLM Document has been created" + rowsCount;
    }


    public boolean addNode(Element parent, String nodeStr, String value) {

        NodeList nodeList = parent.getChildNodes();

        int nl1 = nodeList.getLength();
        Element newEl = null;

        String[] tokens = nodeStr.split("/");
        if (tokens.length <= 1) {
            parent.appendChild(newDoc.createTextNode(value.trim()));
            return true;
        }


        String fchild = tokens[1];


        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeName().equals(fchild)) {
                String newNodeStr = nodeStr.substring(nodeStr.indexOf("/") + 1, nodeStr.length());
                if (newNodeStr != null && newNodeStr != "")
                    return addNode((Element) nodeList.item(i), newNodeStr, value);
                else
                    nodeList.item(i).appendChild(newDoc.createTextNode(value.trim()));
            }

        }

        String newNodeStr = nodeStr.substring(nodeStr.indexOf("/") + 1, nodeStr.length());
        if (newNodeStr != null && newNodeStr != "") {
            newEl = newDoc.createElement(fchild);
            parent.appendChild(newEl);
            System.out.println("Added Node:" + fchild);
            return addNode(newEl, newNodeStr, value);
        } else {
            parent.appendChild(newDoc.createTextNode(value.trim()));
        }
        return false;
    }


    public CdmNewTradeWorkflow convertFileWithCsvMap(File csvFile, char delimiter) {

        int rowsCount = -1;
        BufferedReader csvReader;

        IcmaRepoUtil iruilt = new IcmaRepoUtil();



        try {


            // Read csv file
            csvReader = new BufferedReader(new FileReader(csvFile));

            //** Now using the OpenCSV **//
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(delimiter)
                    .build();

            CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile))
                    .withCSVParser(parser)
                    .build();

            Map<String, Integer> cdmNewTradeMap = new HashMap<>();
            CsvFieldMap csvFieldMap = new CsvFieldMap();
            csvFieldMap.buildNewTradeMap(cdmNewTradeMap);
            CsvMapper csvMapper = new CsvMapper();

            ArrayList<CsvField> csvMap = csvMapper.NewTradeWorkflow(reader);

            CdmNewTradeWorkflow cdmt = new CdmNewTradeWorkflow();

           String t = csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowName")).fieldValue;

           CdmNewTradeWorkflow.Workflow cdmWf = new CdmNewTradeWorkflow.Workflow();

           String test = csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/WorkflowStepName")).fieldValue;



            cdmWf.setWorkflowName(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowName")).fieldValue);
            CdmNewTradeWorkflow.Workflow.WorkflowStep wfs = new CdmNewTradeWorkflow.Workflow.WorkflowStep();
            wfs.setWorkflowStepName(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/WorkflowStepName")).fieldValue);

            wfs.setTransactionDate(iruilt.convertStringtoGrgorianCalendar(
                    csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/TransactionDate")).fieldValue));

            wfs.setPurchaseDate(iruilt.convertStringtoGrgorianCalendar(
                    csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/PurchaseDate")).fieldValue));

            wfs.setRepurchaseDate(iruilt.convertStringtoGrgorianCalendar(
                    csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/RepurchaseDate")).fieldValue));


            wfs.setTradeType(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/TradeType")).fieldValue);
            wfs.setTermType(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/TermType")).fieldValue);
            wfs.setRateType(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/RateType")).fieldValue);
            wfs.setDayCount(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/DayCount")).fieldValue);


            CdmNewTradeWorkflow.Workflow.WorkflowStep.Buyer buyer = new CdmNewTradeWorkflow.Workflow.WorkflowStep.Buyer();
            buyer.setLEI(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Buyer/LEI")).fieldValue);
            buyer.setLEIName(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Buyer/LEIName")).fieldValue);

            CdmNewTradeWorkflow.Workflow.WorkflowStep.Seller seller = new CdmNewTradeWorkflow.Workflow.WorkflowStep.Seller();
            seller.setLEI(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Seller/LEI")).fieldValue);
            seller.setLEIName(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Seller/LEIName")).fieldValue);

            CdmNewTradeWorkflow.Workflow.WorkflowStep.TradeIdentifier tradeId = new CdmNewTradeWorkflow.Workflow.WorkflowStep.TradeIdentifier();
            tradeId.setUTI(Integer.parseInt(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/TradeIdentifier/UTI")).fieldValue));
            wfs.setTradeIdentifier(tradeId);


            CdmNewTradeWorkflow.Workflow.WorkflowStep.CollateralList collList = new CdmNewTradeWorkflow.Workflow.WorkflowStep.CollateralList();

            CdmNewTradeWorkflow.Workflow.WorkflowStep.CollateralList.Collateral coll = new CdmNewTradeWorkflow.Workflow.WorkflowStep.CollateralList.Collateral();
            coll.setDescription(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/Description")).fieldValue);
            coll.setISIN(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/ISIN")).fieldValue);
            coll.setCurrency(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/Currency")).fieldValue);
            coll.setNominalAmount(Integer.parseInt(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/NominalPerUnitAmount")).fieldValue));
            coll.setDayCount(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/DayCount")).fieldValue);
            coll.setInterestRate(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/InterestRate")).fieldValue)));
            coll.setInterestPaymentFrequency(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/InterestPaymentFrequency")).fieldValue);
            coll.setNominalAmount(Integer.parseInt(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/NominalAmount")).fieldValue));
            coll.setCleanPrice(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/CleanPrice")).fieldValue)));
            coll.setAccruedInterestPrice(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/AccruedInterestPrice")).fieldValue)));
            coll.setAccruedInterestAmount(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/AccruedInterestAmount")).fieldValue)));
            coll.setDirtyPrice(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/DirtyPrice")).fieldValue)));
            coll.setUnAdjustedSettlementValue(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/UnAdjustedSettlementValue")).fieldValue)));
            coll.setMargin(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/Margin")).fieldValue)));
            coll.setAdjustedSettlementValue(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/AdjustedSettlementValue")).fieldValue)));
            coll.setHaircut(Short.valueOf(String.valueOf(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/Haircut")).fieldValue)));
            coll.setSettlementAmount(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/CollateralList/Collateral/SettlementAmount")).fieldValue)));

            CdmNewTradeWorkflow.Workflow.WorkflowStep.Loan loan = new CdmNewTradeWorkflow.Workflow.WorkflowStep.Loan();
            loan.setLoanAmount(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Loan/LoanAmount")).fieldValue)));
            loan.setLoanCurrency(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Loan/LoanCurrency")).fieldValue);

            CdmNewTradeWorkflow.Workflow.WorkflowStep.RepoRate repoRate= new CdmNewTradeWorkflow.Workflow.WorkflowStep.RepoRate();

            CdmNewTradeWorkflow.Workflow.WorkflowStep.RepoRate.FixedRate fixedRepoRate = new CdmNewTradeWorkflow.Workflow.WorkflowStep.RepoRate.FixedRate();
            fixedRepoRate.setValue(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/RepoRate/FixedRate/Value")).fieldValue)));


            CdmNewTradeWorkflow.Workflow.WorkflowStep.RepoRate.Floating floatingRepoRate = new CdmNewTradeWorkflow.Workflow.WorkflowStep.RepoRate.Floating();
            floatingRepoRate.setFloatingRateIndex(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/RepoRate/Floating/FloatingRateIndex")).fieldValue);
            floatingRepoRate.setFloatingRateIndexFreq(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/RepoRate/Floating/FloatingRateIndexFreq")).fieldValue)));
            floatingRepoRate.setFloatingRateIndexRate(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/RepoRate/Floating/FloatingRateIndexRate")).fieldValue)));
            floatingRepoRate.setFloatingRateIndexSpread(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/RepoRate/Floating/FloatingRateIndexSpread")).fieldValue)));

            wfs.setPurchasePrice(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/PurchasePrice")).fieldValue)));
            wfs.setProjectedInterest(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/ProjectedInterest")).fieldValue)));
            wfs.setRepurchasePrice(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/RepurchasePrice")).fieldValue)));
            wfs.setDeliveryMethod(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/DeliveryMethod")).fieldValue);


            CdmNewTradeWorkflow.Workflow.WorkflowStep.Optionality optionality = new CdmNewTradeWorkflow.Workflow.WorkflowStep.Optionality();
            optionality.setTerminationOnDemand(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Optionality/TerminationOnDemand")).fieldValue);
            optionality.setNoticePeriod(Short.valueOf(String.valueOf(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Optionality/NoticePeriod")).fieldValue)));

            CdmNewTradeWorkflow.Workflow.WorkflowStep.Substitution substitution = new CdmNewTradeWorkflow.Workflow.WorkflowStep.Substitution();
            substitution.setAllowed(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Substitution/Allowed")).fieldValue);
            substitution.setNumberAllowed(Short.valueOf(String.valueOf(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Substitution/NumberAllowed")).fieldValue)));

            CdmNewTradeWorkflow.Workflow.WorkflowStep.Agreement agreement = new CdmNewTradeWorkflow.Workflow.WorkflowStep.Agreement();
            agreement.setAgreementName(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Agreement/AgreementName")).fieldValue);
            agreement.setAgreementIssuer(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Agreement/AgreementIssuer")).fieldValue);
            agreement.setAgreementGoverningLaw(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Agreement/AgreementGoverningLaw")).fieldValue);
            agreement.setAgreementDate(Integer.parseInt(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Agreement/AgreementDate")).fieldValue));

            agreement.setAgreementVersion(Integer.parseInt(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Agreement/AgreementVersion")).fieldValue));

            agreement.setAgreementIdentifier(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Agreement/AgreementIdentifier")).fieldValue);
            agreement.setAgreementEffectiveDate(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Agreement/AgreementEffectiveDate")).fieldValue);
            agreement.setAgreementUrl(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/Agreement/AgreementUrl")).fieldValue);

            CdmNewTradeWorkflow.Workflow.WorkflowStep.InitialMargin initialMargin = new CdmNewTradeWorkflow.Workflow.WorkflowStep.InitialMargin();
            initialMargin.setValue(BigDecimal.valueOf(iruilt.doubleParseStr(csvMap.get(cdmNewTradeMap.get("CdmNewTradeWorkflow/Workflow/WorkflowStep/InitialMargin/Value")).fieldValue)));


            cdmt.setWorkflow(cdmWf);
            cdmWf.setWorkflowStep(wfs);
            wfs.setBuyer(buyer);
            wfs.setSeller(seller);
            collList.setCollateral(coll);
            wfs.setCollateralList(collList);
            wfs.setLoan(loan);
            repoRate.setFixedRate(fixedRepoRate);
            repoRate.setFloating(floatingRepoRate);
            wfs.setRepoRate(repoRate);
            wfs.setOptionality(optionality);
            wfs.setSubstitution(substitution);
            wfs.setAgreement(agreement);


            //** End of CSV parsing**//
            return cdmt;

        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }


    }

    public IrsCsvModel convertIrsFileWithCsvMap(File csvFile, String xmlFileName,
                                             char delimiter) throws FileNotFoundException {

        int rowsCount = -1;
        BufferedReader csvReader;

        IcmaRepoUtil iruilt = new IcmaRepoUtil();
        Map<String, Integer> cdmNewTradeMap = new HashMap<>();
        CsvMapper csvMapper = new CsvMapper();
        ArrayList<CsvField> csvMap = null;
        try {


            // Read csv file
            csvReader = new BufferedReader(new FileReader(csvFile));

            //** Now using the OpenCSV **//
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(delimiter)
                    .build();

            CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile))
                    .withCSVParser(parser)
                    .build();


            CsvFieldMap csvFieldMap = new CsvFieldMap();
            csvFieldMap.buildNewIrsTradeMap(cdmNewTradeMap);
            csvMap = csvMapper.NewTradeWorkflow(reader);


        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        IrsCsvModel irsCsvModel = new IrsCsvModel();
        irsCsvModel.setIAS_ID(csvMap.get(cdmNewTradeMap.get("IAS_ID")).fieldValue);
        irsCsvModel.setIAS_ID2(csvMap.get(cdmNewTradeMap.get("IAS_ID2")).fieldValue);
        irsCsvModel.setClient_ID(csvMap.get(cdmNewTradeMap.get("Client_ID")).fieldValue);
        irsCsvModel.setIM_ID(csvMap.get(cdmNewTradeMap.get("IM_ID")).fieldValue);
        irsCsvModel.setCP_ID(csvMap.get(cdmNewTradeMap.get("CP_ID")).fieldValue);
        irsCsvModel.setCounterparty_Name(csvMap.get(cdmNewTradeMap.get("Counterparty_Name")).fieldValue);
        irsCsvModel.setOPTION_TYPE(csvMap.get(cdmNewTradeMap.get("OPTION_TYPE")).fieldValue);
        irsCsvModel.setTD(csvMap.get(cdmNewTradeMap.get("TD")).fieldValue);
        irsCsvModel.setSD(csvMap.get(cdmNewTradeMap.get("SD")).fieldValue);
        irsCsvModel.setValuation_Settlement_Currency(csvMap.get(cdmNewTradeMap.get("Valuation/Settlement_Currency")).fieldValue);
        irsCsvModel.setOffset_Currency(csvMap.get(cdmNewTradeMap.get("Offset_Currency")).fieldValue);
        irsCsvModel.setCost_Premium(csvMap.get(cdmNewTradeMap.get("Cost/Premium")).fieldValue);
        irsCsvModel.setEXPD(csvMap.get(cdmNewTradeMap.get("EXPD")).fieldValue);
        irsCsvModel.setSTRIKE(csvMap.get(cdmNewTradeMap.get("STRIKE")).fieldValue);
        irsCsvModel.setPAYER_RECEIVER(csvMap.get(cdmNewTradeMap.get("PAYER/RECEIVER")).fieldValue);
        irsCsvModel.setContract_Size(csvMap.get(cdmNewTradeMap.get("Contract_Size")).fieldValue);
        irsCsvModel.setSwap_Type(csvMap.get(cdmNewTradeMap.get("Swap_Type")).fieldValue);
        irsCsvModel.setED(csvMap.get(cdmNewTradeMap.get("ED")).fieldValue);
        irsCsvModel.setMD(csvMap.get(cdmNewTradeMap.get("MD")).fieldValue);
        irsCsvModel.setPay_Leg_Identifier(csvMap.get(cdmNewTradeMap.get("Pay_Leg_Identifier")).fieldValue);
        irsCsvModel.setPAY_Leg_Notional(csvMap.get(cdmNewTradeMap.get("PAY_Leg_Notional")).fieldValue);
        irsCsvModel.setPayNotionalExchange(csvMap.get(cdmNewTradeMap.get("PayNotionalExchange")).fieldValue);
        irsCsvModel.setPay_Leg_Currency(csvMap.get(cdmNewTradeMap.get("Pay_Leg_Currency")).fieldValue);
        irsCsvModel.setPAY_Leg_Type(csvMap.get(cdmNewTradeMap.get("PAY_Leg_Type")).fieldValue);
        irsCsvModel.setPay_CPN_Cycle(csvMap.get(cdmNewTradeMap.get("Pay_CPN_Cycle")).fieldValue);
        irsCsvModel.setPay_Reset_Frequency(csvMap.get(cdmNewTradeMap.get("Pay_Reset_Frequency")).fieldValue);
        irsCsvModel.setPay_Index(csvMap.get(cdmNewTradeMap.get("Pay_Index")).fieldValue);
        irsCsvModel.setPay_Reset_Lag(csvMap.get(cdmNewTradeMap.get("Pay_Reset_Lag")).fieldValue);
        irsCsvModel.setPay_Spread(csvMap.get(cdmNewTradeMap.get("Pay_Spread")).fieldValue);
        irsCsvModel.setPay_Initial_Index_Rate(csvMap.get(cdmNewTradeMap.get("Pay_Initial_Index_Rate")).fieldValue);
        irsCsvModel.setPay_Fixed_Rate(csvMap.get(cdmNewTradeMap.get("Pay_Fixed_Rate")).fieldValue);
        irsCsvModel.setPay_First_PD(csvMap.get(cdmNewTradeMap.get("Pay_First_PD")).fieldValue);
        irsCsvModel.setPay_Roll_Date(csvMap.get(cdmNewTradeMap.get("Pay_Roll_Date")).fieldValue);
        irsCsvModel.setPay_Bus_Convention(csvMap.get(cdmNewTradeMap.get("Pay_Bus_Convention")).fieldValue);
        irsCsvModel.setPay_Accrual_Code(csvMap.get(cdmNewTradeMap.get("Pay_Accrual_Code")).fieldValue);
        irsCsvModel.setPay_Hol_Cal1(csvMap.get(cdmNewTradeMap.get("Pay_Hol_Cal1")).fieldValue);
        irsCsvModel.setPay_Hol_Cal2(csvMap.get(cdmNewTradeMap.get("Pay_Hol_Cal2")).fieldValue);
        irsCsvModel.setPay_Compounding(csvMap.get(cdmNewTradeMap.get("Pay_Compounding")).fieldValue);
        irsCsvModel.setPay_Leg_Arrears(csvMap.get(cdmNewTradeMap.get("Pay_Leg_Arrears")).fieldValue);
        irsCsvModel.setPay_Leg_UnderlyingID(csvMap.get(cdmNewTradeMap.get("Pay_Leg_UnderlyingID")).fieldValue);
        irsCsvModel.setPay_Leg_UnderlyingName(csvMap.get(cdmNewTradeMap.get("Pay_Leg_UnderlyingName")).fieldValue);
        irsCsvModel.setPay_Leg_Recovery_Rate(csvMap.get(cdmNewTradeMap.get("Pay_Leg_Recovery_Rate")).fieldValue);
        irsCsvModel.setPay_Leg_Other_Calculation(csvMap.get(cdmNewTradeMap.get("Pay_Leg_Other_Calculation(formula_for_calculation)")).fieldValue);
        irsCsvModel.setPayCurFixingRate(csvMap.get(cdmNewTradeMap.get("PayCurFixingRate")).fieldValue);
        irsCsvModel.setPayCurFixingDate(csvMap.get(cdmNewTradeMap.get("PayCurFixingDate")).fieldValue);
        irsCsvModel.setREC_Leg_Identifier(csvMap.get(cdmNewTradeMap.get("REC_Leg_Identifier")).fieldValue);
        irsCsvModel.setREC_Leg_Notional(csvMap.get(cdmNewTradeMap.get("REC_Leg_Notional")).fieldValue);
        irsCsvModel.setRECNotionalExchange(csvMap.get(cdmNewTradeMap.get("RECNotionalExchange")).fieldValue);
        irsCsvModel.setREC_Leg_Currency(csvMap.get(cdmNewTradeMap.get("REC_Leg_Currency")).fieldValue);
        irsCsvModel.setREC_Leg_Type(csvMap.get(cdmNewTradeMap.get("REC_Leg_Type")).fieldValue);
        irsCsvModel.setREC_CPN_Cycle(csvMap.get(cdmNewTradeMap.get("REC_CPN_Cycle")).fieldValue);
        irsCsvModel.setREC_Reset_Frequency(csvMap.get(cdmNewTradeMap.get("REC_Reset_Frequency")).fieldValue);
        irsCsvModel.setREC_Index(csvMap.get(cdmNewTradeMap.get("REC_Index")).fieldValue);
        irsCsvModel.setRECIndexIDType(csvMap.get(cdmNewTradeMap.get("RECIndexIDType")).fieldValue);
        irsCsvModel.setREC_Reset_Lag(csvMap.get(cdmNewTradeMap.get("REC_Reset_Lag")).fieldValue);
        irsCsvModel.setREC_Spread(csvMap.get(cdmNewTradeMap.get("REC_Spread")).fieldValue);
        irsCsvModel.setREC_Initial_Index_Rate(csvMap.get(cdmNewTradeMap.get("REC_Initial_Index_Rate")).fieldValue);
        irsCsvModel.setREC_Fixed_Rate(csvMap.get(cdmNewTradeMap.get("REC_Fixed_Rate")).fieldValue);
        irsCsvModel.setREC_First_PD(csvMap.get(cdmNewTradeMap.get("REC_First_PD")).fieldValue);
        irsCsvModel.setREC_Roll_Date(csvMap.get(cdmNewTradeMap.get("REC_Roll_Date")).fieldValue);
        irsCsvModel.setREC_Bus_Convention(csvMap.get(cdmNewTradeMap.get("REC_Bus_Convention")).fieldValue);
        irsCsvModel.setREC_Accrual_Code(csvMap.get(cdmNewTradeMap.get("REC_Accrual_Code")).fieldValue);
        irsCsvModel.setREC_Hol_Cal1(csvMap.get(cdmNewTradeMap.get("REC_Hol_Cal1")).fieldValue);
        irsCsvModel.setREC_Hol_Cal2(csvMap.get(cdmNewTradeMap.get("REC_Hol_Cal2")).fieldValue);
        irsCsvModel.setREC_Compounding(csvMap.get(cdmNewTradeMap.get("REC_Compounding")).fieldValue);
        irsCsvModel.setREC_Compounding_Freq(csvMap.get(cdmNewTradeMap.get("REC_Compounding_Freq")).fieldValue);
        irsCsvModel.setREC_Leg_Arrears(csvMap.get(cdmNewTradeMap.get("REC_Leg_Arrears")).fieldValue);
        irsCsvModel.setREC_Leg_UnderlyingID(csvMap.get(cdmNewTradeMap.get("REC_Leg_UnderlyingID")).fieldValue);
        irsCsvModel.setREC_Leg_UnderlyingName(csvMap.get(cdmNewTradeMap.get("REC_Leg_UnderlyingName")).fieldValue);
        irsCsvModel.setREC_Leg_Recovery_Rate(csvMap.get(cdmNewTradeMap.get("REC_Leg_Recovery_Rate")).fieldValue);
        irsCsvModel.setREC_Leg_Other_Calculation(csvMap.get(cdmNewTradeMap.get("REC_Leg_Other_Calculation(formula_for_calculation)")).fieldValue);
        irsCsvModel.setRECCurFixingRate(csvMap.get(cdmNewTradeMap.get("RECCurFixingRate")).fieldValue);
        irsCsvModel.setRECCurFixingDate(csvMap.get(cdmNewTradeMap.get("RECCurFixingDate")).fieldValue);
        irsCsvModel.setNotional(csvMap.get(cdmNewTradeMap.get("Notional")).fieldValue);
        irsCsvModel.setCurrency_Code(csvMap.get(cdmNewTradeMap.get("Currency_Code")).fieldValue);
        irsCsvModel.setCredit_Protection_Direction(csvMap.get(cdmNewTradeMap.get("Credit_Protection_Direction")).fieldValue);
        irsCsvModel.setReference_Obligation(csvMap.get(cdmNewTradeMap.get("Reference_Obligation")).fieldValue);
        irsCsvModel.setRefOblIDType(csvMap.get(cdmNewTradeMap.get("RefOblIDType")).fieldValue);
        irsCsvModel.setISIN(csvMap.get(cdmNewTradeMap.get("ISIN")).fieldValue);
        irsCsvModel.setCPN_Type(csvMap.get(cdmNewTradeMap.get("CPN_Type")).fieldValue);
        irsCsvModel.setCPN_Cycle(csvMap.get(cdmNewTradeMap.get("CPN_Cycle")).fieldValue);
        irsCsvModel.setFixed_Rate(csvMap.get(cdmNewTradeMap.get("Fixed_Rate")).fieldValue);
        irsCsvModel.setFirst_PD(csvMap.get(cdmNewTradeMap.get("First_PD")).fieldValue);
        irsCsvModel.setBus_Convention(csvMap.get(cdmNewTradeMap.get("Bus_Convention")).fieldValue);
        irsCsvModel.setAccrual_Code(csvMap.get(cdmNewTradeMap.get("Accrual_Code")).fieldValue);
        irsCsvModel.setHol_Cal1(csvMap.get(cdmNewTradeMap.get("Hol_Cal1")).fieldValue);
        irsCsvModel.setHol_Cal2(csvMap.get(cdmNewTradeMap.get("Hol_Cal2")).fieldValue);
        irsCsvModel.setCompoundingStartDate(csvMap.get(cdmNewTradeMap.get("CompoundingStartDate")).fieldValue);
        irsCsvModel.setCompoundingEndDate(csvMap.get(cdmNewTradeMap.get("CompoundingEndDate")).fieldValue);
        irsCsvModel.setCompoundingFreqID(csvMap.get(cdmNewTradeMap.get("CompoundingFreqID")).fieldValue);
        irsCsvModel.setArrears(csvMap.get(cdmNewTradeMap.get("Arrears")).fieldValue);
        irsCsvModel.setDateAdded(csvMap.get(cdmNewTradeMap.get("DateAdded")).fieldValue);
        irsCsvModel.setDateAmended(csvMap.get(cdmNewTradeMap.get("DateAmended")).fieldValue);
        irsCsvModel.setOPTION_UNDERLYING(csvMap.get(cdmNewTradeMap.get("OPTION_UNDERLYING")).fieldValue);
        irsCsvModel.setAttachmentPoint(csvMap.get(cdmNewTradeMap.get("AttachmentPoint")).fieldValue);
        irsCsvModel.setExhaustionPoint(csvMap.get(cdmNewTradeMap.get("ExhaustionPoint")).fieldValue);
        irsCsvModel.setROID(csvMap.get(cdmNewTradeMap.get("ROID")).fieldValue);
        irsCsvModel.setInflation_Index_Fixing_Name(csvMap.get(cdmNewTradeMap.get("Inflation_Index_Fixing_Name")).fieldValue);
        irsCsvModel.setInflation_Index_Fixing_Lag(csvMap.get(cdmNewTradeMap.get("Inflation_Index_Fixing_Lag")).fieldValue);
        irsCsvModel.setTier(csvMap.get(cdmNewTradeMap.get("Tier")).fieldValue);
        irsCsvModel.setDocument_Clause(csvMap.get(cdmNewTradeMap.get("Document_Clause")).fieldValue);
        irsCsvModel.setSummit_Trade_ID(csvMap.get(cdmNewTradeMap.get("Summit_Trade_ID")).fieldValue);
        irsCsvModel.setDPX_Unique_Pricing_ID(csvMap.get(cdmNewTradeMap.get("DPX_Unique_Pricing_ID")).fieldValue);
        irsCsvModel.setPay_TextField1(csvMap.get(cdmNewTradeMap.get("Pay_TextField1")).fieldValue);
        irsCsvModel.setPay_TextField2(csvMap.get(cdmNewTradeMap.get("Pay_TextField2")).fieldValue);
        irsCsvModel.setPay_TextField3(csvMap.get(cdmNewTradeMap.get("Pay_TextField3")).fieldValue);
        irsCsvModel.setPay_TextField4(csvMap.get(cdmNewTradeMap.get("Pay_TextField4")).fieldValue);
        irsCsvModel.setPay_TextField5(csvMap.get(cdmNewTradeMap.get("Pay_TextField5")).fieldValue);
        irsCsvModel.setRec_TextField1(csvMap.get(cdmNewTradeMap.get("Rec_TextField1")).fieldValue);
        irsCsvModel.setRec_TextField2(csvMap.get(cdmNewTradeMap.get("Rec_TextField2")).fieldValue);
        irsCsvModel.setRec_TextField3(csvMap.get(cdmNewTradeMap.get("Rec_TextField3")).fieldValue);
        irsCsvModel.setRec_TextField4(csvMap.get(cdmNewTradeMap.get("Rec_TextField4")).fieldValue);
        irsCsvModel.setRec_TextField5(csvMap.get(cdmNewTradeMap.get("Rec_TextField5")).fieldValue);
        irsCsvModel.setBarrier_Lower(csvMap.get(cdmNewTradeMap.get("Barrier_Lower")).fieldValue);
        irsCsvModel.setBarrier_Upper(csvMap.get(cdmNewTradeMap.get("Barrier_Upper")).fieldValue);
        irsCsvModel.setSubOption_Type(csvMap.get(cdmNewTradeMap.get("SubOption_Type")).fieldValue);
        irsCsvModel.setBarrier_Frequency(csvMap.get(cdmNewTradeMap.get("Barrier_Frequency")).fieldValue);
        irsCsvModel.setCPRMLevel(csvMap.get(cdmNewTradeMap.get("CPRMLevel")).fieldValue);
        irsCsvModel.setPrimarySource(csvMap.get(cdmNewTradeMap.get("PrimarySource")).fieldValue);
        irsCsvModel.setPrimaryPriceType(csvMap.get(cdmNewTradeMap.get("PrimaryPriceType")).fieldValue);
        irsCsvModel.setSecondarySource(csvMap.get(cdmNewTradeMap.get("SecondarySource")).fieldValue);
        irsCsvModel.setSecondaryPriceType(csvMap.get(cdmNewTradeMap.get("SecondaryPriceType")).fieldValue);
        irsCsvModel.setTeritarySource(csvMap.get(cdmNewTradeMap.get("TeritarySource")).fieldValue);
        irsCsvModel.setTeritaryPriceType(csvMap.get(cdmNewTradeMap.get("TeritaryPriceType")).fieldValue);
        irsCsvModel.setBrokerSecID(csvMap.get(cdmNewTradeMap.get("BrokerSecID")).fieldValue);
        irsCsvModel.setUSI(csvMap.get(cdmNewTradeMap.get("USI")).fieldValue);
        irsCsvModel.setPay_Management_Fee(csvMap.get(cdmNewTradeMap.get("Pay_Management_Fee")).fieldValue);
        irsCsvModel.setRec_Management_Fee(csvMap.get(cdmNewTradeMap.get("Rec_Management_Fee")).fieldValue);
        irsCsvModel.setPosition(csvMap.get(cdmNewTradeMap.get("Position")).fieldValue);
        irsCsvModel.setContract_Rate(csvMap.get(cdmNewTradeMap.get("Contract_Rate")).fieldValue);
        irsCsvModel.setBase_Currency(csvMap.get(cdmNewTradeMap.get("Base_Currency")).fieldValue);
        irsCsvModel.setQuote_Currency(csvMap.get(cdmNewTradeMap.get("Quote_Currency")).fieldValue);
        irsCsvModel.setSettlement_Date(csvMap.get(cdmNewTradeMap.get("Settlement_Date")).fieldValue);
        irsCsvModel.setDelivery_Date(csvMap.get(cdmNewTradeMap.get("Delivery_Date")).fieldValue);
        irsCsvModel.setReference_Obligation_Desc(csvMap.get(cdmNewTradeMap.get("Reference_Obligation_Desc")).fieldValue);
        irsCsvModel.setTrigger_Direction(csvMap.get(cdmNewTradeMap.get("Trigger_Direction")).fieldValue);
        irsCsvModel.setLong_Accounting_System_ID(csvMap.get(cdmNewTradeMap.get("Long_Accounting_System_ID")).fieldValue);
        irsCsvModel.setShort_Accounting_System_ID(csvMap.get(cdmNewTradeMap.get("Short_Accounting_System_ID")).fieldValue);
        irsCsvModel.setAdjusted_MD(csvMap.get(cdmNewTradeMap.get("Adjusted_MD")).fieldValue);
        irsCsvModel.setDiscounting(csvMap.get(cdmNewTradeMap.get("Discounting")).fieldValue);
        irsCsvModel.setPay_Lookback(csvMap.get(cdmNewTradeMap.get("Pay_Lookback")).fieldValue);
        irsCsvModel.setRec_Lookback(csvMap.get(cdmNewTradeMap.get("Rec_Lookback")).fieldValue);
        irsCsvModel.setPay_Lockout(csvMap.get(cdmNewTradeMap.get("Pay_Lockout")).fieldValue);
        irsCsvModel.setRec_Lockout(csvMap.get(cdmNewTradeMap.get("Rec_Lockout")).fieldValue);
        irsCsvModel.setPay_Payment_Delay(csvMap.get(cdmNewTradeMap.get("Pay_Payment_Delay")).fieldValue);
        irsCsvModel.setRec_Payment_Delay(csvMap.get(cdmNewTradeMap.get("Rec_Payment_Delay")).fieldValue);
        irsCsvModel.setMOSID(csvMap.get(cdmNewTradeMap.get("MOSID")).fieldValue);
        irsCsvModel.setPay_Compounding_Freq(csvMap.get(cdmNewTradeMap.get("Pay_Compounding_Freq")).fieldValue);
        irsCsvModel.setPay_Compounding_Method(csvMap.get(cdmNewTradeMap.get("Pay_Compounding_Method")).fieldValue);
        irsCsvModel.setRec_Compounding_Method(csvMap.get(cdmNewTradeMap.get("Rec_Compounding_Method")).fieldValue);
        irsCsvModel.setPay_Observation_Shift(csvMap.get(cdmNewTradeMap.get("Pay_Observation_Shift")).fieldValue);
        irsCsvModel.setRec_Observation_Shift(csvMap.get(cdmNewTradeMap.get("Rec_Observation_Shift")).fieldValue);
        irsCsvModel.setUnderlying_ID_Type(csvMap.get(cdmNewTradeMap.get("Underlying_ID_Type")).fieldValue);
        irsCsvModel.setSettlement_Type(csvMap.get(cdmNewTradeMap.get("Settlement_Type")).fieldValue);
        irsCsvModel.setRepo_Rate(csvMap.get(cdmNewTradeMap.get("Repo_Rate")).fieldValue);
        irsCsvModel.setPAY_ZeroCouponRatio(csvMap.get(cdmNewTradeMap.get("PAY_ZeroCouponRatio")).fieldValue);
        irsCsvModel.setREC_ZeroCouponRatio(csvMap.get(cdmNewTradeMap.get("REC_ZeroCouponRatio")).fieldValue);
        irsCsvModel.setPAY_CrossCurrRatio(csvMap.get(cdmNewTradeMap.get("PAY_CrossCurrRatio")).fieldValue);
        irsCsvModel.setREC_CrossCurrRatio(csvMap.get(cdmNewTradeMap.get("REC_CrossCurrRatio")).fieldValue);
        irsCsvModel.setIsPriced_Flag(csvMap.get(cdmNewTradeMap.get("IsPriced_Flag")).fieldValue);
        irsCsvModel.setStrike_Type(csvMap.get(cdmNewTradeMap.get("Strike_Type")).fieldValue);
        irsCsvModel.setPremium_Ratio(csvMap.get(cdmNewTradeMap.get("Premium_Ratio")).fieldValue);
        irsCsvModel.setPremium_Payment_Date(csvMap.get(cdmNewTradeMap.get("Premium_Payment_Date")).fieldValue);
        irsCsvModel.setAmortized_Swap(csvMap.get(cdmNewTradeMap.get("Amortized_Swap")).fieldValue);
        irsCsvModel.setClearing_House_Code(csvMap.get(cdmNewTradeMap.get("Clearing_House_Code")).fieldValue);
        irsCsvModel.setDPX_Clearing_House_Key(csvMap.get(cdmNewTradeMap.get("DPX_Clearing_House_Key")).fieldValue);
        irsCsvModel.setClearing_House_Key(csvMap.get(cdmNewTradeMap.get("Clearing_House_Key")).fieldValue);
        irsCsvModel.setLCH_Matched_Trade_Ref(csvMap.get(cdmNewTradeMap.get("LCH_Matched_Trade_Ref")).fieldValue);
        irsCsvModel.setDeflation_Floor(csvMap.get(cdmNewTradeMap.get("Deflation_Floor")).fieldValue);
        irsCsvModel.setBond_Start_Date(csvMap.get(cdmNewTradeMap.get("Bond_Start_Date")).fieldValue);
        irsCsvModel.setRatio_Return(csvMap.get(cdmNewTradeMap.get("Ratio_Return")).fieldValue);
        irsCsvModel.setInflation_Index_Fixing_Interpolation(csvMap.get(cdmNewTradeMap.get("Inflation_Index_Fixing_Interpolation")).fieldValue);
        irsCsvModel.setInflation_Index_Fixing_Rate(csvMap.get(cdmNewTradeMap.get("Inflation_Index_Fixing_Rate")).fieldValue);
        irsCsvModel.setAnchored(csvMap.get(cdmNewTradeMap.get("Anchored")).fieldValue);
        irsCsvModel.setLPI_Floor(csvMap.get(cdmNewTradeMap.get("LPI_Floor")).fieldValue);
        irsCsvModel.setLPI_Cap(csvMap.get(cdmNewTradeMap.get("LPI_Cap")).fieldValue);


        return irsCsvModel;

    }

    public CdsCsvModel convertCdsFileWithCsvMap(File csvFile, String xmlFileName,
                                                char delimiter) throws FileNotFoundException {

        int rowsCount = -1;
        BufferedReader csvReader;

        IcmaRepoUtil iruilt = new IcmaRepoUtil();
        Map<String, Integer> cdmNewTradeMap = new HashMap<>();
        CsvMapper csvMapper = new CsvMapper();
        ArrayList<CsvField> csvMap = null;
        try {


            // Read csv file
            csvReader = new BufferedReader(new FileReader(csvFile));

            //** Now using the OpenCSV **//
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(delimiter)
                    .build();

            CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile))
                    .withCSVParser(parser)
                    .build();


            CsvFieldMap csvFieldMap = new CsvFieldMap();
            csvFieldMap.buildNewCdsTradeMap(cdmNewTradeMap);
            csvMap = csvMapper.NewTradeWorkflow(reader);


        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CdsCsvModel cdsCsvModel = new CdsCsvModel();
        cdsCsvModel.setIAS_ID(csvMap.get(cdmNewTradeMap.get("IAS_ID")).fieldValue);
        cdsCsvModel.setIAS_ID2(csvMap.get(cdmNewTradeMap.get("IAS_ID2")).fieldValue);
        cdsCsvModel.setClient_ID(csvMap.get(cdmNewTradeMap.get("Client_ID")).fieldValue);
        cdsCsvModel.setIM_ID(csvMap.get(cdmNewTradeMap.get("IM_ID")).fieldValue);
        cdsCsvModel.setCP_ID(csvMap.get(cdmNewTradeMap.get("CP_ID")).fieldValue);
        cdsCsvModel.setCounterparty_Name(csvMap.get(cdmNewTradeMap.get("Counterparty_Name")).fieldValue);
        cdsCsvModel.setOPTION_TYPE(csvMap.get(cdmNewTradeMap.get("OPTION_TYPE")).fieldValue);
        cdsCsvModel.setTD(csvMap.get(cdmNewTradeMap.get("TD")).fieldValue);
        cdsCsvModel.setSD(csvMap.get(cdmNewTradeMap.get("SD")).fieldValue);
        cdsCsvModel.setValuation_Settlement_Currency(csvMap.get(cdmNewTradeMap.get("Valuation_Settlement_Currency")).fieldValue);
        cdsCsvModel.setOffset_Currency(csvMap.get(cdmNewTradeMap.get("Offset_Currency")).fieldValue);
        cdsCsvModel.setCost_Premium(csvMap.get(cdmNewTradeMap.get("Cost_Premium")).fieldValue);
        cdsCsvModel.setEXPD(csvMap.get(cdmNewTradeMap.get("EXPD")).fieldValue);
        cdsCsvModel.setSTRIKE(csvMap.get(cdmNewTradeMap.get("STRIKE")).fieldValue);
        cdsCsvModel.setPAYER_RECEIVER(csvMap.get(cdmNewTradeMap.get("PAYER_RECEIVER")).fieldValue);
        cdsCsvModel.setContract_Size(csvMap.get(cdmNewTradeMap.get("Contract_Size")).fieldValue);
        cdsCsvModel.setSwap_Type(csvMap.get(cdmNewTradeMap.get("Swap_Type")).fieldValue);
        cdsCsvModel.setED(csvMap.get(cdmNewTradeMap.get("ED")).fieldValue);
        cdsCsvModel.setMD(csvMap.get(cdmNewTradeMap.get("MD")).fieldValue);
        cdsCsvModel.setPay_Leg_Identifier(csvMap.get(cdmNewTradeMap.get("Pay_Leg_Identifier")).fieldValue);
        cdsCsvModel.setPAY_Leg_Notional(csvMap.get(cdmNewTradeMap.get("PAY_Leg_Notional")).fieldValue);
        cdsCsvModel.setPayNotionalExchange(csvMap.get(cdmNewTradeMap.get("PayNotionalExchange")).fieldValue);
        cdsCsvModel.setPay_Leg_Currency(csvMap.get(cdmNewTradeMap.get("Pay_Leg_Currency")).fieldValue);
        cdsCsvModel.setPAY_Leg_Type(csvMap.get(cdmNewTradeMap.get("PAY_Leg_Type")).fieldValue);
        cdsCsvModel.setPay_CPN_Cycle(csvMap.get(cdmNewTradeMap.get("Pay_CPN_Cycle")).fieldValue);
        cdsCsvModel.setPay_Reset_Frequency(csvMap.get(cdmNewTradeMap.get("Pay_Reset_Frequency")).fieldValue);
        cdsCsvModel.setPay_Index(csvMap.get(cdmNewTradeMap.get("Pay_Index")).fieldValue);
        cdsCsvModel.setPay_Reset_Lag(csvMap.get(cdmNewTradeMap.get("Pay_Reset_Lag")).fieldValue);
        cdsCsvModel.setPay_Spread(csvMap.get(cdmNewTradeMap.get("Pay_Spread")).fieldValue);
        cdsCsvModel.setPay_Initial_Index_Rate(csvMap.get(cdmNewTradeMap.get("Pay_Initial_Index_Rate")).fieldValue);
        cdsCsvModel.setPay_Fixed_Rate(csvMap.get(cdmNewTradeMap.get("Pay_Fixed_Rate")).fieldValue);
        cdsCsvModel.setPay_First_PD(csvMap.get(cdmNewTradeMap.get("Pay_First_PD")).fieldValue);
        cdsCsvModel.setPay_Roll_Date(csvMap.get(cdmNewTradeMap.get("Pay_Roll_Date")).fieldValue);
        cdsCsvModel.setPay_Bus_Convention(csvMap.get(cdmNewTradeMap.get("Pay_Bus_Convention")).fieldValue);
        cdsCsvModel.setPay_Accrual_Code(csvMap.get(cdmNewTradeMap.get("Pay_Accrual_Code")).fieldValue);
        cdsCsvModel.setPay_Hol_Cal1(csvMap.get(cdmNewTradeMap.get("Pay_Hol_Cal1")).fieldValue);
        cdsCsvModel.setPay_Hol_Cal2(csvMap.get(cdmNewTradeMap.get("Pay_Hol_Cal2")).fieldValue);
        cdsCsvModel.setPay_Compounding(csvMap.get(cdmNewTradeMap.get("Pay_Compounding")).fieldValue);
        cdsCsvModel.setPay_Leg_Arrears(csvMap.get(cdmNewTradeMap.get("Pay_Leg_Arrears")).fieldValue);
        cdsCsvModel.setPay_Leg_UnderlyingID(csvMap.get(cdmNewTradeMap.get("Pay_Leg_UnderlyingID")).fieldValue);
        cdsCsvModel.setPay_Leg_UnderlyingName(csvMap.get(cdmNewTradeMap.get("Pay_Leg_UnderlyingName")).fieldValue);
        cdsCsvModel.setPay_Leg_Recovery_Rate(csvMap.get(cdmNewTradeMap.get("Pay_Leg_Recovery_Rate")).fieldValue);
        cdsCsvModel.setPay_Leg_Other_Calculation(csvMap.get(cdmNewTradeMap.get("Pay_Leg_Other_Calculation")).fieldValue);
        cdsCsvModel.setPayCurFixingRate(csvMap.get(cdmNewTradeMap.get("PayCurFixingRate")).fieldValue);
        cdsCsvModel.setPayCurFixingDate(csvMap.get(cdmNewTradeMap.get("PayCurFixingDate")).fieldValue);
        cdsCsvModel.setREC_Leg_Identifier(csvMap.get(cdmNewTradeMap.get("REC_Leg_Identifier")).fieldValue);
        cdsCsvModel.setREC_Leg_Notional(csvMap.get(cdmNewTradeMap.get("REC_Leg_Notional")).fieldValue);
        cdsCsvModel.setRECNotionalExchange(csvMap.get(cdmNewTradeMap.get("RECNotionalExchange")).fieldValue);
        cdsCsvModel.setREC_Leg_Currency(csvMap.get(cdmNewTradeMap.get("REC_Leg_Currency")).fieldValue);
        cdsCsvModel.setREC_Leg_Type(csvMap.get(cdmNewTradeMap.get("REC_Leg_Type")).fieldValue);
        cdsCsvModel.setREC_CPN_Cycle(csvMap.get(cdmNewTradeMap.get("REC_CPN_Cycle")).fieldValue);
        cdsCsvModel.setREC_Reset_Frequency(csvMap.get(cdmNewTradeMap.get("REC_Reset_Frequency")).fieldValue);
        cdsCsvModel.setREC_Index(csvMap.get(cdmNewTradeMap.get("REC_Index")).fieldValue);
        cdsCsvModel.setRECIndexIDType(csvMap.get(cdmNewTradeMap.get("RECIndexIDType")).fieldValue);
        cdsCsvModel.setREC_Reset_Lag(csvMap.get(cdmNewTradeMap.get("REC_Reset_Lag")).fieldValue);
        cdsCsvModel.setREC_Spread(csvMap.get(cdmNewTradeMap.get("REC_Spread")).fieldValue);
        cdsCsvModel.setREC_Initial_Index_Rate(csvMap.get(cdmNewTradeMap.get("REC_Initial_Index_Rate")).fieldValue);
        cdsCsvModel.setREC_Fixed_Rate(csvMap.get(cdmNewTradeMap.get("REC_Fixed_Rate")).fieldValue);
        cdsCsvModel.setREC_First_PD(csvMap.get(cdmNewTradeMap.get("REC_First_PD")).fieldValue);
        cdsCsvModel.setREC_Roll_Date(csvMap.get(cdmNewTradeMap.get("REC_Roll_Date")).fieldValue);
        cdsCsvModel.setREC_Bus_Convention(csvMap.get(cdmNewTradeMap.get("REC_Bus_Convention")).fieldValue);
        cdsCsvModel.setREC_Accrual_Code(csvMap.get(cdmNewTradeMap.get("REC_Accrual_Code")).fieldValue);
        cdsCsvModel.setREC_Hol_Cal1(csvMap.get(cdmNewTradeMap.get("REC_Hol_Cal1")).fieldValue);
        cdsCsvModel.setREC_Hol_Cal2(csvMap.get(cdmNewTradeMap.get("REC_Hol_Cal2")).fieldValue);
        cdsCsvModel.setREC_Compounding(csvMap.get(cdmNewTradeMap.get("REC_Compounding")).fieldValue);
        cdsCsvModel.setREC_Compounding_Freq(csvMap.get(cdmNewTradeMap.get("REC_Compounding_Freq")).fieldValue);
        cdsCsvModel.setREC_Leg_Arrears(csvMap.get(cdmNewTradeMap.get("REC_Leg_Arrears")).fieldValue);
        cdsCsvModel.setREC_Leg_UnderlyingID(csvMap.get(cdmNewTradeMap.get("REC_Leg_UnderlyingID")).fieldValue);
        cdsCsvModel.setREC_Leg_UnderlyingName(csvMap.get(cdmNewTradeMap.get("REC_Leg_UnderlyingName")).fieldValue);
        cdsCsvModel.setREC_Leg_Recovery_Rate(csvMap.get(cdmNewTradeMap.get("REC_Leg_Recovery_Rate")).fieldValue);
        cdsCsvModel.setREC_Leg_Other_Calculation(csvMap.get(cdmNewTradeMap.get("REC_Leg_Other_Calculation")).fieldValue);
        cdsCsvModel.setRECCurFixingRate(csvMap.get(cdmNewTradeMap.get("RECCurFixingRate")).fieldValue);
        cdsCsvModel.setRECCurFixingDate(csvMap.get(cdmNewTradeMap.get("RECCurFixingDate")).fieldValue);
        cdsCsvModel.setNotional(csvMap.get(cdmNewTradeMap.get("Notional")).fieldValue);
        cdsCsvModel.setCurrency_Code(csvMap.get(cdmNewTradeMap.get("Currency_Code")).fieldValue);
        cdsCsvModel.setCredit_Protection_Direction(csvMap.get(cdmNewTradeMap.get("Credit_Protection_Direction")).fieldValue);
        cdsCsvModel.setReference_Obligation(csvMap.get(cdmNewTradeMap.get("Reference_Obligation")).fieldValue);
        cdsCsvModel.setRefOblIDType(csvMap.get(cdmNewTradeMap.get("RefOblIDType")).fieldValue);
        cdsCsvModel.setISIN(csvMap.get(cdmNewTradeMap.get("ISIN")).fieldValue);
        cdsCsvModel.setCPN_Type(csvMap.get(cdmNewTradeMap.get("CPN_Type")).fieldValue);
        cdsCsvModel.setCPN_Cycle(csvMap.get(cdmNewTradeMap.get("CPN_Cycle")).fieldValue);
        cdsCsvModel.setFixed_Rate(csvMap.get(cdmNewTradeMap.get("Fixed_Rate")).fieldValue);
        cdsCsvModel.setFirst_PD(csvMap.get(cdmNewTradeMap.get("First_PD")).fieldValue);
        cdsCsvModel.setBus_Convention(csvMap.get(cdmNewTradeMap.get("Bus_Convention")).fieldValue);
        cdsCsvModel.setAccrual_Code(csvMap.get(cdmNewTradeMap.get("Accrual_Code")).fieldValue);
        cdsCsvModel.setHol_Cal1(csvMap.get(cdmNewTradeMap.get("Hol_Cal1")).fieldValue);
        cdsCsvModel.setHol_Cal2(csvMap.get(cdmNewTradeMap.get("Hol_Cal2")).fieldValue);
        cdsCsvModel.setCompoundingStartDate(csvMap.get(cdmNewTradeMap.get("CompoundingStartDate")).fieldValue);
        cdsCsvModel.setCompoundingEndDate(csvMap.get(cdmNewTradeMap.get("CompoundingEndDate")).fieldValue);
        cdsCsvModel.setCompoundingFreqID(csvMap.get(cdmNewTradeMap.get("CompoundingFreqID")).fieldValue);
        cdsCsvModel.setArrears(csvMap.get(cdmNewTradeMap.get("Arrears")).fieldValue);
        cdsCsvModel.setDateAdded(csvMap.get(cdmNewTradeMap.get("DateAdded")).fieldValue);
        cdsCsvModel.setDateAmended(csvMap.get(cdmNewTradeMap.get("DateAmended")).fieldValue);
        cdsCsvModel.setOPTION_UNDERLYING(csvMap.get(cdmNewTradeMap.get("OPTION_UNDERLYING")).fieldValue);
        cdsCsvModel.setAttachmentPoint(csvMap.get(cdmNewTradeMap.get("AttachmentPoint")).fieldValue);
        cdsCsvModel.setExhaustionPoint(csvMap.get(cdmNewTradeMap.get("ExhaustionPoint")).fieldValue);
        cdsCsvModel.setROID(csvMap.get(cdmNewTradeMap.get("ROID")).fieldValue);
        cdsCsvModel.setInflation_Index_Fixing_Name(csvMap.get(cdmNewTradeMap.get("Inflation_Index_Fixing_Name")).fieldValue);
        cdsCsvModel.setInflation_Index_Fixing_Lag(csvMap.get(cdmNewTradeMap.get("Inflation_Index_Fixing_Lag")).fieldValue);
        cdsCsvModel.setTier(csvMap.get(cdmNewTradeMap.get("Tier")).fieldValue);
        cdsCsvModel.setDocument_Clause(csvMap.get(cdmNewTradeMap.get("Document_Clause")).fieldValue);
        cdsCsvModel.setSummit_Trade_ID(csvMap.get(cdmNewTradeMap.get("Summit_Trade_ID")).fieldValue);
        cdsCsvModel.setDPX_Unique_Pricing_ID(csvMap.get(cdmNewTradeMap.get("DPX_Unique_Pricing_ID")).fieldValue);
        cdsCsvModel.setPay_TextField1(csvMap.get(cdmNewTradeMap.get("Pay_TextField1")).fieldValue);
        cdsCsvModel.setPay_TextField2(csvMap.get(cdmNewTradeMap.get("Pay_TextField2")).fieldValue);
        cdsCsvModel.setPay_TextField3(csvMap.get(cdmNewTradeMap.get("Pay_TextField3")).fieldValue);
        cdsCsvModel.setPay_TextField4(csvMap.get(cdmNewTradeMap.get("Pay_TextField4")).fieldValue);
        cdsCsvModel.setPay_TextField5(csvMap.get(cdmNewTradeMap.get("Pay_TextField5")).fieldValue);
        cdsCsvModel.setRec_TextField1(csvMap.get(cdmNewTradeMap.get("Rec_TextField1")).fieldValue);
        cdsCsvModel.setRec_TextField2(csvMap.get(cdmNewTradeMap.get("Rec_TextField2")).fieldValue);
        cdsCsvModel.setRec_TextField3(csvMap.get(cdmNewTradeMap.get("Rec_TextField3")).fieldValue);
        cdsCsvModel.setRec_TextField4(csvMap.get(cdmNewTradeMap.get("Rec_TextField4")).fieldValue);
        cdsCsvModel.setRec_TextField5(csvMap.get(cdmNewTradeMap.get("Rec_TextField5")).fieldValue);
        cdsCsvModel.setBarrier_Lower(csvMap.get(cdmNewTradeMap.get("Barrier_Lower")).fieldValue);
        cdsCsvModel.setBarrier_Upper(csvMap.get(cdmNewTradeMap.get("Barrier_Upper")).fieldValue);
        cdsCsvModel.setSubOption_Type(csvMap.get(cdmNewTradeMap.get("SubOption_Type")).fieldValue);
        cdsCsvModel.setBarrier_Frequency(csvMap.get(cdmNewTradeMap.get("Barrier_Frequency")).fieldValue);
        cdsCsvModel.setCPRMLevel(csvMap.get(cdmNewTradeMap.get("CPRMLevel")).fieldValue);
        cdsCsvModel.setPrimarySource(csvMap.get(cdmNewTradeMap.get("PrimarySource")).fieldValue);
        cdsCsvModel.setPrimaryPriceType(csvMap.get(cdmNewTradeMap.get("PrimaryPriceType")).fieldValue);
        cdsCsvModel.setSecondarySource(csvMap.get(cdmNewTradeMap.get("SecondarySource")).fieldValue);
        cdsCsvModel.setSecondaryPriceType(csvMap.get(cdmNewTradeMap.get("SecondaryPriceType")).fieldValue);
        cdsCsvModel.setTeritarySource(csvMap.get(cdmNewTradeMap.get("TeritarySource")).fieldValue);
        cdsCsvModel.setTeritaryPriceType(csvMap.get(cdmNewTradeMap.get("TeritaryPriceType")).fieldValue);
        cdsCsvModel.setBroker_Name(csvMap.get(cdmNewTradeMap.get("Broker_Name")).fieldValue);
        cdsCsvModel.setBrokerSecID(csvMap.get(cdmNewTradeMap.get("BrokerSecID")).fieldValue);
        cdsCsvModel.setUSI(csvMap.get(cdmNewTradeMap.get("USI")).fieldValue);
        cdsCsvModel.setPay_Management_Fee(csvMap.get(cdmNewTradeMap.get("Pay_Management_Fee")).fieldValue);
        cdsCsvModel.setRec_Management_Fee(csvMap.get(cdmNewTradeMap.get("Rec_Management_Fee")).fieldValue);
        cdsCsvModel.setAllocation_Region(csvMap.get(cdmNewTradeMap.get("Allocation_Region")).fieldValue);
        cdsCsvModel.setPosition(csvMap.get(cdmNewTradeMap.get("Position")).fieldValue);
        cdsCsvModel.setContract_Rate(csvMap.get(cdmNewTradeMap.get("Contract_Rate")).fieldValue);
        cdsCsvModel.setBase_Currency(csvMap.get(cdmNewTradeMap.get("Base_Currency")).fieldValue);
        cdsCsvModel.setQuote_Currency(csvMap.get(cdmNewTradeMap.get("Quote_Currency")).fieldValue);
        cdsCsvModel.setSettlement_Date(csvMap.get(cdmNewTradeMap.get("Settlement_Date")).fieldValue);
        cdsCsvModel.setDelivery_Date(csvMap.get(cdmNewTradeMap.get("Delivery_Date")).fieldValue);
        cdsCsvModel.setReference_Obligation_Desc(csvMap.get(cdmNewTradeMap.get("Reference_Obligation_Desc")).fieldValue);
        cdsCsvModel.setTrigger_Direction(csvMap.get(cdmNewTradeMap.get("Trigger_Direction")).fieldValue);
        cdsCsvModel.setLong_Accounting_System_ID(csvMap.get(cdmNewTradeMap.get("Long_Accounting_System_ID")).fieldValue);
        cdsCsvModel.setShort_Accounting_System_ID(csvMap.get(cdmNewTradeMap.get("Short_Accounting_System_ID")).fieldValue);
        cdsCsvModel.setAdjusted_MD(csvMap.get(cdmNewTradeMap.get("Adjusted_MD")).fieldValue);
        cdsCsvModel.setDiscounting(csvMap.get(cdmNewTradeMap.get("Discounting")).fieldValue);
        cdsCsvModel.setPay_Lookback(csvMap.get(cdmNewTradeMap.get("Pay_Lookback")).fieldValue);
        cdsCsvModel.setRec_Lookback(csvMap.get(cdmNewTradeMap.get("Rec_Lookback")).fieldValue);
        cdsCsvModel.setPay_Lockout(csvMap.get(cdmNewTradeMap.get("Pay_Lockout")).fieldValue);
        cdsCsvModel.setRec_Lockout(csvMap.get(cdmNewTradeMap.get("Rec_Lockout")).fieldValue);
        cdsCsvModel.setPay_Payment_Delay(csvMap.get(cdmNewTradeMap.get("Pay_Payment_Delay")).fieldValue);
        cdsCsvModel.setRec_Payment_Delay(csvMap.get(cdmNewTradeMap.get("Rec_Payment_Delay")).fieldValue);
        cdsCsvModel.setMOSID(csvMap.get(cdmNewTradeMap.get("MOSID")).fieldValue);
        cdsCsvModel.setPay_Compounding_Freq(csvMap.get(cdmNewTradeMap.get("Pay_Compounding_Freq")).fieldValue);
        cdsCsvModel.setPay_Compounding_Method(csvMap.get(cdmNewTradeMap.get("Pay_Compounding_Method")).fieldValue);
        cdsCsvModel.setRec_Compounding_Method(csvMap.get(cdmNewTradeMap.get("Rec_Compounding_Method")).fieldValue);
        cdsCsvModel.setPay_Observation_Shift(csvMap.get(cdmNewTradeMap.get("Pay_Observation_Shift")).fieldValue);
        cdsCsvModel.setRec_Observation_Shift(csvMap.get(cdmNewTradeMap.get("Rec_Observation_Shift")).fieldValue);
        cdsCsvModel.setUnderlying_ID_Type(csvMap.get(cdmNewTradeMap.get("Underlying_ID_Type")).fieldValue);
        cdsCsvModel.setSettlement_Type(csvMap.get(cdmNewTradeMap.get("Settlement_Type")).fieldValue);
        cdsCsvModel.setRepo_Rate(csvMap.get(cdmNewTradeMap.get("Repo_Rate")).fieldValue);
        cdsCsvModel.setPAY_ZeroCouponRatio(csvMap.get(cdmNewTradeMap.get("PAY_ZeroCouponRatio")).fieldValue);
        cdsCsvModel.setREC_ZeroCouponRatio(csvMap.get(cdmNewTradeMap.get("REC_ZeroCouponRatio")).fieldValue);
        cdsCsvModel.setPAY_CrossCurrRatio(csvMap.get(cdmNewTradeMap.get("PAY_CrossCurrRatio")).fieldValue);
        cdsCsvModel.setREC_CrossCurrRatio(csvMap.get(cdmNewTradeMap.get("REC_CrossCurrRatio")).fieldValue);
        cdsCsvModel.setIsPriced_Flag(csvMap.get(cdmNewTradeMap.get("IsPriced_Flag")).fieldValue);
        cdsCsvModel.setStrike_Type(csvMap.get(cdmNewTradeMap.get("Strike_Type")).fieldValue);
        cdsCsvModel.setPremium_Ratio(csvMap.get(cdmNewTradeMap.get("Premium_Ratio")).fieldValue);
        cdsCsvModel.setPremium_Payment_Date(csvMap.get(cdmNewTradeMap.get("Premium_Payment_Date")).fieldValue);
        cdsCsvModel.setAmortized_Swap(csvMap.get(cdmNewTradeMap.get("Amortized_Swap")).fieldValue);
        cdsCsvModel.setClearing_House_Name(csvMap.get(cdmNewTradeMap.get("Clearing_House_Name")).fieldValue);
        cdsCsvModel.setClearing_House_Code(csvMap.get(cdmNewTradeMap.get("Clearing_House_Code")).fieldValue);
        cdsCsvModel.setDPX_Clearing_House_Key(csvMap.get(cdmNewTradeMap.get("DPX_Clearing_House_Key")).fieldValue);
        cdsCsvModel.setClearing_House_Key(csvMap.get(cdmNewTradeMap.get("Clearing_House_Key")).fieldValue);
        cdsCsvModel.setLCH_Matched_Trade_Ref(csvMap.get(cdmNewTradeMap.get("LCH_Matched_Trade_Ref")).fieldValue);
        cdsCsvModel.setDeflation_Floor(csvMap.get(cdmNewTradeMap.get("Deflation_Floor")).fieldValue);
        cdsCsvModel.setBond_Start_Date(csvMap.get(cdmNewTradeMap.get("Bond_Start_Date")).fieldValue);
        cdsCsvModel.setRatio_Return(csvMap.get(cdmNewTradeMap.get("Ratio_Return")).fieldValue);
        cdsCsvModel.setInflation_Index_Fixing_Interpolation(csvMap.get(cdmNewTradeMap.get("Inflation_Index_Fixing_Interpolation")).fieldValue);
        cdsCsvModel.setInflation_Index_Fixing_Rate(csvMap.get(cdmNewTradeMap.get("Inflation_Index_Fixing_Rate")).fieldValue);
        cdsCsvModel.setAnchored(csvMap.get(cdmNewTradeMap.get("Anchored")).fieldValue);
        cdsCsvModel.setLPI_Floor(csvMap.get(cdmNewTradeMap.get("LPI_Floor")).fieldValue);
        cdsCsvModel.setLPI_Cap(csvMap.get(cdmNewTradeMap.get("LPI_Cap")).fieldValue);

        return cdsCsvModel;
    }

}