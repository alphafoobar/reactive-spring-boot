package kaizen7.rest;

import kaizen7.model.CustomError;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultErrorController implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public CustomError error() {
        return new CustomError(HttpStatus.NOT_FOUND, "Not Found");
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
