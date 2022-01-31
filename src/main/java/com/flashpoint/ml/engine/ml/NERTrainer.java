package com.flashpoint.ml.engine.ml;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * @author BumKi Cho
 */

@Component
public class NERTrainer {

    private static final Logger log = LogManager.getLogger(NERTrainer.class);

    public static void train() {
        long startTime = System.currentTimeMillis();
        String trainFilePath = System.getProperty("user.home")+"/Documents/ML/NER/Training/City/";
        File file = new File(trainFilePath);
        String[] extensions = new String[] {"txt"};
        List<File> files = (List<File>) FileUtils.listFiles(file, extensions, true);
        String trainFileNames = "";
        for(File f : files) {
            if(!trainFileNames.isEmpty()) trainFileNames += ",";
            trainFileNames += f.getAbsolutePath();
        }
        String modelFilePath = System.getProperty("user.home")+"/Documents/ML/NER/Model/City/ner-city-model.ser.gz";

        Properties props = new Properties();
        props.put("trainFileList", trainFileNames); // To train with multiple files, a comma separated list
        props.put("map", "word=0,answer=1");
        props.put("useClassFeature", "true");
        props.put("useNGrams", "true");
        props.put("noMidNGrams", "true");
        props.put("maxNGramLeng", "6");
        props.put("useDisjunctive", "true");
        props.put("usePrev", "true");
        props.put("useNext", "true");
        props.put("useSequences", "true");
        props.put("usePrevSequences", "true");
        props.put("maxLeft", "1");
        props.put("useTypeSeqs", "true");
        props.put("useTypeSeqs2", "true");
        props.put("useTypeySequences", "true");
        props.put("wordShape", "chris2useLC");

        SeqClassifierFlags flags = new SeqClassifierFlags(props);
        CRFClassifier<CoreLabel> crf = new CRFClassifier<CoreLabel>(flags);
        crf.train();
        crf.serializeClassifier(modelFilePath);

    }
}
