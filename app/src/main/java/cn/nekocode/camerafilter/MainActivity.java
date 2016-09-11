/*
 * Copyright 2016 nekocode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package cn.nekocode.camerafilter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_SMS_PERMISSION = 102;
    private CameraRenderer renderer;
    private TextureView textureView;
    private View crosshairs;
    private View hitcrosshairs;
    private View healthbar3;
    private View healthbar2;
    private View healthbar1;
    private View healthbar0;
    private TextView countdown;
    private int filterId = R.id.filter0;
    private static final int MAXIMUM_CHARGES = 5;
    private static final int MAXIMUM_HEALTH = 3;
    private int health = 3;
    private int charges = 3;
    private Handler handler;
    private View.OnTouchListener laserListener;
    private int deathCount=10;
    private final static int MY_PERMISSIONS_REQUEST_SEND_SMS=500;
    private EditText phonetext;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public boolean canFire(int chargesRemaining) {
        if (chargesRemaining > 0) {
            return true;
        }
        return false;
    }

    //Needs detecting color method
    public boolean fireLaser(int chargesRemaining) {
        if (canFire(chargesRemaining)) {
            setCharges(getCharges() - 1);
            if (getCharges() == 2) {
                startRecharge();
                displayHit();
                getHit();
                if(getHealth()==0)
                {
                    startRegenerateHealth();
                }
            }
        }
        return false;
    }

    public void getHit() {
        health--;
        if (getHealth() == 2) {
            startRegenerateHealth();
            healthbar3.setVisibility(View.GONE);
            healthbar2.setVisibility(View.VISIBLE);
        } else if (getHealth() == 1) {
            healthbar2.setVisibility(View.GONE);
            healthbar1.setVisibility(View.VISIBLE);
        } else if (getHealth() == 0) {
            signalDeath();
            healthbar1.setVisibility(View.GONE);
            healthbar0.setVisibility(View.VISIBLE);
        }
    }

    public void setCharges(int newCharges) {
        charges = newCharges;
    }

    public void setHealth(int newHealth) {
        health = newHealth;
    }

    public int getCharges() {
        return charges;
    }


    public void displayHit() {
        crosshairs.setVisibility(View.GONE);
        hitcrosshairs.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hitcrosshairs.setVisibility(View.GONE);
                crosshairs.setVisibility(View.VISIBLE);
            }
        }, 1000);

    }

    public int getHealth() {
        return health;
    }

    public void startRecharge() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setCharges(getCharges() + 1);
                if (getCharges() != MAXIMUM_CHARGES) {
                    handler.postDelayed(this, 5000);
                }
            }
        }, 2000);
    }

    public void startRegenerateHealth() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setHealth(getHealth() + 1);
                if (getHealth() == 2) {
                    healthbar1.setVisibility(View.GONE);
                    healthbar2.setVisibility(View.VISIBLE);
                } else if (getHealth() == 3) {
                    healthbar2.setVisibility(View.GONE);
                    healthbar3.setVisibility(View.VISIBLE);
                }
                if (getHealth() != MAXIMUM_HEALTH) {
                    handler.postDelayed(this, 2000);
                }
            }
        }, 5000);
    }

    //Add text feature and disable tapping screen
    public void signalDeath() {

        countdown.setText(Integer.toString(deathCount));
        crosshairs.setVisibility(View.GONE);
        hitcrosshairs.setVisibility(View.GONE);
        countdown.setVisibility(View.VISIBLE);
        textureView.setOnTouchListener(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                deathCount--;
                countdown.setText(Integer.toString(deathCount));
                if (deathCount != 0) {
                    handler.postDelayed(this, 1000);
                } else {
                    resetLife();
                }
            }
        }, 1000);
    }

    public void resetLife() {
        setHealth(MAXIMUM_HEALTH);
        setCharges(MAXIMUM_CHARGES);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle("Laser Tag");

        crosshairs= findViewById(R.id.crosshairs);
        hitcrosshairs= findViewById(R.id.hitcrosshairs);
        countdown= (TextView) findViewById(R.id.countdown);
        healthbar3= findViewById(R.id.healthbar3);
        healthbar2= findViewById(R.id.healthbar2);
        healthbar1= findViewById(R.id.healthbar1);
        healthbar0= findViewById(R.id.healthbar0);
        phonetext= (EditText) findViewById(R.id.phonetext);

        handler= new Handler();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Camera access is required.", Toast.LENGTH_SHORT).show();

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }

        } else {
            setupCameraPreviewView();
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                Toast.makeText(this, "SMS access is required.", Toast.LENGTH_SHORT).show();

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                        REQUEST_SMS_PERMISSION);
            }

        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupCameraPreviewView();
                }
            }
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }

    void setupCameraPreviewView() {
        renderer = new CameraRenderer(this);
        textureView = (TextureView) findViewById(R.id.textureView);
        assert textureView != null;
        textureView.setSurfaceTextureListener(renderer);

        // Show original frame when touch the view
        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:


                        screenTapped();

                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:

                        break;
                }
                return true;
            }
        });

        textureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                renderer.onSurfaceTextureSizeChanged(null, v.getWidth(), v.getHeight());
            }
        });
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter, menu);
        return true;
    }*/







    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        filterId = item.getItemId();

        // TODO: need tidy up
        if (filterId == R.id.capture) {
            Toast.makeText(this,
                    caputre() ? "The capture has been saved to your sdcard root path." :
                            "Save failed!",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        setTitle(item.getTitle());

        if (renderer != null)
            renderer.setSelectedFilter(filterId);

        return true;
    }*/

    private boolean caputre() {

        // create bitmap screen capture
        Bitmap bitmap = textureView.getBitmap();
        int picw = bitmap.getWidth();
        int pich = bitmap.getHeight();

        int y = pich / 2;
        int x = picw / 2;

        int[] pix = new int[1];
        bitmap.getPixels(pix, 0, picw, x, y, 1, 1);

        int R;
        int G;
        int B;

        R = (pix[0] >> 16) & 0xff;     //bitwise shifting
        G = (pix[0] >> 8) & 0xff;
        B = pix[0] & 0xff;


        //R,G.B - Red, Green, Blue
        //to restore the values after RGB modification, use
        //next statement
        pix[0] = 0xff000000 | (R << 16) | (G << 8) | B;


        Log.d("TAG", "pix r " + R + " g " + G + " b " + B);
        OutputStream outputStream = null;


        return true;
    }

    private String genSaveFileName(String prefix, String suffix) {
        Date date = new Date();
        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String timeString = dateformat1.format(date);
        String externalPath = Environment.getExternalStorageDirectory().toString();
        return externalPath + "/" + prefix + timeString + suffix;
    }

    public void screenTapped() {

        //fireLaser(charges);
        // create bitmap screen capture
        Bitmap bitmap = textureView.getBitmap();
        int picw = bitmap.getWidth();
        int pich = bitmap.getHeight();

        int y = pich / 2;
        int x = picw / 2;

        int[] pix = new int[1];
        bitmap.getPixels(pix, 0, picw, x, y, 1, 1);

        int R;
        int G;
        int B;

        R = (pix[0] >> 16) & 0xff;     //bitwise shifting
        G = (pix[0] >> 8) & 0xff;
        B = pix[0] & 0xff;


        //R,G.B - Red, Green, Blue
        //to restore the values after RGB modification, use
        //next statement
        pix[0] = 0xff000000 | (R << 16) | (G << 8) | B;


        Log.d("TAG", "pix r " + R + " g " + G + " b " + B);

        boolean hit;
        int[] rgb = new int[3];
        rgb[0] = 40;
        rgb[1] = 40;
        rgb[2] = 40;


        hit = checkColor(R, G, B, rgb, 20);



        Log.d("TAG", "Detected Object: " + hit);
        if(hit==true)
        {



            if(phonetext!=null) {
                sendText(phonetext.getText(), "You have been shot");
            }
        }
    }

    public boolean checkColor(int r, int g, int b, int[] rgb, int error) {
        int red = rgb[0];
        int green = rgb[1];
        int blue = rgb[2];

        boolean hit = false;
        if (((red - error) <= r && r <= (red + error)) &&
                ((blue - error) <= b && b <= (blue + error)) &&
                ((green - error) <= r && r <= (green + error))) {
            hit = true;
        }
        return hit;

    }

    //Start of game
    public int[] configureOpponent() {

        //Take a picture and assign the rgb values to that picture
        // create bitmap screen capture
        Bitmap bitmap = textureView.getBitmap();
        int picw = bitmap.getWidth();
        int pich = bitmap.getHeight();

        int y = pich / 2;
        int x = picw / 2;

        int[] pix = new int[1];
        bitmap.getPixels(pix, 0, picw, x, y, 1, 1);

        int R;
        int G;
        int B;

        R = (pix[0] >> 16) & 0xff;     //bitwise shifting
        G = (pix[0] >> 8) & 0xff;
        B = pix[0] & 0xff;


        //R,G.B - Red, Green, Blue
        //to restore the values after RGB modification, use
        //next statement
        pix[0] = 0xff000000 | (R << 16) | (G << 8) | B;


        Log.d("TAG", "pix r " + R + " g " + G + " b " + B);

        boolean hit;
        int[] rgb = new int[3];
        rgb[0] = R;
        rgb[1] = G;
        rgb[2] = B;


        return rgb;

    }

    //Display Instructions
    public void displayInstructions() {


    }

    //Start Countdown
    public void startCountdown() {

    }

    public void receiveSignal()
    {

    }

    public void sendSignal()
    {

    }

    public void sendText(Editable phoneNo, String msg){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo.toString(), null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Opponenet Hit!",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cn.nekocode.camerafilter/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cn.nekocode.camerafilter/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
