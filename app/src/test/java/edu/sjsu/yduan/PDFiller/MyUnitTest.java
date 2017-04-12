package edu.sjsu.yduan.PDFiller;

import android.os.Parcel;
import android.util.Log;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentCatalog;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;

import static android.content.ContentValues.TAG;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
public class MyUnitTest {
    private  Form form;
    private Field f;
    @Before
    public void create() {
        form = new Form();
        f = new Field(R.string.Menu_Review);
    }

    @Test
    public void Field_ParcelableWriteRead(){
        f.Page = 100;
        f.PdfID.add("x");
        f.SelectedValue = "A";
        f.Name = "Test...";
        Parcel parcel = Parcel.obtain();
        f.writeToParcel(parcel, f.describeContents());
        parcel.setDataPosition(0);

        Field createdFromParcel = Field.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel.Page, is(100));
        assertThat(createdFromParcel.PdfID.get(0), is("x"));
        assertThat(createdFromParcel.SelectedValue, is("A"));
        assertThat(createdFromParcel.Name, is("Test..."));
    }
    @Test
    public void Test1() throws Exception {
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
                    JSONObject pdf = jsonObj.getJSONObject("pdf");

                    form.Name = pdf.getString("file");
                    //formURL = pdf.getString("url");
                    JSONArray jary = jsonObj.getJSONArray("form");

                    int i = 0;
                    for (; i < jary.length(); i++) {
                        JSONObject joi = jary.getJSONObject(i);
                        Block b = new Block(joi, i);
                        form.add(b);
                    }
                    form.add(new Block(R.string.Menu_Delivery,i));
                    assertEquals(form.Name, "background check");
                    assertEquals(form.size(), 4);
                    assertEquals(form.get(0).Name, "Applicant");
                    assertEquals(form.get(0).get(0).Name, "Name of Applicant");
                    assertEquals(form.get(1).Name, "Questions");
                    assertEquals(form.get(2).Name, "Signature");
                    assertEquals(form.get(3).Name, "Deliver");
                }catch(Exception ex){}
            }
        });
    }
    @Test
    public void Test2() throws Exception {
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
                File pdf = File.createTempFile("form",".pdf");
                OutputStream out = new FileOutputStream(pdf);
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

                PDDocument document = PDDocument.load(pdf);
                PDDocumentCatalog docCatalog = document.getDocumentCatalog();
                PDAcroForm acroForm = docCatalog.getAcroForm();
                PDFieldTreeNode ftn = acroForm.getField("Position");
                assertEquals(ftn.getClass().getName(),"PDComboBox");
                assertEquals(ftn.getValue(),"Professor");
                ftn.setValue("Lecture");
                assertEquals(ftn.getValue(),"Lecture");

                ftn = acroForm.getField("Sex");
                assertEquals(ftn.getClass().getName(),"PDRadioButton");
                ftn.setValue("Female");
                assertEquals(ftn.getValue(),"Female");

                ftn = acroForm.getField("Name");
                assertEquals(ftn.getClass().getName(),"PDTextField");
                ftn.setValue("xxxx");
                assertEquals(ftn.getValue(),"xxxx");
            }
        });
    }
}
