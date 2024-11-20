package com.finxis.cdm.crossproductapp.productlifecycle;

import cdm.base.datetime.*;
import cdm.base.datetime.daycount.DayCountFractionEnum;
import cdm.base.datetime.daycount.metafields.FieldWithMetaDayCountFractionEnum;
import cdm.base.datetime.metafields.ReferenceWithMetaBusinessCenters;
import cdm.base.math.NonNegativeQuantitySchedule;
import cdm.base.math.QuantityChangeDirectionEnum;
import cdm.base.math.UnitType;
import cdm.base.math.metafields.FieldWithMetaNonNegativeQuantitySchedule;
import cdm.base.math.metafields.ReferenceWithMetaNonNegativeQuantitySchedule;
import cdm.base.staticdata.asset.common.ProductTaxonomy;
import cdm.base.staticdata.asset.common.TaxonomySourceEnum;
import cdm.base.staticdata.party.*;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import cdm.event.common.*;
import cdm.event.common.functions.*;
import cdm.event.common.metafields.ReferenceWithMetaTradeState;
import cdm.event.qualification.functions.Qualify_Execution;
import cdm.event.workflow.Workflow;
import cdm.observable.asset.*;
import cdm.observable.asset.metafields.ReferenceWithMetaFloatingRateOption;
import cdm.observable.asset.metafields.ReferenceWithMetaPriceSchedule;
import cdm.product.asset.*;
import cdm.product.asset.calculation.functions.FloatingAmountCalculation;
import cdm.product.asset.floatingrate.FloatingAmountCalculationDetails;
import cdm.product.common.schedule.*;
import cdm.product.common.schedule.metafields.ReferenceWithMetaCalculationPeriodDates;
import cdm.product.common.settlement.PriceQuantity;
import cdm.product.common.settlement.ResolvablePriceQuantity;
import cdm.product.common.settlement.SettlementTerms;
import cdm.product.common.settlement.SettlementTypeEnum;
import cdm.product.template.*;
import cdm.product.template.metafields.ReferenceWithMetaPayout;
import cdm.product.template.validation.datarule.PayoutPaymentDatesAdjustments;
import com.finxis.cdm.crossproductapp.IcmaRepoUtil;
import com.finxis.cdm.crossproductapp.productmodels.ClearedSwapModel;
import com.finxis.cdm.crossproductapp.util.CdmUtil;
import com.finxis.cdm.crossproductapp.workflows.executionworkflow.ExecutionWorkflowBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.rosetta.model.lib.meta.Reference;
import com.rosetta.model.lib.records.Date;
import com.rosetta.model.metafields.MetaFields;
import org.finos.cdm.CdmRuntimeModule;
//import org.fpml.fpml_5.confirmation.CalculationPeriodDatesReference;
import org.joda.time.DateTime;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import static java.lang.Double.parseDouble;

public class ClearedSwapLifecycle {

    private String defaultLocalTimeZone = "UTC";

    public String bookNewTrade(ClearedSwapModel csm) throws IOException {

        String businessEventJson=null;

        TradeState tradeState = createExecution(csm);

        ExecutionInstruction executionInstruction = createExecutionInstruction(csm);
        PrimitiveInstruction primitiveInstruction = PrimitiveInstruction.builder()
                .setExecution(executionInstruction)
                .build();

        BusinessEvent businessEvent = createExecutionBusinessEvent(tradeState, primitiveInstruction);

        businessEventJson = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(businessEvent);

        return businessEventJson;

    }


