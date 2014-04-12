package fi.aalto.kutsuplus.test;

import fi.aalto.kutsuplus.database.TicketInfo;
import fi.aalto.kutsuplus.sms.SMSParser;
import junit.framework.TestCase;

public class SMSParserTest extends TestCase {
	protected String testMsgOK;
	protected String testMsgError;
	protected String testMsgInfo;
	protected String[] keywords = {
			"en","Pickup","stop","Vehicle","Code","pax","Drop-off","e","Order Id","https"};

	public SMSParserTest() {
		super();
	}
	
	protected void setUp() {
		testMsgOK = "Pickup 11:08, stop 1901\n" +
							"Vehicle K11\n" +
							"Code 4U\n" +
							"1 pax\n" +
							"Drop-off 11:41+/-10min, stop E1129\n" +
							"6,74e\n" +
							"Order Id 2013-11-27-58488\n" +
							"https://kutsuplus.fi/t/4Upnw";
		testMsgError = "Pickup 11:08, stop 1901\n" +
							"Vehicle K11\n";
		testMsgInfo = "There are no vehicles available. https://www.kutsuplus.fi";
	}
	
	public void testParseOK() {
		SMSParser parser = new SMSParser(keywords);
		TicketInfo result = parser.parse(testMsgOK);
		assertEquals(result.getPickupTime(), "11:08");
		assertEquals(result.getPickupStop(), "1901");
		assertEquals(result.getVehicleCode(), "K11");
		assertEquals(result.getOrderCode(), "4U");
		assertEquals(result.getPassengerAmount(), "1");
		assertEquals(result.getDropOffTime(), "11:41+/-10min");
		assertEquals(result.getDropOffStop(), "E1129");
		assertEquals(result.getPrice(), "6,74");
		assertEquals(result.getOrderId(), "2013-11-27-58488");
		assertEquals(result.getUrl(), "https://kutsuplus.fi/t/4Upnw");
	}

	public void testParseError() {
		SMSParser parser = new SMSParser(keywords);
		TicketInfo result = parser.parse(testMsgError);
		assertEquals(result.getErrorMessage(), testMsgError);
	}
	
	public void testParseInfo() {
		SMSParser parser = new SMSParser(keywords);
		TicketInfo result = parser.parse(testMsgInfo);
		assertEquals(result.getErrorMessage(), testMsgInfo);
	}
}
