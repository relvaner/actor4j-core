/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.service.node.rest.databind;

public class RESTActorResponse {
	protected String status;
	protected int code;
	protected String data;
	protected String message;
	
	public static final String SUCCESS = "success";
	public static final String FAIL = "fail";
	public static final String ERROR = "error";
	
	public RESTActorResponse() {
		this("", 0,  "", "");
	}
	
	public RESTActorResponse(String status, int code, String data, String message) {
		super();
		this.status = status;
		this.code = code;
		this.data = data;
		this.message = message;
	}

	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "RESTActorResponse [status=" + status + ", code=" + code + ", data=" + data + ", message=" + message
				+ "]";
	}
}
