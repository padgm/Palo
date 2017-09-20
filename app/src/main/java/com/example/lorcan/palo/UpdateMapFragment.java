package com.example.lorcan.palo;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpdateMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateMapFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public UpdateMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdateMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UpdateMapFragment newInstance(String param1, String param2) {
        UpdateMapFragment fragment = new UpdateMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        new UpdateTask().execute();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_map, container, false);
    }





    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class UpdateTask extends AsyncTask<String, String, String> {


        private String responseStatus;
        private static final String strUrl = "http://palo.square7.ch/getStatus.php";
        private Boolean upStarted = false;
        MapFragment mapFragment = new MapFragment();
        Bundle bundle = new Bundle();
        ArrayList<String> args = new ArrayList<>();
        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            try {
                url = new URL(strUrl);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                BufferedReader bf = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String value = bf.readLine();
                responseStatus = value;
                System.out.println("RETURN: _--------" + responseStatus + "--------_");




                try {

                    String[] arg = new String[3];
                    JSONObject jsonObject = new JSONObject(responseStatus);

                    JSONArray jsonArray = jsonObject.getJSONArray("User");
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jObjStatus;
                        jObjStatus = new JSONObject(jsonArray.getString(i));
                        args.add(jObjStatus.getString("Status"));
                        args.add(jObjStatus.getString("Lat"));
                        args.add(jObjStatus.getString("Lng"));

                    }
                    bundle.putStringArrayList("args", args);
                    mapFragment.setArguments(bundle);

                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_from_left)
                            .replace(R.id.relativelayout_for_fragments,
                                    mapFragment,
                                    mapFragment.getTag()
                            ).commit();



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            bundle.putStringArrayList("args", args);
            mapFragment.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_from_left)
                    .replace(R.id.relativelayout_for_fragments,
                            mapFragment,
                            mapFragment.getTag()
                    ).commit();
            System.out.println("FERTIG");
            upStarted = true;
        }


        public ArrayList<JSONObject> update() {
            JSONObject jsonObject = null;
            ArrayList<JSONObject> arrayList = new ArrayList<>();
            if(!upStarted) {
                new UpdateTask().execute();
            }else {

                try {
                    jsonObject = new JSONObject(responseStatus);

                    JSONArray jsonArray = jsonObject.getJSONArray("User");
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jObjStatus;
                        jObjStatus = new JSONObject(jsonArray.getString(i));

                        arrayList.add(jObjStatus);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                upStarted = false;
            }
            System.out.println("ArrayList Return: " + arrayList);
            return arrayList;
        }

    }
}
