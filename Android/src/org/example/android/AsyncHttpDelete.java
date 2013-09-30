package org.example.android;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.*;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AsyncHttpDelete extends AsyncTask<Uri.Builder, Void, String> {
    String params;
	String[] global_id;
	String[] global_body;
	String[] global_nickname;
	String[] global_created_at; 
    private Activity mainActivity;

    public AsyncHttpDelete(Activity activity,OnClickListener onClickListener, String text) {
        // 呼び出し元のアクティビティ
        this.mainActivity = activity;
        params = text;
    }

    // 非同期で処理
    @Override
    protected String doInBackground(Uri.Builder... builder) {
        // httpリクエスト投げる処理
    	String url = "http://10.0.2.2:5000/api/todos";
        try
        {

			// DELETE
        	String urlstring = url+"/"+params;
            HttpDelete methoddelete = new HttpDelete( urlstring );
            DefaultHttpClient clientdelete = new DefaultHttpClient();
            
            // ヘッダを設定する
            methoddelete.setHeader( "Content-Type", "application/x-www-form-urlencoded"  );
            
            HttpResponse responsedelete = clientdelete.execute( methoddelete );
            int statusdelete = responsedelete.getStatusLine().getStatusCode();
            if ( statusdelete != HttpStatus.SC_OK )
                throw new Exception( "" );


            
            HttpGet methodget = new HttpGet( url );

            DefaultHttpClient clientget = new DefaultHttpClient();

            // ヘッダを設定する
            methodget.setHeader( "Connection", "Keep-Alive" );
            
            HttpResponse responseget = clientget.execute( methodget );
            int statusget = responseget.getStatusLine().getStatusCode();
            if ( statusget != HttpStatus.SC_OK )
                throw new Exception( "" );
            
            return EntityUtils.toString( responseget.getEntity(), "UTF-8" );           
            //return EntityUtils.toString( response.getEntity(), "UTF-8" );
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
        
        for(int i=0;i<entries_size;i++){
	        TableRow tableRow = new TableRow(mainActivity);
	        tableLayout.addView(tableRow);
	        tableRow.addView(createTextview("|"+body[i]));
	        tableRow.addView(createTextview("|"+created_at[i]+"|"));
	        tableRow.addView(createButton("Edit", 2*i+1, i));
	        tableRow.addView(createButton("Delete", 2*i+2, i));
        }

    }

    private TextView createTextview(String text) {
    	TextView tv = new TextView(mainActivity);
        tv.setText(text);
        return tv;
    }
    private Button createButton(String text, int button_id, final int db_number) {
        Button button = new Button(mainActivity);
        button.setText(text);
        button.setId(button_id);
        button.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		int btn_id = v.getId();
				String text_id = global_id[db_number];
				//String text_nickname = global_nickname[db_number];
				String text_body = global_body[db_number];
				String text_created_at= global_created_at[db_number];
        		switch (btn_id % 2) {
        			case 0:
        				//Deleteリスナー
                        Uri.Builder builder_delete = new Uri.Builder();
        				AsyncHttpDelete task_delete = new AsyncHttpDelete( mainActivity, this, text_id);
                        task_delete.execute(builder_delete);
                        break;
        			case 1:
        				//Editリスナー
                        Uri.Builder builder_edit = new Uri.Builder();
        				AsyncHttpEdit task_edit = new AsyncHttpEdit( mainActivity, this, text_id, text_body, text_created_at);
                        task_edit.execute(builder_edit);
        				break;
        		}
        	}
        });
        return button;
    }
}