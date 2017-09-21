package com.example.bdondapati.customdocumentbrowser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by BDondapati on 21-09-2017.
 */

public class CommonUtils {

    public static String captilizeWord(String string) {
        if (string != null && !string.equals(""))
            return Character.toUpperCase(string.charAt(0)) + string.substring(1);
        return "";
    }

    public static String fileSize(long fileSizeInBytes) {
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long fileSizeInMB = fileSizeInKB / 1024;
        return fileSizeInMB > 0 ? fileSizeInMB + " MB" : fileSizeInKB + " KB";
    }

    private static String returnDate(SimpleDateFormat formatter, Calendar cal) throws ParseException {
        String returnDate;
        Calendar Currentcal = Calendar.getInstance();
        String date = formatter.format(Currentcal.getTime());
        Date messageDate = formatter.parse(formatter.format(cal.getTime()));
        Date currentDate = formatter.parse(date);
        long diff = currentDate.getTime() - messageDate.getTime();
        long daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        if (daysDiff == 0) {
            SimpleDateFormat todayFormatter = new SimpleDateFormat("hh:mm a");
            returnDate = todayFormatter.format(cal.getTime());
        } else if (daysDiff == 1) {
            returnDate = "Yesterday";
        } else {
            cal.setTime(messageDate);
            returnDate = formatter.format(cal.getTime());
        }
        return returnDate;
    }


    public static String getDateFromMillis(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd h:mm a");
        return "" + sdf.format(timeInMillis);
    }



}
