package fi.aalto.kutsuplus.events;

import fi.aalto.kutsuplus.kdtree.StopObject;

public class DropOffChangeEvent {
	final private int sender;
	final private StopObject bus_stop;

	public DropOffChangeEvent(int sender,StopObject bus_stop) {
		super();
		this.sender=sender;
		this.bus_stop = bus_stop;
	}
	
	public StopObject getBus_stop() {
		return bus_stop;
	}

	public int getSender() {
		return sender;
	}

	@Override
	public String toString() {
		return "DropOffChangeEvent [sender=" + sender + ", bus_stop=" + bus_stop + "]";
	}

	

	
}
