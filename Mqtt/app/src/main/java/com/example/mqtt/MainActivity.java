package com.example.mqtt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mqtt.ChatAdapter;
import com.example.mqtt.ChatItem;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    static final String TAG = MainActivity.class.getSimpleName();
    static final String TOPIC = "house/myroom/lamp";
    static final String TOPIC1 = "house/myroom/alarm";

    private ChatAdapter chatAdapter;
    private MqttClient mqttClient;

    @OnClick(R.id.btn01)
    public void btn01(){
        try{
            mqttClient.publish(TOPIC,new MqttMessage("ON".getBytes()));
        }catch (Exception e){}
    }

    @OnClick(R.id.btn02)
    public void btn02(){
        try{
            mqttClient.publish(TOPIC,new MqttMessage("OFF".getBytes()));
        }catch (Exception e){}
    }

    @OnClick(R.id.btn05)
    public void btn05(){
        try{
            mqttClient.publish(TOPIC1,new MqttMessage("ON".getBytes()));
        }catch (Exception e){}
    }

    @OnClick(R.id.btn06)
    public void btn06(){
        try{
            mqttClient.publish(TOPIC1,new MqttMessage("OFF".getBytes()));
        }catch (Exception e){}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        chatAdapter = new ChatAdapter();
       // chatListView.setAdapter(chatAdapter);
        try{
            connectMqtt();
        }catch(Exception e){
            Log.d(TAG,"MqttConnect Error");
        }
    }

    private void connectMqtt() throws MqttException {
        mqttClient = new MqttClient("tcp://192.168.11.7:1883", MqttClient.generateClientId(), new MemoryPersistence());

        MqttConnectOptions mqttConnectOptions;
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setConnectionTimeout(60);
        mqttConnectOptions.setKeepAliveInterval(200);

        mqttClient.connect(mqttConnectOptions);
        mqttClient.subscribe(TOPIC);
        //mqttClient.subscribe(TOPIC1);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG,"Mqtt ReConnect");
                try{connectMqtt();}catch(Exception e){Log.d(TAG,"MqttReConnect Error");}
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                JSONObject json = new JSONObject(new String(message.getPayload(), "UTF-8"));
                chatAdapter.add(new ChatItem(json.getString("id"), json.getString("content")));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatAdapter.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}
