package com.example.tonto.zees;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.tonto.zees.receivers.AlarmReceiver;
import com.example.tonto.zees.database.Alarm;
import com.example.tonto.zees.adapters.AlarmAdapter;
import com.example.tonto.zees.database.ZeesDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hoang on 4/24/2017.
 */

public class AlarmActivity extends AppCompatActivity {
    public static ZeesDatabase zeesDatabase;
    public static List<Alarm> alarmList;
    private ImageButton addAlarmButton;
    public static int alarmNo;
    public static AlarmManager alarmManager;
    public static AlarmAdapter alarmAdapter;
    private ListView listView;
    private ImageView background;
    private Window window;
    private Toolbar toolbar;
    public static TextView reserved;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.darkRain));
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        toolbar = (Toolbar) findViewById(R.id.toolbar_alarm);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor((getResources().getColor(R.color.lightRain)));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        background = (ImageView) findViewById(R.id.alarm_background);
        background.setImageResource(R.drawable.background_rain);

        reserved = (TextView) findViewById(R.id.reserved_alarm);

        zeesDatabase = new ZeesDatabase(this);
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmList = zeesDatabase.loadAllAlarm();
        if (alarmList.isEmpty()) {
            reserved.setVisibility(View.VISIBLE);
        } else reserved.setVisibility(View.GONE);
        listView = (ListView) findViewById(R.id.alarm_list);
        alarmAdapter = new AlarmAdapter(this, R.layout.alarm_element, alarmList);
        listView.setAdapter(alarmAdapter);
        addAlarmButton = (ImageButton) findViewById(R.id.add_alarm);
        addAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AlarmActivity.this, R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        long curTime = System.currentTimeMillis();
                        long delayms = TimeUnit.HOURS.toMillis(hourOfDay) + TimeUnit.MINUTES.toMillis(minute);
                        System.out.println("Hour: " + hourOfDay + " Minute: " + minute + " Millis: " + delayms);

                        System.out.println("Time: " + curTime + delayms);
                        Calendar calendar = Calendar.getInstance();

                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);

                        if (calendar.getTimeInMillis() < curTime) {
                            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                        }

                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

                        System.out.println(formatter.format(calendar.getTime()));

                        Random randomId = new Random();
                        int id = randomId.nextInt(100);
                        do {
                            id = randomId.nextInt(100);
                        } while (zeesDatabase.checkUniqueId(id) == false);

                        if (zeesDatabase.checkUniqueDate(calendar.getTimeInMillis()) == false) {
                            Toast.makeText(AlarmActivity.this, "This alarm has already been set", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Alarm alarm = new Alarm("Alarm " + alarmNo, delayms + "", calendar.getTimeInMillis() + "", "Test", "true", id + "");
                        alarmNo++;
                        alarmList.add(alarm);
                        reserved.setVisibility(View.GONE);

                        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                        intent.putExtra("Alarm Info", alarm);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), id, intent, 0);

                        SQLiteDatabase db = zeesDatabase.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("name", alarm.getName());
                        values.put("delay", alarm.getDelay());
                        values.put("date", alarm.getDate());
                        values.put("tone", alarm.getTone());
                        values.put("enabled", alarm.getEnabled());
                        values.put("pending_id", alarm.getPendingId());

                        db.insert("alarm", null, values);
                        db.close();

                        alarmAdapter.notifyDataSetChanged();


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        } else
                            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                        long timeLeft = calendar.getTimeInMillis() - curTime;
                        int hours = (int) ((timeLeft / (1000 * 60 * 60)) % 24);
                        int minutes = (int) ((timeLeft / (1000 * 60)) % 60);
                        if (hours != 0 && minutes != 0) {
                            if (hours != 1 && minutes != 1) {
                                Toast.makeText(AlarmActivity.this, "Alarm set for " + hours + " hours and " + minutes + " minutes from now.", Toast.LENGTH_SHORT).show();
                            }
                            if (hours == 1 && minutes != 1) {
                                Toast.makeText(AlarmActivity.this, "Alarm set for " + hours + " hour and " + minutes + " minutes from now.", Toast.LENGTH_SHORT).show();
                            }
                            if (hours == 1 && minutes == 1) {
                                Toast.makeText(AlarmActivity.this, "Alarm set for " + hours + " hour and " + minutes + " minute from now.", Toast.LENGTH_SHORT).show();
                            }
                            if (hours != 1 && minutes == 1) {
                                Toast.makeText(AlarmActivity.this, "Alarm set for " + hours + " hours and " + minutes + " minute from now.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (hours != 0 && minutes == 0) {
                            if (hours != 1)
                                Toast.makeText(AlarmActivity.this, "Alarm set for " + hours + " hours from now.", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(AlarmActivity.this, "Alarm set for " + hours + " hour from now.", Toast.LENGTH_SHORT).show();
                        }
                        if (hours == 0 && minutes != 0) {
                            if (minutes != 1)
                                Toast.makeText(AlarmActivity.this, "Alarm set for " + minutes + " minutes from now.", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(AlarmActivity.this, "Alarm set for " + minutes + " minute from now.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                        , 0, 0, true);
                timePickerDialog.show();
            }
        });
        addAlarmButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zeesDatabase.close();
    }
}
