package com.graff.tester;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        TextView redirect = findViewById(R.id.signupRedirect);
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
                    EditText text = findViewById(R.id.editTextEmail);
                    String email = text.getText().toString();
                    EditText textPsw = findViewById(R.id.editTextPassword);
                    String password = textPsw.getText().toString();
                    FirebaseManager manager = new FirebaseManager();
                    if (!email.isEmpty() && !password.isEmpty()) {
                        manager.loginUser(email, password, new FirebaseManager.OnLoginCallback(){
                            @Override
                            public void onLoginSuccess() {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            @Override
                            public void onLoginFailed(String errorMessage) {
                                Toast.makeText(LoginActivity.this, "שגיאה: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                        } );


        redirect.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerView_gallery), (v, insets) -> {
//
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
}