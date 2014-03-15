package fi.aalto.kutsuplus.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import blogspot.software_and_algorithms.stern_library.string.DamerauLevenshteinAlgorithm;

public class StreetSearchAdapter extends ArrayAdapter implements Filterable {

    private ArrayList<String> fullList;
    private ArrayList<String> copyOfTheList;
    private ArrayFilter streetNameFilter;
    DamerauLevenshteinAlgorithm dla=new DamerauLevenshteinAlgorithm(1,1,1,1);
    
    public StreetSearchAdapter(Context context, int resource, int textViewResourceId, List<String> streets) {

        super(context, resource, textViewResourceId, streets);
        fullList = (ArrayList<String>) streets;
        copyOfTheList = new ArrayList<String>(fullList);

    }

    public StreetSearchAdapter(FragmentActivity activity, int simpleListItem1,
			String[] streets) {
        super(activity, simpleListItem1, streets);
        fullList = new ArrayList<String>();
        for(String item:streets)
        	fullList.add(item);
        copyOfTheList = new ArrayList<String>(fullList);
		
	}

	@Override
    public int getCount() {
        return fullList.size();
    }

    @Override
    public String getItem(int position) {
        return fullList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public Filter getFilter() {
        if (streetNameFilter == null) {
            streetNameFilter = new ArrayFilter();
        }
        return streetNameFilter;
    }


    private class ArrayFilter extends Filter {
        private Object lock=new Object();

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (copyOfTheList == null) {
                synchronized (lock) {
                    copyOfTheList = new ArrayList<String>(fullList);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    ArrayList<String> list = new ArrayList<String>(copyOfTheList);
                    results.values = list;
                    results.count = list.size();
                }
            } else {
                final String prefixString = prefix.toString().toLowerCase();

                ArrayList<String> values = copyOfTheList;
                int count = values.size();

                ArrayList<String> newValues = new ArrayList<String>(count);

                for (int i = 0; i < count; i++) {
                    String item = values.get(i);
                    if (item.toLowerCase().startsWith(prefixString)) {
                        newValues.add(item);
                    }
                     }
                if(newValues.size()<1)
                for (int i = 0; i < count; i++) {
                    String item = values.get(i);
                       if(prefixString.length()>3)
                       {
                    	   int acc_slen=1;
                    	   if(prefixString.length()>5)
                    		   acc_slen=2;
                    	   String prefitem;
                    	   if(item.length()<prefixString.length())
                    		  prefitem=item;
                    	   else
                    	      prefitem=item.substring(0,prefixString.length()).toLowerCase();
                    	   
                    	   if(dla.execute(prefitem, prefixString)<(acc_slen+1))
                    	   {
                               newValues.add(item);
                           }			   
                    }
                 }
                if(newValues.size()<1)
                for (int i = 0; i < count; i++) {
                    String item = values.get(i);
                       if(prefixString.length()>2)
                       {
                    	   int acc_slen=1;
                    	   if(prefixString.length()>5)
                    		   acc_slen=4;
                    	   String prefitem;
                    	   if(item.length()<prefixString.length())
                    		  prefitem=item;
                    	   else
                    	      prefitem=item.substring(0,prefixString.length()).toLowerCase();
                    	   
                    	   if(dla.execute(prefitem, prefixString)<(acc_slen+1))
                    	   {
                               newValues.add(item);
                           }			   
                    }
                 }
                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

        if(results.values!=null){
        fullList = (ArrayList<String>) results.values;
        }else{
            fullList = new ArrayList<String>();
        }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}