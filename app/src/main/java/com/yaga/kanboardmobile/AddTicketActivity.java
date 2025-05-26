package com.yaga.kanboardmobile;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTicketActivity extends AppCompatActivity {

    private EditText editTextCreatedAt;
    private EditText editTextDueDate;

    private final Calendar dueCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 💥 Возвращаем статус-бар
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        setContentView(R.layout.activity_add_ticket);

        // Элементы интерфейса
        EditText editTextTitle = findViewById(R.id.editTextTitle);
        EditText editTextDescription = findViewById(R.id.editTextDescription);
        Spinner spinnerStatus = findViewById(R.id.spinnerStatus);
        editTextCreatedAt = findViewById(R.id.editTextCreatedAt);
        editTextDueDate = findViewById(R.id.editTextDueDate);
        Button buttonSave = findViewById(R.id.buttonSave);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_add);

        // Статус
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"К выполнению", "В процессе", "Готово"}
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Установка текущего времени по умолчанию
        if (editTextCreatedAt.getText().toString().isEmpty()) {
            String now = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());
            editTextCreatedAt.setText(now);
        }

        // Выбор срока окончания
        editTextDueDate.setOnClickListener(v -> showDateTimePicker());

        // Проверка на редактирование
        Intent intent = getIntent();
        boolean isEdit = intent.getBooleanExtra("isEdit", false);

        if (isEdit) {
            editTextTitle.setText(intent.getStringExtra("title"));
            editTextDescription.setText(intent.getStringExtra("description"));
            editTextCreatedAt.setText(intent.getStringExtra("created_at"));
            editTextDueDate.setText(intent.getStringExtra("due_date"));

            String status = intent.getStringExtra("status");
            if (status != null) {
                int index = statusAdapter.getPosition(status);
                if (index != -1) {
                    spinnerStatus.setSelection(index);
                }
            }
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        buttonSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String status = spinnerStatus.getSelectedItem().toString();
            String createdAt = editTextCreatedAt.getText().toString().trim();
            String dueDate = editTextDueDate.getText().toString().trim();

            if (!title.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("title", title);
                resultIntent.putExtra("description", description);
                resultIntent.putExtra("status", status);
                resultIntent.putExtra("created_at", createdAt);
                resultIntent.putExtra("due_date", dueDate);

                if (isEdit) {
                    resultIntent.putExtra("position", intent.getIntExtra("position", -1));
                }

                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();

        new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) -> {
            dueCalendar.set(Calendar.YEAR, year);
            dueCalendar.set(Calendar.MONTH, month);
            dueCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (TimePicker timeView, int hourOfDay, int minute) -> {
                dueCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                dueCalendar.set(Calendar.MINUTE, minute);

                String formatted = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        .format(dueCalendar.getTime());
                editTextDueDate.setText(formatted);

            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();

        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }
}
