package com.example.studymate;

import android.os.Parcel;
import android.os.Parcelable;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.maps.model.LatLng;

public class SearchResultData implements Parcelable {

    private String studyingContent;
    private String email;
    private String library;
    private int floor;
    private int searchQueryNumber;
    private double seatingLatitude;
    private double seatingLongitude;


    public SearchResultData(Parcel in) {
        studyingContent = in.readString();
        email = in.readString();
        library = in.readString();
        searchQueryNumber = in.readInt();
        floor = in.readInt();
        seatingLatitude = in.readDouble();
        seatingLongitude = in.readDouble();
    }
    public SearchResultData() { }

    public SearchResultData(String setStudyingContent,
                            String setEmail,
                            String setLibrary,
                            int setFloor,
                            double setSeatingLatitude,
                            double setSeatingLongitude) {
        this.studyingContent = setStudyingContent;
        this.email = setEmail;
        this.library = setLibrary;
        searchQueryNumber = setEmail.hashCode();
        this.floor = setFloor;
        this.seatingLatitude = setSeatingLatitude;
        this.seatingLongitude = setSeatingLongitude;
    }

    /**
     * Define the kind of object that you gonna parcel,
     * You can use hashCode() here
     */
    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(studyingContent);
        dest.writeString(email);
        dest.writeString(library);
        dest.writeInt(searchQueryNumber);
        dest.writeInt(floor);
        dest.writeDouble(seatingLatitude);
        dest.writeDouble(seatingLongitude);
    }

    /**
     * This field is needed for Android to be able to
     * create new objects, individually or as arrays
     *
     * If you don’t do that, Android framework will raises an exception
     * Parcelable protocol requires a Parcelable.Creator object
     * called CREATOR
     */
    public static final Parcelable.Creator<SearchResultData> CREATOR = new Parcelable.Creator<SearchResultData>() {
        public SearchResultData createFromParcel(Parcel in) {
            return new SearchResultData(in);
        }

        public SearchResultData[] newArray(int size) {
            return new SearchResultData[size];
        }
    };

    public String getEmail() {
        return email;
    }
    public String getLibrary() {
        return library;
    }
    public String getStudyingContent() {
        return studyingContent;
    }
    public int getSearchQueryNumber() {
        return searchQueryNumber;
    }
    public int getFloor() {
        return floor;
    }

    public LatLng getSeatingLatLng() {
        return new LatLng(seatingLatitude, seatingLongitude);
    }
}
