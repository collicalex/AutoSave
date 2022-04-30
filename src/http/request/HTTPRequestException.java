package http.request;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

public class HTTPRequestException extends IOException {

	private static final long serialVersionUID = 2765163312156001611L;

	public HTTPRequestException(UnsupportedEncodingException e) {
		super(e);
	}

	public HTTPRequestException(MalformedURLException e) {
		super(e);
	}

	public HTTPRequestException(IOException e) {
		super(e);
	}

}
