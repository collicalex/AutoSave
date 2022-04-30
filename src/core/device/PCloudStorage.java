package core.device;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import http.crypto.Sha1;
import http.json.JSONArray;
import http.json.JSONObject;
import http.request.HTTPRequest;


/*
 * PCloud API :
 * https://docs.pcloud.com/protocols/http_json_protocol/
 * 
 * TODO ask to pCloud team in API: 
 *    - Manage 2FA
 *    - Manage crypto folder?
 *    - Manage .filename (not visible on website but visible in API) <--  on myPcloud, click on the grey circle in top right > Account > Display system files
 */
public class PCloudStorage extends Device implements FileTransfertListener {

	private String URL_UE = "eapi.pcloud.com";
	private String URL_US = "api.pcloud.com";	
	private String _url = null;
	
	private int METHOD_HTTP = 0;
	private int METHOD_HTTPS = 1;
	private int _method = -1;
	
	private int REGION_UE = 0;
	private int REGION_US = 1;	
	private int _region = -1;
	
	private String _auth = null;
	private String _targetPath = null;
	
	private Set<String> _pathCreated;
	
	//pcloud:region:method:path
	//pcloud:us:http:dir1/dir2
	//pcloud:us:https:dir3
	//pcloud:ue:http:dir4/dir5/dir6
	//pcloud:ue:https:dir7
	public PCloudStorage(String path) throws IOException {
		String pathParts[] = path.split(":");
		if (pathParts.length < 4) {
			throw new IOException("Path \"" + path + "\" must contains 4 parameters");
		}
		
		String storageType = pathParts[0];
		if ("pcloud".compareTo(storageType.toLowerCase()) != 0) {
			throw new IOException("Storage type \"" + storageType + "\" in path \'" + path + "\' is not \"pcloud\"");
		}
		
		if ("ue".compareToIgnoreCase(pathParts[1]) == 0) {
			_region = REGION_UE;
		} else if ("us".compareToIgnoreCase(pathParts[1]) == 0) { 
			_region = REGION_US;
		} else {
			throw new IOException("Region \"" + pathParts[1] + "\" in path \'" + path + "\' is not \"ue\" or \"us\"");
		}
		
		if ("http".compareToIgnoreCase(pathParts[2]) == 0) {
			_method = METHOD_HTTP;
		} else if ("https".compareToIgnoreCase(pathParts[2]) == 0) { 
			_method = METHOD_HTTPS;
		} else {
			throw new IOException("Method \"" + pathParts[2] + "\" in path \'" + path + "\' is not \"http\" or \"https\"");
		}
		
		_url = (_method == METHOD_HTTP) ? "http://" : "https://";
		_url += (_region == REGION_UE) ? URL_UE : URL_US;
		
		_targetPath = pathParts[3];
		_pathCreated = new HashSet<String>();
	}
	
	
	
	
	//-------------------------------------------------------------------------
	//-- Authentication methods
	//-------------------------------------------------------------------------
	
	@Override
	public boolean requiredCredentials() {
		return true;
	}

	@Override
	public void login(String username, String password) throws IOException {
		if (_method == METHOD_HTTP) {
			loginHTTP(username, password);
		} else if (_method == METHOD_HTTPS) {
			loginHTTPS(username, password);
		}
	}
	
	@Override
	public void logout() throws IOException {
		logout_();
	}
	
	
	//-------------------------------------------------------------------------
	//-- Count files
	//-------------------------------------------------------------------------	

	@Override
	public long countFiles(boolean recursive, List<Pattern> ignoredList) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	//-------------------------------------------------------------------------
	//-- Target file and path
	//-------------------------------------------------------------------------		
	
	private String computeTargetFullPath(String path) {
		path = path.replace("\\", "/");
		if (_targetPath.endsWith("/") || path.startsWith("/")) {
			return "/" + _targetPath + path;
		} else {
			return "/" + _targetPath + "/" + path;
		}
	}
	
	@Override
	public String getTargetFullPath(String path) {
		path = path.replace("\\", "/");
		return "pCloud:/" + computeTargetFullPath(path);
	}

	
	//-------------------------------------------------------------------------
	//-- Last modification date
	//-------------------------------------------------------------------------		

	private String _lastParentDirectoryStat = "";
	private Map<String, String> _filesStats;
	
	@Override
	public long lastModified(String path) throws IOException {
		String fullPath = computeTargetFullPath(path);
		
		//--Optimized version : 
		//- 1 - retrieve the stats of all files in a folder in one shot
		String parentDirectory = fullPath.substring(0, fullPath.lastIndexOf("/"));
		if (parentDirectory.compareTo(_lastParentDirectoryStat) != 0) {
			_lastParentDirectoryStat = parentDirectory;
			_filesStats = new HashMap<String, String>();			
			JSONObject fStats = listFolder(parentDirectory);
			JSONObject metadata = fStats.getJSONObject("metadata");
			if (metadata != null) {
				JSONArray array = metadata.getJSONArray("contents");
				if (array != null) {
					for (int i = 0; i < array.size(); ++i) {
						JSONObject stat = array.getJSONObject(i);
						if (stat.getBoolean("isfolder") == false) {
							String filepath = stat.getString("path").replaceAll("\\\\/", "/");
							String filePath2 = new String(filepath.getBytes(), "UTF-8");
							_filesStats.put(filePath2, stat.getString("modified"));
						}
					}
				}
			}
			
		}
		
		//- 2 - from the list, retrieve the stat of the file
		String modified = _filesStats.get(fullPath);
		if (modified == null) {
			return 0L;
		}
		long lastModified = parseDateRFC2822(modified).getTime();
			
		
		/*
		//-- Non-Optimized version : request the stat for each file
		JSONObject stat = stat(fullPath);
		JSONObject metadata = stat.getJSONObject("metadata");
		if (metadata == null) {
			return 0L;
		}
				
		long lastModified = parseDateRFC2822(metadata.getString("modified")).getTime();
		*/
		
		lastModified += 1000; //Add 1 second because pCloud does not save millisecond part.
		return lastModified;
	}
	
