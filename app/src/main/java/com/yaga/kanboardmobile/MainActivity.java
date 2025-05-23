package com.yaga.kanboardmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

    private RecyclerView recyclerView;
    private TicketAdapter adapter;
    private List<Ticket> ticketList;

    private final ActivityResultLauncher<Intent> addTicketLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String title = data.getStringExtra("title");
                        String description = data.getStringExtra("description");
                        Ticket newTicket = new Ticket(title, description, "To Do");
                        ticketList.add(newTicket);
                        adapter.notifyItemInserted(ticketList.size() - 1);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewTickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ticketList = new ArrayList<>();
        ticketList.add(new Ticket("Сделать дизайн", "Создать макет доски", "To Do"));
        ticketList.add(new Ticket("Написать код", "Сделать адаптер и RecyclerView", "In Progress"));
        ticketList.add(new Ticket("Протестировать", "Запустить и проверить", "Done"));

        adapter = new TicketAdapter(ticketList);
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
        List<Ticket> filtered = new ArrayList<>();
        if (status.equals("Все")) {
            filtered.addAll(ticketList);
        } else {
            for (Ticket ticket : ticketList) {
                if (status.equals("К выполнению") && ticket.getStatus().equals("To Do")) {
                    filtered.add(ticket);
                } else if (status.equals("В процессе") && ticket.getStatus().equals("In Progress")) {
                    filtered.add(ticket);
                } else if (status.equals("Готово") && ticket.getStatus().equals("Done")) {
                    filtered.add(ticket);
                }
            }
        }
        adapter.updateList(filtered);
    }
}
