package com.wabbit.lists;

import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * Created by Bogdan Tirca on 23.03.2014.
 */
public abstract class CustBaseAdapter extends BaseAdapter{
    protected Runnable loadMissingContent;
    protected IContentCheck contentCheck;
    protected ListView listView;
    public void setListView(ListView lv){
        listView = lv;
    }
    public void updateList(boolean showWhileInconsistent, boolean fix){
        if(showWhileInconsistent) {
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
//            notifyDataSetChanged();
        }
        if(fix == false)
            return;
        if(contentCheck != null){
            if(contentCheck.consistentContent() == false){
                if(loadMissingContent != null)
                    loadMissingContent.run();
            }
            else{
                updateList(true, false);
            }
        }
    }
    public static interface IContentCheck{
        public boolean consistentContent();
    }
}
