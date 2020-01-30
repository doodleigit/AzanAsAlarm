package azanasalarm.net.doodlei.android.azanasalarm.adapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suke.widget.SwitchButton;

import java.util.Calendar;
import java.util.List;

import azanasalarm.net.doodlei.android.azanasalarm.R;
import azanasalarm.net.doodlei.android.azanasalarm.database.AlarmDB;
import azanasalarm.net.doodlei.android.azanasalarm.model.AlarmModel;
import azanasalarm.net.doodlei.android.azanasalarm.receiver.AlarmReceiver;
import azanasalarm.net.doodlei.android.azanasalarm.ui.AddAlarmActivity;
import azanasalarm.net.doodlei.android.azanasalarm.ui.BenifitsActivity;
import azanasalarm.net.doodlei.android.azanasalarm.util.PrepareTwelveHourFormat;

import static android.content.Context.ALARM_SERVICE;

public class AlarmAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AlarmModel> alarmModelList;
    private Context context;
    private PrepareTwelveHourFormat prepareTwelveHourFormat;
    private AlarmDB alarmDatabase;
    private Intent myIntent;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Calendar calendar;

    public AlarmAdapter(List<AlarmModel> alarmModelList, Context context) {
        this.alarmModelList = alarmModelList;
        this.context = context;
        this.prepareTwelveHourFormat = new PrepareTwelveHourFormat();
        this.alarmDatabase = new AlarmDB(context);
        this.myIntent = new Intent(context, AlarmReceiver.class);
        this.alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        calendar = Calendar.getInstance();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_items, parent, false);
        return new MyViewHolder(itemView);


    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder mainholder, int position) {

        final AlarmModel alarmModel = alarmModelList.get(position);
        String time = prepareTwelveHourFormat.twelveHourformat(Integer.parseInt(alarmModel.getTime()), Integer.parseInt(alarmModel.getMin()));

        final MyViewHolder holder = (MyViewHolder) mainholder;
        holder.time.setText(time);
        holder.am_pm.setText(alarmModel.getAmpm());
        // holder.notes.setText(alarmModel.getNotes());
        holder.name.setText(alarmModel.getName());
        if (alarmModel.getIsSet().equals("true")) {
            holder.aSwitch.setChecked(true);
            holder.alarm_st.setText(context.getResources().getString(R.string.on));
            holder.parent.setBackground(context.getResources().getDrawable(R.drawable.active_alarm_bg, null));
            holder.divider.setBackgroundColor(context.getResources().getColor(R.color.activeDivider, null));
            holder.alarm_st.setTextColor(context.getResources().getColor(R.color.inactiveText, null));

        } else {
            holder.aSwitch.setChecked(false);
            holder.alarm_st.setText(context.getResources().getString(R.string.off));
            holder.parent.setBackground(context.getResources().getDrawable(R.drawable.inactive_alarm_bg, null));
            holder.divider.setBackgroundColor(context.getResources().getColor(R.color.inactiveDivider, null));
            holder.alarm_st.setTextColor(context.getResources().getColor(R.color.inactiveText, null));
        }

        if (alarmModel.getIsAuto().equals("true")) {
            holder.timeSetType.setText(context.getString(R.string.auto));
        } else {
            holder.timeSetType.setText(context.getString(R.string.manual));
        }

        holder.aSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {


                    myIntent.putExtra("eid", alarmModel.getId() + "");
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(alarmModel.getTime()));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(alarmModel.getMin()));
                    pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), alarmModel.getId(), myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                    long difference = calendar.getTimeInMillis() - System.currentTimeMillis();
                    String timefromnow = prepareTwelveHourFormat.differerence(difference);
                    Toast.makeText(context, timefromnow, Toast.LENGTH_SHORT).show();

                    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }
                    long timeInMin = calendar.getTimeInMillis();
                    if (alarmModel.getRepeat().equals("never")) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMin, pendingIntent);
                    } else {
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeInMin, AlarmManager.INTERVAL_DAY, pendingIntent);
                    }
                    alarmModel.setIsSet("true");
                    alarmDatabase.updateCheckedAlarm(alarmModel);
                    holder.alarm_st.setText(context.getResources().getString(R.string.on));
                    holder.parent.setBackground(context.getResources().getDrawable(R.drawable.active_alarm_bg, null));
                    holder.divider.setBackgroundColor(context.getResources().getColor(R.color.activeDivider, null));
                    holder.alarm_st.setTextColor(context.getResources().getColor(R.color.white, null));


                } else {
                    pendingIntent = PendingIntent.getBroadcast(context, alarmModel.getId(), myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmManager.cancel(pendingIntent);
                    alarmModel.setIsSet("false");
                    alarmDatabase.updateCheckedAlarm(alarmModel);
                    holder.alarm_st.setText(context.getResources().getString(R.string.off));
                    holder.parent.setBackground(context.getResources().getDrawable(R.drawable.inactive_alarm_bg, null));
                    holder.divider.setBackgroundColor(context.getResources().getColor(R.color.inactiveDivider, null));
                    holder.alarm_st.setTextColor(context.getResources().getColor(R.color.inactiveText, null));
                }
            }
        });

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddAlarmActivity.class);
                intent.putExtra("id", alarmModel.getId());
                context.startActivity(intent);
            }
        });

        holder.fazilat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, BenifitsActivity.class));
            }
        });


    }

    @Override
    public int getItemCount() {
        return alarmModelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView time, am_pm, fazilat, alarm_st, name, timeSetType;
        SwitchButton aSwitch;
        RelativeLayout parent;
        View divider;

        MyViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            divider = itemView.findViewById(R.id.divider);
            fazilat = itemView.findViewById(R.id.fazilat);
            am_pm = itemView.findViewById(R.id.am_pm);
            aSwitch = itemView.findViewById(R.id.switch1);
            name = itemView.findViewById(R.id.name);
            timeSetType = itemView.findViewById(R.id.timeSetType);
            parent = itemView.findViewById(R.id.parent);
            alarm_st = itemView.findViewById(R.id.alarm_status);

        }
    }


}
