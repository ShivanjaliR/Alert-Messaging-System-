package com.example.shivanjali.remainder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ToDoListActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Location mLastLocation;
    ArrayList<String> list = new ArrayList<String>();
    SingleItem adapter = null;
    GoogleApiClient mGoogleApiClient;
    String TAG = "ToDoListActivity";
    static String Latitude = "test";
    static String Longitude = "test";
    static String str = "test";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //generate list
        list.add("item1");
        list.add("item2");

        //instantiate custom adapter
        adapter = new SingleItem(list, this);

        //handle listview and assign adapter
        ListView lView = (ListView) findViewById(R.id.mylistview);
        lView.setAdapter(adapter);

        try {


            Button buttonOne = (Button) findViewById(R.id.addbutton);
            buttonOne.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    //Do stuff here
                    Intent intent = new Intent(ToDoListActivity.this, AddNewItemActivity.class);
                    startActivityForResult(intent, 1);
                }
            });
            getRealLocation();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("loc", "" + mLastLocation.getLatitude());
            Log.d("loc", "" + mLastLocation.getLongitude());
        } else {
            Toast.makeText(this, "No location detected", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            list.add(data.getStringExtra("result"));
            adapter.notifyDataSetChanged();
            sendNotification();
        }
    }

    public static final String ACCOUNT_SID = "ACec2823ce0d71e1c3af1286ec7bf9d308";
    public static final String AUTH_TOKEN = "f034b7c081a9a73bbdebd17d04e94b7c";

    public void sendNotification() {
        //str = "test";
        if (mLastLocation != null) {
            Latitude = String.valueOf(mLastLocation.getLatitude());
            Longitude = String.valueOf(mLastLocation.getLongitude());
            str = String.valueOf(mLastLocation.getLatitude());
            str += " " + String.valueOf(mLastLocation.getLongitude());
        }
        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(mPhoneNumber, null, str, null, null);
    }

    public void getRealLocation() throws JSONException {

        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://www.google.com";
            StringRequest stringRequest;
            // Instantiate the RequestQueue.
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("Latitude", Latitude);
            jsonBody.put("Longitude", Longitude);
            String mRequestBody = jsonBody.toString();

            // Request a string response from the provided URL.
            stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            System.out.println("Response is: " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("That didn't work!");
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }

            };

            // Add the request to the RequestQueue.
            queue.add(stringRequest);

        }//end of try
        catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
