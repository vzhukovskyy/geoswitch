package ua.pp.rudiki.geoswitch.peripherals;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ua.pp.rudiki.geoswitch.App;


public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = BluetoothBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        boolean deviceConnected = action.equals(BluetoothDevice.ACTION_ACL_CONNECTED);
        boolean deviceDisconnected = action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        int deviceClass = getDeviceClass(intent);
        App.getLogger().logBluetooth(TAG, action+" device name: "+getDeviceName(intent)+", class:"+deviceClass);

        if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE) {
            if(deviceConnected) {
                App.getGpsServiceActivator().connectedViaBluetooth();
            }
            else if (deviceDisconnected) {
                App.getGpsServiceActivator().disconnectedViaBluetooth();
            }
        }
    }

    private int getDeviceClass(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if(device != null) {
            BluetoothClass bluetoothClass = device.getBluetoothClass();
            if(bluetoothClass != null) {
                return bluetoothClass.getDeviceClass();
            }
        }
        return -1;
    }

    private String getDeviceName(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if(device != null) {
            return device.getName();
        }
        return "<none>";
    }

    public static boolean isConnected() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED;
    }

//    public static boolean isConnected() {
//        BluetoothManager bluetoothManager = (BluetoothManager)App.getAppContext().getSystemService(BLUETOOTH_SERVICE);
//        List<BluetoothDevice> connectedDevices = bluetoothManager.getConnectedDevices(GATT);
//        dumpDevices(connectedDevices);
//        return connectedDevices.size() > 0;
//    }

//    private static void dumpDevices(Collection<BluetoothDevice> devices) {
//        for (BluetoothDevice d: devices) {
//            String deviceName = d.getName();
//            String macAddress = d.getAddress();
//            App.getLogger().logBluetooth(TAG, "device: " + deviceName + " at " + macAddress);
//        }
//    }
}
