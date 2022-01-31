package com.flashpoint.ml.engine.datapipeline;

import com.flashpoint.ml.engine.datamodel.City;
import com.flashpoint.ml.engine.dataservice.CityService;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * @author BumKi Cho
 */

@Component
public class CityETLProcessor {

    private static final Logger log = LogManager.getLogger(CityETLProcessor.class);

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    public void processWorldCities() {
        PipelineOptions pipelineOptions = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(pipelineOptions);

        String modelFilePath = System.getProperty("user.home")+"/Documents/ML/NER/Model/City/ner-city-model.ser.gz";
        String outputFilePath = "";
        if(!FileUtils.getFile(modelFilePath).exists()) {
            try {
                outputFilePath = System.getProperty("user.home") + "/Documents/ML/NER/Training/City/";
                FileUtils.cleanDirectory(new File(outputFilePath));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

        ClassLoader classLoader = getClass().getClassLoader();
        String cityFilePath = classLoader.getResource("worldcities.csv").getPath();

        PCollection<String> cityDataLine = pipeline
                .apply("Read CSV File", TextIO.read().from(cityFilePath))
                .apply("Clean CSV Values", ParDo.of(new CleanCSV()));
        PCollection<City> cityObjs = cityDataLine.apply("Parse CSV", ParDo.of(new ExtractCityInfo(beanFactory)));
        PCollection<String> cityObjStr = cityObjs.apply(ParDo.of(new CityTrainingData()));
        if(!outputFilePath.isEmpty()) {
            cityObjStr.apply(TextIO.write().to(outputFilePath + "cityname").withSuffix(".txt"));
        }

        pipeline.run().waitUntilFinish();
    }

    public static class ExtractCityInfo extends DoFn<String, City> {

        private City city;
        private AutowireCapableBeanFactory beanFactory;
        private CityService citySerivce;

        public ExtractCityInfo(AutowireCapableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @ProcessElement
        public void process(ProcessContext context) {
            String[] strArray = context.element().split(",");

            if(!strArray[0].equalsIgnoreCase("city")) {
                City city = new City();
                city.setCityName(strArray[0]);
                city.setLat(strArray[2]);
                city.setLng(strArray[3]);
                city.setCountryId(strArray[4]);
                city.setPopulation(strArray[9]);
                city.setCityId(strArray[10]);
                citySerivce = getCitySerivce();
                citySerivce.saveCity(city);

                context.output(city);
            }
        }

        @Autowired
        private CityService getCitySerivce() {
            return beanFactory.createBean(CityService.class);
        }

    }

    public static class CleanCSV extends DoFn<String, String> {
        @ProcessElement
        public void process(@Element String inString, OutputReceiver<String> out) {
            out.output(inString.replaceAll("\"",""));
        }
    }

    public static class CityTrainingData extends DoFn<City, String> {
        @ProcessElement
        public void process(@Element City city, OutputReceiver<String> out) {
//            out.output("I would love to visit <START:city>" + city.getCityName() + "<END>, <START:country>"+city.getCountryId()+"<END>");
            out.output(city.getCityName() + "\t" + "CITY");
        }
    }
}
