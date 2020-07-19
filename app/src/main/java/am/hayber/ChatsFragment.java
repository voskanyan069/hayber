package am.hayber;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser currentUser = mAuth.getCurrentUser();
    private final String currentUserID = currentUser.getUid();

    private final FirebaseDatabase root = FirebaseDatabase.getInstance();
    private final DatabaseReference chatsRef = root.getReference().child("Contacts").child(currentUserID);
    private final DatabaseReference usersRef = root.getReference().child("Users");

    private View privateChatsView;
    private RecyclerView chatsList;

    private String userImage = "default_image";

    public ChatsFragment() {}

    @Override
    public void onStart() {
        super.onStart();

        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsList = privateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatsView;
    }

    private void init() {
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String usersIDs = getRef(position).getKey();
                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            final String userStatus = dataSnapshot.child("status").getValue().toString();

                            if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                                userImage = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.userProfileImage);
                            }

                            holder.userName.setText(userName);
                            holder.userStatus.setText("Last Seen: " + "\n" + "Date " + " Time");

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                    intent.putExtra("visitUserID", usersIDs);
                                    intent.putExtra("visitUserName", userName);
                                    intent.putExtra("visitUserImage", userImage);
                                    startActivity(intent);
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
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);

                return new ChatsViewHolder(view);
            }
        };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    private static class ChatsViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userStatus;
        CircleImageView userProfileImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.display_user_name);
            userStatus = itemView.findViewById(R.id.display_user_status);
            userProfileImage = itemView.findViewById(R.id.display_user_profile_image);
        }
    }
}