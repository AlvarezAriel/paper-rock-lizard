package avion.piedrapapellagarto.screen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;

import avion.piedrapapellagarto.R;
import avion.piedrapapellagarto.events.Bus;
import avion.piedrapapellagarto.events.DeviceDiscoveredEvent;
import avion.piedrapapellagarto.events.StartDiscoveryEvent;
import avion.piedrapapellagarto.events.StartServerEvent;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class GameChooserFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_chooser, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.join_game)
    @SuppressWarnings(value = "unused")
    public void onScanClicked(View view) {
        Bus.post(new StartDiscoveryEvent());
    }


    @OnClick(R.id.new_game)
    @SuppressWarnings(value = "unused")
    public void onStartServer(View view) {
        Bus.post(new StartServerEvent());
    }


    @Subscribe
    @SuppressWarnings(value = "unused")
    public void onDeviceDiscovered(DeviceDiscoveredEvent event) {
        Timber.d("DEVICE DISCOVERED: %s", event.bluetoothDevice.getName());
    }

    @Override
    public void onStart() {
        super.onStart();
        Bus.bus().register(this);
    }

    @Override
    public void onStop() {
        Bus.bus().unregister(this);
        super.onStop();
    }

}