    public BusinessEvent createExecutionBusinessEvent(TradeState tradeState, PrimitiveInstruction primitiveInstruction) throws IOException {

        Injector injector = Guice.createInjector(new CdmRuntimeModule());

        //Create an instruction from primitive. Before state is null
        Instruction instruction = Instruction.builder()
                .setPrimitiveInstruction(primitiveInstruction)
                .setBefore(null)
                .build();

        List<Instruction> instructionList = List.of(instruction);

        IcmaRepoUtil ru = new IcmaRepoUtil();

        DateTimeFormatter eventDateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        String eventDateTime = localDateTime.format(eventDateFormat);

        BusinessEvent betest = BusinessEvent.builder()
                .addInstruction(instruction)
                .build();

        //Output used for CDM Test Case
        String executionfuncinputJson = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(betest);
        ru.writeEventToFile("cleared-irs-execution-func-input", eventDateTime, executionfuncinputJson);
        //End output for CDM Test Case

        Date effectiveDate = primitiveInstruction.getExecution().getProduct().getContractualProduct().getEconomicTerms().getEffectiveDate().getAdjustableDate().getUnadjustedDate();
        Date eventDate = primitiveInstruction.getExecution().getProduct().getContractualProduct().getEconomicTerms().getEffectiveDate().getAdjustableDate().getUnadjustedDate();


        Create_BusinessEvent rx= new Create_BusinessEvent.Create_BusinessEventDefault();
        injector.injectMembers(rx);
        BusinessEvent businessEvent = rx.evaluate(instructionList, null, eventDate, effectiveDate);

        //Output used for CDM Test Case
        String executionfuncoutputJsonJson = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(businessEvent);
        ru.writeEventToFile("cleared-irs-execution-func-output", eventDateTime, executionfuncoutputJsonJson);
        //End output for CDM Test Case

        Qualify_Execution.Qualify_ExecutionDefault qe = new Qualify_Execution.Qualify_ExecutionDefault();
        injector.injectMembers(qe);

        Boolean result = qe.evaluate(businessEvent);

        if(result)
            System.out.println("Qualify Execution Successful");
        else
            System.out.println("Qualify Execution Failed");

        return businessEvent;
    }

    public TradeState createExecution(ClearedSwapModel csm) throws IOException {

        ExecutionInstruction executionInstruction = createExecutionInstruction(csm);

        Injector injector = Guice.createInjector(new CdmRuntimeModule());

        //Create a primitive execution instruction
        PrimitiveInstruction primitiveInstruction = PrimitiveInstruction.builder()
                .setExecution(executionInstruction);

        //Date effectiveDate = primitiveInstruction.getExecution().getProduct().getContractualProduct().getEconomicTerms().getEffectiveDate().getAdjustableDate().getUnadjustedDate();
        //Date eventDate = primitiveInstruction.getExecution().getProduct().getContractualProduct().getEconomicTerms().getEffectiveDate().getAdjustableDate().getUnadjustedDate();

        Create_Execution.Create_ExecutionDefault execution = new Create_Execution.Create_ExecutionDefault();
        injector.injectMembers(execution);
        TradeState tradeState = execution.evaluate(executionInstruction);


        return tradeState;

    }

    public ExecutionInstruction createExecutionInstruction(ClearedSwapModel csm){

        ExecutionInstruction executionInstruction = new ExecutionInstruction.ExecutionInstructionBuilderImpl();
        CdmUtil cdmUtil = new CdmUtil();
        Party ccp = cdmUtil.createCdmParty(csm.centralCounterparty,"LEI","Central Counterparty");
        Party fixedRatePayer = cdmUtil.createCdmParty(csm.fixedRatePayer,"LEI", csm.fixedRatePayer);
        Party floatingRatePayer = cdmUtil.createCdmParty(csm.floatingRatePayer,"LEI", csm.floatingRatePayer);
        PartyRole ccpRole = PartyRole.builder()
                .setPartyReference(ReferenceWithMetaParty.builder()
                        .setValue(ccp))
                .setRole(PartyRoleEnum.CLEARING_ORGANIZATION)
                .build();

        PartyRole payerRole = PartyRole.builder()
                .setPartyReference(ReferenceWithMetaParty.builder()
                        .setValue(fixedRatePayer))
                .setRole(PartyRoleEnum.BUYER)
                .build();

        PartyRole receiverRole = PartyRole.builder()
                .setPartyReference(ReferenceWithMetaParty.builder()
                        .setValue(floatingRatePayer))
                .setRole(PartyRoleEnum.SELLER)
                .build();

        executionInstruction.toBuilder()
                .setTradeDate(cdmUtil.createCdmDateFromShortDateString(csm.tradeDate))
                .setTradeIdentifier(List.of(cdmUtil.createCdmTradeIdentifier(csm.dealId, "USI","CCP")))
                .setParties(List.of(ccp, fixedRatePayer, floatingRatePayer))
                .setPartyRoles(List.of(ccpRole, payerRole, receiverRole))
                .setProduct(createClearedSwapProduct(csm))
                .setPriceQuantity(List.of(createPriceQuantity(csm)))
                .build();


        return executionInstruction;

    }

