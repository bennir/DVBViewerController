package de.bennir.DVBViewerController.wizard.ui;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import de.bennir.DVBViewerController.R;
import de.bennir.DVBViewerController.wizard.model.TimerDatePage;

import java.util.Calendar;

public class TimerDateFragment extends Fragment {
    private static final String ARG_KEY = "key";
    private PageFragmentCallbacks mCallbacks;
    private String mKey;
    private TimerDatePage mPage;
    private TextView mDateView;
    private TextView mTimeStartView;
    private TextView mTimeEndView;
    private LinearLayout mDateLayout;
    private LinearLayout mTimeStartLayout;
    private LinearLayout mTimeEndLayout;

    public TimerDateFragment() {
    }

    public static TimerDateFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        TimerDateFragment fragment = new TimerDateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mKey = args.getString(ARG_KEY);
        mPage = (TimerDatePage) mCallbacks.onGetPage(mKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page_timer_date, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(getResources().getString(R.string.timer_date));

        mDateView = (TextView) rootView.findViewById(R.id.timer_date);
        mDateView.setText(mPage.getData().getString(TimerDatePage.DATE_DATA_KEY));

        mTimeStartView = (TextView) rootView.findViewById(R.id.timerstart_time);
        mTimeStartView.setText(mPage.getData().getString(TimerDatePage.TIMESTART_DATA_KEY));

        mTimeEndView = (TextView) rootView.findViewById(R.id.timerend_time);
        mTimeEndView.setText(mPage.getData().getString(TimerDatePage.TIMEEND_DATA_KEY));

        mTimeStartLayout = (LinearLayout) rootView.findViewById(R.id.timer_timestart_layout);
        mTimeStartLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start Start Time Picker Dialog
                TimePickerDialog start = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
                        mTimeStartView.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                    }
                }, 0, 0, true);
                start.show(getFragmentManager(), "starttime");
            }
        });

        com.android.datetimepicker.time.TimePickerDialog time;

        mTimeEndLayout = (LinearLayout) rootView.findViewById(R.id.timer_timeend_layout);
        mTimeEndLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start End Time Picker Dialog
                TimePickerDialog end = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
                        mTimeEndView.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                    }
                }, 0, 0, true);
                end.show(getFragmentManager(), "endtime");
            }
        });

        mDateLayout = (LinearLayout) rootView.findViewById(R.id.timer_date_layout);
        mDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH) + 1;

                DatePickerDialog date = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
                        mDateView.setText(String.format("%02d", dayOfMonth) + "." + String.format("%02d", monthOfYear + 1) + "." + year);
                    }
                }, year, month, 0);
                date.show(getFragmentManager(), "date");
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }

        mCallbacks = (PageFragmentCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDateView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                          int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(TimerDatePage.DATE_DATA_KEY,
                        (editable != null) ? editable.toString() : "");
                mPage.notifyDataChanged();
            }
        });

        mTimeStartView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                          int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(TimerDatePage.TIMESTART_DATA_KEY,
                        (editable != null) ? editable.toString() : "");
                mPage.notifyDataChanged();
            }
        });

        mTimeEndView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(TimerDatePage.TIMEEND_DATA_KEY,
                        (editable != null) ? editable.toString() : "");
                mPage.notifyDataChanged();
            }
        });
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
    }
}
