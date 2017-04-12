package edu.sjsu.yduan.PDFiller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.squareup.picasso.Picasso;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentCatalog;
import com.tom_roush.pdfbox.pdmodel.common.COSObjectable;
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDCheckbox;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDComboBox;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDListBox;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDRadioButton;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDSignatureField;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static edu.sjsu.yduan.PDFiller.R.id.datePicker;

public class FillActivity extends AppCompatActivity {
    private int BlockID;
    private int FieldID;
    private Form form;
    private Block block;
    private Field field;
    private PDF pdf;
    //private String City="";
    //private final int MY_PERMISSION_LOCATION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill);

        String flkey= getString(R.string.Form_Index);
        String bkey= getString(R.string.Block_Index);
        String fkey= getString(R.string.Field_Index);
        Intent intent = getIntent();
        form = (Form)intent.getParcelableExtra(flkey);
        pdf = new PDF(form);
        BlockID = (int)intent.getIntExtra(bkey,-1);
        FieldID = (int)intent.getIntExtra(fkey,-1);
        block = form.get(BlockID);
        field = block.get(FieldID);

        ActionBar ab = getSupportActionBar();
        ab.setTitle(block.Name+"#"+FieldID);

        TextView QuestionTitle = (TextView)findViewById(R.id.mQuestionTitle);
        QuestionTitle.setText(field.Name);

        TextView mReq = (TextView)findViewById(R.id.mRequire);
        if(field.Required) {
            mReq.setText("*");
            mReq.setTextColor(getResources().getColor(R.color.red));
            mReq.setVisibility(View.VISIBLE);
        }
        else mReq.setVisibility(View.GONE);