    public Product createClearedSwapProduct(ClearedSwapModel csm){

        CdmUtil cdmUtil = new CdmUtil();


        Product product = Product.builder()
                .setContractualProduct(ContractualProduct.builder()
                        .setProductTaxonomy(List.of(ProductTaxonomy.builder()
                                .setSource(TaxonomySourceEnum.ISDA)
                                .setProductQualifier("InterestRate_IRSwap_FixedFloat")))
                        .setEconomicTerms(EconomicTerms.builder()
                                .setEffectiveDate(AdjustableOrRelativeDate.builder()
                                        .setAdjustableDate(AdjustableDate.builder()
                                                .setUnadjustedDate(cdmUtil.createCdmDateFromShortDateStringWoMeta(csm.effectiveDate))
                                                .setAdjustedDate(cdmUtil.createCdmDateFromShortDateString(csm.effectiveDate))))
                                .setTerminationDate(AdjustableOrRelativeDate.builder()
                                        .setAdjustableDate(AdjustableDate.builder()
                                                .setUnadjustedDate(cdmUtil.createCdmDateFromShortDateStringWoMeta(csm.maturityDate))
                                                .setAdjustedDate(cdmUtil.createCdmDateFromShortDateString(csm.maturityDate))))
                                .setPayout(Payout.builder()
                                        .addInterestRatePayout(InterestRatePayout.builder()
                                                .setPriceQuantity(ResolvablePriceQuantity.builder()
                                                        .setQuantitySchedule(ReferenceWithMetaNonNegativeQuantitySchedule.builder()
                                                                .setReference(Reference.builder()
                                                                        .setScope("DOCUMENT")
                                                                        .setReference("quantity-1"))))
                                                        .setPayerReceiver(PayerReceiver.builder()
                                                                .setPayer(CounterpartyRoleEnum.PARTY_1)
                                                                .setReceiver(CounterpartyRoleEnum.PARTY_2))
                                                        .setSettlementTerms(SettlementTerms.builder()
                                                                .setSettlementType(SettlementTypeEnum.CASH)
                                                                .setSettlementCurrencyValue("USD"))
                                                        .setRateSpecification(RateSpecification.builder()
                                                                .setFloatingRate(FloatingRateSpecification.builder()
                                                                        .setInitialRate(Price.builder()
                                                                                .setValue(BigDecimal.valueOf(parseDouble(csm.floatingRateReference))))
                                                                        .setRateOption(ReferenceWithMetaFloatingRateOption.builder()
                                                                                .setGlobalReference("0"))))
                                                        .setDayCountFraction(FieldWithMetaDayCountFractionEnum.builder()
                                                                .setValue(DayCountFractionEnum.ACT_365_FIXED))
                                                        .setCalculationPeriodDates(CalculationPeriodDates.builder()
                                                                .setEffectiveDate(AdjustableOrRelativeDate.builder()
                                                                        .setAdjustableDate(AdjustableDate.builder()
                                                                                .setUnadjustedDate(cdmUtil.createCdmDateFromShortDateString(csm.effectiveDate).getValue())
                                                                                .setDateAdjustments(BusinessDayAdjustments.builder()
                                                                                        .setBusinessDayConvention(BusinessDayConventionEnum.NONE))))
                                                                .setTerminationDate(AdjustableOrRelativeDate.builder()
                                                                        .setAdjustableDate(AdjustableDate.builder()
                                                                                .setUnadjustedDate(cdmUtil.createCdmDateFromShortDateString(csm.maturityDate).getValue())
                                                                                .setDateAdjustments(BusinessDayAdjustments.builder()
                                                                                        .setBusinessDayConvention(BusinessDayConventionEnum.MODFOLLOWING))))
                                                                .setCalculationPeriodDatesAdjustments(BusinessDayAdjustments.builder()
                                                                        .setBusinessCenters(BusinessCenters.builder()
                                                                                .setBusinessCentersReference(ReferenceWithMetaBusinessCenters.builder()
                                                                                        .setExternalReference("floatPrimaryBusinessCenters"))))
                                                                .setCalculationPeriodFrequency(CalculationPeriodFrequency.builder()
                                                                        .setPeriodMultiplier(3)
                                                                        .setPeriod(PeriodExtendedEnum.M)
                                                                        .setRollConvention(RollConventionEnum._28)))
                                                        .setPaymentDates(PaymentDates.builder()
                                                                .setPaymentFrequency(Frequency.builder()
                                                                        .setPeriodMultiplier(3)
                                                                        .setPeriod(PeriodExtendedEnum.M)))
                                                        .setResetDates(ResetDates.builder()
                                                                .setCalculationPeriodDatesReference(ReferenceWithMetaCalculationPeriodDates.builder()
                                                                        .setGlobalReference("FLOAT_DATES_REFERENCE")
                                                                        .setExternalReference("floatingCalcPeriodDates"))
                                                                .setFixingDates(RelativeDateOffset.builder()
                                                                        .setPeriodMultiplier(-1)
                                                                        .setPeriod(PeriodEnum.D)
                                                                        .setMeta(MetaFields.builder()
                                                                                .setGlobalKey(MetaFields.builder().getGlobalKey())))
                                                                .setResetDatesAdjustments(BusinessDayAdjustments.builder()
                                                                        .setBusinessDayConvention(BusinessDayConventionEnum.MODFOLLOWING)
                                                                        .setBusinessCenters(BusinessCenters.builder()
                                                                                .setBusinessCentersReference(ReferenceWithMetaBusinessCenters.builder()
                                                                                        .setExternalReference("floatPrimaryBusinessCenters")
                                                                                        .setGlobalReference(MetaFields.builder()
                                                                                                .getGlobalKey())))
                                                                        .setMeta(MetaFields.builder()
                                                                                .setExternalKey("floatingLegResetDates")
                                                                                .setGlobalKey(MetaFields.builder().getGlobalKey()))))
                                                                .setCashflowRepresentation(CashflowRepresentation.builder()
                                                                        .setCashflowsMatchParameters(Boolean.TRUE)
                                                                        .addPaymentCalculationPeriod(PaymentCalculationPeriod.builder()
                                                                                        .setAdjustedPaymentDate(cdmUtil.createCdmDateFromShortDateString(csm.effectiveDate).getValue())
                                                                                        .setCalculationPeriod(List.of(CalculationPeriod.builder()
                                                                                                .setAdjustedStartDate(cdmUtil.createCdmDateFromShortDateString(csm.effectiveDate).getValue())
                                                                                                .setAdjustedEndDate(cdmUtil.createCdmDateFromShortDateString(csm.maturityDate).getValue())
                                                                                                .setMeta(MetaFields.builder()
                                                                                                        .setGlobalKey(MetaFields.builder().getGlobalKey()))
                                                                                                .setNotionalAmount(BigDecimal.valueOf(parseDouble(csm.quantity)))
                                                                                                                .setFloatingRateDefinition(FloatingRateDefinition.builder()
                                                                                                                        .setRateObservation(List.of(RateObservation.builder()
                                                                                                                                        .setAdjustedFixingDate(cdmUtil.createCdmDateFromShortDateString(csm.effectiveDate).getValue())
                                                                                                                                        .setObservedRate(BigDecimal.valueOf(parseDouble(csm.floatingRateReference.toString())))
                                                                                                                                        .setMeta(MetaFields.builder()
                                                                                                                                                .setGlobalKey(MetaFields.builder().getGlobalKey()))))))))
                                                                        .addPaymentCalculationPeriod(PaymentCalculationPeriod.builder()
                                                                                                .setAdjustedPaymentDate(cdmUtil.createCdmDateFromShortDateString(csm.effectiveDate).getValue())
                                                                                                .setCalculationPeriod(List.of(CalculationPeriod.builder()
                                                                                                        .setAdjustedStartDate(cdmUtil.createCdmDateFromShortDateString(csm.effectiveDate).getValue())
                                                                                                        .setAdjustedEndDate(cdmUtil.createCdmDateFromShortDateString(csm.effectiveDate).getValue())
                                                                                                        .setMeta(MetaFields.builder()
                                                                                                                .setGlobalKey(MetaFields.builder().getGlobalKey()))
                                                                                                .setNotionalAmount(BigDecimal.valueOf(parseDouble(csm.quantity)))
                                                                                                .setFloatingRateDefinition(FloatingRateDefinition.builder()
                                                                                                        .setRateObservation(List.of(RateObservation.builder()
                                                                                                                .setAdjustedFixingDate(cdmUtil.createCdmDateFromShortDateString(csm.effectiveDate).getValue())
                                                                                                                .setObservedRate(BigDecimal.valueOf(parseDouble(csm.floatingRateReference.toString())))
                                                                                                                .setMeta(MetaFields.builder()
                                                                                                                        .setGlobalKey(MetaFields.builder().getGlobalKey()))))))))))
                                        .addInterestRatePayout(InterestRatePayout.builder()
                                                .setPriceQuantity(ResolvablePriceQuantity.builder()
                                                        .setQuantitySchedule(ReferenceWithMetaNonNegativeQuantitySchedule.builder()
                                                                .setReference(Reference.builder()
                                                                        .setScope("DOCUMENT")
                                                                        .setReference("quantity-2"))))
                                                .setPayerReceiver(PayerReceiver.builder()
                                                        .setPayer(CounterpartyRoleEnum.PARTY_1)
                                                        .setReceiver(CounterpartyRoleEnum.PARTY_2))
                                                .setSettlementTerms(SettlementTerms.builder()
                                                        .setSettlementType(SettlementTypeEnum.CASH)
                                                        .setSettlementCurrencyValue("USD"))
                                                .setRateSpecification(RateSpecification.builder()
                                                        .setFixedRate(FixedRateSpecification.builder()
                                                                .setRateSchedule(RateSchedule.builder()
                                                                        .setPrice(ReferenceWithMetaPriceSchedule.builder()
                                                                                .setReference(Reference.builder()
                                                                                        .setScope("DOCUMENT")
                                                                                        .setReference("price-1"))
                                                                                .setValue(Price.builder()
                                                                                        .setValue(BigDecimal.valueOf(parseDouble(csm.fixedRate)))
                                                                                        .setUnit(UnitType.builder().setCurrencyValue("USD"))
                                                                                        .setPerUnitOf(UnitType.builder().setCurrencyValue("USD"))
                                                                                        .setPriceType(PriceTypeEnum.INTEREST_RATE))))))
                                                .setDayCountFraction(FieldWithMetaDayCountFractionEnum.builder()
                                                        .setValue(DayCountFractionEnum.ACT_365_FIXED))

                                                .setCalculationPeriodDates(CalculationPeriodDates.builder()
                                                        .setEffectiveDate(AdjustableOrRelativeDate.builder()
                                                                .setAdjustableDate(AdjustableDate.builder()
                                                                        .setUnadjustedDate(cdmUtil.createCdmDateFromShortDateString(csm.effectiveDate).getValue())
                                                                        .setDateAdjustments(BusinessDayAdjustments.builder()
                                                                                .setBusinessDayConvention(BusinessDayConventionEnum.NONE))))
                                                        .setTerminationDate(AdjustableOrRelativeDate.builder()
                                                                .setAdjustableDate(AdjustableDate.builder()
                                                                        .setUnadjustedDate(cdmUtil.createCdmDateFromShortDateString(csm.maturityDate).getValue())
                                                                        .setDateAdjustments(BusinessDayAdjustments.builder()
                                                                                .setBusinessDayConvention(BusinessDayConventionEnum.MODFOLLOWING))))
                                                        .setCalculationPeriodDatesAdjustments(BusinessDayAdjustments.builder()
                                                                .setBusinessCenters(BusinessCenters.builder()
                                                                        .setBusinessCentersReference(ReferenceWithMetaBusinessCenters.builder()
                                                                                .setExternalReference("floatPrimaryBusinessCenters"))))
                                                        .setCalculationPeriodFrequency(CalculationPeriodFrequency.builder()
                                                                .setPeriodMultiplier(3)
                                                                .setPeriod(PeriodExtendedEnum.M)
                                                                .setRollConvention(RollConventionEnum._28)))
                                                .setPaymentDates(PaymentDates.builder()
                                                        .setPayRelativeTo(PayRelativeToEnum.CALCULATION_PERIOD_END_DATE)
                                                        .setPaymentFrequency(Frequency.builder()
                                                                .setPeriodMultiplier(3)
                                                                .setPeriod(PeriodExtendedEnum.M)
                                                                .setMeta(MetaFields.builder()
                                                                        .setGlobalKey("aa")))
                                                        .setPaymentDatesAdjustments(BusinessDayAdjustments.builder()
                                                                .setBusinessDayConvention(BusinessDayConventionEnum.MODFOLLOWING)
                                                                .setBusinessCenters(BusinessCenters.builder()
                                                                        .addBusinessCenterValue(BusinessCenterEnum.USNY))
                                                                .setMeta(MetaFields.builder()
                                                                        .setExternalKey("fixedPrimaryBusinessCenters")
                                                                        .setGlobalKey(MetaFields.builder().getGlobalKey())))
                                                        .setMeta(MetaFields.builder()
                                                                .setGlobalKey(MetaFields.builder().getGlobalKey()))
                                                .setMeta(MetaFields.builder()
                                                        .setGlobalKey(MetaFields.builder().getGlobalKey())))

                                                .setCashflowRepresentation(createFixedRateCashFlowRepresenation(csm.effectiveDate, csm.maturityDate, csm.quantity, csm.fixedRate))))))
                                                .build();


        return product;
    }

