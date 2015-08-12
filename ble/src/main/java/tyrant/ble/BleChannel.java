package tyrant.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Created by Administrator on 2015/7/1.
 */
public class BleChannel implements BleConnection.CallBack ,BleScanner.CallBack{

    private final String address;
    private BleConnection bleConnection;
    private final BleConnection.CallBack bleConnectionCallBack;
    private final BleGattService bleGattService;
    private final BleScanScheduler bleScanScheduler;
    private final Context context;
    private boolean isClosed;
    private final Executor executor;

    public BleChannel(Context context, Executor executor, BleGattService bleGattService, String address, BleScanScheduler scanScheduler, BleConnection.CallBack callBack) {
        this.context = context;
        this.executor = executor;
        this.bleGattService = bleGattService;
        this.address = address;
        this.bleScanScheduler = scanScheduler;
        this.bleConnectionCallBack = callBack;
    }

    public void reconnect() {
        if (!isClosed) {
            bleConnection = null;
            bleScanScheduler.registerScanListener(this);
        }
    }

    public synchronized boolean close(String address) {
        if(address.equals(this.address)) {
            isClosed = true;
            bleScanScheduler.unregisterScanListener(this);
            if(bleConnection != null) {
                bleConnection.close();
                bleConnection = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public synchronized void onCharacteristicRead(UUID uuid, byte[] data) {
        bleConnectionCallBack.onCharacteristicRead(uuid, data);
    }

    @Override
    public synchronized void onCharacteristicWrite() {
        bleConnectionCallBack.onCharacteristicWrite();
    }

    @Override
    public synchronized void onConnected() {
        bleConnectionCallBack.onConnected();

    }

    @Override
    public synchronized void onDisconnected() {
        reconnect();
        bleConnectionCallBack.onDisconnected();

    }

    @Override
    public synchronized void onBleDeviceFound(BluetoothDevice device) {
        if(bleConnection != null && device.getAddress().equals(address)) {
            bleConnection = new BleConnection(context, executor,device,bleGattService,this);
            bleConnection.connect();
            bleScanScheduler.unregisterScanListener(this);
        }

    }

    @Override
    public synchronized void onScanFailed(int errorCode) {
        reconnect();
    }

    public synchronized void send(UUID uuid, byte[] data) {
        if(bleConnection != null) {
            bleConnection.send(uuid,data);
        }
    }

    public synchronized void read(UUID uuid) {
        if(bleConnection != null) {
            bleConnection.read(uuid);
        }
    }

    public synchronized boolean start(){
        if(bleConnection == null) {
            return false;
        }
        isClosed = false;
        bleScanScheduler.registerScanListener(this);
        return true;
    }
}
