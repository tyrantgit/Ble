package tyrant.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Administrator on 2015/7/1.
 */
public class BleScanScheduler {

    private final BleGattService bleGattService;
    private BleScanner bleScanner;
    private final Set<BleScanner.CallBack> bleScannerCallBacks;
    private final BluetoothAdapter bluetoothAdapter;
    private final UUID serviceUUID;

    public BleScanScheduler(BleGattService bleGattService, BluetoothAdapter bluetoothAdapter, UUID serviceUUID) {
        this.bleGattService = bleGattService;
        this.bluetoothAdapter = bluetoothAdapter;
        this.serviceUUID = serviceUUID;
        this.bleScannerCallBacks = new LinkedHashSet();
    }

    public synchronized boolean registerScanListener(BleScanner.CallBack callBack) {
        if(bleGattService.isBleSupported() && bleGattService.isBluetoothEnabled() && !bleScannerCallBacks.contains(callBack)) {
            bleScannerCallBacks.add(callBack);
            if(bleScanner == null) {
                bleScanner = new BleScanner(bluetoothAdapter,serviceUUID,new InternalScanListener());
                bleScanner.scan();
            }
            return true;
        }
        return false;
    }

    public synchronized void unregisterScanListener(BleScanner.CallBack callBack) {
        if(bleScanner != null && bleScannerCallBacks.remove(callBack) && bleScannerCallBacks.isEmpty()) {
            bleScanner.stop();
        }
    }

    private synchronized void stopScanning() {
        bleScannerCallBacks.clear();
        if(bleScanner != null) {
            bleScanner.stop();
            bleScanner = null;
        }

    }

    private class InternalScanListener implements BleScanner.CallBack {

        @Override
        public void onBleDeviceFound(BluetoothDevice device) {
            for(BleScanner.CallBack callback:bleScannerCallBacks) {
                callback.onBleDeviceFound(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            for(BleScanner.CallBack callback:bleScannerCallBacks) {
                callback.onScanFailed(errorCode);
            }
            stopScanning();
        }
    }
}
