package gerber.uchicago.edu;

import java.util.List;

/**
 * Created by Adam Gerber on 5/20/2014.
 * University of Chicago
 */
public class GoogleResultsData {
    public ResponseData  responseData;
    public class ResponseData {
        public List<Result> results;
        public class Result {
            public String unescapedUrl;
        }
    }


}
