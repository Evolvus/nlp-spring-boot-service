package com.evolvus.opennlp.demo.response;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class Images implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -833194104037102445L;

	@Id
	private String id;
	
	private String url;

	private String alt;

	private String width;

	private String height;

	private String className;
}
