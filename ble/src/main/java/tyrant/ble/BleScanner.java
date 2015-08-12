package tyrant.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.ParcelUuid;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2015/7/1.
 */
public class BleScanner {

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private final CallBack callBack;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private ScanCallback scanCallback;
    private final UUID serviceUUID;

    public BleScanner(BluetoothAdapter bluetoothAdapter, UUID uuid, CallBack callBack) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.serviceUUID = uuid;
        this.callBack = callBack;
    }

    protected boolean isTargetBleServer(BluetoothDevice device,byte[] scanRecord) {
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void startScanJellybean() {
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if(isTargetBleServer(device,scanRecord)) {
                    callBack.onBleDeviceFound(device);
                }
            }
        };
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    @TargetApi(21)
    private void startScanLollipop() {
        scanCallback = new ScanCallback(){
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for(ScanResult result : results) {
                    callBack.onBleDeviceFound(result.getDevice());
                }
            }

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                callBack.onBleDeviceFound(result.getDevice());
            }

            @Override
            public void onScanFailed(int errorCode) {
                callBack.onScanFailed(errorCode);
            }
        };
        List filters = Arrays.asList(new ScanFilter[] { new ScanFilter.Builder().setServiceUuid(new ParcelUuid(serviceUUID)).build() });
        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(filters, scanSettings, scanCallback);
    }

    @TargetApi(21)
    private void stopScanLollipop() {
        bluetoothLeScanner.stopScan(scanCallback);
        bluetoothLeScanner = null;
        scanCallback = null;
    }

    public synchronized void scan() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startScanLollipop();
        } else {
            startScanJellybean();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public synchronized void stop() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stopScanLollipop();
        } else {
            bluetoothAdapter.stopLeScan(leScanCallback);
            leScanCallback = null;
        }
    }

    public interface CallBack {

         void onBleDeviceFound(BluetoothDevice device);

         void onScanFailed(int errorCode);
    }
}
