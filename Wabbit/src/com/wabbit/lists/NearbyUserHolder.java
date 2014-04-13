package com.wabbit.lists;


public class NearbyUserHolder {
	public String id;
	public String avatar;
	public String name;
    public String distance;
    public int distanceMeters;
	public boolean isFriend;
	
	public NearbyUserHolder(String i, String bmp, String nm, String dist, int distMeters, boolean friend){
		id = i;
		avatar = bmp;
		name = nm;
        distance = dist;
        distanceMeters = distMeters;
		isFriend = friend;
	}
}
