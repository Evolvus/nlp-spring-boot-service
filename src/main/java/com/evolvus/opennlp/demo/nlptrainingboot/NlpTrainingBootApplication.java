package com.evolvus.opennlp.demo.nlptrainingboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.evolvus.opennlp.demo.nlptrainingboot.trainer.NlpTrainer;

@SpringBootApplication
@ComponentScan("com.evolvus")
@EnableMongoRepositories("com.evolvus.opennlp.demo.repo")
@EnableAutoConfiguration
public class NlpTrainingBootApplication {

	@Value("${slots}")
	private String slots;

	@Value("${languageCode}")
	private String language;

	@Value("${training.data.path}")
	private String trainingDataPath;

	public static void main(String[] args) {
		SpringApplication.run(NlpTrainingBootApplication.class, args);
	}

	@Bean(name = "nlpTrainer")
	public NlpTrainer getTrainer() {
		return new NlpTrainer(trainingDataPath, slots, language);
	}
}
