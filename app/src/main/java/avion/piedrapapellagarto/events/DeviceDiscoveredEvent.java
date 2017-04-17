package avion.piedrapapellagarto.events;

import android.bluetooth.BluetoothDevice;

public class DeviceDiscoveredEvent {
    public final BluetoothDevice bluetoothDevice;

    public DeviceDiscoveredEvent(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }
}
