package com.zorba.bt.app.bluetooth;

public interface ConnectionListener {

	public void connectionStarted(boolean isWifi);
	public void connectionLost();
}
