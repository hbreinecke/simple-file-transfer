package com.simple.transfer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;

import com.transfer.util.HttpURLConnectionUtil;

public class Sender {

	private List doAllFiles(String dirPath) {
		List<String> fList = new ArrayList();
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		for (File aFile : files) {
			if (aFile.isFile()) {
				fList.add(aFile.getAbsolutePath());
			}
			if (aFile.isDirectory()) {
				fList.addAll(doAllFiles(aFile.getAbsolutePath()));
			}
		}
		return fList;
	}

	public void processFileList(List<String> fList, String clientid, ILogMessage log) {
		for (String fname : fList) {
			System.out.println(fname);
			Path path = Paths.get(fname);
			try {
				log.showMessage("Sending " +  path.getFileName().toString());
				byte[] data = Files.readAllBytes(path);
				sendData(clientid, path.getFileName().toString(), data);
				log.showMessage("Finished sending: " +  path.getFileName().toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendData(String sClient, String fName, byte[] data ) {
		int junksize = 1000;
		UUID idOne = UUID.randomUUID();
		int index = 0;
		int part = 1;
		int maxpart = (int) 2 + (data.length / junksize);
		if ((int) data.length / junksize == data.length / junksize) {
			maxpart--;
		}
		//System.out.println(" Size " + data.length + " parts " + maxpart);
		
		HttpURLConnectionUtil http = new HttpURLConnectionUtil();
		while (index < data.length) {
			int max = index + junksize;
			if (max > data.length) {
				max = data.length;
			}
			byte[] secondArray = Arrays.copyOfRange(data, index, max);
			String base64encodedString = Base64.getEncoder().encodeToString(secondArray);
			String sJson = "";
			try {
				JSONObject jObj = new JSONObject();
				if (part == 1) {
					jObj.put("clientid", sClient);
					jObj.put("fname", fName);
					jObj.put("part", part);
					jObj.put("partsMax", maxpart);
					jObj.put("size", data.length);
					jObj.put("id", idOne.toString());
					jObj.put("data", base64encodedString);

				} else {
					jObj.put("clientid", sClient);
					jObj.put("data", base64encodedString);
					jObj.put("part", part);
					jObj.put("id", idOne.toString());
				}
				System.out.println(jObj.toString());

				base64encodedString = Base64.getEncoder().encodeToString(jObj.toString().getBytes());
				http.sendFilePart(base64encodedString);
				Thread.sleep(50);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(base64encodedString);
			part++;
			index = index + junksize;
		}
		
	}

}
