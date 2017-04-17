package avion.piedrapapellagarto.events;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

public class Bus {
    public static EventBus bus() {
        return EventBus.getDefault();
    }

    public static void post(Object object) {
        Timber.d("SENT EVENT ----[ %s ]--->", object);
        EventBus.getDefault().post(object);
    }
}
