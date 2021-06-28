package it.pgp.dualscrolldrawexample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.widget.SeekBar;

public class MainActivity extends Activity {
    SeekBar lightBar;
    Context context;
    int brightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
//        lightBar = findViewById(R.id.seekBar);
//        context = getApplicationContext();
//        brightness =
//                Settings.System.getInt(context.getContentResolver(),
//                        Settings.System.SCREEN_BRIGHTNESS, 0);
//        lightBar.setProgress(brightness);
//        lightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Settings.System.putInt(context.getContentResolver(),
//                        Settings.System.SCREEN_BRIGHTNESS, progress);
//            }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) { }
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) { }
//        });
    }
}