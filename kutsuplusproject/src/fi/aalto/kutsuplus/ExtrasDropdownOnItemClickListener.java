package fi.aalto.kutsuplus;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import fi.aalto.kutsuplus.FormFragment;
import fi.aalto.kutsuplus.MainActivity;
import fi.aalto.kutsuplus.R;
/*
 * Idea originally:
 * http://www.codeofaninja.com/2013/04/show-listview-as-drop-down-android.html
 */


public class ExtrasDropdownOnItemClickListener implements OnItemClickListener {

    
    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {

        // get the context and main activity to access variables
        Context mContext = v.getContext();
        MainActivity mainActivity = ((MainActivity) mContext);
        
        // add some animation when a list item was clicked
        Animation fadeInAnimation = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
        fadeInAnimation.setDuration(10);
        v.startAnimation(fadeInAnimation);
        
        // dismiss the pop up
        mainActivity.popupWindow_ExtrasList.dismiss();
        
        // get the text and set it as the button text
        String selectedItemText = ((TextView) v).getText().toString();
        if(mainActivity.extras_list==MainActivity.EXTRAS_FROM)
        {
          final AutoCompleteTextView fromView = (AutoCompleteTextView)mainActivity.findViewById(R.id.from);
          if(selectedItemText.equals("Current location"))
        	  fromView.setText("");
          else
              fromView.setText(selectedItemText);
        }
        else
        {
            final AutoCompleteTextView toView = (AutoCompleteTextView)mainActivity.findViewById(R.id.to);
            if(selectedItemText.equals("Current location"))
          	  toView.setText("");
            else
              toView.setText(selectedItemText);
        }
        
        String selectedItemTag = ((TextView) v).getTag().toString();
        Toast.makeText(mContext, selectedItemTag, Toast.LENGTH_SHORT).show();
        
    }

}