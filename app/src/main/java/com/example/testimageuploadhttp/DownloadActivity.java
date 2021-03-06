package com.example.testimageuploadhttp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

/*import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;*/

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DownloadActivity extends AppCompatActivity {
        public static final String DATA_URL = "https://swulj.000webhostapp.com/download.php";

        //Tag values to read from json
        public static final String TAG_SUB_JSON_STRING = "images";
        public static final String TAG_ERROR = "error";
        public static final String TAG_IMAGE_URL = "url";
        public static final String TAG_NAME = "name";
        public static final String TAG_ID = "id";
        //GridView Object
        private GridView gridView;
        private Button buttonList;
        Button btnNormalList;

        //ArrayList for Storing image urls and titles
        private ArrayList<String> subJson;
        private ArrayList<String> error;
        private ArrayList<String> images;
        private ArrayList<String> names;
        private ArrayList<String> id;


    EditText txtJson;
    ProgressDialog pd;

    JSONParser jsonparser = new JSONParser();
    TextView tv;
    String ab;
    JSONObject jobj = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_download);    //Web api url*/

            gridView = (GridView) findViewById(R.id.gridView);
            buttonList = (Button) findViewById(R.id.buttonList);
            btnNormalList = (Button) findViewById(R.id.buttonList2);

            images = new ArrayList<>();
            names = new ArrayList<>();
            subJson = new ArrayList<>();
            id = new ArrayList<>();
            txtJson = (EditText) findViewById(R.id.tvJsonItem);
            //Calling the getData method
            //getData();
            new JsonTask().execute(DATA_URL);

            buttonList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent inte = new Intent(DownloadActivity.this,
                            ShowListActivity.class);
                    startActivity(inte);
                    //invoke the SecondActivity.
                    finish();*/
                    //ArrayList<Object> object = new ArrayList<Object>();
                    Intent intent = new Intent(DownloadActivity.this,
                            ShowListActivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable("IMAGELIST",(Serializable)images);
                    intent.putExtra("BUNDLE",args);
                    Bundle args2 = new Bundle();
                    args.putSerializable("NAMELIST",(Serializable)names);
                    intent.putExtra("BUNDLE2",args2);
                    startActivity(intent);
                    finish();

                }
            });
            btnNormalList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent inte = new Intent(DownloadActivity.this,
                            ShowNormalListActivity.class);
                    startActivity(inte);
                    //invoke the SecondActivity.
                    finish();
                }
            });
        }

        private void showGrid(JSONArray jsonArray) throws JSONException {
            //Looping through all the elements of json array
            for(int i = 0; i<jsonArray.length(); i++){
                //Creating a json object of the current index
                JSONObject obj = null;
                try {
                    //getting json object from current index
                    obj = jsonArray.getJSONObject(i);

                    //getting image url and title from json object
                    subJson.add(obj.getString(TAG_SUB_JSON_STRING));
                    //error.add(obj.getString(TAG_ERROR));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            String temp = "";
            for(int i = 0; i < subJson.size(); i++)
            {
                temp += subJson.get(i);
                temp += ";";
            }
            txtJson.setText(temp);
            JSONArray jsonArr = new JSONArray(subJson.get(0));
            for(int i = 0; i<jsonArr.length(); i++){
                //Creating a json object of the current index
                JSONObject obj = null;
                try {
                    //getting json object from current index
                    obj = jsonArr.getJSONObject(i);

                    //getting image url and title from json object
                    images.add(obj.getString(TAG_IMAGE_URL));
                    names.add(obj.getString(TAG_NAME));
                    //error.add(obj.getString(TAG_ERROR));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Creating GridViewAdapter Object
            GridViewAdapter gridViewAdapter = new GridViewAdapter(this,images,names);

            //Adding adapter to gridview
            gridView.setAdapter(gridViewAdapter);
        }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(DownloadActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    //Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            try {
                String res = "[" + result + "]";
                txtJson.setText(res);
                JSONArray jsonArr = new JSONArray(res);
                showGrid(jsonArr);
                /*JSONObject jsonObj = new JSONObject(result);
                showGrid(jsonObj.optJsonArray("images"));*/
            } catch (Exception e) {
                e.printStackTrace();
                txtJson.setText("Exception in jsonArray");
            }
        }
    }
}

