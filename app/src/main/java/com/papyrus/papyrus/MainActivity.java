package com.papyrus.papyrus;

import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;


public class MainActivity extends AppCompatActivity {
    private PapSurfaceView drawView;
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn;
    private static final String TAG = "MainActivity";
    public BlockingQueue<byte[]> outcomeMessageQueue = new ArrayBlockingQueue<>(1200);
    public BlockingQueue<byte[]> incomeMessageQueue = new ArrayBlockingQueue<>(1200);

    private int remoteServerPort = 11500;
    private int clientPort = 50001;
    private int serverPort = 50000;
    private String serverIp = "";
    private FindServerTask fs;

    private LinearLayout paintLayout;

    private Handler mHandler = new Handler();


    private static int lastCommand = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_main);
            findServer();
            drawView = (PapSurfaceView) findViewById(R.id.papSurface);
            drawView.setQueue(outcomeMessageQueue);


            UdpClientThread client = new UdpClientThread(remoteServerPort, serverPort, serverIp, outcomeMessageQueue);
            UdpServerThread server = new UdpServerThread(clientPort, incomeMessageQueue);
            //DataProcessor incomeDataProcessor = new DataProcessor(incomeMessageQueue, this, drawView);

            ExecutorService executorService = Executors.newFixedThreadPool(2);
            executorService.submit(client);
            executorService.submit(server);
            //executorService.submit(incomeDataProcessor);

            drawView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        outcomeMessageQueue.put(("SIZE|" + drawView.getWidth() + "|" + drawView.getHeight()).getBytes());
                    } catch (InterruptedException e) {
                    }
                }
            });

            try {
                outcomeMessageQueue.put("REGISTER".getBytes());
            } catch (InterruptedException e) {
            }
            paintLayout = (LinearLayout) findViewById(R.id.paint_colors1);
            currPaint = (ImageButton) paintLayout.getChildAt(7);
            currPaint.setImageResource(R.drawable.paint_pressed);

            drawBtn = (ImageButton) findViewById(R.id.brush_btn);
            drawBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendDrawButtonCommand(DrawCommand.BUTTON_DRAW, "");
                    drawView.setErase(false);
                }
            });


            eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
            eraseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendDrawButtonCommand(DrawCommand.BUTTON_ERASE, "");
                    drawView.setColor("#FFFFFFFF");
                    drawView.setErase(true);
                    Log.i(TAG, "Erase is true");
                }
            });

            newBtn = (ImageButton) findViewById(R.id.new_btn);
            newBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawView.setErase(false);
                    sendDrawButtonCommand(DrawCommand.BUTTON_ERASE, "");
                    AlertDialog.Builder newDialog = new AlertDialog.Builder(MainActivity.this);
                    newDialog.setTitle("New drawing");
                    newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
                    newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            sendDrawButtonCommand(DrawCommand.BUTTON_NEW, "");
                            drawView.startNew();
                            dialog.dismiss();
                        }
                    });
                    newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    newDialog.show();
                }
            });

            saveBtn = (ImageButton) findViewById(R.id.save_btn);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawView.setErase(true);
                    AlertDialog.Builder saveDialog = new AlertDialog.Builder(MainActivity.this);
                    saveDialog.setTitle("Save drawing");
                    saveDialog.setMessage("Save drawing to device Gallery?");
                    saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //save drawing
                            drawView.setDrawingCacheEnabled(true);
                            //attempt to save

                            String imgSaved = MediaStore.Images.Media.insertImage(
                                    MainActivity.this.getContentResolver(), drawView.getDrawingCache(),
                                    UUID.randomUUID().toString() + ".png", "drawing");
                            String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
                            Log.i(TAG, file_path);
                            //feedback
                            if (imgSaved != null) {
                                Toast savedToast = Toast.makeText(getApplicationContext(),
                                        "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                                savedToast.show();
                            } else {
                                Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                        "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                                unsavedToast.show();
                            }
                            drawView.destroyDrawingCache();
                        }
                    });
                    saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    saveDialog.show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void paintClicked(View view) {
        drawView.setErase(false);
        //use chosen color
        if (view != currPaint) {
            //update color
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            Log.i(TAG, color);
            drawView.setColor(color);
            imgView.setImageResource(R.drawable.paint_pressed);
            currPaint.setImageResource(R.drawable.paint);
            currPaint = (ImageButton) view;
            Log.i(TAG, currPaint.toString());
            sendDrawButtonCommand(DrawCommand.BUTTON_COLOR, color);
        }
    }

    private void sendDrawButtonCommand(int action, String color) {
        DrawCommand command = new DrawCommand();
        command.setAction(action);
        if (action == DrawCommand.BUTTON_COLOR) {
            command.setColor(color);
        }
        Gson gson = new Gson();
        try {
            outcomeMessageQueue.put(gson.toJson(command).getBytes());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getRemoteServerPort() {
        return remoteServerPort;
    }

    /*public DrawingView getDrawView() {
        return drawView;
    }*/

    private void findServer() throws InterruptedException, ExecutionException, TimeoutException {
        fs = new FindServerTask(this);
        fs.execute();
        serverIp = fs.get(2, TimeUnit.SECONDS);
        Log.i(TAG, "Fetched Server IP: " + serverIp);
    }
}
