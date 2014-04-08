package fi.aalto.kutsuplus.events;

import fi.aalto.kutsuplus.kdtree.StopObject;

public class DropOffChangeEvent {
	final private int sender;
	final private StopObject so;

	public DropOffChangeEvent(int sender,StopObject so) {
		super();
		this.sender=sender;
		this.so = so;
	}
	
	public StopObject getSo() {
		return so;
	}



	public int getSender() {
		return sender;
	}

	@Override
	public String toString() {
		return "DropOffChangeEvent [sender=" + sender + ", so=" + so + "]";
	}

	
}
