package com.example.simplemessenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

public class SimpleMessenger extends ListActivity 
{
	private String send_ClientMessage;
	private int emulator_instance;
	final String localHost="10.0.2.2";
	private SimpleDateFormat df;
	ArrayList<String> as;
	ArrayAdapter<String> a;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	 	TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    	String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
    	emulator_instance=Integer.parseInt(portStr);
		as=new ArrayList<String>();
		a=new ArrayAdapter<String>(this, R.layout.text_listview, as);
		setListAdapter(a);
		createServer();
		df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
	}

	private void createServer() 
	{
		new Thread(new Runnable()
		{
			@SuppressWarnings("resource")
			public void run()
			{
				try 
				{
					ServerSocket s = new ServerSocket(10000);
					while(true)
					{
						Socket ss=s.accept();
						String msg_ServerSide=null;
						BufferedReader br=new BufferedReader(new InputStreamReader(ss.getInputStream()));
						while((msg_ServerSide=br.readLine())!=null)
						{
							System.out.println();
							if(!msg_ServerSide.isEmpty())
						{
								new displayMessage().execute(msg_ServerSide);
						}
						}	
						br.close();
						ss.close();
					}
				} catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	class displayMessage extends AsyncTask<String, Integer, String>
	{

		@Override
		protected String doInBackground(String... params) 
		{
			return params[0];
		}
		@Override
		protected void onPostExecute(String result) 
		{
			super.onPostExecute(result);
			String time=df.format(Calendar.getInstance().getTime());
			String time_now=time.substring(11, 16);
			a.add(result+"\t"+"\t"+time_now);
			Toast.makeText(getApplicationContext(), result,Toast.LENGTH_SHORT).show();
		}		
	}
	public void sendMessage(View v)
	{
		EditText ed=(EditText)findViewById(R.id.editText);
		send_ClientMessage=ed.getText().toString();
		sendToServer();
		ed.setText(null);
	}

	private void sendToServer() 
	{
	new Thread(new Runnable()
	{
		public void run()
		{
			Socket s=null;
			try {
				if(emulator_instance == 5554)
				{
				s=new Socket(localHost,11112);
				}
				else if(emulator_instance == 5556)
				{
				s=new Socket(localHost,11108);	
				}
				sendandUpdate(s);
				s.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}).start();	
				
	}
	private void sendandUpdate(Socket s) throws IOException 
	{
		PrintWriter pw = new PrintWriter(s.getOutputStream());
		pw.println(send_ClientMessage);
		pw.close();
		s.close();
	}
}
