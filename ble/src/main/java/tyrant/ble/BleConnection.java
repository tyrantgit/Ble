package tyrant.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Created by Administrator on 2015/7/1.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleConnection extends BluetoothGattCallback{

    private final BleGattService bleGattService;
    private final BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService bluetoothGattService;
    private final CallBack callBack;
    private final Context context;
    private final Executor executor;

    public BleConnection(Context context, Executor executor, BluetoothDevice bluetoothDevice, BleGattService bleGattService, CallBack callBack) {
        this.context = context;
        this.executor = executor;
        this.bluetoothDevice = bluetoothDevice;
        this.bleGattService = bleGattService;
        this.callBack = callBack;
    }

    public synchronized void close() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
            bluetoothGattService = null;
            callBack.onDisconnected();
        }
    }

    public void connect() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (BleConnection.this) {
                    bluetoothGatt =  bluetoothDevice.connectGatt(context,false,BleConnection.this);
                    if(bluetoothGatt == null || !bluetoothGatt.connect()) {
                        close();
                    }
                }
            }
        });
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if(bleGattService.containsCharacteristic(characteristic.getUuid())) {
            callBack.onCharacteristicRead(characteristic.getUuid(), characteristic.getValue());
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if(bleGattService.containsCharacteristic(characteristic.getUuid())) {
            callBack.onCharacteristicRead(characteristic.getUuid(), characteristic.getValue());
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if(status == BluetoothGatt.GATT_SUCCESS) {
            callBack.onCharacteristicWrite();
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if(status != BluetoothGatt.GATT_SUCCESS) {
            close();
        } else if(newState != BluetoothProfile.STATE_DISCONNECTED) {
            bluetoothGatt.discoverServices();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if(gatt != null && status == BluetoothGatt.GATT_SUCCESS) {
            bluetoothGattService = gatt.getService(bleGattService.getServiceUUID());
            if(bluetoothGattService != null && bleGattService.initializeCharacteristics(bluetoothGattService,gatt)) {
                callBack.onConnected();
                return;
            }
        }
        close();
    }

    public synchronized void send(UUID uuid,byte[] value) {
        if(bluetoothGattService != null) {
            BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(uuid);
            characteristic.setValue(value);
            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public synchronized void read(UUID uuid) {
        if(bluetoothGattService != null) {
            BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(uuid);
            bluetoothGatt.readCharacteristic(characteristic);
        }
    }


    public interface CallBack {

         void onCharacteristicRead(UUID uuid, byte[] data);

         void onCharacteristicWrite();

         void onConnected();

         void onDisconnected();
    }
}
