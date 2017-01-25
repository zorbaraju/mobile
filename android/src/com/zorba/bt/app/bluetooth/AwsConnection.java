package com.zorba.bt.app.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.zorba.bt.app.CommonUtils;
import com.zorba.bt.app.dao.RoomData;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.UUID;

public class AwsConnection {

	String macAddress = null;
	static final String LOG_TAG = AwsConnection.class.getCanonicalName();

	private boolean isConnected = false;
	private BtIotReceiver receiver = null;
	// --- Constants to modify per your configuration ---

	// IoT endpoint
	// AWS Iot CLI describe-endpoint call returns:
	// XXXXXXXXXX.iot.<region>.amazonaws.com
	public static final String CUSTOMER_SPECIFIC_ENDPOINT = "az6wn08spc7nt.iot.ap-northeast-1.amazonaws.com";
	// Cognito pool ID. For this app, pool needs to be unauthenticated pool with
	// AWS IoT permissions.
	public static final String COGNITO_POOL_ID = "ap-northeast-1:316101d0-19ca-44d2-8534-aa63af6a5486";
	// Name of the AWS IoT policy to attach to a newly created certificate
	public static final String AWS_IOT_POLICY_NAME = "mypolicy";

	// Region of AWS IoT
	public static final Regions MY_REGION = Regions.AP_NORTHEAST_1;
	// Filename of KeyStore file on the filesystem
	public static final String KEYSTORE_NAME = "ks.bks";
	// Password for the private key in the KeyStoreT
	public static final String KEYSTORE_PASSWORD = "rajuraju";
	// Certificate and key aliases in the KeyStore
	public static final String CERTIFICATE_ID = "ks";

	AWSIotClient mIotAndroidClient;
	AWSIotMqttManager mqttManager;
	String clientId;
	String keystorePath;
	String keystoreName;
	String keystorePassword;

	KeyStore clientKeyStore = null;
	String certificateId;

	CognitoCachingCredentialsProvider credentialsProvider;

	public AwsConnection(Context activity, String macaddress) {

		this.macAddress = macaddress;
		// MQTT client IDs are required to be unique per AWS IoT account.
		// This UUID is "practically unique" but does not _guarantee_
		// uniqueness.
		clientId = UUID.randomUUID().toString();

		// Initialize the AWS Cognito credentials provider
		credentialsProvider = new CognitoCachingCredentialsProvider(activity, // context
				COGNITO_POOL_ID, // Identity Pool ID
				MY_REGION // Region
		);

		Region region = Region.getRegion(MY_REGION);

		// MQTT Client
		mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);

		// Set keepalive to 10 seconds. Will recognize disconnects more quickly
		// but will also send
		// MQTT pings every 10 seconds.
		// mqttManager.setKeepAlive(10);
		mqttManager.setConnectionStabilityTime(10);
		// mqttManager.setAutoReconnect(false);
		// Set Last Will and Testament for MQTT. On an unclean disconnect (loss
		// of connection)
		// AWS IoT will publish this message to alert other clients.
		AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("my/lwt/topic",
				"Android client lost connection", AWSIotMqttQos.QOS0);
		mqttManager.setMqttLastWillAndTestament(lwt);

		// IoT Client (for creation of certificate if needed)
		mIotAndroidClient = new AWSIotClient(credentialsProvider);
		mIotAndroidClient.setRegion(region);

		keystorePath = activity.getFilesDir().getPath();
		keystoreName = KEYSTORE_NAME;
		keystorePassword = KEYSTORE_PASSWORD;
		certificateId = CERTIFICATE_ID;

		// To load cert/key from keystore on filesystem
		try {
			if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
				if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath, keystoreName,
						keystorePassword)) {
					Log.i(LOG_TAG, "Certificate " + certificateId + " found in keystore - using for MQTT.");
					// load keystore from file into memory to pass on connection
					clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId, keystorePath, keystoreName,
							keystorePassword);
				} else {
					Log.i(LOG_TAG, "Key/cert " + certificateId + " not found in keystore.");
				}
			} else {
				Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + keystoreName + " not found.");
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
		}

		if (clientKeyStore == null) {
			Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.");

			return;
		}
		try {
			mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {

				@Override
				public void onStatusChanged(AWSIotMqttClientStatus arg0, Throwable arg1) {
					System.out.println("status....." + arg0 + " aa>>" + arg1 + ".." + mqttManager.isAutoReconnect());
					if (arg0.equals(AWSIotMqttClientStatus.Connected)) {
						isConnected = true;
						receiver = new BtIotReceiver();
						mqttManager.subscribeToTopic(macAddress+"/publish", AWSIotMqttQos.QOS0, receiver);
					}
				}
			});

		} catch (Exception e) {
			Log.e(LOG_TAG, "Subscription error.", e);
		}
	}
	
	public boolean isConnected() {
		return isConnected;
	}

	public void sendMessage(byte[] bytes) {
		mqttManager.publishData(bytes, macAddress+"/subscribe", AWSIotMqttQos.QOS0);
	}
	
	public byte[] getData(int reqno) {
		return receiver.getData(reqno);
	}

	public void enableNotificationForRoom(final IOTMessageListener messgeListener, final RoomData rd) {
		String mac = rd.getAddress();
		try {
            mqttManager.subscribeToTopic(mac+"/publish", AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                        	System.out.println("Hai...."+topic+"data>>>"+data);
                        	CommonUtils.printBytes("Read", data);
                    		byte cmd = data[0];
                    		if (cmd == 36) {
	                    		byte reqno = data[1];
	                    		byte num = data[2];
	                    		byte alldevs = (byte) 0xFF;
	                			System.out.println("numdevs...."+num+" alldevs="+alldevs);
	                			if (num == alldevs) {
	                				System.out.println("numdevs...."+num+" alldevs="+alldevs);
	                				int maxdev = data.length-3;
	                				System.out.println("maxdev...."+maxdev);
	                				byte[] devids = new byte[maxdev];
	                				byte[] statuses = new byte[maxdev];
	                				for (int i = 0; i < maxdev; i++) {
	                					statuses[i] = data[i+3];
	                					devids[i] = (byte)(i+1);
	                				}
	                				/*String switchName = BtLocalDB.getInstance(this).getSwitchName(roomDeviceName, switchId);
	                				if( switchName == null){
	                					switchName = "No match";
	                				}*/
	                				messgeListener.mesgReceveid(rd.getName(), devids, statuses);
	                				
	                			} else {
	                				/*String switchName = BtLocalDB.getInstance(this).getSwitchName(roomDeviceName, switchId);
	                				if( switchName == null){
	                					switchName = "No match";
	                				}*/
		                    		byte[] devid = {data[3]};
		                    		byte[] status = {data[4]};
		                    		messgeListener.mesgReceveid(rd.getName(), devid, status);
	                			}
                    		}
                        }
                    });
            System.out.println("Subscribed to "+mac+"/subscribe");
        } catch (Exception e) {
        	 System.out.println("Subscribed to "+mac+"/subscribe"+" Error="+e.getMessage());
        	e.printStackTrace();
            Log.e(LOG_TAG, "Subscription error.", e);
        }
		
	}
	
	public void setNotificationListener(NotificationListener l, IOTMessageListener listener) {
		receiver.setNotificationListener(l, listener);
	}

}
