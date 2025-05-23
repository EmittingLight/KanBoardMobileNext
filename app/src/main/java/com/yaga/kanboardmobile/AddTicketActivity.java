package com.yaga.kanboardmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddTicketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ticket);

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> {
            String title = ((EditText) findViewById(R.id.editTextTitle)).getText().toString().trim();
            String description = ((EditText) findViewById(R.id.editTextDescription)).getText().toString().trim();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("description", description);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}

