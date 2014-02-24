package fi.aalto.kutsuplus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class FormFragment extends Fragment {

	private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.formfragment, container, false);
		
		// Get the streets string array
		String[] streets = readStreets(R.raw.kutsuplus_area_street_names_fi);

		
		// Get a reference to the AutoCompleteTextView in the layout
		final AutoCompleteTextView fromView = (AutoCompleteTextView) rootView.findViewById(R.id.from);
		// Create the adapter and set it to the AutoCompleteTextView 
		ArrayAdapter<String> adapter_from = 
		        new ArrayAdapter<String>(getActivity()		, android.R.layout.simple_list_item_1, streets);
		fromView.setAdapter(adapter_from);
		
		
		final AutoCompleteTextView toView = (AutoCompleteTextView) rootView.findViewById(R.id.to);
		// Get the string array
		ArrayAdapter<String> adapter_to = 
		        new ArrayAdapter<String>(getActivity()		, android.R.layout.simple_list_item_1, streets);
		toView.setAdapter(adapter_to);

		return rootView;
	}

	
	private String[] readStreets(int resource_id) {
		List<String> streets=new ArrayList<String>();
		
		InputStream raw = getResources().openRawResource(resource_id);
		try
		{
	      // Special scandinavian characters handled
		 InputStreamReader isr=new InputStreamReader(raw,"ISO-8859-1");
		 BufferedReader in=new BufferedReader(isr);
		 String line=in.readLine();
		 while(line!=null)
		 {
			 streets.add(line);
			 line=in.readLine();
		 }
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
       return streets.toArray(new String[streets.size()]);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);

    }


}
