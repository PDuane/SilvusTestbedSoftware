import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class SC_GUI_Main {

	public static void main(String[] args) throws IOException {
		URL url = new URL("http://172.20.169.71/streamscape_api");
		URLConnection con = url.openConnection();
		HttpURLConnection http = (HttpURLConnection)con;
		http.setRequestMethod("POST");
		http.setDoOutput(true);
		
		String data = "{\"jsonrpc\":\"2.0\", \"method\":\"rssi_report_address\", \"id\":\"43335\"}";
		
		http.setFixedLengthStreamingMode(data.length());
		http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		http.connect();
		try(OutputStream os = http.getOutputStream()) {
		    os.write(data.getBytes());
		}
		
		try (InputStream is = http.getInputStream()) {
			String inpt = new String(is.readAllBytes());
			System.out.println(inpt);
			Map<String, String> vals = parseJSON(inpt);
			for (String k : vals.keySet()) {
				System.out.println(k + " --> " + vals.get(k));
			}
		}
	}
	
	public static Map<String, String> parseJSON(String json) {
		HashMap<String, String> map = new HashMap<>();
		
		String json_ctnt = json.substring(json.indexOf('{') + 1, json.indexOf('}'));
		
		String[] pairs = json_ctnt.split(",");
		int i, j = 0, l;
		for (i = 0; i < pairs.length; i++) {
			if (pairs[i].contains("[") && ! pairs[i].contains("]")) {
				for (j = i + 1; j < pairs.length; j++) {
					pairs[i] += pairs[j];
					if (pairs[i].contains("]")) break;
				}
				for (l = i + 1; l < pairs.length; l++) {
					if (l > pairs.length - (i - j)) pairs[l] = null;
					else pairs[l] = pairs[l + (i - j)];
				}
			}
		}
		
		
		for (String s : pairs) {
			String[] arr = s.split(":");
			String k = arr[0];
			String v = arr[1];
			
			int idx = -2;
			while (idx != 0 && (idx < -1 || k.charAt(idx - 1) == '\\')) {
				idx = k.indexOf('"');
			}
			k = k.substring(idx + 1);
			
			idx = -2;
			while (idx != 0 && (idx < -1 || k.charAt(idx - 1) == '\\')) {
				idx = k.indexOf('"');
			}
			k = k.substring(0, idx);
			
			idx = -2;
			while (idx != 0 && (idx < -1 || v.charAt(idx - 1) == '\\')) {
				idx = v.indexOf('[');
				if (idx < 0) idx = v.indexOf('"');
				else idx--;
			}
			v = v.substring(idx + 1);
			
			idx = -2;
			while (idx != 0 && (idx < -1 || v.charAt(idx - 1) == '\\')) {
				idx = v.indexOf(']');
				if (idx < 0) idx = v.indexOf('"');
				else idx++;
			}
			v = v.substring(0, idx);
			
			map.put(k, v);
		}
		
		return map;
	}
	
//	String[] parseJSONArray(String json) {
//		json.strip();
//		json = json.substring(1, json.length() - 1);
//		
//		return "";
//	}

}
