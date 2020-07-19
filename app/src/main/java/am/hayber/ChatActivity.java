package am.hayber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolbar;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String messageSenderID = mAuth.getCurrentUser().getUid();

    private final FirebaseDatabase root = FirebaseDatabase.getInstance();
    private DatabaseReference userMessageKeyRef;

    private TextView userName;
    private TextView userLastSeen;
    private CircleImageView userProfileImage;

    private ImageView sendMessageButton;
    private EditText messageInputField;

    private String messageReceiverID;
    private String messageReceiverName;
    private String messageReceiverImage;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        root.getReference().child("Messages").child(messageSenderID).child(messageReceiverID)
            .addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messagesList.add(messages);
                    messageAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    private void init() {
        messageReceiverID = getIntent().getExtras().get("visitUserID").toString();
        messageReceiverName = getIntent().getExtras().get("visitUserName").toString();
        messageReceiverImage = getIntent().getExtras().get("visitUserImage").toString();

        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarView);

        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_profile_last_seen);
        userProfileImage = findViewById(R.id.custom_profile_image);

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userProfileImage);

        messageInputField = findViewById(R.id.input_private_message);
        sendMessageButton = findViewById(R.id.send_private_message_button);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = findViewById(R.id.private_messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String messageText = messageInputField.getText().toString();

        if (!messageText.isEmpty()) {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            userMessageKeyRef = root.getReference().child("Messages").child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            root.getReference().updateChildren(messageBodyDetails);
            messageInputField.setText("");
        }
    }
}