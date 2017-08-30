package com.example.lorcan.palo;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lorcan.palo.MyApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 28.06.17.
 */

public class sendStatusToDB{

    private RequestQueue requestQueue;
    private static final String URL = "http://palo.square7.ch/setStatus.php";
    private StringRequest request;
    protected String status;
    protected String email;
    //private Context context;

    public sendStatusToDB(){

    }



    public void sendStatus(final String email, final String status) {
        // using volley lib to create request
        this.status = status;
        this.email = email;
        this.requestQueue = Volley.newRequestQueue(MyApplicationContext.getAppContext());
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

                hashMap.put("email",email);
                hashMap.put("status", status.toString());

                System.out.println("DAS WAS GESENDET WIRD: " + hashMap);

                return hashMap;
            }
        };

        requestQueue.add(request);


    }
}

