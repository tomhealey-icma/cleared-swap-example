/*******************************************************************************
 * Copyright (c) icmagroup.org  All rights reserved.
 *
 * This file is part of the International Capital Market Association (ICMA)
 * CDM for Repo and Bonds Demo
 *
 * This file is intended for demo purposes only and may not be distributed
 * or used in any commercial capacity other than its intended purpose.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 *
 * Prepared by FINXIS LLC, New York, NY.
 *
 * Contact International Capital Market Association (ICMA),110 Cannon Street,
 * London EC4N 6EU, ph. +44 20 7213 0310, if you have any questions.
 *
 ******************************************************************************/

package com.finxis.cdm.crossproductapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import cdm.base.datetime.*;
import cdm.base.datetime.AdjustableDate;
import cdm.base.datetime.AdjustableDates;
import cdm.base.datetime.AdjustableOrRelativeDate;
import cdm.base.datetime.daycount.DayCountFractionEnum;
import cdm.base.datetime.daycount.metafields.FieldWithMetaDayCountFractionEnum;
import cdm.base.datetime.metafields.FieldWithMetaBusinessCenterEnum;
import cdm.base.datetime.metafields.ReferenceWithMetaBusinessCenters;
import cdm.base.math.NonNegativeQuantitySchedule;
import cdm.base.math.UnitType;
import cdm.base.math.metafields.FieldWithMetaNonNegativeQuantitySchedule;
import cdm.base.math.metafields.ReferenceWithMetaNonNegativeQuantitySchedule;
import cdm.base.staticdata.identifier.AssignedIdentifier;
import cdm.base.staticdata.identifier.Identifier;
import cdm.base.staticdata.identifier.TradeIdentifierTypeEnum;
import cdm.base.staticdata.party.*;
import cdm.base.staticdata.party.metafields.ReferenceWithMetaParty;
import cdm.base.staticdata.asset.common.metafields.FieldWithMetaProductIdentifier;
import cdm.base.staticdata.asset.common.*;
import cdm.event.common.ExecutionInstruction;
import cdm.event.common.Trade;
import cdm.event.common.TradeIdentifier;
import cdm.event.common.ExecutionDetails;
import cdm.event.common.*;
import cdm.observable.asset.Observable;
import cdm.product.asset.*;
import cdm.product.collateral.*;
import cdm.product.common.schedule.CalculationPeriodDates;
import cdm.product.common.schedule.PayRelativeToEnum;
import cdm.product.common.schedule.PaymentDates;
import cdm.product.common.schedule.RateSchedule;
import cdm.product.common.settlement.ResolvablePriceQuantity;
import cdm.observable.asset.*;
import cdm.observable.asset.metafields.FieldWithMetaPriceSchedule;
import cdm.observable.asset.FloatingRateOption;
import cdm.observable.asset.Price;
import cdm.observable.asset.PriceTypeEnum;
import cdm.observable.asset.metafields.ReferenceWithMetaPriceSchedule;
import cdm.product.common.settlement.*;
import cdm.product.template.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regnosys.rosetta.common.hashing.GlobalKeyProcessStep;
import com.regnosys.rosetta.common.hashing.NonNullHashCollector;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.rosetta.model.lib.GlobalKey;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.meta.Key;
import com.rosetta.model.lib.meta.Reference;
import com.rosetta.model.lib.process.PostProcessStep;
import com.rosetta.model.metafields.FieldWithMetaDate;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.model.metafields.MetaFields;
import com.rosetta.model.lib.records.Date;
import org.joda.time.DateTime;


import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.rosetta.model.lib.records.Date.of;


