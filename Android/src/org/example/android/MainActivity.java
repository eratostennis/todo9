package org.example.android;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
    String url = "http://10.0.2.2:5000/api/todos";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Activity CurrentActivity = this;
        
        Uri.Builder builder = new Uri.Builder();
        AsyncHttpRequest task = new AsyncHttpRequest(this);
        task.execute(builder);
        
        Button button_register = (Button) findViewById(R.id.register_btn);
        button_register.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		EditText editText = (EditText) findViewById(R.id.newbody);
        		editText.selectAll();
        		String text = editText.getText().toString();
                Uri.Builder builder = new Uri.Builder();
				AsyncHttpPost task = new AsyncHttpPost( CurrentActivity, this, text);
                task.execute(builder);
        	}
        });

	}
	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
