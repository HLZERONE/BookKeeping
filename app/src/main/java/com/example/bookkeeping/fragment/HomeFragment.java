package com.example.bookkeeping.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.bookkeeping.AddActivity;
import com.example.bookkeeping.EndlessRecyclerViewScrollListener;
import com.example.bookkeeping.Journal;
import com.example.bookkeeping.JournalAdapter;
import com.example.bookkeeping.JournalDatabase;
import com.example.bookkeeping.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//18:29
public class HomeFragment extends Fragment {

    private final static String TAG = "HomeFragment";
    private final static int queryLimit = 20; //number of item that each query return
    public final static String isExpense = "isExpense";

    JournalDatabase journalDatabase;
    List<Journal> journals = new ArrayList<>();
    JournalAdapter journalAdapter;
    LinearLayoutManager linearLayoutManager;
    EndlessRecyclerViewScrollListener scrollListener;

    TextView HomeMonth;
    TextView HomeAmount;
    TextView HomeExpenseAmount;
    TextView HomeSaveAmount;
    Button ExpenseButton;
    Button SaveButton;
    RecyclerView recyclerView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //assign all the view
        HomeMonth = view.findViewById(R.id.HomeMonth);
        HomeAmount = view.findViewById(R.id.HomeAmount);
        HomeExpenseAmount = view.findViewById(R.id.HomeExpenseAmount);
        HomeSaveAmount = view.findViewById(R.id.HomeSaveAmount);
        ExpenseButton = view.findViewById(R.id.ExpenseButton);
        SaveButton = view.findViewById(R.id.SaveButton);
        recyclerView = view.findViewById(R.id.recycleView);

        //build database
        journalDatabase = new JournalDatabase(getContext());


        /*
        journalDatabase.redoTable();
        try {
            journalDatabase.insertJournalData(2000.00, 0, "testAmount", "Salary", JournalDatabase.sqlDateFormat.parse("2021-06-01 12:03:01"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        journalDatabase.insertJournalData(0, 30.12, "", "Transportation", new Date());
         */

        //ASSIGN HomeMonth
        Date today = new Date();
        SimpleDateFormat homeMonthTitle = new SimpleDateFormat("MMM  dd");
        HomeMonth.setText(homeMonthTitle.format(today));

        //ASSIGN HomeAmount, HomeExpenseAmount, HomeSaveAmount
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDateOfMonth = calendar.getTime();
        String sqlFirstDate = JournalDatabase.sqlDateFormat.format(firstDateOfMonth);

        double expenseAmount = journalDatabase.getExpenseBalance(sqlFirstDate,"");
        double saveAmount = journalDatabase.getSaveBalance(sqlFirstDate, "");

        HomeAmount.setText(String.format("$ %.2f", saveAmount-expenseAmount));
        HomeExpenseAmount.setText(String.format("$ %.2f", expenseAmount));
        HomeSaveAmount.setText(String.format("$ %.2f", saveAmount));

        //SET recyclerView
        journalAdapter = new JournalAdapter(getContext(), journals);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(journalAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        journals.clear();
        queryAddAll(0);

        //SET endless scrolling bar for  recyclerView
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                queryAddAll(page);
            }
        };

        //SET LISTENER FOR ExpenseButton
        ExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddActivity.class);
                intent.putExtra(isExpense, true);
                startActivity(intent);
            }
        });

        //SET LISTENER FOR SaveButton
        SaveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddActivity.class);
                intent.putExtra(isExpense, false);
                startActivity(intent);
            }
        });
    }

    //query journals located at page(begins at 20 * page)
    public void queryAddAll(int page){
        List<Journal> newJournals = journalDatabase.queryJournal(page * queryLimit);
        if(newJournals != null && newJournals.size() > 0){
            journals.addAll(newJournals);
            journalAdapter.notifyItemRangeChanged(page * queryLimit, queryLimit);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        journals.clear();
        queryAddAll(0);
    }
}