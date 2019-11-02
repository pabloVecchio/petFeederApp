package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

//http://www.zoftino.com/android-datepicker-example

public class DatePickerFragment extends DialogFragment/* implements DatePickerDialog.OnDateSetListener*/{

    private TextView tvTimePicker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //Use the current time as the default values for the time picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        tvTimePicker = (TextView) getActivity().findViewById(R.id.tv_DatePicker);

        //Create and return a new instance of TimePickerDialog
        return new DatePickerDialog(getActivity(), dateSetListener, year, month, day);
    }

    private DatePickerDialog.OnDateSetListener dateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    Toast.makeText(getActivity(), "La fecha seleccionada es  " + view.getDayOfMonth() +
                            " / " + (view.getMonth()+1) +
                            " / " + view.getYear(), Toast.LENGTH_SHORT).show();

                    tvTimePicker.setText(view.getDayOfMonth() + " / " + (view.getMonth()+1) + " / " + view.getYear());
                }
            };

}
