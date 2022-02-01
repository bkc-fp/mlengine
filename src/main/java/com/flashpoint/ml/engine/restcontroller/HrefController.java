package com.flashpoint.ml.engine.restcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HrefController {

    @RequestMapping("/corenlp-ner")
    public String corenlpRequest() {
        return "corenlp-ner.html";
    }
}
