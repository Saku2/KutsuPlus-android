package fi.aalto.kutsuplus.stops;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class StopListParser {
	Activity activity;
	private static final int STOP_LIST_COLUMNS = 13;

	public StopListParser(Activity act) {
		this.activity = act;
	}
	
	public List<Stop> parse(int resource_id) {
		
		List<Stop> stops = new ArrayList<Stop>();
		
		InputStream raw = this.activity.getResources().openRawResource(resource_id);
		try
		{
	      // Special scandinavian characters handled
		 InputStreamReader isr = new InputStreamReader(raw,"ISO-8859-1");
		 BufferedReader in = new BufferedReader(isr);
		 String line = in.readLine();
		 boolean first = true;
		 
		 // Read line from the file
		 // Generate stop objects based on data
		 while(line!=null)
		 {
			 // Ignore first line, only headers
			 if (!first) {
				// Line format:
				// "soltunnus"#"pyskunta"#"pysnimi"#"pysnimir"#"pysosoite"#"pysosoiter"#"kutsuplus"#"kutsuplusvyo"#"kulkusuunta"#"solox"#"soloy"#"solomx"#"solomy"
				 String[] words = line.split("#");
				 if (words.length == STOP_LIST_COLUMNS) {
						stops.add(new Stop(words[0], words[2], words[3], words[9], words[10]));
					}
			 }
			 line = in.readLine();
			 first = false;
		 }
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stops;
	}

}
