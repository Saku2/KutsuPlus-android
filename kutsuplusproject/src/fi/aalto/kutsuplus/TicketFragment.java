package fi.aalto.kutsuplus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fi.aalto.kutsuplus.events.OTTOCommunication;
 
public class TicketFragment extends Fragment {
	private OTTOCommunication communication = OTTOCommunication.getInstance();
	private View rootView;
	private final String LOG_TAG = "kutsuplus" + this.getClass().getName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.formfragment, container, false);

		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onStart() {
		super.onStart();
	}



}