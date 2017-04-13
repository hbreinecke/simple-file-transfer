package com.simple.transfer;

import javax.swing.JTextArea;

public class MessageLogger implements ILogMessage {

	ILogMessage parent ;

	public void setParent(ILogMessage parent) {
		this.parent = parent;
	}

	@Override
	public void showMessage(String message) {
		parent.showMessage(message);
		
	}


}
