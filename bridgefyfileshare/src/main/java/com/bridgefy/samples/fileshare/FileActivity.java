package com.bridgefy.samples.fileshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bridgefy.samples.fileshare.R;
import com.bridgefy.samples.fileshare.entities.Message;
import com.bridgefy.samples.fileshare.entities.Peer;
import com.bridgefy.sdk.client.BFEngineProfile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.Device;
import com.bridgefy.sdk.framework.controller.DeviceManager;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FileActivity  extends AppCompatActivity {

    private static final String TAG = "file_activity";
    private String conversationName;
    private String conversationId;




    MessagesRecyclerViewAdapter messagesAdapter =
            new MessagesRecyclerViewAdapter(new ArrayList<>());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        ButterKnife.bind(this);

        // recover our Peer object
        conversationName = getIntent().getStringExtra(MainActivity.INTENT_EXTRA_NAME);
        conversationId   = getIntent().getStringExtra(MainActivity.INTENT_EXTRA_UUID);

        // Configure the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Enable the Up button
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(conversationName);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // register the receiver to listen for incoming messages
        LocalBroadcastManager.getInstance(getBaseContext())
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Message message = new Message(intent.getStringExtra(MainActivity.INTENT_EXTRA_MSG));
                        message.setDeviceName(intent.getStringExtra(MainActivity.INTENT_EXTRA_NAME));
                        message.setDirection(Message.INCOMING_MESSAGE);
                        messagesAdapter.addMessage(message);
                    }
                }, new IntentFilter(conversationId));

        // configure the recyclerview
        RecyclerView messagesRecyclerView = findViewById(R.id.message_list);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        messagesRecyclerView.setLayoutManager(mLinearLayoutManager);
        messagesRecyclerView.setAdapter(messagesAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @OnClick({R.id.fab})
    public void onMessageSend(View v) {


        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(1987)
                .withHiddenFiles(true) // Show hidden files and folders
                .start();



        // get the message and push it to the views
//        String messageString = txtMessage.getText().toString();
//        if (messageString.trim().length() > 0) {
//            // update the views
//            txtMessage.setText("");
//            Message message = new Message(messageString);
//            message.setDirection(Message.OUTGOING_MESSAGE);
//            messagesAdapter.addMessage(message);
//
//            // create a HashMap object to send
//            HashMap<String, Object> content = new HashMap<>();
//            content.put("text", messageString);
//
//            // send message text to device
//            if (conversationId.equals(MainActivity.BROADCAST_CHAT)) {
//                // we put extra information in broadcast packets since they won't be bound to a session
//                content.put("device_name", Build.MANUFACTURER + " " + Build.MODEL);
//                content.put("device_type", Peer.DeviceType.ANDROID.ordinal());
//                Bridgefy.sendBroadcastMessage(
//                        Bridgefy.createMessage(content),
//                        BFEngineProfile.BFConfigProfileLongReach);
//            } else {
//                Bridgefy.sendMessage(
//                        Bridgefy.createMessage(conversationId, content),
//                        BFEngineProfile.BFConfigProfileLongReach);
//            }
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode==1987 && data!=null)
        {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            Log.i(TAG, "onActivityResult: file path "+filePath);
            File file=new File(filePath);
            byte fileContent[] = new byte[(int)file.length()];

            try {
                FileInputStream fin = new FileInputStream(file);
                fin.read(fileContent);
                HashMap<String, Object> content = new HashMap<>();
                content.put("device_name", Build.MANUFACTURER + " " + Build.MODEL);
                Bridgefy.sendMessage(Bridgefy.createMessage(conversationId, content));

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }



    /**
     *      RECYCLER VIEW CLASSES
     */
    class MessagesRecyclerViewAdapter
            extends RecyclerView.Adapter<MessagesRecyclerViewAdapter.MessageViewHolder> {

        private final List<Message> messages;

        MessagesRecyclerViewAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        void addMessage(Message message) {
            messages.add(0, message);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).getDirection();
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View messageView = null;

            switch (viewType) {
                case Message.INCOMING_MESSAGE:
                    messageView = LayoutInflater.from(viewGroup.getContext()).
                            inflate((R.layout.message_row_incoming), viewGroup, false);
                    break;
                case Message.OUTGOING_MESSAGE:
                    messageView = LayoutInflater.from(viewGroup.getContext()).
                            inflate((R.layout.message_row_outgoing), viewGroup, false);
                    break;
            }

            return new MessageViewHolder(messageView);
        }

        @Override
        public void onBindViewHolder(final MessageViewHolder messageHolder, int position) {
            messageHolder.setMessage(messages.get(position));
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            final TextView txtMessage;
            Message message;

            MessageViewHolder(View view) {
                super(view);
                txtMessage = view.findViewById(R.id.txtMessage);
            }

            void setMessage(Message message) {
                this.message = message;
                    this.txtMessage.setText(message.getText());

            }
        }
    }
}
