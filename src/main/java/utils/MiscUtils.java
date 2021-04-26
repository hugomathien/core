package utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

public class MiscUtils {

	public static boolean isWeekday(LocalDate ld) {
		DayOfWeek dow = ld.getDayOfWeek();
		if (dow.equals(DayOfWeek.SATURDAY) || dow.equals(DayOfWeek.SUNDAY))	
			return false;
		else 
			return true;
	}

	public static LocalDate weekday(LocalDate ld) {

		DayOfWeek dow = ld.getDayOfWeek();
		if (dow.equals(DayOfWeek.SATURDAY))	
			return ld.plusDays(2);
		else if(dow.equals(DayOfWeek.SUNDAY))
			return ld.plusDays(1);
		else
			return ld;
	}

	public static ZonedDateTime weekday(ZonedDateTime zdt) {

		DayOfWeek dow = zdt.toLocalDate().getDayOfWeek();
		if (dow.equals(DayOfWeek.SATURDAY))	
			return zdt.plusDays(2);
		else if(dow.equals(DayOfWeek.SUNDAY))
			return zdt.plusDays(1);
		else
			return zdt;
	}

	public static LocalDate plusWeekdays(int dayOffset,LocalDate ld) {

		do { 
			ld = ld.plusDays(dayOffset);
		} while(!isWeekday(ld));
		return ld;
	}


	public static boolean onlyDigits(String str)
	{
		int n = str.length();
		for (int i = 0; i < n; i++) {
			if (!Character.isDigit(str.charAt(i)))
				return false;
		}
		return true;
	}
	
}
