package azanasalarm.net.doodlei.android.azanasalarm.ui;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.adapter.AzanAdapter;
import azanasalarm.net.doodlei.android.azanasalarm.database.AlarmDB;
import azanasalarm.net.doodlei.android.azanasalarm.interfaces.OnChecked;
import azanasalarm.net.doodlei.android.azanasalarm.interfaces.OnPlayClick;
import azanasalarm.net.doodlei.android.azanasalarm.model.AlarmModel;
import azanasalarm.net.doodlei.android.azanasalarm.model.AzanModel;

public class SelectAzanTone extends AppCompatActivity implements OnChecked, OnPlayClick {

    private static final int UPDATE_FREQUENCY = 500;
    private final Handler handler = new Handler();
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    AlarmModel alarmModel;
    AzanModel selectedazan = null;
    List<AzanModel> azanModelList = new ArrayList<>();
    private AlarmDB alarmDB;
    private AzanAdapter azanAdapter;

    //Media Player Variables
    private SeekBar seekBar;
    private MediaPlayer player;
    private final Runnable updatePositinRunnable = new Runnable() {
        @Override
        public void run() {
            updatePosition();
        }
    };
    private ImageView thumbnail;
    private ImageButton play;
    private boolean isPlaying = false, isPaused = false;
    private TextView selectedItem, artist;
    private boolean isMovingSeekBar = false;
    private int currentsong = -1;
    private SeekBar.OnSeekBarChangeListener seekBarChanged =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (isMovingSeekBar) {
                        player.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    isMovingSeekBar = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isMovingSeekBar = false;
                }
            };
    private View.OnClickListener OnButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play: {
                    if (currentsong < 0) {
                        currentsong = 0;
                        if (azanModelList.get(currentsong) instanceof AzanModel) {
                            startPlay(azanModelList.get(currentsong));
                        } else {
                            playNext();
                        }


                    } else if (isPaused && !isPlaying) {
                        resumePlay();
                    } else if (isPlaying) {
                        stopPlay();
                    }
                    break;
                }

                case R.id.next: {
                    playNext();
                    break;
                }
                case R.id.previous: {
                    playPrev();
                    break;
                }
            }
        }
    };
    private MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            stopPlay();
        }
    };
    private MediaPlayer.OnErrorListener onError = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }
    };


    private void startPlay(AzanModel song) {


        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, Uri.parse(song.getFile_uri()));
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) {
            Glide.with(this).load(data).into(thumbnail).onLoadFailed(getResources().getDrawable(R.drawable.trackplaceholder));
        } else {
            thumbnail.setImageResource(R.drawable.trackplaceholder);
        }

        selectedItem.setText(song.getTitle());
        artist.setText(song.getArtist());
        seekBar.setProgress(0);
        player.stop();
        player.reset();
        try {
            player.setDataSource(this, Uri.parse(song.getFile_uri()));
            player.prepare();
            player.start();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        seekBar.setMax(player.getDuration());
        play.setImageResource(R.drawable.ic_pause);
        azanAdapter.update(currentsong, 0);
        isPaused = false;
        isPlaying = true;
        updatePosition();


    }

    private void stopPlay() {
        player.pause();
        play.setImageResource(R.drawable.ic_play);
        handler.removeCallbacks(updatePositinRunnable);
        azanAdapter.pause(currentsong, 0);
        isPaused = true;
        isPlaying = false;

    }

    private void resumePlay() {
        player.start();
        azanAdapter.update(currentsong, 0);
        play.setImageResource(R.drawable.ic_pause);
        isPaused = false;
        isPlaying = true;
        updatePosition();

    }


    private void updatePosition() {
        handler.removeCallbacks(updatePositinRunnable);
        seekBar.setProgress(player.getCurrentPosition());
        handler.postDelayed(updatePositinRunnable, UPDATE_FREQUENCY);
    }

    private void playNext() {

        if (currentsong + 1 > azanModelList.size() - 1) {
            currentsong = 0;
        } else {
            currentsong++;
        }
        if (azanModelList.size() > 0 && azanModelList.get(currentsong) instanceof AzanModel) {
            startPlay(azanModelList.get(currentsong));
        } else {
            playNext();
        }
    }

    private void playPrev() {
        if (currentsong > 0) {
            currentsong--;
            if (azanModelList.size() > 0) {
                if (azanModelList.size() > 0 && azanModelList.get(currentsong) instanceof AzanModel) {
                    startPlay(azanModelList.get(currentsong));
                } else {
                    playPrev();
                }
            }
        } else if (currentsong == 0) {
            currentsong = azanModelList.size() - 1;
            if (azanModelList.size() > 0 && azanModelList.get(currentsong) instanceof AzanModel) {
                startPlay(azanModelList.get(currentsong));
            } else {
                playPrev();
            }
        }
    }


    @Override
    public void onPause() {
        stopPlay();
        saveTone();
        super.onPause();
    }

    @Override
    public void onResume() {

        if (player != null) {
            seekBar.setProgress(player.getCurrentPosition());
        }
        super.onResume();

    }


    @Override
    public void onDestroy() {

        handler.removeCallbacks(updatePositinRunnable);
        player.stop();
        player.reset();
        player.release();
        player = null;

        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_azan_tone);
        alarmDB = new AlarmDB(this);
        int id = getIntent().getIntExtra("id", 0);
        alarmModel = alarmDB.getAlarm(id);

        recyclerView = findViewById(R.id.recycle_view);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        seekBar = findViewById(R.id.seekBar);
        ImageButton prev = findViewById(R.id.previous);
        play = findViewById(R.id.play);
        ImageButton next = findViewById(R.id.next);
        selectedItem = findViewById(R.id.selecteditem);
        artist = findViewById(R.id.artist);
        thumbnail = findViewById(R.id.thumbnail);

        player = new MediaPlayer();
        player.setOnCompletionListener(onCompletion);
        player.setOnErrorListener(onError);
        seekBar.setOnSeekBarChangeListener(seekBarChanged);

        prev.setOnClickListener(OnButtonClick);
        play.setOnClickListener(OnButtonClick);
        next.setOnClickListener(OnButtonClick);


        Field[] fields = R.raw.class.getFields();
        MediaMetadataRetriever metaRetriver;
        metaRetriver = new MediaMetadataRetriever();


        for (int count = 0; count < fields.length; count++) {
            Log.i("Raw Asset: ", fields[count].getName());

            String uriPath = "android.resource://" + getPackageName() + "/raw/" + fields[count].getName();
            final Uri uri = Uri.parse(uriPath);
            metaRetriver.setDataSource(this, uri);
            String title = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
            String album = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            azanModelList.add(new AzanModel(fields[count].getName(), "0", uriPath, title, artist, album));


        }
        azanAdapter = new AzanAdapter(this, azanModelList, this, this, id);
        recyclerView.setAdapter(azanAdapter);


    }

    @Override
    public void onCheck(View view, int position) {
        selectedazan = azanModelList.get(position);
        //azanAdapter.uncheckall(position);
    }

    @Override
    public void onPlay(View view, int position) {
        if (position != currentsong) {
            currentsong = position;
            startPlay(azanModelList.get(currentsong));
        } else {
            if (isPlaying && !isPaused) {
                stopPlay();
            } else if (!isPlaying && isPaused) {
                resumePlay();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:

                saveTone();
                NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void saveTone() {

        if (selectedazan != null) {
            alarmModel.setRingtone(selectedazan.getFile_uri());
            alarmDB.updateAlarm(alarmModel.getId(), alarmModel);
        }
    }
}
