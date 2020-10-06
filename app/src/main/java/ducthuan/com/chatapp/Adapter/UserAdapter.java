package ducthuan.com.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ducthuan.com.chatapp.Activity.ChatActivity;
import ducthuan.com.chatapp.Model.Chat;
import ducthuan.com.chatapp.Model.User;
import ducthuan.com.chatapp.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    Context context;
    ArrayList<User>users;
    boolean isChat;
    boolean seen;
    boolean checkLastUser;
    String lastMsg;
    FirebaseUser firebaseUser;

    public UserAdapter(Context context, ArrayList<User> users, boolean isChat,FirebaseUser firebaseUser) {
        this.context = context;
        this.users = users;
        this.isChat = isChat;
        this.firebaseUser = firebaseUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.raw_user,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = users.get(position);
        holder.txtUser.setText(user.getUsername());
        if(user.getImage().equals("default")){
            holder.imgUser.setImageResource(R.drawable.ic_account_circle_primary_24dp);
        }else {
            Picasso.with(context).load(user.getImage()).into(holder.imgUser);
        }

        if(user.getStatus().equals("online")){
            holder.imgOnline.setVisibility(View.VISIBLE);
            holder.imgOffline.setVisibility(View.GONE);
        }else {
            holder.imgOnline.setVisibility(View.GONE);
            holder.imgOffline.setVisibility(View.VISIBLE);
        }


        if(isChat){
            lastMessage(user.getId(), holder.txtLastMSG);
        }else {
            holder.txtLastMSG.setVisibility(View.GONE);
        }




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userid",user.getId());
                context.startActivity(intent);
            }
        });
    }



    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgUser,imgOnline,imgOffline;
        TextView txtUser,txtLastMSG;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.imgUser);
            txtUser = itemView.findViewById(R.id.txtUser);
            imgOnline = itemView.findViewById(R.id.imgOnline);
            imgOffline = itemView.findViewById(R.id.imgOffline);
            txtLastMSG = itemView.findViewById(R.id.txtLastMSG);
        }
    }

    private void lastMessage(final String id, final TextView txtLastMSG) {
        lastMsg = "default";
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(id)
                            || chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(id)){
                        if(chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(id)){
                            checkLastUser = true;
                        }else {
                            checkLastUser = false;
                        }

                        lastMsg = chat.getMessage();
                        seen = chat.isSeen();
                    }
                }

                if(!lastMsg.equals("default")){
                    txtLastMSG.setText(lastMsg);
                    if(seen == false && checkLastUser == false){
                        txtLastMSG.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    }else {
                        txtLastMSG.setTextColor(context.getResources().getColor(R.color.colorGray));
                        txtLastMSG.setTypeface(Typeface.DEFAULT);
                    }
                }else {
                    txtLastMSG.setText("No message");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

}
