package edu.sjsu.yduan.PDFiller;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yduan on 4/9/2017.
 */

public class Field implements Parcelable {
    String TAG = this.getClass().getSimpleName();
    String Name;
    boolean Required;
    String Type;
    List<String> PdfID;
    List<String> Values;
    List<Character> CheckedValues;
    String SelectedValue;
    int Page;
    Field(JSONObject jo){
        try {
            Name = jo.getString("name");
            Required = jo.getBoolean("req");
            Type = jo.getString("type");
            PdfID = new ArrayList<>();
            Values = new ArrayList<>();
            CheckedValues = new ArrayList<>();
            SelectedValue = "";
            Page=0;
            if(Type.equals("combo") || Type.equals("radio")){
                PdfID.add(jo.getString("id"));
                JSONArray jary = jo.getJSONArray("value");
                for(int i=0; i < jary.length(); i++) {
                    String v = jary.getString(i);
                    Values.add(v);
                }
                SelectedValue = Values.get(0);
            }else if(Type.equals("check")){
                JSONArray jary = jo.getJSONArray("id");
                for(int i=0; i < jary.length(); i++) {
                    String v = jary.getString(i);
                    PdfID.add(v);
                    CheckedValues.add('N');
                }
            }
            else{
                PdfID.add(jo.getString("id"));//text date signature
            }

            if(Type.equals("signature")){
                Page = jo.getInt("page");
            }
            MainActivity.NumOfFields++;
        }catch (JSONException e) {
            Log.d(TAG, "on JSON Failure:"+e.getMessage());
        }
    }
    Field(int type){
        Page = 0;
        SelectedValue = "";
        PdfID = new ArrayList<>();
        Values = new ArrayList<>();
        CheckedValues = new ArrayList<>();
        if(type==R.string.Menu_Delivery){
            Name = "Your Email";
            Required = true;
            Type = "text";
        }
        else if(type==R.string.Menu_Review){
            Name = "PDF Review";
            Required = false;
            Type = "review";
        }
    }
    Field(Parcel in){
        this.Page = in.readInt();
        this.Name = in.readString();
        this.Required = (in.readInt() == 0) ? false : true;
        this.Type = in.readString();
        this.SelectedValue = in.readString();
        this.PdfID = new ArrayList<String>();
        in.readList(PdfID,String.class.getClassLoader());
        this.Values = new ArrayList<String>();
        in.readList(Values,String.class.getClassLoader());
        this.CheckedValues = new ArrayList<>();
        in.readList(CheckedValues,Character.class.getClassLoader());
    }
    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Page);
        dest.writeString(Name);
        dest.writeInt(Required ? 1 : 0);
        dest.writeString(Type);
        dest.writeString(SelectedValue);
        dest.writeList(PdfID);
        dest.writeList(Values);
        dest.writeList(CheckedValues);
    }
    public static final Parcelable.Creator<Field> CREATOR = new Parcelable.Creator<Field>() {

        @Override
        public Field createFromParcel(Parcel source) {
            return new Field(source);
        }

        @Override
        public Field[] newArray(int size) {
            return new Field[size];
        }
    };
}
