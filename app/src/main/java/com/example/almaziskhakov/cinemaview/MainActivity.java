package com.example.almaziskhakov.cinemaview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements CinemaView.ChairCallback{

    CinemaView uiCinema;
    TextView   uiChairsState;
    Switch     uiBlockSold;
    Switch     uiBlockSoldAndReserved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiCinema = (CinemaView) findViewById(R.id.cinema);
        uiChairsState = (TextView) findViewById(R.id.chair_states);
        uiBlockSold = (Switch) findViewById(R.id.sold_block);
        uiBlockSoldAndReserved = (Switch) findViewById(R.id.reserved_and_sold_block);

        uiCinema.setChairCallback(this);
    }

    public void onViewStatesClick(View view) {
        String states = "";
        CinemaView.ChairView[][] chairs = uiCinema.getChairs();
        int rowCount = uiCinema.getRowCount();
        int columnCount = uiCinema.getColumnCount();

        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                states += chairs[row][column].getState() + " ";
            }
            states += "\n";
        }

        uiChairsState.setText(states);
    }

    public void onClearClick(View view) {
        uiCinema.clear();
    }

    public void onBlockSoldClick(View view) {
        if (((Switch) view).isChecked()) {
            uiCinema.blockAllSold();
        } else {
            uiCinema.unblockAll();
            uiBlockSold.setChecked(false);
            uiBlockSoldAndReserved.setChecked(false);
        }
    }

    public void onBlockSoldAndReserved(View view) {
        if (((Switch) view).isChecked()) {
            uiCinema.blockAllSoldAndReserved();
        } else {
            uiCinema.unblockAll();
            uiBlockSold.setChecked(false);
            uiBlockSoldAndReserved.setChecked(false);
        }
    }

    @Override public void onChairClick(CinemaView.ChairView view, int row, int column) {
        Toast.makeText(this, "row: " + row + ", column: " + column, Toast.LENGTH_LONG).show();
    }
}
