package com.example.lorcan.palo.Fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.lorcan.palo.CurrLocUpdate;
import com.example.lorcan.palo.GetFromDatabase.GetEncodedImageFromDB;
import com.example.lorcan.palo.GetFromDatabase.GetStatusFromDB;
import com.example.lorcan.palo.MyApplicationContext;
import com.example.lorcan.palo.OldStatus;
import com.example.lorcan.palo.R;
import com.example.lorcan.palo.SendEncodedImageToDB;
import com.example.lorcan.palo.sendStatusToDB;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    public ProfileFragment(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
        startMapAndUploadStatus();
    }


    public final int PERMISSION_ACCESS_FINE_LOCATION = 2;
    public final int PERMISSION_ACCESS_COARSE_LOCATION = 3;
    private Bitmap bitmap;

    private InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            String blockCharacterSet = "\\";
            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    /*
     * Declare elements here to handle them in the onCreateView method.
     */

    EditText etStatus;
    Button btnChange;
    Spinner spinner;

    FloatingActionButton fab_marker1, fab_marker2, fab_marker3, fab_marker4, fab_marker5, fab_marker6, fab_marker7, fab_marker8, fab_marker9, fab_marker10;
    BitmapDrawable bitmapDrawableSelectedMarkerColor;
    Bitmap bitmapSelectedMarkerColor;

    private final String TAG = getClass().getName();

    File file;
    Uri uri;
    Intent CameraIntent, GalleryIntent, CropIntent;
    final int PERMISSION_CAMERA_CODE = 1;
    Bitmap croppedBitmap;
    Bitmap rotatedBitmap;
    Bitmap decodedByte;

    ImageView ivImage;

    FloatingActionButton fabImageDialog;

    final int CAMERA_REQUEST = 1;
    final int GALLERY_REQUEST = 2;
    String selectedPhoto;

    ArrayList<String> spinnerArray = new ArrayList<>();

    String status = "";

    private String android_id;
    public String time;
    public Double lat;
    public Double lng;

    @SuppressLint("HardwareIds")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create and return a new View element here.
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivImage = (ImageView) view.findViewById(R.id.ivImage);
        fabImageDialog = (FloatingActionButton) view.findViewById(R.id.fabImageDialog);

        // Use the created view to get the elements from the xml file.
        etStatus = (EditText) view.findViewById(R.id.etStatus);
        etStatus.setFilters(new InputFilter[]{filter});
        btnChange = (Button) view.findViewById(R.id.btnChangeInMap);

        fab_marker1 = (FloatingActionButton) view.findViewById(R.id.fab_marker1);
        fab_marker2 = (FloatingActionButton) view.findViewById(R.id.fab_marker2);
        fab_marker3 = (FloatingActionButton) view.findViewById(R.id.fab_marker3);
        fab_marker4 = (FloatingActionButton) view.findViewById(R.id.fab_marker4);
        fab_marker5 = (FloatingActionButton) view.findViewById(R.id.fab_marker5);
        fab_marker6 = (FloatingActionButton) view.findViewById(R.id.fab_marker6);
        fab_marker7 = (FloatingActionButton) view.findViewById(R.id.fab_marker7);
        fab_marker8 = (FloatingActionButton) view.findViewById(R.id.fab_marker8);
        fab_marker9 = (FloatingActionButton) view.findViewById(R.id.fab_marker9);
        fab_marker10 = (FloatingActionButton) view.findViewById(R.id.fab_marker10);

        int permissionCheck = ContextCompat.checkSelfPermission(MyApplicationContext.getAppContext(), android.Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            RequestRuntimePermission();
        }

        TelephonyManager tManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(MyApplicationContext.getAppContext(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return null;
        }

        if (tManager != null) {
            android_id = tManager.getDeviceId();
        }

        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        time = dateFormat.format(date);

        // Receive status from database.
        GetStatusFromDB getStatusFromDB = new GetStatusFromDB();
        getStatusFromDB.getStatus(android_id, this, etStatus);

        GetEncodedImageFromDB getEncodedImageFromDB = new GetEncodedImageFromDB();
        getEncodedImageFromDB.getResponseEncodedImage(android_id, this);

        fabImageDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileFragment.this.getActivity());
                builder.setTitle(R.string.alert_upload_image_title);
                builder.setMessage(R.string.alert_upload_image_message);

                // Select Camera
                builder.setPositiveButton(R.string.camera, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CameraOpen();
                    }
                });

                // Select Gallery
                builder.setNegativeButton(R.string.gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GalleryOpen();
                    }
                });

                builder.show();
            }
        });

        /*
         * Create an onClickListener for the button.
         *
         * To make i.e. a correct Toast it's important to replace "this"
         * with "ProfileFragment.this.getActivity()"!
         */

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnChangeClicked();
            }
        });

        spinnerArray.add("--- wähle Status --");

        try {
            String oldUserStatus = OldStatus.getData(MyApplicationContext.getAppContext());
            System.out.println("JSON STATUS PROFILE FRAGMENT: " + oldUserStatus);
            JSONObject jsonObject = new JSONObject(oldUserStatus);
            JSONArray jsonArray = jsonObject.getJSONArray("Status");
            for (int i = 1; i < jsonArray.length(); i++) {
                spinnerArray.add(jsonArray.get(i).toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e1) {
            e1.printStackTrace();

        }

        spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // if any item is selected this one should become the active status
                String selectedItemText = (String) adapterView.getItemAtPosition(i);
                if (!selectedItemText.equals("--- wähle Status --")) {
                    Toast.makeText(ProfileFragment.this.getActivity(), selectedItemText, Toast.LENGTH_SHORT).show();
                    etStatus.setText(selectedItemText);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // if no item is selected the last used status should stay the active status
            }
        });

        /*
         * TODO:
         * bundle bitmapSelectedMarkerColor and set as marker in MapFragment
         * Marker color has to be send to database when a status is posted,
         * so Marker color from user users can be displayed correctly.
         */

        fab_marker1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmapDrawableSelectedMarkerColor = (BitmapDrawable) getResources().getDrawable(R.drawable.marker1);
                bitmapSelectedMarkerColor = createScaledBitmap(bitmapDrawableSelectedMarkerColor.getBitmap(), 170, 125, false);
                Toast.makeText(ProfileFragment.this.getActivity(), R.string.marker1, Toast.LENGTH_SHORT).show();
            }
        });

        fab_marker2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmapDrawableSelectedMarkerColor = (BitmapDrawable) getResources().getDrawable(R.drawable.marker2);
                bitmapSelectedMarkerColor = createScaledBitmap(bitmapDrawableSelectedMarkerColor.getBitmap(), 170, 125, false);
                Toast.makeText(ProfileFragment.this.getActivity(), R.string.marker2, Toast.LENGTH_SHORT).show();
            }
        });

        fab_marker3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmapDrawableSelectedMarkerColor = (BitmapDrawable) getResources().getDrawable(R.drawable.marker3);
                bitmapSelectedMarkerColor = createScaledBitmap(bitmapDrawableSelectedMarkerColor.getBitmap(), 170, 125, false);
                Toast.makeText(ProfileFragment.this.getActivity(), R.string.marker3, Toast.LENGTH_SHORT).show();
            }
        });

        fab_marker4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmapDrawableSelectedMarkerColor = (BitmapDrawable) getResources().getDrawable(R.drawable.marker4);
                bitmapSelectedMarkerColor = createScaledBitmap(bitmapDrawableSelectedMarkerColor.getBitmap(), 170, 125, false);
                Toast.makeText(ProfileFragment.this.getActivity(), R.string.marker4, Toast.LENGTH_SHORT).show();
            }
        });

        fab_marker5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmapDrawableSelectedMarkerColor = (BitmapDrawable) getResources().getDrawable(R.drawable.marker5);
                bitmapSelectedMarkerColor = createScaledBitmap(bitmapDrawableSelectedMarkerColor.getBitmap(), 170, 125, false);
                Toast.makeText(ProfileFragment.this.getActivity(), R.string.marker5, Toast.LENGTH_SHORT).show();
            }
        });

        fab_marker6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmapDrawableSelectedMarkerColor = (BitmapDrawable) getResources().getDrawable(R.drawable.marker6);
                bitmapSelectedMarkerColor = createScaledBitmap(bitmapDrawableSelectedMarkerColor.getBitmap(), 170, 125, false);
                Toast.makeText(ProfileFragment.this.getActivity(), R.string.marker6, Toast.LENGTH_SHORT).show();
            }
        });

        fab_marker7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmapDrawableSelectedMarkerColor = (BitmapDrawable) getResources().getDrawable(R.drawable.marker7);
                bitmapSelectedMarkerColor = createScaledBitmap(bitmapDrawableSelectedMarkerColor.getBitmap(), 170, 125, false);
                Toast.makeText(ProfileFragment.this.getActivity(), R.string.marker7, Toast.LENGTH_SHORT).show();
            }
        });

        fab_marker8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmapDrawableSelectedMarkerColor = (BitmapDrawable) getResources().getDrawable(R.drawable.marker8);
                bitmapSelectedMarkerColor = createScaledBitmap(bitmapDrawableSelectedMarkerColor.getBitmap(), 170, 125, false);
                Toast.makeText(ProfileFragment.this.getActivity(), R.string.marker8, Toast.LENGTH_SHORT).show();
            }
        });

        fab_marker9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmapDrawableSelectedMarkerColor = (BitmapDrawable) getResources().getDrawable(R.drawable.marker9);
                bitmapSelectedMarkerColor = createScaledBitmap(bitmapDrawableSelectedMarkerColor.getBitmap(), 170, 125, false);
                Toast.makeText(ProfileFragment.this.getActivity(), R.string.marker9, Toast.LENGTH_SHORT).show();
            }
        });

        fab_marker10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmapDrawableSelectedMarkerColor = (BitmapDrawable) getResources().getDrawable(R.drawable.marker10);
                bitmapSelectedMarkerColor = createScaledBitmap(bitmapDrawableSelectedMarkerColor.getBitmap(), 170, 125, false);
                Toast.makeText(ProfileFragment.this.getActivity(), R.string.marker10, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0 && resultCode == RESULT_OK) {
            CropImage();
        } else if (requestCode == 2) {
            if (data != null) {
                uri = data.getData();
                selectedPhoto = uri.getPath();
                CropImage();
            }
        } else if (requestCode == 1) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    croppedBitmap = bundle.getParcelable("data");
                }
                rotatedBitmap = getRotatedBitmap(croppedBitmap);
                croppedBitmap.recycle();
                ivImage.setImageBitmap(Bitmap.createScaledBitmap(rotatedBitmap, 200, 200, false));

                // Save cropped image to external storage and get Path afterwards to upload to DB
                Uri croppedUri = saveOutput(rotatedBitmap);
                selectedPhoto = croppedUri.getPath();

                if (selectedPhoto != null) {
                    uploadImage(selectedPhoto);
                }
            }
        }
    }

    public void setEncodedImageAsProfileImage(String image) {

        try {
            if (image.length() > 0) {
                byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                if (decodedByte != null) {
                    decodedByte.recycle();
                    decodedByte = null;
                }
                decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivImage.setRotation(90);
                ivImage.setImageBitmap(Bitmap.createScaledBitmap(decodedByte, 200, 200, false));
                decodedByte.recycle();
                decodedByte = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnChangeClicked() {

        System.out.println("btnChange Clicked");

        if (etStatus.getText().toString().isEmpty()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileFragment.this.getActivity());
            builder.setTitle(R.string.alert_empty_status_title);
            builder.setMessage(R.string.alert_empty_status_message);
            builder.show();
        } else {


            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ProfileFragment.this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                ActivityCompat.requestPermissions(ProfileFragment.this.getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);


                status = etStatus.getText().toString();
                return;
            }
            status = etStatus.getText().toString();
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            // Got last known location. In some rare situations this can be null.
                            sendStatusToDB statusToDB = new sendStatusToDB();
                            statusToDB.sendStatus(etStatus.getText().toString(), lat, lng, time, android_id);
                            CurrLocUpdate upFragment = new CurrLocUpdate();
                            FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction()
                                    .setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_from_left)
                                    .replace(R.id.relativelayout_for_fragments,
                                            upFragment,
                                            upFragment.getTag()
                                    ).commit();
                        }
                    });
        }
    }

    @SuppressLint("HardwareIds")
    public void startMapAndUploadStatus() {

        TelephonyManager tManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this.getActivity(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (tManager != null) {
            android_id = tManager.getDeviceId();
        }

        //bundle the data from status and study course to "send" them to MapFragment.java
        OldStatus oldList = new OldStatus();
        oldList.addNewEntry(status);
        //send status to database
        sendStatusToDB statusToDB = new sendStatusToDB();

        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        time = dateFormat.format(date);
        statusToDB.sendStatus(status, lat, lng, time, android_id);

        CurrLocUpdate mapFragment = new CurrLocUpdate();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_from_left)
                .replace(R.id.relativelayout_for_fragments,
                        mapFragment,
                        mapFragment.getTag()
                ).commit();
    }

    private Bitmap getRotatedBitmap(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) -90);
        return createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void uploadImage(String selectedPhoto) {

        if (selectedPhoto == null || selectedPhoto.equals("")) {
            Toast.makeText(ProfileFragment.this.getActivity(), "No image selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
            bitmap = ImageLoader.init().from(selectedPhoto).requestSize(128, 128).getBitmap();
            String encodedImage = ImageBase64.encode(bitmap);
            bitmap.recycle();
            bitmap = null;
            Log.d(TAG, encodedImage);

            SendEncodedImageToDB sendEncodedImageToDB = new SendEncodedImageToDB();
            sendEncodedImageToDB.sendEncodedImage(encodedImage);


            Toast.makeText(ProfileFragment.this.getActivity(), "Image has been uploaded.", Toast.LENGTH_SHORT).show();


        } catch (FileNotFoundException e) {
            Toast.makeText(ProfileFragment.this.getActivity(), "Something wrong while encoding photos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void RequestRuntimePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileFragment.this.getActivity(), android.Manifest.permission.CAMERA)) {
            Toast.makeText(ProfileFragment.this.getActivity(), "CAMERA permission allows us to access CAMERA app.", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(ProfileFragment.this.getActivity(), new String[]{android.Manifest.permission.CAMERA}, PERMISSION_CAMERA_CODE);
        }
    }

    private void CameraOpen() {

        CameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(),
                "file" + String.valueOf(System.currentTimeMillis()) + ".jpeg");
        uri = Uri.fromFile(file);
        CameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        CameraIntent.putExtra("return-data", true);
        startActivityForResult(CameraIntent, 0);
    }

    private void GalleryOpen() {

        GalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(GalleryIntent, "Select Image from Gallery"), 2);
    }

    private void CropImage() {

        try {
            CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.setDataAndType(uri, "image/*");

            CropIntent.putExtra("crop", "true");
            CropIntent.putExtra("outputX", 180);
            CropIntent.putExtra("outputY", 180);
            CropIntent.putExtra("aspectX", 1);
            CropIntent.putExtra("aspectY", 1);
            CropIntent.putExtra("scaleUpIfNeeded", true);
            CropIntent.putExtra("return-data", true);

            startActivityForResult(CropIntent, 1);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CAMERA_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ProfileFragment.this.getActivity(), "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileFragment.this.getActivity(), "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
            }
            case PERMISSION_ACCESS_COARSE_LOCATION: {

                FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

                if (ActivityCompat.checkSelfPermission(ProfileFragment.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ProfileFragment.this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                lat = location.getLatitude();
                                lng = location.getLongitude();

                            }
                        });
            }

            case PERMISSION_ACCESS_FINE_LOCATION:
            {

                FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

                if (ActivityCompat.checkSelfPermission(ProfileFragment.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ProfileFragment.this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                lat = location.getLatitude();
                                lng = location.getLongitude();

                            }
                        });

            }
        }
    }

    private Uri saveOutput(Bitmap croppedImage) {
        Uri saveUri = null;
        File file = new File(Environment.getExternalStorageDirectory(),"tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        OutputStream outputStream;

        try {
            file.getParentFile().mkdirs();
            saveUri = Uri.fromFile(file);
            outputStream = MyApplicationContext.getAppContext().getContentResolver().openOutputStream(saveUri);
            if (outputStream != null) {
                croppedImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  saveUri;
    }
}