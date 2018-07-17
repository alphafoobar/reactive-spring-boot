package kaizen7.ws;

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import kaizen7.gateway.BitfinexGateway;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;

public class TickerHandler implements WebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final BitfinexGateway gateway;

    public TickerHandler(BitfinexGateway gateway) {
        this.gateway = gateway;
    }

    @Nonnull
    @Override
    public Mono<Void> handle(@Nonnull WebSocketSession session) {
        EmitterProcessor<String> tickers = EmitterProcessor.create();
        session.receive()
            .map(message -> message.getPayloadAsText(Charset.forName("utf-8")))
            .doOnNext(string -> gateway.subscribe(session.getId(), new CurrencyPair(string), tickers))
            .doOnComplete(() -> gateway.unsubscribe(session.getId()))
            .subscribe(logger::info);

        tickers.onNext("{\"message\":\"welcome\"}");
        logger.info("sent welcome to sessionId={}", session.getId());
        return session.send(tickers.map(session::textMessage));
    }
}
