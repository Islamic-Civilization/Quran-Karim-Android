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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.islamic.civil.quran_karim.R;
import org.islamic.civil.quran_karim.data.QuranMetaDataHelper;
import org.islamic.civil.quran_karim.data.model.QuranJuzHizbModel;
import org.islamic.civil.quran_karim.data.model.QuranSuraModel;
import org.islamic.civil.quran_karim.fragment.adapter.QuranJuzHizbAdapter;
import org.islamic.civil.quran_karim.fragment.adapter.QuranListAdapter;
import org.islamic.civil.util.text.SearchUtils;
import org.islamic.civil.util.view.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link QuranJuzListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuranJuzListFragment extends Fragment
        implements SearchView.OnQueryTextListener, DialogInterface.OnClickListener {

    private static final String TAB_POSITION = "tab_position";

    private OnFragmentInteractionListener mListener;

    public static QuranJuzListFragment newInstance(int tabPosition) {
        QuranJuzListFragment fragment = new QuranJuzListFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_POSITION, tabPosition);
        fragment.setArguments(args);
        return fragment;
    }

    public QuranJuzListFragment() {
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
    QuranJuzHizbAdapter mAdapter;
    List<QuranJuzHizbModel> mModels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quran_list, container, false);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        tabPosition = args.getInt(TAB_POSITION);
//        tv.setText("Text in Tab #" + tabPosition);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        loadModelData();

        mAdapter = new QuranJuzHizbAdapter(getActivity(),mModels);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        return view;
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
        mModels = QuranMetaDataHelper.getInstance(getActivity()).getJuzHizbModel();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        return true;
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
//                new AlertDialog.Builder(getActivity())
//                        .setSingleChoiceItems(R.array.quranSortType,mAdapter.sortOrder,this)
//                        .create()
//                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
    }
}
