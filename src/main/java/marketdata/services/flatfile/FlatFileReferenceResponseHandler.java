package marketdata.services.flatfile;

import config.CoreConfig;
import event.events.MarketDataEventFactory;
import event.events.PortfolioCompositionEvent;
import event.processing.EventPriorityQueue;
import finance.identifiers.IdentifierType;
import finance.instruments.IInstrument;
import finance.instruments.IPortfolio;
import finance.instruments.InstrumentFactory;
import finance.instruments.InstrumentType;
import marketdata.services.base.AbstractResponseHandler;
import marketdata.services.base.DataRequest;
import marketdata.services.base.RequestParameters;
import marketdata.services.bloomberg.BBGReferenceDataService;
import marketdata.services.flatfile.FlatFileReferenceDataService;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
@Scope("singleton")
@Lazy(true)
public class FlatFileReferenceResponseHandler extends AbstractResponseHandler<FlatFileReferenceDataService>  {
    @Autowired
    InstrumentFactory factory;
    @Autowired
    private MarketDataEventFactory marketDataEventFactory;

    public FlatFileReferenceResponseHandler() {

    }

    @Override
    @Autowired
    public void setDataService(FlatFileReferenceDataService dataService) {
        this.dataService = dataService;
    }

    public Consumer<Row> universeResponse(DataRequest request) {
        return (r) -> {
            Instant timestamp = ((java.sql.Timestamp) r.getAs("timestamp")).toInstant();
            InstrumentType memberType = InstrumentType.valueOf(r.getAs("instrumentType"));
            String memberId = r.getAs("ticker");
            if(((boolean) request.getParameters().getOrDefault(RequestParameters.useComposite,false)) == true)
                memberId = memberType.replaceIdentifierWithComposite(request.getIdentifierType(),memberId);
            String universeId = r.getAs("universe");
            IInstrument instrument = CoreConfig.services()
                    .instrumentFactory()
                    .makeInstrument(memberType, request.getIdentifierType(), memberId);

            /** TODO: distinguish between portfolio request and universe request
            IPortfolio portfolioInstrument = (IPortfolio) CoreConfig.services()
                    .instrumentFactory()
                    .makeInstrument(request.getInstrumentType(), request.getIdentifierType(), universeId);

            Double weight = 0.0; // TODO: This is a default value, weight column should be optional
            PortfolioCompositionEvent event = new PortfolioCompositionEvent(timestamp, portfolioInstrument, instrument, weight);
            CoreConfig.services().eventQueue.add(event);
             **/
        };

    }
}