    public PriceQuantity createPriceQuantity(ClearedSwapModel csm){

        PriceQuantity priceQuantity = PriceQuantity.builder()
                .setQuantity(List.of(FieldWithMetaNonNegativeQuantitySchedule.builder()
                                .setValue(NonNegativeQuantitySchedule.builder()
                                        .setValue(BigDecimal.valueOf(parseDouble(csm.quantity)))
                                        .setUnit(UnitType.builder()
                                                .setCurrencyValue("USD")))
                                .setMeta(MetaFields.builder())))

                .build();


        return priceQuantity;




    }


    public String cancelClearedSwap(
        String tradeStateStr,
        String effectiveDateStr,
        String eventDateStr
	) throws JsonProcessingException {

            IcmaRepoUtil ru = new IcmaRepoUtil();


            ObjectMapper rosettaObjectMapper = RosettaObjectMapper.getNewRosettaObjectMapper();
            TradeState tradeStateObj = new TradeState.TradeStateBuilderImpl();
            TradeState tradeState  = rosettaObjectMapper.readValue(tradeStateStr, tradeStateObj.getClass());

            Create_QuantityChange qc = new Create_QuantityChange.Create_QuantityChangeDefault();
            Injector injector = Guice.createInjector(new CdmRuntimeModule());
            injector.injectMembers(qc);

            QuantityChangeInstruction qci = QuantityChangeInstruction.builder()
                    .setDirection(QuantityChangeDirectionEnum.REPLACE)
                    .setChange(List.of(PriceQuantity.builder()
                                    .setQuantityValue(List.of(NonNegativeQuantitySchedule.builder()
                                                    .setValue(BigDecimal.valueOf(0.00))))))
                    .build();

            TradeState newTradeState = qc.evaluate(qci,tradeState);

            PrimitiveInstruction pi = PrimitiveInstruction.builder()
                    .setQuantityChange(qci)
                    .build();

            List<Instruction> instructionList = List.of(Instruction.builder()
                            .setBefore(ReferenceWithMetaTradeState.builder()
                                    .setValue(tradeState))
                            .setPrimitiveInstruction(pi)
                            .build());

            Create_BusinessEvent be= new Create_BusinessEvent.Create_BusinessEventDefault();
            injector.injectMembers(be);

            Date effectiveDate = ru.createCDMDate(effectiveDateStr);
            Date eventDate = ru.createCDMDate(eventDateStr);
            BusinessEvent businessEvent = be.evaluate(instructionList, null, eventDate, effectiveDate);

            String repoBusinesseventJson = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(businessEvent);

            return repoBusinesseventJson;


    }
    public String settleClearedSwap(String tradeState){

        String businessEventJson=null;

        return businessEventJson;


    }

