package com.evolvus.opennlp.demo.skype.request;

import com.evolvus.opennlp.demo.skype.response.Conversation;
import com.evolvus.opennlp.demo.skype.response.From;
import com.evolvus.opennlp.demo.skype.response.Recipient;

import lombok.Data;

@Data
public class SkypeRequest {
	
	private String type;
	private String id;
	private String timestamp;
	private String serviceUrl;
	private String channelId;
	private From from;
	private Conversation conversation;
	private Recipient recipient;
	private String textFormat;
	private String locale;
	private String text;
	private ChannelData channelData;

}