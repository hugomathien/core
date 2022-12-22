package finance.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Scope("singleton")
@Lazy(true)
public class ExchangeFactory {
    @Autowired(required = false)
    public Map<String,Exchange> exchangeCodeMap;

    public ExchangeFactory() {

    }

    public Exchange getExchange(String exchangeCode) {
        if(exchangeCode == null)
            return getExchange("OTC");

        if(exchangeCodeMap.containsKey(exchangeCode))
            return exchangeCodeMap.get(exchangeCode);
        else {
            Exchange exchange = new Exchange(exchangeCode);
            this.exchangeCodeMap.put(exchangeCode, exchange);
            return exchange;
        }
    }
}
