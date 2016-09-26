package com.moin.chatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.moin.chatapp.Notification.QuickstartPreferences;
import com.moin.chatapp.Notification.RegistrationIntentService;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.recycler)RecyclerView myRecyclerView;
    @Bind(R.id.messageEditText)EditText messageEditText;
    private String userName = "User2";
    private MessageAdapter myAdapter;
    private ArrayList<MessageStr> data = new ArrayList<>();


    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private Socket socket;
    {
        try{
            socket = IO.socket("http://192.168.1.108:3000");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        registerPushNotification();

        myRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        myRecyclerView.setLayoutManager(layoutManager);
        myAdapter = new MessageAdapter(MainActivity.this);
        myRecyclerView.setAdapter(myAdapter);

        socketConnectionStuff();

    }

    private void registerPushNotification(){


            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context);
                    boolean sentToken = sharedPreferences
                            .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                    if (sentToken) {
//                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                    } else {
//                    mInformationTextView.setText(getString(R.string.token_error_message));
                    }
                }
            };

            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("error", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private Emitter.Listener handleIncomingMessages = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    String imageText;
                    try {
                        message = data.getString("text").toString();
                        Log.d("m",message+"");
                        MessageStr str = new MessageStr();
                        str.isSender = false;
                        str.messageToSend = message;
                        addMessage(str);
                    } catch (JSONException e) {
                        // return;
                    }
//                    try {
//                        imageText = data.getString("image");
//                        addImage(decodeImage(imageText));
//                    } catch (JSONException e) {
//                        //retur
//                    }

//                }
//            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        JSONObject sendText = new JSONObject();
        try{
            sendText.put("UserName",userName);
            socket.emit("register", sendText);
        }catch(JSONException e){

        }
    }

    private void socketConnectionStuff(){
        socket.connect();


        socket.on("message", handleIncomingMessages);
    }

    @OnClick(R.id.sendMessageButton)void sendMessage(View view){
        String message = messageEditText.getText().toString().trim();
        messageEditText.setText("");
        MessageStr curretnStr = new MessageStr();
        curretnStr.messageToSend = message;

        addMessage(curretnStr);
        JSONObject sendText = new JSONObject();
        try{
            sendText.put("UserName",userName);
            sendText.put("text",message);
            socket.emit("message", sendText);
        }catch(JSONException e){

        }
    }

    private void addMessage(MessageStr str){
        data.add(str);
        runOnUiThread (new Thread(new Runnable() {
            public void run() {
                myAdapter.notifyDataSetChanged();
                scrollToBottom();
            }
        }));

    }

    private void scrollToBottom(){
        myRecyclerView.scrollToPosition(myAdapter.getItemCount() - 1);
    }



    public class MessageAdapter extends RecyclerView.Adapter {

        private Context myContext;
        private LayoutInflater inflator;

        public MessageAdapter(Context context) {
            inflator = LayoutInflater.from(context);
            myContext = context;
        }

        @Override
        public int getItemViewType(int position) {
            if (data.get(position).isSender){
                return 1;
            }
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            switch (viewType) {
                case 0:
                    View view = inflator.inflate(R.layout.reciever_cell, parent, false);
                    RecieverCell holder = new RecieverCell(view);
                    return holder;
                case 1:
                    View view1 = inflator.inflate(R.layout.sender_cell, parent, false);
                    SenderCell holder1 = new SenderCell(view1);
                    return holder1;
            }
            return null;
        }

        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            switch (holder.getItemViewType()) {

                case 0:
                    RecieverCell holder1 = (RecieverCell) holder;
                    MessageStr myData;
                    myData = data.get(position);

                    holder1.messageTextField.setText(myData.messageToSend);

                    break;

                case 1:
                    SenderCell holder2 = (SenderCell) holder;
                    MessageStr myData1;
                        myData1 = data.get(position);

                    holder2.messageTextField.setText(myData1.messageToSend);
                    break;
            }
        }


        @Override
        public int getItemCount() {
            return data.size();
        }

        public class SenderCell extends RecyclerView.ViewHolder {
            TextView messageTextField;



            public SenderCell(final View itemView) {
                super(itemView);
                messageTextField = (TextView) itemView.findViewById(R.id.messageTextField);
            }
        }

        public class RecieverCell extends RecyclerView.ViewHolder {
            TextView messageTextField;

            public RecieverCell(final View itemView) {
                super(itemView);
                messageTextField = (TextView) itemView.findViewById(R.id.messageTextField);
            }
        }
    }


    @Override
    protected void onDestroy() {
        socket.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        JSONObject sendText = new JSONObject();
        try{
            sendText.put("UserName",userName);
            socket.emit("leave", sendText);
        }catch(JSONException e){

        }
        super.onStop();
    }
}
