package com.andika.temucashflow.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.andika.temucashflow.model.CategoryStat;
import com.andika.temucashflow.model.Transaction;
import com.andika.temucashflow.model.SavingsGoal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TemuCashflow.db";
    private static final int DATABASE_VERSION = 4; // Incremented to 4

    private static final String TABLE_USERS = "users";
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String TABLE_GOALS = "savings_goals";

    // User Columns
    private static final String COL_USER_ID = "user_id";
    private static final String COL_USER_NAME = "name";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD = "password";
    private static final String COL_USER_CREATED_AT = "created_at";

    // Transaction Columns
    private static final String COL_TRANS_ID = "id";
    private static final String COL_TRANS_TYPE = "type";
    private static final String COL_TRANS_AMOUNT = "amount";
    private static final String COL_TRANS_CATEGORY = "category";
    private static final String COL_TRANS_DESCRIPTION = "description";
    private static final String COL_TRANS_DATE = "date";
    private static final String COL_TRANS_USER_ID = "user_id";
    private static final String COL_TRANS_IMAGE = "image_path";

    // Goals Columns
    private static final String COL_GOAL_ID = "id";
    private static final String COL_GOAL_NAME = "name";
    private static final String COL_GOAL_TARGET = "target_amount";
    private static final String COL_GOAL_CURRENT = "current_amount";
    private static final String COL_GOAL_USER_ID = "user_id";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT, " +
                COL_USER_EMAIL + " TEXT UNIQUE, " +
                COL_USER_PASSWORD + " TEXT, " +
                COL_USER_CREATED_AT + " INTEGER)";

        String createTransactions = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRANS_TYPE + " TEXT, " +
                COL_TRANS_AMOUNT + " REAL, " +
                COL_TRANS_CATEGORY + " TEXT, " +
                COL_TRANS_DESCRIPTION + " TEXT, " +
                COL_TRANS_DATE + " INTEGER, " +
                COL_TRANS_USER_ID + " INTEGER, " +
                COL_TRANS_IMAGE + " TEXT)";

        String createGoals = "CREATE TABLE " + TABLE_GOALS + " (" +
                COL_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_GOAL_NAME + " TEXT, " +
                COL_GOAL_TARGET + " REAL, " +
                COL_GOAL_CURRENT + " REAL, " +
                COL_GOAL_USER_ID + " INTEGER)";

        db.execSQL(createUsers);
        db.execSQL(createTransactions);
        db.execSQL(createGoals);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String createGoals = "CREATE TABLE " + TABLE_GOALS + " (" +
                    COL_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_GOAL_NAME + " TEXT, " +
                    COL_GOAL_TARGET + " REAL, " +
                    COL_GOAL_CURRENT + " REAL, " +
                    COL_GOAL_USER_ID + " INTEGER)";
            db.execSQL(createGoals);
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_USER_CREATED_AT + " INTEGER DEFAULT " + System.currentTimeMillis());
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS + " ADD COLUMN " + COL_TRANS_IMAGE + " TEXT");
        }
    }

    // ==================== USER ====================

    public long registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password);
        values.put(COL_USER_CREATED_AT, System.currentTimeMillis());

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " +
                COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public long getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=?", new String[]{email},
                null, null, null);

        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID));
        }
        cursor.close();
        db.close();
        return id;
    }

    public String getUserName(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_NAME},
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);

        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
        }
        cursor.close();
        db.close();
        return name;
    }

    public boolean updateUserName(long userId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, newName);
        int rows = db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    // ==================== TRANSACTION ====================

    public long insertTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANS_TYPE, transaction.getType());
        values.put(COL_TRANS_AMOUNT, transaction.getAmount());
        values.put(COL_TRANS_CATEGORY, transaction.getCategory());
        values.put(COL_TRANS_DESCRIPTION, transaction.getDescription());
        values.put(COL_TRANS_DATE, transaction.getDate());
        values.put(COL_TRANS_USER_ID, transaction.getUserId());
        values.put(COL_TRANS_IMAGE, transaction.getImagePath());

        long id = db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
        return id;
    }

    public boolean updateTransaction(Transaction t) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANS_TYPE, t.getType());
        values.put(COL_TRANS_AMOUNT, t.getAmount());
        values.put(COL_TRANS_CATEGORY, t.getCategory());
        values.put(COL_TRANS_DESCRIPTION, t.getDescription());
        values.put(COL_TRANS_DATE, t.getDate());
        values.put(COL_TRANS_IMAGE, t.getImagePath());

        int rows = db.update(TABLE_TRANSACTIONS, values, COL_TRANS_ID + "=?",
                new String[]{String.valueOf(t.getId())});
        db.close();
        return rows > 0;
    }

    public Transaction getTransactionById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, COL_TRANS_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        
        Transaction t = null;
        if (cursor.moveToFirst()) {
            t = cursorToTransaction(cursor);
        }
        cursor.close();
        db.close();
        return t;
    }

    public List<Transaction> getAllTransactions(long userId) {
        return getTransactionsFiltered(userId, "all", "date", "desc", "");
    }

    public List<Transaction> getTransactionsFiltered(long userId, String type, String sortBy, String order, String queryText) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder selection = new StringBuilder(COL_TRANS_USER_ID + "=?");
        List<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(String.valueOf(userId));

        if (type != null && !type.equals("all")) {
            selection.append(" AND ").append(COL_TRANS_TYPE).append("=?");
            selectionArgsList.add(type);
        }

        if (queryText != null && !queryText.isEmpty()) {
            selection.append(" AND (").append(COL_TRANS_DESCRIPTION).append(" LIKE ? OR ").append(COL_TRANS_CATEGORY).append(" LIKE ?)");
            selectionArgsList.add("%" + queryText + "%");
            selectionArgsList.add("%" + queryText + "%");
        }

        String[] selectionArgs = selectionArgsList.toArray(new String[0]);

        String orderBy = COL_TRANS_DATE + " DESC";
        if ("amount".equals(sortBy)) {
            orderBy = COL_TRANS_AMOUNT + " " + ("asc".equals(order) ? "ASC" : "DESC");
        } else if ("date".equals(sortBy)) {
            orderBy = COL_TRANS_DATE + " " + ("asc".equals(order) ? "ASC" : "DESC");
        }

        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, selection.toString(), selectionArgs,
                null, null, orderBy);

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToTransaction(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public long getUserCreationDate(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        long date = 0;
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_CREATED_AT},
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            date = cursor.getLong(0);
            cursor.close();
        }
        return date;
    }

    public List<CategoryStat> getCategoryStats(long userId, String type) {
        List<CategoryStat> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COL_TRANS_CATEGORY + ", SUM(" + COL_TRANS_AMOUNT + ") as total, " +
                "COUNT(*) as count FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_TYPE + "=?" +
                " GROUP BY " + COL_TRANS_CATEGORY +
                " ORDER BY total DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), type});

        double grandTotal = 0;
        List<CategoryStat> rawList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_CATEGORY));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
                rawList.add(new CategoryStat(category, total, count));
                grandTotal += total;
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Merge < 5% into "Lainnya"
        double othersTotal = 0;
        int othersCount = 0;
        for (CategoryStat stat : rawList) {
            if (stat.getTotal() < grandTotal * 0.05) {
                othersTotal += stat.getTotal();
                othersCount += stat.getCount();
            } else {
                list.add(stat);
            }
        }
        if (othersTotal > 0) {
            list.add(new CategoryStat("Lainnya", othersTotal, othersCount));
        }

        return list;
    }

    public String exportToJson(long userId) throws JSONException {
        List<Transaction> transactions = getAllTransactions(userId);
        JSONArray jsonArray = new JSONArray();

        for (Transaction t : transactions) {
            JSONObject obj = new JSONObject();
            obj.put("type", t.getType());
            obj.put("amount", t.getAmount());
            obj.put("category", t.getCategory());
            obj.put("description", t.getDescription());
            obj.put("date", t.getDate());
            jsonArray.put(obj);
        }

        JSONObject result = new JSONObject();
        result.put("app", "TemuCashflow");
        result.put("version", "1.0");
        result.put("exportedAt", System.currentTimeMillis());
        result.put("transactions", jsonArray);

        return result.toString(2);
    }

    public int importFromJson(long userId, String jsonString) throws JSONException {
        JSONObject root = new JSONObject(jsonString);
        JSONArray transactions = root.getJSONArray("transactions");

        SQLiteDatabase db = this.getWritableDatabase();
        int imported = 0;

        db.beginTransaction();
        try {
            for (int i = 0; i < transactions.length(); i++) {
                JSONObject obj = transactions.getJSONObject(i);

                ContentValues values = new ContentValues();
                values.put(COL_TRANS_TYPE, obj.getString("type"));
                values.put(COL_TRANS_AMOUNT, obj.getDouble("amount"));
                values.put(COL_TRANS_CATEGORY, obj.getString("category"));
                values.put(COL_TRANS_DESCRIPTION, obj.getString("description"));
                values.put(COL_TRANS_DATE, obj.getLong("date"));
                values.put(COL_TRANS_USER_ID, userId);

                db.insert(TABLE_TRANSACTIONS, null, values);
                imported++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }

        return imported;
    }

    public Map<Long, Double> getDailyBalanceTrend(long userId, long startTime) {
        Map<Long, Double> trend = new TreeMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT " + COL_TRANS_DATE + ", " + COL_TRANS_TYPE + ", " + COL_TRANS_AMOUNT + 
                " FROM " + TABLE_TRANSACTIONS + 
                " WHERE " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_DATE + " >= ?" +
                " ORDER BY " + COL_TRANS_DATE + " ASC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(startTime)});
        
        double currentBalance = 0;
        
        String balanceQuery = "SELECT SUM(CASE WHEN " + COL_TRANS_TYPE + "='income' THEN " + COL_TRANS_AMOUNT + 
                             " ELSE -" + COL_TRANS_AMOUNT + " END) FROM " + TABLE_TRANSACTIONS + 
                             " WHERE " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_DATE + " < ?";
        Cursor balanceCursor = db.rawQuery(balanceQuery, new String[]{String.valueOf(userId), String.valueOf(startTime)});
        if (balanceCursor.moveToFirst()) {
            currentBalance = balanceCursor.getDouble(0);
        }
        balanceCursor.close();

        if (cursor.moveToFirst()) {
            do {
                long date = cursor.getLong(0);
                String type = cursor.getString(1);
                double amount = cursor.getDouble(2);
                
                if ("income".equals(type)) currentBalance += amount;
                else currentBalance -= amount;
                
                long dayTimestamp = (date / 86400000L) * 86400000L;
                trend.put(dayTimestamp, currentBalance);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return trend;
    }

    public double getTotalIncome(long userId) {
        return getTotalByType(userId, "income");
    }

    public double getTotalExpense(long userId) {
        return getTotalByType(userId, "expense");
    }

    public double getMonthlyExpense(long userId, long startTime) {
        return getMonthlyTotalByType(userId, "expense", startTime);
    }

    public double getMonthlyIncome(long userId, long startTime) {
        return getMonthlyTotalByType(userId, "income", startTime);
    }

    private double getMonthlyTotalByType(long userId, String type, long startTime) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        // End of month
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startTime);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long endTime = cal.getTimeInMillis();

        String query = "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COL_TRANS_TYPE + "=? AND " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_DATE + " BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{type, String.valueOf(userId), String.valueOf(startTime), String.valueOf(endTime)});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return total;
    }

    private double getTotalByType(long userId, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COL_TRANS_TYPE + "=? AND " + COL_TRANS_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{type, String.valueOf(userId)});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return total;
    }

    public void deleteTransaction(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, COL_TRANS_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteAllTransactions(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, COL_TRANS_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        db.close();
    }

    public Map<String, Double> getCategoryBreakdown(long userId, String type) {
        Map<String, Double> map = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_TRANS_CATEGORY + ", SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_TYPE + "=?" +
                " GROUP BY " + COL_TRANS_CATEGORY;
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), type});
        if (cursor.moveToFirst()) {
            do {
                map.put(cursor.getString(0), cursor.getDouble(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return map;
    }

    // ==================== SAVINGS GOALS ====================

    public long insertGoal(SavingsGoal goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GOAL_NAME, goal.getName());
        values.put(COL_GOAL_TARGET, goal.getTargetAmount());
        values.put(COL_GOAL_CURRENT, goal.getCurrentAmount());
        values.put(COL_GOAL_USER_ID, goal.getUserId());
        long id = db.insert(TABLE_GOALS, null, values);
        db.close();
        return id;
    }

    public List<SavingsGoal> getAllGoals(long userId) {
        List<SavingsGoal> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GOALS, null, COL_GOAL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                SavingsGoal goal = new SavingsGoal();
                goal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_GOAL_ID)));
                goal.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_NAME)));
                goal.setTargetAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_GOAL_TARGET)));
                goal.setCurrentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_GOAL_CURRENT)));
                goal.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_GOAL_USER_ID)));
                list.add(goal);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean updateGoalCurrentAmount(long goalId, double newAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GOAL_CURRENT, newAmount);
        int rows = db.update(TABLE_GOALS, values, COL_GOAL_ID + "=?", new String[]{String.valueOf(goalId)});
        db.close();
        return rows > 0;
    }

    public void deleteGoal(long goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GOALS, COL_GOAL_ID + "=?", new String[]{String.valueOf(goalId)});
        db.close();
    }

    private Transaction cursorToTransaction(Cursor cursor) {
        Transaction t = new Transaction();
        t.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRANS_ID)));
        t.setType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_TYPE)));
        t.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRANS_AMOUNT)));
        t.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_CATEGORY)));
        t.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DESCRIPTION)));
        t.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRANS_DATE)));
        t.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRANS_USER_ID)));
        t.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_IMAGE)));
        return t;
    }
}
