package fi.aalto.kutsuplus.sms;

public class SMSMessage {
	final private String id;
	final private String address;
	final private String message;
	final private String time;

	public SMSMessage(String id, String address, String message, String time) {
		super();
		this.id = id;
		this.address = address;
		this.message = message;
		this.time = time;
	}

	public String getId() {
		return id;
	}

	public String getAddress() {
		return address;
	}

	public String getMessage() {
		return message;
	}

	public String getTime() {
		return time;
	}

}
