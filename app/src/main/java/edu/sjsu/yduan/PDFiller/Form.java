package edu.sjsu.yduan.PDFiller;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yduan on 4/9/2017.
 */

public class Form implements Parcelable {
    private List<Block> form;
    public String Directory;
    public String PdfPath;
    public String Name;
    public String P12;
    public String SigPath;//image
    Form(){
        this.form = new ArrayList<Block>();
    }
    Form(Parcel in){
        this.Directory = in.readString();
        this.PdfPath = in.readString();
        this.P12 = in.readString();
        this.Name = in.readString();
        this.SigPath = in.readString();
        this.form = new ArrayList<>();
        in.readList(form,Block.class.getClassLoader());
    }
    public void add(Block b){
        this.form.add(b);
    }
    public int size(){
        return form==null?0:form.size();
    }
    public Block get(int i){
        if(form==null) return null;
        else return form.get(i);
    }
    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Directory);
        dest.writeString(PdfPath);
        dest.writeString(P12);
        dest.writeString(Name);
        dest.writeString(SigPath);
        dest.writeList(form);
    }
    public static final Parcelable.Creator<Form> CREATOR = new Parcelable.Creator<Form>() {

        @Override
        public Form createFromParcel(Parcel source) {
            return new Form(source);
        }

        @Override
        public Form[] newArray(int size) {
            return new Form[size];
        }
    };
}
