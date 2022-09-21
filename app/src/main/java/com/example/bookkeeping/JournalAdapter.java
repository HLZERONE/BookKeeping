package com.example.bookkeeping;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookkeeping.fragment.HomeFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder>{

    List<Journal> journals;
    Context context;

    public JournalAdapter(Context context, List<Journal> journals){
        this.context = context;
        this.journals = journals;
    }

    @NonNull
    @Override
    public JournalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.journal_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalAdapter.ViewHolder holder, int position) {
        holder.Bind(journals.get(position));
    }

    @Override
    public int getItemCount() {
        return journals.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView journalIcon;
        TextView categoryName;
        TextView journalComment;
        TextView journalDate;
        TextView journalAmount;
        Journal journal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //assign all the views
            journalIcon = itemView.findViewById(R.id.journalIcon);
            categoryName = itemView.findViewById(R.id.categoryName);
            journalComment = itemView.findViewById(R.id.journalComment);
            journalDate = itemView.findViewById(R.id.journalDate);
            journalAmount = itemView.findViewById(R.id.journalAmount);
            journal = null;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void Bind(Journal journal){
            this.journal = journal;
            //bind categoryName and journalComment
            categoryName.setText(journal.getCategory());
            journalComment.setText(journal.getComment());

            //bind journalDate
            SimpleDateFormat journalDateFormatter = new SimpleDateFormat("MMM dd - yyyy");
            journalDate.setText(journalDateFormatter.format(journal.getDate()));

            //bind journalAmount and set background color
            //isExpense
            if(journal.getSaveAmount() == 0){
                itemView.setBackgroundResource(R.color.yellowBackground);
                journalAmount.setText(String.format("$ %.2f", journal.getExpenseAmount()));
            }else{
                itemView.setBackgroundResource(R.color.teaGreenBackground);
                journalAmount.setText(String.format("$ %.2f", journal.getSaveAmount()));
            }

            //bind journalIcon
            journalIcon.setImageResource(journal.getImageId());
        }

        //EDIT journal
        @Override
        public void onClick(View view) {
            if(journal == null) return;

            Intent intent = new Intent(itemView.getContext(), AddActivity.class);
            if(journal.getSaveAmount() == 0){
                intent.putExtra(HomeFragment.isExpense, true);
                intent.putExtra("amount", journal.getExpenseAmount());
            }else{
                intent.putExtra("isExpense", false);
                intent.putExtra("amount", journal.getSaveAmount());
            }
            intent.putExtra(HomeFragment.NewJournal, false);
            intent.putExtra("id", journal.getId());
            intent.putExtra("comment", journal.getComment());
            intent.putExtra("date", journal.getDate());
            intent.putExtra("category", journal.getCategory());

            itemView.getContext().startActivity(intent);
        }

        //DELETE journal
        @Override
        public boolean onLongClick(View view) {
            //pop up an alert window
            new AlertDialog.Builder(view.getContext())
                    .setIcon(R.drawable.warning)
                    .setTitle("Are you sure?")
                    .setMessage("Do you definitely want to delete this journal?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            JournalDatabase journalDatabase = new JournalDatabase(view.getContext());
                            journalDatabase.deleteJournalData(journal.getId());
                            journals.remove(journals.indexOf(journal));
                            notifyDataSetChanged();

                            //update View
                            HomeFragment.updateAmount(journalDatabase);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

            return true;
        }
    }
}
