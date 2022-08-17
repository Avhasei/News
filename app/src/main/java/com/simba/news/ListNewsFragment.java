package com.simba.news;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListNewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListNewsFragment extends Fragment implements RecyclerAdapter.ItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerAdapter recyclerAdapter;
    private View rootView;
    private FloatingActionButton refreshBtn;
    private SearchView searchView;
    private JSONArray defaultArray;

    private static OkHttpClient okHttpClient;
    ProgressDialog progress;

    public ListNewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListNewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListNewsFragment newInstance(String param1, String param2) {
        ListNewsFragment fragment = new ListNewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_list_news, container, false);

        okHttpClient = new OkHttpClient();
        progress = new ProgressDialog(getActivity());
        searchView = rootView.findViewById(R.id.btnSearch);
        refreshBtn = rootView.findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new doAPICalls().execute();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
        new doAPICalls().execute();
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        System.exit(1);
    }

    @Override
    public void onItemClick(View view, int position) {
        try {
            JSONObject jsonObject = recyclerAdapter.getItem(position);
            Bundle bundle = new Bundle();
            bundle.putString("pageUrl", jsonObject.getString("url"));

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(bundle);
            AppCompatActivity activity = (AppCompatActivity) getContext();
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container_view, detailFragment, detailFragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void filter(String text){
            try {
                JSONArray searchedFor = new JSONArray();
                JSONArray toBeFiltered = defaultArray;
                for(int i=0;i<toBeFiltered.length();i++){
                    JSONObject currentObject = toBeFiltered.optJSONObject(i);
                    if(currentObject.getString("title").toLowerCase().contains(text.toLowerCase())){
                        searchedFor.put(currentObject);
                    }
                }
                recyclerAdapter.setObject(searchedFor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    private class doAPICalls extends AsyncTask<Void,String,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setTitle("Loading");
            progress.setMessage("Wait while loading...");
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            Request request = new Request.Builder()
                    .url("http://api.mediastack.com/v1/news?access_key=effa275193911939d4e3a0d937311ef0&sources=cnn&languages=en&sort=published_desc")
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback(){
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        String responseString = response.body().string();
                        JSONObject responseStringObj = new JSONObject(responseString);
                        publishProgress(responseStringObj.getJSONArray("data").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    progress.dismiss();
                    Log.d("okHttpClient",e.toString());
                }
            });
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            try {
                defaultArray = new JSONArray(values[0]);

                RecyclerView recyclerView = rootView.findViewById(R.id.recycleView);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerAdapter = new RecyclerAdapter(getContext(), defaultArray);
                recyclerAdapter.setClickListener(ListNewsFragment.this);
                recyclerView.setAdapter(recyclerAdapter);
                progress.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}