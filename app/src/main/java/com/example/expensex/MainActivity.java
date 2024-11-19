package com.example.expensex;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // Firebase reference
    private DatabaseReference databaseReference;

    private EditText etExpenseName, etExpenseAmount;
    private Button btnAddExpense, btnViewExpenses, btnCloseExpenses;
    private TextView tvTotalAmount, tvWeeklyAmount, tvMonthlyAmount, tvYearlyAmount;
    private RecyclerView rvExpenses;
    private ArrayList<Expense> expenseList;
    private ExpenseAdapter expenseAdapter;

    private double totalAmount = 0.0;
    private double weeklyAmount = 0.0;
    private double monthlyAmount = 0.0;
    private double yearlyAmount = 0.0;

    Button viewExpenses;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewExpenses = findViewById(R.id.view_expences);

        viewExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, view_expences.class);
            }
        });

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("expenses");

        // Initialize views
        etExpenseName = findViewById(R.id.etExpenseName);
        etExpenseAmount = findViewById(R.id.etExpenseAmount);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnViewExpenses = findViewById(R.id.btnViewExpenses);
        btnCloseExpenses = findViewById(R.id.btnCloseExpenses); // New Close Expenses button
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvWeeklyAmount = findViewById(R.id.tvWeeklyAmount);
        tvMonthlyAmount = findViewById(R.id.tvMonthlyAmount);
        tvYearlyAmount = findViewById(R.id.tvYearlyAmount);
        rvExpenses = findViewById(R.id.rvExpenses);

        // Initialize the expense list and adapter
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(this, expenseList);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(expenseAdapter);

        // Fetch expenses from Firebase
        fetchExpensesFromFirebase();

        // Add expense button click listener
        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExpense();
            }
        });

        // View expenses button click listener
        btnViewExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvExpenses.setVisibility(View.VISIBLE); // Show the list
                tvTotalAmount.setVisibility(View.VISIBLE); // Show the total amount
                tvWeeklyAmount.setVisibility(View.VISIBLE); // Show weekly amount
                tvMonthlyAmount.setVisibility(View.VISIBLE); // Show monthly amount
                tvYearlyAmount.setVisibility(View.VISIBLE); // Show yearly amount
            }
        });

        // Close expenses button click listener
        btnCloseExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvExpenses.setVisibility(View.GONE); // Hide the list
                tvTotalAmount.setVisibility(View.GONE); // Hide the total amount
                tvWeeklyAmount.setVisibility(View.GONE); // Hide weekly amount
                tvMonthlyAmount.setVisibility(View.GONE); // Hide monthly amount
                tvYearlyAmount.setVisibility(View.GONE); // Hide yearly amount
            }
        });
    }

    private void fetchExpensesFromFirebase() {
        // Listen for data changes
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the existing list
                expenseList.clear();

                // Iterate through the data and add it to the list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Expense expense = snapshot.getValue(Expense.class);
                    if (expense != null) {
                        expenseList.add(expense);
                    }
                }

                // Notify the adapter that data has changed
                expenseAdapter.notifyDataSetChanged();

                // Update the total amount
                updateTotals();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                Toast.makeText(MainActivity.this, "Failed to load expenses.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addExpense() {
        String expenseName = etExpenseName.getText().toString();
        String expenseAmountStr = etExpenseAmount.getText().toString();

        if (expenseName.isEmpty() || expenseAmountStr.isEmpty()) {
            Toast.makeText(this, "Please enter both name and amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        double expenseAmount = Double.parseDouble(expenseAmountStr);

        // Add the expense to the list
        Expense expense = new Expense(expenseName, expenseAmount, new Date());
        expenseList.add(expense);
        expenseAdapter.notifyDataSetChanged();

        // Update the totals
        totalAmount += expenseAmount;

        // Calculate weekly, monthly, and yearly totals
        calculateWeeklyAmount(expenseAmount, expense.getDate());
        calculateMonthlyAmount(expenseAmount, expense.getDate());
        calculateYearlyAmount(expenseAmount, expense.getDate());

        // Update UI with calculated values
        tvTotalAmount.setText("Total ৳" + totalAmount);
        tvWeeklyAmount.setText("Weekly ৳" + weeklyAmount);
        tvMonthlyAmount.setText("Monthly ৳" + monthlyAmount);
        tvYearlyAmount.setText("Yearly ৳" + yearlyAmount);

        // Save expense to Firebase
        String expenseId = databaseReference.push().getKey(); // Generate unique ID for each expense
        if (expenseId != null) {
            databaseReference.child(expenseId).setValue(expense);
        }

        // Clear the input fields
        etExpenseName.setText("");
        etExpenseAmount.setText("");
    }

    private void updateTotals() {
        totalAmount = 0.0;
        weeklyAmount = 0.0;
        monthlyAmount = 0.0;
        yearlyAmount = 0.0;

        for (Expense expense : expenseList) {
            totalAmount += expense.getAmount();
            calculateWeeklyAmount(expense.getAmount(), expense.getDate());
            calculateMonthlyAmount(expense.getAmount(), expense.getDate());
            calculateYearlyAmount(expense.getAmount(), expense.getDate());
        }

        tvTotalAmount.setText("৳" + totalAmount);
        tvWeeklyAmount.setText("৳" + weeklyAmount);
        tvMonthlyAmount.setText("৳" + monthlyAmount);
        tvYearlyAmount.setText("৳" + yearlyAmount);
    }

    private void calculateWeeklyAmount(double expenseAmount, Date expenseDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expenseDate);
        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        int currentYear = calendar.get(Calendar.YEAR);

        calendar.setTime(new Date());  // current date
        int currentWeekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        int currentYearNow = calendar.get(Calendar.YEAR);

        if (currentWeek == currentWeekOfYear && currentYear == currentYearNow) {
            weeklyAmount += expenseAmount;
        }
    }

    private void calculateMonthlyAmount(double expenseAmount, Date expenseDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expenseDate);
        int expenseMonth = calendar.get(Calendar.MONTH);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);

        if (expenseMonth == currentMonth) {
            monthlyAmount += expenseAmount;
        }
    }

    private void calculateYearlyAmount(double expenseAmount, Date expenseDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expenseDate);
        int expenseYear = calendar.get(Calendar.YEAR);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        if (expenseYear == currentYear) {
            yearlyAmount += expenseAmount;
        }
    }

    // Expense class to represent individual expenses
    public static class Expense {

        private String name;
        private double amount;
        private Date date;

        public Expense() {
            // Default constructor required for Firebase
        }

        public Expense(String name, double amount, Date date) {
            this.name = name;
            this.amount = amount;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public double getAmount() {
            return amount;
        }

        public Date getDate() {
            return date;
        }
    }

    // Expense Adapter to bind data to RecyclerView
    public static class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

        private ArrayList<Expense> expenseList;
        private android.content.Context context;

        public ExpenseAdapter(android.content.Context context, ArrayList<Expense> expenseList) {
            this.context = context;
            this.expenseList = expenseList;
        }

        @Override
        public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
            return new ExpenseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ExpenseViewHolder holder, int position) {
            Expense expense = expenseList.get(position);
            holder.tvExpenseName.setText(expense.getName());
            holder.tvExpenseAmount.setText("৳" + expense.getAmount());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            holder.tvExpenseDate.setText(sdf.format(expense.getDate()));
        }

        @Override
        public int getItemCount() {
            return expenseList.size();
        }

        public static class ExpenseViewHolder extends RecyclerView.ViewHolder {

            TextView tvExpenseName, tvExpenseAmount, tvExpenseDate;

            public ExpenseViewHolder(View itemView) {
                super(itemView);
                tvExpenseName = itemView.findViewById(R.id.tvExpenseName);
                tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
                tvExpenseDate = itemView.findViewById(R.id.tvExpenseDate);
            }
        }
    }
}
