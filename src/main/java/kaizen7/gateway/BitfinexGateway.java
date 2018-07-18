package kaizen7.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.FluxProcessor;

public class BitfinexGateway implements Closeable {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final StreamingExchange exchange = StreamingExchangeFactory.INSTANCE
        .createExchange(BitfinexStreamingExchange.class.getName());
    private final Map<String, Set<Disposable>> subscribers = Maps.newConcurrentMap();
    private final Map<CurrencyPair, Observable<String>> bitfinexSubscriptions = Maps.newConcurrentMap();

    public BitfinexGateway() {
        connect();
    }

    private void connect() {
        if (!isAlive()) {
            // Connect to the Exchange WebSocket API. Blocking wait for the connection.
            exchange.connect().blockingAwait();
        }
    }

    private boolean isAlive() {
        try {
            return exchange.isAlive();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void subscribe(String sessionId, CurrencyPair pair, FluxProcessor<String, String> onNext) {
        logger.info("subscribing to pair={} for sessionId={}", pair, sessionId);
        connect();

        Set<Disposable> subscriberSubscriptions = subscribers
            .computeIfAbsent(sessionId, p -> Sets.newConcurrentHashSet());
        Observable<String> observable = getTicker(pair);
        subscriberSubscriptions.add(observable.subscribe(onNext::onNext));

        observable.firstElement().subscribe(string -> logger.info("sessionId={}: First ticker={}", sessionId, string));
    }

    // Create an Observable stream that produces Strings.
    private Observable<String> getTicker(CurrencyPair pair) {
        return bitfinexSubscriptions.computeIfAbsent(pair, currencyPair ->
            exchange
                .getStreamingMarketDataService().getTicker(currencyPair)
                .doOnError(
                    throwable -> logger.warn("Error subscribing to symbol={}, errorMessage=\"{}\"", pair, throwable))
                .map(this::valueAsString));
    }

    private String valueAsString(Ticker ticker) {
        try {
            return mapper.writeValueAsString(ticker);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void unsubscribe(String sessionId) {
        logger.info("unsubscribing sessionId={}", sessionId);
        subscribers.getOrDefault(sessionId, Collections.emptySet()).forEach(Disposable::dispose);
    }

    @Override
    public void close() {
        logger.info("closing");
        exchange.disconnect().subscribe(() -> logger.info("Disconnected from the Exchange"));
    }
}
