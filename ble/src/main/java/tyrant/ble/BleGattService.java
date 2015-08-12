package tyrant.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Administrator on 2015/7/1.
 */

public class BleGattService {
    private UUID clientCharacteristicConfig;
    private final BluetoothAdapter bluetoothAdapter;
    private final Set<UUID> characteristicUUIDs;
    private final UUID serviceUUID;

    public BleGattService(BluetoothAdapter bluetoothAdapter, UUID serviceUUID, Set<UUID> characteristicUUIDs, UUID clientCharacteristicConfig) {
        this.serviceUUID = serviceUUID;
        this.characteristicUUIDs = new LinkedHashSet(characteristicUUIDs);
        this.bluetoothAdapter = bluetoothAdapter;
        this.clientCharacteristicConfig = clientCharacteristicConfig;
    }

    public BleGattService(BluetoothAdapter bluetoothAdapter, UUID serviceUUID, Set<UUID> characteristicUUIDs) {
        this(bluetoothAdapter, serviceUUID, characteristicUUIDs, null);
    }

    public static UUID uuid16(String s) {
        return UUID.fromString("0000" + s + "-0000-1000-8000-00805F9B34FB");
    }

    public boolean containsCharacteristic(UUID uuid) {
        return characteristicUUIDs.contains(uuid);
    }

    public UUID getServiceUUID() {
        return serviceUUID;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean initializeCharacteristics(BluetoothGattService bluetoothGattService, BluetoothGatt bluetoothGatt) {
        for (UUID uuid : characteristicUUIDs) {
            BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(uuid);
            if (characteristic == null) {
                return false;
            }

            if (clientCharacteristicConfig != null) {
                if (bluetoothGatt.setCharacteristicNotification(characteristic, true)) {
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(clientCharacteristicConfig);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(descriptor);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isBleSupported() {
        return (Build.VERSION.SDK_INT >= 18) && (bluetoothAdapter != null);
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }
}
