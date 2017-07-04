package com.example.lorcan.palo;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

/**
 * Created by paul on 28.06.17.
 */

public class SendLocToDB{

    private RequestQueue requestQueue;
    private static final String URL = "http://palo.square7.ch/setLocation.php";
    private StringRequest request;
    protected LatLng location;
    protected String email;
    private Context context;

    public SendLocToDB(Context context){
        this.context=context;
    }



    public void sendLocation(final String email, final LatLng locationLatLng) {
        // using volley lib to create request
        this.location = locationLatLng;
        this.email = email;
        this.requestQueue = Volley.newRequestQueue(context);
        this.request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                /*try {
                    JSONObject jsonObject = new JSONObject(response);

                    if(jsonObject.names().get(0).equals("success")){
                        Toast.makeText(getApplicationContext(),"SUCCESS "+jsonObject.getString("success"),Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Error" +jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
                System.out.println("Antwort von PHP File: " + response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){

            // set of parameters in a hashmap, which will be send to the php file (server side)
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> hashMap = new HashMap<String, String>();

                Double lng = location.getLongitude();
                Double lat = location.getLatitude();
                hashMap.put("lng", lng.toString());
                hashMap.put("lat", lat.toString());
                hashMap.put("email",email);

                System.out.println("DAS WAS GESENDET WIRD: " + hashMap);

                return hashMap;
            }
        };

        requestQueue.add(request);


    }
}