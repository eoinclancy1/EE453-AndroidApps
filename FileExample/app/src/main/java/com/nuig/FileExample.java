package com.nuig;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class FileExample extends Activity {

	MyFile myFile = new MyFile();
	private TextView myTextView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myTextView = new TextView(this);
		setContentView(myTextView);

		myFile.createFile("Android_Lab_1.txt");						//Creating a txt file at 'root//Android_Lab_1.txt'

		myFile.write("This is the first line of text");				//Writing 2 lines individually
		myFile.write("\nThis is the second line of text written to file");

		StringBuilder readIn = myFile.read("Android_Lab_1");		//Read in the text written using a StringBuilder

		setContentView(R.layout.main);								//Set the Content View to the relevant xml layout file
		myTextView = (TextView) findViewById(R.id.txtView);			//Store reference to the textView that is to be written to
		myTextView.setText(readIn);									//Set the text equal to what was read from the file

	}
}