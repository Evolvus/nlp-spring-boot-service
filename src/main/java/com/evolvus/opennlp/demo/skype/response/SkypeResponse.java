package com.evolvus.opennlp.demo.skype.response;

import lombok.Data;

@Data
public class SkypeResponse {
	private String type;
	From from;
	Conversation conversation;
	Recipient recipient;
	private String text;
	private String replyToId;
}
