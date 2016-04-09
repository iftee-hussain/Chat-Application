package com.socket;

public class Message {

	public static final String FIRST_LEVEL_SEPERATOR = "@11@";
	public static final String SECOND_LEVEL_SEPERATOR = "@22@";
	public static final String THIRD_LEVEL_SEPERATOR = "@33@";
	public static final String FOURTH_LEVEL_SEPERATOR = "@44@";
	public int type;
	public int callBackId;
	public String sender;
	public String content;
	public String recipient;

	public Message(String msg) {
		String list[] = new String[4];
		list = msg.split(FIRST_LEVEL_SEPERATOR);
		this.type = Integer.parseInt(list[0]);
		this.sender = list[1];
		this.content = list[2];
		this.recipient = list[3];
		this.callBackId = Integer.parseInt(list[4]);
	}

	public Message(int type, String sender, String content, String recipient,
			int callBackId) {
		this.type = type;
		this.sender = sender;
		this.content = content;
		this.recipient = recipient;
		this.callBackId = callBackId;
	}

	@Override
	public String toString() {
		return type + FIRST_LEVEL_SEPERATOR + sender + FIRST_LEVEL_SEPERATOR
				+ content + FIRST_LEVEL_SEPERATOR + recipient
				+ FIRST_LEVEL_SEPERATOR + callBackId;
	}

}
