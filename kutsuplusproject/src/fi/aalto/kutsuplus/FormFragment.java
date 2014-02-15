package fi.aalto.kutsuplus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FormFragment extends Fragment {

	private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.formfragment, container, false);
		return rootView;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
    }

	// Open the web browser for the www-users
	public void doOpenBrowser(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(getString(R.string.kutsuplus_url)));
		startActivity(browserIntent);
	}

}
