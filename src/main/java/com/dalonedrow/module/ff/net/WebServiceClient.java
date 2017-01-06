package com.dalonedrow.module.ff.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.dalonedrow.engine.systems.base.JOGLErrorHandler;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFNpc;
import com.dalonedrow.module.ff.rpg.FFRoomData;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.flyweights.EquipmentItemModifier;
import com.dalonedrow.rpg.base.flyweights.ScriptAction;
import com.dalonedrow.utils.ArrayUtilities;
import com.google.gson.Gson;

/**
 * @author drau
 */
@SuppressWarnings({ "rawtypes" })
public final class WebServiceClient {
    /** the singleton instance of {@link WebServiceClient}. */
    private WebServiceClient instance;
	/**
	 * Gets the singleton instance.
     * @return {@link WebServiceClient}
     */
    public WebServiceClient getInstance() {
        if (instance == null) {
            instance = new WebServiceClient();
        }
        return instance;
    }
    /** the api properties. */
	private final Properties apiProperties;
	/** Hidden constructor. */
	WebServiceClient() {
		apiProperties = new Properties();
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream(
					"ff.properties");
			if (is != null) {
				apiProperties.load(is);
			} else {
				System.out.println("not found");
			}
		} catch (IOException e) {
			JOGLErrorHandler.getInstance().fatalError(e);
		}
	}
	public void get(final String uri, final Class classOfT)
	        throws ClientProtocolException, IOException, PooledException {
	    HttpClient client = HttpClientBuilder.create().build();
	    HttpGet request = new HttpGet(uri);
	    HttpResponse response = client.execute(request);
	    BufferedReader rd =
	            new BufferedReader(new InputStreamReader(
	                    response.getEntity().getContent()));
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
	    Gson gson = new Gson();
	    gson.fromJson(sb.toString(), classOfT);
	}
}
