package kaizen7.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class Controller {

    @RequestMapping("/hello")
    public Mono<String> helloWorld() {
        return Flux.just("Hello World").single();
    }
}
