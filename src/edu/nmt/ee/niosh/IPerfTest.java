package edu.nmt.ee.niosh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class IPerfTest {

	public static final String LOCAL_RADIO_IP      = "local_radio_ip";
	public static final String LOCAL_RADIO_ID      = "local_radio_nodeid";
	public static final String DESTINATION_IP      = "dest_ip";
	public static final String DESTINATION_ID      = "dest_nodeid";
	public static final String DESCRIPTION         = "notes";
	public static final String THROUGHPUT_IP       = "throughput_ip";
	public static final String SENDER_THROUGHPUT   = "send_throughput";
	public static final String RECEIVER_THROUGHPUT = "recv_throughput";
	public static final String PACKET_LOSS         = "packet_loss";
	public static final String RADIO_THROUGHPUT    = "radio_throughput";
	public static final String DROP_RATE           = "loss_rate";
	public static final String RTT_AVERAGE         = "rtt_avg";
	public static final String RTT_MINIMUM         = "rtt_min";
	public static final String RTT_MAXIMUM         = "rtt_max";
	public static final String DISTANCE            = "distance";
	public static final String[] RSSI              = new String[] {
														"rssi_channel1",
														"rssi_channel2",
														"rssi_channel3",
														"rssi_channel4"
													 };
	public static final String INTERFERENCE        = "local_interence";
	public static final String FREQUENCY           = "sc_freq";
	public static final String BANDWIDTH           = "sc_bw";
	public static final String TX_POWER            = "tx_power";
	public static final String ACTUAL_TX_POWER     = "actual_tx_power";
	public static final String ROUTING_PATH        = "routing_path";
	public static final String WEAKEST_LINK        = "weakest_link";
	public static final String NOISE               = "noise";
	public static final String MCS                 = "remote_mcs";
	public static final String ITERATION           = "iteration";
	
	public static final String[] headers = new String[] {
			ITERATION, LOCAL_RADIO_IP, LOCAL_RADIO_ID, DESTINATION_IP, DESTINATION_ID, THROUGHPUT_IP,
			FREQUENCY, BANDWIDTH, TX_POWER, ACTUAL_TX_POWER,
			RTT_AVERAGE, RTT_MINIMUM, RTT_MAXIMUM,
			SENDER_THROUGHPUT, RECEIVER_THROUGHPUT, PACKET_LOSS, RADIO_THROUGHPUT, DROP_RATE,
			DISTANCE, RSSI[0], RSSI[1], RSSI[2], RSSI[3], INTERFERENCE, NOISE,
			ROUTING_PATH, WEAKEST_LINK, MCS, DESCRIPTION
	};
	
	public static void main(String[] args) throws InterruptedException {
		
		@SuppressWarnings("unused")
		DCGui gui = new DCGui() {
			private static final long serialVersionUID = 7417025818575100353L;

			@Override
			public void onRun(int iteration) {
				String localRadioIP = getLocalIp();
				String address = getRemoteIp();
				
				boolean doThroughputTest = shouldRunIperf();
				String iperfServerIp;
				if (doThroughputTest) {
					iperfServerIp = getIperfIp();
				} else {
					iperfServerIp = null;
				}
				
				double distance = getDistance();
				
				HashMap<String, Object> data = new HashMap<>();
				
				boolean isScRadio = true;
				String localNodeId, nodeId = null;
				
				String otptFile = getOutputFilename();
				boolean append = shouldAppend();
				
				JSONParser parser = new JSONParser();
				
				try {
					data.put(ITERATION, iteration);
					data.put(DESCRIPTION, getNotes());
					data.put(DISTANCE, distance);
					setRunDialogText("Running Ping latency test");
					System.out.println("Running Ping Latency Test");
					HashMap<String, Long> ping_res = ping(address);
					if (ping_res == null) {
						System.err.println("Could not connect to " + address);
					}
					data.put(RTT_AVERAGE, ping_res.get("rtt_avg"));
					data.put(RTT_MINIMUM, ping_res.get("rtt_min"));
					data.put(RTT_MAXIMUM, ping_res.get("rtt_max"));
					
					setRunDialogText("Retreiving local radio ID");
					localNodeId = streamcaster(localRadioIP, "", "nodeid", false);
					JSONArray arr;
					JSONObject resp = (JSONObject) parser.parse(localNodeId);
					data.put(LOCAL_RADIO_IP, localRadioIP);
					if (resp.containsKey("error")) {
						data.put(LOCAL_RADIO_ID, "error");
					} else {
						localNodeId = (String) ((JSONArray)resp.get("result")).get(0);
						data.put(LOCAL_RADIO_ID, localNodeId);
					}
					
					setRunDialogText("Retreiving frequency and bandwidth");
					resp = (JSONObject) parser.parse(streamcaster(localRadioIP, localNodeId, "freq_bw", false));
					if (resp.containsKey("error")) {
						data.put(FREQUENCY, "error");
						data.put(BANDWIDTH, "error");
					} else {
						arr = (JSONArray)resp.get("result");
						data.put(FREQUENCY, Integer.parseInt((String) arr.get(0)));
						data.put(BANDWIDTH, Integer.parseInt((String) arr.get(1)));
					}
					
					setRunDialogText("Retreiving ambient noise level");
					resp = (JSONObject) parser.parse(streamcaster(localRadioIP, localNodeId, "noise_level", false));
					if (resp.containsKey("error")) {
						data.put(NOISE, "error");
					} else {
						arr = (JSONArray)resp.get("result");
						data.put(NOISE, arr.get(0));
					}
					
					setRunDialogText("Retreiving transmit power");
					resp = (JSONObject) parser.parse(streamcaster(localRadioIP, localNodeId, "power_dBm", false));
					if (resp.containsKey("error")) {
						data.put(TX_POWER, "error");
					} else {
						arr = (JSONArray)resp.get("result");
						data.put(TX_POWER, arr.get(0));
					}
					
					setRunDialogText("Retreiving actual transmit power");
					resp = (JSONObject) parser.parse(streamcaster(localRadioIP, localNodeId, "read_power_dBm", false));
					if (resp.containsKey("error")) {
						data.put(ACTUAL_TX_POWER, "error");
					} else {
						arr = (JSONArray)resp.get("result");
						data.put(ACTUAL_TX_POWER, arr.get(0));
					}
					
					setRunDialogText("Retreiving local interference");
					resp = (JSONObject) parser.parse(streamcaster(localRadioIP, localNodeId, "local_stats_interference", false));
					if (resp.containsKey("error")) {
						data.put(INTERFERENCE, "error");
					} else {
						arr = (JSONArray)resp.get("result");
						data.put(INTERFERENCE, arr.get(0));
					}
					
					setRunDialogText("Retreiving data drop rate");
					resp = (JSONObject) parser.parse(streamcaster(localRadioIP, localNodeId, "local_stats_input_dropped", false));
					if (resp.containsKey("error")) {
						data.put(DROP_RATE, "error");
					} else {
						arr = (JSONArray)resp.get("result");
						data.put(DROP_RATE, arr.get(0));
					}
					System.out.println(resp.toJSONString());
				} catch (ConnectException e) {
					System.err.println("Not connected to a StreamCaster radio");
					return;
				} catch (ParseException e) {
					e.printStackTrace();
					return;
				}
				
				try {
					try {
						streamcaster_login(address);
					} catch (IOException e) {
						e.printStackTrace();
					}
					setRunDialogText("Determining if remote device is an SC radio");
					nodeId = streamcaster(address, "", "nodeid", true);
					JSONObject resp = (JSONObject) parser.parse(nodeId);
					nodeId = (String) ((JSONArray)resp.get("result")).get(0);
					
				} catch (ConnectException e) {
					isScRadio = false;
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				try {
					if (isScRadio) {
						System.out.println("It is a StreamCaster radio");
						try {
							String[] parameters;
							JSONObject resp;
							JSONArray arr;
							
							parameters = new String[] {nodeId, "2"};
							setRunDialogText("Node " + nodeId + ": Retreiving throughput from radio");
							resp = (JSONObject) parser.parse(streamcaster(localRadioIP, localNodeId, "link_throughput", parameters, true));
							if (resp.containsKey("error")) {
								data.put(RADIO_THROUGHPUT, "error");
								System.err.println("Received error for " + RADIO_THROUGHPUT);
							} else {
								arr = (JSONArray)resp.get("result");
								data.put(RADIO_THROUGHPUT, arr.get(0));
							}
							
							if (doThroughputTest) {
								setRunDialogText("Running IPerf throuhgput test");
								System.out.println("Running IPerf Throuhgput Test");
								data.put(THROUGHPUT_IP, iperfServerIp);
								HashMap<String, Double> iperf_res = iperf(iperfServerIp);
								if (iperf_res == null) {
									System.err.println("IPerf command erred");
									data.put(SENDER_THROUGHPUT, "error");
									data.put(RECEIVER_THROUGHPUT, "error");
									data.put(PACKET_LOSS, "error");
								} else {
									data.put(SENDER_THROUGHPUT, iperf_res.get("send_bps"));
									data.put(RECEIVER_THROUGHPUT, iperf_res.get("recv_bps"));
									data.put(PACKET_LOSS, (1.0 - iperf_res.get("pckt_loss")) * 100);
								}
							} else {
								data.put(THROUGHPUT_IP, "");
							}
								
							parameters = new String[] {nodeId};
							setRunDialogText("Node " + nodeId + ": Retreiving RSSI");
							resp = (JSONObject) parser.parse(streamcaster(localRadioIP, localNodeId, "nbr_rssi", parameters, true));
							if (resp.containsKey("error")) {
								for (int i = 0; i < RSSI.length; i++) {
									data.put(RSSI[i], "error");
								}
								System.err.println("Received error for rssi");
							} else {
								for (int i = 0; i < RSSI.length; i++) {
									data.put(RSSI[i], "");
								}
								arr = (JSONArray)resp.get("result");
								for (int i = 0; i < arr.size(); i++) {
									data.put(RSSI[i], arr.get(i));
								}
							}
							
							// Get remote noise value
							parameters = new String[] {nodeId};
							setRunDialogText("Node " + nodeId + ": Retreiving RSSI");
							resp = (JSONObject) parser.parse(streamcaster(address, nodeId, "noise_level", parameters, true));
							if (resp.containsKey("error")) {
								for (int i = 0; i < RSSI.length; i++) {
									data.put(RSSI[i], "error");
								}
								System.err.println("Received error for rssi");
							} else {
								for (int i = 0; i < RSSI.length; i++) {
									data.put(RSSI[i], "");
								}
								arr = (JSONArray)resp.get("result");
								for (int i = 0; i < arr.size(); i++) {
									data.put(RSSI[i], arr.get(i));
								}
							}
							
							setRunDialogText("Node " + nodeId + ": Retreiving weakest link along path");
							parameters = new String[] {nodeId};
							String str = streamcaster(localRadioIP, localNodeId, "weakest_link", parameters, true);
							System.out.println("Weakest Link: " + str);
							resp = (JSONObject) parser.parse(str);
							if (resp.containsKey("error")) {
								data.put(WEAKEST_LINK, "error");
								System.err.println("Received error for " + WEAKEST_LINK);
							} else {
								arr = (JSONArray)resp.get("result");
								if (arr.size() > 0) {
									data.put(WEAKEST_LINK, arr.toString());
								} else {
									data.put(WEAKEST_LINK, "");
								}
							}
							
							setRunDialogText("Node " + nodeId + ": Retreiving MCS value");
							parameters = new String[] {nodeId};
							resp = (JSONObject) parser.parse(streamcaster(localRadioIP, localNodeId, "nbr_mcs", parameters, true));
							if (resp.containsKey("error")) {
								data.put(MCS, "error");
								System.err.println("Received error for " + MCS);
							} else {
								arr = (JSONArray)resp.get("result");
								if (arr.size() > 0) {
									data.put(MCS, arr.get(0));
								} else {
									data.put(MCS, "");
								}
							}
							
							setRunDialogText("Node " + nodeId + ": Retreiving routing path");
							parameters = new String[] {nodeId};
							resp = (JSONObject) parser.parse(streamcaster(localRadioIP, localNodeId, "routing_path", parameters, true));
							if (resp.containsKey("error")) {
								data.put(ROUTING_PATH, "error");
								System.err.println("Received error for " + ROUTING_PATH);
							} else {
								String hops = "";
								arr = (JSONArray)resp.get("result");
								for (int i = 0; i < arr.size(); i++) {
									hops += (i == 0 ? "" : ",") + arr.get(i);
								}
								data.put(ROUTING_PATH, hops);
							}
							
							System.out.println(resp.toJSONString());
							
						} catch (ParseException e) {
							System.err.println("Unable to parse radio response");
							e.printStackTrace();
							return;
						}
					} else {
						System.out.println("Not a StreamCaster Radio");
						if (!doThroughputTest) {
							System.out.println("Note: You have not opted to do a throughput test.");
							System.out.println("      Only the ping test has been run.");
						} else {
							setRunDialogText("Not an SC radio. Running IPerf throughput test");
							if (iperfServerIp == null || iperfServerIp.equals("")) {
								iperfServerIp = address;
							}
							HashMap<String, Double> iperf_res = iperf(address);
							data.put(SENDER_THROUGHPUT, iperf_res.get("send_bps"));
							data.put(RECEIVER_THROUGHPUT, iperf_res.get("recv_bps"));
							data.put(PACKET_LOSS, (1.0 - iperf_res.get("pckt_loss")) * 100);
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				for (String s : headers) {
					data.putIfAbsent(s, "");
				}
				
				try {
					setRunDialogText("Writing data to file");
					File otpt = new File(otptFile);
					if (otpt.exists()) otpt.createNewFile();
					
					FileOutputStream fos = new FileOutputStream(otpt, append);
					if (!append) {
						for (int i = 0; i < headers.length; i++) {
							if (i != 0)  fos.write("|".getBytes());
							fos.write(headers[i].getBytes());
						}
						fos.write("\r\n".getBytes());
					}
					for (int i = 0; i < headers.length; i++) {
						if (i != 0)  fos.write("|".getBytes());
						fos.write(data.get(headers[i]).toString().getBytes());
					}
					fos.write("\r\n".getBytes());
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				setRunDialogText("Done");
				setDialogDone(true);
			}
		};
	}
	
	private static CookieManager cookieManager = new CookieManager();
	
	public static void streamcaster_login(String ip) throws IOException {
		URL url = new URL("http://" + ip + "/login.sh?username=admin&password=HelloWorld&Submit=1");
		URLConnection con = url.openConnection();
		HttpURLConnection http = (HttpURLConnection)con;
		http.setRequestMethod("GET");
		http.setDoOutput(true);
		
		http.connect();
		
//		String str = new String(http.getInputStream().readAllBytes());
		List<String> cookies = http.getHeaderFields().get("Set-Cookie");
		
		for (String cookie : cookies) {
			System.out.println(cookie);
			cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
		}
	}
	
	public static String streamcaster(String ip, String id, String method, boolean needsSecurityCookie) throws ConnectException {
		return streamcaster(ip, id, method, null, needsSecurityCookie);
	}
	
	@SuppressWarnings("unchecked")
	public static String streamcaster(String ip, String id, String method, String[] parameters, boolean needsSecurityCookie) throws ConnectException {
		try {
			URL url = new URL("http://" + ip + "/streamscape_api");
			URLConnection con = url.openConnection();
			HttpURLConnection http = (HttpURLConnection)con;
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			
			JSONObject scCmd = new JSONObject();
			scCmd.put("jsonrpc", "2.0");
			scCmd.put("method", method);
			scCmd.put("id", id);
			
			if (parameters != null) {
				JSONArray jArr = new JSONArray();
				for (int i = 0; i < parameters.length; i++) {
					jArr.add(parameters[i]);
				}
				scCmd.put("params", jArr);
			}
			
			String params = scCmd.toJSONString();
			
			http.setFixedLengthStreamingMode(params.length());
			http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			String cookies = "";
			if (needsSecurityCookie) {
				for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
					System.out.println(cookie.toString());
					cookies += cookie.toString() + ";";
				}
				System.out.println("Cookies: " + cookies);
				http.setRequestProperty("Cookie", cookies);
			}
			http.connect();
			try(OutputStream os = http.getOutputStream()) {
			    os.write(params.getBytes());
			}
			
			try (InputStream is = http.getInputStream()) {
				return new String(is.readAllBytes());
			}
		
		} catch (ConnectException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getParamString(Map<String, String> params) throws UnsupportedEncodingException {
		String str = "";
		String[] keySet = new String[params.keySet().size()];
		params.keySet().toArray(keySet);
		for (int i = 0; i < keySet.length; i++) {
			str += (i == 0 ? "" : "&") + URLEncoder.encode(keySet[0], "UTF-8");
			str += "=" + URLEncoder.encode(params.get(keySet[0]), "UTF-8");
		}
		
		return str;
	}
	
	public static HashMap<String, Double> iperf(String ip) throws IOException {
		double senderBps, receiverBps, packetLoss;
		String command = String.format("iperf3 -J -c %s", ip);
		System.out.println(command);
		Process proc = Runtime.getRuntime().exec(command);
		while (proc.getInputStream().available() < 1);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		String json = new String(proc.getInputStream().readAllBytes());
		
		if (proc.isAlive()) proc.destroy();
		else if (proc.exitValue() != 0) {
			System.err.println("iperf Command exited with nonzero value: " + proc.exitValue());
			System.err.println(new String(proc.getErrorStream().readAllBytes()));
			System.err.println(json);
//			System.err.println(new String(proc.getInputStream().readAllBytes()));
			return null;
		}
		
		JSONObject jobj;
		try {
			jobj = (JSONObject) new JSONParser().parse(json);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		
		JSONObject end = (JSONObject)jobj.get("end");
		
		senderBps = doubleize(((JSONObject)end.get("sum_sent")).get("bits_per_second"));
		receiverBps = doubleize(((JSONObject)end.get("sum_received")).get("bits_per_second"));
		
		long senderPackets = (long)((JSONObject)end.get("sum_sent")).get("bytes");
		long receiverPackets = (long)((JSONObject)end.get("sum_received")).get("bytes");
		
		packetLoss = (receiverPackets * 1.0) / senderPackets;
		
		HashMap<String, Double> map = new HashMap<>();
		map.put("send_bps", senderBps);
		map.put("recv_bps", receiverBps);
		map.put("pckt_loss", packetLoss);
		
		return map;
	}
	
	public static HashMap<String, Long> ping(String ip) {
		long rttMin, rttMax, rttAvg;
		try {
			int pingCount = 10;
			String command = String.format("ping -n %d %s", pingCount, ip);
			Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();
			
			if (proc.exitValue() != 0) {
				System.err.println("ping Command exited with nonzero value: " + proc.exitValue());
				System.err.println(new String(proc.getInputStream().readAllBytes()));
				return null;
			}
			
			String otpt = new String(proc.getInputStream().readAllBytes());
			otpt = otpt.substring(otpt.lastIndexOf('\n', otpt.length() - 2) + 1, otpt.length() - 2).strip();
			
			String[] otpt_split = otpt.split(",");
			for (int i = 0; i < otpt_split.length; i++) {
				otpt_split[i] = otpt_split[i].strip();
				otpt_split[i] = otpt_split[i].substring(otpt_split[i].indexOf('=') + 1).strip();
				otpt_split[i] = otpt_split[i].substring(0, otpt_split[i].length() - 2);
			}
			
			rttMin = Long.parseLong(otpt_split[0]);
			rttMax = Long.parseLong(otpt_split[1]);
			rttAvg = Long.parseLong(otpt_split[2]);
			
			HashMap<String, Long> map = new HashMap<>();
			map.put("rtt_avg", rttAvg);
			map.put("rtt_min", rttMin);
			map.put("rtt_max", rttMax);
			
			return map;
			
//			System.out.println(otpt);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static double doubleize(Object obj) {
		if (obj instanceof Double) {
			return ((Double)obj).doubleValue();
		} else if (obj instanceof Long) {
			return ((Long)obj).doubleValue();
		} else if (obj instanceof Integer) {
			return ((Integer)obj).doubleValue();
		} else if (obj instanceof String) {
			return Double.parseDouble((String)obj);
		} else {
			return Double.NaN;
		}
	}
	
	public static String unitize(double value, int decimals) {
		int lVal = (int)Math.floor(Math.log10(value));
		lVal /= 3;
		
		String unit;
		
		switch(lVal) {
		case -4:
			unit = "p";
			break;
		case -3:
			unit = "n";
			break;
		case -2:
			unit = "u";
			break;
		case -1:
			unit = "m";
			break;
		case 1:
			unit = "k";
			break;
		case 2:
			unit = "M";
			break;
		case 3:
			unit = "G";
			break;
		case 4:
			unit = "T";
			break;
		default:
			unit = "";
			break;
		}
		
		return String.format("%." + decimals + "f %s", value / (Math.pow(10, lVal * 3)), unit);
	}

}
