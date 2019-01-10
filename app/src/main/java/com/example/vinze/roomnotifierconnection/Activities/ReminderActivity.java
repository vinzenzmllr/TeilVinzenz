package com.example.vinze.roomnotifierconnection.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.vinze.roomnotifierconnection.Entities.Reminder;
import com.example.vinze.roomnotifierconnection.NotificationManager.AlertReceiver;
import com.example.vinze.roomnotifierconnection.R;
import com.example.vinze.roomnotifierconnection.ReminderAdapter;
import com.example.vinze.roomnotifierconnection.ViewModels.ReminderViewModel;

import java.util.Calendar;
import java.util.List;

public class ReminderActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int ADD_REMINDER_REQUEST = 1;
    public static final int EDIT_REMINDER_REQUEST = 2;

    public AlarmManager alarmManager;
    public long id;

    private ReminderViewModel reminderViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReminderActivity.this, AddEditReminderActivity.class);
                startActivityForResult(intent, ADD_REMINDER_REQUEST);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final ReminderAdapter adapter = new ReminderAdapter();
        recyclerView.setAdapter(adapter);

        reminderViewModel = ViewModelProviders.of(this).get(ReminderViewModel.class);
        reminderViewModel.getAllReminders().observe(this, new Observer<List<Reminder>>() {
            @Override
            public void onChanged(@Nullable List<Reminder> reminders) {
                adapter.submitList(reminders);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                reminderViewModel.delete(adapter.getReminderat(viewHolder.getAdapterPosition()));

                Toast.makeText(ReminderActivity.this, "Erinnerung gelöscht", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

       /* adapter.setOnItemClickListener(new ReminderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Reminder reminder) {
                Intent intent = new Intent(ReminderActivity.this, AddEditReminderActivity.class);
                intent.putExtra(AddEditReminderActivity.EXTRA_ID, reminder.getId());
                intent.putExtra(AddEditReminderActivity.EXTRA_MEDICATIONNAME, reminder.getMedicationName());
                intent.putExtra(AddEditReminderActivity.EXTRA_MORNINGTIME, reminder.getMorningTime());
                intent.putExtra(AddEditReminderActivity.EXTRA_NOONTIME, reminder.getNoonTime());
                intent.putExtra(AddEditReminderActivity.EXTRA_EVENINGTIME, reminder.getEveningTime());
                startActivityForResult(intent, EDIT_REMINDER_REQUEST);
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_REMINDER_REQUEST && resultCode == RESULT_OK) {

            String medicationname = data.getStringExtra(AddEditReminderActivity.EXTRA_MEDICATIONNAME);
            String morningTime = data.getStringExtra(AddEditReminderActivity.EXTRA_MORNINGTIME);
            String noonTime = data.getStringExtra(AddEditReminderActivity.EXTRA_NOONTIME);
            String eveningTime = data.getStringExtra(AddEditReminderActivity.EXTRA_EVENINGTIME);

            Reminder reminder = new Reminder(medicationname, morningTime,noonTime,eveningTime,false);

            reminderViewModel.insert(reminder);
            id = reminder.getId();
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

            if(!(" ").equals(morningTime))
            {
                Calendar cMorning = Calendar.getInstance();
                cMorning.setTimeInMillis(System.currentTimeMillis());

                int morningHour = Integer.parseInt(morningTime.substring(0,2));
                int morningMinute = Integer.parseInt(morningTime.substring(3,5));

                cMorning.set(Calendar.HOUR_OF_DAY, morningHour);
                cMorning.set(Calendar.MINUTE, morningMinute);
                cMorning.set(Calendar.SECOND, 0);
                cMorning.add(Calendar.HOUR,checkHour(morningHour));

                start(cMorning, 1000+(int)id, medicationname);
            }

            if(!(" ").equals(noonTime))
            {
                Calendar cNoon = Calendar.getInstance();
                cNoon.setTimeInMillis(System.currentTimeMillis());

                int noonHour = Integer.parseInt(noonTime.substring(0,2));
                int noonMinute = Integer.parseInt(noonTime.substring(3,5));

                cNoon.set(Calendar.HOUR_OF_DAY, noonHour);
                cNoon.set(Calendar.MINUTE, noonMinute);
                cNoon.set(Calendar.SECOND, 0);
                cNoon.add(Calendar.HOUR,checkHour(noonHour));

                start(cNoon, 2000+(int)id, medicationname);
            }

            if(!(" ").equals(eveningTime))
            {
                Calendar cEvening = Calendar.getInstance();
                cEvening.setTimeInMillis(System.currentTimeMillis());

                int eveningHour = Integer.parseInt(eveningTime.substring(0,2));
                int eveningMinute = Integer.parseInt(eveningTime.substring(3,5));

                cEvening.set(Calendar.HOUR_OF_DAY, eveningHour);
                cEvening.set(Calendar.MINUTE, eveningMinute);
                cEvening.set(Calendar.SECOND, 0);
                cEvening.add(Calendar.HOUR,checkHour(eveningHour));

                start(cEvening, 3000+(int)id, medicationname);
            }

            Toast.makeText(this, "Erinnerung gespeichert", Toast.LENGTH_SHORT).show();


        } else if (requestCode == EDIT_REMINDER_REQUEST && resultCode == RESULT_OK) {
            id = data.getIntExtra(AddEditReminderActivity.EXTRA_ID, -1);

            if (id == -1) {
                Toast.makeText(this, "Erinnerung konnte nicht geändert werden!", Toast.LENGTH_SHORT).show();
                return;
            }

            String medicationName = data.getStringExtra(AddEditReminderActivity.EXTRA_MEDICATIONNAME);
            String morningTime = data.getStringExtra(AddEditReminderActivity.EXTRA_MORNINGTIME);
            String noonTime = data.getStringExtra(AddEditReminderActivity.EXTRA_NOONTIME);
            String eveningTime = data.getStringExtra(AddEditReminderActivity.EXTRA_EVENINGTIME);

            Reminder reminder = new Reminder(medicationName, morningTime,noonTime,eveningTime,false);
            reminder.setId((int)id);
            reminderViewModel.update(reminder);

            Toast.makeText(this, "Erinnerung geändert", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erinnerung nicht gespeichert!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_notes:
                reminderViewModel.deleteAllReminders();
                //TODO alarmManager.cancel();
                Toast.makeText(this, "Alle Erinnerungen gelöscht", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void start(Calendar calendar, int setCode, String medicationName) {

        alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("medicationName", medicationName);
        intent.putExtra("SETCODE", setCode);
        intent.putExtra("TIME", calendar.getTimeInMillis());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, setCode, intent, 0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            System.out.println(System.currentTimeMillis());
            System.out.println(calendar.getTimeInMillis());
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
        else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
        Toast.makeText(this, "Alarm Set!" + " " + id, Toast.LENGTH_SHORT).show();
    }

    public int checkHour(int hour){
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.getTime().getHours();

        if(hour < currentHour){
            return 24-(currentHour-hour);
        }
        else
        {
            return 0;
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    Intent switchToReminder = null;
   /* public void onClickSwitch(View view)
    {
        switchToReminder = switchToReminder == null? new Intent(this, ReminderActivity.class): switchToReminder;
        startActivity(switchToReminder);
        finish();
    }*/


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_HomePage) {
            switchToReminder = switchToReminder == null? new Intent(this, ReminderActivity.class): switchToReminder;
            startActivity(switchToReminder);
            finish();

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
