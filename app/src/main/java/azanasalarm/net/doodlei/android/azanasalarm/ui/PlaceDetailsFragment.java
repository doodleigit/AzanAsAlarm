package azanasalarm.net.doodlei.android.azanasalarm.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.adapter.PlacePhotoAdapter;
import azanasalarm.net.doodlei.android.azanasalarm.model.MarkerInfo;
import azanasalarm.net.doodlei.android.azanasalarm.model.PlaceDetails;
import azanasalarm.net.doodlei.android.azanasalarm.util.AppContants;

public class PlaceDetailsFragment extends BottomSheetDialogFragment {

    View view;
    private Context context;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ViewPager viewpager;
    private TextView tvName, tvRating, tvReview, tvPlaceType, tvAddress, tvPhoneNumber, tvOpeningHour, tvWebsite, tvDirection, tvCall;

    private PlacePhotoAdapter placePhotoAdapter;

    private MarkerInfo originMarkerInfo, destinationMarkerInfo;
    private PlaceDetails placeDetails;

    private String placeId = "";

    public static PlaceDetailsFragment newInstance(String placeId, MarkerInfo originMarkerInfo, MarkerInfo destinationMarkerInfo) {
        Bundle args = new Bundle();
        args.putString("place_id", placeId);
        args.putParcelable("origin_marker_info", originMarkerInfo);
        args.putParcelable("destination_marker_info", destinationMarkerInfo);

        PlaceDetailsFragment fragment = new PlaceDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.place_details_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle argument = getArguments();
        if (argument != null) {
            //   reasonList = argument.getParcelableArrayList(MESSAGE_key);
            placeId = argument.getString("place_id");
            originMarkerInfo = argument.getParcelable("origin_marker_info");
            destinationMarkerInfo = argument.getParcelable("destination_marker_info");
        }

//        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(placeId);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar);
        viewpager = view.findViewById(R.id.viewpager);
        tvName = view.findViewById(R.id.name);
        tvRating = view.findViewById(R.id.rating);
        tvReview = view.findViewById(R.id.review);
        tvPlaceType = view.findViewById(R.id.placeType);
        tvAddress = view.findViewById(R.id.address);
        tvPhoneNumber = view.findViewById(R.id.phoneNumber);
        tvOpeningHour = view.findViewById(R.id.openingHour);
        tvWebsite = view.findViewById(R.id.websiteUrl);
        tvDirection = view.findViewById(R.id.direction);
        tvCall = view.findViewById(R.id.call);
        viewpager.setClipToPadding(false);
        viewpager.setPadding(0, 0, 40, 0);
        viewpager.setPageMargin(10);

        getPlaceDetails(placeId);
        implementClickListener();
    }

    private void getSetData() {
        placePhotoAdapter = new PlacePhotoAdapter(context, placeDetails.getPhotos());
        viewpager.setAdapter(placePhotoAdapter);

        if (placeDetails.getPhotos().size() == 0) {
            viewpager.setVisibility(View.GONE);
        }

        tvCall.setVisibility(placeDetails.getPhoneNumber().isEmpty() ? View.INVISIBLE : View.VISIBLE);

        tvName.setText(placeDetails.getName());
        tvRating.setText(placeDetails.getRating());
        tvReview.setText(placeDetails.getReview().isEmpty() ? "" : getString(R.string.custom_review, placeDetails.getReview()));
        tvPlaceType.setText(getString(R.string.custom_place_type, placeDetails.getPlaceType(), placeDetails.getTimeDistance()));
        tvAddress.setText(placeDetails.getAddress());
        tvPhoneNumber.setText(placeDetails.getPhoneNumber().isEmpty() ? getString(R.string.none) : placeDetails.getPhoneNumber());
        tvOpeningHour.setText(placeDetails.isOpeningHour()? getString(R.string.opened) : getString(R.string.closed));
        tvWebsite.setText(placeDetails.getWebsiteUrl().isEmpty() ? getString(R.string.none) : placeDetails.getWebsiteUrl());
    }

    private void implementClickListener() {
        tvDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDirectionClick();
            }
        });

        tvCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", placeDetails.getPhoneNumber(), null));
                startActivity(intent);
            }
        });
    }

    private void handleDirectionClick() {
        Intent intent = new Intent(context, PlaceDirectionActivity.class);
        intent.putExtra("origin", originMarkerInfo);
        intent.putExtra("destination", destinationMarkerInfo);
        startActivity(intent);
    }

    private void getPlaceDetails(String placeId) {
        String url = AppContants.googlePlaceDetailsApi + "place_id=" + placeId + "&fields=" + "name,photo,formatted_address,rating,user_ratings_total,formatted_phone_number,opening_hours,website,geometry" + "&key=" + getString(R.string.google_maps_key);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("OK")) {
                        JSONObject object = jsonObject.getJSONObject("result");
                        String name, rating, review, placeType, timeDistance, phoneNumber, address, websiteUrl;
                        boolean openingHour;
                        LatLng latLng;
                        ArrayList<String> photos = new ArrayList<>();
                        double lat, lng;
                        lat = object.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        lng = object.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        latLng = new LatLng(lat, lng);
                        name = jsonValidator(object, "name");
                        rating = jsonValidator(object, "rating");
                        review = jsonValidator(object, "user_ratings_total");
                        placeType = "Mosque";
                        timeDistance = "0.5"; //String.valueOf(SphericalUtil.computeDistanceBetween(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), latLng));
                        phoneNumber = jsonValidator(object, "formatted_phone_number");
                        address = jsonValidator(object, "formatted_address");
                        openingHour = jsonObjectValidator(object, "opening_hours", "open_now").isEmpty();
                        websiteUrl = jsonValidator(object, "website");

                        if (object.has("photos")) {
                            JSONArray array = object.getJSONArray("photos");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                String photo = jsonValidator(obj, "photo_reference");
                                photos.add(photo);
                            }
                        }
                        placeDetails = new PlaceDetails(latLng, photos, name, rating, review, placeType, timeDistance, phoneNumber, address, openingHour, websiteUrl);
                        getSetData();
                    } else {
                        //SnakeBar not found alertl.
                        showAlertMessage(getString(R.string.an_error_occurred_while_searching_for_mosque_near_you));
                    }

                } catch (JSONException e) {
                    //SnakeBar data error
                    showAlertMessage(getString(R.string.an_error_occurred_while_searching_for_mosque_near_you));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showAlertMessage(getString(R.string.server_is_not_responding));
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.getCache().clear();
        requestQueue.add(stringRequest);
    }

    private void showAlertMessage(String message) {
        Snackbar snackbar = Snackbar
                .make(view.findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getPlaceDetails(placeId);
                    }
                });

        snackbar.show();
    }

    private String jsonObjectValidator(JSONObject object, String objectKey, String key) {
        try {
            if (object.has(objectKey)) {
                return jsonValidator(object.getJSONObject(objectKey), key);
            } else {
                return "";
            }
        } catch (JSONException e) {
            return "";
        }
    }

    private String jsonValidator(JSONObject object, String key) {
        try {
            if (object.has(key)) {
                return object.getString(key);
            } else {
                return "";
            }
        } catch (JSONException e) {
            return "";
        }
    }

}
