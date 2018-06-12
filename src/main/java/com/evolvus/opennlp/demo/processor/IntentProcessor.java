package com.evolvus.opennlp.demo.processor;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evolvus.opennlp.demo.nlptrainingboot.trainer.Intent;
import com.evolvus.opennlp.demo.nlptrainingboot.trainer.NlpTrainer;
import com.evolvus.opennlp.demo.repo.StandardResponseRepository;
import com.evolvus.opennlp.demo.response.StandardResponse;
import com.evolvus.opennlp.demo.skype.request.SkypeRequest;
import com.evolvus.opennlp.demo.skype.response.Conversation;
import com.evolvus.opennlp.demo.skype.response.From;
import com.evolvus.opennlp.demo.skype.response.Recipient;
import com.evolvus.opennlp.demo.skype.response.SkypeResponse;
import com.evolvus.opennlp.demo.util.NlpResponse;
import com.google.gson.Gson;

@RestController
@RequestMapping("/api/v0.1")
@CrossOrigin(allowCredentials = "true")
public class IntentProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(IntentProcessor.class);

	@Autowired
	private NlpTrainer intentExtractor;

	private String[] _DEFAULT_RESPONSE = { "Sorry! Can you repeat that", "I cannot understand that", "One more time?",
			"I missed that!" };

	@Autowired
	private StandardResponseRepository stdRespRepository;

	Random random;
	Gson gson = new Gson();

	@RequestMapping(value = "/diana", method = RequestMethod.POST)
	public ResponseEntity<NlpResponse> process(@RequestParam("query") String inputText) {
		random = new Random();
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
				resp.setBody(_DEFAULT_RESPONSE[random.nextInt(_DEFAULT_RESPONSE.length)]);
			} else {
				resp.setBody(stdResponses.get(0));
			}
		} else {
			resp.setBody(_DEFAULT_RESPONSE[random.nextInt(_DEFAULT_RESPONSE.length)]);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("End process:{}", resp);
		}
		return new ResponseEntity<NlpResponse>(resp, resp.getStatus());

	}

	@RequestMapping(value = "/webhook", method = RequestMethod.POST)
	public ResponseEntity<SkypeResponse> webhook(@RequestBody String input) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Start webhook query:{}", input);
		}
		System.out.println("/api/v0.1/webhook:" + input);
		
		SkypeRequest  request = gson.fromJson(input, SkypeRequest.class);
		
		System.out.println("Converted SkypeRequest Object "+request);
			
		SkypeResponse response = new SkypeResponse();
		response.setType("message");
		From from = new From();

		from.setId(request.getFrom().getId());
		from.setName(request.getFrom().getName());
		response.setFrom(from);

		Conversation conversation = new Conversation();
		conversation.setId(request.getConversation().getId());
		conversation.setName("shrimank");
		response.setConversation(conversation);

		Recipient recipient = new Recipient();
		recipient.setId(request.getRecipient().getId());
		recipient.setName(request.getRecipient().getName());
		response.setRecipient(recipient);

		response.setText("I have several times available on saturday");
		response.setReplyToId(request.getFrom().getId());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("End process:{}", response);
		}
		HttpHeaders  header = new HttpHeaders();
		header.add("X-Correlating-OperationId", request.getId());
		System.out.println("End Processing sending response :" + response);
		return new ResponseEntity<SkypeResponse>(response,header, HttpStatus.OK);

	}

}
