package azanasalarm.net.doodlei.android.azanasalarm.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.database.AlarmDB;
import azanasalarm.net.doodlei.android.azanasalarm.interfaces.OnChecked;
import azanasalarm.net.doodlei.android.azanasalarm.interfaces.OnPlayClick;
import azanasalarm.net.doodlei.android.azanasalarm.model.AzanModel;

public class AzanAdapter extends RecyclerView.Adapter<AzanAdapter.AzanViewHolder> {

    Context context;
    List<AzanModel> azanModelList;
    MediaPlayer mediaPlayer;
    OnChecked onChecked;
    OnPlayClick onPlayClick;
    AlarmDB alarmDB;
    String file_url;
    Boolean ispaused = false;
    private Boolean[] play_pause_check;

    public AzanAdapter(Context context, List<AzanModel> azanModelList, OnPlayClick onPlayClick, OnChecked onChecked, int alarmid) {
        this.context = context;
        this.azanModelList = azanModelList;
        this.onChecked = onChecked;
        this.onPlayClick = onPlayClick;
        alarmDB = new AlarmDB(context);
        file_url = alarmDB.getAlarm(alarmid).getRingtone();
        mediaPlayer = new MediaPlayer();
        play_pause_check = new Boolean[azanModelList.size()];
        for (int i = 0; i < azanModelList.size(); i++) {
            play_pause_check[i] = false;

        }


    }

    @NonNull
    @Override
    public AzanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.azan_items, parent, false);
        return new AzanViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AzanViewHolder holder, final int position) {


        AzanModel azan = azanModelList.get(position);
        holder.title.setText(azan.getTitle());


        if (play_pause_check[position]) {
            holder.play_icon.setImageResource(R.drawable.ic_pause);
            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.activeMusic));

        } else {
            holder.play_icon.setImageResource(R.drawable.ic_play_circle);
            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        }

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayClick.onPlay(v, position);
                onChecked.onCheck(v, position);
            }
        });

        if (azanModelList.get(position).getFile_uri().equals(file_url)) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }


    }

    public void update(int cur, int prev) {
        ispaused = false;
        file_url = azanModelList.get(cur).getFile_uri();
        for (int i = 0; i < azanModelList.size(); i++) {
            play_pause_check[i] = false;
            notifyItemChanged(i);

        }
        play_pause_check[cur] = true;
        notifyItemChanged(cur);
    }

    public void pause(int audioIndex, int i) {
        ispaused = true;
        if (audioIndex >= 0 && audioIndex < azanModelList.size()) {
            file_url = azanModelList.get(audioIndex).getFile_uri();
            play_pause_check[audioIndex] = false;
            notifyItemChanged(audioIndex);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return azanModelList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class AzanViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView thumbnail, play_icon;
        CheckBox checkBox;
        LinearLayout linearLayout;
        RelativeLayout parent;

        AzanViewHolder(View view) {
            super(view);
            parent = view.findViewById(R.id.parent);
            linearLayout = view.findViewById(R.id.play);
            title = view.findViewById(R.id.song_title);
            checkBox = view.findViewById(R.id.checkbox);
            thumbnail = view.findViewById(R.id.thumbnail);
            artist = view.findViewById(R.id.artist);
            play_icon = view.findViewById(R.id.play_icon);
        }


    }

}
