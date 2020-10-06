package ducthuan.com.chatapp.Adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ducthuan.com.chatapp.Model.Chat;
import ducthuan.com.chatapp.R;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    Context context;
    ArrayList<Chat>chats;
    String imageURL;

    FirebaseUser firebaseUser;

    public ChatAdapter(Context context, ArrayList<Chat> chats, String imageURL) {
        this.context = context;
        this.chats = chats;
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_LEFT){
            view = LayoutInflater.from(context).inflate(R.layout.raw_chat_left,parent,false);
        }else {
            view = LayoutInflater.from(context).inflate(R.layout.raw_chat_right,parent,false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.txtMessage.setText(chat.getMessage());
        if(imageURL.equals("default")){
            holder.imgUser.setImageResource(R.drawable.ic_account_circle_primary_24dp);
        }else {
            Picasso.with(context).load(imageURL).into(holder.imgUser);
        }

        if(position == chats.size()-1){
            if(chat.isSeen()){
                holder.txtSeen.setText("Seen");
            }else {
                holder.txtSeen.setText("Sent");
            }
        }else {
            holder.txtSeen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgUser;
        TextView txtMessage,txtSeen;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgUser = itemView.findViewById(R.id.imgUser);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtSeen = itemView.findViewById(R.id.txtSeen);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chats.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        return MSG_TYPE_LEFT;
    }
}
