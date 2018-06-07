package com.evolvus.opennlp.demo.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.evolvus.opennlp.demo.nlptrainingboot.trainer.Intent;
import com.evolvus.opennlp.demo.nlptrainingboot.trainer.NlpTrainer;
import com.evolvus.opennlp.demo.util.NlpResponse;
import com.google.gson.Gson;

/**
 * 
 * @author EVOLVUS\shrimank
 *
 */
@RestController
@RequestMapping("/api/v0.1")
@CrossOrigin(allowCredentials = "true")
public class TrainDataController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrainDataController.class);

	@Autowired
	private NlpTrainer trainer;

	@Value("${training.data.path}")
	private String trainingDataPath;

	/**
	 * Input - IntentName,Slots,Training Data
	 * 
	 * 
	 * 
	 * @param trainDir
	 * @param slotStr
	 * @param languageCode
	 * @throws IOException
	 */

	@RequestMapping(value = "/train", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<NlpResponse> trainData(@RequestBody final String intentString) throws IOException {

		NlpResponse response = new NlpResponse(HttpStatus.OK, "Training Completed.");

		Gson gson = new Gson();
		final Intent intent = gson.fromJson(intentString, Intent.class);
		List<String> slots = this.extractSlotsAsStringArray(intent);

		String intentName = intent.getIntentName();

		String fileNameWithPath = String.format("%s/%s.%s", trainingDataPath, intentName, "txt");
		File trainingFile = new File(fileNameWithPath);
		try (FileOutputStream fos = new FileOutputStream(trainingFile, true)) {

			// Writing Utternace to File
			intent.getUtterances().forEach(utterance -> {
				try {
					fos.write(utterance.getBytes());
					fos.write("\n".getBytes());
				} catch (IOException ioee) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Exception while writing to file:{}", ioee);
					}
					System.err.println("Exception while writing to file" + ioee);
				}

			});
			String slotStr = slots!=null?slots.stream().collect(Collectors.joining(",")):null;
			trainer.train(trainingDataPath, slotStr, "en");

		} catch (IOException ioe) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception while training:{}", ioe);
			}
			response.setBody(ioe.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<NlpResponse>(response, HttpStatus.OK);
	}

	private List<String> extractSlotsAsStringArray(final Intent intent) {
		if(intent.getSlots()!=null) {
		return intent.getSlots().stream().map(slot -> slot.getName()).collect(Collectors.toList());
		}else {
			return null;
		}
	}

}
