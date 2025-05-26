package com.yaga.kanboardmobile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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

        // 💡 Сначала инициализируем элементы
        EditText editTextTitle = findViewById(R.id.editTextTitle);
        EditText editTextDescription = findViewById(R.id.editTextDescription);
        Button buttonSave = findViewById(R.id.buttonSave);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_add);

        // 🔁 Только потом используем
        Intent intent = getIntent();
        boolean isEdit = intent.getBooleanExtra("isEdit", false);

        if (isEdit) {
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            editTextTitle.setText(title);
            editTextDescription.setText(description);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        buttonSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();

            if (!title.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("title", title);
                resultIntent.putExtra("description", description);

                if (getIntent().getBooleanExtra("isEdit", false)) {
                    resultIntent.putExtra("position", getIntent().getIntExtra("position", -1));
                }

                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
