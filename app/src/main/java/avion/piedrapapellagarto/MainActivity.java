package avion.piedrapapellagarto;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    public static final String BEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    private BeaconManager beaconManager;
    @BindView(R.id.output)
    TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_game_chooser);
        ButterKnife.bind(this);
    }

    private void writeText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                output.setText(output.getText() + "\n" + text);
            }
        });
    }

    @OnClick(R.id.join_game)
    @SuppressWarnings(value = "unused")
    public void onScanClicked(View view) {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
         beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
        beaconManager.bind(this);
        writeText("Started scanning");
    }


    @OnClick(R.id.new_game)
    @SuppressWarnings(value = "unused")
    public void onStartServer(View view) {
        Beacon beacon = new Beacon.Builder()
                .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x0118)
                .setTxPower(-59)
                .setDataFields(Collections.singletonList(0L))
                .build();

        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT);
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

            @Override
            public void onStartFailure(int errorCode) {
                writeText("Advertisement start failed with code: "+errorCode);

            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                writeText("Started advertising");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                writeText("Enter region: " + region.getUniqueId());
            }

            @Override
            public void didExitRegion(Region region) {
                writeText("No beacon: " + region.getUniqueId());
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                writeText("Switched state: " + (state == 1 ? "INSIDE" : "OUTSIDE"));
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            Timber.e(e);
        }
    }
}
