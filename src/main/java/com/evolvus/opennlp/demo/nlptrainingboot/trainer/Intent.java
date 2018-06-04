package com.evolvus.opennlp.demo.nlptrainingboot.trainer;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * 
 * @author EVOLVUS\shrimank
 *
 */
@Data
@Document
public class Intent implements Serializable {

	/**
	 * Generated Serializable VersioonUID.
	 */
	private static final long serialVersionUID = 1774801687351450023L;

	private String intentName;

	private boolean hasSlots;

	private boolean fulfilled;

	private List<String> utterances;

	private List<Slot> slots;

	private JSONObject args;

	public Intent(String intentName, JSONObject args, boolean hasSlots, boolean fulfilled, List<String> utterances,
			List<Slot> slots) {
		this.intentName = intentName;
		this.args = args;
		this.fulfilled = fulfilled;
		this.hasSlots = hasSlots;
		this.utterances = utterances;
		this.slots = slots;
	}

}
