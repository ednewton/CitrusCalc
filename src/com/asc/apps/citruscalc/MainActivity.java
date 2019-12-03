package com.asc.apps.citruscalc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;

public class MainActivity extends Activity {
    private static final String DECIMAL_FORMAT = "%3.1f";
    private MainActivitySO so;
    private EditText gpd;
    private EditText timerValue;
    private final Context context = this;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
    private CountDownTimer countDownTimer;
    private long valueInMillis;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // This is the Smart Object (see the article "The Humble Dialog Box" by Michael Feathers at
        // http://www.objectmentor.com/resources/articles/TheHumbleDialogBox.pdf)
        so = new MainActivitySO(this);

        createConfigControl();
        createDiameterControl();
        createTypeControl();

        // grab the output fields that we'll update
        gpd = (EditText) findViewById(R.id.gpd);
        timerValue = (EditText) findViewById(R.id.minutesAtRate);
        timerValue.setText(simpleDateFormat.format(0));
        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCountdownTimer();
                countDownTimer.start();
            }
        });
        Button pauseButton = (Button) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
            }
        });
    }

    private void createConfigControl() {
        ImageView gear = (ImageView) findViewById(R.id.gear);
        gear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View flowRatePrompt = li.inflate(R.layout.frpromt, null);

                // set prompts.xml to alertdialog builder
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);
                alertDialogBuilder.setView(flowRatePrompt);

                final EditText userInput = (EditText) flowRatePrompt
                        .findViewById(R.id.flowRate);

                userInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String text = userInput.getText().toString();
                        if (text == null || text.equals("") || Float.valueOf(text) <= 0.0f) {
                            userInput.setError(getText(R.string.flowRateError));
                        }
                    }
                });

                float flowRate = so.getFlowRate();
                if (flowRate > 0.0f)
                    userInput.setText(String.format(DECIMAL_FORMAT, flowRate));

                // set dialog message
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton(getText(R.string.okButtonText),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                so.setFlowRate(Float.valueOf(userInput.getText().toString()));
                                so.calculate();
                            }
                        });
                alertDialogBuilder.setNegativeButton(getText(R.string.cancelButtonText),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        // if the user has never set the flow rate, he or she must set it before continuing.
        if (so.getFlowRate() < 0.0f) {
            gear.performClick();
        }
    }

    private void createTypeControl() {
    /* The typeSpinner allows a user to select a tree type:  Grapefruit, Mandarin, or other. */
        Spinner typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                so.typeSelected((String) parent.getItemAtPosition(position));
                so.calculate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op since something will always be selected.
            }
        });

        typeSpinner.setAdapter(getCharSequenceArrayAdapter(R.array.type_array));
        so.typeSelected((String) typeSpinner.getItemAtPosition(0));
    }

    private void createDiameterControl() {
    /*
    The diameterSpinner allows a user to select a tree diameter between 2 and 30 (step 2).
     */
        Spinner diameterSpinner = (Spinner) findViewById(R.id.diameterSpinner);
        diameterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                so.diameterSelected(Integer.parseInt((String) parent.getItemAtPosition(position)));
                so.calculate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op since something will always be selected.
            }
        });

        diameterSpinner.setAdapter(getCharSequenceArrayAdapter(R.array.diameter_array));
        so.diameterSelected(Integer.parseInt((String) diameterSpinner.getItemAtPosition(0)));
    }

    private ArrayAdapter<CharSequence> getCharSequenceArrayAdapter(int array) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    // How many gallons/day the tree needs.
    public void setGpd(double gpd) {
        this.gpd.setText(String.format(DECIMAL_FORMAT, gpd));
    }

    // based on the flow rate and the gallons/day, how long must I water the tree?
    public void setTimerValue(double minutes) {
        if (countDownTimer != null)
            countDownTimer.cancel();

        valueInMillis = (long) (minutes * 60 * 1000);
        this.timerValue.setText(simpleDateFormat.format(valueInMillis));

        createCountdownTimer();
    }

    private void createCountdownTimer() {
        countDownTimer = new CountDownTimer((long) valueInMillis, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                valueInMillis = millisUntilFinished;
                timerValue.setText(simpleDateFormat.format(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                // alert the user
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setMessage(R.string.timerFinished);
                alertDialogBuilder.setTitle(R.string.timerAlertDialogTitle);

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });

                alertDialog.show();

                valueInMillis = 0;
                timerValue.setText(simpleDateFormat.format(valueInMillis));
            }
        };
    }
}
