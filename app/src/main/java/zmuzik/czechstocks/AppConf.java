package zmuzik.czechstocks;

import zmuzik.czechstocks.utils.TimeUtils;

public class AppConf {
    public static final String SERVER_API_ROOT = "http://185.8.238.141/csapi";
    public static final String DB_NAME = "czech-stocks-db";
    public static final String EXCHANGE_TIME_ZONE = "Europe/Prague";

    public static final long CURRENT_DATA_UPDATE_INTERVAL = TimeUtils.ONE_MINUTE;
    public static final long HIST_DATA_UPDATE_INTERVAL = TimeUtils.FOUR_HOURS;

}
