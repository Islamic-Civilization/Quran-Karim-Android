package org.islamic.civil.quran_karim.fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.islamic.civil.quran_karim.QuranActivity;
import org.islamic.civil.quran_karim.R;
import org.islamic.civil.quran_karim.data.model.QuranSuraModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TODO add fast scroll
 * TODO add section
 * TODO add swype or longPress
 */
public class QuranListAdapter extends RecyclerView.Adapter<QuranListAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public QuranSuraModel model;
        public TextView name,tname,index,page;
        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            tname = (TextView) itemView.findViewById(R.id.tname);
            index = (TextView) itemView.findViewById(R.id.index);
            page = (TextView) itemView.findViewById(R.id.page);
            itemView.setTag(this);
            itemView.setOnClickListener(this);
        }

        public void bind(QuranSuraModel model){
            this.model = model;
            name.setText(model.name);
            tname.setText(model.tname);
            index.setText(Short.toString(model.sura));
            page.setText(Short.toString((short) 123));
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), QuranActivity.class);
            intent.setAction(QuranActivity.StartSura);
            intent.putExtra(QuranActivity.SuraStart, model.start);
            intent.putExtra(QuranActivity.SuraIndex, model.sura);
            intent.putExtra(QuranActivity.SuraAya, model.ayas);
            intent.putExtra(QuranActivity.Title,model.name);
            view.getContext().startActivity(intent);
        }
    }

    private final LayoutInflater mInflater;
    private List<QuranSuraModel> mModels;

    public QuranListAdapter(Context context, List<QuranSuraModel> models) {
        mInflater = LayoutInflater.from(context);
        mModels = new ArrayList<>(models);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.adapter_sura_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        QuranSuraModel model = mModels.get(position);
        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }

    public void animateTo(List<QuranSuraModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<QuranSuraModel> newModels) {
        for (int i = mModels.size() - 1; i >= 0; i--) {
            final QuranSuraModel model = mModels.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<QuranSuraModel> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final QuranSuraModel model = newModels.get(i);
            if (!mModels.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<QuranSuraModel> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final QuranSuraModel model = newModels.get(toPosition);
            final int fromPosition = mModels.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public QuranSuraModel removeItem(int position) {
        final QuranSuraModel model = mModels.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, QuranSuraModel model) {
        mModels.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final QuranSuraModel model = mModels.remove(fromPosition);
        mModels.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void flushItem(List<QuranSuraModel> models){
        this.mModels = models;
        notifyDataSetChanged();
    }

    public int sortOrder = 0;
    public void sort(int order){
        sortOrder = order;
        Comparator<? super QuranSuraModel> cmp = null;
        switch (order){
            default:
            case 0:
                cmp = new QuranSuraModel.IndexComparator();
                break;
            case 1:
                cmp = new QuranSuraModel.OrderComparator();
                break;
            case 2:
                cmp = new QuranSuraModel.NameComparator();
                break;
            case 3:
                cmp = new QuranSuraModel.AyaComparator();
        }
        Collections.sort(mModels,cmp);
        notifyDataSetChanged();
    }
}
