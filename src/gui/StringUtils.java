package gui;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class StringUtils {
	
    public static String formatTime(long nb) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String date[] = sdf.format(nb).split(":");
		return date[0] + "h" + date[1] + "m" + date[2] + "s";
    }
    
    public static String formatDateTime(long nb) {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss");
		return sdf.format(nb);
    }
    
    public static String formatDuration(long time) {
        int milliseconds = (int)(time % 1000);
        int seconds = (int)((time/1000) % 60);
        int minutes = (int)((time/60000) % 60);
        int hours = (int)((time/3600000) % 24);
        String secondsStr = (seconds<10 ? "0" : "")+seconds;
        String minutesStr = (minutes<10 ? "0" : "")+minutes;
        if (hours > 0)
            return new String(hours+"h"+minutesStr+"m"+secondsStr+"s");
        if (minutes > 0)
            return new String(minutes+"m"+secondsStr+"s");
        if (seconds > 0)
            return new String(seconds+"s");
        if (milliseconds > 0) {
        	return new String(milliseconds + "ms");
        }
        return new String("0s");
    }
    
    public static String formatCount(long nb) {
    	return NumberFormat.getIntegerInstance().format(nb);
    }
}