        Button btn = (Button)findViewById(R.id.mNextBtn);
        if(FieldID==block.size()-1 && BlockID==form.size()-1){
            btn.setText("Send");
        }
        ImageView vImg = (ImageView)findViewById(R.id.imageView);
        if(field.Type.equals("signature")){
            vImg.setVisibility(View.GONE);
        }else{
            vImg.setVisibility(View.VISIBLE);
            Random r = new Random();
            String url = "http://52.53.248.35:8123/download/"+(r.nextInt(4)+1)+".png";
            Picasso.with(vImg.getContext()).load(url).into(vImg);
        }
        //LinearLayout layout = (LinearLayout) findViewById(R.id.mInputLayout);
        if(field.Type.equals("text")){
            TextView text = (TextView)findViewById(R.id.TextField);
            text.setText(field.SelectedValue);
            text.setVisibility(View.VISIBLE);
        }
        else if(field.Type.equals("combo")){
            Spinner sp = (Spinner)findViewById(R.id.Spinner);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, field.Values);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp.setAdapter(dataAdapter);
            int idx = field.Values.indexOf(field.SelectedValue);
            if(idx>0 && idx<field.Values.size()) sp.setSelection(idx);
            sp.setVisibility(View.VISIBLE);
        }else if(field.Type.equals("check")){
            LinearLayout layout = (LinearLayout) findViewById(R.id.CheckGroup);
            layout.removeAllViewsInLayout();
            Context context= getApplicationContext();
            for(int i=0;i<field.PdfID.size();i++){
                CheckBox cb = new CheckBox(context);
                cb.setText(field.PdfID.get(i));
                cb.measure(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                cb.setId(8000+i);
                if(field.CheckedValues.get(i)=='Y') cb.setChecked(true);
                else cb.setChecked(false);
                layout.addView(cb);
            }
            layout.setVisibility(View.VISIBLE);
        }else if(field.Type.equals("radio")){
            RadioGroup rg = (RadioGroup)findViewById(R.id.RadioGroup);
            rg.removeAllViewsInLayout();
            Context context= getApplicationContext();
            for(int i=0;i<field.Values.size();i++){
                RadioButton rb = new RadioButton(context);
                if(i==0) rb.setChecked(true);
                rb.setText(field.Values.get(i));
                rb.measure(RadioGroup.LayoutParams.WRAP_CONTENT,RadioGroup.LayoutParams.WRAP_CONTENT);
                rb.setId(4000+i);
                if(field.Values.get(i).equals(field.SelectedValue)) rb.setChecked(true);
                rg.addView(rb);
            }
            rg.setVisibility(View.VISIBLE);
        }
        else if(field.Type.equals("date")){
            DatePicker dp = (DatePicker) findViewById(datePicker);
            int year,month,day;
            if(field.SelectedValue.length()>0){
                String[] ss = field.SelectedValue.split("-");
                month = Integer.parseInt(ss[0]);
                day = Integer.parseInt(ss[1]);
                year = Integer.parseInt(ss[2]);
            }
            else{
                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            }
            dp.init(year,month,day,null);
            dp.setVisibility(View.VISIBLE);
        }
        else if(field.Type.equals("signature")){
            final SignaturePad mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
//            final Button mClearButton = (Button) findViewById(R.id.Clear);
//            final Button mSaveButton = (Button) findViewById(R.id.Save);
            mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
                @Override
                public void onStartSigning() {}

                @Override
                public void onSigned() {
//                    mSaveButton.setEnabled(true);
//                    mClearButton.setEnabled(true);
                }

                @Override
                public void onClear(){
//                    mSaveButton.setEnabled(false);
//                    mClearButton.setEnabled(false);
                }
            });
            LinearLayout layout = (LinearLayout) findViewById(R.id.mSigLayout);
            layout.setVisibility(View.VISIBLE);
            btn.setVisibility(View.GONE);
            //requestCurrentCity();
        }
    }
    public void onBtnClick(View view) {
        Context context = view.getContext();
        if(field.Type.equals("text")){
            TextView tv = (TextView)findViewById(R.id.TextField);
            String text = tv.getText().toString();
            field.SelectedValue = text;
            if(text.length()==0 && field.Required){
                Toast.makeText(context, "Required input is empty", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if(field.Type.equals("combo")){
            Spinner sp = (Spinner)findViewById(R.id.Spinner);
            String text = sp.getSelectedItem().toString();
            field.SelectedValue = text;
            if(text.length()==0 && field.Required){
                Toast.makeText(context, "Required input is empty", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if(field.Type.equals("check")){
            LinearLayout layout = (LinearLayout) findViewById(R.id.CheckGroup);
            boolean checked = false;
            for(int i=0;i<field.PdfID.size();i++){
                CheckBox chk=(CheckBox)layout.getChildAt(i);
                if(chk.isChecked()){
                    field.CheckedValues.set(i,'Y');
                    checked = true;
                }
                else{
                    field.CheckedValues.set(i,'N');
                }
            }
            if(!checked&&field.Required){
                Toast.makeText(context, "Required input is empty", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if(field.Type.equals("radio")){
            RadioGroup rg = (RadioGroup)findViewById(R.id.RadioGroup);
            int selectedId = rg.getCheckedRadioButtonId();
            RadioButton rb = (RadioButton) findViewById(selectedId);
            field.SelectedValue = rb.getText().toString();
        }
        else if(field.Type.equals("date")){
            DatePicker dp = (DatePicker) findViewById(datePicker);
            int day = dp.getDayOfMonth();
            int month = dp.getMonth();
            int year = dp.getYear();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            String strDate = dateFormatter.format(calendar.getTime());
            field.SelectedValue = strDate;
        }
        Next(context);
    }
    private void Next(Context context){
        pdf.save();
        if(FieldID<block.size()-1) startFillActivity(context, BlockID, FieldID + 1);
        else if(BlockID<form.size()-1) startFillActivity(context, BlockID+1, 0);
        else{//delivery by email
            TextView text = (TextView)findViewById(R.id.TextField);
            SendEmail(text.getText().toString());
        }
        this.finish();
    }
    private void startFillActivity(Context context,int b, int f){
        String bkey=context.getString(R.string.Block_Index);
        String fkey=context.getString(R.string.Field_Index);
        String flkey=context.getString(R.string.Form_Index);
        Intent intent;
        if(form.get(b).get(f).Type.equals("review")) intent= new Intent(context,Review.class);
        else intent= new Intent(context,FillActivity.class);
        intent.putExtra(bkey,b);
        intent.putExtra(fkey,f);
        intent.putExtra(flkey,form);
        context.startActivity(intent);
    }
    public void SendEmail(String eml){
        //pdf.Fill();
        File file = new File(form.PdfPath);
        if (!file.exists() || !file.canRead()) {
            Log.d("Error","File not exist or not readable");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/email");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{eml});
        intent.putExtra(Intent.EXTRA_SUBJECT, form.Name);
        intent.putExtra(Intent.EXTRA_TEXT, "Here is your filled form");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Pick an Email provider"));
        }
    }

    private void print(PDAcroForm acroForm){
        List<PDFieldTreeNode> fields = acroForm.getFields();
        for(PDFieldTreeNode f : fields){
            try{
                printFormFileds(f,1);
            }catch (Exception ex){
                Log.d("Exception",ex.getMessage());
            }
        }
    }
    void printFormFileds(PDFieldTreeNode f,int lvl) throws IOException{
        if(f instanceof PDNonTerminalField){
            Log.d("NonTerminal@Level:"+lvl,f.getPartialName());
            for(COSObjectable child:f.getKids()){
                printFormFileds((PDFieldTreeNode)child,lvl+1);
            }
        }
        else {
            String type = f.getClass().getSimpleName();
            Log.d("Fields@Level:"+lvl,f.getFullyQualifiedName()+",type:"+type);//+",value:"+v
            if(f instanceof PDTextField){
                Log.d("text value",((PDTextField)f).getValue().toString());
            }
            else if(f instanceof PDCheckbox){
                Log.d("check value",((PDCheckbox)f).getValue().toString());
            }
            else if(f instanceof PDComboBox) {
                Log.d("combo value",((PDComboBox)f).getValue().toString());
            }
            else if (f instanceof PDListBox) {
                Log.d("list value",((PDListBox)f).getValue().toString());
            }
            else if (f instanceof PDRadioButton){
                //((PDRadioButton)f).setValue("Female");
                Log.d("radio value",((PDRadioButton)f).getValue().toString());
            }
            else if (f instanceof PDSignatureField){
                Log.d("sig value",((PDSignatureField)f).getSignature().toString());
            }
        }
    }
    private void SignPDF(PDDocument document,Field f) throws IOException,KeyStoreException,CertificateException,NoSuchAlgorithmException {
        PDDocumentCatalog docCatalog = document.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();
        //PDSignatureField sf = (PDSignatureField)acroForm.getField(f.PdfID.get(0));
        String name = "Self";
        String location = "San Jose";
        String reason = "Sign";
        InputStream bmp_is = new FileInputStream(form.SigPath);
        document.getCurrentAccessPermission().setCanModify(true);
        PDSignature signature = new PDSignature();
        signature.setByteRange(new int[]{0, 0, 0, 0});
        signature.setContents(new byte[4 * 1024]);
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName(name);
        signature.setLocation(location);
        signature.setReason(reason);
        signature.setSignDate(Calendar.getInstance());
        //sf.setSignature(signature);
        PDVisibleSignDesigner signatureDesigner = new PDVisibleSignDesigner(
                document, bmp_is, document.getNumberOfPages());
        signatureDesigner.xAxis(250).yAxis(1000).zoom(-95).signatureFieldName(f.PdfID.get(0));
        PDVisibleSigProperties signatureProperties = new PDVisibleSigProperties();
        signatureProperties.signerName(name)
                .signerLocation(location)
                .signatureReason(reason).preferredSize(0).page(f.Page)
                .visualSignEnabled(true).setPdVisibleSignature(signatureDesigner)
                .buildSignature();
        SignatureOptions options = new SignatureOptions();
        options.setVisualSignature(signatureProperties);
        options.setPage(signatureProperties.getPage()-1);
        File ksFile = new File(form.P12);
        InputStream ks_is = new FileInputStream(ksFile);
        char[] pin = "123456".toCharArray();
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(ks_is, pin);
        Signer dataSigner = new Signer(keystore,pin);
        document.addSignature(signature, dataSigner, options);
        bmp_is.close();
        ks_is.close();
    }
    public void onClearBtnClick(View view) {
        final SignaturePad mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        mSignaturePad.clear();
    }
    public void onSaveBtnClick(View view) {
        final SignaturePad mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
        Bitmap resized = Bitmap.createScaledBitmap(signatureBitmap, 90, 36, true);
        //ImageView iv = (ImageView)findViewById(R.id.imageView);
        //iv.setImageBitmap(signatureBitmap);
        form.SigPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/sign.png";
        try {
            OutputStream stream = new FileOutputStream(form.SigPath);
            resized.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            Next(view.getContext());
        }catch(IOException ex){Log.d("Exception",ex.getMessage());}
    }
}
