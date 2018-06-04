package com.evolvus.opennlp.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evolvus.opennlp.demo.nlptrainingboot.trainer.Intent;
import com.evolvus.opennlp.demo.nlptrainingboot.trainer.NlpTrainer;
import com.evolvus.opennlp.demo.util.NlpResponse;

/**
 * 
 * @author EVOLVUS\shrimank
 *
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

	@Autowired
	private NlpTrainer trainer;

	/**
	 * '/api/test?query=inputText'
	 * 
	 * @param input
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	public ResponseEntity<NlpResponse> testModel(final @RequestParam("query") String input) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Start input query{}", input);
		}
		HttpStatus httpStatus = HttpStatus.OK;
		Intent json = trainer.test(input);
		NlpResponse response = new NlpResponse(httpStatus, json);
		System.out.println(json);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("End Returning Status:{},Response:{}", httpStatus, json);
		}
		return new ResponseEntity<NlpResponse>(response, httpStatus);
	}
	
	
}
