package com.example.jota.asynctask;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    String codigos[] = {"USD","GBP","CHF"};
    Double tasas[] = {0.8,1.13,1.26};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        descargarTasas();
    }
    //------ DESCARGAR TASAS DE INTERNET-------------
    private void descargarTasas() {
        class getTasas extends AsyncTask<String, Integer, String> {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(String... urls) {
                String resultado = "";
                HttpURLConnection canal = null;
                try {
                    URL u = new URL(urls[0]);
                    canal = (HttpURLConnection) u.openConnection();
                    canal.setRequestMethod("GET");
                    canal.setDoOutput(true);
                    canal.connect();
                    InputStreamReader entrada = new InputStreamReader(canal.getInputStream());
                    BufferedReader lectura = new BufferedReader(entrada);
                    StringBuilder stringBuilder = new StringBuilder();
                    String linia;
                    while ((linia = lectura.readLine()) != null) {
                        stringBuilder.append(linia).append("\n");
                    }
                    lectura.close();
                    resultado = stringBuilder.toString();
                    Log.e("****","Tasas de cambio descargadas");
                } catch (Exception e) {
                    resultado = "ERROR";
                    Log.e("!!!!",e.getMessage());
                } finally {
                    canal.disconnect();
                    return resultado;
                }
            }

            @Override
            public void onPostExecute(String result) {
                if (result.startsWith("ERROR")) {
                    Toast.makeText(getApplicationContext(), "Error conexión ECB. Reiniciar.", Toast.LENGTH_LONG).show();
                    return;
                }
                XmlPullParser parser = Xml.newPullParser();
                try {
                    parser.setInput(new StringReader(result));
                    int evento;
                    while ((evento = parser.next()) != XmlPullParser.END_DOCUMENT) {
                        if (evento == XmlPullParser.START_TAG) {
                            if (parser.getName().equals("Cube") && (parser.getAttributeCount() == 2)) {
                                String codigo = parser.getAttributeValue(0).toString();
                                for (int i = 0; i < codigos.length; i++) {
                                    if (codigo.equalsIgnoreCase(codigos[i])) {
                                        String s = parser.getAttributeValue(1).toString();
                                        Float f= Float.parseFloat(s);
                                        tasas[i] = Double.parseDouble(parser.getAttributeValue(1).toString());
                                    }
                                }
                                continue;
                            }
                        }
                    }
                    // muestra datos descargados por pantalla
                    String datos="";
                    for (int i=0;i<codigos.length;i++) {
                        datos += codigos[i] + " " + tasas[i].toString() + "\n";
                    }
                    textView.setText(datos);
                } catch (Exception e) {
                    Log.e("**** ERROR *", e.getMessage());
                }
            }
        }
        getTasas tarea;
        tarea = new getTasas();
        tarea.execute("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
    }
}
