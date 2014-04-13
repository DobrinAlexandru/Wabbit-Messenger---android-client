package com.wabbit.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.firebase.client.Firebase;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wabbit.abstracts.Enums;
import com.wabbit.abstracts.MessagesMgr;
import com.wabbit.abstracts.ParseMemCache;
import com.wabbit.activity.MainActivity;
import com.wabbit.application.BaseApp;
import com.wabbit.interfaces.IHelper;
import com.wabbit.libraries.LocationUtils;
import com.wabbit.libraries.cache.BitmapLruCache;
import com.wabbit.lists.MessagesListAdapter;
import com.wabbit.lists.MessagesListLoader;
import com.wabbit.messenger.R;
import com.wabbit.parse.PMessage;

import asynclist.AsyncListView;
import asynclist.ItemManager;

/**
 * Created by Bogdan Tirca on 27.02.2014.
 */
public class ChatFragment extends BaseFragment{
    private AsyncListView mListView;
    private MessagesListAdapter mListAdapter;

    private ImageView menuButton;
    private RelativeLayout backButton;
    private TextView partnerName;
    private TextView partnerDistance;
    private BootstrapEditText messageInput;
    private BootstrapButton sendButton;
    private Firebase firebaseRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.messages_layout, null);
    }

    private boolean initialized = false;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        firebaseRef = BaseApp.getApp().getFirebaseRef();

        menuButton = (ImageView) getView().findViewById(R.id.menu_button);
        menuButton.setOnClickListener(onMenuClick);
