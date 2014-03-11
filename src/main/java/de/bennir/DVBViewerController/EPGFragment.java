package de.bennir.DVBViewerController;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.bennir.DVBViewerController.channels.DVBChannel;
import de.bennir.DVBViewerController.epg.EPGInfo;
import de.bennir.DVBViewerController.epg.EPGInfoAdapter;
import de.bennir.DVBViewerController.service.DVBService;
import de.bennir.DVBViewerController.view.QuickReturnListView;

public class EPGFragment extends Fragment {
    private static final String TAG = EPGFragment.class.toString();
    private static final int STATE_ONSCREEN = 0;
    private static final int STATE_OFFSCREEN = 1;
    private static final int STATE_RETURNING = 2;

    private int mQuickReturnHeight;
    private int mState = STATE_ONSCREEN;
    private int mScrollY;
    private int mMinRawY = 0;

    public static EPGChannelAdapter drawerLvAdapter;
    private ExpandableListView mDrawerListView;
    private QuickReturnListView mListView;
    private Context mContext;
    private DVBService mDVBService;
    private TextView mQuickReturnView;
    private View mPlaceHolder;
    private View mHeader;
    private DrawerLayout mDrawerLayout;

    public EPGInfoAdapter lvAdapter;
    public ArrayList<EPGInfo> epgInfos = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.epg_fragment, container, false);

        mListView = (QuickReturnListView) rootView.findViewById(R.id.epg_listview);
        mQuickReturnView = (TextView) rootView.findViewById(R.id.sticky);

        mHeader = inflater.inflate(R.layout.epg_fragment_header, null);
        mPlaceHolder = mHeader.findViewById(R.id.placeholder);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mDVBService = DVBService.getInstance(mContext);

        setHasOptionsMenu(true);

        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);

        /**
         * Right Drawer ListView
         */
        mDrawerListView = (ExpandableListView) getActivity().findViewById(R.id.epg_channel_list);
        drawerLvAdapter = new EPGChannelAdapter(mContext, mDVBService.getGroupNames(), mDVBService.getChannelGroupMap());

        mDrawerListView.setAdapter(drawerLvAdapter);
        mDrawerListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                TextView mChannel = (TextView) v.findViewById(R.id.epg_channel_item);
                String channelName = mChannel.getText().toString();
                mQuickReturnView.setText(channelName);
                mDrawerLayout.closeDrawer(Gravity.END);

                DVBChannel channel = mDVBService.getChannelByName(channelName);
                Log.d(TAG, "channelId: " + channel.channelId);

                epgInfos.clear();
                for(int i=0;i<30;i++) {
                    EPGInfo info = new EPGInfo();
                    info.title = channelName;
                    epgInfos.add(info);
                }

                lvAdapter = new EPGInfoAdapter(mContext, epgInfos);
                mListView.setAdapter(lvAdapter);


                return true;
            }
        });

        mQuickReturnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.END);
            }
        });


        lvAdapter = new EPGInfoAdapter(mContext, epgInfos);
        mListView.setAdapter(lvAdapter);

        mQuickReturnView.setText("No Channel Selected");
        mListView.addHeaderView(mHeader);

        mListView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mQuickReturnHeight = mQuickReturnView.getHeight();
                        mListView.computeScrollY();
                    }
                });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                mScrollY = 0;
                int translationY = 0;

                if (mListView.scrollYIsComputed()) {
                    mScrollY = mListView.getComputedScrollY();
                }

                int rawY = mPlaceHolder.getTop() - mScrollY;

                switch (mState) {
                    case STATE_OFFSCREEN:
                        if (rawY <= mMinRawY) {
                            mMinRawY = rawY;
                        } else {
                            mState = STATE_RETURNING;
                        }
                        translationY = rawY;
                        break;

                    case STATE_ONSCREEN:
                        if (rawY < -mQuickReturnHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                        translationY = rawY;
                        break;

                    case STATE_RETURNING:
                        translationY = (rawY - mMinRawY) - mQuickReturnHeight;
                        if (translationY > 0) {
                            translationY = 0;
                            mMinRawY = rawY - mQuickReturnHeight;
                        }

                        if (rawY > 0) {
                            mState = STATE_ONSCREEN;
                            translationY = rawY;
                        }

                        if (translationY < -mQuickReturnHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                        break;
                }

                //mQuickReturnView.animate().cancel();
                mQuickReturnView.setTranslationY(translationY);

            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });
    }

    public class EPGChannelAdapter extends BaseExpandableListAdapter {

        private Context mContext;
        private List<String> mHeaders; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<String>> mItems;

        public EPGChannelAdapter(Context context, List<String> headers,
                                 HashMap<String, List<String>> items) {
            this.mContext = context;
            this.mHeaders = headers;
            this.mItems = items;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this.mItems.get(this.mHeaders.get(groupPosition))
                    .get(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final String childText = (String) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.epg_channel_list_item, null);
            }

            TextView chan = (TextView) convertView.findViewById(R.id.epg_channel_item);
            chan.setText(childText);

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this.mItems.get(this.mHeaders.get(groupPosition))
                    .size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.mHeaders.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this.mHeaders.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalter = (LayoutInflater) this.mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalter.inflate(R.layout.epg_channel_list_header, null);
            }

            TextView groupName = (TextView) convertView.findViewById(R.id.epg_channel_header);
            groupName.setTypeface(null, Typeface.BOLD);
            groupName.setText(headerTitle);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
