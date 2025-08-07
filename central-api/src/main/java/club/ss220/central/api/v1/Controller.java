package club.ss220.central.api.v1;

import club.ss220.central.api.ApiVersion;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiVersion.V1 + "/ping")
public class Controller {

    @GetMapping
    public String ping() {
        return "pong!";
    }
}