    public String closeoutClearedSwap(String tradeState){

        String businessEventJson=null;

        return businessEventJson;


    }

    public String fullTerminationClearedSwap(String tradeState){

        String businessEventJson=null;

        return businessEventJson;


    }


    public CashflowRepresentation createFixedRateCashFlowRepresenation (String effectiveDateStr,
                                                               String terminationDateStr,
                                                               String notionalAmount,
                                                               String fixedRate
                                                               ) {


        String paymentDateStr = "";
        String previousDateTimeStr;

        effectiveDateStr = effectiveDateStr + "T00:00:00.000+00:00";
        terminationDateStr = terminationDateStr + "T00:00:00.000+00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        ZonedDateTime effectiveDateTime = ZonedDateTime.parse(effectiveDateStr, formatter);
        ZonedDateTime terminationDateTime = ZonedDateTime.parse(terminationDateStr, formatter);
        ZonedDateTime previousDateTime = effectiveDateTime;
        ZonedDateTime nextDateTime = effectiveDateTime;
        ZonedDateTime paymentDate = effectiveDateTime;
        CdmUtil cdmUtil = new CdmUtil();

        List<PaymentCalculationPeriod> paymentCalculationPeriodList = new ArrayList<PaymentCalculationPeriod>();
        PaymentCalculationPeriod paymentCalculationPeriod = null;


        while (nextDateTime.isBefore(terminationDateTime)) {
            paymentDate = nextDateTime;
            previousDateTimeStr = previousDateTime.format(formatter);
            paymentDateStr = paymentDate.format(formatter);

            paymentCalculationPeriod = PaymentCalculationPeriod.builder()
                    .setAdjustedPaymentDate(cdmUtil.createCdmDateFromLongDateStringNoMeta(paymentDateStr))
                    .setCalculationPeriod(List.of(CalculationPeriod.builder()
                            .setAdjustedStartDate(cdmUtil.createCdmDateFromLongDateStringNoMeta(previousDateTimeStr))
                            .setAdjustedEndDate(cdmUtil.createCdmDateFromLongDateStringNoMeta(paymentDateStr))
                            .setMeta(MetaFields.builder()
                                    .setGlobalKey(MetaFields.builder().getGlobalKey()))
                            .setNotionalAmount(BigDecimal.valueOf(parseDouble(notionalAmount)))
                            .setFixedRate(BigDecimal.valueOf(parseDouble(fixedRate)))))
                    .setMeta(MetaFields.builder()
                            .setGlobalKey(MetaFields.builder().getGlobalKey()))
                    .build();

            paymentCalculationPeriodList.add(paymentCalculationPeriod);
            previousDateTime = nextDateTime;
            nextDateTime = nextDateTime.plusYears(1);


        }

        paymentDate = nextDateTime;
        previousDateTimeStr = previousDateTime.format(formatter);
        paymentDateStr = paymentDate.format(formatter);

        CashflowRepresentation cashflowRepresentation = CashflowRepresentation.builder()
                .setCashflowsMatchParameters(true)
                .setPaymentCalculationPeriod(paymentCalculationPeriodList)
                .addPaymentCalculationPeriod(PaymentCalculationPeriod.builder()
                        .setAdjustedPaymentDate(cdmUtil.createCdmDateFromLongDateStringNoMeta(paymentDateStr))
                        .setCalculationPeriod(List.of(CalculationPeriod.builder()
                                .setAdjustedStartDate(cdmUtil.createCdmDateFromLongDateStringNoMeta(previousDateTimeStr))
                                .setAdjustedEndDate(cdmUtil.createCdmDateFromLongDateStringNoMeta(paymentDateStr))
                                .setMeta(MetaFields.builder()
                                        .setGlobalKey(MetaFields.builder().getGlobalKey()))
                                .setNotionalAmount(BigDecimal.valueOf(parseDouble(notionalAmount)))
                                .setFixedRate(BigDecimal.valueOf(parseDouble(fixedRate)))
                                .setMeta(MetaFields.builder()
                                        .setGlobalKey(MetaFields.builder().getGlobalKey())))))
                .build();

        return cashflowRepresentation;
    }

