package event.sequencing;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.springframework.beans.factory.annotation.Autowired;

import config.CoreConfig;
import event.events.TimerEvent;
import event.sequencing.processing.CoreEventType;
import exceptions.MarketDataMissingException;
import finance.identifiers.IdentifierType;
import finance.instruments.IInstrument;
import finance.instruments.InstrumentType;
import marketdata.field.Field;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import streaming.source.MemoryStreamWrapper;

public class InstrumentStateCaptureSequencer extends AbstractEventSequencer<InstrumentStateCaptureSequencer> implements Sequenceable  {
	@Autowired
	private MemoryStreamWrapper memoryStream;  
	private String[] universe;
	private String[] identifiers;
	private InstrumentType instrumentType;
	private IdentifierType[] identifierTypes;
	private String[] staticDataFields;
	private String[] spotDataFields;

	public InstrumentStateCaptureSequencer() {
		super();
	}

	public InstrumentStateCaptureSequencer(Builder builder) {
		super(builder);
		this.universe = builder.universe;
		this.identifiers = builder.identifiers;
		this.instrumentType = builder.instrumentType;
		this.identifierTypes = builder.identifierTypes;
		this.staticDataFields = builder.staticDataFields;
		this.spotDataFields = builder.spotDataFields;
	}

	@PostConstruct
	public void init() {		
		ArrayList<StructField> structArrayList = new ArrayList<StructField>();
		structArrayList.add(new StructField("date", DataTypes.DateType, true, Metadata.empty()));
		structArrayList.add(new StructField("datetime", DataTypes.TimestampType, true, Metadata.empty()));
		structArrayList.add(new StructField("instrumentType", DataTypes.StringType, true, Metadata.empty()));

		for(IdentifierType type : ArrayUtils.nullToEmpty(identifierTypes,IdentifierType[].class))
			structArrayList.add(new StructField(type.toString().toLowerCase(), DataTypes.StringType, true, Metadata.empty()));
		for(String field : ArrayUtils.nullToEmpty(staticDataFields))
			structArrayList.add(new StructField(field.toString().toLowerCase(), Field.get(field).sparkDataType(), true, Metadata.empty()));
		for(String field : ArrayUtils.nullToEmpty(spotDataFields))
			structArrayList.add(new StructField(field.toString().toLowerCase(), Field.get(field).sparkDataType(), true, Metadata.empty()));

		StructField[] structFields =  structArrayList.stream().toArray(StructField[]::new);
		this.memoryStream.init(structFields);
		super.init();
	}

	@Override
	public void execute(Instant t, Object... args) {
		Set<IInstrument> instruments = CoreConfig
				.services()
				.instrumentFactory()
				.instrumentsForPortfolioUniverse(universe)
				.collect(Collectors.toSet());

		for(String identifierString : this.identifiers) {
			IInstrument instrument = CoreConfig.services().getInstrument(identifierString);
			if(instrument != null)
				instruments.add(instrument);
		}

		ArrayList<Row> rowList = new ArrayList<Row>();
		for(IInstrument instrument : instruments) {
			Object[] values = new Object[this.memoryStream.getStructFields().length];
			java.sql.Timestamp timestamp = java.sql.Timestamp.from(t); // TODO: Check the effect of time zone on this conversion. Should we use GLOBAL_ZONE_ID ?
			LocalDate ld = t.atZone(CoreConfig.GLOBAL_ZONE_ID).toLocalDate();
			java.sql.Date dt = java.sql.Date.valueOf(ld);
			
			values[0] = dt;
			values[1] = timestamp;
			values[2] = this.instrumentType.toString();

			int idx = 3;
			for(IdentifierType idtype : identifierTypes) {
				values[idx] = instrument.getIdentifier(idtype).toString();
				idx++;
			}

			for(String field : ArrayUtils.nullToEmpty(staticDataFields)) {
				values[idx] = instrument.getStaticData().get(Field.get(field));
				idx++;
			}			

			for(String field : ArrayUtils.nullToEmpty(spotDataFields)) {

				try {
					values[idx] = instrument.getSpot(Field.get(field));
				}
				catch(MarketDataMissingException e) {
					values[idx] = null;
				}
				idx++;
			}

			Row row = RowFactory.create(values);
			rowList.add(row);
		}

		Seq<Row> seq = JavaConverters.asScalaIteratorConverter(rowList.iterator()).asScala().toSeq();										
		// TODO: Should throw an error if schema and data don't match 
		this.memoryStream.getSparkStream().addData(seq);
	}

	@Override
	public TimerEvent createEvent(Instant eventTimestamp, Temporal start, Temporal end) {
		TimerEvent te = new TimerEvent(this,eventTimestamp,this,CoreEventType.INSTRUMENT_STATE_CAPTURE);
		return te;
	}

	public MemoryStreamWrapper getMemoryStream() {
		return memoryStream;
	}

	public void setMemoryStream(MemoryStreamWrapper memoryStream) {
		this.memoryStream = memoryStream;
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

	public static class Builder extends AbstractEventSequencer.Builder<InstrumentStateCaptureSequencer> {
		private String[] universe;
		private String[] identifiers;
		private InstrumentType instrumentType;
		private IdentifierType[] identifierTypes;
		private String[] staticDataFields;
		private String[] spotDataFields;

		public Builder() {
			super();
		}

		@Override
		public InstrumentStateCaptureSequencer build() {
			return CoreConfig.ctx.getBean(InstrumentStateCaptureSequencer.class,this);
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

	}

}
