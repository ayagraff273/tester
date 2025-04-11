package com.graff.tester;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> {
            EditText text = findViewById(R.id.editTextEmail);
            String email = text.getText().toString();
            EditText textPsw = findViewById(R.id.editTextPassword);
            String password = textPsw.getText().toString();
            FirebaseManager manager = new FirebaseManager();
            if (!email.isEmpty() && !password.isEmpty()) {
                manager.createUser(email, password, new FirebaseManager.OnUserAddedCallback() {
                    @Override
                    public void onUserAddedSuccessfully() {
                        onUserAdded();
                    }
                    @Override
                    public void onUserAdditionFailed(String errorMessage) {
                        onUserAddFailed(errorMessage);
                    }
                });
            }
        });
    }

    void onUserAdded() {
        //Added user successfully
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    void onUserAddFailed(String errorMessage) {
        Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }
}