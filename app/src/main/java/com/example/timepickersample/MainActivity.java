package com.example.timepickersample;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private LinearLayout timePickerContainer;
    private EditText doseInput;
    private Button generateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        doseInput = findViewById(R.id.dose_input);
        generateButton = findViewById(R.id.generate_button);
        timePickerContainer = findViewById(R.id.time_picker_container);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int doseCount;
                try {
                    doseCount = Integer.parseInt(doseInput.getText().toString());
                } catch (NumberFormatException e) {
                    doseInput.setError("Enter a valid number");
                    return;
                }

                timePickerContainer.removeAllViews(); // Clear previous pickers
                for (int i = 0; i < doseCount; i++) {
                    createAndAddTimePicker(i + 1);
                }
            }
        });
    }


    private void createAndAddTimePicker(int doseNumber) {
        final TextView timeTextView = new TextView(this);
        timeTextView.setText("Dose " + doseNumber + " Time: Not Set");
        timeTextView.setPadding(10, 20, 10, 20);
        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String time = String.format("%02d:%02d", hourOfDay, minute);
                                timeTextView.setText("Dose " + doseNumber + " Time: " + time);

                                // 通知リマインダーを設定
                                setMedicationReminder(hourOfDay, minute, doseNumber);
                            }


                        }, 12, 0, true);
                timePickerDialog.show();
            }
        });

        timePickerContainer.addView(timeTextView);
    }

    private void setMedicationReminder(int hour, int minute, int doseNumber) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, MedicationReminderReceiver.class);
        intent.putExtra("doseTime", String.format("%02d:%02d", hour, minute));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, doseNumber, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);


        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            // ログに出力して設定値を確認
            Log.d("MedicationReminder", "Reminder set for dose " + doseNumber + " at " + hour + ":" + minute);

            // ユーザーに設定完了を知らせるためのToastメッセージ
            Toast.makeText(this, "Reminder set for dose " + doseNumber + " at " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
        } else {
            Log.e("MedicationReminder", "AlarmManager is null");
        }
    }


}