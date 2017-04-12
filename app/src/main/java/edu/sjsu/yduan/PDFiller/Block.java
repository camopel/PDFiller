package edu.sjsu.yduan.PDFiller;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Block implements Parcelable {
    String Name;
    int Icon;
    int MenuID;
    List<Field> Fields;
    String TAG = this.getClass().getSimpleName();
    Block(JSONObject jo,int idx){
        try{
            MenuID = idx;
            Name = jo.getString("name");
            String ic = jo.getString("icon");
            if(ic.equals("p")){Icon = R.drawable.ic_pi;}
            else if(ic.equals("i")){Icon = R.drawable.ic_info;}
            else if(ic.equals("s")){Icon = R.drawable.ic_sign;}
            JSONArray jary = jo.getJSONArray("fileds");
            Fields = new ArrayList<>();
            for (int i = 0; i < jary.length(); i++) {
                JSONObject joi = jary.getJSONObject(i);
                Field f = new Field(joi);
                Fields.add(f);
            }
        }catch (JSONException e) {
            Log.d(TAG, "on JSON Failure:"+e.getMessage());
        }
    }
    Block(int type, int i){
        MenuID = i;
        Fields = new ArrayList<>();
        Fields.add(new Field(type));
        if(type==R.string.Menu_Delivery){
            Icon = R.drawable.ic_eml;
            Name = "Delivery";
        }
        else if(type==R.string.Menu_Review){
            Icon = R.drawable.ic_rv;
            Name = "Review";
        }
    }

    Block(Parcel in){
        this.Name = in.readString();
        this.Icon = in.readInt();
        this.MenuID = in.readInt();
        this.Fields = new ArrayList<Field>();
        in.readList(Fields,Field.class.getClassLoader());
    }
    public int size(){
        return Fields.size();
    }
    public Field get(int i){
        return Fields.get(i);
    }
    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Name);
        dest.writeInt(Icon);
        dest.writeInt(MenuID);
        dest.writeList(Fields);
    }
    public static final Parcelable.Creator<Block> CREATOR = new Parcelable.Creator<Block>() {

        @Override
        public Block createFromParcel(Parcel source) {
            return new Block(source);
        }

        @Override
        public Block[] newArray(int size) {
            return new Block[size];
        }
    };
}
