package avion.piedrapapellagarto;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.UUID;

import avion.piedrapapellagarto.events.Bus;
import avion.piedrapapellagarto.events.DeviceDiscoveredEvent;
import avion.piedrapapellagarto.events.StartDiscoveryEvent;
import avion.piedrapapellagarto.events.StartServerEvent;
import avion.piedrapapellagarto.model.GameChoice;
import avion.piedrapapellagarto.model.Gamer;
import avion.piedrapapellagarto.screen.GameChooserFragment;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BT = 14;
    public static final int REQUEST_DISCOVERABLE_BT = 15;
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 16;
    public static final UUID MY_UUID = java.util.UUID.fromString("1111-2222-3333-4444-5555");
    private BluetoothAdapter bluetoothAdapter;
    private boolean isBluetoothAvailable = false;

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Bus.post(new DeviceDiscoveredEvent(device));
                new ConnectThread(device).start();
            }
        }
    };

    private static final int DISCOVERABLE_DURATION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fragment gameFragment = new GameChooserFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_container, gameFragment)
                .commit();

    }

    protected void init() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        registerReceiver(discoveryReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        if (bluetoothAdapter == null) {
            Timber.e("Bluetooth is not supported on this device");
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            isBluetoothAvailable = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Timber.e("ACTIVITY RESULT: requestCode=%d resultCode=%d", requestCode, resultCode);

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            isBluetoothAvailable = true;
            Timber.d("Enable bluetooth: request accepted. Bluetooth enabled.");
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            Timber.e("Enable bluetooth: request canceled");
        } else {
            if (requestCode == REQUEST_DISCOVERABLE_BT && resultCode == DISCOVERABLE_DURATION) {
                Timber.d("BLUETOOTH STATE: %d", bluetoothAdapter.getState());
                new AcceptThread().start();
            }
        }
    }

    @Subscribe
    @SuppressWarnings(value = "unused")
    public void scanServers(StartDiscoveryEvent event) {
        Timber.d("Scan for servers [isBluetoothAvailable == %s]", isBluetoothAvailable);
        askForLocationPermission();
        if (isBluetoothAvailable) {
            bluetoothAdapter.startDiscovery();
        }
    }

    @Subscribe
    @SuppressWarnings(value = "unused")
    public void startServer(StartServerEvent event) {
        Timber.d("Start server [isBluetoothAvailable == %s]", isBluetoothAvailable);
        if (isBluetoothAvailable) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT);
        }
    }

    private void askForLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                REQUEST_ACCESS_COARSE_LOCATION);
                    }
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bus.bus().register(this);
        init();
    }

    @Override
    protected void onStop() {
        Bus.bus().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(discoveryReceiver);
        super.onDestroy();
    }


    class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Lizard", MY_UUID);
            } catch (IOException e) {
                Timber.e("Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Timber.e("Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    connectedToClient(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Timber.e("Could not close the connect socket", e);
            }
        }
    }

    private void connectedToClient(BluetoothSocket client) {
        Timber.d("BLUETOOTH CONNECTION ESTABLISHED");
        try {
            byte[] bytes = new byte[1024];
            InputStream inputStream = client.getInputStream();
            int len = inputStream.read(bytes);
            String choice = new String(bytes, 0, len);
            Timber.d("RECEIVED CHOICE: %s (len=%d)", choice, len);
        } catch (IOException e) {
            Timber.e(e);
            try {
                client.close();
            } catch (IOException e1) {
                Timber.e(e);
            }
        }
    }

    private void connectedToServer(BluetoothSocket server) {
        Timber.d("BLUETOOTH CONNECTION ESTABLISHED");
        String choice = "lizard";
        try {
            server.getOutputStream().write(choice.getBytes());
        } catch (IOException e) {
            Timber.e(e);
            try {
                server.close();
            } catch (IOException e1) {
                Timber.e(e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Timber.e("Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Timber.e("Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedToServer(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Timber.e("Could not close the client socket", e);
            }
        }
    }
}
