package com.codepath.nytimessearch;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Sonam on 11/1/2016.
 */

public class FilterDialogFragment extends DialogFragment implements DatePickerFragment.DatePickerListener, View.OnClickListener {

    EditText etBeginDate;
    Spinner spSortOrder;
    CheckBox checkArts;
    CheckBox checkFashion;
    CheckBox checkSports;
    Button btnSave;
    String beginDate;
    ArrayList<String> sortArray;

    public interface FilterDialogListener {
        void onFinishFilterDialog(String date, String sort, ArrayList<String> newsDesk);
    }

    public FilterDialogFragment() {

    }


    public static FilterDialogFragment newInstance(String s) {
        FilterDialogFragment frag = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", s);
        frag.setArguments(args);
        return frag;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etBeginDate = (EditText) view.findViewById(R.id.etBeginDate);
        spSortOrder = (Spinner) view.findViewById(R.id.spSortOrder);

        sortArray = new ArrayList<>();
        setupNewsDesk(view);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_order_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spSortOrder.setAdapter(adapter);

        etBeginDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }

        });

        btnSave = (Button) view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


    }

    private void showDatePickerDialog() {
        FragmentManager fm = getFragmentManager();
        DatePickerFragment newFragment = new DatePickerFragment();
        // SETS the target fragment for use later when sending results
        newFragment.setTargetFragment(FilterDialogFragment.this, 300);
        newFragment.show(fm, "datepicker");
    }

    @Override
    public void onFinishDatePicker(int yy, int mm, int dd) {
        mm = mm + 1;
        String date = dd + "/" + mm + "/" + yy;
        String formattedDay = (String.valueOf(dd));
        String formattedMonth = (String.valueOf(mm));
        if(dd<10){
            formattedDay = "0"+dd;
        }
        if(mm<10){
            formattedMonth = "0"+mm;
        }
        beginDate = yy+""+formattedMonth+""+formattedDay;
        etBeginDate.setText(date);
    }

    public void setupNewsDesk(View view) {
        checkArts = (CheckBox) view.findViewById(R.id.cbArts);
        checkFashion = (CheckBox) view.findViewById(R.id.cbFashion);
        checkSports = (CheckBox) view.findViewById(R.id.cbSports);
        checkArts.setOnCheckedChangeListener(new checkBoxChangeClicker());
        checkFashion.setOnCheckedChangeListener(new checkBoxChangeClicker());
        checkSports.setOnCheckedChangeListener(new checkBoxChangeClicker());
    }

    class checkBoxChangeClicker implements CheckBox.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton view,
                                     boolean isChecked) {
            switch(view.getId()) {
                case R.id.cbArts:
                    if (isChecked) {
                        sortArray.add(view.getText().toString());
                    } else {
                        sortArray.remove(view.getText().toString());
                    }
                    break;
                case R.id.cbFashion:
                    if (isChecked) {
                        sortArray.add(view.getText().toString());
                    } else {
                        sortArray.remove(view.getText().toString());
                    }
                    break;
                case R.id.cbSports:
                    if (isChecked) {
                        sortArray.add(view.getText().toString());
                    } else {
                        sortArray.remove(view.getText().toString());
                    }
                    break;
            }

        }
    }


    @Override
    public void onClick(View v) {
        String sortOrderValue = spSortOrder.getSelectedItem().toString();
        ArrayList<String> newsDeskValue = sortArray;
        FilterDialogListener activity = (FilterDialogListener) getActivity();
        activity.onFinishFilterDialog(beginDate,sortOrderValue,newsDeskValue);
        dismiss();
    }
}

