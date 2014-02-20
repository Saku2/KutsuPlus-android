package fi.aalto.kutsuplus;

public class SMSParsingException extends Exception {

	/**
	 * An exception for parsing errors
	 */
	private static final long serialVersionUID = 6911453478024024705L;

	public SMSParsingException() {
	}

	public SMSParsingException(String detailMessage) {
		super(detailMessage);
	}

	public SMSParsingException(Throwable throwable) {
		super(throwable);
	}

	public SMSParsingException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
