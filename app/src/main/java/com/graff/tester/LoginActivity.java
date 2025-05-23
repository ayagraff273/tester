package com.graff.tester;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        TextView redirect = findViewById(R.id.signupRedirect);
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
                    EditText textemail = findViewById(R.id.editTextEmail);
                    String email = textemail.getText().toString();
                    EditText textPsw = findViewById(R.id.editTextPassword);
                    String password = textPsw.getText().toString();
                    DatabaseManager manager = DataManagerFactory.getDataManager();
                    if (!email.isEmpty() && !password.isEmpty()) {
                        manager.loginUser(email, password, new DatabaseManager.OnLoginCallback(){
                            @Override
                            public void onLoginSuccess() {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            @Override
                            public void onLoginFailed(String errorMessage) {
                                Toast.makeText(LoginActivity.this, "error: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                        } );


        redirect.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

    }
}