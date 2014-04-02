package fi.aalto.kutsuplus.events;

import com.squareup.otto.Bus;

public class CommunicationBus {
	 private final Bus commucication_bus=new Bus();
	 
	 public Bus getCommucicationBus() {
		return commucication_bus;
	}
	private static CommunicationBus instance = null;
	   protected CommunicationBus() {
	      // Exists only to defeat instantiation.
	   }
	   public static CommunicationBus getInstance() {
	      if(instance == null) {
	         instance = new CommunicationBus();
	      }
	      return instance;
	   }
	   
	   
	
}
