package com.wabbit.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.wabbit.abstracts.Enums;
import com.wabbit.libraries.remoting.FBMgr;

/**
 * Created by Bogdan Tirca on 03.02.2014.
 */
@ParseClassName("Message")
public class PMessage extends ParseObject{
    private String temporaryId;

    public PMessage(){}
    public PMessage(String text, String fromId, String fromName, String fbId, String toId){
        put(Enums.ParseKey.MSG_TYPE, "text");
        put(Enums.ParseKey.MSG_BODY, text);
        put(Enums.ParseKey.MSG_FROM_ID, fromId);
        put(Enums.ParseKey.MSG_FROM_NAME, fromName);
        put(Enums.ParseKey.MSG_FROM_FB_ID, fbId);
        put(Enums.ParseKey.MSG_TO_ID, toId);
        put(Enums.ParseKey.MSG_READ, false);
    }
    public String getType(){
        return getString(Enums.ParseKey.MSG_TYPE);
    }
    public String getText(){
        return getString(Enums.ParseKey.MSG_BODY);
    }
    public String getFromId(){
        return getString(Enums.ParseKey.MSG_FROM_ID);
    }
    public String getFromName(){
        return getString(Enums.ParseKey.MSG_FROM_NAME);
    }
    public String getToId(){
        return getString(Enums.ParseKey.MSG_TO_ID);
    }
    public String getSenderAvatar(){
        return FBMgr.gi().getLinkToProfilePictureByParseId(getFromId());
    }

    public boolean isSent(){
        return ParseUser.getCurrentUser().getObjectId().equals(getFromId());
    }
    public String getPartnerId(){
        if(isSent())
            return getToId();
        return getFromId();
    }

    public boolean isRead(){
        return getBoolean(Enums.ParseKey.MSG_READ);
    }
    public void setRead(boolean readState){
        put(Enums.ParseKey.MSG_READ, readState);
    }

    public boolean hasTemporaryId(){
        return temporaryId != null;
    }
    public String getTemporaryId(){
        return this.temporaryId;
    }
    public void setTemporaryId(String temporaryId){
        this.temporaryId = temporaryId;
    }
    public void removeTemporaryId(){
        temporaryId = null;
    }
    public String getId(){
        if(hasTemporaryId())
            return temporaryId;
        return getObjectId();
    }

    @Override
    public int hashCode(){
        return getId().hashCode();
    }
    @Override
    public boolean equals(Object other){
        return getId().equals(((PMessage)other).getId());
    }
}
