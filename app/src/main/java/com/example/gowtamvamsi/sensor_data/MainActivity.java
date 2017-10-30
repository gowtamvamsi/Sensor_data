package com.example.gowtamvamsi.sensor_data;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.CallLog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;


import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;


import android.media.MediaPlayer;
import android.media.MediaRecorder;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends Activity implements SensorEventListener, LocationListener {

    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    //private final float NOISE = (float) 2.0;
    private String filename = "Samplesensordata.csv";
    private String filename1 = "gps.csv";
    private String filename2 = "sounddata.csv";
    private String filename3 = "calldata.csv";
    private String filepath = "Internal storage";
    private File myExternalFile;
    private File myExternalFile1;
    private File myExternalFile2;
    private File myExternalFile3;
    private FileOutputStream fos;
    private FileOutputStream fos1;
    private FileOutputStream fos2;
    private FileOutputStream fos3;
    private MyCountDownTimer myCountDownTimer;
    private Button btn;
    private TextView latituteField;
    private TextView longitudeField;
    private LocationManager locationManager;
    private String provider;
    private String bestProvider;
    private String AudioSavePathInDevice = null;
    private MediaRecorder mediaRecorder;
    public static final int RequestPermissionCode = 1;

    //location

    public void onLocationChanged(Location location) {
        float lat = (float) (location.getLatitude());
        float lng = (float) (location.getLongitude());
        latituteField.setText(String.valueOf(lat));
        longitudeField.setText(String.valueOf(lng));
        try {


            fos1 = new FileOutputStream(myExternalFile1, true);
            fos1.write(Float.toString(lat).getBytes());
            fos1.write(',');
            fos1.write(Float.toString(lng).getBytes());
            fos1.write('\n');
            fos1.flush();
            fos1.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    //microphone
    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }


    //countdowntimer

    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }


        @Override
        public void onTick(long millisUntilFinished) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            bestProvider = locationManager.getBestProvider(criteria, false);


            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, MainActivity.this);
            Location location = locationManager.getLastKnownLocation(bestProvider);

            // Initialize the location fields
            if (location != null) {
                System.out.println("Provider " + provider + " has been selected.");
                onLocationChanged(location);
            } else {
                latituteField.setText("Location not available");
                longitudeField.setText("Location not available");
            }
            try {


                fos2 = new FileOutputStream(myExternalFile2, true);
                fos2.write(String.valueOf(mediaRecorder.getMaxAmplitude()).getBytes());
                fos2.write('\n');
                fos2.flush();
                fos2.close();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFinish() {
            mSensorManager.unregisterListener(MainActivity.this);
            TextView tvX = (TextView) findViewById(R.id.x_axis);
            TextView tvY = (TextView) findViewById(R.id.y_axis);
            TextView tvZ = (TextView) findViewById(R.id.z_axis);
            tvX.setText("0.0");
            tvY.setText("0.0");
            tvZ.setText("0.0");
            latituteField.setText("finished");
            longitudeField.setText("finished");
            mediaRecorder.stop();
            btn.setEnabled(true);

        }
    }
    //calls data

    public class CallLogObserver extends ContentObserver {
        private Context context;

        public CallLogObserver(Handler handler, Context context) {
            super(handler);
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Cursor c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
                if (c != null) {
                    if (c.moveToFirst()) {
                        int type = Integer.parseInt(c.getString(c
                                .getColumnIndex(CallLog.Calls.TYPE)));
                    /*
                     * increase call counter for outgoing call only
                     */
                        if (type == 2) {
                            String number = c.getString(c
                                    .getColumnIndex(CallLog.Calls.NUMBER));

                            long duration = c.getLong(c
                                    .getColumnIndex(CallLog.Calls.DURATION));
                            fos3 = new FileOutputStream(myExternalFile3,true);
                            fos3.write(number.getBytes());
                            fos3.write(',');
                            fos3.write(String.valueOf(type).getBytes());
                            fos3.write(',');
                            fos3.write(String.valueOf(duration).getBytes());
                            fos3.write('\n');
                            fos3.flush();
                            fos3.close();

                            //Log.i(TAG, "numer = " + number + " type = " + type + " duration = " + duration);

                        }
                    }
                    c.close();
                }
            } catch (Exception e) {
                //Log.e(TAG, "Error on onChange : " + e.toString());
            }

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //System.out.println(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        TextView tvX = (TextView) findViewById(R.id.x_axis);
        TextView tvY = (TextView) findViewById(R.id.y_axis);
        TextView tvZ = (TextView) findViewById(R.id.z_axis);
        tvX.setText("0.0");
        tvY.setText("0.0");
        tvZ.setText("0.0");
        if (!(!isExternalStorageAvailable() || isExternalStorageReadOnly())) {
            myExternalFile = new File(getExternalFilesDir(filepath), filename);
        }
        if (!(!isExternalStorageAvailable() || isExternalStorageReadOnly())) {
            myExternalFile1 = new File(getExternalFilesDir(filepath), filename1);
        }
        if (!(!isExternalStorageAvailable() || isExternalStorageReadOnly())) {
            myExternalFile2 = new File(getExternalFilesDir(filepath), filename2);
        }
        if (!(!isExternalStorageAvailable() || isExternalStorageReadOnly())) {
            myExternalFile3 = new File(getExternalFilesDir(filepath), filename3);
        }
        btn = findViewById(R.id.button_send);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.hours);
                float hr = Float.valueOf(editText.getText().toString());
                hr = hr * 60 * 60 * 1000;
                int hrs = (int) hr;
                myCountDownTimer = new MyCountDownTimer(hrs, 1000);
                myCountDownTimer.start();
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensorManager.registerListener(MainActivity.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                latituteField = (TextView) findViewById(R.id.TextView02);
                longitudeField = (TextView) findViewById(R.id.TextView04);
                Random rand = new Random();

                int  n = rand.nextInt(10000) + 1;
                if(checkPermission()) {

                    AudioSavePathInDevice = getExternalFilesDir(filepath)+"/"+String.valueOf(n)+"rec.3gp";

                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                } else {
                    requestPermission();
                }
                 Context context=MainActivity.this;
                ContentResolver contentResolver = context.getContentResolver();
                CallLogObserver mObserver = new CallLogObserver(new Handler(), context);
                contentResolver.registerContentObserver(Uri.parse("content://call_log/calls"), true, mObserver);






            }
        });




    }
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }



    protected void onResume() {
        super.onResume();

    }

    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored for this demo
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView tvX= (TextView)findViewById(R.id.x_axis);
        TextView tvY= (TextView)findViewById(R.id.y_axis);
        TextView tvZ= (TextView)findViewById(R.id.z_axis);

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            tvX.setText("0.0");
            tvY.setText("0.0");
            tvZ.setText("0.0");
            mInitialized = true;
        } else {

            tvX.setText(Float.toString(x));
            tvY.setText(Float.toString(y));
            tvZ.setText(Float.toString(z));
            try {


                fos = new FileOutputStream(myExternalFile,true);
                fos.write(Float.toString(x) .getBytes());
                fos.write(',');
                fos.write(Float.toString(y) .getBytes());
                fos.write(',');
                fos.write(Float.toString(z) .getBytes());
                fos.write('\n');
                fos.flush();
                fos.close();



            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}