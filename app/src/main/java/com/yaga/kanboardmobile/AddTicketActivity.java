package com.yaga.kanboardmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class AddTicketActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 💥 Жестко возвращаем статус-бар
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        setContentView(R.layout.activity_add_ticket);

        // 💡 Инициализируем элементы
        EditText editTextTitle = findViewById(R.id.editTextTitle);
        EditText editTextDescription = findViewById(R.id.editTextDescription);
        Spinner spinnerStatus = findViewById(R.id.spinnerStatus);
        Button buttonSave = findViewById(R.id.buttonSave);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_add);

        // 🔧 Настраиваем адаптер для Spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"К выполнению", "В процессе", "Готово"}
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // 🔁 Если редактируем задачу
        Intent intent = getIntent();
        boolean isEdit = intent.getBooleanExtra("isEdit", false);

        if (isEdit) {
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String status = intent.getStringExtra("status");

            editTextTitle.setText(title);
            editTextDescription.setText(description);

            if (status != null) {
                int index = statusAdapter.getPosition(status);
                if (index != -1) {
                    spinnerStatus.setSelection(index);
                }
            }
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // 💾 Сохраняем
        buttonSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String selectedStatus = spinnerStatus.getSelectedItem().toString();

            if (!title.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("title", title);
                resultIntent.putExtra("description", description);
                resultIntent.putExtra("status", selectedStatus);

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
}
