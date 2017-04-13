package com.simple.transfer;

import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class ReceiverFrontend extends JFrame implements ILogMessage {

	public static void main(String[] args) {
		ReceiverFrontend rf = new ReceiverFrontend();

	}

	RunnableThread runner;
	Receiver rec;
	JTextArea messagefield;

	public ReceiverFrontend() {
		JLabel label1 = new JLabel("Name");
		label1.setBounds(0, 0, 50, 20);
		JLabel label2 = new JLabel("Status");
		label2.setBounds(0, 50, 50, 20);
		JLabel label3 = new JLabel("Client");
		label3.setBounds(0, 100, 50, 20);
		JLabel label4 = new JLabel("Logs");
		label4.setBounds(0, 250, 50, 20);
		JLabel label5 = new JLabel("SaveTo");
		label5.setBounds(0, 70, 50, 20);
		
		JFrame f = new JFrame("File Receiver v1.0");

		final JTextField username = new JTextField();
		username.setBounds(50, 00, 150, 20);
		username.setText("noname");

		final JTextField status = new JTextField();
		status.setBounds(50, 50, 150, 20);

		final JTextField clientid = new JTextField();
		clientid.setBounds(50, 100, 150, 20);
		clientid.setText("1010");

		final JTextField saveTo = new JTextField();
		saveTo.setBounds(50, 70, 150, 20);
		saveTo.setText("C:/received/");
		
		MessageLogger mes = new MessageLogger();
		mes.setParent(this);

		JButton b = new JButton("Start receiving");
		b.setBounds(50, 150, 195, 30);

		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sDir = saveTo.getText();
				if(!sDir.endsWith("/")){
					sDir= sDir + "/";
				}
				File f = new File(sDir);
				if (f.exists() && f.isDirectory()) {
					showMessage("Directory exists. Continue. " + f.getAbsolutePath());
				}else{
					showMessage("Directory missing " + f.getAbsolutePath());
					return;
				}
				status.setText("Running.");
				rec = new Receiver(clientid.getText(), mes, sDir);
				runner = new RunnableThread("getter", rec);
			}
		});
		
		
		f.add(b);
		JButton s = new JButton("Stop receiving");
		s.setBounds(50, 200, 195, 30);
		s.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				status.setText("Stopped.");
				runner.stop();
				runner = null;
			}
		});

		messagefield = new JTextArea(5, 20);
		messagefield.setBounds(50, 250, 250, 200);
		
		final JFileChooser fc = new JFileChooser();
		JButton selectFile = new JButton("Select File to Send");
		selectFile.setBounds(50, 500, 195, 30);
		selectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 int returnVal = fc.showOpenDialog(ReceiverFrontend.this);

			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
			            Sender client = new Sender();
			            List<String> fList = new ArrayList();
			    		fList.add(file.getAbsolutePath());
			    		client.processFileList(fList, "1010", mes);
			            //This is where a real application would open the file.
			            //log.append("Opening: " + file.getName() + "." + newline);
			        } else {
			            //log.append("Open command cancelled by user." + newline);
			        }		 

			}
		});
		f.add(selectFile);
		

		f.add(s);
		f.add(label1);
		f.add(label2);
		f.add(label3);
		f.add(label4);
		f.add(label5);
		f.add(saveTo);
		f.add(clientid);
		f.add(status);
		f.add(username);
		f.add(messagefield);
		f.setSize(400, 600);
		f.setLayout(null);
		f.setVisible(true);
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	@Override
	public void showMessage(String message) {
		String sText = messagefield.getText();
		sText = sText + "\n" + message ;
		if (sText.length() > 300) {
			sText = sText.substring(sText.length()-200);
		}
		messagefield.setText(sText);

	}

}
