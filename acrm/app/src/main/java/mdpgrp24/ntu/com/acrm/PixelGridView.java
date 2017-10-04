package mdpgrp24.ntu.com.acrm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import java.util.ArrayList;

/**
 * Created by shelinalusandro on 2/9/17.
 */

public class PixelGridView extends View {

    private int numColumns = 15;
    private int numRows = 20;
    private int cellWidth, cellHeight;
    private Paint blackPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint redPaint = new Paint();
    private Paint whitePaint = new Paint();
    private Paint bluePaint = new Paint();
    private Paint pinkPaint = new Paint();

    private boolean[][] cellChecked;
    private int[][] cellType;
    private boolean[][] cellFront;
    private boolean[][] cellRear;
    private boolean[][] cellCenter;
    private boolean[][] cellWaypoint;
    private String type = null;
    private ArrayList<Rect> rectangles = new ArrayList<Rect>();

    //north 0, south 180, east 90, west 270
    private int currentAngle;
    Bitmap rightArrow;
    Bitmap leftArrow;
    Bitmap upArrow;
    Bitmap downArrow;
    PixelGridView pgv;
    private float scale = 1f;
    private ScaleGestureDetector SGD;

    //call from class
    private String startpoint = null;
    private String waypoint = null;
    private boolean startclick = true;
    private boolean waypointclick = true;

    public String getStartpoint(){return startpoint;}
    public String getWaypoint(){return waypoint;}
    public boolean getWaypointClick(){return waypointclick;}
    public boolean getStartClick(){return startclick;}

    private ArenaActivity arena;

    private boolean enableClick = false;

    public PixelGridView(Context context) {this(context, null);
    }