	//-------------------------------------------------------------------------
	//-- Copy one file
	//-------------------------------------------------------------------------

	@Override
	public void copyOneFile(File source, String path, List<FileTransfertListener> listeners) throws IOException {
		_listeners = listeners;
		String fullPath = computeTargetFullPath(path);
		
		String parentDirectory = fullPath.substring(0, fullPath.length() - source.getName().length());
		createFoldersTreeIfNotExists(parentDirectory);
		uploadFile(source, parentDirectory);
	}

	
	//--------------------------------------------------------------------------
	//-- FileTransferListener
	//--------------------------------------------------------------------------

	private List<FileTransfertListener> _listeners = null;

	@Override
	public void fileTransferStart(String path, long sizeToTransfert) {
		notifyListenersFileTransferStart(_listeners, path, sizeToTransfert);
	}

	@Override
	public void fileTransferPart(long sizeTransmitted) {
		notifyListenersFileTransferPart(_listeners, sizeTransmitted);
	}

	@Override
	public void fileTransferFinish() {
		notifyListenersFileTransferFinish(_listeners);
	}	
	
	
	
	//--------------------------------------------------------------------------
	//-- PCloud API Helpers
	//--------------------------------------------------------------------------
	
	
	private void createFoldersTreeIfNotExists(String path) throws IOException {
		//remove first slash if exists
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		
		//remove last slash if exists
		if (path.endsWith("/")) {
			path = path.substring(0, path.length()-1);
		}
		
		String pathParts[] = path.split("/");
		String currentPath = "";
		for (String part : pathParts) {
			currentPath += "/" + part;
			if (_pathCreated.contains(currentPath) == false) {
				createFolderIfNotExists(currentPath);
				_pathCreated.add(currentPath);
			}
		}
	}
	
	private Date parseDateRFC2822(String date) throws IOException {
		String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
		SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
		try {
			return format.parse(date);
		} catch (ParseException e) {
			throw new IOException(e);
		}
	}
	
	//--------------------------------------------------------------------------
	//-- PCloud API
	//--------------------------------------------------------------------------
	
	private void loginHTTPS(String username, String password) throws IOException {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("getauth", "1");
		parameters.put("logout", "1");
		parameters.put("username", username);
		parameters.put("password", password);
		
		String auth = HTTPRequest.request(_url + "/userinfo", parameters);
		
		JSONObject jobj2 = new JSONObject(auth);
		_auth = jobj2.getString("auth");
		if (_auth == null) {
			String errorMsg = jobj2.getString("error");
			if (errorMsg != null) {
				throw new IOException(errorMsg);
			} else {
				throw new IOException("Login failed");				
			}
		}
	}
	
	private void loginHTTP(String username, String password) throws IOException {
		String result = HTTPRequest.request(_url + "/getdigest");
		JSONObject jobj1 = new JSONObject(result);
		String digest = jobj1.getString("digest");
		
		String passworddigest = Sha1.encode(password + Sha1.encode(username) + digest);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("getauth", "1");
		parameters.put("logout", "1");
		parameters.put("username", username);
		parameters.put("digest", digest);
		parameters.put("passworddigest", passworddigest);
		
		String auth = HTTPRequest.request(_url + "/userinfo", parameters);
		
		JSONObject jobj2 = new JSONObject(auth);
		_auth = jobj2.getString("auth");
		if (_auth == null) {
			String errorMsg = jobj2.getString("error");
			if (errorMsg != null) {
				throw new IOException(errorMsg);
			} else {
				throw new IOException("Login failed");				
			}
		}	
	}
	
	
	private void logout_() throws IOException {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("auth", _auth);
		
		String result = HTTPRequest.request(_url + "/logout", parameters);
		System.out.println("PCLOUD LOGOUT");
		System.out.println(result);
	}
	
	private JSONObject stat(String path) throws IOException {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("path", path);
		
		String result = HTTPRequest.request(_url + "/stat", parameters);
		return new JSONObject(result);
	}
	
	private JSONObject listFolder(String path) throws IOException {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("path", path);
		parameters.put("recursive", "0");
		parameters.put("showdeleted", "0");
		
		String result = HTTPRequest.request(_url + "/listfolder", parameters);
		return new JSONObject(result);		
	}
	
	private void uploadFile(File file, String path) throws IOException {
		
		if (path.endsWith("/")) {
			path = path.substring(0, path.length()-1);
		}
		
		Map<String, String> urlParameters = new HashMap<String, String>();
		urlParameters.put("path", path);
		urlParameters.put("filename", file.getName());
		urlParameters.put("nopartial", "1");
		
		Map<String, Object> formParameters = new HashMap<String, Object>();
		formParameters.put(file.getName(), file);
		
		String result = HTTPRequest.request(_url + "/uploadfile", urlParameters, HTTPRequest.POST, formParameters, this);
		
		//Error: {	"result": 0,	"metadata": [	],	"checksums": [	],	"fileids": [	]}
		
		JSONObject jsonResult = new JSONObject(result);
		String error = jsonResult.getString("error");
		if (error != null) {
			throw new IOException(error);
		}		
		
	}
	
	private void createFolderIfNotExists(String path) throws IOException {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("path", path);
		
		String result = HTTPRequest.request(_url + "/createfolderifnotexists", parameters);
		JSONObject jsonResult = new JSONObject(result);
		String error = jsonResult.getString("error");
		if (error != null) {
			throw new IOException(error);
		}
	}



}
