package am.hayber;

import android.annotation.SuppressLint;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String messageSenderID = mAuth.getCurrentUser().getUid();

    private final FirebaseDatabase root = FirebaseDatabase.getInstance();
    private final DatabaseReference usersRef = root.getReference().child("Users");

    private List<Messages> userMessagesList;

    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String messageType = messages.getType();

        if (messageType.equals("text")) {
            MessageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
            MessageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderID)) {
                MessageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                MessageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                MessageViewHolder.senderMessageText.setText(messages.getMessage());
            } else {
                MessageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                MessageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                MessageViewHolder.receiverMessageText.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        @SuppressLint("StaticFieldLeak")
        public static TextView senderMessageText, receiverMessageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
        }
    }
}
