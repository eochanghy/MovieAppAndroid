package fu.prm391.sampl.finalproject_movieapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

import fu.prm391.sampl.finalproject_movieapp.Model.VideoUploadDetails;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    static final String NO_VIDEO = "No video selected";


    private Uri videoUri;
    private TextView txtVideoSelected;
    private String videoCategory;
    private String videoTitle;
    private String currentUID;
    private StorageReference mstorafeRef;
    private StorageTask mUploadTask;
    private DatabaseReference referenceVideo;
    private EditText videoDescription;
    private Spinner spinner;
    private List<String> categories;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtVideoSelected = findViewById(R.id.textVideoSelected);
        videoDescription = findViewById(R.id.movieDescription);
        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        referenceVideo = FirebaseDatabase.getInstance().getReference().child("videos");
        mstorafeRef = FirebaseStorage.getInstance().getReference().child("videos");
        categories = new ArrayList<>();
        categories.add("Action");
        categories.add("Adventure");
        categories.add("Sports");
        categories.add("Romantic");
        categories.add("Comedy");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        videoCategory = parent.getItemAtPosition(position).toString();

        Toast.makeText(this, "Selected: "+videoCategory, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void openVideoFiles(View view) {
        if (Build.VERSION.SDK_INT < 19) {
            final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/* video/*");
            startActivityForResult(photoPickerIntent, 101);
        } else {
            final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("*/*");
            photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
            startActivityForResult(photoPickerIntent, 101);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data.getData() != null ) {
            videoUri = data.getData();
//            String path = null;
//            int columnIndexData;
            String [] projection = {
                    MediaStore.MediaColumns.DATA,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Thumbnails.DATA
            };
            final String orderBy = MediaStore.Video.Media.DEFAULT_SORT_ORDER;
            Cursor cursor = getContentResolver().query(
                    videoUri,
                    projection,
                    null,
                    null,
                    null
            );
//            cursor.moveToFirst();
//            int columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                System.out.println("Path: "+ path);
                videoTitle = FilenameUtils.getBaseName(path);
            }
            txtVideoSelected.setText(videoTitle);
        }
    }

    public void uploadFiletoFireBase(View view) {
        if(txtVideoSelected.equals(NO_VIDEO)) {
            Toast.makeText(this, "Please select a video", Toast.LENGTH_SHORT).show();

        } else {
            if(mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(this, "Video uploads is all ready in progrress", Toast.LENGTH_SHORT).show();
            } else {
                uploadFile();
            }
        }
    }

    private void uploadFile() {
        if(videoUri != null) {
            System.out.println("Video Uri: "+videoUri);
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Video uploading...");
            progressDialog.show();
            final StorageReference storageReference = mstorafeRef.child(videoTitle);
            mUploadTask = storageReference.putFile(videoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String videoUrl = uri.toString();
                            VideoUploadDetails videoUploadDetails = new VideoUploadDetails(
                                    "",
                                    "",
                                    "",
                                    videoUrl,
                                    videoTitle,
                                    videoDescription.getText().toString(),
                                    videoCategory
                                    );
                            String uploadId = referenceVideo.push().getKey();
                            referenceVideo.child(uploadId).setValue(videoUploadDetails);
                            currentUID = uploadId;
                            progressDialog.dismiss();
                            if(currentUID.equals(uploadId)) {
                                startThumbnailActivity();
                            }
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded "+ (int) progress+ "%...");
                }
            });
        } else {
            Toast.makeText(this, NO_VIDEO, Toast.LENGTH_SHORT).show();
        }
    }
    public void startThumbnailActivity() {
        Intent intent = new Intent(MainActivity.this, UploadThumbnailActivity.class);
        intent.putExtra("currentUID", currentUID);
        intent.putExtra("thumbnailsName", videoTitle);
        startActivity(intent);
        Toast.makeText(this, "Upload video success..Please upload thumbnail!", Toast.LENGTH_LONG).show();

    }
}