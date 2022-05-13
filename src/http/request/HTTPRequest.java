package http.request;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import core.device.FileTransfertListener;

public class HTTPRequest {
	
	private static boolean _debug = false;
	
	public static int GET = 0;
	public static int POST = 1;
	
	public static String request(String url) throws HTTPRequestException {
		return request(url, null, GET, null, null);
	}
	
	public static String request(String url, Map<String, String> parameters)  throws HTTPRequestException {
		return request(url, parameters, GET, null, null);
	}
	
	public static String request(String url, Map<String, String> urlParameters, int method, Map<String, Object> formParameters, FileTransfertListener listener) throws HTTPRequestException {
		
		if (listener == null) {
			listener = new DummyFileTransferListener();
		}
		
		String encodedUrlParameters = null;
		if (urlParameters != null) {
			try {
				encodedUrlParameters = getParamsString(urlParameters);
			} catch (UnsupportedEncodingException e) {
				throw new HTTPRequestException(e);
			}
		}
		
		if (encodedUrlParameters != null) {
			url += "?" + encodedUrlParameters;
		}
		
		if (_debug) {
			System.out.println(url);
		}
		
		URL urll = null;
		try {
			urll = new URL(url);
		} catch (MalformedURLException e) {
			throw new HTTPRequestException(e);
		}
		
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) urll.openConnection();
		} catch (IOException e) {
			throw new HTTPRequestException(e);
		}
		
		try {
			con.setRequestMethod("GET");
		} catch (ProtocolException e) {
			con.disconnect();
			throw new HTTPRequestException(e);
		}
		
		
		con.setConnectTimeout(5000);
		con.setReadTimeout(5000);
		
		if (method == GET) {
			con.setRequestProperty("Content-Type", "application/json");
		} else if (method == POST) {
			String boundary = "===" + System.currentTimeMillis() + "===";
			String LINE_FEED = "\r\n";
			
			con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			
			try {
				con.setFixedLengthStreamingMode(getContentLength(formParameters, boundary, LINE_FEED));
			} catch (UnsupportedEncodingException e1) {
				con.disconnect();
				throw new HTTPRequestException(e1);
			}
			
			
			con.setDoOutput(true);
			DataOutputStream out = null;
			try {
				out = new DataOutputStream(con.getOutputStream());
				
				if (formParameters != null) {
					for (Map.Entry<String, Object> entry : formParameters.entrySet()) {
						out.writeBytes("--" + boundary + LINE_FEED);
						
						if (entry.getValue() instanceof String) {
							//header
							out.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_FEED);
							out.writeBytes("Content-Type: text/plain;charset=UTF-8" + LINE_FEED);
							//white line
							out.writeBytes(LINE_FEED);
							out.flush();
							//content
							out.writeBytes((String)entry.getValue());
						} else if (entry.getValue() instanceof File) {
							File fileToUpload = (File) entry.getValue();
							//header
							out.writeBytes("Content-Disposition: form-data; filename=\"" + encodeFileName(entry.getKey()) + "\"" + LINE_FEED);
							out.writeBytes("Content-Transfer-Encoding: binary" + LINE_FEED);
							//white line
							out.writeBytes(LINE_FEED);
							out.flush();
							//content
							listener.fileTransferStart(fileToUpload.getAbsolutePath(), fileToUpload.length());
					        FileInputStream inputStream = new FileInputStream(fileToUpload);
					        byte[] buffer = new byte[4096];
					        int bytesRead = -1;
					        while ((bytesRead = inputStream.read(buffer)) != -1) {
					        	out.write(buffer, 0, bytesRead);
								listener.fileTransferPart(bytesRead);
					        }
					        out.flush();
					        inputStream.close();
							listener.fileTransferFinish();
						}
						out.writeBytes(LINE_FEED);
						out.flush();
					}
					
					out.writeBytes("--" + boundary + "--" + LINE_FEED);
				}

				out.flush();
				out.close();
			} catch (IOException e) {
				forceClose(out);
				con.disconnect();
				throw new HTTPRequestException(e);
			}
		}
		
		int status = -1;
		try {
			status = con.getResponseCode();
		} catch (IOException e) {
			con.disconnect();
			throw new HTTPRequestException(e);			
		}
		
		if (_debug) {
			System.out.println(status);
		}		

		BufferedReader in;
		if (status > 299) {
			in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		} else {
			try {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} catch (IOException e) {
				con.disconnect();
				throw new HTTPRequestException(e);
			}
		}
		
		String inputLine;
		StringBuffer content = new StringBuffer();
		try {
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			in.close();
		} catch (IOException e) {
			forceClose(in);
			con.disconnect();
			throw new HTTPRequestException(e);
		}
				
		con.disconnect();
		
		if (_debug) {
			System.out.println(content);
		}			
		
		return content.toString();
	}
	
	private static long getContentLength(String str) {
		return str.getBytes().length;
	}
	
	private static long getContentLength(File file) {
		return file.length();
	}
	
	private static long getContentLength(Map<String, Object> formParameters, String boundary, String LINE_FEED) throws UnsupportedEncodingException {
		long contentLenght = 0;
		
		if (formParameters != null) {
			for (Map.Entry<String, Object> entry : formParameters.entrySet()) {
				contentLenght += getContentLength("--" + boundary + LINE_FEED);
				
				if (entry.getValue() instanceof String) {
					//header
					contentLenght += getContentLength("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_FEED);
					contentLenght += getContentLength("Content-Type: text/plain;charset=UTF-8" + LINE_FEED);
					contentLenght += getContentLength(LINE_FEED);
					contentLenght += getContentLength((String)entry.getValue());
				} else if (entry.getValue() instanceof File) {
					File fileToUpload = (File) entry.getValue();
					contentLenght += getContentLength("Content-Disposition: form-data; filename=\"" + encodeFileName(entry.getKey()) + "\"" + LINE_FEED);
					contentLenght += getContentLength("Content-Transfer-Encoding: binary" + LINE_FEED);
					contentLenght += getContentLength(LINE_FEED);
					contentLenght += getContentLength(fileToUpload);
				}
				contentLenght += getContentLength(LINE_FEED);
			}
			
			contentLenght += getContentLength("--" + boundary + "--" + LINE_FEED);
		}
		return contentLenght;
	}
	
	private static String encodeFileName(String fileName) throws UnsupportedEncodingException {
		return new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
	}


	public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, String> entry : params.entrySet()) {
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			result.append("&");
		}
		
		String resultString = result.toString();
		return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
	}
	
	private static void forceClose(BufferedReader in) {
		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void forceClose(DataOutputStream out) {
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private static class DummyFileTransferListener implements FileTransfertListener {

		@Override
		public void fileTransferStart(String path, long sizeToTransfert) {
		}

		@Override
		public void fileTransferPart(long sizeTransmitted) {
		}

		@Override
		public void fileTransferFinish() {
		}
		
	}
	
}
