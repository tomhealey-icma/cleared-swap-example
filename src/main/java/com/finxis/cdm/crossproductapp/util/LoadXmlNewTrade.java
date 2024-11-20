package com.finxis.cdm.crossproductapp.util;

import cdm.base.staticdata.party.Party;
import cdm.base.staticdata.party.PartyRole;
import cdm.event.common.*;
import cdm.event.common.functions.Create_Execution;
import cdm.event.workflow.EventTimestamp;
import cdm.event.workflow.Workflow;
import cdm.event.workflow.WorkflowStep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.finxis.cdm.crossproductapp.RepoExecutionCreation;
import com.finxis.cdm.crossproductapp.productlifecycle.ClearedCdsSwapLifecycle;
import com.finxis.cdm.crossproductapp.productlifecycle.ClearedSwapLifecycle;
import com.finxis.cdm.crossproductapp.productmodels.CdsCsvModel;
import com.finxis.cdm.crossproductapp.productmodels.ClearedCdsSwapModel;
import com.finxis.cdm.crossproductapp.productmodels.ClearedSwapModel;
import com.finxis.cdm.crossproductapp.productmodels.IrsCsvModel;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.common.hashing.GlobalKeyProcessStep;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.rosetta.model.lib.GlobalKey;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.process.PostProcessStep;
import com.rosetta.model.metafields.FieldWithMetaDate;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.io.FileUtils;
import org.finos.cdm.CdmRuntimeModule;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import javax.print.Doc;
import javax.sql.rowset.spi.XmlReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.finxis.cdm.crossproductapp.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.finxis.cdm.crossproductapp.util.CdmUtil;

import com.regnosys.rosetta.common.hashing.GlobalKeyProcessStep;
import com.regnosys.rosetta.common.hashing.NonNullHashCollector;
import com.rosetta.model.lib.GlobalKey;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.meta.Reference;
import com.rosetta.model.lib.process.PostProcessStep;
import com.rosetta.model.lib.records.Date;
import com.rosetta.model.metafields.*;
import com.rosetta.model.metafields.FieldWithMetaDate;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.model.metafields.MetaFields;

public class LoadXmlNewTrade {

    private final PostProcessStep keyProcessor;

    public LoadXmlNewTrade() {
        keyProcessor = new GlobalKeyProcessStep(NonNullHashCollector::new);
    }

    /**
     * Utility to post process a {@link RosettaModelObject} to add ll gloval keys.
     */
    private <T extends RosettaModelObject> T addGlobalKey(Class<T> type, T modelObject) {
        RosettaModelObjectBuilder builder = modelObject.toBuilder();
        keyProcessor.runProcessStep(type, builder);
        return type.cast(builder.build());
    }