//        registerForContextMenu(menuButton);
        backButton = (RelativeLayout) getView().findViewById(R.id.layer_back);
        backButton.setOnClickListener(onPartnerNameClick);
        partnerName =(TextView)getView().findViewById(R.id.partner_name);
        partnerDistance = (TextView)getView().findViewById(R.id.distance);
        messageInput = (BootstrapEditText) getView().findViewById(R.id.message_input);
        messageInput.addTextChangedListener(onTextChanged);
        sendButton = (BootstrapButton) getView().findViewById(R.id.send_button);
        sendButton.setOnClickListener(onSendMessage);
        sendButton.setEnabled(false);
        mListView = (AsyncListView) getView().findViewById(R.id.messages_list);

        BitmapLruCache cache = BaseApp.getApp().getBitmapCache();
        MessagesListLoader loader = new MessagesListLoader(cache);

        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(5);
        builder.setThreadPoolSize(4);

        mListView.setItemManager(builder.build());

        mListAdapter = new MessagesListAdapter(getActivity(), MessagesMgr.gi().getMessages());
        mListView.setAdapter(mListAdapter);

        setHasOptionsMenu(true);

        initialized = true;
    }

    private Runnable onResumeRun = null;
    @Override
    public void onResume(){
        super.onResume();
        //Set partner name
        String partnerId = MessagesMgr.gi().getChatPartenerId();
        String partnerNameStr = ParseMemCache.gi().getUser(partnerId, false).getString(Enums.ParseKey.USER_NAME);
        String partnerDist = LocationUtils.getDistanceFrom(ParseMemCache.gi().getUser(partnerId, false));
        partnerName.setText(partnerNameStr);
        partnerDistance.setText(partnerDist);

        //If runnable scheduled for onResume
        if(onResumeRun != null) {
            onResumeRun.run();
            onResumeRun = null;
        }
    }

    public void setChatPartner(final String userid){
        final boolean partnerChanged = MessagesMgr.gi().setChatPartner(userid);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (partnerChanged) {  //If partner changed, start loading messages from cache
                    MessagesMgr.gi().clearMessages();
                    updateList.run();
                    //if no message in memory cache
                    if (MessagesMgr.gi().getNrMessagesReadFrom(userid) == 0) {
                        MessagesMgr.gi().loadMessagesFromOnline(userid, onLoadMessagesFromOnline);
                        Log.d("messages", "ps online");
                    } else {
                        MessagesMgr.gi().addReadMessages(userid);
                        updateList.run();
                        Log.d("messages", "ps mem cache");
                    }
                } else {
                    MessagesMgr.gi().addUnreadMessages(userid);
                    updateList.run();
                }
                //Mark last message as read
                MessagesMgr.gi().markRecentAsRead(userid);
                //Clear input text
                messageInput.setText("");
            }
        };

        //If all the variables are initialized
        if(initialized)
            run.run();  //Run directly
        else{
            onResumeRun = run;  //Run in onResume
        }
    }

    private void closefragment() {
        getActivity().getSupportFragmentManager().popBackStack();
        if(getActivity() instanceof IHelper){
                ((IHelper)getActivity()).onChatClosed();
        }
        else{
            Toast.makeText(getActivity(), "Failed to open chat", Toast.LENGTH_LONG).show();
        }

        //Hide keyboard
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(messageInput.getWindowToken(), 0);
    }

    public void onNewMessage(){
        onLoadUnreadMessages.run();
    }

    private void showMenu(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Options");
        builder.setItems(R.array.options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                switch (position){
                    case 0:
                        showBlockDialog(MessagesMgr.gi().getChatPartenerId());
                        break;
                    case 1:
                        ((MainActivity)getActivity()).showProfile(MessagesMgr.gi().getChatPartenerId());
                        break;
                    default:
                        break;
                }
            }
        });
        builder.setInverseBackgroundForced(true);
        builder.create();
        builder.show();
    }

    private void showBlockDialog(final String user){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setInverseBackgroundForced(true);
        dialog.setMessage("Are you sure you want to block this person?");
        dialog.setPositiveButton("Block", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                blockUser(user);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.show();
    }
    private void blockUser(String user){
        ParseObject block = new ParseObject("Blocked");
        block.put("blocker", ParseUser.getCurrentUser().getObjectId());
        block.put("blocked", user);
        block.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(getActivity() != null)
                    Toast.makeText(getActivity(), "User blocked", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private View.OnClickListener onPartnerNameClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            closefragment();
        }
    };
    private View.OnClickListener onMenuClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showMenu();
        }

    };

    private View.OnClickListener onSendMessage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!sendButton.isEnabled())
                return;
            BaseApp.getApp().mixtrack("Send message");
            final String msgText = messageInput.getText().toString();
            final String partnerId = MessagesMgr.gi().getChatPartenerId();

            final PMessage msg = new PMessage(msgText,
                    ParseUser.getCurrentUser().getObjectId(),
                    ParseUser.getCurrentUser().getString(Enums.ParseKey.USER_NAME),
                    ParseUser.getCurrentUser().getString(Enums.ParseKey.USER_FBID),
                    partnerId);
            MessagesMgr.gi().sendMsg(msg, new Runnable() {
                @Override
                public void run() {//On blocked
                    if(getActivity() != null)
                        Toast.makeText(getActivity(), "You can't message this user right now.", Toast.LENGTH_LONG).show();
                }
            }, new Runnable() {
                @Override
                public void run() {//On msg sent
                    Firebase newPushRef = firebaseRef.child(partnerId).push();
                    newPushRef.setValue(ParseUser.getCurrentUser().getObjectId());

                    //Update recent messages
                    MessagesMgr.gi().setRecentMsg(msg, true);
                    MessagesMgr.gi().sortRecent();

                    //Update list
                    updateList.run();
                }
            });

            //Empty the input area
            messageInput.setText("");
            //Update list
            updateList.run();
        }
    };

    private Runnable onLoadMessagesFromOnline = new Runnable() {
        @Override
        public void run() {
            MessagesMgr.gi().addReadMessages(MessagesMgr.gi().getChatPartenerId());
            MessagesMgr.gi().addUnreadMessages(MessagesMgr.gi().getChatPartenerId());
            updateList.run();

            MessagesMgr.gi().addReadToRecent(MessagesMgr.gi().getChatPartenerId());
        }
    };

    private Runnable onLoadUnreadMessages = new Runnable() {
        @Override
        public void run() {
            //Add unread msgs to read list
            MessagesMgr.gi().addUnreadMessages(MessagesMgr.gi().getChatPartenerId());
            updateList.run();

            //Mark last message as read
            MessagesMgr.gi().markRecentAsRead(MessagesMgr.gi().getChatPartenerId());
        }
    };
    private Runnable updateListOnUi = new Runnable() {
        @Override
        public void run() {
            Activity act = getActivity();
            if(act != null)
                act.runOnUiThread(updateList);
        }
    };
    private Runnable updateList = new Runnable() {
        private Runnable toTop = new Runnable() {
            @Override
            public void run() {
                mListView.setSelection(mListAdapter.getCount() - 1);
            }
        };
        @Override
        public void run() {
            ((BaseAdapter)mListView.getAdapter()).notifyDataSetChanged();
//            mListAdapter.notifyDataSetChanged();
            if(mListAdapter.getCount() > 0) {
                mListView.clearFocus();
                mListView.post(toTop);
            }
        }
    };

    private TextWatcher onTextChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        @Override
        public void afterTextChanged(Editable s) {
            if(messageInput.getText().toString().equals(""))
                sendButton.setEnabled(false);
            else
                sendButton.setEnabled(true);
//            Log.d("input ", messageInput.getText().toString());
        }
    };

}
