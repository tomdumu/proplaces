package gerber.uchicago.edu.yelp;

import com.google.gson.Gson;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import gerber.uchicago.edu.YelpResultsData;

/**
 * Created by slim on 15-5-23.
 */
public class Yelp {
    //these are my developers keys, please get your own by going to: http://www.yelp.com/developers/getting_started/api_access
    private static final String CONSUMER_KEY = "dSZgGbpE51gcJ2mPFy8Dag";
    private static final String CONSUMER_SECRET = "CAe7Yp1NEYVPh2Z2ZpDDetqUpWM";
    private static final String TOKEN = "ksJ-aFEUA-sO8YKI9TwbTem8DoLOOtH0";
    private static final String TOKEN_SECRET = "O1oqDGf93zFEz-_ctYgicO1VYQM";

    OAuthService service;
    Token accessToken;

//        /**
//         * Setup the Yelp API OAuth credentials.
//         *
//         * OAuth credentials are available from the developer site, under Manage API access (version 2 API).
//         *
//         * @param consumerKey Consumer key
//         * @param consumerSecret Consumer secret
//         * @param token Token
//         * @param tokenSecret Token secret
//         */
//        public Yelp(String consumerKey, String consumerSecret, String token, String tokenSecret) {
//            this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
//            this.accessToken = new Token(token, tokenSecret);
//        }

    public Yelp(){
        this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(CONSUMER_KEY).apiSecret(CONSUMER_SECRET).build();
        this.accessToken = new Token(TOKEN, TOKEN_SECRET);
    }


    public YelpResultsData searchMultiple(String searchTerm, String city) {

        // Execute a signed call to the Yelp service.
        OAuthService service = new ServiceBuilder().provider(YelpApi2.class).apiKey(CONSUMER_KEY).apiSecret(CONSUMER_SECRET).build();
        Token accessToken = new Token(TOKEN, TOKEN_SECRET);
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");

        request.addQuerystringParameter("location", city);
        //request.addQuerystringParameter("category", "restaurants");
        request.addQuerystringParameter("term", searchTerm);
        request.addQuerystringParameter("limit", "20");

        service.signRequest(accessToken, request);
        Response response = request.send();
        String rawData = response.getBody();

        YelpResultsData mYelpSearchResult = null;

        try {
            mYelpSearchResult = new Gson().fromJson(rawData, YelpResultsData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mYelpSearchResult;
    }



}
