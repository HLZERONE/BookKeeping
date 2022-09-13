package com.example.bookkeeping;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JournalDatabase extends SQLiteOpenHelper {

    private static final String TAG = "JournalDatabase";

    public static final String DATABASE_NAME = "Journals.db";

    public static final String TABLE_NAME_JOURNALS = "journals";
    public static final String COL_ID = "id";
    public static final String COL_SAVE_AMOUNT = "saveAmount";
    public static final String COL_EXPENSE_AMOUNT = "expenseAmount";
    public static final String COL_COMMENT = "comment";
    public static final String COL_CATEGORY = "category";
    public static final String COL_DATE = "dateColumn";

    public static final String TABLE_EXPENSE_CATEGORY = "expense_categories";
    public static final String TABLE_SAVE_CATEGORY = "save_categories";
    public static final String COL_IMAGE_ID = "imageId";

    public static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public JournalDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //journal table
        db.execSQL("CREATE TABLE journals(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "saveAmount DOUBLE, " +
                "expenseAmount DOUBLE, " +
                "comment VARCHAR(250), " +
                "category VARCHAR(50)," +
                "dateColumn DATETIME NOT NULL)");

        int shoppingImage = R.drawable.shopping_1;

        //expense categories table
        db.execSQL("CREATE TABLE expense_categories(category VARCHAR(50) PRIMARY KEY, imageId INTEGER)");
        db.execSQL("INSERT INTO expense_categories(category, imageId) VALUES ('Shopping', ?)", new String[]{String.valueOf(R.drawable.shopping_1)});
        db.execSQL("INSERT INTO expense_categories(category, imageId) VALUES ('Food and Drinks', ?)", new String[]{String.valueOf(R.drawable.food_and_drinks_2)});
        db.execSQL("INSERT INTO expense_categories(category, imageId) VALUES ('Housing and Utilities', ?)", new String[]{String.valueOf(R.drawable.housing_and_utility_3)});
        db.execSQL("INSERT INTO expense_categories(category, imageId) VALUES ('Transportation', ?)", new String[]{String.valueOf(R.drawable.transportation_4)});
        db.execSQL("INSERT INTO expense_categories(category, imageId) VALUES ('Unknown/Others', ?)", new String[]{String.valueOf(R.drawable.others_0)});

        //save categories table
        db.execSQL("CREATE TABLE save_categories(category VARCHAR(50) PRIMARY KEY, imageId INTEGER)");
        db.execSQL("INSERT INTO save_categories(category, imageId) VALUES ('Salary', ?)", new String[]{String.valueOf(R.drawable.salary_6)});
        db.execSQL("INSERT INTO save_categories(category, imageId) VALUES ('Investment', ?)", new String[]{String.valueOf(R.drawable.investment_7)});
        db.execSQL("INSERT INTO save_categories(category, imageId) VALUES ('Sold Items', ?)", new String[]{String.valueOf(R.drawable.sold_items_8)});
        db.execSQL("INSERT INTO save_categories(category, imageId) VALUES ('Unknown/Others', ?)", new String[]{String.valueOf(R.drawable.others_0)});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_JOURNALS);
    }

    public void redoTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_JOURNALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_CATEGORY);
        onCreate(db);
    }

    //insert a new journal by saveAmount, expenseAmount, comment, and category
    public boolean insertJournalData(double saveAmount, double expenseAmount, String comment, String category, Date dateTime){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_SAVE_AMOUNT, saveAmount);
        contentValues.put(COL_EXPENSE_AMOUNT, expenseAmount);
        contentValues.put(COL_COMMENT, comment);
        contentValues.put(COL_CATEGORY, category);
        contentValues.put(COL_DATE, sqlDateFormat.format(dateTime));
        long result = DB.insert(TABLE_NAME_JOURNALS, null, contentValues);
        if(result == -1){
            DB.close();
            return false;
        }
        DB.close();
        return true;
    }

    //update journal by id, saveAmount, expenseAmount, comment, category
    public boolean updateJournalData(int id, double saveAmount, double expenseAmount, String comment, String category, Date dateTime){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM " + TABLE_NAME_JOURNALS + " WHERE "+COL_ID+" = ?", new String[]{String.valueOf(id)});
        if(cursor.getCount() > 0){
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_SAVE_AMOUNT, saveAmount);
            contentValues.put(COL_EXPENSE_AMOUNT, expenseAmount);
            contentValues.put(COL_COMMENT, comment);
            contentValues.put(COL_CATEGORY, category);
            contentValues.put(COL_DATE, sqlDateFormat.format(dateTime));
            long result = DB.update(TABLE_NAME_JOURNALS, contentValues, COL_ID + "=?", new String[]{String.valueOf(id)});
            if(result == -1){
                DB.close();
                cursor.close();
                return false;
            }
        }else{
            DB.close();
            cursor.close();
            return false;
        }
        DB.close();
        cursor.close();
        return true;
    }

    //delete a journal by id
    //TODO: POP UP WARNING WINDOW BEFORE DELETE IT
    public boolean deleteJournalData(int id){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM "+ TABLE_NAME_JOURNALS +" WHERE id = ?", new String[]{String.valueOf(id)});
        if(cursor.getCount() > 0){
            long result = DB.delete(TABLE_NAME_JOURNALS, COL_ID + "=?", new String[]{String.valueOf(id)});
            if(result == -1){
                DB.close();
                cursor.close();
                return false;
            }
        }else{
            DB.close();
            cursor.close();
            return false;
        }
        DB.close();
        cursor.close();
        return true;
    }

    //Calculating sum of all expense, if date == "", calculate all
    public double getExpenseBalance(String beforeDate, String categoryName){
        double result = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        String sqlStatement = "SELECT SUM(" +COL_EXPENSE_AMOUNT+ ") FROM " + TABLE_NAME_JOURNALS;
        String[] selectionArgs = null;

        if(!beforeDate.equals("") && !categoryName.equals("")){
            sqlStatement += " WHERE date("+ COL_DATE +") >= date(?) AND "+ COL_CATEGORY + " = ?";
            selectionArgs = new String[]{beforeDate, categoryName};
        }
        else if(!beforeDate.equals("")){
            sqlStatement += " WHERE date("+ COL_DATE +") >= date(?)";
            selectionArgs = new String[]{beforeDate};
        }
        else if(!categoryName.equals("")){
            sqlStatement += " WHERE " + COL_CATEGORY + " = ?";
            selectionArgs = new String[]{categoryName};
        }

        Cursor cursor = db.rawQuery(sqlStatement, selectionArgs);
        if(cursor.moveToFirst()){
            result = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return result;
    }

    //Calculating sum of all save, if date == "", calculate all
    public double getSaveBalance(String beforeDate , String categoryName){
        double result = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        String sqlStatement = "SELECT SUM(" +COL_SAVE_AMOUNT+ ") FROM " + TABLE_NAME_JOURNALS;
        String[] selectionArgs = null;

        if(!beforeDate.equals("") && !categoryName.equals("")){
            sqlStatement += " WHERE date("+ COL_DATE +") >= date(?) AND "+ COL_CATEGORY + " = ?";
            selectionArgs = new String[]{beforeDate, categoryName};
        }
        else if(!beforeDate.equals("")){
            sqlStatement += " WHERE date("+ COL_DATE +") >= date(?)";
            selectionArgs = new String[]{beforeDate};
        }
        else if(!categoryName.equals("")){
            sqlStatement += " WHERE " + COL_CATEGORY + " = ?";
            selectionArgs = new String[]{categoryName};
        }

        Cursor cursor = db.rawQuery(sqlStatement, selectionArgs);
        if(cursor.moveToFirst()){
            result = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return result;
    }

    //add a new expense category
    public boolean insertExpenseCategoriesData(String category, int imageId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_CATEGORY, category);
        contentValues.put(COL_IMAGE_ID, imageId);
        long result = db.insert(TABLE_EXPENSE_CATEGORY, null, contentValues);
        if(result == -1){
            db.close();
            return false;
        }
        db.close();
        return true;
    }

    //delete an expense category
    //TODO: POP UP WARNING WINDOW BEFORE DELETE IT
    public boolean deleteExpenseCategoriesData(String category){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TABLE_EXPENSE_CATEGORY +" WHERE " + COL_CATEGORY + "=?", new String[]{category});
        if(cursor.getCount() > 0){
            long result = db.delete(TABLE_EXPENSE_CATEGORY, COL_CATEGORY + "=?", new String[]{category});
            if(result == -1){
                cursor.close();
                db.close();
                return false;
            }
        }
        else{
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();
        db.close();
        return true;
    }

    //add a new save category
    public boolean insertSaveCategoriesData(String category, int imageId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_CATEGORY, category);
        contentValues.put(COL_IMAGE_ID, imageId);
        long result = db.insert(TABLE_SAVE_CATEGORY, null, contentValues);
        if(result == -1){
            db.close();
            return false;
        }
        db.close();
        return true;
    }

    //delete a save category
    //TODO: POP UP WARNING WINDOW BEFORE DELETE IT
    public boolean deleteSaveCategoriesData(String category){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TABLE_SAVE_CATEGORY +" WHERE " + COL_CATEGORY + "=?", new String[]{category});
        if(cursor.getCount() > 0){
            long result = db.delete(TABLE_SAVE_CATEGORY, COL_CATEGORY + "=?", new String[]{category});
            if(result == -1){
                cursor.close();
                db.close();
                return false;
            }
        }
        else{
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();
        db.close();
        return true;
    }

    //find the imageId by category name and if it is expense(True) or save(False)
    //ASSUMPTION: CATEGORY NAME EXITS
    public int findImageId(String category, boolean isExpense){
        SQLiteDatabase db = getWritableDatabase();
        int imageId = 0;
        if(isExpense){
            Cursor cursor = db.rawQuery("SELECT " + COL_IMAGE_ID + " FROM " + TABLE_EXPENSE_CATEGORY + " WHERE " + COL_CATEGORY + "=?", new String[]{category});
            if(cursor.moveToFirst()){
                imageId = cursor.getInt(0);
            }
            cursor.close();
        }
        else{
            Cursor cursor = db.rawQuery("SELECT " + COL_IMAGE_ID + " FROM " + TABLE_SAVE_CATEGORY + " WHERE " + COL_CATEGORY + "=?", new String[]{category});
            if(cursor.moveToFirst()){
                imageId = cursor.getInt(0);
            }
            cursor.close();
        }
        db.close();
        return imageId;
    }

    //query journal and return list of Journal object, start at row offset+1
    public List<Journal> queryJournal(int offset){
        List<Journal> journals = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        /*
        SELECT *
        FROM journals
        ORDER BY dateColumn DESC
        LIMIT 20 OFFSET offset
         */
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_JOURNALS
                + " ORDER BY " + COL_DATE + " DESC LIMIT 20 OFFSET ?", new String[]{String.valueOf(offset)});
        if(cursor.moveToFirst()){
            //assign index value
            int idIndex = cursor.getColumnIndex(COL_ID);
            int saveAmountIndex = cursor.getColumnIndex(COL_SAVE_AMOUNT);
            int expenseAmountIndex = cursor.getColumnIndex(COL_EXPENSE_AMOUNT);
            int commentIndex = cursor.getColumnIndex(COL_COMMENT);
            int categoryIndex = cursor.getColumnIndex(COL_CATEGORY);
            int dateIndex = cursor.getColumnIndex(COL_DATE);

            while(!cursor.isAfterLast()){
                int id = cursor.getInt(idIndex);
                double saveAmount = cursor.getDouble(saveAmountIndex);
                double expenseAmount = cursor.getDouble(expenseAmountIndex);
                String comment = cursor.getString(commentIndex);
                String category = cursor.getString(categoryIndex);
                String dateString = cursor.getString(dateIndex);
                Date date = null;
                try {
                    date = sqlDateFormat.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //find imageId
                int imageId = R.drawable.others_0;
                if(saveAmount == 0){
                    imageId = findImageId(category, true);
                } else{
                    imageId = findImageId(category, false);
                }
                //add a new journal
                journals.add(new Journal(id, saveAmount, expenseAmount, comment, category, date, imageId));
                cursor.moveToNext();
            }
        }

        cursor.close();
        db.close();

        return journals;
    }

    public List<Category> queryExpenseCategory(){
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        //query Expense Categories Table
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSE_CATEGORY, null);
        if(cursor.moveToFirst()){
            int categoryNameIndex = cursor.getColumnIndex(COL_CATEGORY);
            int categoryImageIdIndex = cursor.getColumnIndex(COL_IMAGE_ID);
            while(!cursor.isAfterLast()){
                categoryList.add(new Category(cursor.getString(categoryNameIndex), cursor.getInt(categoryImageIdIndex)));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return categoryList;
    }

    public List<Category> querySaveCategory(){
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SAVE_CATEGORY, null);
        if(cursor.moveToFirst()){
            int categoryNameIndex = cursor.getColumnIndex(COL_CATEGORY);
            int categoryImageIdIndex = cursor.getColumnIndex(COL_IMAGE_ID);
            while(!cursor.isAfterLast()){
                categoryList.add(new Category(cursor.getString(categoryNameIndex), cursor.getInt(categoryImageIdIndex)));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return categoryList;
    }
}
