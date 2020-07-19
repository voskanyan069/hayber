package am.hayber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String currentUserID = mAuth.getCurrentUser().getUid();

    private final FirebaseDatabase root = FirebaseDatabase.getInstance();
    private final DatabaseReference usersRef = root.getReference().child("Users");
    private DatabaseReference groupsNameRef;
    private DatabaseReference groupMessageKeyRef;

    private Toolbar groupChatToolbar;
    private EditText userMessageInput;
    private ImageView sendMessageBtn;

    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private String currentGroupName;
    private String currentUserName;
    private String currentDate;
    private String currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        init();
        getUserInfo();
        sendMessage();
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupsNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    displayAllMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    displayAllMessages(dataSnapshot);
                }
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

    @SuppressLint("WrongViewCast")
    private void init() {
        currentGroupName = getIntent().getExtras().get("groupName").toString();

        groupsNameRef = root.getReference().child("Groups").child(currentGroupName);

        groupChatToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(groupChatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle(currentGroupName);

        mScrollView = findViewById(R.id.my_scroll_view);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
        userMessageInput = findViewById(R.id.input_group_message);
        sendMessageBtn = findViewById(R.id.send_message_button);

        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void getUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageIntoDB();
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                userMessageInput.setText("");
            }
        });
    }

    private void displayAllMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            displayTextMessages.append(chatName + "\n" + chatMessage + "\n" + chatTime + " | " + chatDate + "\n\n");
        }
    }

    private void saveMessageIntoDB() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupsNameRef.push().getKey();

        if (!(TextUtils.isEmpty(message))) {
            Calendar calForDate = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupsNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupsNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name", currentUserName);
                messageInfoMap.put("message", message);
                messageInfoMap.put("date", currentDate);
                messageInfoMap.put("time", currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);

        }
    }
}