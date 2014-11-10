package zmuzik.czechstocks.local;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import zmuzik.czechstocks.AppConf;

public class GraphDateFormat extends Format {

    public static final int HOUR_MINUTE_FORMAT = 0;
    public static final int DAY_MONTH_FORMAT = 1;
    public static final int MONTH_YEAR_FORMAT = 2;

    SimpleDateFormat dateFormat;

    public GraphDateFormat(int formatCode) {
        String format = "";
        switch (formatCode) {
            case HOUR_MINUTE_FORMAT:
                format = "HH:mm";
                break;
            case DAY_MONTH_FORMAT:
                format = "dd.MM";
                break;
            case MONTH_YEAR_FORMAT:
                format = "MM/yyyy";
                break;
        }
        setFormat(format);
    }

    public GraphDateFormat(String formatString) {
        setFormat(formatString);
    }

    public void setFormat(String formatString) {
        TimeZone exchangeTimeZone = TimeZone.getTimeZone(AppConf.EXCHANGE_TIME_ZONE);
        dateFormat = new SimpleDateFormat(formatString);
        dateFormat.setTimeZone(exchangeTimeZone);
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        Date date = new Date(((Number) obj).longValue());
        return dateFormat.format(date, toAppendTo, pos);
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return null;
    }
}

