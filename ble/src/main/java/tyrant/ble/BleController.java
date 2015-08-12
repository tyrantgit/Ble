package tyrant.ble;

import java.util.UUID;

/**
 * Created by Administrator on 2015/7/1.
 */
public interface BleController {

    boolean connect(String address, BleConnection.CallBack callBack);

    void disconnect(String address);

    boolean isBleSupported();

    boolean isBluetoothEnabled();

    boolean registerScanListener(BleScanner.CallBack callBack);

    void send(UUID uuid, byte[] data);

    void read(UUID uuid);

    void unregisterScanListener(BleScanner.CallBack callBack);


}
