package ducthuan.com.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ducthuan.com.chatapp.Adapter.ChatAdapter;
import ducthuan.com.chatapp.Model.Chat;
import ducthuan.com.chatapp.Model.User;
import ducthuan.com.chatapp.Notifications.Client;
import ducthuan.com.chatapp.Notifications.Data;
import ducthuan.com.chatapp.Notifications.MyResponse;
import ducthuan.com.chatapp.Notifications.Sender;
import ducthuan.com.chatapp.Notifications.Token;
import ducthuan.com.chatapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    CircleImageView imgUser,imgOnline,imgOffline;
    ImageView imgBack;
    ImageButton btnSend;
    EditText edMessenger;
    TextView txtUser;

    RecyclerView rvTinNhan;
    ChatAdapter chatAdapter;
    ArrayList<Chat>chats;

    Intent intent;
    String userid = "";

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    //khai bao de khi thoat man hinh chat se huy trang thai
    ValueEventListener seenEventMessage;

    APIService apiService;
    boolean notify = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        intent = getIntent();
        if(intent.hasExtra("userid")){
            userid = intent.getStringExtra("userid");
            addControls();
            seeMessage();
            addEvents();
        }

    }

    private void seeMessage() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        seenEventMessage = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)){
                        Map<String, Object>map = new HashMap<>();
                        map.put("seen",true);
                        dataSnapshot.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addControls() {
        imgBack = findViewById(R.id.imgBack);
        imgUser = findViewById(R.id.imgUser);
        txtUser = findViewById(R.id.txtUser);
        rvTinNhan = findViewById(R.id.rvTinNhan);
        edMessenger = findViewById(R.id.edSend);
        btnSend = findViewById(R.id.btnSend);
        imgOnline = findViewById(R.id.imgOnline);
        imgOffline = findViewById(R.id.imgOffline);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        rvTinNhan.setHasFixedSize(true);

        //RecycleView show bottom on top
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this);
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        rvTinNhan.setLayoutManager(layoutManager);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                txtUser.setText(user.getUsername());
                if(user.getImage().equals("default")){
                    imgUser.setImageResource(R.drawable.ic_account_circle_white_24dp);
                }else {
                    Picasso.with(ChatActivity.this).load(user.getImage()).into(imgUser);
                }

                if(user.getStatus().equals("online")){
                    imgOnline.setVisibility(View.VISIBLE);
                    imgOffline.setVisibility(View.GONE);
                }else {
                    imgOnline.setVisibility(View.GONE);
                    imgOffline.setVisibility(View.VISIBLE);
                }

                readMessage(firebaseUser.getUid(),userid,user.getImage());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");




    }

    private void readMessage(final String uid, final String userid, final String image) {

        chats = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(chat.getSender().equals(uid) && chat.getReceiver().equals(userid) ||
                            chat.getReceiver().equals(uid) && chat.getSender().equals(userid)){
                        chats.add(chat);
                    }
                }

                chatAdapter = new ChatAdapter(ChatActivity.this,chats,image);
                rvTinNhan.setAdapter(chatAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addEvents() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = edMessenger.getText().toString().trim();
                if(!msg.equals("")){
                    sendMessage(firebaseUser.getUid(),userid,msg);
                }
                edMessenger.setText("");
            }
        });

    }

    private void sendMessage(String uid, final String userid, String msg) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        Map<String,Object>map = new HashMap<>();
        map.put("sender",uid);
        map.put("receiver",userid);
        map.put("message",msg);
        map.put("seen",false);
        databaseReference.push().setValue(map);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid()).child(userid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg1 = msg;
//
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(userid, user.getUsername(), msg1);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotifiaction(String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // goi class token
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_launcher, username+": "+message, "Có tin nhắn mới!!",
                            userid);
                    // goi class sender
                    Sender sender = new Sender(data, token.getToken());
                    // goi sendNotification trong token dung CallBack retrofit xem dong bo ko
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(ChatActivity.this, "Thất bại!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("notshow",MODE_PRIVATE).edit();
        editor.putString("cu",userid);
        editor.apply();
    }


    public void status(String status){
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        Map<String,Object>map = new HashMap<>();
        map.put("status",status);
        databaseReference.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(seenEventMessage);
        status("offline");
        currentUser("none");
    }
}
