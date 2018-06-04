package com.evolvus.opennlp.demo.util;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author EVOLVUS\shrimank
 *
 */

public class NlpResponse {

	@Setter
	@Getter
	private HttpStatus status;

	@Setter
	@Getter
	private Object body;

	public NlpResponse(HttpStatus status, Object body) {
		this.status = status;
		this.body = body;
	}

}