    public PixelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        SGD = new ScaleGestureDetector(context, new ScaleListener());
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        greenPaint.setColor(Color.GREEN);
        redPaint.setColor(Color.RED);
        whitePaint.setColor(Color.WHITE);
        bluePaint.setColor(Color.BLUE);
        pinkPaint.setColor(Color.MAGENTA);

//        rightArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.right_icon_32);
//        leftArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.left_icon_32);
//        upArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.up_icon_32);
//        downArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.down_icon_32);
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        calculateDimensions(0,0);
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
        calculateDimensions(0,0);
    }

    public int getNumRows() {
        return numRows;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setEnabledPGV(boolean enabled){
        this.enableClick = enabled;
    }

    //define the center coordinates for robot startpoint or the waypoint
    public void setCoordinates(int coor1,int coor2){
        calculateDimensions(coor1,coor2);

        //testing
        System.out.println(coor2 + "-" + coor1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions(0,0);
    }

    private void calculateDimensions(int x ,  int y) {
        if (x == 0 && y == 0) {
            if (numColumns == 0 || numRows == 0)
                return;

            cellWidth = getWidth() / numColumns;
            cellHeight = getHeight() / numRows;

            cellChecked = new boolean [numRows][numColumns];
            cellWaypoint = new boolean[numRows][numColumns];

            cellFront = new boolean[numRows][numColumns];
            cellRear = new boolean[numRows][numColumns];
            cellCenter = new boolean[numRows][numColumns];

            cellType = new int[numRows][numColumns];

            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    cellType[i][j] = 0;
                }
            }
            //default cellChecked in the middle
            //AMD test
//            cellFront[0][1] = true;
//            cellRear[0][0] = true;

            invalidate();

        } else {

            if(type == "startpoint" && y!=0 && x!=0){

                for (int i = 0; i < numRows; i++) {
                    for (int j = 0; j < numColumns; j++) {
                        if(cellCenter[i][j]){cellCenter[i][j]=!cellCenter[i][j];}
                        if(cellFront[i][j]){cellFront[i][j]=!cellFront[i][j];}
                        if(cellRear[i][j]){cellRear[i][j]=!cellRear[i][j];}
                    }
                }

                cellCenter[x][y] = true;
                cellRear[x-1][y-1] = true;
                cellRear[x-1][y] = true;
                cellRear[x-1][y+1] = true;
                cellRear[x][y-1] = true;
                cellRear[x][y+1] = true;
                cellRear[x+1][y-1] = true;
                cellRear[x+1][y+1] = true;
                cellFront[x+1][y] = true;
                if(currentAngle==0){

                }else if(currentAngle==90){
                    cellFront[x+1][y] = !cellFront[x+1][y];
                    cellFront[x][y+1] = !cellFront[x][y+1];
                    cellRear[x+1][y] = !cellRear[x+1][y];
                    cellRear[x][y+1] = !cellRear[x][y+1];
                }else if (currentAngle==180){
                    cellFront[x+1][y] = !cellFront[x+1][y];
                    cellFront[x-1][y] = !cellFront[x-1][y];
                    cellRear[x+1][y] = !cellRear[x+1][y];
                    cellRear[x-1][y] = !cellRear[x-1][y];
                }else if (currentAngle==270){
                    cellFront[x+1][y] = !cellFront[x+1][y];
                    cellFront[x][y-1] = !cellFront[x][y-1];
                    cellRear[x+1][y] = !cellRear[x+1][y];
                    cellRear[x][y-1] = !cellRear[x][y-1];
                }

            }
            else if(type == "waypoint"){
                cellType[x][y] = 2;
                cellWaypoint[x][y] = true;
                System.out.println("waypoint at: "+x+","+y);
            }

            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //SGD.onTouchEvent(ev);
        if (enableClick == false) {
            System.out.println("TOUCH Check hereeee WRONG");
            return false;
        } else {
            int touchX = (int) event.getX();
            int touchY = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    System.out.println("Touching down!");
                    for (Rect rect : rectangles) {
                        if (rect.contains(touchX, touchY)) {
                            int index = rectangles.indexOf(rect);
                            final int row = index / numColumns;
                            final int column = index % numColumns;
                            final String rowstr = ("0" + row).substring(("0" + row).length() - 2);
                            final String colstr = ("0" + column).substring(("0" + column).length() - 2);

                            final String point = colstr + "-" + rowstr;

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                            //Set message
                            builder.setMessage("Are you sure you want to select " + colstr + "-" + rowstr + " as " + type + " ?");

                            // Add the buttons
                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (type == "startpoint") {
                                        ArenaActivity.getInstance().startclick=false;
                                        ArenaActivity.getInstance().startpoint="startpoint="+point;
                                        setEnabledPGV(false);
                                        //ArenaActivity.getInstance().sendMessage("startpoint coordinate ("+column+","+row+")");
                                        //robot
                                        ArenaActivity.getInstance().sendMessage("Psp:"+row+":"+column+":0");
                                        System.out.println("Startpoint received! " + point);
                                        currentAngle = 0;
                                        setCoordinates(row,column);
                                    } else if (type == "waypoint") {
                                        ArenaActivity.getInstance().waypointclick=false;
                                        ArenaActivity.getInstance().waypoint="waypoint="+point;
                                        setEnabledPGV(false);
                                        //ArenaActivity.getInstance().sendMessage("waypoint coordinate ("+column+","+row+")");
                                        //robot
                                        ArenaActivity.getInstance().sendMessage("Pwp:"+row+":"+column);
                                        System.out.println("Waypoint received! " + point);
                                        setCoordinates(row,column);
                                    }
                                    dialog.dismiss();
                                }
                            });

                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                            //Create alert dialog
                            AlertDialog alertD = builder.create();
                            alertD.show();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //System.out.println("Touching up!");
                    break;
                case MotionEvent.ACTION_MOVE:
                    //System.out.println("Sliding your finger around on the screen.");
                    break;
            }
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //System.out.println("Hi");
            scale *= detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(scale, 5.0f));
            // matrix.setScale(scale, scale);
            //img.setImageMatrix(matrix);

            invalidate();
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(scale, scale);
        //...
        //Your onDraw() code
        //...


        canvas.drawColor(Color.WHITE);

        if (numColumns == 0 || numRows == 0)
            return;

        int width = getWidth();
        int height = getHeight();

        rectangles.clear();

        //AMD test
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (cellRear[i][j]  || cellCenter[i][j]) {
//                    System.out.println(cellRear[i][j]);
//                    System.out.println(cellCenter[i][j]);
                    Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * (cellHeight), (j + 1) * cellWidth, (numRows - 1 - i + 1) * (cellHeight));
                    canvas.drawRect(rect, bluePaint);
                    rectangles.add(rect);
