package zmuzik.czechstocks.local;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import zmuzik.czechstocks.AppConf;


public class GraphDateFormat extends Format {
    SimpleDateFormat dateFormat;

    public GraphDateFormat(String formatString) {
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

