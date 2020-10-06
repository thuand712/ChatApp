package ducthuan.com.chatapp.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import ducthuan.com.chatapp.Adapter.UserAdapter;
import ducthuan.com.chatapp.Model.User;
import ducthuan.com.chatapp.R;


public class UserFragment extends Fragment {

    RecyclerView rvUsers;
    UserAdapter userAdapter;
    ArrayList<User>users;
    EditText edSearch;

    FirebaseUser firebaseUser;
    DatabaseReference data;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user, container, false);
        addControls();
        addEvents();
        return view;
    }

    private void addEvents() {
        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUser(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void searchUser(String charSequence) {

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
                .startAt(charSequence)
                .endAt(charSequence+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    if(!user.getId().equals(firebaseUser.getUid())){
                        users.add(user);
                    }
                }

                userAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addControls() {
        rvUsers = view.findViewById(R.id.rvUsers);
        edSearch = view.findViewById(R.id.edSearch);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        rvUsers.setHasFixedSize(true);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsers.setNestedScrollingEnabled(true);
        rvUsers.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        users = new ArrayList<>();

        data = FirebaseDatabase.getInstance().getReference("Users");
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    if(!user.getId().equals(firebaseUser.getUid())){
                        users.add(user);
                    }
                }

                userAdapter = new UserAdapter(getContext(),users,false,firebaseUser);
                rvUsers.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


}
