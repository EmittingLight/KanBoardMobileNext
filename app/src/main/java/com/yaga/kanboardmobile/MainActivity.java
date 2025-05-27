package com.yaga.kanboardmobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
                if (result.getResultCode() == RESULT_OK) {
                    refreshList();
                    updateStats(); // 💥 обновляем статистику сразу после добавления
                }
            });

    private final ActivityResultLauncher<Intent> editTicketLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    int id = data.getIntExtra("id", -1);
                    if (id != -1) {
                        String title = data.getStringExtra("title");
                        String description = data.getStringExtra("description");
                        String status = data.getStringExtra("status");
                        String createdAt = data.getStringExtra("created_at");
                        String dueDate = data.getStringExtra("due_date");

                        Ticket updated = new Ticket(id, title, description, status, createdAt, dueDate);
                        dbHelper.updateTicket(updated);

                        refreshList();
                        recyclerView.scrollToPosition(0);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(0); // 👈 это обязательно

        dbHelper = new TicketDatabaseHelper(this);
        dbHelper.getWritableDatabase();

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewTickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TicketAdapter(new ArrayList<>(), dbHelper, this::updateStats);
        recyclerView.setAdapter(adapter);
        setupAdapterListeners();

        updateStats();

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

        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                dbHelper.updateOverdueStatuses();
                refreshList();
                refreshHandler.postDelayed(this, 30_000);
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

        adapter = new TicketAdapter(filtered, dbHelper, this::updateStats);
        recyclerView.setAdapter(adapter);
        setupAdapterListeners();

        updateStats(); // 💥 всегда обновляем после загрузки списка
    }


    private void updateStats() {
        List<Ticket> all = dbHelper.getAllTicketsRaw();

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

    private void setupAdapterListeners() {
        adapter.setOnItemClickListener(position -> {
            Ticket ticket = adapter.getItem(position);
            Intent intent = new Intent(this, AddTicketActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("id", ticket.getId());
            intent.putExtra("title", ticket.getTitle());
            intent.putExtra("description", ticket.getDescription());
            intent.putExtra("status", ticket.getStatus());
            intent.putExtra("created_at", ticket.getCreatedAt());
            intent.putExtra("due_date", ticket.getDueDate());
            intent.putExtra("position", position);
            editTicketLauncher.launch(intent);
        });

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
    }
}
