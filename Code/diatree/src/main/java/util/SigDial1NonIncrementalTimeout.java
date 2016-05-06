package util;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import model.SigDialException;


public class SigDial1NonIncrementalTimeout extends Thread {
	
	
	private static SigDial1NonIncrementalTimeout timeoutThread;
	private static int duration;
	
	
	public static void setVariables(int d) {
		duration = d;
	}
	
//	singleton
	public static SigDial1NonIncrementalTimeout getInstance() {
		
		if (timeoutThread == null) {
			timeoutThread = new SigDial1NonIncrementalTimeout();
		}
		
		return timeoutThread;
	}
	
	public void reset() {
		timeoutThread.interrupt();
		timeoutThread = new SigDial1NonIncrementalTimeout();
		timeoutThread.start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(duration);
			display();
		} 
		catch (InterruptedException e) {
		}
	}
	
	private void display() {
		 EventQueue.invokeLater(new Runnable() {
		        
	            @Override
	            public void run() {
	        		JFrame frame = new JFrame();
	        		frame.setSize(1000, 1000);
	        		frame.setLayout( new GridLayout(0,1));
	        		frame.addKeyListener(new KeyListener() {

						@Override
						public void keyTyped(KeyEvent e) {
							if (e.getID() == 400) {
		                    	frame.setVisible(false);
		                    	frame.dispose();
		                    	
							}
						}

						@Override
						public void keyPressed(KeyEvent e) {
						}

						@Override
						public void keyReleased(KeyEvent e) {
						}
	        				
	        				
	        		});
	        		
	        		String utt = "<html>Phase A Fertig!</html>";
	        		
	        		JLabel text = new JLabel(utt);
	        		
	        		JButton done = new JButton("OK");
	        		done.addActionListener(new ActionListener() {
	                    @Override
	                    public void actionPerformed(ActionEvent e) {
	                    	frame.setVisible(false);
	                    	frame.dispose();
	                    	System.exit(0);
	                    }
	                });
	        		
	        		text.setFont(new Font(text.getFont().getName(), Font.PLAIN, 36));
	        		frame.getContentPane().add(text);
	        		frame.getContentPane().add(done);
	        		frame.pack();
//	        		frame.setLocationRelativeTo();
	        		frame.setVisible(true);
	        		frame.requestFocus();
	            }

	        });
		
	}
	
	
	
}
