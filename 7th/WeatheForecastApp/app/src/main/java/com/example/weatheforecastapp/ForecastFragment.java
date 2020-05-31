package com.example.weatheforecastapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> myAdapter;
    private IFetchWeatherService mService;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(FetchWeatherService.ACTION_RETRIEVE_WEATHER_DATA)) {
                String[] data = intent.getStringArrayExtra(FetchWeatherService.EXTRA_WEATHER_DATA);
                myAdapter.clear();
                for(String dayForecastStr : data) {
                    myAdapter.add(dayForecastStr);
                }
            }
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IFetchWeatherService.Stub.asInterface(service);

            try {
                mService.registerFetchDataListener(mFetchDataListener);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private IFetchDataListener.Stub mFetchDataListener = new IFetchDataListener.Stub() {

        @Override
        public void onWeatherDataRetrieved(String[] data) throws RemoteException {
            myAdapter.clear();
            for(String dayForecastStr : data) {
                myAdapter.add(dayForecastStr);
            }
        }
    } ;

    public ForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        getActivity().bindService(new Intent(getActivity(), FetchWeatherService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        if (mService != null) {
            try {
                mService.unregisterFetchDataListener(mFetchDataListener);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        getActivity().unbindService(mServiceConnection);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_action, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refreshWeatherData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshWeatherData() {
        /*
        Intent intent = new Intent(getActivity(), FetchWeatherService.class);
        intent.setAction(FetchWeatherService.ACTION_RETRIEVE_WEATHER_DATA);
        getActivity().startService(intent);
        */

        if (mService != null) {
            try {
                mService.retrieveWeatherData();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forecast,null);

        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };

        final List<String> LIST_SAMPLE = new ArrayList<String>(Arrays.asList(data));

        myAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_view_item,
                R.id.list_item_forecast_textview,
                LIST_SAMPLE
        );

        ListView listView = (ListView) view.findViewById(R.id.listview);
        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                String forecast = (String) myAdapter.getItem(position);
                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("data", forecast);
                startActivity(intent);
            }
        });

//        IntentFilter intentFilter = new IntentFilter(FetchWeatherService.ACTION_RETRIEVE_WEATHER_DATA);
//        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);

        return view;
        //return inflater.inflate(R.layout.fragment_forecast, container, false);
    }

    @Override
    public void onDestroyView() {
//        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroyView();
    }

}
