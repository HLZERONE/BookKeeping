package com.example.bookkeeping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookkeeping.fragment.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    boolean isExpense;
    private final String TAG = "AddActivity";

    EditText addAmount;
    EditText addComment;
    GridView gridView;
    FloatingActionButton addFab;
    Button datePickerButton;

    List<Category> categoryList;
    CategoryAdapter categoryAdapter;
    JournalDatabase journalDatabase;

    int backPosition = -1;
    String categoryName = "Unknown/Others"; //default category
    Date dateTime = new Date();
    int year = Calendar.getInstance().get(Calendar.YEAR);
    int month = Calendar.getInstance().get(Calendar.MONTH);
    int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        //Add back button in Action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); //hide title
        actionBar.setDisplayHomeAsUpEnabled(true); //set up back button

        //MATCH all the view
        addAmount = findViewById(R.id.addAmount);
        addComment = findViewById(R.id.addComment);
        gridView = findViewById(R.id.gridView);
        addFab = findViewById(R.id.addFab);
        datePickerButton = findViewById(R.id.datePickerButton);

        //Assign adapter into gridView
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList);
        gridView.setAdapter(categoryAdapter);

        //Set Background Color and Adding categories into list
        journalDatabase = new JournalDatabase(this);
        isExpense = getIntent().getBooleanExtra(HomeFragment.isExpense, true);
        if(isExpense){
            findViewById(R.id.addLayout).setBackgroundResource(R.color.yellowBackground);
            categoryList.addAll(journalDatabase.queryExpenseCategory());
        }else{
            findViewById(R.id.addLayout).setBackgroundResource(R.color.teaGreenBackground);
            categoryList.addAll(journalDatabase.querySaveCategory());
        }
        categoryAdapter.notifyDataSetChanged();


        //GridView ItemOnClick, CategoryName: which category user clicks on
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //remove the color of previous selected item and update the backPosition
                if(backPosition != -1){
                    View lastView = adapterView.getChildAt(backPosition);
                    lastView.setSelected(false);
                    lastView.setBackgroundResource(R.color.moonWhite);
                }

                view.setBackgroundResource(R.color.white);
                categoryName = ((TextView) view.findViewById(R.id.categoryItemName)).getText().toString(); //categoryName
                backPosition = i;
            }
        });

        //datePickerButton OnClick, pick a date
        //DEFAULT: today
        String selectedDateString = year + "-" + (month+1) + "-" + day;
        datePickerButton.setText("DATE:" + selectedDateString);

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDiaglog();
            }
        });

        //addFb: Floating Action Button OnClick, Save a new journal
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });

    }

    //Back Button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home: //when click on back button
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Show Date Picker Dialog
    public void showDatePickerDiaglog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, year, month, day);
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
        year = y;
        month = m;
        day = d;
        SetDateButton();
    }

    //Update the text on datePickerButton and dateTime variable
    public void SetDateButton(){
        String selectedDateString = year + "-" + (month+1) + "-" + day;
        datePickerButton.setText("DATE:" + selectedDateString);
        try {
            dateTime = JournalDatabase.sqlDateFormat.parse(selectedDateString  + " 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //Add a new Expense/Save
    public void add(){
        String addAmountString = addAmount.getText().toString();
        //Check Amount: Empty, equal to 0
        if(addAmountString == null || addAmountString.length() <= 0){
            Toast.makeText(this, "Please enter your amount", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount = Double.valueOf(addAmountString); //Amount
        if(amount == 0){
            Toast.makeText(this, "Amount could not be 0", Toast.LENGTH_SHORT).show();
            return;
        }
        String comment = addComment.getText().toString(); //Comment

        //Adding new Journal to DataBase
        if(isExpense){
            journalDatabase.insertJournalData(0, amount, comment, categoryName, dateTime);
        }
        else{
            journalDatabase.insertJournalData(amount, 0, comment, categoryName, dateTime);
        }
        finish();
    }
}