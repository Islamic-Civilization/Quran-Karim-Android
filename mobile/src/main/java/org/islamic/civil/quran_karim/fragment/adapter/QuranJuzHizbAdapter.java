package org.islamic.civil.quran_karim.fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.islamic.civil.quran_karim.QuranActivity;
import org.islamic.civil.quran_karim.R;
import org.islamic.civil.quran_karim.data.model.QuranJuzHizbModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amirhakh on 11/12/2015.
 */
public class QuranJuzHizbAdapter extends RecyclerView.Adapter<QuranJuzHizbAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public QuranJuzHizbModel model;
        public TextView name,index,page,tname;
        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            tname = (TextView) itemView.findViewById(R.id.tname);
            tname.setVisibility(View.GONE);
            index = (TextView) itemView.findViewById(R.id.index);
            page = (TextView) itemView.findViewById(R.id.page);
            itemView.setTag(this);
            itemView.setOnClickListener(this);
        }

        public void bind(QuranJuzHizbModel model){
            this.model = model;
            index.setText(Short.toString(model.index));
            name.setText(model.title);
            page.setText(Short.toString((short) 123));
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), QuranActivity.class);
            intent.setAction(QuranActivity.StartHizb);
            intent.putExtra(QuranActivity.StartHizb, model.start);
            intent.putExtra(QuranActivity.SuraIndex, model.sura);
            intent.putExtra(QuranActivity.SuraAya, model.aya);
            intent.putExtra(QuranActivity.Hizb, model.index);
            intent.putExtra(QuranActivity.Title,model.title);
            view.getContext().startActivity(intent);
        }
    }

    private final LayoutInflater mInflater;
    private List<QuranJuzHizbModel> mModels;

    public QuranJuzHizbAdapter(Context context, List<QuranJuzHizbModel> models) {
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
        QuranJuzHizbModel model = mModels.get(position);
        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }
}
