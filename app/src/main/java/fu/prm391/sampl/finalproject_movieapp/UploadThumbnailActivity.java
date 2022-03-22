package fu.prm391.sampl.finalproject_movieapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class UploadThumbnailActivity extends AppCompatActivity {

    final static String NO_FILE_SELECTED = "No file selected";

    private Uri videoThumbUri;
    private String thumbnailUrl;
    private ImageView thumbImage;
    private StorageReference mStorageThumb;
    private DatabaseReference referenceVideo;
    private TextView txtSelected;
    private RadioButton rdbLatest, rdbPopular, rdbNotype, rdbSlide;
    private StorageTask mStorageTask;
    private DatabaseReference updateDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_thumbnail);

        txtSelected = findViewById(R.id.txtThumbSelected);
        thumbImage = findViewById(R.id.imgView);
        rdbLatest = findViewById(R.id.rdbLastMovie2);
        rdbNotype = findViewById(R.id.rdbNoType2);
        rdbPopular = findViewById(R.id.rdbPopMovie2);
        rdbSlide = findViewById(R.id.rdbSlideMovie2);
        mStorageThumb = FirebaseStorage.getInstance().getReference().child("VideoThumbnails");
        referenceVideo = FirebaseDatabase.getInstance().getReference().child("videos");
        String currentUID = getIntent().getExtras().getString("currentUID");
        updateDataRef = FirebaseDatabase.getInstance().getReference("videos").child(currentUID);
//        rdbLatest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String latestMovie = rdbLatest.getText().toString();
//                updateDataRef.child("videoSlide").setValue(latestMovie);
//                Toast.makeText(UploadThumbnailActivity.this, "Selected: "+latestMovie, Toast.LENGTH_SHORT).show();
//            }
//        });

        rdbNotype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String latestMovie = rdbLatest.getText().toString();
                updateDataRef.child("videoType").setValue("");
                updateDataRef.child("videoSlide").setValue("");
                Toast.makeText(UploadThumbnailActivity.this, "Selected: No type", Toast.LENGTH_SHORT).show();
            }
        });
        rdbLatest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String latestMovie = rdbLatest.getText().toString();
                updateDataRef.child("videoType").setValue(latestMovie);
                updateDataRef.child("videoSlide").setValue("");
                Toast.makeText(UploadThumbnailActivity.this, "Selected: "+latestMovie, Toast.LENGTH_SHORT).show();
            }
        });
        rdbPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String popMovie = rdbPopular.getText().toString();
                updateDataRef.child("videoType").setValue(popMovie);
                updateDataRef.child("videoSlide").setValue("");
                Toast.makeText(UploadThumbnailActivity.this, "Selected: "+popMovie, Toast.LENGTH_SHORT).show();
            }
        });
        rdbSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String slideMovie = rdbSlide.getText().toString();
                updateDataRef.child("videoSlide").setValue(slideMovie);
                Toast.makeText(UploadThumbnailActivity.this, "Selected: "+slideMovie, Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void showImageChooser(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 102 && resultCode == RESULT_OK && data.getData() != null) {
            videoThumbUri = data.getData();
            try {
                String thumbName = getFileName(videoThumbUri);
                txtSelected.setText(thumbName);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), videoThumbUri);
                thumbImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileName (Uri uri) {
        String result = null;
        if(uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(
                    uri,
                    null,
                    null,
                    null,
                    null
            );
            try {
                if(cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut != -1) {
                result = result.substring(cut+1);
            }
        }
        return result;
    }

    private void uploadFile() {
        if(videoThumbUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading thumbnail...");
            progressDialog.show();
            String videoTitle = getIntent().getExtras().getString("thumbnailsName");

            StorageReference sRef = mStorageThumb.child(videoTitle+"."+ getFileExtension(videoThumbUri));
            sRef.putFile(videoThumbUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            thumbnailUrl = uri.toString();
                            updateDataRef.child("videoThumb").setValue(thumbnailUrl);
                            progressDialog.dismiss();
                            Toast.makeText(UploadThumbnailActivity.this, "File uploaded", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadThumbnailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading..."+(int)progress + "%");
                }
            });

        }
    }
    public void uploadFileToFirebase(View view) {
        if(txtSelected.equals(NO_FILE_SELECTED)) {
            Toast.makeText(UploadThumbnailActivity.this, "Please select image", Toast.LENGTH_SHORT).show();
        } else {
            if(mStorageTask != null && mStorageTask.isInProgress()) {
                Toast.makeText(this, "Upload file already in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadFile();
            }
        }
    }
    public String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }
}