//                    System.out.println("Blue at"+j+"-"+i);
                } else if (cellFront[i][j]) {
                    Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight);
                    canvas.drawRect(rect, greenPaint);
                    rectangles.add(rect);
                }else if (cellWaypoint[i][j]){
                    Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight);
                    canvas.drawRect(rect, pinkPaint);
                    rectangles.add(rect);
                } else {
                    //amd
                    if (cellType[i][j] == 0) {
                        Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight);
                        canvas.drawRect(rect, redPaint);
                        rectangles.add(rect);
                        //System.out.println("Red");
                    } else if (cellType[i][j] == 1) {
                        Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight);
                        canvas.drawRect(rect, whitePaint);
                        rectangles.add(rect);
                        // System.out.println("White");
                    } else if (cellType[i][j] == 2) {
                        Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight);
                        canvas.drawRect(rect, pinkPaint);
                        rectangles.add(rect);
                        System.out.println("Pink");
                    }else if (cellType[i][j] == 3) {
                        //obstacle, color black
                        Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight);
                        canvas.drawRect(rect, blackPaint);
                        rectangles.add(rect);
                        // System.out.println("Black");
                    }
                }
            }
        }

        //drawing lines
        for (int i = 0; i < numColumns+1; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, blackPaint);
        }

        for (int i = 0; i < numRows+1; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, blackPaint);
        }
        canvas.restore();
    }

    public void moveRight() {
        int columnFront = -1;
        int rowFront = -1;
        if (currentAngle == 90) {
            ArenaActivity.getInstance().sendMessage("AF:1");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        cellChecked[i][j]=true;
                        rowFront = i;
                        columnFront = j;
                        System.out.println(i);
                        System.out.println(j);

                    }
                }
            }
            if(columnFront+1 < numColumns && columnFront >=0) {
                if (cellType[rowFront][columnFront+1] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront][columnFront+1] = !cellFront[rowFront][columnFront+1];
                    cellCenter[rowFront][columnFront-1] = !cellCenter[rowFront][columnFront-1];
                    cellCenter[rowFront][columnFront] = !cellCenter[rowFront][columnFront];

                    cellRear[rowFront+1][columnFront] = !cellRear[rowFront+1][columnFront];
                    cellRear[rowFront+1][columnFront+1] = !cellRear[rowFront+1][columnFront+1];
                    cellRear[rowFront-1][columnFront] = !cellRear[rowFront-1][columnFront];
                    cellRear[rowFront-1][columnFront+1] = !cellRear[rowFront-1][columnFront+1];
                    cellRear[rowFront+1][columnFront-1] = !cellRear[rowFront+1][columnFront-1];
                    cellRear[rowFront+1][columnFront] = !cellRear[rowFront+1][columnFront];
                    cellRear[rowFront-1][columnFront-1] = !cellRear[rowFront-1][columnFront-1];
                    cellRear[rowFront-1][columnFront] = !cellRear[rowFront-1][columnFront];
                    cellRear[rowFront+1][columnFront-2] = !cellRear[rowFront+1][columnFront-2];
                    cellRear[rowFront+1][columnFront-1] = !cellRear[rowFront+1][columnFront-1];
                    cellRear[rowFront][columnFront-2] = !cellRear[rowFront][columnFront-2];
                    cellRear[rowFront][columnFront-1] = !cellRear[rowFront][columnFront-1];
                    cellRear[rowFront-1][columnFront-2] = !cellRear[rowFront-1][columnFront-2];
                    cellRear[rowFront-1][columnFront-1] = !cellRear[rowFront-1][columnFront-1];
                }
            }
