package it.datafinance.scrape_engine.adapter.in.web.secure;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure")
class SecureController {

  @GetMapping("/hello")
  String hello(Principal principal) {
    return "Hello, " + principal.getName();
  }
}
