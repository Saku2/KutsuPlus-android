package fi.aalto.kutsuplus.database;

public class TicketInfo {
	private String pickupTime = null;
	private String pickupStop = null;
	private String vehicleCode = null;
	private String orderCode = null;
	private String passengerAmount = null;
	private String dropOffTime = null;
	private String dropOffStop = null;
	private String price = null;
	private String orderId = null;
	private String url = null;
	private String errorMessage = null;

	public TicketInfo() {
	}

	public String getPickupTime() {
		return pickupTime;
	}

	public void setPickupTime(String pickupTime) {
		this.pickupTime = pickupTime;
	}

	public String getPickupStop() {
		return pickupStop;
	}

	public void setPickupStop(String pickupStop) {
		this.pickupStop = pickupStop;
	}

	public String getVehicleCode() {
		return vehicleCode;
	}

	public void setVehicleCode(String vehicleCode) {
		this.vehicleCode = vehicleCode;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getPassengerAmount() {
		return passengerAmount;
	}

	public void setPassengerAmount(String passengerAmount) {
		this.passengerAmount = passengerAmount;
	}

	public String getDropOffTime() {
		return dropOffTime;
	}

	public void setDropOffTime(String dropOffTime) {
		this.dropOffTime = dropOffTime;
	}

	public String getDropOffStop() {
		return dropOffStop;
	}

	public void setDropOffStop(String dropOffStop) {
		this.dropOffStop = dropOffStop;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
