package com.jvega30.project4;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MusicCentralService extends Service {

    String[] songTitle = {"Dire, Dire Docks", "Operation", "The Moon and The Prince (and LEOPALDON MIX)", "Always Been But Never Dreamed", "Floral Fury", "Butterfly Effect area X", "Apex Legends Main Theme"};
    String[] songArtist = {"Koji Kondo", "Keiki Kobayashi", "Akitaka Tohyama, Remixed by LEOPALDON", "Metamorphosis", "Kristofer Maddigan", "Hydelic", "Stephen Barton"};
    String[] songURL = {"https://vgmdownloads.com/soundtracks/super-mario-64-soundtrack/guedpzxu/09%20Dire%2C%20Dire%20Docks.mp3",
                        "https://vgmdownloads.com/soundtracks/ace-combat-4-shattered-skies-original-soundtracks/cbulyddf/104%20operation.mp3",
                        "https://vgmdownloads.com/soundtracks/katamari-forever/plargfjg/209%20The%20Moon%20and%20The%20Prince%20%28and%20LEOPALDON%20MIX%29.mp3",
                        "https://vgmdownloads.com/soundtracks/tetris-effect-the-complete-soundtrack/qelsvaqkii/1-29%20-%20Metamorphosis%20-%20Always%20Been%20But%20Never%20Dreamed.mp3",
                        "https://vgmdownloads.com/soundtracks/cuphead/oseoaxpz/13%20Floral%20Fury.mp3",
                        "https://vgmdownloads.com/soundtracks/rez-infinite-original-soundtrack/ozeztauo/15.%20Butterfly%20Effect%20area%20X.mp3",
                        "https://vgmdownloads.com/soundtracks/apex-legends-2019/zqoxecihsr/01%20Apex%20Legends%20Main%20Theme.mp3"};

    private final static Set<UUID> mIDs = new HashSet<UUID>();

    // Implement the Stub for this Object
    private final MusicCentral.Stub mBinder = new MusicCentral.Stub() {

        public String[] getSongTitle() {
            return songTitle;
        }

        public String[] getSongArtist() {
            return songArtist;
        }

        public String[] getSongURL() {
            return songURL;
        }

        public Bitmap[] getBitmap()
        {
            Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.supermario64);
            Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.acecombat4);
            Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.katamariforever);
            Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.tetriseffect);
            Bitmap bitmap5 = BitmapFactory.decodeResource(getResources(), R.drawable.cuphead);
            Bitmap bitmap6 = BitmapFactory.decodeResource(getResources(), R.drawable.rezinfinite);
            Bitmap bitmap7 = BitmapFactory.decodeResource(getResources(), R.drawable.apexlegends);

            Bitmap bitmaps[] = {bitmap1, bitmap2, bitmap3, bitmap4, bitmap5, bitmap6, bitmap7};
            return bitmaps;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
