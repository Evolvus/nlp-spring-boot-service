package com.evolvus.opennlp.demo.nlptrainingboot.trainer;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

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

	@Setter
	@Getter
	private DocumentCategorizerME categorizer;
	
	@Setter
	@Getter
	private NameFinderME[] nameFinderMEs;

	public NlpTrainer(final String trainDir, final String slotStr, final String languageCode) {
		this.train(trainDir, slotStr, languageCode);
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

		//Intent intent = null;
		// OptionalDouble max = DoubleStream.of(outcome).max();
		// if (max.isPresent() && max.getAsDouble() * 100.00 > 90) {
		// double[] val = { max.getAsDouble() };
		Intent  intent = new Intent(categorizer.getBestCategory(outcome), obj, true, false, null, null);
		// pass the intent name to db and get the slot status
		if (!obj.isEmpty()) {
			intent.setHasSlots(true);
			intent.setFulfilled(true);
		}
		// }

		return intent;
	}

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
				System.err.println("Training file :"+trainingFile.getAbsolutePath().contains("'"));
				String intent = trainingFile.getName().replaceFirst("[.][^.]+$", "");
				ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(trainingFile), "UTF-8");
				ObjectStream<DocumentSample> documentSampleStream = new IntentDocumentSampleStream(intent, lineStream);
				categoryStreams.add(documentSampleStream);
			}
			ObjectStream<DocumentSample> combinedDocumentSampleStream = ObjectStreamUtils
					.createObjectStream(categoryStreams.toArray(new ObjectStream[0]));

			DoccatModel doccatModel = DocumentCategorizerME.train(languageCode, combinedDocumentSampleStream, 0, 100);
			combinedDocumentSampleStream.close();

			List<TokenNameFinderModel> tokenNameFinderModels = new ArrayList<TokenNameFinderModel>();

			if (slots != null) {
				for (String slot : slots) {
					List<ObjectStream<NameSample>> nameStreams = new ArrayList<ObjectStream<NameSample>>();
					for (File trainingFile : trainingDirectory.listFiles()) {
						ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(trainingFile),
								"UTF-8");
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

			System.out.println("Training complete. Ready.");
		} catch (Exception excep) {
			excep.printStackTrace();
			System.err.println(excep);
		}
	}

}
