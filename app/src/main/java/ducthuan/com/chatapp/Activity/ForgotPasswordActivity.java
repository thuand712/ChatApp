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
import com.google.firebase.auth.FirebaseAuth;

import ducthuan.com.chatapp.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextInputLayout edEmail;
    Button btnReset;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

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

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!CheckEmail()){
                    return;
                }
                ResetPassword();
            }
        });


    }

    private void ResetPassword() {
        String email = edEmail.getEditText().getText().toString().trim();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this, "Please check your email and follow the instructions!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ForgotPasswordActivity.this,LoginActivity.class));
                }else {
                    Toast.makeText(ForgotPasswordActivity.this, "Wrong email!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addControls() {
        edEmail = findViewById(R.id.edEmail);
        toolbar = findViewById(R.id.toolbar);
        btnReset = findViewById(R.id.btnReset);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_white_24dp);
        auth = FirebaseAuth.getInstance();

    }

    private boolean CheckEmail(){
        String email = edEmail.getEditText().getText().toString().trim();
        if(email.equals("")){
            edEmail.setError("Email is not empty");
            return false;
        }else if(email.matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+")){
            edEmail.setErrorEnabled(false);
            return true;
        }else {
            edEmail.setError("Email sai định dạng");
            return false;
        }
    }
}