    public String createResetEvent (String tradeStateStr, String resetDate, String effectiveDateStr,
                                    String eventDateStr) throws JsonProcessingException {

        IcmaRepoUtil ru = new IcmaRepoUtil();


        ObjectMapper rosettaObjectMapper = RosettaObjectMapper.getNewRosettaObjectMapper();
        TradeState tradeStateObj = new TradeState.TradeStateBuilderImpl();
        TradeState tradeState  = rosettaObjectMapper.readValue(tradeStateStr, tradeStateObj.getClass());

        Create_Reset create_reset = new Create_Reset.Create_ResetDefault();
        Injector injector = Guice.createInjector(new CdmRuntimeModule());
        injector.injectMembers(create_reset);

        Date effectiveDate = ru.createCDMDate(effectiveDateStr);
        Date eventDate = ru.createCDMDate(eventDateStr);
        ResetInstruction ri = ResetInstruction.builder()
                .setPayout(ReferenceWithMetaPayout.builder()
                        .setValue(tradeState.getTrade().getTradableProduct().getProduct().getContractualProduct().getEconomicTerms().getPayout()))
                .setResetDate(eventDate)
                .build();


        TradeState newTradeState = create_reset.evaluate(ri,tradeState);

        PrimitiveInstruction pi = PrimitiveInstruction.builder()
                .setReset(ri)
                .build();

        List<Instruction> instructionList = List.of(Instruction.builder()
                .setBefore(ReferenceWithMetaTradeState.builder()
                        .setValue(tradeState))
                .setPrimitiveInstruction(pi)
                .build());

        Create_BusinessEvent be= new Create_BusinessEvent.Create_BusinessEventDefault();
        injector.injectMembers(be);


        BusinessEvent businessEvent = be.evaluate(instructionList, null, eventDate, effectiveDate);

        String repoBusinesseventJson = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(businessEvent);

        return repoBusinesseventJson;
    }

