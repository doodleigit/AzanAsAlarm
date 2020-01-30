package azanasalarm.net.doodlei.android.azanasalarm.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.interfaces.OnPlaceClick;
import azanasalarm.net.doodlei.android.azanasalarm.model.NearByPlace;

public class PlacePagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<NearByPlace> arrayList;
    private LayoutInflater inflater;
    private OnPlaceClick onPlaceClick;

    public PlacePagerAdapter(Context context, ArrayList<NearByPlace> arrayList, OnPlaceClick onPlaceClick) {
        this.context = context;
        this.arrayList = arrayList;
        this.onPlaceClick = onPlaceClick;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View view = inflater.inflate(R.layout.place_pager_item, container, false);
        TextView tvName, tvRating, tvReview, tvPlaceType, tvDirection, tvCall;
        tvName = view.findViewById(R.id.name);
        tvRating = view.findViewById(R.id.rating);
        tvReview = view.findViewById(R.id.review);
        tvPlaceType = view.findViewById(R.id.placeType);
        tvDirection = view.findViewById(R.id.direction);
        tvCall = view.findViewById(R.id.call);

        tvName.setText(arrayList.get(position).getName());
        tvRating.setText(arrayList.get(position).getRating());
        tvReview.setText(arrayList.get(position).getReview().isEmpty() ? "" : context.getString(R.string.custom_review, arrayList.get(position).getReview()));
        tvPlaceType.setText(context.getString(R.string.custom_place_type, arrayList.get(position).getPlaceType(), getDistanceInMiles(arrayList.get(position).getDistance())));

        tvCall.setVisibility(arrayList.get(position).getPhoneNumber().isEmpty() ? View.INVISIBLE : View.VISIBLE);

        tvDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlaceClick.onDirectionClick();
            }
        });

        tvCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlaceClick.onCallClick(arrayList.get(position).getPhoneNumber());
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlaceClick.onPlaceClick(arrayList.get(position).getId());
            }
        });

        container.addView(view, 0);
        return view;
    }

    private String getDistanceInMiles(String distance) {
        double distanceInMiles = Double.parseDouble(distance) / 1609.344;
        return String.valueOf(new DecimalFormat("##.##").format(distanceInMiles));
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }
}
