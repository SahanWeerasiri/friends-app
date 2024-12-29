package com.example.friends;

import android.content.Intent;
import android.net.Uri;

public class NormalCalls {
    String number;
    public NormalCalls(String number){
        this.number=number;
    }
    public Intent call(){
        Intent calling=new Intent(Intent.ACTION_CALL);
        calling.setData(Uri.parse("tel:"+number));
        return calling;
    }
}
