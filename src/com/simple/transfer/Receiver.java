package com.simple.transfer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.transfer.util.HttpURLConnectionUtil;
import com.transfer.util.ReceivedFile;

public class Receiver implements IReceiver {

	private long lastfetchtime = 0;
	private String clientid = "";
	private String sDirectory = "D:/receivedfiles/";
	HttpURLConnectionUtil http = new HttpURLConnectionUtil();
	ArrayList<JSONObject> recievedData = new <JSONObject>ArrayList();
	Map<String, ReceivedFile> receivedFiles = new HashMap<>();
	ILogMessage log;

	// public static void main(String[] args) {
	//
	// Receiver rs = new Receiver();
	// rs.receiveOneFile("1010");
	// }
	public Receiver(String sclientid, ILogMessage pmessagerec, String saveDir) {
		clientid = sclientid;
		log = pmessagerec;
		sDirectory = saveDir;
		
	}

	@Override
	public void receive() {
		recievedData = new <JSONObject>ArrayList();
		long lasttime = lastfetchtime;
		lastfetchtime = new Date().getTime() - 500;
		String s = getLatestParts(clientid, lasttime);

		if (s.length() > 1) {
			recievedData.addAll(splitLinesToObjects(s));
			try {
				receivedFiles = processContent(recievedData, receivedFiles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			receivedFiles = checkComplete(receivedFiles);
		}
	}

	private ArrayList<JSONObject> splitLinesToObjects(String sData) {
		ArrayList<JSONObject> jsonData = new <JSONObject>ArrayList();
		String[] as = sData.split("\\|\\|\\|");
		System.out.println("Zeilen =  " + as.length);
		ArrayList ar = new ArrayList();
		int index = 0;
		while (index < as.length) {
			JSONObject j;
			try {
				j = new JSONObject(as[index]);
				jsonData.add(j);
			} catch (JSONException e) {
				System.out.println("invalid line: " + as[index]);
				e.printStackTrace();
			}
			index++;
		}

		return jsonData;
	}

	private Map<String, ReceivedFile> processContent(ArrayList<JSONObject> recievedData3,
			Map<String, ReceivedFile> receivedFiles2) throws Exception {
		int index = 0;
		int max = recievedData3.size();
		for (index = 0; index < max; index++) {
			System.out.println("receivedData index: " + recievedData3.get(index));
			System.out.println("receivedData index: " + recievedData3.get(index).optInt("part"));
			JSONObject jPart = recievedData.get(index);
			// if it is first part of the file, then it has fname
			String id = jPart.optString("id");
			if (jPart.has("fname")) {
				if (!receivedFiles2.containsKey(id)) {
					ReceivedFile rfile = new ReceivedFile(jPart.optString("id"));

					rfile.setFname(jPart.optString("fname"));
					rfile.setMaxparts(jPart.optInt("partsMax"));
					rfile.getParts().put(jPart.optInt("part"), jPart.optString("data"));
					rfile.setFsize(jPart.optLong("size"));
					receivedFiles2.put(rfile.getFileid(), rfile);
					log.showMessage("Receiving " + rfile.getFname());
				}
			} else {
				if (receivedFiles2.containsKey(id)) {
					ReceivedFile rfile = receivedFiles2.get(id);
					rfile.parts.put(jPart.optInt("part"), jPart.optString("data"));
					receivedFiles2.put(id, rfile);
					int proz = (jPart.optInt("part") * 100) / rfile.getMaxparts();
					log.showMessage("Receiving " + rfile.getFname() + " Procent: " + proz);
					for (int n = 1; n <= jPart.optInt("part"); n++) {
						if (!rfile.parts.containsKey(n)) {
							log.showMessage("Error: Missing Parts in " + rfile.getFname() );
							throw new Exception("missing Part " + n);
							
						}
					}
				}
			}

		}
		return receivedFiles2;
	}

	private Map<String, ReceivedFile> checkComplete(Map<String, ReceivedFile> receivedFiles2) {
		Set<String> toRemove = new HashSet();
		//System.out.println("check complete");
		Set<String> ids = receivedFiles2.keySet();
		for (String id : ids) {
			ReceivedFile rfile = receivedFiles2.get(id);
			System.out.println("rfile.getFname().()" + rfile.getFname());
			System.out.println("rfile.getParts().size()" + rfile.getParts().size());
			System.out.println("rfile.getMaxparts()" + rfile.getMaxparts());
			if (rfile.getParts().size() == rfile.getMaxparts()) {
				//System.out.println("filecomplete");
				if (writeToFile(rfile)) {
					toRemove.add(id);
				}
			}
		}
		for (String id : toRemove) {
			//System.out.println("removing " + id);
			receivedFiles2.remove(id);
		}
		return receivedFiles2;
	}

	private boolean writeToFile(ReceivedFile rfile) {
		
		log.showMessage("Complete " + rfile.getFname());
		boolean bRet = true;
		ByteArrayOutputStream byteoutstream = new ByteArrayOutputStream();

		byte[] wholeFile = new byte[(int) rfile.getFsize()];
		ByteBuffer bufferFile = ByteBuffer.wrap(wholeFile);

		File file;

		StringBuffer sb = new StringBuffer();
		for (int index = 1; index <= rfile.getMaxparts(); index++) {
			byte[] valueDecoded = Base64.getDecoder().decode(rfile.parts.get(index));
			byteoutstream.write(valueDecoded, 0, valueDecoded.length);
		}
		FileOutputStream stream = null;
		try {
			log.showMessage("Writing to " + sDirectory + rfile.getFname());
			file = new File(sDirectory + rfile.getFname());
			//System.out.println(file.getAbsolutePath());
			if (file.exists()) {
				log.showMessage("File " + file.getAbsolutePath() + " already exists.");
				return true;
			}

			stream = new FileOutputStream(file);

		} catch (FileNotFoundException e) {
			bRet = false;
			e.printStackTrace();
		}
		try {
			try {
				stream.write(byteoutstream.toByteArray());
			} catch (IOException e) {
				bRet = false;
				e.printStackTrace();
			}
		} finally {
			try {
				stream.close();
				file = null;
			} catch (IOException e) {
				bRet = false;
				e.printStackTrace();
			}
		}
		return bRet;
	}

	private String getLatestParts(String paramClientid, long paramLastfetchtime) {
		String result = "";
		Date date = new Date();
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("clientid", paramClientid);
			jObj.put("clienttime", date.getTime());
			jObj.put("lastfetchtime", paramLastfetchtime);
			//System.out.println("sending " + jObj.toString());
			String base64encodedString = Base64.getEncoder().encodeToString(jObj.toString().getBytes());
			result = http.getFilePart(base64encodedString);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println("result = " + result);
		return result;
	}

}
