package com.android.jsonregistercheck.collegeinfo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jsonregistercheck.R;
import com.android.jsonregistercheck.dark_statusbar.DarkStatusBar;
import com.android.jsonregistercheck.list_expandable_height.ExpandableListViewHeight;
import com.android.jsonregistercheck.model.College_Aboutus;
import com.android.jsonregistercheck.model.ReviewClass;
import com.android.jsonregistercheck.model.Review_withdetails;
import com.android.jsonregistercheck.retrofit_apiclient.RetrofitClient;
import com.android.jsonregistercheck.retrofit_interface.ApiInterface;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class College_Info extends AppCompatActivity {
    ExpandableListViewHeight listView;
    Toolbar toolbar;
    TextView toolbar_title, txt_college_name, txt_college_desc, txt_comments_count, txt_no_review;
    ImageView item_image;
    EditText edit_comment;
    RelativeLayout post_review;
    String review, menu_items_id;
    Button btn_about;
    String image,userid,username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college__info);

        listView = findViewById(R.id.review_list);
        toolbar = findViewById(R.id.tool_bar);
        item_image = findViewById(R.id.item_image);
        txt_college_desc = findViewById(R.id.txt_item_desc);
        txt_college_name = findViewById(R.id.txt_item_name);
        edit_comment = findViewById(R.id.edit_comment);
        txt_comments_count = findViewById(R.id.txt_comments);
        txt_no_review = findViewById(R.id.txt_no_review);
        post_review = findViewById(R.id.post_review);
        btn_about=findViewById(R.id.about_us);

        toolbar_title = toolbar.findViewById(R.id.toolbar_title);

        menu_items_id = getIntent().getExtras().getString("id");
        final String collegename = getIntent().getExtras().getString("college_name");


        btn_about.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                try{
                    Intent intent = new Intent(College_Info.this,Aboutus_Activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("id",menu_items_id);
                    intent.putExtra("college_name",collegename);



                    startActivity(intent);
                }catch (AndroidRuntimeException e){
                    e.printStackTrace();
                }


                Toast.makeText(College_Info.this,"You clicked "+collegename, Toast.LENGTH_SHORT).show();
            }
        });


        toolbar_title.setText(collegename);


        Window window = getWindow();
        View view = window.getDecorView();
        DarkStatusBar.setLightStatusBar(view, this);

        listView.setFocusable(false);

        post_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               reviewEdit();
            }
        });


        /////// get Review related to menu item
        getReviews(menu_items_id);




    }

    public void getReviews(String menu_items_id){


        ApiInterface menuItemApiInterface = RetrofitClient.getFormData().create(ApiInterface.class);



        Call<List<Review_withdetails>> model = menuItemApiInterface.getReview(menu_items_id);
         model.enqueue(new Callback<List<Review_withdetails>>() {
             @Override
             public void onResponse(Call<List<Review_withdetails>> call, Response<List<Review_withdetails>> response) {
                 List<Review_withdetails> reviews= response.body();
                 Reviewadapter recyclerviewadapter = new Reviewadapter(College_Info.this, reviews);
                 //Recyclerviewadapter recyclerviewadapter1 =new Recyclerviewadapter(MainActivity.this,response.body());
                 RecyclerView.LayoutManager layoutManager = new GridLayoutManager(College_Info.this, 1);
                 //  LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(College_Display.this, LinearLayoutManager.HORIZONTAL, false);
                 //recyclerView.setLayoutManager(horizontalLayoutManager);
                 listView.setAdapter(recyclerviewadapter);
                 listView.setExpanded(true);

                 recyclerviewadapter.notifyDataSetChanged();
             }

             @Override
             public void onFailure(Call<List<Review_withdetails>> call, Throwable t) {

             }


         });

   }

    public void reviewEdit(){

        review = edit_comment.getText().toString();

        if (review.equals("")){
            Toast.makeText(this, "Please write some review", Toast.LENGTH_SHORT).show();
       }else {

            //// retrive data from shared preference //////

           SharedPreferences sharedPreferences = getSharedPreferences("UserDetail", Context.MODE_PRIVATE);
            image = sharedPreferences.getString("image",null);
            username = sharedPreferences.getString("username","");
            userid = sharedPreferences.getString("customer_id","");

           if (!username.equals("") && !userid.equals("")){
               reviewPost(menu_items_id,userid,username,review,image);
                edit_comment.setText("");
            }



        }
    }
   public void reviewPost(final String collegeid, String  userid, String username, String review, String image){

        ApiInterface postReviewApi = RetrofitClient.getFormData().create(ApiInterface.class);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("college_id",collegeid)
                .addFormDataPart("user_id",userid)
                .addFormDataPart("name",username)
                .addFormDataPart("review",review)
                .addFormDataPart("image",image)
                .build();



        postReviewApi.postReview(requestBody).enqueue(new Callback<ResponseBody>() {
           @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {

                 //  the response-body is already parseable to your ResponseBody object

                   ResponseBody responseBody = response.body();

            //     you can do whatever with the response body now...

                   String responseBodyString = null;
                    try {
                        responseBodyString = responseBody.string();
                        Log.d("Response body", responseBodyString);

                       try {
                           JSONObject jsonObjet = new JSONObject(responseBodyString);
                            String code = jsonObjet.getString("status");
                            String message = jsonObjet.getString("message");

                           if (code.equals("0")) {



                               Toast.makeText(College_Info.this, message, Toast.LENGTH_SHORT).show();
                           } else if (code.equals("1")) {
                                getReviews(menu_items_id);



                                Toast.makeText(College_Info.this, message, Toast.LENGTH_SHORT).show();

                            }
                            else{
                               Toast.makeText(College_Info.this, message, Toast.LENGTH_SHORT).show();
                           }

                       } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException ex) {
                           ex.printStackTrace();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                   }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }
    public void backArrow(View view) {
        finish();
    }
}

