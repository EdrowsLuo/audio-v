package com.edlplan.audiov.ui;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.edlplan.audiov.R;

import java.util.ArrayList;
import java.util.List;

public class OperationDialog extends Dialog {

    public OperationDialog(@NonNull Context context) {
        super(context, R.style.Theme_Design_BottomSheetDialog);
        setContentView(R.layout.operation_dialog);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public OperationBuilder operationBuilder() {
        return new OperationBuilder();
    }

    public class OperationBuilder {

        private List<OperationNode> operationNodes = new ArrayList<>();

        public OperationBuilder addOperation(String name, Runnable runnable) {
            OperationNode node = new OperationNode();
            node.name = name;
            node.runnable = runnable;
            operationNodes.add(node);
            return this;
        }

        public void build() {
            RecyclerView recyclerView = findViewById(R.id.body_list);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

            recyclerView.setLayoutManager(layoutManager);

            layoutManager.setOrientation(OrientationHelper.VERTICAL);

            layoutManager.setSmoothScrollbarEnabled(true);

            recyclerView.setAdapter(new ListAdapter(operationNodes));
        }

    }

    private static class EntryHolder extends RecyclerView.ViewHolder {

        private TextView name;

        private View button;

        private View body;

        public EntryHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.entry_name);
            button = itemView.findViewById(R.id.more_details);
            body = itemView.findViewById(R.id.list_entry_body);
        }
    }

    private static class ListAdapter extends RecyclerView.Adapter<EntryHolder> {

        private List<OperationNode> operationNodes;

        public ListAdapter(List<OperationNode> operationNodes) {
            this.operationNodes = operationNodes;
        }

        @NonNull
        @Override
        public EntryHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new EntryHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.entry_list_entry, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull EntryHolder entryHolder, int i) {
            entryHolder.name.setText(operationNodes.get(i).name);
            entryHolder.body.setOnClickListener(v -> operationNodes.get(i).runnable.run());
        }

        @Override
        public int getItemCount() {
            return operationNodes.size();
        }

    }


    private static class OperationNode {

        private String name;

        private Runnable runnable;

    }
}
