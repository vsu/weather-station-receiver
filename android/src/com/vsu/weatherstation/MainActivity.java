package com.vsu.weatherstation;

import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.codebutler.android_websockets.WebSocketClient;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    
    /**
     * The instance of the web socket client.
     */
    private WebSocketClient mClient;
    
    /**
     * The current server connection state.
     */
    private boolean mServerConnected = false;    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String prefAddress = sharedPref.getString("pref_server_address", ""); 

        final TextView statusView = (TextView)findViewById(R.id.status_main);
        
        if (prefAddress.equals("")) {
            statusView.setText(getString(R.string.status_no_server_address));
        } else {
            if (!mServerConnected) {
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo network = connManager.getActiveNetworkInfo();
                
                if (!network.isConnected()) {
                    statusView.setText(getString(R.string.status_no_network));
                } else {
                    connectServer(prefAddress);
                }
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return false;
    }

    /**
     * Opens a connection to the weather station server.
     * @param location  The network location in hostname:port format.
     */
    private void connectServer(String location) {
        String uri = "ws://" + location + Constants.WS_PATH;

        List<BasicNameValuePair> extraHeaders = new ArrayList<BasicNameValuePair>();
        
        mClient = new WebSocketClient(URI.create(uri), new WebSocketClient.Listener() {
            @Override
            public void onConnect() {
               Log.d(TAG, "Web socket connected");
               mServerConnected = true;
               
               runOnUiThread(new Runnable() {
                   public void run() {
                       final TextView statusView = (TextView)findViewById(R.id.status_main);
                       statusView.setText("");
                       //Toast.makeText(getApplicationContext(), 
                       //        getResources().getString(R.string.status_server_connected), 
                       //        Toast.LENGTH_SHORT).show();
                       
                       try {
                           JSONObject json = new JSONObject();
                           json.put(Constants.KEY_OP, Constants.OP_QUERY);
                           json.put(Constants.KEY_TYPE, Constants.TYPE_TEMPERATURE);
                           mClient.send(json.toString());
                       } catch (JSONException e) {
                       }
                   }
               });
            }

            @Override
            public void onMessage(final String message) {
                Log.d(TAG, String.format("Web socket string message: %s", message));
                
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            JSONObject json = new JSONObject(message);
                            
                            if (json.has(Constants.KEY_RESULT) && json.getString(Constants.KEY_RESULT).equals(Constants.RESULT_OK)) {
                                if (json.has(Constants.KEY_DATETIME)) {
                                    long ms = json.getLong(Constants.KEY_DATETIME);
                                    Date dateTime = new Date(ms);
                                    
                                    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
                                    
                                    final TextView textView = (TextView)findViewById(R.id.label_datetime);
                                    textView.setText("Last updated: " + dateFormat.format(dateTime));
                                }
                                
                                if (json.has(Constants.TYPE_TEMPERATURE)) {
                                    final int val = json.getInt(Constants.TYPE_TEMPERATURE);
                                    final TextView textView = (TextView)findViewById(R.id.text_temperature);
                                    
                                    if (val != -1) {
                                        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                                        final String tempUnit = sharedPref.getString("pref_temp_unit", Constants.UNIT_FAHRENHEIT); 
                                        
                                        if (tempUnit.equals(Constants.UNIT_FAHRENHEIT)) {
                                            textView.setText(Math.round(10.0 * ((val * 0.18) + 32)) / 10.0 + "\u00B0F");
                                        } else {
                                            textView.setText(val / 10.0 + "\u00B0C");
                                        }
                                    } else {
                                        textView.setText("");
                                    }
                                }
                                
                                if (json.has(Constants.TYPE_HUMIDITY)) {
                                    final int val = json.getInt(Constants.TYPE_HUMIDITY);
                                    final TextView textView = (TextView)findViewById(R.id.text_humidity);
                                    
                                    if (val != -1) {
                                        textView.setText(val + "%");
                                    } else {
                                        textView.setText("");
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });                
            }

            @Override
            public void onMessage(byte[] data) {
            }

            @Override
            public void onDisconnect(int code, String reason) {
                Log.d(TAG, String.format("Web socket disconnected.  Code: %d Reason: %s", code, reason));
                mServerConnected = false;
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Web socket error", error);
                mServerConnected = false;

                runOnUiThread(new Runnable() {
                    public void run() {
                        final TextView statusView = (TextView)findViewById(R.id.status_main);
                        statusView.setText(getResources().getString(R.string.status_connection_failed));
                    }
                });
            }
        }, extraHeaders);  

        mClient.connect();
    }

}
