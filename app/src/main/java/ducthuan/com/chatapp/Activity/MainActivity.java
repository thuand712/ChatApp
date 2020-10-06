package ducthuan.com.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import ducthuan.com.chatapp.Fragment.AccountFragment;
import ducthuan.com.chatapp.Fragment.ChatFragment;
import ducthuan.com.chatapp.Fragment.UserFragment;
import ducthuan.com.chatapp.Model.Chat;
import ducthuan.com.chatapp.Model.User;
import ducthuan.com.chatapp.R;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "99";
    public static final String CHANNEL_NAME = "notify";

    CircleImageView imgUser;
    TextView txtUser,txtSL;
    ImageView imgMore;

    FrameLayout frameLayout;
    BottomNavigationView naviBottom;

    FirebaseUser firebaseUser;
    DatabaseReference data;
    ArrayList<Chat>listChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addControls();
        addEvents();
    }

    private void addEvents() {
        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,imgMore);
                popupMenu.inflate(R.menu.menu_more);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.itLogout){
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(MainActivity.this,StartActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();
                        }else if(item.getItemId() == R.id.itNotify){
                            sendNotify();
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        naviBottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment seletedFragment = null;
                switch (menuItem.getItemId()) {
                    case R.id.itChat:
                        seletedFragment = new ChatFragment();
                        break;
                    case R.id.itUsers:
                        seletedFragment = new UserFragment();
                        break;
                    case R.id.itAccount:
                        seletedFragment = new AccountFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, seletedFragment).commit();
                return true;
            }
        });

    }

    private void sendNotify() {

        createNotificationChannel();

        Intent intent = new Intent(MainActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_account_circle_primary_24dp)
                .setContentTitle("New notification")
                .setContentText("This is demo notification")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);
        notificationManagerCompat.notify(200, builder.build());

    }


    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void addControls() {
        imgUser = findViewById(R.id.imgUser);
        txtUser = findViewById(R.id.txtUser);
        imgMore = findViewById(R.id.imgMore);
        frameLayout = findViewById(R.id.frameLayout);
        naviBottom = findViewById(R.id.naviBottom);

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) naviBottom.getChildAt(0);
        View view = menuView.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) view;
        View slmsg = LayoutInflater.from(this).inflate(R.layout.custom_item_giohang,itemView,false);
        itemView.addView(slmsg);
        txtSL = view.findViewById(R.id.txtSL);
        listChat = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Query query = FirebaseDatabase.getInstance().getReference("Chats")
                .orderByChild("receiver").equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int sl = 0;
                listChat.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    listChat.add(chat);
                }

                for (int i = 0; i < listChat.size(); i++) {
                    if(!listChat.get(i).isSeen()){
                        int dem = 0;
                        for (int j = 0; j < i; j++) {
                            if(!listChat.get(j).isSeen()){
                                if(listChat.get(i).getSender().equals(listChat.get(j).getSender())){
                                    dem = 1;
                                    break;
                                }
                            }
                        }
                        if(dem==0){
                            sl++;
                        }
                    }
                }

                if(sl > 0){
                    txtSL.setVisibility(View.VISIBLE);
                    txtSL.setText(sl+"");
                }else {
                    txtSL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        data = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                txtUser.setText(user.getUsername());
                if(user.getImage().equals("default")){
                    imgUser.setImageResource(R.drawable.ic_account_circle_white_24dp);
                }else {
                    Picasso.with(MainActivity.this).load(user.getImage()).into(imgUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,new ChatFragment()).commit();

    }


    public void status(String status){
        data = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        Map<String,Object> map = new HashMap<>();
        map.put("status",status);
        data.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

}