import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IcmaRepoUtil {


	private final PostProcessStep keyProcessor;

	public IcmaRepoUtil() {
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

	private TradeState tradeState;


	public FieldWithMetaDate createTradeDate(int y, int m, int d) {

		Date dt;
		dt = Date.of(y, m, d);

		return FieldWithMetaDate.builder().setValue(dt).build();


	}

	public TradeIdentifier createRepoTradeIdentifier(String identifier, String scheme, String issuer) {

		return TradeIdentifier.builder().addAssignedIdentifier(
						AssignedIdentifier.builder().setIdentifier(
										FieldWithMetaString.builder().setValue(identifier)
												.setMeta(MetaFields.builder()
														.setScheme(scheme)
														.build())
												.build())
								.build())
				.setIssuer(FieldWithMetaString.builder()
						.setValue(issuer)
						.build())
				.setIdentifierType(TradeIdentifierTypeEnum.valueOf("UNIQUE_TRANSACTION_IDENTIFIER"))
				.build();
	}

	public Party createRepoParty(String partyId, String scheme, String pName) {

		Party party;


		if ((partyId == null) || (partyId.isEmpty()) || (partyId.isEmpty())  || (partyId.equals("")) && (pName.equals("")))
			party = null;
		else{
			party = addGlobalKey(Party.class,
					Party.builder()
							.addPartyId(PartyIdentifier.builder()
									.setIdentifierValue(partyId)
									.setMeta(MetaFields.builder()
											.setScheme(scheme).build())
									.build())
							.setNameValue(pName)
							.build());
		}
		return party;
	}

	public PartyRole createRepoPartyRole(Party party, String reference, String role) {

		PartyRole partyRole;

		if (party == null )
			partyRole = null;
		else {
			partyRole = PartyRole.builder()
					.setPartyReference(ReferenceWithMetaParty.builder()
							.setGlobalReference(getGlobalReference(party))
							.setExternalReference(reference)
							.build())
					.setRole(PartyRoleEnum.valueOf(role))
					.build();
		}
		return partyRole;
	}

	public Counterparty createRepoCounterparty(Party party, String role) {

		return Counterparty.builder()
				.setPartyReferenceValue(party)
				.setRole(CounterpartyRoleEnum.valueOf(role))
				.build();
	}

	public List<PriceQuantity> createRepoPriceQuantity() {

		PriceQuantity pc = null;

		return List.of(pc, pc);
	}

	public List<PriceQuantity> createRepoPriceQuantity(
			String cashQuantityStr,
			String cashCurrencyStr,
			String collateralISINStr,
			String collateralQuantityStr,
			String collateralCurrencyStr,
			String repoRateStr,
			String haircutStr,
			String collateralCleanPriceStr,
			String collateralDirtyPriceStr
	) throws JsonProcessingException {

		double cashQuantity = Double.parseDouble(cashQuantityStr);
		double collateralQuantity = Double.parseDouble(collateralQuantityStr);
		double repoRate = Double.parseDouble(repoRateStr);
		double haircut = Double.parseDouble(haircutStr);
		double collateralCleanPrice = Double.parseDouble(collateralCleanPriceStr);
		double collateralDirtyPrice = Double.parseDouble(collateralDirtyPriceStr);

		PriceQuantity loanPriceQuantity = addGlobalKey(PriceQuantity.class,
				createLoanPriceQuantity(cashCurrencyStr, cashQuantity, collateralCurrencyStr, collateralQuantity, collateralCleanPrice, collateralDirtyPrice, repoRate, collateralISINStr, ""));

		PriceQuantity collateralPriceQuantity = addGlobalKey(PriceQuantity.class,
				createCollateralPriceQuantity(cashCurrencyStr, cashQuantity, collateralCurrencyStr, collateralQuantity, collateralCleanPrice, collateralDirtyPrice, repoRate, collateralISINStr, ""));

		List<PriceQuantity> repoPriceQuantity = List.of(loanPriceQuantity, collateralPriceQuantity);

		return repoPriceQuantity;
	}

	public PriceQuantity createLoanPriceQuantity(
			String cashCurrencyStr,
			double cashQuantity,
			String collateralCurrencyStr,
			double collateralQuantity,
			double collteralCleanPrice,
			double collateralDirtyPrice,
			double rate,
			String collateralISINStr,
			String scheme) throws JsonProcessingException {

		return PriceQuantity.builder()
				// Set cash amount and rate
				.addPrice(FieldWithMetaPriceSchedule.builder()
						.setMeta(MetaFields.builder().setScheme(scheme).build())
						.setValue(Price.builder()
								.setUnit(UnitType.builder()
										.setCurrencyValue(cashCurrencyStr)))
						.setValue(Price.builder()
								.setValue(BigDecimal.valueOf(rate))
								.setUnit(UnitType.builder()
										.setCurrencyValue(cashCurrencyStr))
								.setPerUnitOf(UnitType.builder()
										.setCurrencyValue(cashCurrencyStr))
								.setPriceType(PriceTypeEnum.INTEREST_RATE))
						.build())

				.addQuantity(FieldWithMetaNonNegativeQuantitySchedule.builder()
						.setValue(NonNegativeQuantitySchedule.builder()
								.setValue(BigDecimal.valueOf(cashQuantity))
								.setUnit(UnitType.builder()
										.setCurrencyValue(cashCurrencyStr)))
						.build())

				.build();

	}


	private PriceQuantity createCollateralPriceQuantity(
			String cashCurrencyStr,
			double cashQuantity,
			String collateralCurrencyStr,
			double collateralQuantity,
			double collteralCleanPrice,
			double collateralDirtyPrice,
			double rate,
			String collateralISINStr,
			String scheme) throws JsonProcessingException {

		return PriceQuantity.builder()
				// Set cash amount and rate
				.setObservable(Observable.builder()
						.addProductIdentifierValue(ProductIdentifier.builder()
								.setIdentifierValue(collateralISINStr)
								.setSource(ProductIdTypeEnum.ISIN)
								.build()))
				// Set collateral amount and price
				.addPrice(FieldWithMetaPriceSchedule.builder()
						.setValue(Price.builder()
								.setValue(BigDecimal.valueOf(collateralDirtyPrice))
								.setUnit(UnitType.builder()
										.setCurrencyValue(collateralCurrencyStr))
								.setPerUnitOf(UnitType.builder()
										.setCurrencyValue(collateralCurrencyStr))
								.setPriceType(PriceTypeEnum.ASSET_PRICE)
								.setPriceExpression(PriceExpressionEnum.PERCENTAGE_OF_NOTIONAL))
						.build())

				.addQuantity(FieldWithMetaNonNegativeQuantitySchedule.builder()
						.setValue(NonNegativeQuantitySchedule.builder()
								.setValue(BigDecimal.valueOf(collateralQuantity))
								.setUnit(UnitType.builder()
										.setCurrencyValue(collateralCurrencyStr)))
						.build())

				.build();

	}

	private List<EligibleCollateralSpecification> createEligibleCollateralSpecification(
			String collateralId,
			String collateralCurrencyStr) {

		return List.of(
				EligibleCollateralSpecification.builder()
						.addIdentifier(Identifier.builder()
								.setAssignedIdentifier(List.of(AssignedIdentifier.builder()
										.setIdentifier(FieldWithMetaString.builder()
												.setValue(collateralId)
												.setMeta(MetaFields.builder()
														.setGlobalKey(collateralId)))))
								.build())
						.addCriteria(EligibleCollateralCriteria.builder()
								.setAsset(List.of(
										AssetCriteria.builder()
												.addDenominatedCurrency(CurrencyCodeEnum.valueOf(collateralCurrencyStr))
												.addCollateralAssetType(
														AssetType.builder()
																.setAssetType(AssetTypeEnum.SECURITY)
																.setDebtType(DebtType.builder()
																		.setDebtClass(DebtClassEnum.VANILLA)))
												.build()))
								.setIssuer(List.of(
										IssuerCriteria.builder()
												.addIssuerType(CollateralIssuerType.builder()
														.setIssuerType(IssuerTypeEnum.SOVEREIGN_CENTRAL_BANK))
												.addIssuerAgencyRating(List.of(
														AgencyRatingCriteria.builder()
																.addCreditNotation(CreditNotation.builder()
																		.setAgency(CreditRatingAgencyEnum.FITCH)
																		.setNotation(FieldWithMetaString.builder()
																				.setValue("A-")
																				.build())
																		.build()))
												)
												.build())))

						.build()

		);

	}

	public void writeEventToFile(String eventName, String eventId, String data) throws IOException {

		String userDirectory = Paths.get("")
				.toAbsolutePath()
				.toString();
		File udir = new File(userDirectory);
		File pudir = udir.getParentFile();
		String eventlogs = udir.getPath() + "/eventlogs";

		File logFile = new File(eventlogs);
		final File log_directory = logFile.getAbsoluteFile();
		if (null != log_directory) {
			log_directory.mkdirs();
		}

		File eventfile = new File(eventlogs + "/" + eventName + "_" + eventId + ".txt");
		FileWriter fr = new FileWriter(eventfile);

		try {
			boolean result = eventfile.exists();
			if (result) {
				fr.write(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//close resources
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("writing event file output:" + eventfile);

	}

	public AdjustableOrRelativeDate createAdjustableOrRelativeDate(String dateStr) {


		//Extract only the data in case dateStr includes time or other characters
		String pm = "([0-9]{4}-[0-9]{2}-[0-9]{2})";
		Pattern pattern = Pattern.compile(pm);
		Matcher matcher = pattern.matcher(dateStr);
		matcher.find();
		dateStr = matcher.group(1) + "T00:00:00.000+00:00";

		//Set the termination date
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz");
		ZonedDateTime zdtWithZoneOffset = ZonedDateTime.parse(dateStr, formatter);
		ZonedDateTime zdtInLocalTimeline = zdtWithZoneOffset.withZoneSameInstant(ZoneId.systemDefault());

		Date terminationDate = Date.of(zdtWithZoneOffset.getYear(), zdtWithZoneOffset.getMonthValue(), zdtWithZoneOffset.getDayOfMonth());

		AdjustableOrRelativeDate adjustableOrRelativeDateTerminationDateDate = AdjustableOrRelativeDate.builder()
				.setAdjustableDate(AdjustableDate.builder()
						.setUnadjustedDate(terminationDate)
						.setDateAdjustments(BusinessDayAdjustments.builder()
								.setBusinessDayConvention(BusinessDayConventionEnum.NONE)));

		return adjustableOrRelativeDateTerminationDateDate;
	}

	public AdjustableOrAdjustedOrRelativeDate createAdjustableOrAdjustedOrRelativeDate(String dateStr) {

		//Extract only the data in case dateStr includes time or other characters
		String pm = "([0-9]{4}-[0-9]{2}-[0-9]{2})";
		Pattern pattern = Pattern.compile(pm);
		Matcher matcher = pattern.matcher(dateStr);
		matcher.find();
		dateStr = matcher.group(1) + "T00:00:00.000+00:00";

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz");
		ZonedDateTime zdtWithZoneOffset = ZonedDateTime.parse(dateStr , formatter);
		ZonedDateTime zdtInLocalTimeline = zdtWithZoneOffset.withZoneSameInstant(ZoneId.systemDefault());

		Date adjDate = Date.of(zdtWithZoneOffset.getYear(), zdtWithZoneOffset.getMonthValue(), zdtWithZoneOffset.getDayOfMonth());


		AdjustableOrAdjustedOrRelativeDate adjustableOrAdjustedOrRelativeDate = AdjustableOrAdjustedOrRelativeDate.builder()
				.setUnadjustedDate(adjDate)
				.setDateAdjustments(BusinessDayAdjustments.builder()
						.setBusinessDayConvention(BusinessDayConventionEnum.NONE))
				.build();

		return adjustableOrAdjustedOrRelativeDate;
	}


	public Date createCDMDate(String dateStr) {

		//Extract only the data in case dateStr includes time or other characters
		String pm = "([0-9]{4}-[0-9]{2}-[0-9]{2})";
		Pattern pattern = Pattern.compile(pm);
		Matcher matcher = pattern.matcher(dateStr);
		matcher.find();
		dateStr = matcher.group(1) + "T00:00:00.000+00:00";

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz");
		ZonedDateTime zdtWithZoneOffset = ZonedDateTime.parse(dateStr, formatter);
		ZonedDateTime zdtInLocalTimeline = zdtWithZoneOffset.withZoneSameInstant(ZoneId.systemDefault());

		Date cdmDate = Date.of(zdtWithZoneOffset.getYear(), zdtWithZoneOffset.getMonthValue(), zdtWithZoneOffset.getDayOfMonth());

		return cdmDate;
	}

	public String getAfterTradeState(String businessEvent) throws JsonProcessingException {

		ObjectMapper rosettaObjectMapper = RosettaObjectMapper.getNewRosettaObjectMapper();
		BusinessEvent beobj = new BusinessEvent.BusinessEventBuilderImpl();
		BusinessEvent be = rosettaObjectMapper.readValue(businessEvent, beobj.getClass());

		TradeState tradeState = be.getAfter().get(0);
		String tsjson = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(tradeState);

		return tsjson;
		//String deserializedObject = String.valueOf(rosettaObjectMapper.readValue(tsjson, tradeState.getClass()));

		//return deserializedObject;
	}

	public JComboBox addParties(JComboBox jb) {
		jb.addItem(new CItem("Global Bank Inc", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new CItem("UK Bank plc", "XX6I6TESTEU3UXPYDY54"));
		jb.addItem(new CItem("EU Bank", "MP6I5ZYZBEU3UXPYFY54"));
		jb.addItem(new CItem("Credit Suisse Securities (Europe) Ltd", "DL6FFRRLF74S01HE2M14"));
		jb.addItem(new CItem("XYZ Bank Plc Paris branch", "BG661XYBNEU6ASPGLA12"));
		jb.addItem(new CItem("Hochreutiner Cuckoo Clocks AG", "EM774SWRESA3UXBPIF77"));
		jb.addItem(new CItem("Global Fund Management Ltd (investment/asset manager)", "549300RM34L56MA11M54"));
		jb.addItem(new CItem("Dealer Bank Europe Ltd", "AL61GG34LM12CV28I911"));
		jb.addItem(new CItem("Swiss Reinsurance Company Ltd", "549300WZRVQERM819Z90"));
		jb.addItem(new CItem("MUFG Securities EMEA Plc", "U7M81AY481YLIOR75625"));
		jb.addItem(new CItem("Tullett Liberty Securities LLC", "5493002MIGPVI71S2611"));
		jb.addItem(new CItem("WhiteSands US Treasury Fund ", "4138114CCP90NM2127HG2"));
		jb.addItem(new CItem("BNY Mellon Capital Markets LLC", "VJW2DOOHGDT6PR0ZRO63"));
		jb.addItem(new CItem("LCH SA", "R1IO4YJ0O79SMWVCHB58"));

		return jb;
	}


	public JComboBox addCustomerAccounts(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("A100000", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("A200000", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("A300000", "XX6I5TESTEU3UXPYFY54"));


		return jb;
	}

	public JComboBox addCcps(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("CME", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("ICE", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("LSEG", "XX6I5TESTEU3UXPYFY54"));


		return jb;
	}

	public JComboBox addClearingBrokers(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("ClBk1", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("ClBk2", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("BNP", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("Cantor Fitzgerald", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("Citigroup Capital Markets", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("Goldman Sachs", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("JP Morgan", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("Morgan Stanley", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("UBS Securities", "XX6I5TESTEU3UXPYFY54"));


		return jb;
	}

	public JComboBox addExecutingBrokers(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("ExBk1", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("ExBk2", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("BNP", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("Cantor Fitzgerald", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("Citigroup Capital Markets", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("Goldman Sachs", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("JP Morgan", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("Morgan Stanley", "XX6I5TESTEU3UXPYFY54"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("UBS Securities", "XX6I5TESTEU3UXPYFY54"));


		return jb;
	}

	public JComboBox addIssuanceType(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Programme", "PROGRAMME"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Standalone", "STANDALONE"));

		return jb;
	}

	public JComboBox addStatusOfNoteType(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Senior Secured", "SENIOR_SECURED"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Senior Unsecured", "SENIOR_UNSECURED"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Senior Preferred", "SENIOR_PREFERRED"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Senior Non-Preferred", "SENIOR_NON_PREFERRED"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Subordinate", "SUBORDINATED"));

		return jb;
	}

	public JComboBox addFormOfNoteType(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Bearer", "BEARER"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Registered", "REGISTERED"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Dematerialised", "DEMATERIALISED"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Book Entry", "BOOK_ENTRY"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Collective Debt Registered", "COLLECTIVE_DEBT_REGISTERED_CLAIM"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Dematerialised Bearer", "DEMATERIALISED_BEARER"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Dematerialised Book Entry", "DEMATERIALISED_BOOK_ENTRY"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Dematerialised Registered", "DEMATERIALISED_REGISTERED"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Materialised Bearer", "MATERIALISED_BEARER"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Materialised Registered", "MATERIALISED_REGISTERED"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Uncertificated Book Entry", "UNCERTIFICATED_DEMATERIALISED_BOOK_ENTRY"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Uncertificated Registered", "UNCERTIFICATED_REGISTERED"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Uncertificated", "UNCERTIFIED"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Book Entry", "BOOK_ENTRY"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Uncertificated Dematerialised", "UNCERTIFIED_DEMATERIALISED_BOOK_ENTRY"));

		return jb;
	}

	public JComboBox addSecurities(JComboBox jb) {

		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("US718286AY36", "US718286AY36"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("US715638AP79", "US715638AP79"));

		return jb;
	}

	public JComboBox addReferenceObligation(JComboBox jb) {

		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("789AAEAE3", "789AAEAE3"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("786B9BAB8", "786B9BAB8"));
		jb.addItem(new com.finxis.cdm.crossproductapp.CItem("115CCBAP3", "115CCBAP3"));

		return jb;
	}

	public JComboBox addDltPlatformType(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Local", "LOCAL"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("Main Net", "MAINNET"));

		return jb;
	}

	public JComboBox addCurrency(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("USD","USD"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("GBP","GBP"));
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("EUR","EUR"));
		return jb;
	}

	public JComboBox addListing(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("",""));
		return jb;
	}

	public JComboBox addClearingSystems(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("",""));
		return jb;
	}

	public JComboBox addSettlementSystem(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("",""));
		return jb;
	}

	public JComboBox addMethodOfDistribution(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("",""));
		return jb;
	}

	public JComboBox addGoverningLaw(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("",""));
		return jb;
	}

	public JComboBox addSellingRestrictions(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("",""));
		return jb;
	}

	public JComboBox addPRIIPs(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("",""));
		return jb;
	}

	public JComboBox addManufacturerTargetMarketType(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("",""));
		return jb;
	}

	public JComboBox addDayCountFraction(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("",""));
		return jb;
	}

	public JComboBox addBusinessDayConvention(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("",""));
		return jb;
	}

	public JComboBox addBusinessDayCenter(JComboBox jb) {
		jb.addItem(new com.finxis.cdm.crossproductapp.util.CItem("",""));
		return jb;
	}


}

//Combobox lists
class CItem {
	String label;
	String value;
	String code;
	String scheme;


	public CItem(String label, String value) {
		this.label = label;
		this.value = value;
	}

	public CItem(String label, String value, String code, String scheme){
		this.label = label;
		this.value = value;
		this.code = code;
		this.scheme = scheme;
	}

	public String getClabel() {
			return label;
	}

	public String getCValue(){

		return value;

	}

	@Override
	public String toString() {
		return label;
	}

}

class CdmEnumMap{

	private List<Map.Entry<String, String>> list;
	public Map buildEnumMap(Map<String, String> map) {

		map.put("DVP", "DELIVERY_VERSUS_PAYMENT");
		map.put("TP", "DELIVERY_VERSUS_PAYMENT");
		map.put("FOP", "FREE_OF_PAYMENT");

		map.put("DAYS", "D");
		map.put("WEEKS", "W");
		map.put("MONTHS", "M");
		map.put("YEARS", "Y");

		map.put("DAY", "D");
		map.put("WEEK", "W");
		map.put("MONTH", "M");
		map.put("YEAR", "Y");

		map.put("SONIA", "GBP_SONIA");
		map.put("ESTR", "EUR_EUROSTR");

		map.put("Repurchase Agreement", "REPURCHASE_AGREEMENT");
		map.put("Buy/Sell-Back Agreement", "BUY/SELL_BACK_AGREEMENT");

		map.put("USD-SOFR-COMPOUND","USD_SOFR_COMPOUND");

		return map;
	}

class Currency{

	//Load reference data
	public List<List<String>>  getCcyReferenceData() {

		List<List<String>> records = new ArrayList<>();

		InputStream is = this.getClass().getResourceAsStream("/data/currency.csv");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		{
			String line;

			while (true) {
				try {
					if (!((line = br.readLine()) != null)) break;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				String[] values = line.split(",");
				records.add(Arrays.asList(values));
			}
		}
		return records;
	}
}

class XmlLoader{

		}

class CsvLoader{

}

class FileReader{

	}

class MySQLReader{

}

}


