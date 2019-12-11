package com.example.studymate.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.studymate.GeneralFunctions;
import com.example.studymate.R;
import com.example.studymate.SearchResultData;

import java.util.LinkedList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        SearchView searchView = root.findViewById(R.id.searchView);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(true);

        SearchResultData[] importedData;

        importedData = GeneralFunctions.pullUsersFromDatabase();

        searchView.setOnSearchClickListener(unused -> {
            String query = (String) searchView.getQuery();
            loadSearches(search(query, importedData));
        });
        return root;
    }

    private List<SearchResultData> search(String query, SearchResultData[] userStudyingInfo) {
        if (userStudyingInfo == null || query == null || query == "") {
            return null;
        }
        String[] keyWords = query.split(" ");
        List<SearchResultData> toReturn = new LinkedList<SearchResultData>();
        for (SearchResultData srd : userStudyingInfo) {
            boolean contains = true;
            for (String keyWord : keyWords) {
                if (!srd.getStudyingContent().contains(keyWord)) {
                    contains = false;
                }
            }
            if (contains) {
                toReturn.add(srd);
            }
        }
        return toReturn;
    }

    private void loadSearches(List<SearchResultData> matchingUsers) {

    }
}