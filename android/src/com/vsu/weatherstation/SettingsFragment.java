package com.vsu.weatherstation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vsu.common.net.ssdp.SSDPClient;
import com.vsu.common.net.ssdp.SSDPConstants;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SettingsFragment extends PreferenceFragment {
    private SSDPClient mClient = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        Preference button = (Preference)findPreference("pref_scan_server");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) { 
                new SSDPSearchTask().execute();
                return true;
            }
        });          
    }

    @Override
    public void onResume() {
        super.onResume();
        mClient = new SSDPClient();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mClient != null) {
            mClient.close();
            mClient = null;
        }
    }
    
    /**
     * Background task for searching for the Hub peripheral by SSDP.
     */
    private class SSDPSearchTask extends AsyncTask<Void, String, String> {
        private final static String SERVICE_NAME = "wsd";
        private ProgressDialog dialog;
        
        protected String doInBackground(Void... params) {
            WifiManager wm = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
            WifiManager.MulticastLock multicastLock = wm.createMulticastLock("multicastLock"); 
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();
            
            int count = 0;
            String location = "";
            List<Map<String, String>> results = mClient.discover(SERVICE_NAME, 3000);
            
            do {
                count++;

                if (!results.isEmpty()) {
                    // Take first result with location header
                    Iterator<Map<String, String>> it = results.iterator();
                    while (it.hasNext()) {
                        Map<String, String> result = it.next();
                        if (result.containsKey(SSDPConstants.HEADER_LOCATION)) {
                            location = result.get(SSDPConstants.HEADER_LOCATION);
                            break;
                        }
                    }                
                }
            } while (location.equals("") && count < 4);
                        
            multicastLock.release();
            return location;
        }
        
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.status_scanning));
            dialog.show();
        }
        
        protected void onPostExecute(String result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            
            if (result.equals("")) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.status_cannot_find_server), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.status_found_server) + result, Toast.LENGTH_LONG).show();

                EditTextPreference pref = (EditTextPreference) findPreference("pref_server_address");
                pref.setText(result);
            }
        }
    }
}