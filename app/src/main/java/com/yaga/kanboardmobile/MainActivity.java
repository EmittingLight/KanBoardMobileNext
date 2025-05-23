package com.yaga.kanboardmobile;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewTickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Временные тестовые данные
        ticketList = new ArrayList<>();
        ticketList.add(new Ticket("Сделать дизайн", "Создать макет доски", "To Do"));
        ticketList.add(new Ticket("Написать код", "Сделать адаптер и RecyclerView", "In Progress"));
        ticketList.add(new Ticket("Протестировать", "Запустить и проверить", "Done"));

        adapter = new TicketAdapter(ticketList);
        recyclerView.setAdapter(adapter);
    }
}

