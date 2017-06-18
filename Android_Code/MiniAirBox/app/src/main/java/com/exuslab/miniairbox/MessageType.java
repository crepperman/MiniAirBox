package com.exuslab.miniairbox;

/**
 * Created by Wei on 2016/10/8.         */
public class MessageType {
    public static final int DATA_RECEIVED = 3001;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String BT_MESSAGE = "bt_message";
    public static final String TOAST = "toast";
}

