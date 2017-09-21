package com.example.bdondapati.customdocumentbrowser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by BDondapati on 21-09-2017.
 */

public class MainActivity extends AppCompatActivity {

    private Button mPickDocuments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPickDocuments= (Button) findViewById(R.id.pick_document);

        mPickDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, CustomFileBrowserActivity.class);
                startActivity(intent);
            }
        });

    }
}
