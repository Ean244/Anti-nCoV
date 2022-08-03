package edu.sg.nushigh.h1930006.anti_ncov.ui.login;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.regex.Pattern;

import edu.sg.nushigh.h1930006.anti_ncov.ForgotPasswordActivity;
import edu.sg.nushigh.h1930006.anti_ncov.R;
import edu.sg.nushigh.h1930006.anti_ncov.SettingsUtil;
import edu.sg.nushigh.h1930006.anti_ncov.ui.main.MainActivity;
import edu.sg.nushigh.h1930006.anti_ncov.ui.register.RegisterActivity;

public class LoginActivity extends AppCompatActivity {

    public static final Pattern EMAIL_PATTERN = Pattern.compile("(h[0-9]{7}|nhs(.+))@(nushigh|nus)\\.edu\\.sg");
    private FirebaseAuth auth;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseFirestore.setLoggingEnabled(true);

        setupNightMode();

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        registerTemperatureAlarm();

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            proceedToMainActivity();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText emailInput = findViewById(R.id.input_temperature);
        final EditText passwordInput = findViewById(R.id.input_password);
        final Button loginButton = findViewById(R.id.button_login);
        final Button registerButton = findViewById(R.id.button_register);
        final ProgressBar loadingBar = findViewById(R.id.progress_loading);
        final Button forgotButton = findViewById(R.id.button_forgot);

        forgotButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            if (!processInput(emailInput, passwordInput))
                return;
            loadingBar.setVisibility(View.VISIBLE);
            auth.signInWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            proceedToMainActivity();
                        } else {
                            loadingBar.setVisibility(View.INVISIBLE);
                            emailInput.setError("Invalid Email or Password");
                            passwordInput.setError("Invalid Email or Password");
                            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        registerButton.setOnClickListener(e -> {
            if (!processInput(emailInput, passwordInput))
                return;
            loadingBar.setVisibility(View.VISIBLE);
            FirebaseAuth auth = FirebaseAuth.getInstance();

            auth.createUserWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(this, RegisterActivity.class);
                            startActivity(intent);
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                emailInput.setError("That email has been taken!");
                                Toast.makeText(this, "That email has been registered!", Toast.LENGTH_SHORT).show();
                                loadingBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        });
    }

    private void setupNightMode() {
        if (SettingsUtil.getInstance(this).isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void proceedToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Welcome, " + auth.getCurrentUser().getDisplayName(), Toast.LENGTH_LONG)
                .show();
    }

    private boolean processInput(EditText emailInput, EditText passwordInput) {
        emailInput.setError(null);
        passwordInput.setError(null);

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailInput.setError("Please Provide A Valid School Email");
            return false;
        }

        if (password.length() < 8) {
            passwordInput.setError("Password Must Be at Least 8 characters");
            return false;
        }
        return true;
    }

    private void registerTemperatureAlarm() {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmMgr == null) {
            Log.e("LOGIN", "Failed to obtain Alarm Service!");
            return;
        }

        Intent i = new Intent(LoginActivity.this, AlarmReceiver.class);
        final PendingIntent pendingIntent =
                PendingIntent.getBroadcast(LoginActivity.this, 0, i,
                        PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            //alarm already created
            return;
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, createAlarmCalendar(8, 0).getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, createAlarmCalendar(20, 30).getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    private Calendar createAlarmCalendar(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar;
    }
}
