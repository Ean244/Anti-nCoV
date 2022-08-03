package edu.sg.nushigh.h1930006.anti_ncov.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.sg.nushigh.h1930006.anti_ncov.R;
import edu.sg.nushigh.h1930006.anti_ncov.SettingsUtil;
import edu.sg.nushigh.h1930006.anti_ncov.ui.login.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setSupportActionBar(findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ImageView profile = findViewById(R.id.image_profile);
        TextView name = findViewById(R.id.text_name);
        TextView email = findViewById(R.id.text_email);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (user == null)
            throw new IllegalStateException("User must be authenticated");

        email.setText(user.getEmail());

        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile)
                    .onLoadFailed(getDrawable(R.drawable.account));
        }

        name.setText(user.getDisplayName());

        String classCache = SettingsUtil.getInstance(this).getUserClass(user.getEmail());
        if (classCache != null) {
            updateNameText(name, classCache);
        } else {
            firestore.collection("users").document(user.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String mClass = task.getResult().get("class").toString();
                            updateNameText(name, mClass);
                            SettingsUtil.getInstance(this).setUserClass(user.getEmail(), mClass);
                        } else {
                            updateNameText(name, "M20403");
                        }
                    });
        }

        Button logout = findViewById(R.id.button_logout);
        logout.setOnClickListener(e -> {
            FirebaseAuth.getInstance().signOut();

            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void updateNameText(TextView name, String mClass) {
        String shortenedClass = mClass.substring(mClass.length() - 3);
        name.setText(String.format("%s (%s)", name.getText(), shortenedClass));
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
