package kaizen7;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import kaizen7.gateway.BitfinexGateway;
import kaizen7.ws.TickerHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.TomcatRequestUpgradeStrategy;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public BitfinexGateway gateway() {
        return new BitfinexGateway();
    }

    @Bean
    public TickerHandler handler(BitfinexGateway gateway) {
        return new TickerHandler(gateway);
    }

    @Bean
    public HandlerMapping handlerMapping(TickerHandler handler) {
        Map<String, Object> map = new HashMap<>();
        map.put("/ws", handler);
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);

        // before annotated controllers
        mapping.setOrder(-10);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
