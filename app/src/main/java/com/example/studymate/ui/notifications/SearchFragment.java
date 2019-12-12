package com.example.studymate.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.studymate.GeneralFunctions;
import com.example.studymate.MainScreenActivity;
import com.example.studymate.R;
import com.example.studymate.SearchResultData;
import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;

    private SearchResultData[] importedData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        SearchView searchView = root.findViewById(R.id.searchView);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(true);

        importedData = GeneralFunctions.pullUsersFromDatabase();

        SearchResultData samplePerson = new SearchResultData("Calculus 2",
                                                            "testEmail@gmail.com",
                                                            "grainger",
                                                            0,
                                                            1,
                                                            85, -45);

        importedData = new SearchResultData[]{samplePerson};

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadSearches(search(query, importedData), root);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }


        });
        return root;
    }

    private List<SearchResultData> search(String query, SearchResultData[] userStudyingInfo) {
        if (userStudyingInfo == null || query == null || query == "") {
            return null;
        }
        query.toLowerCase();
        String[] keyWords = query.split(" ");
        List<SearchResultData> toReturn = new LinkedList<SearchResultData>();
        for (SearchResultData srd : userStudyingInfo) {
            boolean contains = true;
            for (String keyWord : keyWords) {
                if (!srd.getStudyingContent().toLowerCase().contains(keyWord)) {
                    contains = false;
                }
            }
            if (contains) {
                toReturn.add(srd);
            }
        }
        return toReturn;
    }

    private void loadSearches(List<SearchResultData> matchingUsers, View root) {
        SearchResultAdapter adapter = new SearchResultAdapter(getContext(), matchingUsers);
        ListView itemsListView = (ListView) root.findViewById(R.id.searchResultPresenter);
        itemsListView.setAdapter(adapter);
    }

    private void resetSearches(View root) {
        ListView itemsListView = (ListView) root.findViewById(R.id.searchResultPresenter);
        itemsListView.clearChoices();
    }

    public class SearchResultAdapter extends BaseAdapter {
        private Context context;
        private List<SearchResultData> items;

        public SearchResultAdapter(Context setContext, List<SearchResultData> setItems) {
            this.context = setContext;
            this.items = setItems;
        }


        @Override
        public int getCount() {
            return items.size(); //returns total of items in the list
        }

        @Override
        public Object getItem(int position) {
            return items.get(position); //returns list item at the specified position
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // inflate the layout for each list row
            if (convertView == null) {
                convertView = LayoutInflater.from(context).
                        inflate(R.layout.search_element, parent, false);
            }

            // get current item to be displayed
            SearchResultData currentItem = (SearchResultData) getItem(position);

            TextView emailText = convertView.findViewById(R.id.emailText);
            emailText.setText(currentItem.getEmail());

            TextView contentStudying = convertView.findViewById(R.id.contentText);
            contentStudying.setText(currentItem.getStudyingContent());

            TextView library = convertView.findViewById(R.id.libraryText);
            library.setText("Library: " + GeneralFunctions.capitalize(currentItem.getLibrary()));

            TextView floor = convertView.findViewById(R.id.floorText);
            floor.setText("Floor: " + currentItem.getFloor());

            Button switchToMap = convertView.findViewById(R.id.switchToMapButton);
            switchToMap.setOnClickListener(unused -> {
                switchToMap(currentItem.getLibrary(), currentItem.getFloor(), currentItem.getSeatingLatLng());
            });
            
            Button sendUserMessage = convertView.findViewById(R.id.messageUserButton);
            sendUserMessage.setOnClickListener(unused -> {
                showMessagePopup();
            });

            // returns the view for the current row
            return convertView;
        }

        private void showMessagePopup() {
        }
    }

    private void switchToMap(String library, int floor, LatLng seatingLatLng) {
        MainScreenActivity activity = (MainScreenActivity) getActivity();
        activity.switchToFloor(library, floor, "search");
    }
}