// IFetchWeatherService.aidl
package com.example.weatheforecastapp;

// Declare any non-default types here with import statements
import com.example.weatheforecastapp.IFetchDataListener;

interface IFetchWeatherService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     void retrieveWeatherData();
     void registerFetchDataListener(IFetchDataListener listener);
     void unregisterFetchDataListener(IFetchDataListener listener);
}
