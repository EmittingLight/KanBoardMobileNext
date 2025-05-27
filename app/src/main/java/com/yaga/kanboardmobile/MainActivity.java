package com.yaga.kanboardmobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "KanBoardLog";

    private RecyclerView recyclerView;
    private TicketAdapter adapter;
    private TicketDatabaseHelper dbHelper;

    private Ticket recentlyDeleted;
    private int recentlyDeletedPosition;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    private final ActivityResultLauncher<Intent> addTicketLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String title = data.getStringExtra("title");
                    String description = data.getStringExtra("description");
                    String status = data.getStringExtra("status");
                    String createdAt = data.getStringExtra("created_at");
                    String dueDate = data.getStringExtra("due_date");

                    dbHelper.insertTicket(title, description, status, createdAt, dueDate);
                    refreshList();
                }
            });

    private final ActivityResultLauncher<Intent> editTicketLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    int position = data.getIntExtra("position", -1);
                    if (position != -1) {
                        Ticket ticket = adapter.getItem(position);
                        ticket.setTitle(data.getStringExtra("title"));
                        ticket.setDescription(data.getStringExtra("description"));
                        ticket.setStatus(data.getStringExtra("status"));
                        ticket.setCreatedAt(data.getStringExtra("created_at"));
                        ticket.setDueDate(data.getStringExtra("due_date"));

                        dbHelper.updateTicket(ticket);
                        refreshList();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new TicketDatabaseHelper(this);
        dbHelper.getWritableDatabase();

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewTickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TicketAdapter(new ArrayList<>(), dbHelper, this::updateStats);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            Ticket ticket = adapter.getItem(position);
            Intent intent = new Intent(this, AddTicketActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("title", ticket.getTitle());
            intent.putExtra("description", ticket.getDescription());
            intent.putExtra("status", ticket.getStatus());
            intent.putExtra("created_at", ticket.getCreatedAt());
            intent.putExtra("due_date", ticket.getDueDate());
            intent.putExtra("position", position);
            editTicketLauncher.launch(intent);
        });

        // Свайп
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                recentlyDeletedPosition = viewHolder.getAdapterPosition();
                recentlyDeleted = adapter.getItem(recentlyDeletedPosition);
                adapter.removeItem(recentlyDeletedPosition);

                Snackbar.make(recyclerView, "Задача удалена", Snackbar.LENGTH_LONG)
                        .setAction("ОТМЕНИТЬ", v -> {
                            adapter.restoreItem(recentlyDeleted, recentlyDeletedPosition);
                            recyclerView.scrollToPosition(recentlyDeletedPosition);
                            updateStats();
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override public void onDismissed(Snackbar transientBar, int event) {
                                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                    dbHelper.deleteTicket(recentlyDeleted);
                                    updateStats();
                                }
                            }
                        })
                        .show();
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);

        // Спиннер
        Spinner spinner = findViewById(R.id.spinnerStatusFilter);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Все", "К выполнению", "В процессе", "Готово"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshList();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                refreshList();
            }
        });

        // 🔁 Таймер автообновления
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshList();
                refreshHandler.postDelayed(this, 30_000); // каждые 30 сек
            }
        };
        refreshHandler.post(refreshRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            addTicketLauncher.launch(new Intent(this, AddTicketActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshList() {
        Spinner spinner = findViewById(R.id.spinnerStatusFilter);
        String selected = spinner.getSelectedItem().toString();

        List<Ticket> all = dbHelper.getAllTickets();
        List<Ticket> filtered = new ArrayList<>();
        for (Ticket ticket : all) {
            if ("Все".equals(selected) || ticket.getStatus().equals(selected)) {
                filtered.add(ticket);
            }
        }

        adapter.updateList(filtered);
        updateStats();
    }

    private void updateStats() {
        List<Ticket> all = dbHelper.getAllTickets();
        int total = all.size();
        int todo = 0, inProgress = 0, done = 0;

        for (Ticket t : all) {
            switch (t.getStatus()) {
                case "К выполнению": todo++; break;
                case "В процессе": inProgress++; break;
                case "Готово": done++; break;
            }
        }

        TextView statsText = findViewById(R.id.textStats);
        statsText.setText("Всего: " + total +
                " | К выполнению: " + todo +
                " | В процессе: " + inProgress +
                " | Готово: " + done);
    }
}
