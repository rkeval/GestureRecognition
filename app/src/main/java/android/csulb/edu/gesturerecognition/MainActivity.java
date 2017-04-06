package android.csulb.edu.gesturerecognition;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity implements
        GestureOverlayView.OnGesturePerformedListener, GestureOverlayView.OnGesturingListener,
        TextToSpeech.OnInitListener {

    private static final String LOG_TAG = MainActivity.class
            .getName();
    private GestureLibrary gestureLib;
    private Location currentLocation;
    private LocationManager mLocationManager;
    private String locationProvider;
    VibratorUI vUI;
    TextToSpeech ttsSpeaker;

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.landing_screen,
                null);
        gestureOverlayView.addView(inflate);
        gestureOverlayView.addOnGesturePerformedListener(this);
        gestureOverlayView.setGestureColor(Color.CYAN);
        gestureOverlayView.setUncertainGestureColor(Color.GRAY);
        gestureOverlayView.addOnGesturingListener(this);
        gestureLib = GestureLibraries.fromRawResource(this,
                R.raw.gestures);
        if (!gestureLib.load()) {
            finish();
        }
        setContentView(gestureOverlayView);

        vUI = new VibratorUI((Vibrator) getSystemService(VIBRATOR_SERVICE));
        ttsSpeaker = new TextToSpeech(this, this);
        ttsSpeaker.setSpeechRate((float) 1.0);

    }

    @Override
    protected void onResume() {
//		initLocationService();
//		mLocationManager.requestLocationUpdates(locationProvider, 4000, 100,
//				this);
        super.onResume();
    }

    @Override
    protected void onDestroy() {

        if (ttsSpeaker != null) {
            ttsSpeaker.stop();
            ttsSpeaker.shutdown();
        }
        super.onDestroy();
    }

    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
        // for (Prediction prediction : predictions) {
        if (predictions.size() > 0) {
            if (predictions.get(0).score > 2.0) {
                // Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT)
                // .show();
                switch (Integer.parseInt(predictions.get(0).name.trim())) {

                    case GlobalValues.GESTURE_NAVIGATE_LEFT:
                        Toast.makeText(this, "Gesture left performed.",
                                Toast.LENGTH_SHORT).show();
                        // write geocoding code here.
                        if (currentLocation == null) {
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            currentLocation = mLocationManager
                                    .getLastKnownLocation(locationProvider);
                        }

                        Geocoder gCoder = new Geocoder(this);
                        ArrayList<Address> addresses = new ArrayList<Address>();
                        try {
                            addresses = (ArrayList<Address>) gCoder
                                    .getFromLocation(currentLocation.getLatitude(),
                                            currentLocation.getLongitude(), 1);
                        } catch (IOException e) {
                            say("Location is not available. Pleas try after some time!");
                            e.printStackTrace();
                        }// getFromLocation(123456789, 123456789, 1);
                        if (addresses != null && addresses.size() > 0) {
                            // Toast.makeText(
                            // this,
                            // "country: " + addresses.get(0).getCountryName(),
                            // Toast.LENGTH_LONG).show();
                            Log.d(LOG_TAG, "Address :"
                                    + addresses.get(0).getAddressLine(0) + " "
                                    + addresses.get(0).getAddressLine(1) + " "
                                    + addresses.get(0).getMaxAddressLineIndex());
                            say("You are at " + addresses.get(0).getAddressLine(0)
                                    + " " + addresses.get(0).getAddressLine(1));
                        }
                        break;

                    case GlobalValues.GESTURE_NAVIGATE_RIGHT:
                        Intent contactIntent = new Intent(
                                MainActivity.this,
                                ContactListActivity.class);
                        startActivity(contactIntent);
                        break;
                    case GlobalValues.GESTURE_NAVIGATE_UP:
                        Toast.makeText(this, "Gesture up performed.",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case GlobalValues.GESTURE_NAVIGATE_DOWN:
                        Toast.makeText(this, "Gesture down performed.",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case GlobalValues.GESTURE_NAVIGATE_ANTI_CLOCK_ROUND:
                        Toast.makeText(this, "Gesture search performed.",
                                Toast.LENGTH_SHORT).show();
                        break;
                }

                // Toast.makeText(this, predictions.get(0).name,
                // Toast.LENGTH_SHORT).show();
            } else {
                vUI.vibrate(VibratorUI.MEDIUM_GAP);
            }

        }
    }

    public void onGesturingEnded(GestureOverlayView overlay) {
        // vUI.vibrate(VibratorUI.DOT);
    }

    public void onGesturingStarted(GestureOverlayView overlay) {
        vUI.vibrate(VibratorUI.DOT);
    }



    public void onInit(int arg0) {
        say("Welcome to project third eye!");

    }

    private void say(String text2Speak) {
        ttsSpeaker.speak(text2Speak, TextToSpeech.QUEUE_FLUSH, null);

    }

    private BroadcastReceiver battery_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPresent = intent.getBooleanExtra("present", false);
            // String technology = intent.getStringExtra("technology");
            int plugged = intent.getIntExtra("plugged", -1);
            int scale = intent.getIntExtra("scale", -1);
            // int health = intent.getIntExtra("health", 0);
            int status = intent.getIntExtra("status", 0);
            int rawlevel = intent.getIntExtra("level", -1);
            int level = 0;

            Bundle bundle = intent.getExtras();

            Log.d(LOG_TAG, "Battery level:" + bundle.toString());

            if (isPresent) {
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }

                String info = "Battery Level is" + level + "%\n";

                info += ("Battery is " + getPlugTypeString(plugged) + "\n");

                info += ("and Status is " + getStatusString(status) + "\n");

                setBatteryLevelText(info);// );
            } else {
                setBatteryLevelText("Battery not present!!!");
            }
        }
    };

    private String getPlugTypeString(int plugged) {
        String plugType = "Unplugged";

        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                plugType = "Plugged with AC adapter.";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                plugType = "Plugged with USB.";
                break;
        }

        return plugType;
    }

    private String getStatusString(int status) {
        String statusString = "Unknown";

        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusString = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                statusString = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                statusString = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                statusString = "Not Charging";
                break;
        }

        return statusString;
    }

    private void setBatteryLevelText(String text) {
        say(text);
    }

    private void registerBatteryLevelReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(battery_receiver, filter);
    }

}
