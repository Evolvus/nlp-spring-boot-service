package com.evolvus.opennlp.demo.nlptrainingboot.trainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.evolvus.opennlp.demo.repo.StandardResponseRepository;
import com.evolvus.opennlp.demo.response.StandardResponse;

import lombok.Getter;
import lombok.Setter;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;

@Component
public class NlpTrainer {

	private static final String EN = "en";

	private static final String UTF_8 = "UTF-8";

	private static final Logger LOGGER = LoggerFactory.getLogger(NlpTrainer.class);

	@Setter
	@Getter
	private DocumentCategorizerME categorizer;

	@Setter
	@Getter
	private NameFinderME[] nameFinderMEs;

	@Value("${training.data.path}")
	private String trainingDataPath;

	@Autowired
	private StandardResponseRepository repository;

	public NlpTrainer() {
		// this.loadTrainingDataToNLP();
	}

	// Get Intent and Slots value
	@SuppressWarnings("unchecked")
	public Intent test(String s) {
		double[] outcome = categorizer.categorize(s);

		JSONObject obj = new JSONObject();

		String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(s);
		for (NameFinderME nameFinderME : nameFinderMEs) {
			Span[] spans = nameFinderME.find(tokens);
			String[] names = Span.spansToStrings(spans, tokens);
			for (int i = 0; i < spans.length; i++) {
				obj.put(spans[i].getType(), names[i]);
			}
		}

		// Intent intent = null;
		// OptionalDouble max = DoubleStream.of(outcome).max();
		// if (max.isPresent() && max.getAsDouble() * 100.00 > 90) {
		// double[] val = { max.getAsDouble() };
		Intent intent = new Intent(categorizer.getBestCategory(outcome), obj, true, false, null, null);
		// pass the intent name to db and get the slot status
		if (!obj.isEmpty()) {
			intent.setHasSlots(true);
			intent.setFulfilled(true);
		}
		// }

		return intent;
	}

	@PostConstruct
	public void loadTrainingDataToNLP() {

		try {
			String dtStr = (new SimpleDateFormat("dd-MM-yyyy")).format(new Date());

			final File trainingDirectory = new File(String.format("%s/_%s_", trainingDataPath, dtStr));
			trainingDirectory.mkdir();

			List<StandardResponse> srData = repository.findAll();
			Set<String> slotsSet = new HashSet<String>();
			srData.parallelStream().forEach(sr -> {
				Intent intent = sr.getAction();
				String intentName = intent.getIntentName().replaceFirst("[.][^.]+$", "");
				File file = new File(String.format("%s/%s.txt", trainingDirectory.getAbsolutePath(), intentName));
				slotsSet.addAll(intent.getSlots().stream().map(slot -> slot.getName()).collect(Collectors.toSet()));
				try {
					if (!file.exists()) {
						if (file.createNewFile())
							LOGGER.info("File was'nt there,created !!");
					} else {
						LOGGER.info("File exist not created !!");
					}
				} catch (IOException ioExcep) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Exception while creating to file:{}", ioExcep);
					}
				}

				try (FileOutputStream fos = new FileOutputStream(file, false)) {

					intent.getUtterances().forEach(utterance -> {
						try {
							fos.write(toCapitalize(utterance).getBytes());
							fos.write("\n".getBytes());
							fos.write(utterance.toLowerCase().getBytes());
							fos.write("\n".getBytes());
							fos.write(utterance.toUpperCase().getBytes());
							fos.write("\n".getBytes());
						} catch (IOException ioee) {
							if (LOGGER.isErrorEnabled()) {
								LOGGER.error("Exception while writing to file:{}", ioee);
							}
						}

					});

				} catch (IOException ioe) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("IOException while training utterance {}", ioe);
					}
				} catch (Exception execep) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Exception while training utterance {}", execep);
					}
				}

			});

			String slots = !slotsSet.isEmpty() ? slotsSet.stream().collect(Collectors.joining(",")) : null;
			this.train(trainingDirectory.getAbsolutePath(), slots, EN);

		} catch (Exception excep) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception while loading training data from database{}", excep);
			}
		}
	}

	private String toCapitalize(String utterance) {
		String[] inputs = utterance.split("\\s");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inputs.length; i++) {
			sb.append(inputs[i].substring(0, 1).toUpperCase() + inputs[i].substring(1).toLowerCase());
			sb.append(" ");
		}

		return sb.toString();

	}

	/**
	 * 
	 * @param trainDir
	 * @param slotStr
	 * @param languageCode
	 */
	public void train(final String trainDir, final String slotStr, final String languageCode) {
		try {
			File trainingDirectory = new File(trainDir);
			String[] slots = new String[0];

			slots = slotStr != null ? slotStr.split(",") : null;

			if (!trainingDirectory.isDirectory()) {
				throw new IllegalArgumentException(
						"TrainingDirectory is not a directory: " + trainingDirectory.getAbsolutePath());
			}

			List<ObjectStream<DocumentSample>> categoryStreams = new ArrayList<ObjectStream<DocumentSample>>();
			for (File trainingFile : trainingDirectory.listFiles()) {
				String intent = trainingFile.getName().replaceFirst("[.][^.]+$", "");
				ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(trainingFile), UTF_8);
				ObjectStream<DocumentSample> documentSampleStream = new IntentDocumentSampleStream(intent, lineStream);
				categoryStreams.add(documentSampleStream);
			}
			ObjectStream<DocumentSample> combinedDocumentSampleStream = ObjectStreamUtils
					.createObjectStream(categoryStreams.toArray(new ObjectStream[0]));

			DoccatModel doccatModel = DocumentCategorizerME.train(languageCode, combinedDocumentSampleStream, 0, 100);
			combinedDocumentSampleStream.close();

			List<TokenNameFinderModel> tokenNameFinderModels = new ArrayList<TokenNameFinderModel>();

			if (slots != null && slots.length > 0) {
				for (String slot : slots) {
					List<ObjectStream<NameSample>> nameStreams = new ArrayList<ObjectStream<NameSample>>();
					for (File trainingFile : trainingDirectory.listFiles()) {
						ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(trainingFile),
								UTF_8);
						ObjectStream<NameSample> nameSampleStream = new NameSampleDataStream(lineStream);
						nameStreams.add(nameSampleStream);
					}
					ObjectStream<NameSample> combinedNameSampleStream = ObjectStreamUtils
							.createObjectStream(nameStreams.toArray(new ObjectStream[0]));

					TokenNameFinderModel tokenNameFinderModel = NameFinderME.train(languageCode, slot,
							combinedNameSampleStream, TrainingParameters.defaultParams(),
							(AdaptiveFeatureGenerator) null, Collections.<String, Object>emptyMap());
					combinedNameSampleStream.close();
					tokenNameFinderModels.add(tokenNameFinderModel);
				}
			}

			categorizer = new DocumentCategorizerME(doccatModel);
			nameFinderMEs = new NameFinderME[tokenNameFinderModels.size()];
			for (int i = 0; i < tokenNameFinderModels.size(); i++) {
				nameFinderMEs[i] = new NameFinderME(tokenNameFinderModels.get(i));
			}

			LOGGER.info("Training complete. Ready.");
		} catch (Exception excep) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception while training data {}", excep);
			}
		}
	}

}
