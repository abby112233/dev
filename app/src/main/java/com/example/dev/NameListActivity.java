package com.example.dev;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NameListActivity extends AppCompatActivity {
    private ListView namelist;
    private TextView user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hide the bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //hide the title bar
        getSupportActionBar().hide();
        //enable full screen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_name_list);

        this.namelist = (ListView) findViewById(R.id.name_list);

        name_list();

        namelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item text from ListView
                String selectedItem = (String) parent.getItemAtPosition(position);

                // Display the selected item text on toast
                showToast("Your favorite : " + selectedItem);
            }
        });

        //set user name
        this.user_name = (TextView) findViewById(R.id.user_name);
        this.user_name.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    //input data to list view
    private void name_list(){
        String[] foods={"麵線","魯肉飯","小籠包","珍奶","肉包","雞排","便當","大冰奶","原來氏你","舒跑","咖啡"};

        ListView listView=(ListView)findViewById(R.id.name_list);
        listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,foods));
    }
    public void btn_sign_out(View view){
        FirebaseAuth.getInstance().signOut();
        showToast("Signing out your account");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    public void showToast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
}
