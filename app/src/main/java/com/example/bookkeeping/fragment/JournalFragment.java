package com.example.bookkeeping.fragment;

import android.app.DatePickerDialog;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class JournalFragment extends Fragment {

    TextView JournalAmount;
    TextView JournalMonth;
    TextView JournalExpenseAmount;
    TextView JournalSaveAmount;
    PieChart pieChart;
    Switch pieChartSwitch;

    TextView fromDate; //afterDate
    TextView toDate; //beforeDate
    Button applyButton;

    JournalDatabase journalDatabase;
    ArrayList<Integer> colors;
    String beforeDate = "";
    String afterDate = "";
    int beforeYear;
    int beforeMonth;
    int beforeDay;
    int afterYear;
    int afterMonth;
    int afterDay;

    private final String TAG = "JournalFragment";
    double total = 0;

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
        fromDate = view.findViewById(R.id.fromDate);
        toDate = view.findViewById(R.id.toDate);
        applyButton = view.findViewById(R.id.applyButton);

        //GET database
        journalDatabase = new JournalDatabase(getContext());

        //ASSIGN JournalMonth
        Date today = new Date();
        SimpleDateFormat homeMonthTitle = new SimpleDateFormat("MM/dd/yyyy");
        JournalMonth.setText(homeMonthTitle.format(today));

        //ASSIGN JournalAmount, JournalExpenseAmount, JournalSaveAmount
        double expenseAmount = journalDatabase.getExpenseBalance("", "", "");
        double saveAmount = journalDatabase.getSaveBalance("", "", "");

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
                    pieChartSwitch.setText("Save");
                }else{
                    loadPieChartData(true);
                    pieChartSwitch.setText("Expense");
                }
            }
        });

        //SETUP Text of toDate and Y, M, D of beforeDate and afterDate
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        beforeYear = calendar.get(Calendar.YEAR);
        beforeMonth = calendar.get(Calendar.MONTH);
        beforeDay = calendar.get(Calendar.DAY_OF_MONTH);
        afterYear = calendar.get(Calendar.YEAR);
        afterMonth = calendar.get(Calendar.MONTH);
        afterDay = calendar.get(Calendar.DAY_OF_MONTH);
        toDate.setText(beforeYear + "-" + (beforeMonth+1) + "-" + beforeDay);

        //PICK beforeDate/toDate
        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                        beforeYear = y;
                        beforeMonth = m;
                        beforeDay = d;
                        beforeDate = beforeYear + "-" + (beforeMonth+1) + "-" + beforeDay;
                        toDate.setText(beforeDate);
                        try{
                            Date date = JournalDatabase.sqlDateFormat.parse(beforeDate  + " 00:00:00");
                            beforeDate = JournalDatabase.sqlDateFormat.format(date);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }, beforeYear, beforeMonth, beforeDay);
                datePickerDialog.show();
            }
        });

        //PICK afterDate/fromDate
        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                        afterYear = y;
                        afterMonth = m;
                        afterDay = d;
                        afterDate = afterYear + "-" + (afterMonth+1) + "-" + afterDay;
                        fromDate.setText(afterDate);
                        try{
                            Date date = JournalDatabase.sqlDateFormat.parse(afterDate  + " 00:00:00");
                            afterDate = JournalDatabase.sqlDateFormat.format(date);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }, afterYear, afterMonth, afterDay);
                datePickerDialog.show();
            }
        });

        //SETUP Listener for ApplyButton
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pieChartSwitch.isChecked()){
                    loadPieChartData(false);
                }else{
                    loadPieChartData(true);
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
        pieChart.setCenterTextSize(24);

        pieChart.getDescription().setEnabled(false); //not showing the description text

        //pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setEnabled(true);
        l.setXEntrySpace(4f);
        l.setYEntrySpace(0f);
        l.setWordWrapEnabled(true);
    }


    private void loadPieChartData(boolean isExpense){
        PieDataSet dataSet = (isExpense)? buildExpensePieDataSet() : buildSavePieDataSet();
        dataSet.setColors(colors); //color of pieChart data
        //dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true); //show value
        data.setValueFormatter(new PercentFormatter(pieChart)); //in percentage format
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate(); //Need to be update and refresh

        pieChart.animateY(800, Easing.EaseInOutBack);
        pieChart.setCenterText("Total: " + String.format("%.2f",total));
    }


    public PieDataSet buildExpensePieDataSet(){
        ArrayList<PieEntry> entries = new ArrayList<>();

        ArrayList<Category> categoryList = new ArrayList<>();
        categoryList.addAll(journalDatabase.queryExpenseCategory());

        total = 0;
        for(int i=0; i<categoryList.size(); i++){
            String categoryName = categoryList.get(i).getCategoryName();
            double val = journalDatabase.getExpenseBalance(beforeDate, afterDate, categoryName);
            total += val;
            if(val != 0){
                entries.add(new PieEntry((float) val, categoryName + " " + String.format("%.2f",val)));
            }
        }
        PieDataSet dataSet = new PieDataSet(entries, "");

        return dataSet;
    }

    public PieDataSet buildSavePieDataSet(){
        ArrayList<PieEntry> entries = new ArrayList<>();

        ArrayList<Category> categoryList = new ArrayList<>();
        categoryList.addAll(journalDatabase.querySaveCategory());
        total = 0;
        for(int i=0; i<categoryList.size(); i++){
            String categoryName = categoryList.get(i).getCategoryName();
            double val = journalDatabase.getSaveBalance(beforeDate, afterDate, categoryName);
            total += val;
            if(val != 0){
                entries.add(new PieEntry((float) val, categoryName + " " + String.format("%.2f",val)));
            }
        }
        PieDataSet dataSet = new PieDataSet(entries, "");

        return dataSet;
    }
}