package com.example.bookkeeping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookkeeping.fragment.HomeFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    boolean isExpense;
    boolean NewJournal;
    private final String TAG = "AddActivity";

    EditText addAmount;
    EditText addComment;
    GridView gridView;
    FloatingActionButton addFab;
    Button datePickerButton;
    ChipGroup CategoryIconGroup;
    TextView newCategoryName;
    ImageView addCategoryButton;

    List<Category> categoryList;
    CategoryAdapter categoryAdapter;
    JournalDatabase journalDatabase;

    int backPosition = -1;
    int id = -1;
    String categoryName = "Unknown/Others"; //default category
    Date dateTime = new Date();
    int year;
    int month;
    int day;


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
        addCategoryButton = findViewById(R.id.addCategoryButton);

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

        //DELETE category item if long click
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String categoryName = categoryList.get(i).getCategoryName();
                if(categoryName != null){
                    new AlertDialog.Builder(AddActivity.this)
                            .setIcon(R.drawable.warning)
                            .setTitle("Are you sure?")
                            .setMessage("Do you definitely want to delete the category " + categoryName + " ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int place) {
                                    if(isExpense){
                                        journalDatabase.deleteExpenseCategoriesData(categoryName);
                                    }else{
                                        journalDatabase.deleteSaveCategoriesData(categoryName);
                                    }
                                    categoryList.remove(i);
                                    categoryAdapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                return false;
            }
        });

        //Check if create or edit journal
        NewJournal = getIntent().getBooleanExtra(HomeFragment.NewJournal, true);
        if(!NewJournal){
            this.id = getIntent().getIntExtra("id", -1);
            categoryName = getIntent().getStringExtra("category");
            dateTime = (Date) getIntent().getSerializableExtra("date");
            addComment.setText(getIntent().getStringExtra("comment"));
            addAmount.setText(String.valueOf(getIntent().getDoubleExtra("amount", 0)));
            addFab.setImageResource(R.drawable.ic_check);
        }

        //Find year, month, day by dateTime(Date)
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

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

        //Place cursor at the end of EditText
        addAmount.setSelection(addAmount.getText().length());
        addComment.setSelection(addComment.getText().length());

        //AddCategoryButton Listener
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addNewCategory();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                final AlertDialog dialog = builder.create();

                View dialogView = View.inflate(AddActivity.this, R.layout.add_category_view, null);
                CategoryIconGroup = dialogView.findViewById(R.id.CategoryIconGroup);
                newCategoryName = dialogView.findViewById(R.id.newCategoryName);

                dialog.setTitle("Add A New Category");
                dialog.setView(dialogView);
                dialog.show();
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
    private void add(){
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
        if(NewJournal){
            if(isExpense){
                journalDatabase.insertJournalData(0, amount, comment, categoryName, dateTime);
            }
            else{
                journalDatabase.insertJournalData(amount, 0, comment, categoryName, dateTime);
            }
        }
        else{
            if(id == -1){
                Toast.makeText(this, "Fail to update", Toast.LENGTH_SHORT).show();
            }
            else if(isExpense){
                journalDatabase.updateJournalData(id, 0, amount, comment, categoryName, dateTime);
            }
            else{
                journalDatabase.updateJournalData(id, amount, 0, comment, categoryName, dateTime);
            }
        }
        finish();
    }

    //Add New Category
    private void addNewCategory(){
        String categoryName = newCategoryName.getText().toString();
        if(categoryName == null || categoryName.equals("")){
            Toast.makeText(AddActivity.this, "Fail to add. Category name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        int chipId = CategoryIconGroup.getCheckedChipId();
        int imageId = -1;
        //Modify if adding more chip icons!
        switch(chipId){
            case R.id.icon1:
                imageId = R.drawable.shopping_1;
                break;
            case R.id.icon2:
                imageId = R.drawable.food_and_drinks_2;
                break;
            case R.id.icon3:
                imageId = R.drawable.housing_and_utility_3;
                break;
            case R.id.icon4:
                imageId = R.drawable.transportation_4;
                break;
            case R.id.icon5:
                imageId = R.drawable.baby_5;
                break;
            case R.id.icon6:
                imageId = R.drawable.salary_6;
                break;
            case R.id.icon7:
                imageId = R.drawable.investment_7;
                break;
            case R.id.icon8:
                imageId = R.drawable.sold_items_8;
                break;
            case R.id.icon9:
                imageId = R.drawable.building_9;
                break;
            case R.id.icon10:
                imageId = R.drawable.drugs_10;
                break;
            case R.id.icon11:
                imageId = R.drawable.gift_11;
                break;
            case R.id.icon12:
                imageId = R.drawable.insurance_12;
                break;
            case R.id.icon13:
                imageId = R.drawable.party_popper_13;
                break;
            case R.id.icon14:
                imageId = R.drawable.pet_house_14;
                break;
            default:
                imageId = R.drawable.others_0;
        }
        //Add new category to database
        if(isExpense){
            journalDatabase.insertExpenseCategoriesData(categoryName, imageId);
        }else{
            journalDatabase.insertSaveCategoriesData(categoryName, imageId);
        }
        categoryList.add(new Category(categoryName, imageId));
        categoryAdapter.notifyDataSetChanged();
    }
}