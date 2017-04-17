package avion.piedrapapellagarto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import avion.piedrapapellagarto.screen.GameFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GameFragment gameFragment = new GameFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_container, gameFragment)
                .commit();
    }
}
