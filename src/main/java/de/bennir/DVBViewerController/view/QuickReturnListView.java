
/*
 * Copyright 2013 Lars Werkman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bennir.DVBViewerController.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import de.bennir.DVBViewerController.R;

public class QuickReturnListView extends ListView {
    private static final String TAG = QuickReturnListView.class.toString();

    private int mItemCount;
    private int mItemOffsetY[];
    private boolean scrollIsComputed = false;
    private int mHeight;
    private Context mContext;

    public QuickReturnListView(Context context) {
        super(context);
        mContext = context;
    }

    public QuickReturnListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public int getListHeight() {
        return mHeight;
    }

    public void computeScrollY() {
        mHeight = 0;
        mItemCount = getAdapter().getCount();
        mItemOffsetY = new int[mItemCount];

        for (int i = 0; i < mItemCount; ++i) {
            View view = getAdapter().getView(i, null, this);
            view.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mItemOffsetY[i] = mHeight;
            mHeight += view.getMeasuredHeight();
            // Add divider height
            mHeight += mContext.getResources().getDimension(R.dimen.dvblist_divider);
        }
        scrollIsComputed = true;
    }

    public boolean scrollYIsComputed() {
        return scrollIsComputed;
    }

    public int getComputedScrollY() {
        int pos, nScrollY, nItemY;
        View view = null;
        pos = getFirstVisiblePosition();
        view = getChildAt(0);
        nItemY = view.getTop();
        nScrollY = mItemOffsetY[pos] - nItemY;
        return nScrollY;
    }

    public void reset() {
        mItemCount = 0;
        mItemOffsetY = null;
        scrollIsComputed = false;
        mHeight = 0;
        computeScrollY();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                reset();
            }
        });
    }
}