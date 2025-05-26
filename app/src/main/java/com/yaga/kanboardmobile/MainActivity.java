package com.yaga.kanboardmobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "KanBoardLog";

    private RecyclerView recyclerView;
    private TicketAdapter adapter;
    private TicketDatabaseHelper dbHelper;

    private final ActivityResultLauncher<Intent> addTicketLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String title = data.getStringExtra("title");
                        String description = data.getStringExtra("description");

                        Log.d(TAG, "Получена задача от активности: " + title);
                        dbHelper.insertTicket(title, description, "К выполнению");

                        // Обновляем список
                        adapter.updateList(dbHelper.getAllTickets());

                        // Сбрасываем фильтр
                        Spinner spinner = findViewById(R.id.spinnerStatusFilter);
                        spinner.setSelection(0);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new TicketDatabaseHelper(this);
        dbHelper.getWritableDatabase();
        Log.d(TAG, "База данных открыта или создана");

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewTickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TicketAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(position -> {
            Ticket ticket = adapter.getItem(position);
            Intent intent = new Intent(MainActivity.this, AddTicketActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("title", ticket.getTitle());
            intent.putExtra("description", ticket.getDescription());
            intent.putExtra("position", position);
            editTicketLauncher.launch(intent);
        });

        recyclerView.setAdapter(adapter);

        Spinner spinner = findViewById(R.id.spinnerStatusFilter);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Все", "К выполнению", "В процессе", "Готово"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = (String) parent.getItemAtPosition(position);
                filterTickets(selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterTickets("Все");
            }
        });

        // Загружаем изначально
        filterTickets("Все");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(this, AddTicketActivity.class);
            addTicketLauncher.launch(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filterTickets(String status) {
        List<Ticket> allTickets = dbHelper.getAllTickets();
        List<Ticket> filtered = new ArrayList<>();

        for (Ticket ticket : allTickets) {
            if (status.equals("Все") || ticket.getStatus().equals(status)) {
                filtered.add(ticket);
            }
        }

        Log.d(TAG, "Фильтрация: " + status + " | Результат: " + filtered.size());
        adapter.updateList(filtered);
    }

    private final ActivityResultLauncher<Intent> editTicketLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String newTitle = data.getStringExtra("title");
                    String newDescription = data.getStringExtra("description");
                    int position = data.getIntExtra("position", -1);

                    if (position != -1) {
                        Ticket ticket = adapter.getItem(position);
                        ticket.setTitle(newTitle);
                        ticket.setDescription(newDescription);
                        adapter.notifyItemChanged(position);
                    }
                }
            });
}
