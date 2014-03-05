package fi.aalto.kutsuplus.sms;


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
	private boolean ticket=true;
	final static public int PICKUP_TIME =0;
	final static public int PICKUP_STOP_CODE=1;
	final static public int VECHILE_CODE=2;
	final static public int ORDER_CODE=3;
	final static public int AMOUNT_OF_PASSENGERS=4;
	final static public int DROP_OFF_TIME=5;
	final static public int DROP_OFF_STOP_CODE=6;
	final static public int PRICE=7;
	final static public int ORDER_ID=8;
	final static public int URL=9;
	final static public int ERROR_MESSAGE=0;
	
	private static final int CONF_MSG_SIZE = 8;

	public SMSParser() {
		
	}
	
	
	private String[] catchWords = {"Pickup", "Vehicle", "Code", "pax", "Drop-off", "e", "Order Id", "https"};
	
	public String[] parse(String message) {
		String[] result;
		String[] lines;
		lines = message.split("\n");
		try {
			if (lines.length == CONF_MSG_SIZE) {
				//parse the confirmation message
				result = new String[10];
				
				//Split first line
				String[] words0 = lines[0].trim().split(" ");
				try {
					if (!words0[0].equals(catchWords[0]) || words0.length != 4) {
						throw new SMSParsingException("Syntax error at line 1:"+lines[0]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line 1:"+lines[1]);
				}
				//Get pickup time, don't include ','
				result[PICKUP_TIME] = words0[1].substring(0, words0[1].length()-1);
				//Get pickup stop code
				result[PICKUP_STOP_CODE] = words0[3];
				
				//Split second line
				String[] words1 = lines[1].trim().split(" ");
				try {
					if (!words1[0].equals(catchWords[1]) || words1.length != 2) {
						throw new SMSParsingException("Syntax error at line 2:"+lines[1]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line 2:"+lines[1]);
				}
				//Get vehicle code
				result[VECHILE_CODE] = words1[1];
				
				//Split third line
				String[] words2 = lines[2].trim().split(" ");
				try {
					if (!words2[0].equals(catchWords[2]) || words2.length != 2) {
						throw new SMSParsingException("Syntax error at line 3:"+lines[2]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line 3:"+lines[2]);
				}
				//Get order code
				result[ORDER_CODE] = words2[1];
				
				//Split fourth line
				String[] words3 = lines[3].trim().split(" ");
				try {
					if (!words3[1].equals(catchWords[3]) || words3.length != 2) {
						throw new SMSParsingException("Syntax error at line 4:"+lines[3]+"-"+words3.length+" "+words3[1]+"-"+catchWords[3]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line, null pointer 4:"+lines[3]);
				}
				//Get amount of passengers
				result[AMOUNT_OF_PASSENGERS] = words3[0];
				
				//Split fifth line
				String[] words4 = lines[4].trim().split(" ");
				try {
					if (!words4[0].equals(catchWords[4]) || words4.length != 4) {
						throw new SMSParsingException("Syntax error at line 5:"+lines[4]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line, null pointer 5:"+lines[4]);
				}
				//Get drop-off time, don't include ','
				result[DROP_OFF_TIME] = words4[1].substring(0, words4[1].length()-1);
				//Get drop-off stop code
				result[DROP_OFF_STOP_CODE] = words4[3];
				
				//Sixth line should contain only one string
				if (!lines[5].contains(catchWords[5]) || lines[5].contains(" ")) {
					throw new SMSParsingException("Syntax error at line 6");
				}
				//Get price, don't include 'e'
				result[PRICE] = lines[5].substring(0, lines[5].length()-1);
				
				//Split seventh line
				String[] words6 = lines[6].trim().split(" ");
				try {
					if (!lines[6].contains(catchWords[6]) || words6.length != 3) {
						throw new SMSParsingException("Syntax error at line 7:"+lines[6]);
					}
				} catch (java.lang.NullPointerException e) {
					throw new SMSParsingException("Syntax error at line, null pointer 7:"+lines[6]);
				}
				//Get order id
				result[ORDER_ID] = words6[2];
				
				//Eighth line should contain only the URL
				if (!lines[7].contains(catchWords[7])) {
					throw new SMSParsingException("Syntax error at line 8");
				}
				//Get URL
				result[URL] = lines[7];
				ticket=true;
			}
			// An error message was received, let's just display it.
			else {
				result = new String[1];
				result[ERROR_MESSAGE] = message;
				ticket=false;
			}
		} catch (SMSParsingException e) {
			System.err.println(e.getMessage());
			String[] msg = new String[1];
			msg[0] = message;
			ticket=false;
			return msg;
		}
		return result;
	}

	public boolean isTicket() {
		return ticket;
	}
	
}
