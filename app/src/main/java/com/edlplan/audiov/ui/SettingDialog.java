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
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.edlplan.audiov.GlobalVar;
import com.edlplan.audiov.R;
import com.edlplan.audiov.core.option.IHasOption;
import com.edlplan.audiov.core.option.OptionEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingDialog extends Dialog {

    private IHasOption option;

    public SettingDialog(@NonNull Context context, String title, IHasOption option) {
        super(context, R.style.Theme_Design_BottomSheetDialog);
        this.option = option;
        setContentView(R.layout.setting_dialog);
        setTitle(title);

        RecyclerView recyclerView = findViewById(R.id.body_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(layoutManager);

        layoutManager.setOrientation(OrientationHelper.VERTICAL);

        layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setAdapter(new ListAdapter(option.dumpOptions()));
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    private static class EntryHolder extends RecyclerView.ViewHolder {

        private TextView name, description;

        private Switch aSwitch;

        private Button edit;

        public EntryHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            aSwitch = itemView.findViewById(R.id.switch1);
            edit = itemView.findViewById(R.id.edit);
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<EntryHolder> {

        private List<OptionEntry<?>> optionEntries;

        private Map<String, OptionEntry<?>> optionEntryMap;

        public ListAdapter(Map<String, OptionEntry<?>> optionEntryMap) {
            this.optionEntryMap = optionEntryMap;
            optionEntries = new ArrayList<>(optionEntryMap.values());
        }

        @NonNull
        @Override
        public EntryHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new EntryHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.setting_entry, viewGroup, false));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onBindViewHolder(@NonNull EntryHolder entryHolder, int i) {
            OptionEntry<?> entry = optionEntries.get(i);
            entryHolder.name.setText(GlobalVar.parseValue(entry.getName()));
            entryHolder.aSwitch.setVisibility(View.GONE);
            entryHolder.edit.setVisibility(View.GONE);
            entryHolder.aSwitch.setVisibility(View.GONE);
            entryHolder.edit.setVisibility(View.GONE);
            if (entry.getDescription() == null) {
                entryHolder.description.setVisibility(View.GONE);
            } else {
                entryHolder.description.setVisibility(View.VISIBLE);
                entryHolder.description.setText(GlobalVar.parseValue(entry.getDescription()));
            }
            if (entry.getData() != null) {
                if (entry.getData() instanceof Boolean) {
                    entryHolder.aSwitch.setVisibility(View.VISIBLE);
                    entryHolder.aSwitch.setChecked((Boolean) entry.getData());
                    entryHolder.aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        ((OptionEntry<Boolean>) entry).setData(isChecked);
                        option.applyOptions(optionEntryMap);
                    });
                } else {
                    entryHolder.edit.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return optionEntries.size();
        }

    }
}

