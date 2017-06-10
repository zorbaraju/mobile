package com.zorba.bt.app.bluetooth;

public interface ConnectionListener {

	public void connectionStarted(int connectionType);
	public void connectionLost();
}
