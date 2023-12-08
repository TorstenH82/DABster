package com.thf.dabplayer.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.location.LocationListener;
import android.location.Location;
import android.location.Criteria;
import android.os.Handler;
import android.os.Looper;
import android.location.LocationManager;
import com.thf.dabplayer.R;
import java.time.ZonedDateTime;
import org.shredzone.commons.suncalc.SunTimes;

public class SunriseSunset {
  private Context context;
  private LocationManager locationManager;
  private static Location location;
  private String provider;
  private Criteria criteria;
  private Thread thread;
  private boolean autoEnabled = false;
  private static final String STATUS_SUNSET = "SUNSET";
  private static final String STATUS_SUNRISE = "SUNRISE";
  private boolean isInitialized = false;

  LocationListener locationListener =
      new LocationListener() {
        public void onLocationChanged(Location loc) {
          Logger.d("SunriseSunset: location provider onLocationChanged");
          location = loc;
          Logger.d(
              "SunriseSunset: new current location from provider "
                  + location.getProvider()
                  + ": lat: "
                  + location.getLatitude()
                  + " / lon: "
                  + location.getLongitude());
          if (autoEnabled) enableAuto();
          locationManager.removeUpdates(this);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
          // deprecated
        }

        public void onProviderDisabled(String provider) {
          Logger.d("SunriseSunset: location provider onProviderDisabled " + provider);
        }

        public void onProviderEnabled(String provider) {
          Logger.d("SunriseSunset: location provider onProviderEnabled " + provider);
        }
      };

  private SunriseSunsetCallbacks listener;

  public SunriseSunset(Context context, SunriseSunsetCallbacks listener) {
    this.context = context;
    this.listener = listener;
  }

  private void initialize() {
    int checkVal = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
    if (checkVal != PackageManager.PERMISSION_GRANTED) {
      listener.onMissingPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
      return;
    }

    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    // Define the criteria how to select the location provider
    criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_COARSE); // default

    // user defines the criteria
    criteria.setCostAllowed(false);
    // get the best provider depending on the criteria
    provider = locationManager.getBestProvider(criteria, false);

    Logger.d("SunriseSunset: location provider: " + provider);

    // the last known location of this provider
    Location loc = locationManager.getLastKnownLocation(provider);
    if (loc != null) location = loc;
    if (location != null) {
      Logger.d(
          "SunriseSunset: last known location from "
              + provider
              + ": lat: "
              + location.getLatitude()
              + " / lon: "
              + location.getLongitude());
    }

    if (location == null && !LocationManager.GPS_PROVIDER.equals(provider)) {

      checkVal = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
      if (checkVal != PackageManager.PERMISSION_GRANTED) {
        listener.onMissingPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        return;
      }

      location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

      if (location != null) {
        Logger.d(
            "SunriseSunset: last known location from "
                + LocationManager.GPS_PROVIDER
                + ": lat: "
                + location.getLatitude()
                + " / lon: "
                + location.getLongitude());
      }
    }

    if (location == null) {
      Logger.d(
          "SunriseSunset: location provider '"
              + provider
              + "' enabled: "
              + locationManager.isProviderEnabled(provider));
      locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

      if (!LocationManager.GPS_PROVIDER.equals(provider)) {
        Logger.d(
            "SunriseSunset: location provider '"
                + LocationManager.GPS_PROVIDER
                + "' enabled: "
                + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 0, 0, locationListener);
      }
    }
    this.isInitialized = true;
  }

  public Location getLocation() {
    return location;
  }

  public interface SunriseSunsetCallbacks {
    public void onSunrise();

    public void onSunset();

    public void onMissingPermission(String permission);
  }

  public void disableAuto() {
    autoEnabled = false;
    if (thread != null && thread.isAlive()) {
      thread.interrupt();
    }
  }

  public void enableAuto() {
    if (!isInitialized) initialize();
    autoEnabled = true;

    if (thread != null && thread.isAlive()) {
      thread.interrupt();
    }

    thread = new Thread(runnable);
    thread.start();
    Logger.d("SunriseSunset: (re-)started");
  }

  private Runnable runnable =
      new Runnable() {
        private String status = null;

        @Override
        public void run() {
          while (true) {
            Location loc = getLocation(); // locationManager.getLastKnownLocation(provider);

            if (loc == null) {
              Logger.d("SunriseSunset: no location - retry in 5 seconds...");
              try {
                Thread.sleep(5 * 1000);
              } catch (InterruptedException ex) {
                Logger.d("SunriseSunset: interrupted");
                return;
              }
              continue;
            }

            ZonedDateTime dateTime = ZonedDateTime.now(); // date, time and timezone of calculation
            // double lat, lng = // geolocation
            SunTimes times =
                SunTimes.compute()
                    .on(dateTime) // set a date
                    .at(loc.getLatitude(), loc.getLongitude()) // set a location
                    .execute(); // get the results
            Logger.d(
                "SunriseSunset: sunrise: " + times.getRise() + " / " + "Sunset: " + times.getSet());

            if (times.getSet().toEpochSecond() < times.getRise().toEpochSecond()) {
              Logger.d("SunriseSunset: based on sun times show: bright screen");
              if (listener != null && !STATUS_SUNRISE.equals(status)) status = STATUS_SUNRISE;
              new Handler(Looper.getMainLooper())
                  .post(
                      new Runnable() {
                        @Override
                        public void run() {
                          listener.onSunrise();
                        }
                      });
            } else {
              Logger.d("SunriseSunset: based on sun times show: dark screen");
              if (listener != null && !STATUS_SUNSET.equals(status)) status = STATUS_SUNSET;
              new Handler(Looper.getMainLooper())
                  .post(
                      new Runnable() {
                        @Override
                        public void run() {
                          listener.onSunset();
                        }
                      });
            }

            try {
              Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException ex) {
              Logger.d("SunriseSunset: interrupted");
              return;
            }
          }
        }
      };
}
