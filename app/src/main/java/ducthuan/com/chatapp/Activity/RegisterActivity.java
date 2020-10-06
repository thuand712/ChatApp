package ducthuan.com.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import ducthuan.com.chatapp.R;

public class RegisterActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextInputLayout edUsername, edPassword, edEmail;
    Button btnRegister;

    FirebaseAuth auth;
    DatabaseReference data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        addControls();
        addEvents();
    }

    private void addEvents() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!CheckUsername() | !CheckEmail() | !CheckPassword()){
                    return;
                }

                Register();


            }
        });

    }

    private void Register() {
        final String username = edUsername.getEditText().getText().toString().trim();
        final String email = edEmail.getEditText().getText().toString().trim();
        String password = edPassword.getEditText().getText().toString().trim();

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    data = FirebaseDatabase.getInstance().getReference("Users").child(auth.getUid());
                    Map<String,String>map = new HashMap<>();
                    map.put("id",auth.getUid());
                    map.put("username",username);
                    map.put("email",email);
                    map.put("image","default");
                    map.put("status","offline");

                    data.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Sign up success !", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this,MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        }
                    });




                }else {
                    Toast.makeText(RegisterActivity.this, "Register fail !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addControls() {
        edUsername = findViewById(R.id.edUsername);
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        btnRegister = findViewById(R.id.btnRegister);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_white_24dp);
        auth = FirebaseAuth.getInstance();

    }

    private boolean CheckEmail(){
        String emailInput = edEmail.getEditText().getText().toString().trim();
        if(emailInput.isEmpty()){
            edEmail.setError("Email không được bỏ trống");
            return false;
            //Kiểm tra định dạng email
        }else if(emailInput.matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+")){
            edEmail.setError(null);
            edEmail.setErrorEnabled(false);
            return true;
        } else {
            edEmail.setError("Email sai định dạng");
            return false;
        }
    }

    private boolean CheckUsername(){
        String username = edUsername.getEditText().getText().toString().trim();
        if(username.isEmpty()){
            edUsername.setError("Username không được bỏ trống");
            return false;
        }else {
            edUsername.setError(null);
            edUsername.setErrorEnabled(false);
            return true;
        }
    }

    private boolean CheckPassword(){
        String password = edPassword.getEditText().getText().toString().trim();
        if(password.isEmpty()){
            edPassword.setError("Password không được bỏ trống");
            return false;
        }else if(password.length()<6){
            edPassword.setError("Password ít nhất 6 ký tự");
            return false;
        }else{
            edPassword.setError(null);
            edPassword.setErrorEnabled(false);
            return true;
        }
    }
}
