package azanasalarm.net.doodlei.android.azanasalarm.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PlaceDetails {

    private String name, rating, review, placeType, timeDistance, phoneNumber, address, websiteUrl;
    private boolean openingHour;
    private LatLng latLng;
    private ArrayList<String> photos;

    public PlaceDetails(LatLng latLng, ArrayList<String> photos, String name, String rating, String review, String placeType, String timeDistance, String phoneNumber, String address, boolean openingHour, String websiteUrl) {
        this.latLng = latLng;
        this.photos = photos;
        this.name = name;
        this.rating = rating;
        this.review = review;
        this.placeType = placeType;
        this.timeDistance = timeDistance;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.openingHour = openingHour;
        this.websiteUrl = websiteUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public String getTimeDistance() {
        return timeDistance;
    }

    public void setTimeDistance(String timeDistance) {
        this.timeDistance = timeDistance;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(boolean openingHour) {
        this.openingHour = openingHour;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<String> photos) {
        this.photos = photos;
    }
}
