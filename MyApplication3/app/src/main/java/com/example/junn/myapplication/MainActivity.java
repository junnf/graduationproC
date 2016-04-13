package com.example.junn.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Toast;
//import com.example.junn.myapplication.LoginActivity;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final int  GET_CHECK = 0;
    private static final int  FAIL_CHECK = 1;

    private String parseJSON(String jsondata){
        StringBuilder fin_json = new StringBuilder();
        Gson gson = new Gson();
        CheckJson json = gson.fromJson(jsondata, CheckJson.class);
        fin_json.append(json.getCode());
        return fin_json.toString();
    }

    public Handler handler =  new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case GET_CHECK:
                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(intent);
                    break;
                case FAIL_CHECK:
                    //String a = parseJSON(msg.obj.toString());
                    Toast.makeText(MainActivity.this, "认证失败，请重新登录", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };

    private void check(final String post){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                String postdata = "token=" + post;
                try{
                    URL url = new URL("http://114.215.84.22:8000/check");
                    connection=(HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes(postdata);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    Message message = new Message();
                    String code = parseJSON(response.toString());
                    /*if code is 0 , check successful, code is 1, check fail
                    * */
                    message.obj = response;
                    String _t = new String("0");
                    if(code.equals(_t)){
                        message.what = GET_CHECK;
                        handler.sendMessage(message);
                    } else {
                        message.what = FAIL_CHECK;
                        handler.sendMessage(message);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public String get_token(){
        SharedPreferences pref = getSharedPreferences("token_record",MODE_PRIVATE);
        String token = pref.getString("token","None");
        return token;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                Button button1 = (Button)findViewById(R.id.button1);
                Button button2 = (Button)findViewById(R.id.button2);

                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    }
                });

                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                });
                //check(get_token());
                String _temp = get_token();
                if(_temp.equals("None")){
                    Toast.makeText(MainActivity.this,"请登录，若无帐号请先注册", Toast.LENGTH_SHORT).show();
                } else {
                   check(_temp);
                }
        //if (!get_token().equals("None")) {
        //    Toast.makeText(MainActivity.this,"认证失败，请重新登录", Toast.LENGTH_SHORT).show();
        //    check(get_token());
        //}
    }
}
