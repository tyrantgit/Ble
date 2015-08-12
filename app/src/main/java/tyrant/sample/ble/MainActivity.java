package tyrant.sample.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

import tyrant.ble.AndroidMainThreadExecutor;
import tyrant.ble.BleConnection;
import tyrant.ble.BleController;
import tyrant.ble.BleControllerImpl;
import tyrant.ble.BleGattService;
import tyrant.ble.BleScanScheduler;
import tyrant.ble.BleScanner;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Executor executor = new AndroidMainThreadExecutor();
        UUID serverUUID = BleGattService.uuid16("1234");
        Set<UUID> characteristicUUIDs = new HashSet<>();
        final BleGattService server = new BleGattService(BluetoothAdapter.getDefaultAdapter(), serverUUID, characteristicUUIDs);
        final BleScanScheduler scheduler = new BleScanScheduler(server, BluetoothAdapter.getDefaultAdapter(), serverUUID);
        final BleController bleController = new BleControllerImpl(this, executor, server, scheduler);
        final BleConnection.CallBack callBack = new BleConnection.CallBack() {
            @Override
            public void onCharacteristicRead(UUID uuid, byte[] data) {

            }

            @Override
            public void onCharacteristicWrite() {

            }

            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected() {

            }
        };
        bleController.registerScanListener(new BleScanner.CallBack() {
            @Override
            public void onBleDeviceFound(BluetoothDevice device) {
                bleController.connect(device.getAddress(), callBack);
            }

            @Override
            public void onScanFailed(int errorCode) {

            }
        });

    }

}
