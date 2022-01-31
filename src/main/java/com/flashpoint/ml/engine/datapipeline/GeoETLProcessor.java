package com.flashpoint.ml.engine.datapipeline;

import com.flashpoint.ml.engine.datamodel.GeoName;
import com.flashpoint.ml.engine.dataservice.GeoNameService;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

/*
The direct runner is for testing with very small data. It is engineered for maximum quality assurance, with performance not much of a priority.
For example:
- it randomly shuffles data to make sure you are not depending on ordering that will not exist in production
- it serializes and deserializes data after each step, to make sure the data will be transmitted correctly (production runners will avoid serialization as much as possible)
- it checks whether you have mutated elements in forbidden ways, which would cause you data loss in production
 */

/*
allCountries.txt 1.5GB is too big to handle via direct runner for the direct runner's limitations specified above

A) Read from BigQuery table and load it into memory for fast lookup
B) Create Google Cloud SQL instance and use it in the application
 */

/**
 * @author BumKi Cho
 */

@Component
public class GeoETLProcessor {
    private static final Logger log = LogManager.getLogger(CityETLProcessor.class);

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    public void processGeoNames() {
        PipelineOptions pipelineOptions = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(pipelineOptions);

        ClassLoader classLoader = getClass().getClassLoader();
        String geoFilePath = classLoader.getResource("allCountries.txt").getPath();

        PCollection<String> geoDataLine = pipeline.apply("Read GEO File", TextIO.read().from(geoFilePath));
        PCollection<GeoName> geoObjs = geoDataLine.apply("Parse TSV", ParDo.of(new ExtractGeoInfo(beanFactory)));

        pipeline.run().waitUntilFinish();

    }

    public static class ExtractGeoInfo extends DoFn<String, GeoName> {
        private AutowireCapableBeanFactory beanFactory;
        private GeoName geoName;
        private GeoNameService geoNameService;

        public ExtractGeoInfo(AutowireCapableBeanFactory beanFactory) { this.beanFactory = beanFactory; }

        @ProcessElement
        public void process(ProcessContext context) {
            String[] strArray = context.element().split("\t");

            GeoName geoNameInstance = new GeoName();
            geoNameInstance.setGeoId(strArray[0]);
            geoNameInstance.setGeoName(strArray[1]);
            geoNameInstance.setAltName(strArray[3]);
            geoNameInstance.setLat(strArray[4]);
            geoNameInstance.setLng(strArray[5]);
            geoNameInstance.setCountryId(strArray[8]);
            geoNameInstance.setPopulation(strArray[14]);

            geoNameService = getGeoNameService();
            geoNameService.saveGeoName(geoNameInstance);

            context.output(geoNameInstance);
        }

        @Autowired
        private GeoNameService getGeoNameService() {
            return beanFactory.createBean(GeoNameService.class);
        }

    }

}
