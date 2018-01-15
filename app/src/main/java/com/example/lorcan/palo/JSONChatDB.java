package com.example.lorcan.palo;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class JSONChatDB {

    static String fileName = "chats.json";

    public static void createNewDBDeleteOld(String nameJSON) {
        try {
            System.out.println("New created DB: " + nameJSON);
            FileWriter file = new FileWriter(MyApplicationContext.getAppContext().getFilesDir().getPath() + "/" + fileName);
            
            file.write(nameJSON);
            file.flush();
            file.close();
        } catch (IOException e) {
            Log.e("TAG", "Error in Writing: " + e.getLocalizedMessage());
        }
    }

    public static String getData(Context context) {

        try {
            File f = new File(context.getFilesDir().getPath() + "/" + fileName);
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer);
        } catch (IOException e) {
            Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());
            return null;
        }
    }

    public void addNewChatUser(String newUser){
        String old = getData(MyApplicationContext.getAppContext());
        newUser = newUser.substring(0, newUser.length()-1);
        int intBool = 0;
        System.out.println(old);
        try {
            if(old == null){
                createNewDBDeleteOld("{ \"Users\" : [\"\"]}");
                old = getData(MyApplicationContext.getAppContext());
            }
            JSONObject jsonObject = new JSONObject(old);
            old = old.substring(0, old.length()-2);
            JSONArray jsonArray = jsonObject.getJSONArray("Users");
            for(int i=1; i < jsonArray.length(); i++){
                System.out.println("NEWUSER: " + newUser);
                System.out.println("JSONList: " + jsonArray.get(i).toString());
                if(jsonArray.get(i).toString().equals(newUser)){
                    intBool++;
                }
            }

            if(intBool < 1){
                old = old + ", \"" + newUser + "\"]}";
                System.out.println(old);
                createNewDBDeleteOld(old);
                intBool = 0;
            }else{
                old = old + "]}";
                System.out.println(old);
                createNewDBDeleteOld(old);
            }

        }catch(JSONException e){
            e.printStackTrace();
        }

    }

    public void deleteUser(String user){
        String list = JSONChatDB.getData(MyApplicationContext.getAppContext());
        JSONObject jsonObject = null;
        String data = "";
        try {
            jsonObject = new JSONObject(list);
            JSONArray listeNicknames = jsonObject.getJSONArray("Users");
            for(int i =1; i<listeNicknames.length(); i++){
                System.out.println("User der gelöscht werden sollte: " + user);
                if(!listeNicknames.get(i).toString().equals(user)){

                    System.out.println("User die durchgelassen werden: " + listeNicknames.get(i).toString());
                    data = data + ", \"" + listeNicknames.get(i).toString() + "\" ";
                }
            }
            if(data.equals("")){
                data = "{ \"Users\" : [\"\"]}";
            }else {
                data = "{ \"Users\" : [\"\"" + data + "]}";
            }
            createNewDBDeleteOld(data);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}