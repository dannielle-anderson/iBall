package com.theiballgroup.iballapp;

import java.util.LinkedList;
import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.TextView;


public class GameActivity extends Activity implements LocationListener {
    private static final String[] S = { "Out of Service",
            "Temporarily Unavailable", "Available" };

    private TextView output;
    private LocationManager locationManager;
    private String bestProvider;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private float[] distanceArray = {0,0,0};
    private LinkedList<Location> previousLocations = new LinkedList<>();
    private int LOCATIONLISTSIZE = 5;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Get the output UI
        output = (TextView) findViewById(R.id.output);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        bestProvider = locationManager.getBestProvider(criteria, false);
        output.append("\nBest Provider:\n");
        LocationProvider info = locationManager.getProvider(bestProvider);
        output.append(info.toString() + "\n");

    }


    /** Register for the updates when Activity is in foreground */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(bestProvider, 2000, 0, this);
    }

    /** Stop the updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    public void onProviderDisabled(String provider) {

    }

    public void onProviderEnabled(String provider) {

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        output.append("\n\nProvider Status Changed: " + provider + ", Status="
                + S[status] + ", Extras=" + extras);
    }

    private void handleNewLocation(Location location) {
        if (location == null) {
            output.append("\nLocation[unknown]\n\n");
        } else {
            if (location != null) {
                previousLocations.addFirst(location);
                if (previousLocations.size() > LOCATIONLISTSIZE) {
                    previousLocations.removeLast();
                    Location bestGuess = filterLocations();
                    /**
                     if (endLatitude != 0) {
                     startLatitude = endLatitude;
                     startLongitude = endLongitude;
                     }
                     endLatitude = previousLocations.getFirst().getLatitude();
                     endLongitude = previousLocations.getFirst().getLongitude();
                     if (startLatitude != 0) {
                     Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, distanceArray);
                     // distance = distanceArray[0];
                     }
                     */
                }

            }

            output.append("\n\nLat / Long: " + String.valueOf(location.getLatitude()) + ", " +
                    String.valueOf(location.getLongitude()) + "\nTime: " +
                    String.valueOf(location.getTime()) + "\nAccuracy: " +
                    String.valueOf(location.getAccuracy()));
        }
    }

    private Location filterLocations() {
        Location weightedLocation = new Location(previousLocations.getFirst());
        double[] weights = new double[LOCATIONLISTSIZE];
        double[] normal = new double[LOCATIONLISTSIZE];
        double doubleSum = 0;
        double weightedLatitude = 0;
        double weightedLongitude = 0;
        long currentTime = previousLocations.getFirst().getTime();

        for (int i = 0; i < LOCATIONLISTSIZE; i++) {
            double timeWeight = 1000 / (currentTime - previousLocations.get(i).getTime() + 1000);
            double accuracyWeight = 1 / (previousLocations.get(i).getAccuracy());
            double pointWeight = timeWeight * accuracyWeight;
            weights[i] = pointWeight;
        }

        for (double d:weights) {
            doubleSum += d;
        }

        for(int k = 0; k < LOCATIONLISTSIZE; k++)
        {
            normal[k] = weights[k]/doubleSum;
        }

        for (int l = 0; l < LOCATIONLISTSIZE; l++) {
            weightedLatitude += previousLocations.get(l).getLatitude() * normal[l];
            weightedLongitude += previousLocations.get(l).getLongitude() * normal[l];
        }

        weightedLocation.setLatitude(weightedLatitude);
        weightedLocation.setLongitude(weightedLongitude);

        return weightedLocation;
    }

}

