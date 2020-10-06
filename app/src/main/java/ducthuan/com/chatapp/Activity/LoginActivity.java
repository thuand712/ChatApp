package ducthuan.com.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import ducthuan.com.chatapp.R;

public class LoginActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextInputLayout edPassword, edEmail;
    Button btnLogin;
    TextView txtForgotPassword;

    FirebaseAuth auth;
    DatabaseReference data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login();
            }
        });

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });

    }

    private void Login() {
        String email = edEmail.getEditText().getText().toString().trim();
        String password = edPassword.getEditText().getText().toString().trim();
        if(email.equals("") || password.equals("")){
            Toast.makeText(this, "Please enter your email and password !", Toast.LENGTH_SHORT).show();
        }else {
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "Login in successfully !", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this,MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    }else {
                        Toast.makeText(LoginActivity.this, "Please check your email and password!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }

    private void addControls() {
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        btnLogin = findViewById(R.id.btnLogin);
        toolbar = findViewById(R.id.toolbar);
        txtForgotPassword = findViewById(R.id.txtForgotpassword);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_white_24dp);
        auth = FirebaseAuth.getInstance();

    }
}
