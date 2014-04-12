package fi.aalto.kutsuplus.sms;

import fi.aalto.kutsuplus.database.TicketInfo;

/**
 * SMSParser has methods for parsing a string into an array of strings
 * */
// SMS format is
// Pickup 11:08, stop 1901
// Vehicle K11
// Code 4U
// 1 pax
// Drop-off 11:41+/-10min, stop E1129
// 6,74e
// Order Id 2013-11-27-58488
// https://kutsuplus.fi/t/4Upnw

public class SMSParser {
	
	private boolean ticket = true;
	final static public int PICKUP_TIME = 0;
	final static public int PICKUP_STOP_CODE = 1;
	final static public int VEHICLE_CODE = 2;
	final static public int ORDER_CODE = 3;
	final static public int AMOUNT_OF_PASSENGERS = 4;
	final static public int DROP_OFF_TIME = 5;
	final static public int DROP_OFF_STOP_CODE = 6;
	final static public int PRICE = 7;
	final static public int ORDER_ID = 8;
	final static public int URL = 9;
	final static public int ERROR_MESSAGE = 0;
	
	private static final int CONF_MSG_SIZE = 8;
	
	static private String[] keywords;

	public SMSParser(String[] keywordList) {
		keywords = keywordList;
	}
		
	public TicketInfo parse(String message) {
		//String[] result;
		String[] lines;
		TicketInfo ticketInfo = new TicketInfo();
		lines = message.split("\n");
		try {
			if (lines.length == CONF_MSG_SIZE) {
				//parse the confirmation message
				
				//Split first line
				String[] words0 = lines[0].trim().split(" ");
				try {
					if (!words0[0].equals(keywords[1]) || words0.length != 4 || !words0[2].equals(keywords[2])) {
						throw new SMSParsingException("Syntax error at line:" + lines[0]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line:" + lines[1]);
				}
				//Get pickup time, don't include ','
				ticketInfo.setPickupTime(words0[1].substring(0, words0[1].length() - 1));
				//Get pickup stop code
				ticketInfo.setPickupStop(words0[3]);
				
				//Split second line
				String[] words1 = lines[1].trim().split(" ");
				try {
					if (!words1[0].equals(keywords[3]) || words1.length != 2) {
						throw new SMSParsingException("Syntax error at line:" + lines[1]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line:" + lines[1]);
				}
				//Get vehicle code
				ticketInfo.setVehicleCode(words1[1]);
				
				//Split third line
				String[] words2 = lines[2].trim().split(" ");
				try {
					if (!words2[0].equals(keywords[4]) || words2.length != 2) {
						throw new SMSParsingException("Syntax error at line:" + lines[2]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line, null pointer:" + lines[2]);
				}
				//Get order code
				ticketInfo.setOrderCode(words2[1]);
				
				//Split fourth line
				String[] words3 = lines[3].trim().split(" ");
				try {
					if (!words3[1].equals(keywords[5]) || words3.length != 2) {
						throw new SMSParsingException("Syntax error at line:" + 
														lines[3] + "-" + words3.length + 
														" " + words3[1] + "-" + keywords[3]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line, null pointer:" + lines[3]);
				}
				//Get amount of passengers
				ticketInfo.setPassengerAmount(words3[0]);
				
				//Split fifth line
				String[] words4 = lines[4].trim().split(" ");
				try {
					if (!words4[0].equals(keywords[6]) || words4.length != 4 || !words4[2].equals(keywords[2])) {
						throw new SMSParsingException("Syntax error at line:" + lines[4]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line, null pointer:" + lines[4]);
				}
				//Get drop-off time, don't include ','
				ticketInfo.setDropOffTime(words4[1].substring(0, words4[1].length() - 1));
				//Get drop-off stop code
				ticketInfo.setDropOffStop(words4[3]);
				
				//Sixth line should contain only one string
				if (!lines[5].contains(keywords[7]) || lines[5].contains(" ")) {
					throw new SMSParsingException("Syntax error at line:" + lines[5]);
				}
				//Get price, don't include 'e'
				ticketInfo.setPrice(lines[5].substring(0, lines[5].length() - 1));
				
				//Split seventh line
				String[] words6 = lines[6].trim().split(" ");
				try {
					/**@TODO check word count, take different languages into account */
					if (!lines[6].contains(keywords[8])) {
						throw new SMSParsingException("Syntax error at line:" + lines[6]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line, null pointer:" + lines[6]);
				}
				//Get order id
				if (keywords[0].equals(new String("en"))) {
					ticketInfo.setOrderId(words6[2]);
				}
				else if (keywords[0].equals(new String("fi")) || keywords[0].equals(new String("sv"))) {
					ticketInfo.setOrderId(words6[1]);
				}
				
				//Eighth line should contain only the URL
				if (!lines[7].contains(keywords[9])) {
					throw new SMSParsingException("Syntax error at line 8");
				}
				//Get URL
				ticketInfo.setUrl(lines[7]);
				ticket = true;
			}
			// An error message was received, let's just display it.
			else {
				ticketInfo.setErrorMessage(message);
				ticket = false;
			}
		} catch (SMSParsingException e) {
			System.err.println(e.getMessage());
			TicketInfo nullInfo = new TicketInfo();
			ticket = false;
			return nullInfo;
		}
		return ticketInfo;
	}

	public boolean isTicket() {
		return ticket;
	}
	
}
