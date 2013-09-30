package org.example.android;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.*;
import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AsyncHttpEdit extends AsyncTask<Uri.Builder, Void, String> {
	String edit_id;
	String edit_body;
	String edit_created_at;
    private Activity mainActivity;
	String[] global_id;
	String[] global_body;
	String[] global_nickname;
	String[] global_created_at; 

    public AsyncHttpEdit(Activity activity,OnClickListener onClickListener, String text_id, String text_body, String text_created_at) {
        // 呼び出し元のアクティビティ
        this.mainActivity = activity;
        edit_id = text_id;
        edit_body = text_body;
        edit_created_at = text_created_at;
        
    }


	// 非同期で処理
    @Override
    protected String doInBackground(Uri.Builder... builder) {
        // httpリクエスト投げる処理
    	String url = "http://10.0.2.2:5000/api/todos";
        try
        {
            HttpGet method = new HttpGet( url );

            DefaultHttpClient client = new DefaultHttpClient();

            // ヘッダを設定する
            method.setHeader( "Connection", "Keep-Alive" );
            
            HttpResponse response = client.execute( method );
            int status = response.getStatusLine().getStatusCode();
            if ( status != HttpStatus.SC_OK )
                throw new Exception( "" );
            
            return EntityUtils.toString( response.getEntity(), "UTF-8" );
        }
        catch ( Exception e )
        {
        	e.printStackTrace();
            return null;
        }
    }


    // 非同期処理後
    @Override
    protected void onPostExecute(String result) {
        try{
	        // オブジェクトを作る
        	JSONObject json = new JSONObject(result);
        	JSONArray entries = json.getJSONArray("entries");
        	String[] id = new String[entries.length()];
        	String[] body = new String[entries.length()];
        	String[] nickname = new String[entries.length()];
        	String[] created_at = new String[entries.length()];
        	global_id = new String[entries.length()];
        	global_body = new String[entries.length()];
        	global_created_at = new String[entries.length()];
        	global_nickname = new String[entries.length()];
        	for(int i = 0; i < entries.length(); i++){
        		JSONObject entry = entries.getJSONObject(i);
        		id[i] = entry.getString("id");
        		body[i] = entry.getString("body");
        		nickname[i] = entry.getString("nickname");
        		created_at[i] = entry.getString("created_at");
        		global_id[i] = id[i];
        		global_body[i] = body[i];
        		global_nickname[i] = nickname[i];
        		global_created_at[i] = created_at[i];
        	}
            // 取得した結果をテキストビューに入れる
        	initTableLayout(body, created_at, entries.length());

        }catch( Exception e ){       	
        	e.printStackTrace();
        }
    }
    private void initTableLayout(String[] body, String[] created_at,int entries_size) {
        TableLayout tableLayout = (TableLayout) mainActivity.findViewById(R.id.tableLayout);
        tableLayout.removeAllViews();
        
        TableRow tablefirstRow = new TableRow(mainActivity);
        tablefirstRow.addView(createTextview("|Content"));
        tablefirstRow.addView(createTextview("|Update Time|"));

        TextView textUnvisi = (TextView)mainActivity.findViewById(R.id.new_title);
        textUnvisi.setVisibility(View.INVISIBLE);
        EditText editUnvisi = (EditText)mainActivity.findViewById(R.id.newbody);
        editUnvisi.setVisibility(View.INVISIBLE);
        Button buttonUnvisi = (Button)mainActivity.findViewById(R.id.register_btn);
        buttonUnvisi.setVisibility(View.INVISIBLE);
        
        for(int i=0;i<entries_size;i++){
	        TableRow tableRow = new TableRow(mainActivity);
	        tableLayout.addView(tableRow);
	        if(global_id[i].equals(edit_id)){
		        tableRow.addView(createEditview(body[i], 3*i+1));
		        tableRow.addView(createButton("Update", 3*i+2));
		        tableRow.addView(createButton("Cancel", 3*i+3));
	        }else{
		        tableRow.addView(createTextview("|"+body[i]));
		        tableRow.addView(createTextview("|"+created_at[i]+"|"));
        	}
        }

    }

    private TextView createTextview(String text) {
        //Button button = new Button(this);
    	TextView tv = new TextView(mainActivity);
        tv.setText(text);
        //tv.setBackgroundColor(Color.rgb(255,255,204));
        return tv;
    }
    private EditText createEditview(String text, int edit_id) {
        //Button button = new Button(this);
    	EditText tv = new EditText(mainActivity);
    	tv.setId(edit_id);
        tv.setText(text);
        return tv;
    }
    private Button createButton(String text, int button_id) {
        Button button = new Button(mainActivity);
        button.setText(text);
        button.setId(button_id);
        button.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		int btn_id = v.getId();
        		switch (btn_id % 3) {
	    			case 0:
	    				//Cancelリスナー
	                    Uri.Builder builder = new Uri.Builder();
	    				AsyncHttpRequestBtn task = new AsyncHttpRequestBtn( mainActivity, this);
	                    task.execute(builder);
	    				break;
        			case 2:
        				//Editリスナー
                		EditText editText = (EditText) mainActivity.findViewById(btn_id-1);
                		editText.selectAll();
                		String text = editText.getText().toString();
	                    Uri.Builder builder_edit = new Uri.Builder();
	    				AsyncHttpPut task_edit = new AsyncHttpPut( mainActivity, this, edit_id, text);
	                    task_edit.execute(builder_edit);
        				break;
        		}
        	}
        });
        return button;
    }
}

