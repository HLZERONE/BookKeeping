package com.example.bookkeeping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView journalIcon;
        TextView categoryName;
        TextView journalComment;
        TextView journalDate;
        TextView journalAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //assign all the views
            journalIcon = itemView.findViewById(R.id.journalIcon);
            categoryName = itemView.findViewById(R.id.categoryName);
            journalComment = itemView.findViewById(R.id.journalComment);
            journalDate = itemView.findViewById(R.id.journalDate);
            journalAmount = itemView.findViewById(R.id.journalAmount);
        }

        public void Bind(Journal journal){
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
    }
}
