package com.example.androidgooglemap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    Context context;
    private SupportMapFragment support_map_fragment;


    String link;
    LocationManager locationManager;
    Location location;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    double latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getCurrentLocation();
        init();
    }

    private void init()
    {
        context = MainActivity.this;
        support_map_fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.support_map_fragment_id);
        support_map_fragment.getMapAsync(this);
    }

    private void getCurrentLocation() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e("Exception", "Turn On GPS");
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.e("Exception", "Network Connection Error");
        }


        if (gps_enabled) {
            Log.e("GPS", "ENABLED");
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
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        else
        {
            Log.e("Suggestion", "Turn On GPS");
        }
        if(network_enabled)
        {
            Log.e("NETWORK", "ENABLED");
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, NETWORK_LISTENER);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        else
        {
            Log.e("Suggestion", "No Internet");
        }

        if( location != null )
        {
            latitude = location.getLatitude();

            longitude = location.getLongitude();

            //link = "http://api.openweathermap.org/data/2.5/weather?lat="+ String.valueOf(latitude) +"&lon="+ String.valueOf(longitude) +"&appid=523e5a69b59c771d0f4ac2eea56dc2cb";

        }
        else
        {

        }
    }


    private String getAddress(LatLng latLng)
    {
        Geocoder geocoder = new Geocoder(context);
        String address = "";

        try
        {
            /*address = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
                    .get(0).getAddressLine(1);*/

            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            for (Address local_address : addressList)
            {
                for (int i=0;i<local_address.getMaxAddressLineIndex();i++)
                {
//                    local_address.
                    address = address + local_address.getAddressLine(i) + " ";
//                    Log.i("Address",local_address.getAddressLine(i));
                }

            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return address;
    }

    private LatLng getLatLngFromLocation(String address)
    {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addressList;
        LatLng latLng;
        try
        {
            addressList = geocoder.getFromLocationName(address,5);
            if (addressList!=null)
            {
                Address location = addressList.get(0);
                latLng = new LatLng(location.getLatitude(),location.getLongitude());
                return latLng;
            }
            else
            {
                Toast.makeText(context,"Not a valid destination",Toast.LENGTH_LONG).show();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void jsonFeed(String link)
    {

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                link,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response)
                    {
//                        Log.i("Result",response.toString());
                        parseJson(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleVolleyError(error);
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void handleVolleyError(VolleyError error)
    {
        if (error instanceof AuthFailureError || error instanceof TimeoutError)
        {
            Toast.makeText(context,"AuthFailureError/TimeoutError",Toast.LENGTH_LONG).show();
        }
        else if (error instanceof NoConnectionError)
        {
            Toast.makeText(context,"NoConnectionError",Toast.LENGTH_LONG).show();
        }
        else if (error instanceof NetworkError)
        {
            Toast.makeText(context,"NetworkError",Toast.LENGTH_LONG).show();
        }
        else if (error instanceof ServerError)
        {
            Toast.makeText(context,"ServerError",Toast.LENGTH_LONG).show();
        }
        else if (error instanceof ParseError)
        {
            Toast.makeText(context,"ParseError",Toast.LENGTH_LONG).show();
        }
    }

    private void parseJson(JSONObject jsonObject)
    {
        try
        {
            JSONArray jsonArray = jsonObject.getJSONArray("routes");
            JSONObject jsonObject1 = null;
            for (int i=0;i<jsonArray.length();i++)
            {
                jsonObject1 = jsonArray.getJSONObject(i);
            }
            
            JSONArray legs_json_array = jsonObject1.getJSONArray("legs");

            JSONObject jsonObject2 = legs_json_array.getJSONObject(0);

            JSONObject distance_json_object = jsonObject2.getJSONObject("distance");
            String text = distance_json_object.getString("text");
            Log.i("Text",text);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng current_location = new LatLng(latitude, longitude);
        String current_address = getAddress(current_location);
        LatLng latLng = getLatLngFromLocation("ecil");
        googleMap.addMarker(new MarkerOptions().position(latLng).title("E.C.I.L"));
        Log.i("Ecil","Lat = "+latLng.latitude+" Lng = "+latLng.longitude);
//        link = "https://maps.googleapis.com/maps/api/directions/json?origin=madhapur&destination=ecil";
        link = "https://maps.googleapis.com/maps/api/directions/json?origin="+current_location.latitude+","+current_location.longitude+"&destination=ecil";//&key=AIzaSyA0-tj_VOTvcBk23y6cyKr-2V_pLJ1xRTo
        googleMap.addMarker(new MarkerOptions().position(current_location).title(current_address));
        googleMap.addCircle(new CircleOptions().center(current_location).radius(100*1000).fillColor(Color.parseColor("#22000000")).strokeColor(Color.TRANSPARENT));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current_location,7));
        jsonFeed(link);
    }

}
