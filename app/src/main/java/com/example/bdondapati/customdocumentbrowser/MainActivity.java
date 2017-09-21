package com.example.bdondapati.customdocumentbrowser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by BDondapati on 21-09-2017.
 */

public class MainActivity extends AppCompatActivity {

    private int DOCUMENT_REQUEST_CODE = 100;

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
                startActivityForResult(intent, DOCUMENT_REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == DOCUMENT_REQUEST_CODE  )
        {
            if(resultCode==RESULT_OK)
            {
                String documentURI = data.getStringExtra("resultURI");

                Toast.makeText(MainActivity.this, "Selected Document Path at:: " + documentURI , Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(MainActivity.this, "Document Selection is cancelled by user" , Toast.LENGTH_LONG).show();
            }
        }

    }
}
