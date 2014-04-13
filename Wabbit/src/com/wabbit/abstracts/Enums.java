package com.wabbit.abstracts;

public class Enums {

	public static class LoginResult {
		public static int WrongPassword = 0;
		public static int Success = 1;
		public static int Disabled = 2;
		public static int TemporaryBan = 3;
		public static int Activating = 4;
		public static int NoGuestAccount = 5;
		public static int UpdateRequired = 6;
		public static int Logout = 7;
	}

	public static class AppUserIsActive {
		public static int PasswordNotSet = 0;
		public static int Disabled = 1;
		public static int ActiveAuto = 2;
		public static int Activating = 3;
		public static int ActivePerm = 4;
		public static int TemporaryBan = 5;
	}
	
	public static class GeneralResult {
		public static int Success = 0;
		public static int Fail = 1;
		public static int AlreadyDone = 2;
		public static int Online = 3;
		public static int Offline = 4;
	}

    public static class ParseKey{
        //Instalation
        public static String INSTAL_USER = "user";
        public static String INSTAL_ANDROID_ID = "androidID";

        public static String PLACE_RELEASED = "released";
        public static String PLACE_RADIUS = "radius";

        public static String USER_FBID = "fbid";
        public static String USER_NAME = "usr_name";
        public static String USER_VISIBLE = "visible";
        public static String USER_LOCATION = "location";
        public static String USER_FB_PHOTOS = "fb_photos";

        public static String MSG_TYPE = "type";
        public static String MSG_BODY = "msg";
        public static String MSG_FROM_ID = "from_id";
        public static String MSG_FROM_NAME = "from_name";
        public static String MSG_FROM_FB_ID = "fb_id";
        public static String MSG_TO_ID = "to_id";
        public static String MSG_READ = "read";

    }
}
