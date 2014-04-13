package com.wabbit.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.wabbit.parse.PMessage;

import java.util.List;

/**
 * Created by Bogdan Tirca on 23.03.2014.
 */
@Table(name = "Recents")
public class Recent extends Model {
    @Column(name = "UserId")
    public String userId;

    @Column(name = "LastMsgText")
    public String lastMsgText;

    @Column(name = "LastMsgTime")
    public Long lastMsgTime;

    @Column(name = "LastMsgRead")
    public Boolean lastMsgRead;


    public Recent(){}
    public Recent(PMessage msg){
        userId = msg.getPartnerId();
        setLastMsg(msg);
        lastMsgRead = false;
    }
    public void setLastMsg(PMessage msg){
        lastMsgText = msg.getText();
        lastMsgTime = msg.getCreatedAt().getTime();
    }

    public static List<Recent> getAll(){
        return new Select()
                .from(Recent.class)
                .orderBy("LastMsgTime")
                .execute();
    }
}
