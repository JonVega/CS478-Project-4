package com.jvega30.project4b;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.ContextMenu;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jvega30.project4.MusicCentral;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MusicCentral mcService; // MusicCentral is the interface from the AIDL
    private boolean mcisBound = false;
    private MediaPlayer mediaPlayer;
    private Button allSongsButton;
    private Button oneSongButton;
    private ListView mListView;
    int something = 0;

    ArrayList<Movie> movies = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listView);

        allSongsButton = (Button) findViewById(R.id.listAllSongs);
        oneSongButton = (Button) findViewById(R.id.listOneSong);
        allSongsButton.setVisibility(View.GONE);
        oneSongButton.setVisibility(View.GONE);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getApplicationContext(), String.valueOf(movies.size()), Toast.LENGTH_SHORT).show();

                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                String url = null;
                //Movie listItem = (Movie)mListView.getItemAtPosition(position);
                try {

                    if(movies.size() == 1) {
                        url = mcService.getSongURL()[something-1];
                    }
                    else {
                        url = mcService.getSongURL()[position];
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.setDataSource(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.prepare(); // might take long! (for buffering, etc)
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context, menu);
        menu.setHeaderTitle("Music Controls");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getItemId() == R.id.pauseSong){
            if(mediaPlayer.isPlaying()) {
                Toast.makeText(getApplicationContext(),"pausing...",Toast.LENGTH_LONG).show();
                mediaPlayer.pause();
            }

            else if(!mediaPlayer.isPlaying()) {
                Toast.makeText(getApplicationContext(),"resuming...",Toast.LENGTH_LONG).show();
                mediaPlayer.start();
            }
        }
        else if(item.getItemId() == R.id.stopSong){
            Toast.makeText(getApplicationContext(),"song stopped",Toast.LENGTH_LONG).show();
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        }else{
            return false;
        }
        return true;
    }

    public class Movie {
        public Movie(String title, String artist, Bitmap albumArt) {
            this.songTitle = title;
            this.artistName = artist;
            this.albumArt = albumArt;
        }

        public String getSongTitle() {
            return songTitle;
        }

        public void setSongTitle(String movieTitle) {
            this.songTitle = movieTitle;
        }

        public String getArtistName() {
            return artistName;
        }

        public void setArtistName(String movieTitle) {
            this.artistName = movieTitle;
        }

        public Bitmap getAlbumArt() {
            return albumArt;
        }

        public void setAlbumArt(Bitmap albumArt) {
            this.albumArt = albumArt;
        }

        private String songTitle;
        private String artistName;
        private Bitmap albumArt;
    }

    public class MovieAdapter extends ArrayAdapter<Movie> {

        private Context mContext;
        int mResource;

        public MovieAdapter(Context context, int resource, ArrayList<Movie> objects) {
            super(context, resource, objects);
            mContext = context;
            mResource = resource;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            String title = getItem(position).getSongTitle();
            String artist = getItem(position).getArtistName();
            Bitmap albumart = getItem(position).getAlbumArt();

            Movie m = new Movie(title, artist, albumart);

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            TextView movieName = (TextView) convertView.findViewById(R.id.movieTitle);
            TextView movieYear = (TextView) convertView.findViewById(R.id.movieYearReleased);
            ImageView moviePoster = (ImageView) convertView.findViewById(R.id.moviePoster);

            movieName.setText(title);
            movieYear.setText(artist);
            moviePoster.setImageBitmap(albumart);

            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuass, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.bindmenu:
                Toast.makeText(getApplicationContext(), "Binding Service...", Toast.LENGTH_SHORT).show();

                if (!mcisBound) {

                    boolean b = false;
                    Intent i = new Intent(MusicCentral.class.getName());

                    // UB:  Stoooopid Android API-20 no longer supports implicit intents
                    // to bind to a service #@%^!@..&**!@
                    // Must make intent explicit or lower target API level to 19.
                    ResolveInfo info = getPackageManager().resolveService(i, 0);
                    i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

                    b = bindService(i, this.mConnection, Context.BIND_AUTO_CREATE);
                    if (b) {
                        Log.i("Boop", "Ugo says bindService() succeeded");
                    } else {
                        Log.i("Boop", "Ugo says bindService() failed!");
                    }
                }


                try {
                    if (mcisBound) {

                        //Toast.makeText(getApplicationContext(), String.valueOf(mcService.getSongURL()[1]), Toast.LENGTH_SHORT).show();
                        allSongsButton.setVisibility(View.VISIBLE);
                        oneSongButton.setVisibility(View.VISIBLE);

                        allSongsButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                movies.clear();
                                try {
                                    for(int i = 0; i < mcService.getSongTitle().length; i++) {
                                        movies.add(new Movie(mcService.getSongTitle()[i], mcService.getSongArtist()[i], mcService.getBitmap()[i]));
//                                        movies.add(new Movie(mcService.getSongTitle()[i], mcService.getSongArtist()[i]));
                                    }
                                } catch (RemoteException r) {
                                    Toast.makeText(getApplicationContext(), String.valueOf(r), Toast.LENGTH_SHORT).show();
                                }

                                MovieAdapter mAdapter = new MovieAdapter(getApplicationContext(), R.layout.adapter_view_layout, movies);
                                mListView.setAdapter(mAdapter);
                                registerForContextMenu(mListView);

                            }
                        });

                        oneSongButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                movies.clear();

                                final EditText taskEditText = new EditText(MainActivity.this);
                                AlertDialog dialog = null;
                                try {
                                    dialog = new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Enter Song Track Number To Play")
                                            .setMessage("Pick a number from 1 to " + String.valueOf(mcService.getSongTitle().length))
                                            .setView(taskEditText)
                                            .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        something = Integer.valueOf(String.valueOf(taskEditText.getText()));
                                                    }
                                                    catch (Exception ext) {
                                                        Toast.makeText(getApplicationContext(), String.valueOf(ext), Toast.LENGTH_SHORT).show();
                                                    }
                                                    try {
                                                        movies.add(new Movie(mcService.getSongTitle()[something-1], mcService.getSongArtist()[something-1], mcService.getBitmap()[something-1]));

                                                    } catch (RemoteException r) {
                                                        Toast.makeText(getApplicationContext(), String.valueOf(r), Toast.LENGTH_SHORT).show();
                                                    }
                                                    MovieAdapter mAdapter = new MovieAdapter(getApplicationContext(), R.layout.adapter_view_layout, movies);
                                                    mListView.setAdapter(mAdapter);
                                                    registerForContextMenu(mListView);
                                                }
                                            })
                                            .setNegativeButton("Cancel", null)
                                            .create();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                dialog.show();
                            }
                        });

                    } else {
                        Log.i("Boop", "Unko");
                        Toast.makeText(getApplicationContext(), "FAIL", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("Boop", e.toString());
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }

                this.setTitle("Project 4B - MusicClient (Bounded)");
                return true;
            case R.id.unbindmenu:
                Toast.makeText(getApplicationContext(), "Unbinding Service...", Toast.LENGTH_SHORT).show();
                this.setTitle("Project 4B - MusicClient (Unbounded)");
                movies.clear();
                allSongsButton.setVisibility(View.GONE);
                oneSongButton.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);

                if (mcisBound) {

                    try {
                        unbindService(this.mConnection);
                        Toast.makeText(getApplicationContext(),"onServiceDisconnected",Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e) {
                        Log.e("Boop", String.valueOf(e));
                    }

                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showAddItemDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Add a new task")
                .setMessage("What do you want to do next?")
                .setView(taskEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        something = Integer.valueOf(String.valueOf(taskEditText.getText()));
                        Toast.makeText(getApplicationContext(), String.valueOf(something), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iservice) {
            mcService = MusicCentral.Stub.asInterface(iservice);
            mcisBound = true;
            Toast.makeText(getApplicationContext(), "Bounded is " + String.valueOf(mcisBound), Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            mcService = null;
            mcisBound = false;
            Toast.makeText(getApplicationContext(),"onServiceDisconnected",Toast.LENGTH_SHORT).show();

        }
    };

    // Unbind from KeyGenerator Service
    @Override
    protected void onPause() {

        super.onPause();

        if (mcisBound) {
            unbindService(this.mConnection);
        }
    }

    // Bind to KeyGenerator Service
    @Override
    protected void onResume() {
        super.onResume();

        if (!mcisBound) {

            boolean b = false;
            Intent i = new Intent(MusicCentral.class.getName());

            // UB:  Stoooopid Android API-20 no longer supports implicit intents
            // to bind to a service #@%^!@..&**!@
            // Must make intent explicit or lower target API level to 19.
            ResolveInfo info = getPackageManager().resolveService(i, 0);
            i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            b = bindService(i, this.mConnection, Context.BIND_AUTO_CREATE);
            if (b) {
                Log.i("Boop", "Ugo says bindService() succeeded");
            } else {
                Log.i("Boop", "Ugo says bindService() failed!");
            }
        }
    }
}