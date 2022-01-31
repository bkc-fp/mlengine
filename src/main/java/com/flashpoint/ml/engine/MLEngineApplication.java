package com.flashpoint.ml.engine;

//import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author BumKi Cho
 */

@SpringBootApplication
public class MLEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(MLEngineApplication.class, args);
	}

}

/*
 * BELOW IS TO RUN AS COMMAND LINE RUNNER

public class MLEngineApplication implements CommandLineRunner {

	@Autowired
	private MLEngineRunner mlEngineRunner;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(MLEngineApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		mlEngineRunner.set(args);
		mlEngineRunner.run();
	}

	@Bean
	public MLEngineRunner getMLEngineRunner(String... args) {
		return new MLEngineRunner();
	}
}
*/
