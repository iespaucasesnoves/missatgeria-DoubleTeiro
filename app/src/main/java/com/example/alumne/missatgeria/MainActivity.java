package com.example.alumne.missatgeria;

import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private ReceptorXarxa receptor;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private EditText usuario, contraseña;
    private Button btn_login;
    private static boolean ejecutado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receptor = new ReceptorXarxa();
        this.registerReceiver(receptor, filter);

        usuario = (EditText) findViewById(R.id.editText_usuario);
        contraseña = (EditText) findViewById(R.id.editText_contrasena);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://iesmantpc.000webhostapp.com/public/login/";
                String user = usuario.getText().toString();
                String passw = contraseña.getText().toString();

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("nom", user);
                map.put("password", passw);

                CridadaPost(url, map);
                if (ejecutado == true){
                    editor.putString("user",user);
                    editor.putString("password", passw);
                    editor.apply();
                    Intent intent = new Intent(v.getContext(), ListaMensajes.class);
                    startActivity(intent);
                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        if (receptor != null) {
            this.unregisterReceiver(receptor);
        }
    }

    public static String CridadaPost(String adrecaURL,HashMap<String, String> parametres) {
        String resultat = "";
        try {
            URL url = new URL(adrecaURL);
            Log.i("ResConnectUtils", "Connectant" + adrecaURL);

            HttpsURLConnection httpConn = (HttpsURLConnection) url.openConnection();
            httpConn.setReadTimeout(15000);
            httpConn.setConnectTimeout(25000);
            httpConn.setRequestMethod("POST");
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);

            OutputStream os = httpConn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(montaParametres(parametres));
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
        ejecutado = true;
        return resultat;
    }

    private static String montaParametres(HashMap<String, String> params) throws UnsupportedEncodingException {
        // A partir d'un hashmap clau-valor cream
        // clau1=valor1&clau2=valor2&...
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }
}
