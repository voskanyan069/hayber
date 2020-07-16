package am.hayber;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String currentUserID = mAuth.getCurrentUser().getUid();

    private final FirebaseDatabase root = FirebaseDatabase.getInstance();
    private final DatabaseReference currentContactsRef = root.getReference().child("Contacts").child(currentUserID);
    private final DatabaseReference userRef = root.getReference().child("Users");

    private View contactsView;

    private RecyclerView contactsList;

    public ContactsFragment() {}

    @Override
    public void onStart() {
        super.onStart();

        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactsList = contactsView.findViewById(R.id.contacts_list);
        contactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return contactsView;
    }

    private void init() {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(currentContactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {
                String usersIDs = getRef(position).getKey();

                userRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("name").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();

                        if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                            String userImage = dataSnapshot.child("image").getValue().toString();

                            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.userProfileImage);
                        }

                        holder.userName.setText(userName);
                        holder.userStatus.setText(userStatus);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);

                return new ContactsViewHolder(view);
            }
        };

        contactsList.setAdapter(adapter);
        adapter.startListening();
    }

    private static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userStatus;
        CircleImageView userProfileImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.other_user_name);
            userStatus = itemView.findViewById(R.id.other_user_status);
            userProfileImage = itemView.findViewById(R.id.other_user_profile_image);
        }

    }
}