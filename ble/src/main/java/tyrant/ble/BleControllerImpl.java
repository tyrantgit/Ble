package tyrant.ble;

import android.content.Context;

import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Created by Administrator on 2015/7/1.
 */
public class BleControllerImpl implements BleController{

    private BleChannel bleChannel;
    private final BleGattService bleGattService;
    private final BleScanScheduler bleScanScheduler;
    private final Context context;
    private final Executor executor;

    public BleControllerImpl(Context context, Executor executor, BleGattService bleGattService, BleScanScheduler scheduler) {
        this.context = context;
        this.bleGattService = bleGattService;
        this.executor = executor;
        this.bleScanScheduler = scheduler;
    }

    @Override
    public synchronized boolean connect(String address, BleConnection.CallBack callBack) {
        if(isBleSupported() && isBluetoothEnabled() && bleChannel == null) {
            bleChannel = new BleChannel(context, executor,bleGattService,address,bleScanScheduler,callBack);
            return bleChannel.start();
        }
        return false;
    }



    @Override
    public synchronized void disconnect(String address) {
        if(bleChannel != null && bleChannel.close(address)) {
            bleChannel = null;
        }
    }

    @Override
    public boolean isBleSupported() {
        return bleGattService.isBleSupported();
    }

    @Override
    public boolean isBluetoothEnabled() {
        return bleGattService.isBluetoothEnabled();
    }

    @Override
    public synchronized boolean registerScanListener(BleScanner.CallBack callBack) {
        return bleScanScheduler.registerScanListener(callBack);
    }

    @Override
    public synchronized void send(UUID uuid, byte[] data) {
        if(bleChannel == null) {
            throw new IllegalStateException("NO established BLE channel!");
        }
        bleChannel.send(uuid,data);
    }

    @Override
    public synchronized void read(UUID uuid) {
        if(bleChannel == null) {
            throw new IllegalStateException("NO established BLE channel!");
        }
        bleChannel.read(uuid);
    }

    @Override
    public synchronized void unregisterScanListener(BleScanner.CallBack callBack) {
        bleScanScheduler.unregisterScanListener(callBack);
    }
}
