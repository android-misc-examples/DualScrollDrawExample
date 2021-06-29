package it.pgp.dualscrolldrawexample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {
    CustomView customView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        customView = findViewById(R.id.customView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite(this))
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS),0);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // only 1 request type sent, no need to check requestCode
        if(!Settings.System.canWrite(this)) {
            Toast.makeText(this, "Please enable permissions to write settings, in order to set brightness programmatically", Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
        else {
            recreate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customView.restoreBrightness();
    }
}