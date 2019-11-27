package com.example.studymate.ui.floor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FloorViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FloorViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is floor fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}