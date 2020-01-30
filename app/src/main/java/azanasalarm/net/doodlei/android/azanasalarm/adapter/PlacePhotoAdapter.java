package azanasalarm.net.doodlei.android.azanasalarm.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.util.AppContants;

public class PlacePhotoAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> arrayList;
    private LayoutInflater inflater;

    public PlacePhotoAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View imageLayout = inflater.inflate(R.layout.place_photo_item, container, false);
        ImageView ivPhoto = imageLayout.findViewById(R.id.photo);
        String photoUrl = AppContants.googlePlacePhotoApi + "maxwidth=" + "400" + "&photoreference=" +  arrayList.get(position) + "&key=" + context.getString(R.string.google_maps_key);
        Glide.with(context).load(photoUrl).into(ivPhoto);
        container.addView(imageLayout, 0);
        return imageLayout;
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
