package finance.instruments;

import config.CoreConfig;
import finance.identifiers.Identifier;
import finance.identifiers.IdentifierType;
import finance.identifiers.Ric;
import finance.misc.Exchange;

public enum InstrumentType {
SingleStock, Future, Index, ETF, FX;

    public Exchange guessExchange(IdentifierType idType, String id) {
        switch(this) {
            case SingleStock:
            case ETF:
                return this.guessExchangeForEquity(idType,id);
            default:
                return null;
        }
    }

    public String replaceIdentifierWithComposite(IdentifierType idType, String id) {
        switch(this) {
            case SingleStock:
            case ETF:
                return this.replaceIdentifierWithCompositeForEquity(idType,id);
            default:
                return null;
        }
    }

    public  Exchange guessExchangeForEquity(IdentifierType idType, String id) { // TODO: This is instrument specific, should be accessed from relevant instrument type class instead
        String exchangeCode = null;
        switch(idType) {
            case TICKER:
                exchangeCode = id.substring(Math.max(id.length() - 2, 0));
                break;
            case RIC:
                String substring = id.substring(Math.max(id.length() - 2, 0));
                exchangeCode = Ric.ricExchangeCodeMap.getOrDefault(substring,"");
            default:
                break;
        }

        return CoreConfig.services().getExchange(exchangeCode);
    }

    public String replaceIdentifierWithCompositeForEquity(IdentifierType idType,String id) {
        Exchange exchange = this.guessExchangeForEquity(idType,id);
        String compositeExchangeCode = exchange.getCompositeExchangeCode();
        return id.substring(0,id.length() - 2) + compositeExchangeCode;
    }
}