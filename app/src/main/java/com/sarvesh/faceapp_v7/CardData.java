package com.sarvesh.faceapp_v7;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.media.Image;
import android.widget.ImageView;
import android.widget.Switch;

public class CardData {
    ImageView PersonPhoto;
    String PersonName;
    Switch PersonPermissionStatus;

    CardData(Bitmap personPhoto, String personName, boolean personPermissionSwitch)
    {
        this.PersonPhoto.setImageBitmap(personPhoto);
        this.PersonName = personName;
        this.PersonPermissionStatus.setChecked(personPermissionSwitch);
    }
}