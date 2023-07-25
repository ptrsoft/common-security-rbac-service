package com.synectiks.security.entities;

public class Status {

	private int code;
	private String type;
	private String message;
	private Object object;

    private String mfaKey;

    public String getMfaKey() {
        return mfaKey;
    }

    public void setMfaKey(String mfaKey) {
        this.mfaKey = mfaKey;
    }

    public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}


    public static Status build(int statusCode, String statusType, String msg, Object obj){
        Status st = new Status();
        st.setCode(statusCode);
        st.setType(statusType);
        st.setMessage(msg);
        st.setObject(obj);
        return st;
    }
}
