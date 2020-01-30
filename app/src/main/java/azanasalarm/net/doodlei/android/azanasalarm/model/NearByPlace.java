package azanasalarm.net.doodlei.android.azanasalarm.model;

import com.google.android.gms.maps.model.LatLng;

public class NearByPlace {

    private LatLng latLng;
    private String id, name, icon, vicinity, rating, review, placeType, distance, phoneNumber;

    public NearByPlace(LatLng latLng, String id, String name, String icon, String vicinity, String rating, String review, String placeType, String distance, String phoneNumber) {
        this.latLng = latLng;
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.vicinity = vicinity;
        this.rating = rating;
        this.review = review;
        this.placeType = placeType;
        this.distance = distance;
        this.phoneNumber = phoneNumber;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
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

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
