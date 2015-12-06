package org.islamic.civil.quran_karim.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bluejamesbond.text.DocumentView;

import org.islamic.civil.quran_karim.R;
import org.islamic.civil.quran_karim.data.QuranMetaDataHelper;
import org.islamic.civil.quran_karim.fragment.adapter.QuranListAdapter;
import org.islamic.civil.util.text.SearchUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link QuranPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuranPageFragment extends Fragment
        implements SearchView.OnQueryTextListener, DialogInterface.OnClickListener {

    private static final String TAB_POSITION = "tab_position";

    private OnFragmentInteractionListener mListener;

    public static QuranPageFragment newInstance(int tabPosition) {
        QuranPageFragment fragment = new QuranPageFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_POSITION, tabPosition);
        fragment.setArguments(args);
        return fragment;
    }

    public QuranPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabPosition = getArguments().getInt(TAB_POSITION);
        }
    }

    int tabPosition;
    RecyclerView mRecyclerView;
    QuranListAdapter mAdapter;
    List<QuranListAdapter.QuranSuraModel> mModels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quran_page, container, false);
        setHasOptionsMenu(true);

        DocumentView dv = (DocumentView) view.findViewById(R.id.documentView);
        dv.setText(loadModelDataS());
        dv.getDocumentLayoutParams().setReverse(true);

        Bundle args = getArguments();
        tabPosition = args.getInt(TAB_POSITION);
//        tv.setText("Text in Tab #" + tabPosition);

        return view;
    }

    String loadModelDataS(){
        SQLiteDatabase db = new QuranMetaDataHelper(getActivity()).getReadableDatabase();
        Cursor c = db
                .query("quran_text", new String[]{"`text`"}, null, null, null, null, null, "60");
        String text = "";
        while(c.moveToNext())
            text += c.getString(0);
        c.close();
        return text;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    void loadModelData(){
        mModels = new ArrayList<>();
        short i=0;
        try {
            SQLiteDatabase db = new QuranMetaDataHelper(getActivity()).getReadableDatabase();
            Cursor c = db.rawQuery("SELECT `start`, `ayas`, `order`, `name`,`tname`,`type` FROM quran_data_sura;",null);
//            db.query("quran_data_sura",
//                    new String[]{"start", "ayas", "order", "name", "tname", "type"},
//                    "*", null, null, null, null);

            while(c.moveToNext()){
                if(c.getShort(1)<=1)
                    break;
                QuranListAdapter.QuranSuraModel sura = new QuranListAdapter.QuranSuraModel();
                sura.index = ++i;
                sura.start = c.getInt(0);
                sura.ayas = c.getShort(1);
                sura.order = c.getShort(2);
                sura.name = c.getString(3);
                sura.tname = c.getString(4);
                sura.medinan = c.getInt(5) == 1;
                mModels.add(sura);
            }
            c.close();
            db.close();
        } catch (Exception e){
            e.printStackTrace();
            mModels.clear();
        }

        if(mModels.size() == 0) {
            String[] sura = getActivity().getResources().getStringArray(R.array.sura_names);
            for (String name :
                    sura) {
                mModels.add(new QuranListAdapter.QuranSuraModel(name, null, ++i));
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = SearchUtils.arabicSimplify4AdvancedSearch(newText);
        final List<QuranListAdapter.QuranSuraModel> filteredModelList = filter(mModels, text);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    private List<QuranListAdapter.QuranSuraModel> filter(List<QuranListAdapter.QuranSuraModel> models, String query) {
        query = query.toLowerCase();

        final List<QuranListAdapter.QuranSuraModel> filteredModelList = new ArrayList<>();
        for (QuranListAdapter.QuranSuraModel model : models) {
            String text = model.name;//.toLowerCase()
            if (text.contains(query)|| (model.tname != null && (model.tname.toLowerCase()).contains(query)))
                filteredModelList.add(model);
        }
        return filteredModelList;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.quran_list, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_sort:
                new AlertDialog.Builder(getActivity())
                        .setSingleChoiceItems(R.array.quranSortType,mAdapter.sortOrder,this)
                        .create()
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        mAdapter.sort(i);
        dialogInterface.dismiss();
    }
}
