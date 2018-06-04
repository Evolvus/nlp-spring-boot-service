package com.evolvus.opennlp.demo.processor;

import java.util.List;

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
import com.evolvus.opennlp.demo.repo.StandardResponseRepository;
import com.evolvus.opennlp.demo.response.StandardResponse;
import com.evolvus.opennlp.demo.util.NlpResponse;

@RestController
@RequestMapping("/api/v0.1")
public class IntentProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(IntentProcessor.class);

	@Autowired
	private NlpTrainer intentExtractor;

	private String[] _DEFAULT_RESPONSE = { "Sorry! Can you repeat that", "I cannot understand that" };

	@Autowired
	private StandardResponseRepository stdRespRepository;

	@RequestMapping(value = "/diana", method = RequestMethod.POST)
	public ResponseEntity<NlpResponse> process(@RequestParam("query") String inputText) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Start process query:{}", inputText);
		}

		NlpResponse resp = new NlpResponse(HttpStatus.OK, null);
		Intent intent = this.intentExtractor.test(inputText);

		if (intent != null) {

			String action = intent.getIntentName();

			List<StandardResponse> stdResponses = stdRespRepository.findByActionIntentName(action);
			if (stdResponses.isEmpty()) {
				resp.setStatus(HttpStatus.NOT_FOUND);
				resp.setBody(_DEFAULT_RESPONSE[0]);
			} else {
				resp.setBody(stdResponses.get(0));
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("End process:{}", resp);
		}
		return new ResponseEntity<NlpResponse>(resp, resp.getStatus());

	}

}
