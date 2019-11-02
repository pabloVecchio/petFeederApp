package com.example.myapplication;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.app.DialogFragment;
import android.app.Dialog;
import java.util.Calendar;
import android.widget.TimePicker;
import android.widget.Toast;

//https://android--examples.blogspot.com/2015/04/timepickerdialog-in-android.html
/**
 * A simple {@link Fragment} subclass.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //Use the current time as the default values for the time picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        //Create and return a new instance of TimePickerDialog
        return new TimePickerDialog(getActivity(),this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    //onTimeSet() callback method
    public void onTimeSet(TimePicker view, int hourOfDay, int minute){

        Toast.makeText(getActivity(), "La hora seleccionada es  " + String.format( "%02d",  hourOfDay )+ ":" + String.format( "%02d",minute) , Toast.LENGTH_SHORT).show();

        //Do something with the user chosen time
        //Get reference of host activity (XML Layout File) TextView widget
        TextView tv = (TextView) getActivity().findViewById(R.id.tv_TimePicker);
        //Set a message for user
        //tv.setText("Your chosen time is...\n\n");
        //Display the user changed time on TextView
        tv.setText(/*tv.getText()+*/ String.format( "%02d",  hourOfDay ) + ":" + String.format( "%02d",minute));
    }
}
