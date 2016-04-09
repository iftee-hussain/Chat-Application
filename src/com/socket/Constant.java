package com.socket;

public class Constant {
	//SUCCESS 2xx
	public static final int OK = 200;
	public static final int HELLO = 201;
	public static final int BYE = 202;
    public static final int MESSAGE = 203;           
    public static final int UPLOAD_REQUEST = 206;
    public static final int UPLOAD_RESPONSE = 207;
    public static final int MESSAGE_GROUP = 208;
    public static final int DELETE_USERS = 209;
    public static final int BLOCK = 210;
    public static final int CHANGE_PASS = 211;
    public static final int FRIEND_REQ = 212;
    public static final int GET_FRIEND_REQ = 213;
    public static final int CALL_TRANSFER = 214;
    public static final int FILTERED_TEXT = 215;
    public static final int UPDATE_FRIEND_REQUEST = 216;
    public static final int UPDATE_MY_PROFILE = 217;
	//INFO 1xx
    public static final int LOGIN = 100;       
    public static final int SINE_UP = 101; 
    public static final int AVAILABLE = 102;
    public static final int NEW_USER = 103;
    public static final int USER_LIST = 105;
    public static final int HISTORY = 106;
    public static final int SPECIFIC_USERS = 107;
    public static final int SPECIFIC_USER_PERMISSION = 108;
    public static final int CHAT_PERMISSION = 109;
    public static final int NOTIFICATION_LIST = 110;
    public static final int STATUS_CHECK = 111;
    public static final int IS_CLIENT_ACTIVE = 112;

    // ERROR 4xx
    public static final int WRONG_PASSWORD = 400; 
    public static final int TOO_LONG_USERNAME= 401;  
    public static final int TOO_LONG_PASSWORD= 402;  
    public static final int USER_EXISTS = 403;      
    public static final int USER_NOT_EXISTS = 404; 
    public static final int SERVER_ERROR = 405;
    public static final int UNKNOWN_COMMAND = 406;
    public static final int SERVER_NOT_FOUND = 407;
    public static final int UPDATE_PERMISSION = 408;
    public static final int EDIT = 409;
    
    //Group Chat
    public static final int CREATE_GROUP = 501;
    public static final int DELETE_GROUP = 502;
    public static final int GET_GROUPS_NAMES = 503;
    public static final int ADD_MEMBER_TO_GROUP = 504;
    public static final int REMOVE_MEMBER_FROM_GROUP = 505;
    public static final int GET_MEMBERS_FROM_GROUP = 506;
    public static final int GROUP_MESSAGE = 507; 
    public static final int GROUP_HISTORY = 508;
    public static final int SET_GROUP_NOTIFICATION = 509;
    public static final int GET_GROUP_NOTIFICATION = 510;

}