    public String closeoutClearedSwap(
            String tradeStateStr,
            String effectiveDateStr,
            String eventDateStr
    ) throws JsonProcessingException {

        IcmaRepoUtil ru = new IcmaRepoUtil();


        ObjectMapper rosettaObjectMapper = RosettaObjectMapper.getNewRosettaObjectMapper();
        TradeState tradeStateObj = new TradeState.TradeStateBuilderImpl();
        TradeState tradeState  = rosettaObjectMapper.readValue(tradeStateStr, tradeStateObj.getClass());

        Create_QuantityChange qc = new Create_QuantityChange.Create_QuantityChangeDefault();
        Injector injector = Guice.createInjector(new CdmRuntimeModule());
        injector.injectMembers(qc);

        QuantityChangeInstruction qci = QuantityChangeInstruction.builder()
                .setDirection(QuantityChangeDirectionEnum.REPLACE)
                .setChange(List.of(PriceQuantity.builder()
                        .setQuantityValue(List.of(NonNegativeQuantitySchedule.builder()
                                .setValue(BigDecimal.valueOf(0.00))))))
                .build();

        TradeState newTradeState = qc.evaluate(qci,tradeState);

        PrimitiveInstruction pi = PrimitiveInstruction.builder()
                .setQuantityChange(qci)
                .build();

        List<Instruction> instructionList = List.of(Instruction.builder()
                .setBefore(ReferenceWithMetaTradeState.builder()
                        .setValue(tradeState))
                .setPrimitiveInstruction(pi)
                .build());

        Create_BusinessEvent be= new Create_BusinessEvent.Create_BusinessEventDefault();
        injector.injectMembers(be);

        Date effectiveDate = ru.createCDMDate(effectiveDateStr);
        Date eventDate = ru.createCDMDate(eventDateStr);
        BusinessEvent businessEvent = be.evaluate(instructionList, null, eventDate, effectiveDate);

        String repoBusinesseventJson = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(businessEvent);

        return repoBusinesseventJson;


    }

    public FloatingAmountCalculationDetails createFloatingAmount(
            InterestRatePayout interestRatePayout,
            CalculationPeriodBase calculationPeriodBase,
            Boolean initialPeriod,
            BigDecimal notional,
            BigDecimal rate
    ){

        ObjectMapper rosettaObjectMapper = RosettaObjectMapper.getNewRosettaObjectMapper();

        FloatingAmountCalculation fc = new FloatingAmountCalculation.FloatingAmountCalculationDefault();
        Injector injector = Guice.createInjector(new CdmRuntimeModule());
        injector.injectMembers(fc);

        FloatingAmountCalculationDetails fcd = new FloatingAmountCalculationDetails.FloatingAmountCalculationDetailsBuilderImpl();
        fcd = fc.evaluate(interestRatePayout, calculationPeriodBase, initialPeriod, notional,  rate);

        return fcd;

    }
}
