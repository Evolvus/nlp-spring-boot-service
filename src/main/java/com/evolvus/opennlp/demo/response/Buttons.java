package com.evolvus.opennlp.demo.response;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class Buttons implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4284438487261285966L;

	@Id
	private String id;

	private String name;

	private String value;

	private String className;

	private String buttonId;

}
