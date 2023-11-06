// MusicCentral.aidl
package com.jvega30.project4;

// Declare any non-default types here with import statements

interface MusicCentral {
    String[] getSongTitle();
    String[] getSongArtist();
    String[] getSongURL();
    Bitmap[] getBitmap();
}
