package edu.sg.nushigh.h1930006.anti_ncov.ui.register;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import edu.sg.nushigh.h1930006.anti_ncov.R;
import edu.sg.nushigh.h1930006.anti_ncov.SettingsUtil;
import edu.sg.nushigh.h1930006.anti_ncov.ui.main.MainActivity;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    static int REQUEST_IMAGE_ATTACH = 2;
    private ImageView profile;
    private UploadTask uploadTask;
    private Task<Uri> imageTask;
    private ProgressBar uploadProgress;
    private FirebaseUser user;

    private static final Pattern CLASS_PATTERN = Pattern.compile("M20[123456]0[1234567]");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null)
            throw new IllegalStateException("User must be authenticated!");

        Button attachButton = findViewById(R.id.button_attach);
        Button createButton = findViewById(R.id.button_create);
        profile = findViewById(R.id.image_profile);
        EditText nameInput = findViewById(R.id.input_name);
        EditText classInput = findViewById(R.id.input_class);
        uploadProgress = findViewById(R.id.progress_upload);

        InputFilter[] editFilters = classInput.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.AllCaps();
        classInput.setFilters(newFilters);

        attachButton.setOnClickListener(e -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                    REQUEST_IMAGE_ATTACH);
        });

        createButton.setOnClickListener(e -> {
            if (nameInput.getText().toString().trim().isEmpty()) {
                nameInput.setError("Name is required!");
                return;
            }

            if (!CLASS_PATTERN.matcher(classInput.getText().toString()).matches()) {
                classInput.setError("Please input a valid class!");
                return;
            }

            if (uploadTask.isInProgress() || !imageTask.isComplete()) {
                Toast.makeText(RegisterActivity.this, "Profile picture is still being uploaded! Please wait for a while",
                        Toast.LENGTH_LONG).show();
                return;
            }

            String name = nameInput.getText().toString();
            String mClass = classInput.getText().toString();
            SettingsUtil.getInstance(this).setUserClass(user.getEmail(), mClass);

            Log.i(TAG, "updating database data");
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            Map<String, Object> data = new HashMap<>();
            data.put("class", mClass);
            firestore.collection("users").document(user.getUid()).set(data);

            Log.i(TAG, "updating profile");
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .setPhotoUri(imageTask.getResult())
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(t -> {
                        if (t.isSuccessful()) {
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to update profile! Please check your network connectivity!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_ATTACH && resultCode == RESULT_OK) {
            Log.i(TAG, "uploading profile picture!");

            StorageReference reference = FirebaseStorage.getInstance().getReference().child(user.getUid() + ".jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] imageData = baos.toByteArray();
            uploadTask = reference.putBytes(imageData);
            uploadProgress.setVisibility(View.VISIBLE);
            uploadProgress.setMin(0);
            uploadProgress.setMax(100);
            uploadTask
                    .addOnFailureListener(exception -> {
                        Toast.makeText(this, "Failed to upload profile picture! Please try again!", Toast.LENGTH_LONG).show();
                        uploadProgress.setVisibility(View.GONE);
                    })
                    .addOnProgressListener(taskSnapshot -> uploadProgress.setProgress((int) (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount() * 100)))
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(this, "Successfully uploaded profile photo!", Toast.LENGTH_LONG).show();
                        imageTask = reference.getDownloadUrl();
                        Glide.with(this)
                                .load(data.getData())
                                .apply(RequestOptions.circleCropTransform())
                                .into(profile);
                    });
        }
    }
}
