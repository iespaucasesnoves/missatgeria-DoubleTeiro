package com.example.alumne.missatgeria;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class ListaMensajes extends AppCompatActivity {
    private ListView lv;
    private ReceptorXarxa receptor;
    private SharedPreferences prefs;

    //{"dades":{"codiusuari":"15","nom":"LACUADRA","token":"684985881ecbf61ccb3eec0a6df73cb7"},"correcta":true,"missatge":"","rowcount":0}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Comprobación de conexión
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receptor = new ReceptorXarxa();
        this.registerReceiver(receptor, filter);

        String url = "https://iesmantpc.000webhostapp.com/public/provamissatge/";

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String user = preferences.getString("user", "");
        String passw = preferences.getString("password", "");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("nom", user);
        map.put("password", passw);

        //new DescargarMensaje().execute(url);
        CridadaGet(url, map);

    }

    class DescargarMensaje extends AsyncTask<String, Integer, String>{

        private ListView lst;
        private Context context;
        Bitmap bitmap;
        InputStream in = null;
        int responseCode = -1;

        @Override
        protected String doInBackground(String... params) {
            StringBuilder text = new StringBuilder();
            String resultat = "";
            try {
                URL url = new URL(params[0]);
                Log.i("ResConnectUtils", "Connectant" + params[0]);

                HttpsURLConnection httpConn = (HttpsURLConnection) url.openConnection();
                httpConn.setReadTimeout(15000);
                httpConn.setConnectTimeout(25000);
                httpConn.setRequestMethod("GET");
                //httpConn.setRequestProperty("Authorization", token);
                httpConn.setDoInput(true);
                httpConn.setDoOutput(true);

                OutputStream os = httpConn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                writer.flush();
                writer.close();
                os.close();

                int resposta = httpConn.getResponseCode();
                if (resposta == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(httpConn.getInputStream()));

                    while ((line=br.readLine()) != null) {
                        resultat+=line;
                    }
                    Log.i("ResConnectUtils", resultat);
                } else {
                    resultat="";
                }
                Log.i("ResConnectUtils","Errors:"+resposta);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultat;
        }

        @Override
        protected void onPostExecute(String resultat) {
            try{
                JSONObject jsonObject = new JSONObject(resultat);
                JSONArray jsonArray = jsonObject.getJSONArray("dades");

                boolean bo = jsonObject.getBoolean("correcta");

                if (!bo) {
                    Toast.makeText(context,"Dades incorrectes",Toast.LENGTH_LONG).show();
                }

                ArrayList<HashMap<String, Object>> llista = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    HashMap<String, Object> map = new HashMap<>();
                    JSONObject jsonObjectTemp = jsonArray.getJSONObject(i);
                    map.put("MISSATGE",jsonObjectTemp.getString("missatge"));
                    map.put("MISSATGE",jsonObjectTemp.getString("missatge"));
                    map.put("MISSATGE",jsonObjectTemp.getString("missatge"));

                    llista.add(map);
                }

                String[] from={"MIISATGE"};
                int[] to = {R.id.textViewMensaje};

                SimpleAdapter simpleAdapter = new SimpleAdapter(context, llista, R.layout.lista,from, to);
                lst.setAdapter(simpleAdapter);

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (receptor != null) {
            this.unregisterReceiver(receptor);
        }
    }
}
