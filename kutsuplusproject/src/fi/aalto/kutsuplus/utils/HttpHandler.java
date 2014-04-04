package fi.aalto.kutsuplus.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class HttpHandler {

	public HttpHandler() {

	}

	public String makeHttpGet(String url, List<NameValuePair> params) {

		String response = "";
		ReittiopasGetTask task = new ReittiopasGetTask();
		task.execute(url, params);
		try {
			response = task.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return response;
		
	}
	
	private class ReittiopasGetTask extends AsyncTask<Object, Integer, String> {
	     protected String doInBackground(Object... args) {
	 		String response = "";
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpEntity httpEntity = null;
				HttpResponse httpResponse = null;
				String url = (String)args[0];
				List<NameValuePair> params = (List<NameValuePair>)args[1];
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
	     }
	 }
}