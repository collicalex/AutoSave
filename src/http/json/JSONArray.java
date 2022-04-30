package http.json;

import java.util.ArrayList;

public class JSONArray {

	private String _str;
	private ArrayList<Integer> _indexes;
	
	public JSONArray(String json) {
		_str = json;
		this.build();
	}
	
	private void build() {
		_indexes = new ArrayList<Integer>();
		_indexes.add(0);
		if (_str.compareTo("[]") == 0) {
			return ;
		}
		int level = 0;
		for (int i = 1; i < _str.length(); ++i) { //first char is [
			char c = _str.charAt(i);
			if (c == '{') {
				level++;
			} else if (c == '}') {
				level--;
			} else {
				if ((level == 0) && (c == ',')) {
					_indexes.add(i);
				}
			}
		}
		_indexes.add(_str.length()-1);
	}
	
	public int size() {
		return _indexes.size()-1;
	}

	public int length() {
		return size();
	}
	
	private String trimQuote(String str) {
		if (str.startsWith("\"") && str.endsWith("\"")) {
			return str.substring(1, str.length()-1);
		} else {
			return str;
		}
	}
	
	public Boolean getBoolean(int index) {
		String str = this.getString(index);
		if (str == null) {
			return null;
		}
		return Boolean.parseBoolean(trimQuote(str));
	}	
	
	public Integer getInt(int index) {
		String str = this.getString(index);
		if (str == null) {
			return null;
		}
		return Integer.parseInt(trimQuote(str));
	}
	
	public String getString(int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException(index + " is out of [;" + size() + "[");
		}
		int a = _indexes.get(index);
		int b = _indexes.get(index+1);
		return _str.substring(a+1, b);
	}
	
	public JSONObject getJSONObject(int index) {
		String str = this.getString(index);
		if (str != null) {
			if (str.trim().charAt(0) != '{') {
				str = "{" + str + "}";
			}
			return new JSONObject(str);
		} else {
			return null;
		}		
	}
	
	@Override
	public String toString() {
		return _str;
	}
}
