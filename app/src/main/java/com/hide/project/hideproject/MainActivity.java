package com.hide.project.hideproject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.erikagtierrez.multiple_media_picker.Gallery;
import com.bumptech.glide.Glide;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import es.dmoral.toasty.Toasty;

import static android.os.Process.*;


public class MainActivity extends AppCompatActivity {

    private static final int OPEN_MEDIA_PICKER = 1;
    private static boolean doubleBackToExitPressedOnce = false;
    private static boolean detect = false;
    private final static String TAG = MainActivity.class.getSimpleName();
    final Context cont = this;
    private static final String MAIN_DIR = "/.HidenFiles/";
    public static final String PATH_PREF = "MAP";
    public static final String PATH_PREF_FILE = "MAPfile";
    private Set<String> paths;
    private List<String> displayPaths = new ArrayList<String>();



    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2;
    Button settings;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_shield);

        settings = findViewById(R.id.toolbarSettings);
        toolbar.setTitle("    HideProject");

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Floating menu button init
        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);

        paths = getSet(); // SET OF PATHS IN APP FOLDER

        /*
        Initiating gallery view
         */

        GridView gallery = (GridView) findViewById(R.id.galleryGridView);

        gallery.setAdapter(new ImageAdapter(this));

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                if (null != displayPaths && !displayPaths.isEmpty())
                    Toasty.normal(cont,"position " + position + " " + displayPaths.get(position), Toast.LENGTH_SHORT).show();
            }
        });




        /*
        menu item to create a new folder in app's directory with user input dialog
         */
        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                isPermissionGranted();

                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(cont);
                View dial = layoutInflaterAndroid.inflate(R.layout.input_dialog_text, null);

                AlertDialog.Builder userInput = new AlertDialog.Builder(cont);
                userInput.setView(dial);
                Log.d(TAG, "onClick: INPUT DIALOG INFLATE");


                final EditText dialogEdit = (EditText) dial.findViewById(R.id.userInputDialogText);
                final TextView title = dial.findViewById(R.id.dialogTitleText);



                userInput.setCancelable(false).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface box, int id) {

                        String folderName = dialogEdit.getText().toString();
                        int good = folderName.length();


                        if(good != 0 && good <= 10 ) {
                            generateSecretFolder(folderName);
                            Toasty.success(getBaseContext(),"Folder Created with name " + folderName, Toast.LENGTH_SHORT, true).show();
                        }
                        else if(good == 0) {
                            box.cancel();
                            Toasty.warning(getBaseContext(),"Name can't be emtpy ", Toast.LENGTH_SHORT, true).show();
                        }
                        else if(good > 10) {
                            box.cancel();
                            Toasty.warning(getBaseContext(),"Name can't be more than 10 chars ", Toast.LENGTH_SHORT, true).show();
                        }


                    }
                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface box, int id) {
                                        box.cancel();
                                    }
                                });

                AlertDialog dialog = userInput.create();
                dialog.show();
                deleteSet();
            }
        });


        /*
        upload item functionality menu item
         */
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(isPermissionGranted()){

                    openGallery(v);

                }

            }
        });

    }


    // Gallery selection activity start   Mode 1: video and picture data
    public void openGallery(View view) {
        Intent intent= new Intent(this, Gallery.class);
        intent.putExtra("title","Select media");
        intent.putExtra("mode",1);
        intent.putExtra("maxSelection",10);
        startActivityForResult(intent,OPEN_MEDIA_PICKER);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == OPEN_MEDIA_PICKER) {
            // Make sure the request was successful

            if (resultCode == RESULT_OK && data != null) {

                ArrayList<String> selectionResult=data.getStringArrayListExtra("result"); // path of file
                materialDesignFAM.close(true);

                List<String> namesImg = getFileNames(selectionResult);
                generateSecretMain();

                for(int i = 0; i < selectionResult.size(); i++) {
                    moveFile(selectionResult.get(i), namesImg.get(i));
                }

                reloadMain();

                // Logs for testing purposes
                if(!namesImg.isEmpty()) {
                    Log.d(TAG, "onActivityResult MEDIA PICK: ...................." + namesImg.get(0));
                    Log.d(TAG, "onActivityResult MEDIA PICK: ...................." + selectionResult.get(0));
                }
                     Log.d(TAG, "onActivityResult: MEDIA PICKER finish ****************" );

            }

            else {
                Toasty.info(this,"Make a selection", Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    /*
    Method to be called after uplaod actvity finishes it's job
     */
    public void reloadMain() {
        GridView gallery = (GridView) findViewById(R.id.galleryGridView);

        gallery.setAdapter(new ImageAdapter(this));
    }

    // Put data into set
    public void storeSet(Set<String> path) {
        SharedPreferences uri = this.getSharedPreferences(PATH_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = uri.edit();
        editor.putStringSet(PATH_PREF_FILE, path);
        editor.commit();
    }

    // get data from set
    public Set<String> getSet() {
        SharedPreferences uri = this.getSharedPreferences(PATH_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = uri.edit();
        Set<String> myStrings = uri.getStringSet(PATH_PREF_FILE, new HashSet<String>());
        return myStrings;
    }

    // Removes set file from shared prefs
    public void deleteSet() {
        SharedPreferences preferences = getSharedPreferences(PATH_PREF_FILE, 0);
        preferences.edit().remove(PATH_PREF).commit();
    }


    /*
        get the directory for the application files   '/data'/user/0/com.hide.project.hideproject
        */
    public static String getDataDir(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .applicationInfo.dataDir;
        }
        catch (Exception e) {
            Log.d(TAG, "getDataDir: No dir");
        }
        return null;
    }

    /*
     Get just names of a file from path
      */
    public static List<String> getFileNames(ArrayList<String> selectionResult) {
        List<String> namesImg = new ArrayList<>();
        for(int i = 0; i < selectionResult.size(); i++) {
            String name = selectionResult.get(i).trim();
            String newName;
            int a = selectionResult.get(i).lastIndexOf("/");
            int b = selectionResult.get(i).length();
            newName = name.substring(a+1,b);

            namesImg.add(newName);
        }
        return namesImg;
    }


    /*
    generates folder which is hidden
    */
    public void generateSecretFolder(String dirName) {
        try {

            File root = new File(Environment.getExternalStorageDirectory() + getDataDir(this) + "/." + dirName);
            Log.d(TAG, "generateSecretFolder: " + Environment.getExternalStorageDirectory() + getDataDir(this) + "/." + dirName);
            if (!root.exists()) {  // makes a new directory if there is none
                root.mkdirs();
            }
        } catch (Exception e) {
            Log.d(TAG, "generateFolder: Exception thrown");
        }
    }

    /*
    Generate main folder for files of app in data dir
     */
    public void generateSecretMain() {
        try {

            File root = new File(Environment.getExternalStorageDirectory() + getDataDir(this) + MAIN_DIR);

            if (!root.exists()) {  // makes a new directory if there is none
                root.mkdirs();
                Log.d(TAG, "generateSecretFolder: main **************************");
            }
        } catch (Exception e) {
            Log.d(TAG, "generateFolder: Exception thrown");
        }
    }

    /*
    Generate a simple folder, not hidden
     */
    public void generateFolder(String dirName) {
        try {

            File root = new File(Environment.getExternalStorageDirectory() + getDataDir(this) + "/" + dirName);
            Log.d(TAG, "generateSecretFolder: " + Environment.getExternalStorageDirectory() + getDataDir(this) + "/" + dirName);
            if (!root.exists()) {  // makes a new directory if there is none
                root.mkdirs();
            }
        } catch (Exception e) {
            Log.d(TAG, "generateFolder: Exception thrown");
        }
    }


    /*
    Copy file from gallery to app folder
     */
    public void moveFile(String location, String name) {

        File source= new File(location);
        File destination= new File(Environment.getExternalStorageDirectory() + getDataDir(this) + "/.HidenFiles/", name);

        try {

            if (source.exists()) {
                FileChannel src = new FileInputStream(source).getChannel();
                FileChannel dst = new FileOutputStream(destination).getChannel();
                dst.transferFrom(src, 0, src.size());
                
                src.close();
                dst.close();
                Log.d(TAG, "moveFile: File moved ********************************");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "moveFile: Exception called ********************************");
        }

        source.delete();  // delete file from gallery and send broadcast to update gallery
        cont.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(location))));

        paths.add(Environment.getExternalStorageDirectory() + getDataDir(this) + "/.HidenFiles/" + name);  //Store paths of file in set
        storeSet(paths);
        Log.d(TAG, "moveFile: Deleted file in source *******************");

        if(!paths.isEmpty()) {
            for(String i : paths) {
                Log.d(TAG, "moveFile: Paths Set ******************* " + i);
            }
        }

        Toasty.success(cont,"File uploaded", Toast.LENGTH_SHORT, true).show();
    }


    /*
    exit on 2 back button actions
     */
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            killProcess(myPid());
        }

        doubleBackToExitPressedOnce = true;
        Toasty.normal(MainActivity.this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    /*
    Permission handlers
     */

    // do we have permissions already
    public  boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG","Permission is granted *********");
                return true;
            } else {

                Log.v("TAG","Permission is not present * * * * ** *");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 99);
                return false;
            }
        }

            Log.v("TAG","SDK lower than 23 ******");
            return true;

    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == 99) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /*
    Class for glide image grid
     */

    private class ImageAdapter extends BaseAdapter {

        private Activity context;


        public ImageAdapter(Activity localContext) {
            context = localContext;
            paths = getSet();
            Log.d(TAG, "ImageAdapter: " + paths.size());
            for(String i: paths){
                displayPaths.add(i);
            }

        }

        public int getCount() {
            return displayPaths.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ImageView picturesView;
            if (convertView == null) {
                picturesView = new ImageView(context);
                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picturesView
                        .setLayoutParams(new GridView.LayoutParams(700, 750));

            } else {
                picturesView = (ImageView) convertView;
            }

           Glide.with(context).load(displayPaths.get(position)).into(picturesView);

            return picturesView;
        }

    }
}
