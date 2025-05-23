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
}
