package de.bennir.DVBViewerController;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockActivity;

/**
 * User: miriam
 * Date: 13.12.12
 * Time: 21:00
 */
public class DeviceSelectionActivity extends SherlockActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_deviceselection);

        Button btnSkip = (Button) findViewById(R.id.button_skip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DeviceSelectionActivity.this, DVBViewerControllerActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                DeviceSelectionActivity.this.finish();
            }
        });
    }
}