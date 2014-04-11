package fi.aalto.kutsuplus.events;

public class FromAddressChangeEvent {
	final private int sender;
	final private String street_address;

	
	public FromAddressChangeEvent(int sender, String street_address) {
		super();
		this.sender = sender;
		this.street_address = street_address;
	}

	public int getSender() {
		return sender;
	}

	public String getStreet_address() {
		return street_address;
	}

	@Override
	public String toString() {
		return "FromAddressChangeEvent [sender=" + sender + ", street_address=" + street_address + "]";
	}

	
	
}
