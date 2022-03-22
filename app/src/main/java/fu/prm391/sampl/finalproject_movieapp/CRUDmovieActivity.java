package fu.prm391.sampl.finalproject_movieapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fu.prm391.sampl.finalproject_movieapp.Model.VideoUploadDetails;
import fu.prm391.sampl.finalproject_movieapp.adapter.MyAdapter;

public class CRUDmovieActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<VideoUploadDetails> listMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud_movie);
        recyclerView = findViewById(R.id.recyclerVirew);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listMovie = new ArrayList<>();

        initMovie();

    }

    private void initMovie() {
        listMovie = new ArrayList<>();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference().child("videos");
        videoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listMovie.clear();
                for(DataSnapshot item: snapshot.getChildren()) {
                    listMovie.add(item.getValue(VideoUploadDetails.class));
                }
                MyAdapter adapter = new MyAdapter(listMovie, getApplicationContext(), new MyAdapter.IClickListener() {
                    @Override
                    public void onClickUpdateItem(VideoUploadDetails videoUploadDetails) {
                        Intent intent = new Intent(getApplicationContext(), UpdateMovieActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("selectedVideo",videoUploadDetails);
                        intent.putExtras(bundle);
                        getApplicationContext().startActivity(intent);
                    }

                    @Override
                    public void onClickDeleteItem(VideoUploadDetails videoUploadDetails) {
                        DatabaseReference videoRef2;
                        videoRef2 = FirebaseDatabase.getInstance().getReference().child("videos");
                        videoRef2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot item: snapshot.getChildren()) {
                                    if(item.getValue(VideoUploadDetails.class).getVideoId().equals(videoUploadDetails.getVideoId())){
                                        item.getRef().removeValue();
                                        Toast.makeText(getApplicationContext(), "Delete Successfully !!!" ,Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                });
                recyclerView.setAdapter(adapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}