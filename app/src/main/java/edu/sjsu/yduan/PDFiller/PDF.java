package edu.sjsu.yduan.PDFiller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentCatalog;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDCheckbox;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by yduan on 4/11/2017.
 */

public class PDF {
    private ParcelFileDescriptor mFileDescriptor;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;
    public Form form;
    private int CurrentPage;
    PDF(Form f){
        form = f;
        CurrentPage = 0;
    }
    public void CreateRender(){
        File pdf = new File(form.PdfPath);
        if (pdf.exists()){
            try{
                mFileDescriptor = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY);
                mPdfRenderer = new PdfRenderer(mFileDescriptor);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void CloseRenderer(){
        try{
            if (null != mCurrentPage) mCurrentPage.close();
            mPdfRenderer.close();
            mFileDescriptor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//    public Boolean RenderPage(ImageView mImageView) {
//        if (mPdfRenderer.getPageCount() <= CurrentPage || CurrentPage<0) return false;
//        else{
//            if (null != mCurrentPage) mCurrentPage.close();
//            mCurrentPage = mPdfRenderer.openPage(CurrentPage);
//            Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(), Bitmap.Config.ARGB_8888);
//            Matrix m = mImageView.getImageMatrix();
//            Rect rect = new Rect(0, 0, mImageView.getWidth(), mImageView.getHeight());
//            mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
//            mImageView.setImageBitmap(bitmap);
//            return true;
//        }
//    }
    public boolean RenderPage(ImageView mImageView){
        try{
            File pdf = new File(form.PdfPath);
            PDDocument document = PDDocument.load(pdf);
            int max=document.getNumberOfPages();
            if (max <= CurrentPage || CurrentPage<0) return false;
            else{
                PDFRenderer renderer = new PDFRenderer(document);
                Bitmap bitmap = renderer.renderImage(CurrentPage, 1, Bitmap.Config.RGB_565);
                mImageView.setImageBitmap(bitmap);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public Boolean RenderNextPage(ImageView mImageView){
        CurrentPage++;
        return RenderPage(mImageView);
    }

    public Boolean RenderPrevPage(ImageView mImageView){
        CurrentPage--;
        return RenderPage(mImageView);
    }

    public void SetPage(int page){
        this.CurrentPage = page;
        if (null != mCurrentPage) mCurrentPage.close();
    }

    public void Fill(){
        try{
            File pdf = new File(form.PdfPath);
            PDDocument document = PDDocument.load(pdf);
            PDDocumentCatalog docCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();
            //print(acroForm);
            for(int i=0;i<form.size();i++){
                Block b = form.get(i);
                for(int j=0;j<b.size();j++){
                    Field f = b.get(j);
                    PDFieldTreeNode ftn;
                    if(f.Type.equals("text")||f.Type.equals("date")||f.Type.equals("radio")||f.Type.equals("combo")){
                        if(f.PdfID.size()==0) continue;
                        ftn = acroForm.getField(f.PdfID.get(0));
                        ftn.setValue(f.SelectedValue);
                        ftn.setReadonly(true);
                    }
                    else if(f.Type.equals("check")){
                        for(int c=0;c<f.PdfID.size();c++){
                            ftn = acroForm.getField(f.PdfID.get(c));
                            PDCheckbox cb = (PDCheckbox)ftn;
                            if(f.CheckedValues.get(c)=='Y') cb.check();
                            else cb.unCheck();
                            ftn.setReadonly(true);
                        }
                    }
                    else if(f.Type.equals("signature")){
                        ftn = acroForm.getField(f.PdfID.get(0));
                        ftn.setReadonly(true);
                        if(form.SigPath==null||form.SigPath.length()==0) continue;
                        File sig = new File(form.SigPath);
                        if(sig.exists()){
                            InputStream in = new FileInputStream(form.SigPath);
                            Bitmap alphaImage = BitmapFactory.decodeStream(in);
                            PDImageXObject image = LosslessFactory.createFromImage(document, alphaImage);
                            //PDImageXObject image = JPEGFactory.createFromStream(document, in);
                            PDPageTree pages = document.getDocumentCatalog().getPages();
                            PDPageContentStream content = new PDPageContentStream(document, pages.get(f.Page-1),true,true);
                            content.drawImage(image, 320, 435);
                            content.close();
                        }
                    }
                }
            }
            document.save(pdf);
            document.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void save(){
        try {
            String dbPath = form.Directory+"/db.tmp";
            FileOutputStream fos = new FileOutputStream(dbPath);
            Parcel p = Parcel.obtain();
            form.writeToParcel(p, 0);
            fos.write(p.marshall());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void Restore(){
        try {
            File f = new File(form.Directory+"/db.tmp");
            if(!f.exists()) return;
            FileInputStream fis = new FileInputStream(f);
            byte[] array = new byte[(int) fis.getChannel().size()];
            fis.read(array, 0, array.length);
            fis.close();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(array, 0, array.length);
            parcel.setDataPosition(0);
            Form tmp = Form.CREATOR.createFromParcel(parcel);
            if(tmp!=null){
                for(int i=0;i<form.size();i++){
                    Block b = form.get(i);
                    for(int j=0;j<b.size();j++){
                        Field fld = b.get(j);
                        fld.SelectedValue=tmp.get(i).get(j).SelectedValue;
                        for(int k=0;k<fld.CheckedValues.size();k++){
                            fld.CheckedValues.set(k,tmp.get(i).get(j).CheckedValues.get(k));
                        }
                    }
                }
            }
            //Bundle out = parcel.readBundle(form.getClass().getClassLoader());
            //out.putAll(out);
            parcel.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