    /**
     * Utility to get the global reference string from a {@link GlobalKey} instance.
     */
    private String getGlobalReference(GlobalKey globalKey) {
        return globalKey.getMeta().getGlobalKey();
    }
    public WorkflowStep createNewTradeFromXML(File xmlFile) {


        WorkflowStep workflowStep = WorkflowStep.builder();

        try {

            JAXBContext jaxbContext;

            try {
                jaxbContext = JAXBContext.newInstance(CdmNewTradeWorkflow.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                CdmNewTradeWorkflow cdmNewTradeWorkflow = (CdmNewTradeWorkflow) jaxbUnmarshaller.unmarshal(xmlFile);
                workflowStep = createNewTrade(cdmNewTradeWorkflow );
                System.out.println(cdmNewTradeWorkflow);
            } catch (JAXBException e) {
                e.printStackTrace();
            }

            System.out.println("Processing XML File Complete.");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        return workflowStep;
    }

    public WorkflowStep createNewTradeFromXMLStream(String xmlStr) {


        com.finxis.cdm.crossproductapp.util.XmlReader xmlReader = new com.finxis.cdm.crossproductapp.util.XmlReader();

        WorkflowStep workflowStep = WorkflowStep.builder();

        try {
            //DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            //Document doc = db.parse(new ByteArrayInputStream(xmlStr.getBytes("UTF-8")));
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlStr));

            //Document doc = db.parse(is);

            JAXBContext jaxbContext;

            try {
                jaxbContext = JAXBContext.newInstance(CdmNewTradeWorkflow.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                CdmNewTradeWorkflow cdmNewTradeWorkflow = (CdmNewTradeWorkflow) jaxbUnmarshaller.unmarshal(is);
                workflowStep = createNewTrade(cdmNewTradeWorkflow );
                System.out.println(cdmNewTradeWorkflow);
            } catch (JAXBException e) {
                e.printStackTrace();
            }

            System.out.println("Processing XML File Complete.");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        return workflowStep;
    }

    public WorkflowStep createNewTradeFromXMLDoc(Document doc) throws IOException, ParserConfigurationException, SAXException, TransformerException {


        WorkflowStep workflowStep = WorkflowStep.builder();

        DocumentToFile documentToFile = new DocumentToFile();
        documentToFile.writeWc3DocumentToFile(doc);
        String xmlString = documentToFile.writeWc3DocumentToString(doc);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder builder = dbf.newDocumentBuilder();

        //String xmlString = FileUtils.readFileToString(new File("cdmTrade2.xml"), StandardCharsets.UTF_8);
        Reader xmlDoc = new StringReader(xmlString);

        try {

            JAXBContext jaxbContext;

            try {
                jaxbContext = JAXBContext.newInstance(CdmNewTradeWorkflow.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                CdmNewTradeWorkflow cdmNewTradeWorkflow = (CdmNewTradeWorkflow) jaxbUnmarshaller.unmarshal(xmlDoc);
                workflowStep = createNewTrade(cdmNewTradeWorkflow );
                System.out.println(cdmNewTradeWorkflow);
            } catch (JAXBException e) {
                e.printStackTrace();
            }

            System.out.println("Processing XML File Complete.");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        return workflowStep;
    }

    public WorkflowStep createNewTrade(CdmNewTradeWorkflow cdmt) throws IOException {

        
        CdmUtil cdmUtil = new CdmUtil();
        IcmaRepoUtil ru = new IcmaRepoUtil();
        RepoExecutionCreation repoExecutionInstructionCreation = new RepoExecutionCreation();

        Party buyer = cdmUtil.createRepoParty(cdmt.workflow.workflowStep.buyer.getLEI(), "LEI", cdmt.workflow.workflowStep.buyer.getLEIName());
        Party seller = cdmUtil.createRepoParty(cdmt.workflow.workflowStep.seller.getLEI(), "LEI", cdmt.workflow.workflowStep.seller.getLEIName());

        FieldWithMetaDate tradeDate = cdmUtil.createCdmDateFromShortDateString(cdmt.workflow.workflowStep.getTransactionDate().toString());
        FieldWithMetaDate purchaseDate = cdmUtil.createCdmDateFromShortDateString(cdmt.workflow.workflowStep.getPurchaseDate().toString());
        FieldWithMetaDate repurchaseDate = cdmUtil.createCdmDateFromShortDateString(cdmt.workflow.workflowStep.getRepurchaseDate().toString());

        PartyRole buyerRole = cdmUtil.createRepoPartyRole(buyer,cdmt.workflow.workflowStep.buyer.getLEI(),"BUYER");
        PartyRole sellerRole = cdmUtil.createRepoPartyRole(buyer,cdmt.workflow.workflowStep.seller.getLEI(),"SELLER");

        CdmNewTradeWorkflow.Workflow.WorkflowStep wfs = cdmt.workflow.getWorkflowStep();

        ExecutionInstruction repoExecutionInstruction = repoExecutionInstructionCreation.createRepoExecutionInstruction(
                wfs.getTransactionDate().toString(),
                wfs.getPurchaseDate().toString(),
                wfs.getRepurchaseDate().toString(),
                wfs.getTradeIdentifier().toString(),
                wfs.getTradeIdentifier().toString(),
                wfs.buyer.getLEI(),
                wfs.buyer.getLEIName(),
                wfs.seller.getLEI(),
                wfs.seller.getLEIName(),
                wfs.getCollateralList().getCollateral().getDescription(),
                wfs.getCollateralList().getCollateral().getISIN(),
                String.valueOf(wfs.getCollateralList().getCollateral().getNominalAmount()),
                String.valueOf(wfs.getCollateralList().getCollateral().getCleanPrice()),
                String.valueOf(wfs.getCollateralList().getCollateral().getDirtyPrice()),
                String.valueOf(wfs.getCollateralList().getCollateral().getAdjustedSettlementValue()),
                wfs.getCollateralList().getCollateral().getCurrency(),
                String.valueOf(wfs.getRepoRate().fixedRate.getValue().toString()),
                wfs.getCollateralList().getCollateral().getCurrency(),
                String.valueOf(wfs.getPurchasePrice()),
                String.valueOf(wfs.getCollateralList().getCollateral().getHaircut()),
                wfs.getTermType(),
                wfs.getOptionality().getTerminationOnDemand(),
                String.valueOf(wfs.getOptionality().getNoticePeriod()),
                wfs.getDeliveryMethod().toString(),
                wfs.getSubstitution().getAllowed(),
                wfs.getRateType(),
                wfs.getDayCount(),
                String.valueOf(wfs.getPurchasePrice()),
                String.valueOf(wfs.getRepurchasePrice()),
                wfs.agreement.getAgreementName().toString(),
                wfs.agreement.getAgreementGoverningLaw().toString(),
                String.valueOf(wfs.agreement.getAgreementVersion()),
                wfs.agreement.getAgreementIssuer().toString(),
                String.valueOf(wfs.agreement.getAgreementDate()),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
                );

        Injector injector = Guice.createInjector(new CdmRuntimeModule());

        //Create a primitive execution instruction
        PrimitiveInstruction repoPrimitiveInstruction = PrimitiveInstruction.builder()
                .setExecution(repoExecutionInstruction);

        Date effectiveDate = repoExecutionInstruction.getProduct().getContractualProduct().getEconomicTerms().getEffectiveDate().getAdjustableDate().getUnadjustedDate();
        Date eventDate = repoExecutionInstruction.getProduct().getContractualProduct().getEconomicTerms().getEffectiveDate().getAdjustableDate().getUnadjustedDate();

        Create_Execution.Create_ExecutionDefault repoExecution = new Create_Execution.Create_ExecutionDefault();
        injector.injectMembers(repoExecution);
        TradeState tradeState = repoExecution.evaluate(repoExecutionInstruction);

        //Create an instruction from primitive. Before state is null
        Instruction instruction = Instruction.builder()
                .setPrimitiveInstruction(repoPrimitiveInstruction)
                .setBefore(null)
                .build();

        List<Instruction> instructionList = List.of(instruction);


        DateTimeFormatter eventDateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        String eventDateTime = localDateTime.format(eventDateFormat);

        BusinessEvent businessEvent = BusinessEvent.builder()
                .addInstruction(instruction)
                .setAfter(List.of(tradeState))
                .build();

        FileWriter fileWriter = new FileWriter();

        //Output used for CDM Test Case
        String icmarepoexecutionfuncinputJson = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(businessEvent);
        fileWriter.writeEventToFile("icma-repo-execution-func-input", eventDateTime, icmarepoexecutionfuncinputJson);

        String tDate = wfs.getTransactionDate().toString().replaceAll("\\s", "") + "T00:00:00.000+00:00";
        DateTimeFormatter formatter  = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz");
        ZonedDateTime zdtWithZoneOffset = ZonedDateTime.parse(tDate, formatter);
        ZonedDateTime zdtInLocalTimeline = zdtWithZoneOffset.withZoneSameInstant(ZoneId.systemDefault());
        EventTimestamp eventTimestamp = new EventTimestamp.EventTimestampBuilderImpl()
                .setDateTime(zdtWithZoneOffset);

        Workflow wf = Workflow.builder()
                .setSteps(List.of(WorkflowStep.builder()
                        .addParty(List.of(buyer,seller))
                        .setBusinessEvent(businessEvent)
                        .addTimestamp(eventTimestamp)
                        .setPreviousWorkflowStepValue(null)))
                        .build();

        return wf.getSteps().get(0);

    }

    public WorkflowStep createNewIrsTrade(IrsCsvModel irsm) throws IOException {


        CdmUtil cdmUtil = new CdmUtil();
        IcmaRepoUtil ru = new IcmaRepoUtil();
        ClearedSwapLifecycle csl = new ClearedSwapLifecycle();
        ClearedSwapModel csm = mapIrsCsvToSwapModel(irsm);

        csl.createExecutionInstruction(csm);

        ClearedSwapLifecycle clearedSwapLifecycle = new ClearedSwapLifecycle();
        String businessEventJson=null;

        TradeState tradeState = clearedSwapLifecycle.createExecution(csm);

        ExecutionInstruction executionInstruction = clearedSwapLifecycle.createExecutionInstruction(csm);
        PrimitiveInstruction primitiveInstruction = PrimitiveInstruction.builder()
                .setExecution(executionInstruction)
                .build();

        BusinessEvent businessEvent = clearedSwapLifecycle.createExecutionBusinessEvent(tradeState, primitiveInstruction);

        businessEventJson = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(businessEvent);

        String tDate = businessEvent.getEffectiveDate().toString().replaceAll("\\s", "") + "T00:00:00.000+00:00";
        DateTimeFormatter formatter  = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz");
        ZonedDateTime zdtWithZoneOffset = ZonedDateTime.parse(tDate, formatter);
        ZonedDateTime zdtInLocalTimeline = zdtWithZoneOffset.withZoneSameInstant(ZoneId.systemDefault());
        EventTimestamp eventTimestamp = new EventTimestamp.EventTimestampBuilderImpl()
                .setDateTime(zdtWithZoneOffset);
        Party buyer = businessEvent.getAfter().get(0).getTrade().getParty().get(0);
        Party seller = businessEvent.getAfter().get(0).getTrade().getParty().get(1);
        Workflow wf = Workflow.builder()
                .setSteps(List.of(WorkflowStep.builder()
                        .addParty(List.of(buyer,seller))
                        .setBusinessEvent(businessEvent)
                        .addTimestamp(eventTimestamp)
                        .setPreviousWorkflowStepValue(null)))
                .build();
        return wf.getSteps().get(0);
    }

    public ClearedSwapModel mapIrsCsvToSwapModel(IrsCsvModel irsm){

        CdmUtil cdmUtil = new CdmUtil();
        IcmaRepoUtil ru = new IcmaRepoUtil();
        Map<String, String> cdmMap = new HashMap<>();
        CdmEnumMap map = new CdmEnumMap();
        map.buildEnumMap(cdmMap);


        ClearedSwapModel csm = new ClearedSwapModel();
        csm.effectiveDate = converDateFormat(irsm.getED());
        csm.maturityDate = converDateFormat(irsm.getMD());
        csm.tradeDate = converDateFormat(irsm.getTD());
        csm.customerAccount = irsm.getIM_ID();
        csm.currency = irsm.getCurrency_Code();
        csm.dealId = irsm.getDPX_Clearing_House_Key();
        csm.centralCounterparty = irsm.getClearing_House_Code();

        if(irsm.getIAS_ID().equals("")){ //Client receiving fixed
            csm.fixedRatePayer = irsm.getClearing_House_Code();
            csm.floatingRatePayer = irsm.getClient_ID();
            csm.quantity = irsm.getREC_Leg_Notional();
            csm.fixedRate = irsm.getREC_Fixed_Rate();
            csm.payBusinessDayConvention = irsm.getPay_Bus_Convention();
            csm.receiveBusinessDayConvention = irsm.getREC_Bus_Convention();
            csm.floatingRateDayCountFraction = irsm.getPay_Accrual_Code();
            csm.fixedRateDayCountFraction = irsm.getREC_Accrual_Code();
            csm.floatingRateReference = "0.0";
            csm.floatingRatePayFreq= irsm.getPay_CPN_Cycle();
            csm.fixedRatePayFreq = irsm.getREC_CPN_Cycle();

        }else{
            csm.fixedRatePayer = irsm.getClient_ID();
            csm.floatingRatePayer = irsm.getClearing_House_Code();
            csm.quantity = irsm.getPAY_Leg_Notional();
            csm.fixedRate = irsm.getPay_Fixed_Rate();
            csm.payBusinessDayConvention = irsm.getREC_Bus_Convention();
            csm.receiveBusinessDayConvention = irsm.getPay_Bus_Convention();
            csm.floatingRateDayCountFraction = irsm.getREC_Accrual_Code();
            csm.fixedRateDayCountFraction = irsm.getPay_Accrual_Code();
            csm.floatingRateReference = "0.0";
            csm.floatingRatePayFreq= irsm.getREC_CPN_Cycle();
            csm.fixedRatePayFreq = irsm.getPay_CPN_Cycle();
        }


        return csm;


    }


    public WorkflowStep createNewCdsTrade(CdsCsvModel cdsCsvModel) throws IOException {


        CdmUtil cdmUtil = new CdmUtil();
        IcmaRepoUtil ru = new IcmaRepoUtil();
        ClearedCdsSwapLifecycle csl = new ClearedCdsSwapLifecycle();
        ClearedCdsSwapModel cdsm = mapCdsCsvToSwapModel(cdsCsvModel);

        csl.createExecutionInstruction(cdsm);

        ClearedCdsSwapLifecycle clearedCdsSwapLifecycle = new ClearedCdsSwapLifecycle();
        String businessEventJson=null;

        TradeState tradeState = clearedCdsSwapLifecycle.createExecution(cdsm);

        ExecutionInstruction executionInstruction = clearedCdsSwapLifecycle.createExecutionInstruction(cdsm);
        PrimitiveInstruction primitiveInstruction = PrimitiveInstruction.builder()
                .setExecution(executionInstruction)
                .build();

        BusinessEvent businessEvent = clearedCdsSwapLifecycle.createExecutionBusinessEvent(tradeState, primitiveInstruction);

        businessEventJson = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(businessEvent);

        String tDate = businessEvent.getEffectiveDate().toString().replaceAll("\\s", "") + "T00:00:00.000+00:00";
        DateTimeFormatter formatter  = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz");
        ZonedDateTime zdtWithZoneOffset = ZonedDateTime.parse(tDate, formatter);
        ZonedDateTime zdtInLocalTimeline = zdtWithZoneOffset.withZoneSameInstant(ZoneId.systemDefault());
        EventTimestamp eventTimestamp = new EventTimestamp.EventTimestampBuilderImpl()
                .setDateTime(zdtWithZoneOffset);
        Party buyer = businessEvent.getAfter().get(0).getTrade().getParty().get(0);
        Party seller = businessEvent.getAfter().get(0).getTrade().getParty().get(1);
        Workflow wf = Workflow.builder()
                .setSteps(List.of(WorkflowStep.builder()
                        .addParty(List.of(buyer,seller))
                        .setBusinessEvent(businessEvent)
                        .addTimestamp(eventTimestamp)
                        .setPreviousWorkflowStepValue(null)))
                .build();
        return wf.getSteps().get(0);
    }
    public ClearedCdsSwapModel mapCdsCsvToSwapModel(CdsCsvModel cdsCsvm){

        CdmUtil cdmUtil = new CdmUtil();
        IcmaRepoUtil ru = new IcmaRepoUtil();
        Map<String, String> cdmMap = new HashMap<>();
        CdmEnumMap map = new CdmEnumMap();
        map.buildEnumMap(cdmMap);


        ClearedCdsSwapModel cdsm = new ClearedCdsSwapModel();
        cdsm.effectiveDate = converDateFormat(cdsCsvm.getED());
        cdsm.maturityDate = converDateFormat(cdsCsvm.getMD());
        cdsm.tradeDate = converDateFormat(cdsCsvm.getTD());
        cdsm.customerAccount = cdsCsvm.getIM_ID();
        cdsm.currency = cdsCsvm.getCurrency_Code();
        cdsm.dealId = cdsCsvm.getDPX_Clearing_House_Key();
        cdsm.centralCounterparty = cdsCsvm.getClearing_House_Code();


        cdsm.fixedRatePayer = "CLIENTID";
        cdsm.quantity = cdsCsvm.getPAY_Leg_Notional();
        cdsm.fixedRate = cdsCsvm.getFixed_Rate();
        cdsm.payBusinessDayConvention = cdsCsvm.getBus_Convention();
        cdsm.fixedRateDayCountFraction = cdsCsvm.getAccrual_Code();
        cdsm.fixedRatePayFreq= cdsCsvm.getCPN_Cycle();
        cdsm.referenceObligation = cdsCsvm.getReference_Obligation();
        cdsm.referenceObligationId = cdsCsvm.getISIN();


        return cdsm;


    }

    public String converDateFormat(String date){

        String month = date.substring(0,2);
        String day = date.substring(3,5);
        String year = date.substring(6,10);

        String newDate = year + "-" + month + "-" + day;

        return newDate;

    }

}
