package com.zorba.bt.app.bluetooth;

public interface IOTMessageListener {

	void mesgReceveid(String roomname, byte devid, byte status);

}
