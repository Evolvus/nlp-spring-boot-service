package com.evolvus.opennlp.demo.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evolvus.opennlp.demo.nlptrainingboot.trainer.NlpTrainer;
import com.evolvus.opennlp.demo.repo.StandardResponseRepository;
import com.evolvus.opennlp.demo.response.StandardResponse;
import com.evolvus.opennlp.demo.util.NlpResponse;
import com.google.gson.Gson;

@RestController
@RequestMapping("/api/v0.1")
public class StandardResponseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(StandardResponseController.class);

	@Autowired
	private StandardResponseRepository repository;

	@Autowired
	private TrainDataController trainerDataCtrl;

	Gson gson = new Gson();

	/**
	 * 
	 * @param requestBody
	 * @return
	 */
	@RequestMapping(value = "sr", method = RequestMethod.POST)
	public ResponseEntity<NlpResponse> create(@RequestBody String requestBody) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Start create:{}", requestBody);
		}

		NlpResponse response = new NlpResponse(HttpStatus.CREATED, "Data created successfully.");
		try {
			StandardResponse stdResponse = gson.fromJson(requestBody, StandardResponse.class);
			trainerDataCtrl.trainData(gson.toJson(stdResponse.getAction()));
			stdResponse = repository.save(stdResponse);
			response.setBody(stdResponse);
		} catch (Exception excep) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception while creating StandardResponse:{}", excep);
			}
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			response.setBody(excep.getMessage());
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("End create:{}", response);
		}
		return new ResponseEntity<NlpResponse>(response, response.getStatus());
	}

	/**
	 * 
	 * @param id
	 * @param requestBody
	 * @return
	 */
	@RequestMapping(value = "sr", method = RequestMethod.PUT)
	public ResponseEntity<NlpResponse> update(@RequestParam("id") String id, @RequestBody String requestBody) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Start update:{}", requestBody);
		}

		NlpResponse response = new NlpResponse(HttpStatus.ACCEPTED, "Data updated successfully.");
		try {
			Optional<StandardResponse> stdResponse = repository.findById(id);
			if (stdResponse.isPresent()) {
				StandardResponse updateStdResponse = stdResponse.get();
				updateStdResponse = gson.fromJson(requestBody, StandardResponse.class);
				updateStdResponse.setId(stdResponse.get().getId());
				//Training updated utterances
				trainerDataCtrl.trainData(gson.toJson(updateStdResponse.getAction()));
				updateStdResponse = repository.save(updateStdResponse);
				response.setBody(updateStdResponse);
			} else {
				response.setStatus(HttpStatus.NOT_FOUND);
				response.setBody(String.format("No record with id:%s found.", id));
			}

		} catch (Exception excep) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception while updating StandardResponse:{}", excep);
			}
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			response.setBody(excep.getMessage());
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("End update:{}", response);
		}
		return new ResponseEntity<NlpResponse>(response, response.getStatus());
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "sr/{id}", method = RequestMethod.GET)
	public ResponseEntity<NlpResponse> getById(@PathVariable("id") String id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Start getById:{}", id);
		}

		NlpResponse response = new NlpResponse(HttpStatus.OK, "Data fetched successfully.");
		try {
			Optional<StandardResponse> stdResponse = repository.findById(id);
			if (stdResponse.isPresent()) {
				response.setBody(stdResponse.get());
			} else {
				response.setStatus(HttpStatus.NOT_FOUND);
				response.setBody(String.format("No record with id:%s found.", id));
			}

		} catch (Exception excep) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception while getById:{}", excep);
			}
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			response.setBody(excep.getMessage());
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("End getById:{}", response);
		}
		return new ResponseEntity<NlpResponse>(response, response.getStatus());
	}

	/**
	 * 
	 * GetAll
	 * 
	 * @return
	 */
	@RequestMapping(value = "sr/all", method = RequestMethod.GET)
	public ResponseEntity<NlpResponse> getAll() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Start getAll");
		}

		NlpResponse response = new NlpResponse(HttpStatus.OK, "Data fetched successfully.");
		try {
			List<StandardResponse> stdResponses = repository.findAll();
			if (stdResponses.isEmpty()) {
				response.setStatus(HttpStatus.NOT_FOUND);
				response.setBody(String.format("No records found."));
			} else {
				response.setBody(stdResponses);
			}
		} catch (Exception excep) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception while gettingAll StandardResponses:{}", excep);
			}
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			response.setBody(excep.getMessage());
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("End getAll:{}", response);
		}
		return new ResponseEntity<NlpResponse>(response, response.getStatus());
	}

	/**
	 * 
	 * GetAll
	 * 
	 * @return
	 */
	@RequestMapping(value = "sr", method = RequestMethod.GET)
	public ResponseEntity<NlpResponse> getByIntentName(@RequestParam("intentName") String intentName) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Start getByIntentName:{}");
		}

		NlpResponse response = new NlpResponse(HttpStatus.OK, null);
		try {
			List<StandardResponse> stdResponses = repository.findByActionIntentName(intentName);
			if (stdResponses.isEmpty()) {
				response.setStatus(HttpStatus.NOT_FOUND);
				response.setBody(String.format("No records found with intentName :%s", intentName));
			} else {
				response.setBody(stdResponses);
			}
		} catch (Exception excep) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception while getByIntentName StandardResponses:{}", excep);
			}
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			response.setBody(excep.getMessage());
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("End getByIntentName:{}", response);
		}
		return new ResponseEntity<NlpResponse>(response, response.getStatus());
	}

}
