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
import android.widget.TextView;

import com.bridgefy.samples.fileshare.entities.BridgefyFile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.Message;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        Log.i("C", "onCreate: conversation id is "+conversationId);

        // register the receiver to listen for incoming bridgefyFiles
        LocalBroadcastManager.getInstance(getBaseContext())
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.i(TAG, "onReceive: received message to adapter");
                        Message message = intent.getParcelableExtra(MainActivity.INTENT_EXTRA_MSG);
                        byte[] fileBytes = message.getData();
                        BridgefyFile bridgefyFile = new BridgefyFile((String)message.getContent().get("file"));
                        bridgefyFile.setData(fileBytes);
                        bridgefyFile.setDeviceName(intent.getStringExtra(MainActivity.INTENT_EXTRA_NAME));
                        bridgefyFile.setDirection(BridgefyFile.INCOMING_FILE);
                        messagesAdapter.addMessage(bridgefyFile);
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
                content.put("file",file.getName());

                com.bridgefy.sdk.client.Message.Builder builder=new com.bridgefy.sdk.client.Message.Builder();
                com.bridgefy.sdk.client.Message message = builder.setReceiverId(conversationId).setContent(content).setData(fileContent).build();
                Bridgefy.sendMessage(message);
                BridgefyFile bridgefyFile = new BridgefyFile(filePath);
                bridgefyFile.setDirection(BridgefyFile.OUTGOING_FILE);
                bridgefyFile.setData(fileContent);
                messagesAdapter.addMessage(bridgefyFile);




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

        private final List<BridgefyFile> bridgefyFiles;

        MessagesRecyclerViewAdapter(List<BridgefyFile> bridgefyFiles) {
            this.bridgefyFiles = bridgefyFiles;
        }

        @Override
        public int getItemCount() {
            return bridgefyFiles.size();
        }

        void addMessage(BridgefyFile bridgefyFile) {
            bridgefyFiles.add(0, bridgefyFile);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return bridgefyFiles.get(position).getDirection();
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View messageView = null;

            switch (viewType) {
                case BridgefyFile.INCOMING_FILE:
                    messageView = LayoutInflater.from(viewGroup.getContext()).
                            inflate((R.layout.message_row_incoming), viewGroup, false);
                    break;
                case BridgefyFile.OUTGOING_FILE:
                    messageView = LayoutInflater.from(viewGroup.getContext()).
                            inflate((R.layout.message_row_outgoing), viewGroup, false);
                    break;
            }

            return new MessageViewHolder(messageView);
        }

        @Override
        public void onBindViewHolder(final MessageViewHolder messageHolder, int position) {
            messageHolder.setBridgefyFile(bridgefyFiles.get(position));
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            final TextView txtMessage;
            BridgefyFile bridgefyFile;

            MessageViewHolder(View view) {
                super(view);
                txtMessage = view.findViewById(R.id.txtMessage);
            }

            void setBridgefyFile(BridgefyFile bridgefyFile) {
                this.bridgefyFile = bridgefyFile;
                    this.txtMessage.setText(bridgefyFile.getFilePath() + " File size "+bridgefyFile.getData().length);

            }
        }
    }
}
