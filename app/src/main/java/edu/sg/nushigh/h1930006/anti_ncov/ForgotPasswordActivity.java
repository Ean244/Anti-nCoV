package edu.sg.nushigh.h1930006.anti_ncov;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import edu.sg.nushigh.h1930006.anti_ncov.ui.login.LoginActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setSupportActionBar(findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        EditText email = findViewById(R.id.input_email);
        Button reset = findViewById(R.id.button_request_reset);

        reset.setOnClickListener(e -> {
            if (!LoginActivity.EMAIL_PATTERN.matcher(email.getText().toString()).matches()) {
                email.setError("Please provide a valid school email");
                return;
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    AlertDialog alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this).setTitle("Success")
                            .setMessage("Password reset email sent!")
                            .setPositiveButton("OK", (dialog, which) -> {
                                email.getText().clear();
                            }).create();
                    alertDialog.show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Failed to send password reset link! Email not found!", Toast.LENGTH_LONG)
                            .show();
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
