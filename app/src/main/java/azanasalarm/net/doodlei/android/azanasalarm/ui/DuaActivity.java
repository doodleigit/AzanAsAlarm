package azanasalarm.net.doodlei.android.azanasalarm.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import azanasalarm.net.doodlei.android.azanasalarm.R;

public class DuaActivity extends AppCompatActivity {

    private InterstitialAd mPublisherInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dua);

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adReq = new AdRequest.Builder().build();
        mAdView.loadAd(adReq);

        mPublisherInterstitialAd = new InterstitialAd(this);
        mPublisherInterstitialAd.setAdUnitId(getResources().getString(R.string.interestitial));
        AdRequest adRequest = new AdRequest.Builder().build();
        mPublisherInterstitialAd.loadAd(adRequest);
        mPublisherInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                displayInterstitial();
            }
        });
    }

    private void displayInterstitial() {
        if (mPublisherInterstitialAd.isLoaded()) {
            mPublisherInterstitialAd.show();
        }
    }

}
