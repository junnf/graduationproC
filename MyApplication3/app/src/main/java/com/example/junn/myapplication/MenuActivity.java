package com.example.junn.myapplication;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MenuActivity extends Activity implements View.OnClickListener {

    private final static int INFO_GET = 0;
    private final static int INFO_NOGET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Button getmessage_button = (Button)findViewById(R.id.person_manage);

        getmessage_button.setOnClickListener(this);
    }

    public String get_token(){
        SharedPreferences pref = getSharedPreferences("token_record",MODE_PRIVATE);
        String token = pref.getString("token","None");
        return token;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.person_manage){
            String _token = get_token();
            getinfo(_token);
        }
    }

    private String parseJSON(String jsondata){
        StringBuilder fin_json = new StringBuilder();

        Gson gson = new Gson();
        LoginJson json = gson.fromJson(jsondata, LoginJson.class);
        if (Integer.valueOf(json.getCode()).intValue() >= 1) {
            return "1";
        }
        fin_json.append(json.getInformation());
        return fin_json.toString();
    }

    public Handler handler =  new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case INFO_GET:
                    //Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    //startActivity(intent);
                    Toast.makeText(MenuActivity.this, "aaa"+msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case INFO_NOGET:
                    //String a = parseJSON(msg.obj.toString());
                    Toast.makeText(MenuActivity.this, "未得到信息", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };

    private void getinfo(final String token){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    URL url = new URL("http://114.215.84.22:8000/student/info/"+token);
                    connection=(HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
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
                    String _t = new String("1");
                    if(code.equals(_t)){
                        message.what = INFO_NOGET;
                        handler.sendMessage(message);
                    } else {
                        message.what = INFO_GET;
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

}
