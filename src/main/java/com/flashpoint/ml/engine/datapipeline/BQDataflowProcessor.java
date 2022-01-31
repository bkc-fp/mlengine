package com.flashpoint.ml.engine.datapipeline;

import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class BQDataflowProcessor {

    private static final Logger log = LogManager.getLogger(CityETLProcessor.class);

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    public void processFetchRequest(String dataType, String projectName, String gcsLocation) {
        DataflowPipelineOptions pipelineOptions = PipelineOptionsFactory.as(DataflowPipelineOptions.class);
        pipelineOptions.setJobName("FetchRequested"+dataType);
        pipelineOptions.setProject(projectName);
        pipelineOptions.setGcpTempLocation(gcsLocation);
        pipelineOptions.setRunner(DataflowRunner.class);
        Pipeline pipeline = Pipeline.create(pipelineOptions);

    }
}
