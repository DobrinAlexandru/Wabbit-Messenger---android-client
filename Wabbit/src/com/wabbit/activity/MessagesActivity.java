package com.wabbit.activity;//package com.wabbit.activity;
//
//import android.os.Bundle;
//import android.view.View;
//import android.view.animation.OvershootInterpolator;
//import android.widget.AbsListView;
//import android.widget.AdapterView;
//import com.beardedhen.androidbootstrap.BootstrapButton;
//import com.beardedhen.androidbootstrap.BootstrapEditText;
//import com.nineoldandroids.animation.ObjectAnimator;
//import com.parse.ParseUser;
//import com.wabbit.abstracts.Enums;
//import com.wabbit.libraries.asynclist.AsyncBaseAdapter;
//import com.wabbit.libraries.asynclist.AsyncListView;
//import com.wabbit.libraries.asynclist.ItemManager;
//import com.wabbit.libraries.cache.BitmapLruCache;
//import com.wabbit.lists.ListItemMessageHolder;
//import com.wabbit.lists.MessagesListAdapter;
//import com.wabbit.lists.MessagesListLoader;
//import com.wabbit.abstracts.MessagesMgr;
//import com.wabbit.messenger.R;
//import com.wabbit.parse.PMessage;
//
//import java.util.ArrayList;
//
//public class MessagesActivity extends BaseActivity{
//
//    private AsyncListView mListView;
//    private MessagesListAdapter mListAdapter;
//    private ArrayList<ListItemMessageHolder> mListItems = new ArrayList<ListItemMessageHolder>();
//
//    private BootstrapEditText messageInput;
//    private BootstrapButton sendButton;
//
//    @Override
//    public void onCreate(Bundle savedExtras){
//        super.onCreate(savedExtras);
//
//        setContentView(R.layout.messages_layout);
//
//        messageInput = (BootstrapEditText) findViewById(R.id.message_input);
//        sendButton = (BootstrapButton) findViewById(R.id.send_button);
//        sendButton.setOnClickListener(onSendMessage);
//        mListView = (AsyncListView) findViewById(R.id.messages_list);
//
//        BitmapLruCache cache = getApp().getBitmapCache();
//        MessagesListLoader loader = new MessagesListLoader(cache);
//
//        ItemManager.Builder builder = new ItemManager.Builder(loader);
//        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(5);
//        builder.setThreadPoolSize(4);
//
//        mListView.setItemManager(builder.build());
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                startActivity(MessagesActivity.class, false, null);
//            }
//        });
//
//        mListAdapter = new MessagesListAdapter(MessagesActivity.this, mListItems);
//        mListView.setAdapter(mListAdapter);
//
//        //Fucking lame. TODO
//        final ObjectAnimator animators[] = new ObjectAnimator[1000];
//        for(int i = 0; i < animators.length; i ++){
//            animators[i] = ObjectAnimator.ofFloat(null, "translationX", 0, 1);
//            animators[i].setDuration(600);
//            animators[i].setInterpolator(new OvershootInterpolator());
//        }
//        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            private int previousFirstVisibleItem = 0;
//            private int previousLastVisibleItem = 0;
//            private int lastVisibleItem = 0;
//            private double speed = 0;
//            private int i;
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//            }
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                if(firstVisibleItem < previousFirstVisibleItem){
//                    MessagesListAdapter adapter = (MessagesListAdapter)((AsyncBaseAdapter) view.getAdapter()).getWrappedAdapter();
//                    MessagesListAdapter.MessageViewHolder hold = adapter.holders.get(firstVisibleItem);
//                    if(hold == null)
//                        return ;
//                    animators[firstVisibleItem].setTarget(hold.bodyHolder);
//                    animators[firstVisibleItem].setFloatValues(150f, 0f);
//                    animators[firstVisibleItem].start();
//                }
//                else{
//                    for(i = previousFirstVisibleItem; i < firstVisibleItem; i ++)
//                        animators[i].cancel();
//                }
//                lastVisibleItem = firstVisibleItem + (visibleItemCount == 0 ? 1 : visibleItemCount) - 1;
//                if(lastVisibleItem > previousLastVisibleItem){
//                    MessagesListAdapter adapter = (MessagesListAdapter)((AsyncBaseAdapter) view.getAdapter()).getWrappedAdapter();
//                    MessagesListAdapter.MessageViewHolder hold = adapter.holders.get(lastVisibleItem);
//                    if(hold == null)
//                        return ;
//                    animators[lastVisibleItem].setTarget(hold.bodyHolder);
//                    animators[lastVisibleItem].setFloatValues(-100f, 0f);
//                    animators[lastVisibleItem].start();
//                }
//                else{
//                    for(i = lastVisibleItem; i < previousLastVisibleItem; i ++)
//                        animators[i].cancel();
//                }
//                previousLastVisibleItem = lastVisibleItem;
//                previousFirstVisibleItem = firstVisibleItem;
////                Log.v("xxxxxx", firstVisibleItem + " " + visibleItemCount + " " + totalItemCount + " " + speed);
//            }});
//    }
//
//
//    @Override
//    public void onResume(){
//        super.onResume();
////        Communicator.gi().testCloudCode(new IRemoteCallback() {
////            @Override
////            public void onResult(RemoteCallHolder rch) {
////                JsonObject res = (JsonObject) rch.parsedResult;
////                Toast.makeText(MessagesActivity.this, res.toString(), Toast.LENGTH_LONG).show();
////            }
////        });
//    }
//
//    private View.OnClickListener onSendMessage = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//        String msgText = messageInput.getText().toString();
//        PMessage msg = new PMessage(msgText,
//                ParseUser.getCurrentUser().getObjectId(),
//                ParseUser.getCurrentUser().getString(Enums.ParseKey.USER_NAME),
//                MessagesMgr.gi().getChatPartenerId(),
//                true);
////        Communicator.gi().sendMsg(msg, new IRemoteCallback() {
////            @Override
////            public void onResult(RemoteCallHolder rch) {
////                JsonObject res = (JsonObject) rch.parsedResult;
////                Log.d("xxx", res.toString());
////                Toast.makeText(MessagesActivity.this, res.toString(), Toast.LENGTH_LONG).show();
////            }
////        });
//        msg.saveInBackground();
//
//        mListItems.add(new ListItemMessageHolder(msg));
//
//        messageInput.setText("");
//        mListAdapter.notifyDataSetChanged();
//        }
//    };
//}
