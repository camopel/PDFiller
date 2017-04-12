package edu.sjsu.yduan.PDFiller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Toolbar toolbar=null;
    ProgressDialog mProgressDialog;
    private static final String TAG =  MainActivity.class.getSimpleName();
    //private String formURL;
    //public Form form;
    public PDF pdf;
    public static int NumOfFields = 0;
    //String dbPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!isNetworkConnected()){
            Toast.makeText(getApplicationContext(), "No Network Connection", Toast.LENGTH_LONG).show();
            return;
        }
        //progress
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("downloading data");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIcon(0);
        mProgressDialog.show();
        downloadJSON();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //java.util.logging.Logger.getLogger("com.tom_roush.pdfbox").setLevel(java.util.logging.Level.OFF);
        //dbPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/db.tmp";
    }

    private void setup() {
        PDFBoxResourceLoader.init(getApplicationContext());
    }
    @Override
    protected void onStart() {
        super.onStart();
        setup();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        int index = item.getItemId()-Menu.FIRST;
        //Block b = form.get(index);
        startFillActivity(index,0);
        return true;
    }
    private void startFillActivity(int b, int f){
        pdf.Restore();
        Context context= getApplicationContext();
        Intent intent;
        if(pdf.form.get(b).get(f).Type.equals("review")) intent= new Intent(context,Review.class);
        else intent= new Intent(context,FillActivity.class);
        String bkey=context.getString(R.string.Block_Index);
        String fkey=context.getString(R.string.Field_Index);
        String flkey=context.getString(R.string.Form_Index);
        intent.putExtra(bkey,b);
        intent.putExtra(fkey,f);
        intent.putExtra(flkey,pdf.form);
        context.startActivity(intent);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
    public void downloadJSON(){
        OkHttpClient client = new OkHttpClient();
        String url = "http://52.53.248.35:8123/?id=form";
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d(TAG, "on okhttp JSON Download Failure:"+e.getMessage());
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                final String result = response.body().string();
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONObject jsonpdf = jsonObj.getJSONObject("pdf");
                    Form form = new Form();
                    form.Directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    form.Name = jsonpdf.getString("file");
                    //formURL = pdf.getString("url");
                    JSONArray jary = jsonObj.getJSONArray("form");

                    int i=0;
                    for(; i < jary.length(); i++) {
                        JSONObject joi = jary.getJSONObject(i);
                        Block b = new Block(joi,i);
                        form.add(b);
                    }
                    form.add(new Block(R.string.Menu_Review,i++));//add review
                    form.add(new Block(R.string.Menu_Delivery,i++));//add email
                    pdf = new PDF(form);
                    downloadPDF();
                } catch (JSONException e) {
                    Log.d(TAG, "on JSON Failure:"+e.getMessage());
                }
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        InitMenu();
                        InitToolBarTitle(pdf.form.Name);
                        //if (mProgressDialog != null) mProgressDialog.hide();
                    }
                });
            }
        });
    }
    private void InitMenu(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        int firstID = Menu.FIRST;
        for(int i=0;i<pdf.form.size();i++){
            Block b = pdf.form.get(i);
            menu.add(R.id.iMenuGroup,firstID+b.MenuID,Menu.NONE,b.Name).setIcon(b.Icon);
        }
    }
    private void InitToolBarTitle(String t){
        toolbar.setTitle(t);
    }
    public void downloadPDF(){
        OkHttpClient client = new OkHttpClient();
        String url = "http://52.53.248.35:8123/download/form.pdf";
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d(TAG, "on okhttp PDF Download Failure:"+e.getMessage());
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                pdf.form.PdfPath = pdf.form.Directory+"/form.pdf";
                OutputStream out = new FileOutputStream(pdf.form.PdfPath);
                int read = 0;
                byte[] bytes = new byte[1024];
                InputStream is  = response.body().byteStream();
                BufferedInputStream in = new BufferedInputStream(is);
                while ((read = in.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.flush();
                out.close();
                in.close();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        if (mProgressDialog != null) mProgressDialog.hide();
                    }
                });
                //downloadP12();
            }
        });
    }
    public void downloadP12(){
        OkHttpClient client = new OkHttpClient();
        String url = "http://52.53.248.35:8123/download/sign.p12";
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d(TAG, "on okhttp PDF Download Failure:"+e.getMessage());
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                //P12Path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/sign.p12";
                pdf.form.P12 = pdf.form.Directory+"/sign.p12";
                OutputStream out = new FileOutputStream(pdf.form.P12);
                int read = 0;
                byte[] bytes = new byte[1024];
                InputStream is  = response.body().byteStream();
                BufferedInputStream in = new BufferedInputStream(is);
                while ((read = in.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.flush();
                out.close();
                in.close();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        if (mProgressDialog != null) mProgressDialog.hide();
                    }
                });
            }
        });
    }
}
