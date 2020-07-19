package am.hayber;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GroupsFragment extends Fragment {

    private View groupsFragmentView;
    private ListView groupsList;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listOfGroups = new ArrayList<>();

    private final DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

    public GroupsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupsFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        init();
        retrieveAndDisplayGroups();

        return groupsFragmentView;
    }

    private void init() {
        groupsList = groupsFragmentView.findViewById(R.id.groups_list);
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, listOfGroups);
        groupsList.setAdapter(arrayAdapter);

        listClick();
    }

    private void listClick() {
        groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName = parent.getItemAtPosition(position).toString();

                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                startActivity(groupChatIntent);
            }
        });
    }

    private void retrieveAndDisplayGroups() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    set.add(snapshot.getKey());
                }

                listOfGroups.clear();
                listOfGroups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}