package fi.aalto.kutsuplus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class FormFragment extends Fragment {

	private View rootView;
	String popUpContents[];
    ImageButton buttonShowDropDown_fromExtras;
    ImageButton buttonShowDropDown_toExtras;
    PopupWindow popupWindow_ExtrasList;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.formfragment, container, false);
		
		// Get the streets string array
		String[] streets = readStreets(R.raw.kutsuplus_area_street_names);

		
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
		createDropDown(rootView);
		return rootView;
	}

	
	private String[] readStreets(int resource_id) {
		List<String> streets=new ArrayList<String>();
		
		
		InputStream raw = getResources().openRawResource(resource_id);
		try
		{
	      // Special Scandinavian characters handled
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

	private void createDropDown(View rootView)
	{
        // add items on the array dynamically
        // format is DogName::DogID
        List<String> optionsList = new ArrayList<String>();
        optionsList.add("Current location");
        optionsList.add("Street 1");
        optionsList.add("Street 2");
        // convert to simple array
        popUpContents = new String[optionsList.size()];
        optionsList.toArray(popUpContents);

        /*
         * initialize pop up window
         */
        Context mContext = rootView.getContext();
        final MainActivity mainActivity = ((MainActivity) mContext);
        mainActivity.popupWindow_ExtrasList = popupWindowDogs();
        popupWindow_ExtrasList=mainActivity.popupWindow_ExtrasList;
        /*
         * fromExtras button on click listener
         */
        View.OnClickListener extras_handler = new View.OnClickListener() {
            public void onClick(View v) {                                                                                                                                                                                                                                                                                                 

                switch (v.getId()) {
                case R.id.from_extras:
                    // show the list view as dropdown
                	mainActivity.extras_list=MainActivity.EXTRAS_FROM;
                    popupWindow_ExtrasList.showAsDropDown(v, 0, 0);                    
                    break;
                    
                case R.id.to_extras:
                    // show the list view as dropdown
                	mainActivity.extras_list=MainActivity.EXTRAS_TO;
                    popupWindow_ExtrasList.showAsDropDown(v, 0, 0);
                    break;
                }
            }
        };

        // toExreas button
        buttonShowDropDown_fromExtras = (ImageButton) rootView.findViewById(R.id.from_extras);
        buttonShowDropDown_fromExtras.setOnClickListener(extras_handler);

        // fromExreas button
        buttonShowDropDown_toExtras = (ImageButton) rootView.findViewById(R.id.to_extras);
        buttonShowDropDown_toExtras.setOnClickListener(extras_handler);


	}
	 /*
     * 
     */
    public PopupWindow popupWindowDogs() {

        PopupWindow popupWindow = new PopupWindow(rootView, 
                               LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        // the drop down list is a list view
        ListView listViewExtras = new ListView(getActivity());
        
        // set our adapter and pass our pop up window contents
        listViewExtras.setAdapter(extrasAdapter(popUpContents));
        
        // set the item click listener
        listViewExtras.setOnItemClickListener(new ExtrasDropdownOnItemClickListener());

        // some other visual settings
        popupWindow.setFocusable(true);
        popupWindow.setWidth(250);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        
        // set the list view as pop up window content
        popupWindow.setContentView(listViewExtras);

        return popupWindow;
    }

    /*
     * adapter where the list values will be set
     */
    private ArrayAdapter<String> extrasAdapter(String dogsArray[]) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, dogsArray) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // setting the ID and text for every items in the list
                String item = getItem(position);

                // visual settings for the list item
                TextView listItem = new TextView(getContext());

                listItem.setText(item);
                listItem.setTag(item);
                listItem.setTextSize(22);
                listItem.setPadding(15, 15, 15, 15);
                listItem.setTextColor(Color.BLACK);
                listItem.setBackgroundColor(Color.WHITE);
                
                return listItem;
            }
        };
        
        return adapter;
    }
}
