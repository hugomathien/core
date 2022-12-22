package dataset;

import config.CoreConfig;
import event.timers.Sequenceable;
import exceptions.MarketDataMissingException;
import finance.identifiers.IdentifierType;
import finance.instruments.IInstrument;
import finance.instruments.InstrumentType;
import marketdata.field.Field;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StateToDataframe implements Sequenceable {
	public enum DFContainerType  {
		MEMORY_STREAM,
		BATCH
	}

	private DFContainerType dfContainerType = DFContainerType.BATCH;
	private AbstractDFContainer dfContainer;
	private String[] universe;
	private String[] identifiers;
	private InstrumentType instrumentType;
	private IdentifierType[] identifierTypes;
	private String[] staticDataFields;
	private String[] spotDataFields;
	private boolean expandingUniverse = false;
	private String customUniverseName;
	private Duration watermark = Duration.ofDays(1);
	private boolean replaceIdentifiersWithComposite = false;
	public StateToDataframe() {

	}

	public StateToDataframe(StateToDataframe.Builder builder) {
		this.universe = builder.universe;
		this.identifiers = builder.identifiers;
		this.instrumentType = builder.instrumentType;
		this.identifierTypes = builder.identifierTypes;
		this.staticDataFields = builder.staticDataFields;
		this.spotDataFields = builder.spotDataFields;
		this.dfContainerType = builder.dfContainerType;
		this.expandingUniverse = builder.expandingUniverse;
		this.customUniverseName = builder.customUniverseName;
		this.watermark = builder.watermark;
		this.replaceIdentifiersWithComposite = builder.replaceIdentifiersWithComposite;
	}

	@PostConstruct
	public void init() {
		if(this.customUniverseName == null && this.universe != null)
			this.customUniverseName = String.join("_", universe);

		ArrayList<StructField> structArrayList = new ArrayList<StructField>();
		structArrayList.add(new StructField("date", DataTypes.DateType, true, Metadata.empty()));
		structArrayList.add(new StructField("datetime", DataTypes.TimestampType, true, Metadata.empty()));
		structArrayList.add(new StructField("year", DataTypes.IntegerType, true, Metadata.empty()));
		structArrayList.add(new StructField("month", DataTypes.IntegerType, true, Metadata.empty()));
		structArrayList.add(new StructField("instrumentType", DataTypes.StringType, true, Metadata.empty()));
		structArrayList.add(new StructField("universe", DataTypes.StringType, true, Metadata.empty()));

		for(IdentifierType type : ArrayUtils.nullToEmpty(identifierTypes,IdentifierType[].class))
			structArrayList.add(new StructField(type.toString().toLowerCase(), DataTypes.StringType, true, Metadata.empty()));
		for(String field : ArrayUtils.nullToEmpty(staticDataFields))
			structArrayList.add(new StructField(field.toString().toLowerCase(), Field.get(field).sparkDataType(), true, Metadata.empty()));
		for(String field : ArrayUtils.nullToEmpty(spotDataFields))
			structArrayList.add(new StructField(field.toString().toLowerCase(), Field.get(field).sparkDataType(), true, Metadata.empty()));

		StructField[] structFields =  structArrayList.stream().toArray(StructField[]::new);

		dfContainer = AbstractDFContainer.makeContainer(this.dfContainerType,structFields);

	}

	@Override
	public void execute(Instant t, Object... args) {

		// From Universe
		Set<IInstrument> instruments = CoreConfig
				.services()
				.instrumentFactory()
				.instrumentsForPortfolioUniverse(universe,expandingUniverse)
				.filter(i -> i.getInstrumentType().equals(this.instrumentType))
				.collect(Collectors.toSet());

		// From Instruments
		for(String identifierString : ArrayUtils.nullToEmpty(this.identifiers)) {
			IInstrument instrument = CoreConfig.services().getInstrument(identifierString);
			if(instrument != null && instrument.getInstrumentType().equals(this.instrumentType))
				instruments.add(instrument);
		}

		// If empty, get all platform instruments for that type
		if(instruments.isEmpty())
			instruments = CoreConfig.services().instrumentFactory().getInstrumentSet().stream().filter(i -> i.getInstrumentType().equals(this.instrumentType)).collect(Collectors.toSet());

		ArrayList<Row> rowList = instruments.parallelStream().map(instrument -> {
				Object[] values = new Object[this.dfContainer.getStructFields().length];
				java.sql.Timestamp timestamp = java.sql.Timestamp.from(t); // TODO: Check the effect of time zone on this conversion. Should we use GLOBAL_ZONE_ID ?
				LocalDate ld = t.atZone(CoreConfig.GLOBAL_ZONE_ID).toLocalDate();
				java.sql.Date dt = java.sql.Date.valueOf(ld);

				values[0] = dt;
				values[1] = timestamp;
				values[2] = ld.getYear();
				values[3] = ld.getMonth().getValue();
				values[4] = this.instrumentType.toString();
				values[5] = this.customUniverseName;

				int idx = 6;
				for (IdentifierType idtype : identifierTypes) {
					String id = instrument.getIdentifier(idtype).toString();
					if (replaceIdentifiersWithComposite)
						id = instrument.getInstrumentType().replaceIdentifierWithComposite(idtype, id);
					values[idx] = id;
					idx++;
				}

				for (String field : ArrayUtils.nullToEmpty(staticDataFields)) {
					values[idx] = instrument.getStaticData().get(Field.get(field));
					idx++;
				}

				for (String field : ArrayUtils.nullToEmpty(spotDataFields)) {
					try {
						values[idx] = instrument.getMarketData().getSpot().getTimestamp(Field.get(field)).isAfter(t.minus(this.watermark)) ? instrument.getSpot(Field.get(field)) : null;
					} catch (MarketDataMissingException e) {
						values[idx] = null;
					}
					idx++;
				}

				Row row = RowFactory.create(values);
				return row;
			}).collect(Collectors.toCollection(ArrayList::new));

		// TODO: Should throw an error if schema and data don't match
		this.dfContainer.addData(rowList);
	}


	public String[] getUniverse() {
		return universe;
	}

	public void setUniverse(String[] universe) {
		this.universe = universe;
	}

	public String[] getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(String[] identifiers) {
		this.identifiers = identifiers;
	}

	public InstrumentType getInstrumentType() {
		return instrumentType;
	}

	public void setInstrumentType(InstrumentType instrumentType) {
		this.instrumentType = instrumentType;
	}

	public IdentifierType[] getIdentifierTypes() {
		return identifierTypes;
	}

	public void setIdentifierTypes(IdentifierType[] identifierTypes) {
		this.identifierTypes = identifierTypes;
	}

	public String[] getStaticDataFields() {
		return staticDataFields;
	}

	public void setStaticDataFields(String[] staticDataFields) {
		this.staticDataFields = staticDataFields;
	}

	public String[] getSpotDataFields() {
		return spotDataFields;
	}

	public void setSpotDataFields(String[] spotDataFields) {
		this.spotDataFields = spotDataFields;
	}

	public DFContainerType getDfContainerType() {
		return dfContainerType;
	}

	public void setDfContainerType(DFContainerType dfContainerType) {
		this.dfContainerType = dfContainerType;
	}

	public AbstractDFContainer getDfContainer() {
		return dfContainer;
	}

	public void setDfContainer(AbstractDFContainer dfContainer) {
		this.dfContainer = dfContainer;
	}

	public boolean isExpandingUniverse() {
		return expandingUniverse;
	}

	public void setExpandingUniverse(boolean expandingUniverse) {
		this.expandingUniverse = expandingUniverse;
	}

	public String getCustomUniverseName() {
		return customUniverseName;
	}

	public void setCustomUniverseName(String customUniverseName) {
		this.customUniverseName = customUniverseName;
	}

	public Duration getWatermark() {
		return watermark;
	}

	public void setWatermark(Duration watermark) {
		this.watermark = watermark;
	}

	public static class Builder {
		private String[] universe;
		private String[] identifiers;
		private InstrumentType instrumentType;
		private IdentifierType[] identifierTypes;
		private String[] staticDataFields;
		private String[] spotDataFields;
		private DFContainerType dfContainerType;
		private boolean expandingUniverse = false;
		private String customUniverseName;
		private Duration watermark = Duration.ofDays(1);
		private boolean replaceIdentifiersWithComposite = false;
		public Builder() {
			super();
		}

		public StateToDataframe build() {
			return new CoreConfig().ctx.getBean(StateToDataframe.class,this);
		}		

		public Builder universe(String... universe) {
			this.universe = universe;
			return this;
		}

		public Builder identifiers(String... identifiers) {
			this.identifiers = identifiers;
			return this;
		}

		public Builder identifierTypes(IdentifierType... identifierTypes) {
			this.identifierTypes = identifierTypes;
			return this;
		}

		public Builder staticDataFields(String... staticDataFields) {
			this.staticDataFields = staticDataFields;
			return this;
		}

		public Builder spotDataFields(String... spotDataFields) {
			this.spotDataFields = spotDataFields;
			return this;
		}		

		public Builder instrumentType(InstrumentType instrumentType) {
			this.instrumentType = instrumentType;
			return this;
		}

		public Builder dfContainerType(DFContainerType dfContainerType) {
			this.dfContainerType = dfContainerType;
			return this;
		}

		public Builder expandingUniverse(boolean expandingUniverse) {
			this.expandingUniverse = expandingUniverse;
			return this;
		}

		public Builder customUniverseName(String customUniverseName) {
			this.customUniverseName = customUniverseName;
			return this;
		}

		public Builder watermark(Duration watermark) {
			this.watermark = watermark;
			return this;
		}

		public Builder replaceIdentifiersWithComposite(boolean replaceIdentifiersWithComposite) {
			this.replaceIdentifiersWithComposite = replaceIdentifiersWithComposite;
			return this;
		}
	}

}
