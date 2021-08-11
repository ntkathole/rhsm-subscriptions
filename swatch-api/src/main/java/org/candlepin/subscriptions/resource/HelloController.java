package org.candlepin.subscriptions.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {
    
    @GetMapping("/world")
    ResponseEntity<Object> testEndpoint(){
        return ResponseEntity.accepted().build();
    }
    
}
