package fi.aalto.kutsuplus.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import fi.aalto.kutsuplus.events.OTTOCommunication;
import fi.aalto.kutsuplus.kdtree.GoogleMapPoint;
import fi.aalto.kutsuplus.kdtree.MapPoint;
import fi.aalto.kutsuplus.kdtree.StopObject;
import fi.aalto.kutsuplus.kdtree.StopTreeHandler;

public class ReittiopasHttpHandler {
	private OTTOCommunication communication = OTTOCommunication.getInstance();

	public ReittiopasHttpHandler() {

	}

	public void makeGetStartAddress(String url, List<NameValuePair> params) {

		ReittiopasGetStartAddressTask task = new ReittiopasGetStartAddressTask();
		task.execute(url, params);

	}

	public void makeGetEndAddress(String url, List<NameValuePair> params) {

		ReittiopasGetEndAdressTask task = new ReittiopasGetEndAdressTask();
		task.execute(url, params);

	}

	private class ReittiopasGetStartAddressTask extends AsyncTask<Object, Integer, String> {
		protected String doInBackground(Object... args) {
			String response = "";
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpEntity httpEntity = null;
				HttpResponse httpResponse = null;
				String url = (String) args[0];
				List<NameValuePair> params = (List<NameValuePair>) args[1];
				if (params != null) {
					String paramString = URLEncodedUtils.format(params, "utf-8");
					url += "?" + paramString;
				}
				HttpGet httpGet = new HttpGet(url);
				httpResponse = httpClient.execute(httpGet);
				httpEntity = httpResponse.getEntity();
				response = EntityUtils.toString(httpEntity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return response;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(String result) {
			try {
				JSONArray jsonArray = null;
				JSONObject json = null;

				jsonArray = new JSONArray(result);
				json = jsonArray.getJSONObject(0);

				String[] coords = json.getString("coords").split(",");
				// latitude is a geographic coordinate that
				// specifies the north-south position of a point
				String longtitude = coords[0];
				String latitude = coords[1];
				try {
					MapPoint mp = new MapPoint(Integer.parseInt(longtitude), Integer.parseInt(latitude));
					StopObject pickupStop_so = StopTreeHandler.getInstance().getClosestStops(mp, 1)[0].getNeighbor().getValue();
					GoogleMapPoint gmp = CoordinateConverter.kkj2xy_to_wGS84lalo(mp.getX(), mp.getY());
					LatLng ll = new LatLng(gmp.getX(), gmp.getY());

					communication.setStart_location(OTTOCommunication.FORM_FRAGMENT, ll);
					communication.setPick_up_stop(OTTOCommunication.FORM_FRAGMENT, pickupStop_so);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (JSONException je) {
				je.printStackTrace();
			}

		}

	}

	private class ReittiopasGetEndAdressTask extends AsyncTask<Object, Integer, String> {
		protected String doInBackground(Object... args) {
			String response = "";
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpEntity httpEntity = null;
				HttpResponse httpResponse = null;
				String url = (String) args[0];
				List<NameValuePair> params = (List<NameValuePair>) args[1];
				if (params != null) {
					String paramString = URLEncodedUtils.format(params, "utf-8");
					url += "?" + paramString;
				}
				HttpGet httpGet = new HttpGet(url);
				httpResponse = httpClient.execute(httpGet);
				httpEntity = httpResponse.getEntity();
				response = EntityUtils.toString(httpEntity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return response;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(String result) {
			try {
				JSONArray jsonArray = null;
				JSONObject json = null;

				jsonArray = new JSONArray(result);
				json = jsonArray.getJSONObject(0);

				String[] coords = json.getString("coords").split(",");
				// latitude is a geographic coordinate that
				// specifies the north-south position of a point
				String longtitude = coords[0];
				String latitude = coords[1];
				try {
					MapPoint mp = new MapPoint(Integer.parseInt(longtitude), Integer.parseInt(latitude));
					StopObject dropoffStop_so = StopTreeHandler.getInstance().getClosestStops(mp, 1)[0].getNeighbor().getValue();
					GoogleMapPoint gmp = CoordinateConverter.kkj2xy_to_wGS84lalo(mp.getX(), mp.getY());
					LatLng ll = new LatLng(gmp.getX(), gmp.getY());

					communication.setEnd_location(OTTOCommunication.FORM_FRAGMENT, ll);
					communication.setDrop_off_stop(OTTOCommunication.FORM_FRAGMENT, dropoffStop_so);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (JSONException je) {
				je.printStackTrace();
			}

		}

	}

}