//    		else if (columnFront+1 == numColumns) {
//    			if (cellFront[rowFront][columnFront]) {
//    				cellRear[rowFront][columnFront-1] = !cellRear[rowFront][columnFront-1];
//        			cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
//        			cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
//    			}
//    		}
        } else if (currentAngle == 0) {
            ArenaActivity.getInstance().sendMessage("AR:90");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront+1 != numColumns) {
                if (cellType[rowFront][columnFront+1] != 3 && cellType[rowFront-1][columnFront+1] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront-1][columnFront+1] = !cellFront[rowFront-1][columnFront+1];
                    cellRear[rowFront-1][columnFront+1] = !cellRear[rowFront-1][columnFront+1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    currentAngle = 90;
                }
            }
        } else if (currentAngle == 180) {
            ArenaActivity.getInstance().sendMessage("AL:90");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront+1 != numColumns ) {
                if (cellType[rowFront][columnFront+1] != 3 && cellType[rowFront+1][columnFront+1] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront+1] = !cellFront[rowFront+1][columnFront+1];
                    cellRear[rowFront+1][columnFront+1] = !cellRear[rowFront+1][columnFront+1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    currentAngle = 90;
                }
            }
        } else if (currentAngle == 270) {
            ArenaActivity.getInstance().sendMessage("AP:180");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront+1 != numColumns ) {
                cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                cellFront[rowFront][columnFront+2] = !cellFront[rowFront][columnFront+2];
                cellRear[rowFront][columnFront+2] = !cellRear[rowFront][columnFront+2];
                cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                currentAngle = 90;
            }
        }
        invalidate();
    }
    public void moveLeft() {
        int columnFront = -1;
        int rowFront = -1;
        if (currentAngle == 270) {
            ArenaActivity.getInstance().sendMessage("AF:1");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront-1 != -1) {
                if (cellType[rowFront][columnFront-1] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront][columnFront-1] = !cellFront[rowFront][columnFront-1];
                    cellCenter[rowFront][columnFront+1] = !cellCenter[rowFront][columnFront+1];
                    cellCenter[rowFront][columnFront] = !cellCenter[rowFront][columnFront];

                    cellRear[rowFront+1][columnFront] = !cellRear[rowFront+1][columnFront];
                    cellRear[rowFront+1][columnFront-1] = !cellRear[rowFront+1][columnFront-1];
                    cellRear[rowFront-1][columnFront] = !cellRear[rowFront-1][columnFront];
                    cellRear[rowFront-1][columnFront-1] = !cellRear[rowFront-1][columnFront-1];
                    cellRear[rowFront+1][columnFront+1] = !cellRear[rowFront+1][columnFront+1];
                    cellRear[rowFront+1][columnFront] = !cellRear[rowFront+1][columnFront];
                    cellRear[rowFront-1][columnFront+1] = !cellRear[rowFront-1][columnFront+1];
                    cellRear[rowFront-1][columnFront] = !cellRear[rowFront-1][columnFront];
                    cellRear[rowFront+1][columnFront+2] = !cellRear[rowFront+1][columnFront+2];
                    cellRear[rowFront+1][columnFront+1] = !cellRear[rowFront+1][columnFront+1];
                    cellRear[rowFront][columnFront+2] = !cellRear[rowFront][columnFront+2];
                    cellRear[rowFront][columnFront+1] = !cellRear[rowFront][columnFront+1];
                    cellRear[rowFront-1][columnFront+2] = !cellRear[rowFront-1][columnFront+2];
                    cellRear[rowFront-1][columnFront+1] = !cellRear[rowFront-1][columnFront+1];
                }
            }
        } else if (currentAngle == 0) {
            ArenaActivity.getInstance().sendMessage("AL:90");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront-1 != -1) {
                if (cellType[rowFront][columnFront-1] != 3 && cellType[rowFront-1][columnFront-1] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront-1][columnFront-1] = !cellFront[rowFront-1][columnFront-1];
                    cellRear[rowFront-1][columnFront-1] = !cellRear[rowFront-1][columnFront-1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];

                    currentAngle = 270;
                }
            }
        } else if (currentAngle == 180) {
            ArenaActivity.getInstance().sendMessage("AR:90");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront-1 != -1) {
                if (cellType[rowFront][columnFront-1] != 3 && cellType[rowFront+1][columnFront-1] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront-1] = !cellFront[rowFront+1][columnFront-1];
                    cellRear[rowFront+1][columnFront-1] = !cellRear[rowFront+1][columnFront-1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    currentAngle = 270;
                }
            }
        } else if (currentAngle == 90) {
            ArenaActivity.getInstance().sendMessage("AP:180");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront-1 != -1) {
                if (cellType[rowFront][columnFront-1] != 3 && cellType[rowFront+1][columnFront-1] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront][columnFront-2] = !cellFront[rowFront][columnFront-2];
                    cellRear[rowFront][columnFront-2] = !cellRear[rowFront][columnFront-2];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    currentAngle = 270;
                }
            }
        }
        invalidate();
    }

    public void moveForward() {
        int columnFront = -1;
        int rowFront = -1;
        if (currentAngle == 90) {
            ArenaActivity.getInstance().sendMessage("AL:90");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront+1 != numRows) {
                if (cellType[rowFront+1][columnFront] != 3 && cellType[rowFront+1][columnFront-1] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront-1] = !cellFront[rowFront+1][columnFront-1];
                    cellRear[rowFront+1][columnFront-1] = !cellRear[rowFront+1][columnFront-1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    currentAngle = 0;
                }
            }
        } else if (currentAngle == 0) {
            ArenaActivity.getInstance().sendMessage("AF:1");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront+1 != numRows) {
                if (cellType[rowFront+1][columnFront] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront] = !cellFront[rowFront+1][columnFront];
                    cellCenter[rowFront-1][columnFront] = !cellCenter[rowFront-1][columnFront];
                    cellCenter[rowFront][columnFront] = !cellCenter[rowFront][columnFront];

                    cellRear[rowFront][columnFront-1] = !cellRear[rowFront][columnFront-1];
                    cellRear[rowFront+1][columnFront-1] = !cellRear[rowFront+1][columnFront-1];
                    cellRear[rowFront][columnFront+1] = !cellRear[rowFront][columnFront+1];
                    cellRear[rowFront+1][columnFront+1] = !cellRear[rowFront+1][columnFront+1];
                    cellRear[rowFront-1][columnFront-1] = !cellRear[rowFront-1][columnFront-1];
                    cellRear[rowFront][columnFront-1] = !cellRear[rowFront][columnFront-1];
                    cellRear[rowFront-1][columnFront+1] = !cellRear[rowFront-1][columnFront+1];
                    cellRear[rowFront][columnFront+1] = !cellRear[rowFront][columnFront+1];
                    cellRear[rowFront-2][columnFront-1] = !cellRear[rowFront-2][columnFront-1];
                    cellRear[rowFront-1][columnFront-1] = !cellRear[rowFront-1][columnFront-1];
                    cellRear[rowFront-2][columnFront] = !cellRear[rowFront-2][columnFront];
                    cellRear[rowFront-1][columnFront] = !cellRear[rowFront-1][columnFront];
                    cellRear[rowFront-2][columnFront+1] = !cellRear[rowFront-2][columnFront+1];
                    cellRear[rowFront-1][columnFront+1] = !cellRear[rowFront-1][columnFront+1];
                }
            }
        } else if (currentAngle == 270) {
            ArenaActivity.getInstance().sendMessage("AR:90");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront+1 != numRows) {
                if (cellType[rowFront+1][columnFront] != 3 && cellType[rowFront+1][columnFront+1] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront+1] = !cellFront[rowFront+1][columnFront+1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    cellRear[rowFront+1][columnFront+1] = !cellRear[rowFront+1][columnFront+1];
                    currentAngle = 0;
                }
            }
        } else if (currentAngle == 180){
            ArenaActivity.getInstance().sendMessage("AP:180");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront+1 != numRows) {
                cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                cellFront[rowFront+2][columnFront] = !cellFront[rowFront+2][columnFront];
                cellRear[rowFront+2][columnFront] = !cellRear[rowFront+2][columnFront];
                cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                currentAngle = 0;
            }
        }
        invalidate();
    }
    public void moveDown() {
        int columnFront = -1;
        int rowFront = -1;
        if (currentAngle == 90) {
            ArenaActivity.getInstance().sendMessage("AR:90");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront-1 != -1) {
                if (cellType[rowFront-1][columnFront] != 3 && cellType[rowFront-1][columnFront-1] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront-1][columnFront-1] = !cellFront[rowFront-1][columnFront-1];
                    cellRear[rowFront-1][columnFront-1] = !cellFront[rowFront-1][columnFront-1];
                    cellRear[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    currentAngle = 180;
                }
            }
        } else if (currentAngle == 180) {
            ArenaActivity.getInstance().sendMessage("AF:1");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront-1 != -1) {
                if (cellType[rowFront-1][columnFront] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront-1][columnFront] = !cellFront[rowFront-1][columnFront];
                    cellCenter[rowFront+1][columnFront] = !cellCenter[rowFront+1][columnFront];
                    cellCenter[rowFront][columnFront] = !cellCenter[rowFront][columnFront];

                    cellRear[rowFront][columnFront-1] = !cellRear[rowFront][columnFront-1];
                    cellRear[rowFront-1][columnFront-1] = !cellRear[rowFront-1][columnFront-1];
                    cellRear[rowFront][columnFront+1] = !cellRear[rowFront][columnFront+1];
                    cellRear[rowFront-1][columnFront+1] = !cellRear[rowFront-1][columnFront+1];
                    cellRear[rowFront+1][columnFront-1] = !cellRear[rowFront+1][columnFront-1];
                    cellRear[rowFront][columnFront-1] = !cellRear[rowFront][columnFront-1];
                    cellRear[rowFront+1][columnFront+1] = !cellRear[rowFront+1][columnFront+1];
                    cellRear[rowFront][columnFront+1] = !cellRear[rowFront][columnFront+1];
                    cellRear[rowFront+2][columnFront-1] = !cellRear[rowFront+2][columnFront-1];
                    cellRear[rowFront+1][columnFront-1] = !cellRear[rowFront+1][columnFront-1];
                    cellRear[rowFront+2][columnFront+1] = !cellRear[rowFront+2][columnFront+1];
                    cellRear[rowFront+1][columnFront+1] = !cellRear[rowFront+1][columnFront+1];
                    cellRear[rowFront+2][columnFront] = !cellRear[rowFront+2][columnFront];
                    cellRear[rowFront+1][columnFront] = !cellRear[rowFront+1][columnFront];
                }
            }
        } else if (currentAngle == 270) {
            ArenaActivity.getInstance().sendMessage("AL:90");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront-1 != -1) {
                if (cellType[rowFront-1][columnFront] != 3 && cellType[rowFront-1][columnFront+1] != 3) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront-1][columnFront+1] = !cellFront[rowFront-1][columnFront+1];
                    cellRear[rowFront-1][columnFront+1] = !cellRear[rowFront-1][columnFront+1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    currentAngle = 180;
                }
            }
        } else if (currentAngle == 0) {
            ArenaActivity.getInstance().sendMessage("AP:180");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront-1 != -1) {
                cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                cellFront[rowFront-2][columnFront] = !cellFront[rowFront-2][columnFront];
                cellRear[rowFront-2][columnFront] = !cellRear[rowFront-2][columnFront];
                cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                currentAngle = 180;
            }
        }
        invalidate();
    }

    public void updateMap() {
        invalidate();
    }

    public void robotPosition(int row, int column, String direction){
        ArenaActivity.getInstance().sendMessage("PACKrobotPos");
        if (direction.equalsIgnoreCase("0")) {
            currentAngle = 0;
        } else if (direction.equalsIgnoreCase("90")) {
            currentAngle = 90;
        } else if (direction.equalsIgnoreCase("180")) {
            currentAngle = 180;
        } else if (direction.equalsIgnoreCase("270")) {
            currentAngle = 270;
        }

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if(cellCenter[i][j]){cellCenter[i][j]=!cellCenter[i][j];}
                if(cellFront[i][j]){cellFront[i][j]=!cellFront[i][j];}
                if(cellRear[i][j]){cellRear[i][j]=!cellRear[i][j];}
            }
        }

        type = "startpoint";
        setCoordinates(row,column);
    }

    public void mapExploration(String mapExplore){
        ArenaActivity.getInstance().sendMessage("PACKmapExp");
        String mapExpFilter = mapExplore.replaceAll(" ", "");
        String mapExpBinary = hex2binary(mapExpFilter);

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (mapExpBinary.length() != 0) {
                    System.out.println("cellType :" +i+"-"+j+cellType[i][j]);
                    if(cellWaypoint[i][j] != true || cellType[i][j]!=3 || cellFront[i][j]!=true || cellCenter[i][j] != true || cellRear[i][j]!=true) {
                        cellType[i][j] = Integer.parseInt(mapExpBinary.substring(0, 1));
                    }
                    System.out.println("exploration: row" + i + "col" + j + "=" + cellType[i][j]);
                    mapExpBinary = mapExpBinary.substring(1);
                }
            }
        }
        invalidate();
    }

    public void mapObstacle(String mapGrid) {
        ArenaActivity.getInstance().sendMessage("PACKmapGrid");
        String mapGridFilter = mapGrid.replaceAll(" ", "");
        String mapGridBinary = hex2binary(mapGridFilter);

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (mapGridBinary.length() != 0) {
                    if(cellWaypoint[i][j] != true || cellType[i][j]!=0 || cellFront[i][j]!=true || cellCenter[i][j] != true || cellRear[i][j]!=true) {
                        if (Integer.parseInt(mapGridBinary.substring(0, 1)) == 1) {
                            cellType[i][j] = 3;
                            System.out.println("updateobstaclereally: row"+i+"col"+j+"="+cellType[i][j]);
                        }
                    }
                    System.out.println("updateobstacle: row"+i+"col"+j+"="+cellType[i][j]);
                    mapGridBinary = mapGridBinary.substring(1);
                }
            }
        }
        invalidate();
    }

    public void mapInStringAMD(String map, int column, int row, String direction) {

        if (direction.equalsIgnoreCase("0")) {
            currentAngle = 0;
        } else if (direction.equalsIgnoreCase("90")) {
            currentAngle = 90;
        } else if (direction.equalsIgnoreCase("180")) {
            currentAngle = 180;
        } else if (direction.equalsIgnoreCase("270")) {
            currentAngle = 270;
        }

        type = "startpoint";
        setCoordinates(row,column);

        String mapFilter = map.replaceAll(" ", "");

        String mapBinary = hex2binary(mapFilter);

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (mapBinary.length() != 0) {
                    cellType[i][j] = Integer.parseInt(mapBinary.substring(0,1));
                    System.out.println("row"+i+"col"+j+"="+cellType[i][j]);
                    mapBinary = mapBinary.substring(1);
                }
            }
        }
        invalidate();
    }

    public static String hex2binary(String hex) {
        StringBuilder result = new StringBuilder(hex.length() * 4);
        for (char c : hex.toUpperCase().toCharArray()) {
            switch (c) {
                case '0': result.append("0000"); break;
                case '1': result.append("0001"); break;
                case '2': result.append("0010"); break;
                case '3': result.append("0011"); break;
                case '4': result.append("0100"); break;
                case '5': result.append("0101"); break;
                case '6': result.append("0110"); break;
                case '7': result.append("0111"); break;
                case '8': result.append("1000"); break;
                case '9': result.append("1001"); break;
                case 'A': result.append("1010"); break;
                case 'B': result.append("1011"); break;
                case 'C': result.append("1100"); break;
                case 'D': result.append("1101"); break;
                case 'E': result.append("1110"); break;
                case 'F': result.append("1111"); break;
                default: throw new IllegalArgumentException("Invalid hex: '" + hex + "'");
            }
        }
        return result.toString();
    }
}
