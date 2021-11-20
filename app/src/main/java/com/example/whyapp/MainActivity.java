package com.example.whyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.CALL_PHONE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lstNames;
    public TextView textview;
    public HashMap<String,String> NametoVideoID = new HashMap<String,String>();
    public HashMap<String,String> NametoVoiceID = new HashMap<String,String>();

    public String[] Names = {"宝卿 (Charlene)","宝珠 (Joanne)", "Justin", "Jenna","Julian","Joshua" };
    public String[] VoiceID = {"160","263", "286", "256","293","329" };
    public String[] VideoID = {"161","264", "287", "257","294","330" };

    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Find the list view
        this.lstNames = (ListView) findViewById(R.id.lstNames);
        Button videoButton = findViewById(R.id.buttonview);
        Button callButton = findViewById(R.id.buttonCall);
        textview = findViewById(R.id.textView);
        // Read and show the contacts

        showContacts();

        videoButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        videoCall();
                    }
                });
        callButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        voiceCall();
                    }
                });

        lstNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String s = lstNames.getItemAtPosition(i).toString();
                textview.setText(s);


            }
        });
        
    }

    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            List<String> contacts = getContactNames();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
            lstNames.setAdapter(adapter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    showContacts();
                } else {
                    Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CALL_PHONE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    voiceCall();
                } else {
                    Toast.makeText(this, "Until you grant the permission, we canot call the names", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    private List<String> getContactNames() {
        List<String> contacts = new ArrayList<>();
        boolean contactsSet = true;
        if(!contactsSet) {
            //Finding the contacts
            // Get the ContentResolver
//        ContentResolver cr = getContentResolver();
//        String[] projection = new String[]{ContactsContract.Data._ID, ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data.MIMETYPE};
//
//        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, ContactsContract.Data.DISPLAY_NAME);

            String[] projection = new String[]{ContactsContract.Data._ID, ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data.MIMETYPE};


            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(ContactsContract.Data.CONTENT_URI, projection, null, null, ContactsContract.Contacts.DISPLAY_NAME);

            Log.d("Debug", "Code is outside loop");

            while (cursor != null && cursor.moveToNext()) {
                // Iterate through the cursor
                // Get the contacts name
                int id_index = cursor.getColumnIndex(ContactsContract.Data._ID);
                int displayName_index = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
                int mimeType_index = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE);
                Log.d("Data", displayName_index + " : " + id_index + ": " + mimeType_index);

                // Get the contacts name
                @SuppressLint("Range") long _id = cursor.getLong(id_index);
                @SuppressLint("Range") String displayName = cursor.getString(displayName_index);
                @SuppressLint("Range") String mimeType = cursor.getString(mimeType_index);
                Log.d("Data", displayName + " : " + _id + " ");


                if (mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.voip.call") || mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.video.call")) {
                    Log.d("Video OR Call", "GOT Something");

                    if (mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.voip.call")) {
                        String voiceCallID = Long.toString(_id);
                        displayName = "Voice Caller: " + displayName + "- ID is: " + voiceCallID;
                        Log.d("Voice Call", "Name: " + displayName + "- ID is: " + voiceCallID);

                    } else {

                        String videoCallID = Long.toString(_id);
                        displayName = "Video Caller: " + displayName + "- ID is: " + videoCallID;

                        Log.d("Video Call", "Name: " + displayName + "- ID is: " + videoCallID);

                    }

                    contacts.add(displayName);

                }


            }
            // Close the curosor
            cursor.close();

        }else{
            //Setting the contacts beforehand
            for (int i = 0; i < Names.length; i++){
                contacts.add(Names[i]);
                Log.d("Data", "Name: " + Names[i] + " and ID: "  + VideoID[i]);
                Log.d("Data", "Name: " + Names[i] + " and ID: "  + VoiceID[i]);

                NametoVideoID.put(Names[i],VideoID[i]);
                NametoVoiceID.put(Names[i],VoiceID[i]);

            }

        }
        return contacts;
    }

    public void videoCall() {
        String id = textview.getText().toString();
        if(id.equals("Type Name")){
            return;
        }
        id = NametoVideoID.get(id);
        Log.d("Data", "video Call: tried");

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        intent.setDataAndType(Uri.parse("content://com.android.contacts/data/" + id),
                "vnd.android.cursor.item/vnd.com.whatsapp.video.call");
        intent.setPackage("com.whatsapp");

        startActivity(intent);

    }

    public void voiceCall() {
        String id = textview.getText().toString();
        if(id.equals("Type Name")){
            return;
        }
        id = NametoVoiceID.get(id);

        String buttonID = textview.getText().toString();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
        } else {
            Log.d("Data", "voiceCall: tried");
            boolean installed = appInstalled("com.whatsapp");
            if (installed) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("content://com.android.contacts/data/" + id),
                        "vnd.android.cursor.item/vnd.com.whatsapp.voip.call");
                intent.setPackage("com.whatsapp");
                startActivity(intent);
            } else {
                Log.d("Data", "NOT INSTALLED");
                Toast.makeText(this, "APP IS NOT INSTALLED", Toast.LENGTH_SHORT).show();

            }

        }

    }

    private boolean appInstalled(String url) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        Log.d("Debug", "Checking app installed");

        try {
            pm.getPackageInfo(url, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
}