package ducthuan.com.chatapp.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import ducthuan.com.chatapp.Adapter.UserAdapter;
import ducthuan.com.chatapp.Model.Chatlist;
import ducthuan.com.chatapp.Model.User;
import ducthuan.com.chatapp.Notifications.Token;
import ducthuan.com.chatapp.R;


public class ChatFragment extends Fragment {
    View view;

    RecyclerView rvUser;
    UserAdapter userAdapter;
    ArrayList<User> users;
    ArrayList<Chatlist> chatlists;

    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        addControls();
        addEvents();
        return view;
    }

    private void addEvents() {


    }

    private void addControls() {
        rvUser = view.findViewById(R.id.rvUser);

        users = new ArrayList<>();
        rvUser.setHasFixedSize(true);
        rvUser.setLayoutManager(new LinearLayoutManager(getContext()));

        chatlists = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlists.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chatlist chatlist = dataSnapshot.getValue(Chatlist.class);
                    chatlists.add(chatlist);
                }

                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);
    }

    private void chatList() {

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    User user = dataSnapshot.getValue(User.class);
                    for(Chatlist chatlist:chatlists){
                        if(chatlist.getId().equals(user.getId())){
                            users.add(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(),users,true,firebaseUser);
                rvUser.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
