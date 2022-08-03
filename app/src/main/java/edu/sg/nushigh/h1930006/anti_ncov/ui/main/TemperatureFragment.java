package edu.sg.nushigh.h1930006.anti_ncov.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import edu.sg.nushigh.h1930006.anti_ncov.KeyboardManager;
import edu.sg.nushigh.h1930006.anti_ncov.R;

public class TemperatureFragment extends Fragment {
    private static final Pattern TEMP_PATTERN = Pattern.compile("[0-9]{2}\\.[0-9]");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_temperature, container, false);

        EditText temperatureField = root.findViewById(R.id.input_temperature);
        Button submitButton = root.findViewById(R.id.button_subscribe);

        submitButton.setOnClickListener(e -> {
            KeyboardManager.hideKeyboardFrom(getContext(), submitButton);
            String input = temperatureField.getText().toString();
            if (!TEMP_PATTERN.matcher(input).matches()) {
                temperatureField.setError("Please leave your temperature to 1dp");
                return;
            }
            Toast.makeText(getContext(), "Submitting temperature...", Toast.LENGTH_LONG).show();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            DocumentReference reference = firestore.collection("temperature").document(user.getEmail());
            Map<String, Object> data = new HashMap<>();
            data.put(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), input);
            reference.set(data, SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            AlertDialog alertDialog = new AlertDialog.Builder(container.getContext()).setTitle("Success")
                                    .setMessage("Your temperature has been submitted!")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        temperatureField.getText().clear();
                                        temperatureField.clearFocus();
                                    }).create();
                            alertDialog.show();
                        } else {
                            Toast.makeText(getContext(), "Failed to submit temperature! Please check your network connection!", Toast.LENGTH_LONG).show();
                        }
                    });
        });
        return root;
    }

}