package com.evolvus.opennlp.demo.response;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.evolvus.opennlp.demo.nlptrainingboot.trainer.Intent;

import lombok.Data;

@Data
@Document
public class StandardResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2151054452173130447L;

	@Id
	private String id;

	private Intent action;

	private List<String> speechText;

	private List<String> displayText;

	private ResponseType type;

	private List<Images> images;

	private List<Buttons> buttons;

}
