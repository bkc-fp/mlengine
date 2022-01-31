package com.flashpoint.ml.engine.restcontroller;

import com.flashpoint.ml.engine.dataservice.CityService;
import com.flashpoint.ml.engine.ml.FlashpointCoreNLP;
import com.flashpoint.ml.engine.ml.NERTrainer;
import com.flashpoint.ml.engine.ml.NERType;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author BumKi Cho
 */

@RestController
@RequestMapping(value = "/api")
public class NERRestController {

    @Autowired
    private StanfordCoreNLP stanfordCoreNLP;

    @Autowired
    private FlashpointCoreNLP flashpointCoreNLP;

    @Autowired
    private CityService cityService;

    @PostMapping
    @RequestMapping(value = "/fpNer")
    public HashSet<String> fpNer(@RequestBody final String input, @RequestParam final NERType type) {
        CoreDocument coreDocument = new CoreDocument(input);
        flashpointCoreNLP.annotate(coreDocument);
        List<CoreLabel> coreLabels = coreDocument.tokens();
        return new HashSet<String>(collectListFP(coreLabels, type));
    }

    private List<String> collectListFP(List<CoreLabel> coreLabels, final NERType type) {
        Stream<CoreLabel> stream = coreLabels.stream();
        /*
         * Until ML Model is improved, work around is to pass annotation type
         * see below -> findCityByName(cityName, type)
         *
        if (!type.getNerType().equalsIgnoreCase("ALL")) {
            stream = stream.filter(coreLabel -> type.getNerType().equalsIgnoreCase(coreLabel.get(CoreAnnotations.NamedEntityTagAnnotation.class)));
        }
        */
        List<String> cityInfo = new ArrayList();

        stream.map(coreLabel -> coreLabel.originalText()+":"+coreLabel.ner() + ":" + coreLabel.word())
                .collect(Collectors.toList())
                .forEach(text -> cityInfo.add(cityService.findCityByName(text, type)));

        return cityInfo;
    }

    @PostMapping
    @RequestMapping(value = "/coreNer")
    public HashSet<String> coreNer(@RequestBody final String input, @RequestParam final NERType type) {

        CoreDocument coreDocument = new CoreDocument(input);
        stanfordCoreNLP.annotate(coreDocument);

        List<CoreLabel> coreLabels = coreDocument.tokens();

        return new HashSet<String>(collectListCore(coreLabels, type));

    }

    private List<String> collectListCore(List<CoreLabel> coreLabels, final NERType type) {
        Stream<CoreLabel> stream = coreLabels.stream();
        if (!type.getNerType().equalsIgnoreCase("ALL")) {
            stream = stream.filter(coreLabel -> type.getNerType().equalsIgnoreCase(coreLabel.get(CoreAnnotations.NamedEntityTagAnnotation.class)));
        }
        return stream
                .map(CoreLabel::originalText)
                .collect(Collectors.toList());
    }

    @GetMapping
    @RequestMapping(value = "/train")
    public ResponseEntity<String> trainModel() {
        NERTrainer.train();
        return new ResponseEntity<String>("Training", HttpStatus.OK);
    }
}
