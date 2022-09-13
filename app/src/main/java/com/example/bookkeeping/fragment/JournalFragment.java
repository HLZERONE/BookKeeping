package com.example.bookkeeping.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.method.BaseKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.bookkeeping.Category;
import com.example.bookkeeping.JournalDatabase;
import com.example.bookkeeping.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JournalFragment extends Fragment {

    TextView JournalAmount;
    TextView JournalMonth;
    TextView JournalExpenseAmount;
    TextView JournalSaveAmount;
    PieChart pieChart;
    Switch pieChartSwitch;

    JournalDatabase journalDatabase;
    ArrayList<Integer> colors;

    private final String TAG = "JournalFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_journal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //ASSIGN all the View
        JournalAmount = view.findViewById(R.id.JournalAmount);
        JournalMonth = view.findViewById(R.id.JournalMonth);
        JournalExpenseAmount = view.findViewById(R.id.JournalExpenseAmount);
        JournalSaveAmount = view.findViewById(R.id.JournalSaveAmount);
        pieChart = view.findViewById(R.id.pieChart);
        pieChartSwitch = view.findViewById(R.id.pieChartSwitch);

        //GET database
        journalDatabase = new JournalDatabase(getContext());

        //ASSIGN JournalMonth
        Date today = new Date();
        SimpleDateFormat homeMonthTitle = new SimpleDateFormat("MM/dd/yyyy");
        JournalMonth.setText(homeMonthTitle.format(today));

        //ASSIGN JournalAmount, JournalExpenseAmount, JournalSaveAmount
        double expenseAmount = journalDatabase.getExpenseBalance("", "");
        double saveAmount = journalDatabase.getSaveBalance("", "");

        JournalAmount.setText(String.format("$ %.2f", saveAmount-expenseAmount));
        JournalExpenseAmount.setText(String.format("$ %.2f", expenseAmount));
        JournalSaveAmount.setText(String.format("$ %.2f", saveAmount));

        //SET up pieChart
        colors = new ArrayList<>();
        for(int color: ColorTemplate.MATERIAL_COLORS){
            colors.add(color);
        }
        for(int color: ColorTemplate.VORDIPLOM_COLORS){
            colors.add(color);
        }
        setupPieChart();
        loadPieChartData(true);

        //SET up pieChartSwitch
        pieChartSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    loadPieChartData(false);
                    pieChart.setCenterText("Expense");
                }else{
                    loadPieChartData(true);
                    pieChart.setCenterText("Save");
                }
            }
        });
    }

    //setting up the pieChart
    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true); //like a doughnut than a pie
        pieChart.setUsePercentValues(true); //using percentage value
        //showing the labels
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        //center text
        pieChart.setCenterText("Expense");
        pieChart.setCenterTextSize(24);

        pieChart.getDescription().setEnabled(false); //not showing the description text

        pieChart.getLegend().setEnabled(false);
        Legend l = pieChart.getLegend();
    }


    private void loadPieChartData(boolean isExpense){
        PieDataSet dataSet = (isExpense)? buildExpensePieDataSet() : buildSavePieDataSet();
        dataSet.setColors(colors); //color of pieChart data

        PieData data = new PieData(dataSet);
        data.setDrawValues(true); //show value
        data.setValueFormatter(new PercentFormatter(pieChart)); //in percentage format
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate(); //Need to be update and refresh

        pieChart.animateY(800, Easing.EaseInOutBack);
    }


    public PieDataSet buildExpensePieDataSet(){
        ArrayList<PieEntry> entries = new ArrayList<>();

        ArrayList<Category> categoryList = new ArrayList<>();
        categoryList.addAll(journalDatabase.queryExpenseCategory());
        for(int i=0; i<categoryList.size(); i++){
            String categoryName = categoryList.get(i).getCategoryName();
            double val = journalDatabase.getExpenseBalance("", categoryName);
            if(val != 0){
                entries.add(new PieEntry((float) val, categoryName + " " + String.valueOf(val)));
            }
        }
        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");

        return dataSet;
    }

    public PieDataSet buildSavePieDataSet(){
        ArrayList<PieEntry> entries = new ArrayList<>();

        ArrayList<Category> categoryList = new ArrayList<>();
        categoryList.addAll(journalDatabase.querySaveCategory());
        for(int i=0; i<categoryList.size(); i++){
            String categoryName = categoryList.get(i).getCategoryName();
            double val = journalDatabase.getSaveBalance("", categoryName);
            if(val != 0){
                entries.add(new PieEntry((float) val, categoryName+ " " + String.valueOf(val)));
            }
        }
        PieDataSet dataSet = new PieDataSet(entries, "Save Categories");

        return dataSet;
    }
}