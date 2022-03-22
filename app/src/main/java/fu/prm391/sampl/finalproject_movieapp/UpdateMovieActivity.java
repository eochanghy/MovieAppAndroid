package fu.prm391.sampl.finalproject_movieapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fu.prm391.sampl.finalproject_movieapp.Model.VideoUploadDetails;

public class UpdateMovieActivity extends AppCompatActivity {
    private TextView etName;
    private TextView etDescription;
    private ImageView imageView;
    private List<String> categories;
    private Spinner spinner;
    private VideoUploadDetails selectedVideo;
    private RadioButton rdbLatest, rdbPopular, rdbNotype, rdbSlide, selectedRdb;
    private RadioGroup rdgGroup;
    private DatabaseReference videoRef;
    private Button btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_movie);

        Bundle bundle = getIntent().getExtras();
        if(bundle==null) return;
         selectedVideo = (VideoUploadDetails) bundle.get("selectedVideo");

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        imageView = findViewById(R.id.imageView2);
        spinner = findViewById(R.id.spinner2);
        rdbLatest = findViewById(R.id.rdbLastMovie2);
        rdbPopular = findViewById(R.id.rdbPopMovie2);
        rdbNotype = findViewById(R.id.rdbNoType2);
        rdbSlide = findViewById(R.id.rdbSlideMovie2);
        rdgGroup = findViewById(R.id.radioGroup2);
        btnUpdate = findViewById(R.id.btnUpdate2);

        etName.setText(selectedVideo.getVideoName());
        etDescription.setText(selectedVideo.getVideoDescription());
        Glide.with(this).load(selectedVideo.getVideoThumb()).into(imageView);
        initCategory();
        if(selectedVideo.getVideoType().equals("No Type")){
            rdbNotype.setChecked(true);
        }else if(selectedVideo.getVideoType().equals("Latest Movie")){
            rdbLatest.setChecked(true);
        }else if(selectedVideo.getVideoType().equals("Best Popular Movie")){
            rdbPopular.setChecked(true);
        }else if(selectedVideo.getVideoType().equals("Slide Movie")){
            rdbSlide.setChecked(true);
        }

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoRef = FirebaseDatabase.getInstance().getReference().child("videos");
                videoRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot item: snapshot.getChildren()) {
                            if(item.getValue(VideoUploadDetails.class).getVideoId().equals(selectedVideo.getVideoId())) {
                                System.out.println(etName.getText());
                                item.getRef().child("videoName").setValue(etName.getText().toString());
                                item.getRef().child("videoCategory").setValue(spinner.getSelectedItem().toString());
                                selectedRdb = findViewById(rdgGroup.getCheckedRadioButtonId());
                                item.getRef().child("videoType").setValue(selectedRdb.getText());
                                item.getRef().child("videoDescription").setValue(etDescription.getText().toString());
                                Toast.makeText(getApplicationContext(), "Update Successfully !!!" ,Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });


    }

    private void initCategory() {
        categories = new ArrayList<>();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference().child("category");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot item: snapshot.getChildren()) {
                    categories.add(item.getValue(String.class));
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(UpdateMovieActivity.this,
                        android.R.layout.simple_spinner_item, categories);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(dataAdapter);
                int i = 0;
                for(String s : categories){
                    if(s.equals(selectedVideo.getVideoCategory())){
                        spinner.setSelection(i);
                        break;
                    }
                    i++;
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}