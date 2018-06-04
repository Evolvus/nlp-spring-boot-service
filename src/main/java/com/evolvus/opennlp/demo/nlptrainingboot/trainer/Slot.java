package com.evolvus.opennlp.demo.nlptrainingboot.trainer;

import lombok.Data;

@Data
public class Slot {

	private String name;

	private String type;

	public Slot(String name, String type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String toString() {
		return String.format("Slot [name=%s, type=%s]", name, type);
	}
	
	
}