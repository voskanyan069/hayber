
// TODO = CANCEL REQUEST IF IT ALREADY SENT BY THIS USER





package am.hayber;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class RequestsFragment extends Fragment {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String currentUserID = mAuth.getCurrentUser().getUid();

    private final FirebaseDatabase root = FirebaseDatabase.getInstance();
    private final DatabaseReference usersRef = root.getReference().child("Users");
    private final DatabaseReference chatRequestRef = root.getReference().child("Chat Requests");
    private final DatabaseReference contactsRef = root.getReference().child("Contacts");
    private final DatabaseReference currentUserRequestsRef = chatRequestRef.child(currentUserID);

    private View requestsFragmentView;
    private RecyclerView requestsList;

    public RequestsFragment() {}

    @Override
    public void onStart() {
        super.onStart();

        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        requestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        requestsList = requestsFragmentView.findViewById(R.id.chat_requests_list);
        requestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestsFragmentView;
    }

    private void init() {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(currentUserRequestsRef, Contacts.class)
                .build();

        final FirebaseRecyclerAdapter<Contacts, RequestsFragment.RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestsFragment.RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsFragment.RequestsViewHolder holder, int position, @NonNull Contacts model) {
                final String listUserID = getRef(position).getKey();
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final String type = dataSnapshot.getValue().toString();
                            usersRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (type.equals("received")) {
                                        holder.itemView.findViewById(R.id.request_action_buttons).setVisibility(View.VISIBLE);
                                    } else {
                                        holder.itemView.findViewById(R.id.your_request).setVisibility(View.VISIBLE);
                                    }

                                    final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                    String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                    if (dataSnapshot.hasChild("image")) {
                                        String requestUserImage = dataSnapshot.child("image").getValue().toString();

                                        Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.userProfileImage);
                                    }

                                    holder.userName.setText(requestUserName);
                                    holder.userStatus.setText(requestUserStatus);

                                    holder.acceptRequestButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            contactsRef.child(currentUserID).child(listUserID).child("Contact").setValue("Saved")
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                contactsRef.child(listUserID).child(currentUserID).child("Contact").setValue("Saved")
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    chatRequestRef.child(currentUserID).child(listUserID).removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        chatRequestRef.child(listUserID).child(currentUserID).removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if (task.isSuccessful()) {
                                                                                                                            Toast.makeText(getContext(), "Contact Saved", Toast.LENGTH_LONG).show();
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
                                    });
                                    holder.cancelRequestButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            chatRequestRef.child(currentUserID).child(listUserID).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                chatRequestRef.child(listUserID).child(currentUserID).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_LONG).show();
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    });
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
            }

            @NonNull
            @Override
            public RequestsFragment.RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);

                return new RequestsFragment.RequestsViewHolder(view);
            }
        };

        requestsList.setAdapter(adapter);
        adapter.startListening();
    }

    private static class RequestsViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userStatus;
        CircleImageView userProfileImage;
        Button acceptRequestButton;
        Button cancelRequestButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.display_user_name);
            userStatus = itemView.findViewById(R.id.display_user_status);
            userProfileImage = itemView.findViewById(R.id.display_user_profile_image);
            acceptRequestButton = itemView.findViewById(R.id.request_accept_button);
            cancelRequestButton = itemView.findViewById(R.id.request_cancel_button);
        }
    }
}