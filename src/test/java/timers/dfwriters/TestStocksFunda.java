package timers.dfwriters;

import com.google.common.collect.Lists;
import config.CoreConfig;
import dataset.StateToDataframe;
import event.timers.AbstractEventSequencer;
import event.timers.TimerDataframeWriterBatch;
import event.timers.TimerDataframeWriterStream;
import event.timers.TimerStateToDataframe;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.identifiers.Identifier;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.utils.RequestOverrides;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class TestStocksFunda {

	private AbstractEventSequencer<StateToDataframe> sequencer;
	private DataRequest requestComposition;
	private DataRequest requestTechnical;
	private TimerDataframeWriterStream writerStream;
	private TimerDataframeWriterBatch writerBatch;

	Map<String, String[]> fieldsMap = new HashMap<String, String[]>() {{
		/* put("balance_sheet1", new String[]{"BVPS", "ACCOUNTS_RECEIVABLE", "ACCOUNTS_PAYABLE", "CURRENT_ASSETS", "CURRENT_LIABILITIES", "TOT_DPST", "INVENTORY", "NET_FIXED_ASSETS", "RE_ASSET", "TOT_ASSET", "TOT_CAP"});
		put("balance_sheet2", new String[]{"TOT_INSUR_RSRV", "TOT_LIAB", "TOT_LOAN", "EARN_ASSET", "NET_ASSETS", "NET_DEBT", "EMPLOYEE", "TOTAL_DEBT", "NET_WORTH", "TOTAL_EQUITY", "BOE_PRODUCTION"});
		put("income_statement1", new String[]{"CAPEX","EARN_FOR_COM","EBIT","EBITDA","EBITDA_ADJUSTED","CONT_INC","SALES_ADJUSTED","COGS","COMM_FEES_EARN","IS_COMP_SALES","DEPR_EXP","EPS_DILUTED","EPS","EPS_AFTER_XO_ITEMS","INC_BEF_XO_ITEM","INC_TAX_EXP","NET_NON_OPER_LOSS","OPER_INCOME"});
		put("income_statement2", new String[]{"OPER_EXPENSE","PROPERTY_TAX_REVENUE","PROV_FOR_LOAN_LOSS","RD_EXPEND","RENT_INC","TOT_COM_DVD","NET_INCOME","NET_INT_INC","NET_OPER_INCOME","NON_INT_EXP","NON_INT_INC","REINVEST_EARN","SALES_PER_SH","EPS_CONT_OPS_RR_DIL","SALES","SAME_STORE_NOI","INT_EXP","UNDW_COST"});
		put("cash_flow_statement", new String[]{"CASH_FLOW_PER_SH","CF_TO_FIRM","CASH_FLOW","FFO","FREE_CASH_FLOW","FCF_TO_FIRM","NET_CHANGE_IN_CASH","FREE_CASH_FLOW_PER_SH","WORKING_CAPITAL",});
		put("earnings_calendar", new String[]{"ANNOUNCEMENT_DT","LATEST_ANNOUNCEMENT_DT","LATEST_PERIOD_END_DT_FULL_RECORD","FISCAL_YEAR_PERIOD"});
		put("margins", new String[]{
				"RETURN_COM_EQY","RETURN_ON_ASSET","RETURN_ON_CAP","RETURN_ON_INV_CAPITAL",
				"GROSS_MARGIN","EBITDA_TO_REVENUE","OPER_MARGIN",
				"INCREMENTAL_OPERATING_MARGIN","PRETAX_INC_TO_NET_SALES",
				"INC_BEF_XO_ITEMS_TO_NET_SALES","PROF_MARGIN",
				"NET_INCOME_TO_COMMON_MARGIN","EFF_TAX_RATE",
				"DVD_PAYOUT_RATIO","SUSTAIN_GROWTH_RT"});*/
		/*put("normalized_margins", new String[]{
				"NORMALIZED_ROE","NORMALIZED_INCOME","NORMALIZED_ROA","NORMALIZED_ROCE"});*/
		put("analyst_recommendations", new String[]{
				"TOT_ANALYST_REC","TOT_BUY_REC","TOT_SELL_REC","TOT_HOLD_REC","EQY_REC_CONS"});
	}};

	private String fund_per = "Q";
	private String region = "amrs";
	private String index = "RAY";
	private String format="parquet";
	private int batchSize = 4000;
	private boolean replaceWithComposite = true;

	@Before
	public void setup() throws DataQueryException, DataServiceStartException {
		requestComposition = new DataRequest.Builder()
				.dataService(DataServiceEnum.FLAT_FILE)
				.parameters("filepath","C:\\Users\\admin\\Documents\\workspace\\data\\"+format+"\\equity\\"+region+"\\"+index.toLowerCase()+"\\composition")
				.parameters("fileformat",format)
				.instrumentType(InstrumentType.Index)
				.identifierType(IdentifierType.TICKER)
				.identifiers(InstrumentType.Index, new String[]{index})
				.requestType(RequestType.UniverseRequest)
				.parameters(RequestParameters.startDate, CoreConfig.GLOBAL_START_DATE)
				.parameters(RequestParameters.endDate,  CoreConfig.GLOBAL_END_DATE)
				.parameters(RequestParameters.useComposite,replaceWithComposite)
				.build();

		requestComposition.query();

	}

	@Test
	public void testWriter() throws TimeoutException, InterruptedException, DataServiceStartException, DataQueryException {

		for(Map.Entry<String, String[]> entry : fieldsMap.entrySet()) {
			String exportFileName = entry.getKey();
			String[] fields = entry.getValue();
			List<Identifier> idList = CoreConfig.services().instrumentFactory()
					.getInstrumentSet()
					.stream()
					.filter(p -> p.getInstrumentType().equals(InstrumentType.SingleStock))
					.map(i -> i.getIdentifier(IdentifierType.TICKER))
					.collect(Collectors.toList());

			List<List<Identifier>> batches = Lists.partition(idList, batchSize);

			for (List<Identifier> idBatch : batches) {
				requestTechnical = new DataRequest.Builder()
						.dataService(DataServiceEnum.BLOOMBERG)
						.requestType(RequestType.HistoricalDataRequest)
						.fields(fields)
						.parameters(RequestParameters.UseDPDF, true)
						.parameters(RequestParameters.startDate, CoreConfig.GLOBAL_START_DATE.minusYears(2))
						.parameters(RequestParameters.endDate, CoreConfig.GLOBAL_END_DATE)
						.override(RequestOverrides.FUND_PER, fund_per)
						.override(RequestOverrides.FILING_STATUS, "OR")
						.identifierType(IdentifierType.TICKER)
						.instrumentType(InstrumentType.SingleStock)
						.identifiers(idBatch)
						.build();

				requestTechnical.query();

				StateToDataframe capture = new StateToDataframe.Builder()
						.dfContainerType(StateToDataframe.DFContainerType.BATCH)
						.instrumentType(InstrumentType.SingleStock)
						.identifierTypes(IdentifierType.TICKER)
						.spotDataFields(fields)
						.identifiers(idBatch.stream().map(i -> i.getName()).toArray(String[]::new))
						.build();

				sequencer = new TimerStateToDataframe.Builder()
						.stateCapture(capture)
						.startDate(CoreConfig.GLOBAL_START_DATE.minusYears(2))
						.endDate(CoreConfig.GLOBAL_END_DATE)
						.step(Duration.ofDays(1))
						.includeWeekend(true)
						.build();

				writerBatch = (TimerDataframeWriterBatch) new TimerDataframeWriterBatch.Builder()
						.dfContainer(capture.getDfContainer())
						.path("C:\\Users\\admin\\Documents\\workspace\\data\\" + format + "\\equity\\" + region + "\\" + index.toLowerCase() + "\\" + exportFileName + "_" + fund_per)
						.format(format)
						.outputMode("append")
						.partitionColumns(new String[]{"year"})
						.startDate(CoreConfig.GLOBAL_START_DATE)
						.endDate(CoreConfig.GLOBAL_END_DATE)
						.step(Duration.ofDays(365))
						.runOnceOnTermination(true)
						.build();

				CoreConfig.services().run();
				System.gc();
			}
		}
	}
	
	
}
