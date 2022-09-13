package com.example.bookkeeping;


import java.util.Date;

public class Journal {

    int id;
    double saveAmount;
    double expenseAmount;
    String comment;
    String category;
    Date date;
    int imageId;

    public Journal(int id, double saveAmount, double expenseAmount, String comment, String category, Date date, int imageId){
        this.id = id;
        this.saveAmount = saveAmount;
        this.expenseAmount = expenseAmount;
        this.comment = comment;
        this.category = category;
        this.date = date;
        this.imageId = imageId;
    }

    public int getId(){
        return id;
    }

    public double getSaveAmount(){
        return saveAmount;
    }

    public double getExpenseAmount(){
        return expenseAmount;
    }

    public String getComment() {
        return comment;
    }

    public String getCategory(){ return category; }

    public Date getDate(){
        return date;
    }

    public int getImageId(){
        return imageId;
    }
}
