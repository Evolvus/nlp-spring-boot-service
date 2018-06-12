package com.evolvus.opennlp.demo.skype.response;

import lombok.Data;

@Data
public class SkypeResponse {
	private String type;
	From FromObject;
	Conversation ConversationObject;
	Recipient RecipientObject;
	private String text;
	private String replyToId;

}
