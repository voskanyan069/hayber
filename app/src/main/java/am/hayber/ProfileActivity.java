package am.hayber;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String currentUserID = mAuth.getCurrentUser().getUid();

    private final FirebaseDatabase root = FirebaseDatabase.getInstance();
    private final DatabaseReference usersRef = root.getReference().child("Users");
    private final DatabaseReference chatRequestRef = root.getReference().child("Chat Requests");
    private final DatabaseReference contactsRef = root.getReference().child("Contacts");
    private DatabaseReference thisUserRef;

    private String receiverUserID;
    private String CURRENT_STATE;

    private CircleImageView userProfileImage;
    private TextView userProfileName;
    private TextView userProfileStatus;
    private Button sendMessageRequestButton;
    private Button declineMessageRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();
    }

    private void init() {
        receiverUserID = getIntent().getExtras().get("visitUserID").toString();
        CURRENT_STATE = "new";

        thisUserRef = usersRef.child(receiverUserID);

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_user_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button);

        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        thisUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("name").getValue().toString();
                String userStatus = dataSnapshot.child("status").getValue().toString();

                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                }

                userProfileName.setText(userName);
                userProfileStatus.setText(userStatus);
                manageChatRequests();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequests() {

        chatRequestRef.child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID)) {
                            String REQUEST_TYPE = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (REQUEST_TYPE.equals("sent")) {
                                CURRENT_STATE = "request_sent";
                                sendMessageRequestButton.setText(R.string.cancel_chat_request);
                            } else if (REQUEST_TYPE.equals("received")) {
                                CURRENT_STATE = "request_received";
                                sendMessageRequestButton.setText(R.string.accept_chat_request);

                                declineMessageRequestButton.setVisibility(View.VISIBLE);
                                declineMessageRequestButton.setEnabled(true);

                                declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        } else {
                            contactsRef.child(currentUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID)) {
                                                CURRENT_STATE = "friends";
                                                sendMessageRequestButton.setText(R.string.remove_this_contact);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (currentUserID.equals(receiverUserID)) {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        } else {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessageRequestButton.setEnabled(false);

                    if (CURRENT_STATE.equals("new")) {
                        sendChatRequest();
                    }
                    if (CURRENT_STATE.equals("request_sent")) {
                        cancelChatRequest();
                    }
                    if (CURRENT_STATE.equals("request_received")) {
                        acceptChatRequest();
                    }
                    if (CURRENT_STATE.equals("friends")) {
                        removeSpecificContact();
                    }
                }
            });
        }
    }

    private void sendChatRequest() {
        chatRequestRef.child(currentUserID).child(receiverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserID).child(currentUserID).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendMessageRequestButton.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                sendMessageRequestButton.setText(R.string.cancel_chat_request);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest() {
        chatRequestRef.child(currentUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserID).child(currentUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendMessageRequestButton.setEnabled(true);
                                                CURRENT_STATE = "new";
                                                sendMessageRequestButton.setText(R.string.send_message);

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest() {
        contactsRef.child(currentUserID).child(receiverUserID).child("Contact").setValue("Saved")
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        contactsRef.child(receiverUserID).child(currentUserID).child("Contact").setValue("Saved")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            chatRequestRef.child(currentUserID).child(receiverUserID).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                chatRequestRef.child(receiverUserID).child(currentUserID).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    sendMessageRequestButton.setEnabled(true);
                                                                                    CURRENT_STATE = "friends";
                                                                                    sendMessageRequestButton.setText(R.string.remove_this_contact);

                                                                                    declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    declineMessageRequestButton.setEnabled(false);
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                }
            });
    }

    private void removeSpecificContact() {
        contactsRef.child(currentUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserID).child(currentUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendMessageRequestButton.setEnabled(true);
                                                CURRENT_STATE = "new";
                                                sendMessageRequestButton.setText(R.string.send_message);

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}