package com.transfer.util;

import java.util.HashMap;
import java.util.Map;

public class ReceivedFile {
	public ReceivedFile(String id){
		this.fileid = id;
	}
	private int maxparts =0;
	private String fileid ="";
	private String fname = "";
	private long  fsize = 0;
	
	 
	public Map<Integer, String> parts = new HashMap();
	
	public int getMaxparts() {
		return maxparts;
	}
	public void setMaxparts(int maxparts) {
		this.maxparts = maxparts;
	}
	public String getFileid() {
		return fileid;
	}
	 
	public String getFname() {
		return fname;
	}
	public void setFname(String fname) {
		this.fname = fname;
	}
	public Map<Integer, String> getParts() {
		return parts;
	}
	public void setParts(Map<Integer, String> parts) {
		this.parts = parts;
	}
	public long getFsize() {
		return fsize;
	}
	public void setFsize(long fsize) {
		this.fsize = fsize;
	}
	public void setFileid(String fileid) {
		this.fileid = fileid;
	}
}
