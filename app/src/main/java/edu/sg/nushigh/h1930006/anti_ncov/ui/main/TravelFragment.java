package edu.sg.nushigh.h1930006.anti_ncov.ui.main;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import edu.sg.nushigh.h1930006.anti_ncov.KeyboardManager;
import edu.sg.nushigh.h1930006.anti_ncov.R;


public class TravelFragment extends Fragment {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isStartDate;
    private EditText startDateInput;
    private EditText endDateInput;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_travel, container, false);

        Button submit = root.findViewById(R.id.button_submit);
        EditText country = root.findViewById(R.id.input_destination_country);
        EditText city = root.findViewById(R.id.input_destination_city);
        startDateInput = root.findViewById(R.id.input_start_date);
        endDateInput = root.findViewById(R.id.input_end_date);

        startDateInput.setOnClickListener(e -> {
            startDateInput.setError(null);
            showDateDialog(container.getContext());
            isStartDate = true;
        });
        endDateInput.setOnClickListener(e -> {
            endDateInput.setError(null);
            showDateDialog(container.getContext());
            isStartDate = false;
        });

        submit.setOnClickListener(e -> {
            KeyboardManager.hideKeyboardFrom(getContext(), submit);

            if (startDate == null) {
                startDateInput.setError("This is a required field!");
                return;
            }
            if (endDate == null) {
                endDateInput.setError("This is a required field!");
                return;
            }

            if (country.getText().toString().isEmpty()) {
                country.setError("This is a required field!");
                return;
            }

            if (city.getText().toString().isEmpty()) {
                city.setError("This is a required field!");
                return;
            }

            if (startDate.isAfter(endDate)) {
                Toast.makeText(getContext(), "Start date cannot be after end date!", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(getContext(), "Submitting travel declaration...", Toast.LENGTH_LONG).show();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            DocumentReference reference = firestore.collection("travel").document();
            Map<String, Object> data = new HashMap<>();
            data.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            data.put("user", user.getEmail());
            data.put("country", country.getText().toString());
            data.put("city", city.getText().toString());
            data.put("start-date", startDateInput.getText().toString());
            data.put("end-date", endDateInput.getText().toString());

            reference.set(data)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            AlertDialog alertDialog = new AlertDialog.Builder(container.getContext()).setTitle("Success")
                                    .setMessage("Your travel declaration has been submitted!")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        country.getText().clear();
                                        city.getText().clear();
                                        startDateInput.getText().clear();
                                        endDateInput.getText().clear();
                                    }).create();
                            alertDialog.show();
                        } else {
                            Toast.makeText(getContext(), "Failed to submit travel declaration! Please check your network connection!", Toast.LENGTH_LONG).show();
                        }
                    });
        });
        return root;
    }

    private void showDateDialog(Context context) {
        LocalDate now = LocalDate.now();
        new DatePickerDialog(context, new DateAlertListener(),
                now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()).show();
    }

    private class DateAlertListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            if (isStartDate) {
                startDate = LocalDate.of(year, month + 1, dayOfMonth);
                startDateInput.setText(startDate.format(DATE_FORMAT));
            } else {
                endDate = LocalDate.of(year, month + 1, dayOfMonth);
                endDateInput.setText(endDate.format(DATE_FORMAT));
            }
        }
    }

}
