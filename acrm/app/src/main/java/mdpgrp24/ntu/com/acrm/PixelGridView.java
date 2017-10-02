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

    //FUNCTION NOT IMPLEMENTED
    //Exit Arena everything will be restored

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

        rightArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.right_icon_32);
        leftArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.left_icon_32);
        upArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.up_icon_32);
        downArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.down_icon_32);
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

            cellChecked = new boolean[numRows][numColumns];

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
                cellCenter[x][y] = true;
                cellRear[x-1][y-1] = true;
                cellRear[x-1][y] = true;
                cellRear[x-1][y+1] = true;
                cellRear[x][y-1] = true;
                cellRear[x][y+1] = true;
                cellRear[x+1][y-1] = true;
                cellRear[x+1][y+1] = true;
                cellFront[x+1][y] = true;

                //actual robot
//                cellChecked[x][y] = true;
//                cellChecked[x-1][y-1] = true;
//                cellChecked[x-1][y] = true;
//                cellChecked[x-1][y+1] = true;
//                cellChecked[x][y-1] = true;
//                cellChecked[x][y+1] = true;
//                cellChecked[x+1][y-1] = true;
//                cellChecked[x+1][y+1] = true;
//                cellChecked[x+1][y] = true;
                System.out.println("CHECK ROBOT STARTPOINT");
            }
            else if(type == "waypoint"){
                System.out.println("CHECK CELL TYPE WAYPOINT");
                cellType[x][y] = 2;
            }

            //default cellChecked in the middle
            //AMD test
//            cellFront[x][y]=true;
//            cellRear[x][y-1]=true;
            currentAngle = 0;
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
                            System.out.println("Touched Rectangle" + colstr + "-" + rowstr);

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
                                        ArenaActivity.getInstance().sendMessage("startpoint coordinate ("+column+","+row+")");
                                        //robot
                                        //ArenaActivity.getInstance().sendMessage("waypoint="+point);
                                        System.out.println("Startpoint received! " + point);
                                        setCoordinates(row,column);
                                    } else if (type == "waypoint") {
                                        ArenaActivity.getInstance().waypointclick=false;
                                        ArenaActivity.getInstance().waypoint="waypoint="+point;
                                        setEnabledPGV(false);
                                        ArenaActivity.getInstance().sendMessage("waypoint coordinate ("+column+","+row+")");
                                        //robot
                                        //ArenaActivity.getInstance().sendMessage("waypoint="+point);
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
                //cellChecked[i][j]=true;
               /* if(cellChecked[i][j]) {
                    System.out.println("Green");
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, greenPaint);
               }*/
                if (cellRear[i][j]  || cellCenter[i][j]) {
                    System.out.println(cellRear[i][j]);
                    System.out.println(cellCenter[i][j]);
                    Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * (cellHeight), (j + 1) * cellWidth, (numRows - 1 - i + 1) * (cellHeight));
                    canvas.drawRect(rect, bluePaint);
                    rectangles.add(rect);
                    //canvas.drawRect((j-1) * cellWidth, (numRows-1-i) * (cellHeight), (j) * cellWidth, (numRows-1-i + 1) * (cellHeight), greenPaint);
                    cellChecked[i][j] = true;
                    System.out.println("Blue at"+j+"-"+i);
                } else if (cellFront[i][j]) {
                    Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight);
                    canvas.drawRect(rect, redPaint);
                    rectangles.add(rect);
                    cellChecked[i][j] = true;
                    // System.out.println("Red");
                } else {
                    if (cellType[i][j] == 0) {
                        Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight);
                        canvas.drawRect(rect, whitePaint);
                        rectangles.add(rect);
                        //System.out.println("White");
                    } else if (cellType[i][j] == 1) {
                        Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight);
                        canvas.drawRect(rect, blackPaint);
                        // System.out.println("Black");
                    } else if (cellType[i][j] == 2) {
                        Rect rect = new Rect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight);
                        canvas.drawRect(rect, pinkPaint);
                        System.out.println("Pink");
                    }
                }

               /* if (cellChecked[i][j]) {
                    System.out.println("Green");
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, greenPaint);
                    System.out.println(cellWidth);
                    System.out.println(cellHeight);

                }*/
            }

        }

        //actual robot
//        for (int i = 0; i < numRows; i++) {
//            for (int j = 0; j < numColumns; j++) {
//                if (cellChecked[i][j]) {
//                	//cellChecked = current robot position, cellChecked in green
//                    //canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, greenPaint);
//                	if (currentAngle == 0) {
//                		canvas.drawBitmap(upArrow, j * cellWidth, i * cellHeight, null);
//                	} else if (currentAngle == 90) {
//                		canvas.drawBitmap(rightArrow, j * cellWidth, i * cellHeight, null);
//                	} else if (currentAngle == 180) {
//                		canvas.drawBitmap(downArrow, j * cellWidth, i * cellHeight, null);
//                	} else if (currentAngle == 270) {
//                		canvas.drawBitmap(leftArrow, j * cellWidth, i * cellHeight, null);
//                	}
//                } else {
//
//	                if (cellType[i][j] == 0) {
//	                	//red
//	                	//unexplored, define to do
//                      Rect rect = new Rect(j * cellWidth, i * cellHeight, (j + 1) * cellWidth, (i + 1) * cellHeight);
//	                	canvas.drawRect(rect, redPaint)
//                      rectangles.add(rect);
//	                } else if (cellType[i][j] == 1) {
//	                	//white
//	                	//empty, define to do
//                      Rect rect = new Rect(j * cellWidth, i * cellHeight, (j + 1) * cellWidth, (i + 1) * cellHeight);
//	                	canvas.drawRect(rect, whitePaint);
//                      rectangles.add(rect);
// 	                } else if (cellType[i][j] == 2) {
//	                	//waypoint, color pink
//                      Rect rect = new Rect(j * cellWidth, i * cellHeight, (j + 1) * cellWidth, (i + 1) * cellHeight);
//	                	canvas.drawRect(rect, blackPaint);
//                      rectangles.add(rect);
//	                } else if (cellType[i][j] == 3) {
//	                	//obstacle, color black
//                      Rect rect = new Rect(j * cellWidth, i * cellHeight, (j + 1) * cellWidth, (i + 1) * cellHeight);
//	                	canvas.drawRect(rect, blackPaint);
//                      rectangles.add(rect);
//	                } else if (cellType[i][j] == 4) {
//	                	//green
//                      Rect rect = new Rect(j * cellWidth, i * cellHeight, (j + 1) * cellWidth, (i + 1) * cellHeight);
//	                	canvas.drawRect(rect, greenPaint);
//                      rectangles.add(rect);
//	                }
//                }
//            }
//        }

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
            ArenaActivity.getInstance().sendMessage("FORWARD");
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
                if (cellType[rowFront][columnFront+1] != 1) {
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
            ArenaActivity.getInstance().sendMessage("ROTATERIGHT");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront+1 != numColumns) {
                if (cellType[rowFront][columnFront+1] != 1 && cellType[rowFront-1][columnFront+1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront-1][columnFront+1] = !cellFront[rowFront-1][columnFront+1];
                    cellRear[rowFront-1][columnFront+1] = !cellRear[rowFront-1][columnFront+1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    currentAngle = 90;
                }
            }
        } else if (currentAngle == 180) {
            ArenaActivity.getInstance().sendMessage("ROTATELEFT");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront+1 != numColumns ) {
                if (cellType[rowFront][columnFront+1] != 1 && cellType[rowFront+1][columnFront+1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront+1] = !cellFront[rowFront+1][columnFront+1];
                    cellRear[rowFront+1][columnFront+1] = !cellRear[rowFront+1][columnFront+1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    currentAngle = 90;
                }
            }
        } else if (currentAngle == 270) {
            ArenaActivity.getInstance().sendMessage("REVERSE");
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
            ArenaActivity.getInstance().sendMessage("FORWARD");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront-1 != -1) {
                if (cellType[rowFront][columnFront-1] != 1) {
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
            ArenaActivity.getInstance().sendMessage("ROTATELEFT");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront-1 != -1) {
                if (cellType[rowFront][columnFront-1] != 1 && cellType[rowFront-1][columnFront-1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront-1][columnFront-1] = !cellFront[rowFront-1][columnFront-1];
                    cellRear[rowFront-1][columnFront-1] = !cellRear[rowFront-1][columnFront-1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];

                    currentAngle = 270;
                }
            }
        } else if (currentAngle == 180) {
            ArenaActivity.getInstance().sendMessage("ROTATERIGHT");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront-1 != -1) {
                if (cellType[rowFront][columnFront-1] != 1 && cellType[rowFront+1][columnFront-1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront-1] = !cellFront[rowFront+1][columnFront-1];
                    cellRear[rowFront+1][columnFront-1] = !cellRear[rowFront+1][columnFront-1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    currentAngle = 270;
                }
            }
        } else if (currentAngle == 90) {
            ArenaActivity.getInstance().sendMessage("REVERSE");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront-1 != -1) {
                if (cellType[rowFront][columnFront-1] != 1 && cellType[rowFront+1][columnFront-1] != 1) {
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
            ArenaActivity.getInstance().sendMessage("ROTATELEFT");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront+1 != numRows) {
                if (cellType[rowFront+1][columnFront] != 1 && cellType[rowFront+1][columnFront-1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront-1] = !cellFront[rowFront+1][columnFront-1];
                    cellRear[rowFront+1][columnFront-1] = !cellRear[rowFront+1][columnFront-1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    currentAngle = 0;
                }
            }
        } else if (currentAngle == 0) {
            ArenaActivity.getInstance().sendMessage("FORWARD");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront+1 != numRows) {
                if (cellType[rowFront+1][columnFront] != 1) {
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
            ArenaActivity.getInstance().sendMessage("ROTATERIGHT");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront+1 != numRows) {
                if (cellType[rowFront+1][columnFront] != 1 && cellType[rowFront+1][columnFront+1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront+1] = !cellFront[rowFront+1][columnFront+1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    cellRear[rowFront+1][columnFront+1] = !cellRear[rowFront+1][columnFront+1];
                    currentAngle = 0;
                }
            }
        } else if (currentAngle == 180){
            ArenaActivity.getInstance().sendMessage("REVERSE");
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
            ArenaActivity.getInstance().sendMessage("ROTATERIGHT");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront-1 != -1) {
                if (cellType[rowFront-1][columnFront] != 1 && cellType[rowFront-1][columnFront-1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront-1][columnFront-1] = !cellFront[rowFront-1][columnFront-1];
                    cellRear[rowFront-1][columnFront-1] = !cellFront[rowFront-1][columnFront-1];
                    cellRear[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    currentAngle = 180;
                }
            }
        } else if (currentAngle == 180) {
            ArenaActivity.getInstance().sendMessage("FORWARD");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront-1 != -1) {
                if (cellType[rowFront-1][columnFront] != 1) {
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
            ArenaActivity.getInstance().sendMessage("ROTATELEFT");
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront-1 != -1) {
                if (cellType[rowFront-1][columnFront] != 1 && cellType[rowFront-1][columnFront+1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront-1][columnFront+1] = !cellFront[rowFront-1][columnFront+1];
                    cellRear[rowFront-1][columnFront+1] = !cellRear[rowFront-1][columnFront+1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                    currentAngle = 180;
                }
            }
        } else if (currentAngle == 0) {
            ArenaActivity.getInstance().sendMessage("REVERSE");
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

    //actual robot
//    public void moveForward() {
//    	int column = -1;
//    	int row = -1;
//    	if (currentAngle == 0) {
//	        for (int i = 0; i < numRows; i++) {
//	            for (int j = 0; j < numColumns; j++) {
//	                if (cellChecked[i][j]) {
//	                    row = i;
//	                    column = j;
//
//	                }
//	            }
//	        }
//	        //single grid movement
//	        //cellChecked[column][row] = !cellChecked[column][row];
//	    	//cellChecked[column+1][row] = !cellChecked[column+1][row];
//	        //last cell checked == [1][1]
//
//	        if(column+1 != numColumns) {
////	        	//4 grids
////	        	//uncheck cells
//		        cellChecked[column-1][row-1] = !cellChecked[column-1][row-1];
//		        cellChecked[column-1][row] = !cellChecked[column-1][row];
//		        //check cellls
//		        cellChecked[column+1][row] = !cellChecked[column+1][row];
//		        cellChecked[column+1][row-1] = !cellChecked[column+1][row-1];
////	        	cellChecked[column-1][row] = !cellChecked[column-1][row];
////	        	cellChecked[column+1][row] = !cellChecked[column+1][row];
//
//	        }
//
//	    	invalidate();
//    	} else if (currentAngle == 90) {
//    		for (int i = 0; i < numColumns; i++) {
//	            for (int j = 0; j < numRows; j++) {
//	                if (cellChecked[i][j]) {
//	                    column = i;
//	                    row = j;
//	                }
//	            }
//	        }
//    		//single grid movement
//    		//cellChecked[column][row] = !cellChecked[column][row];
//    		//cellChecked[column][row+1] = !cellChecked[column][row+1];
//
//    		 if(row+1 != numRows) {
//	    		//uncheck cells
//		    	cellChecked[column-1][row-1] = !cellChecked[column-1][row-1];
//		    	cellChecked[column][row-1] = !cellChecked[column][row-1];
//		    	//check cells
//		    	cellChecked[column-1][row+1] = !cellChecked[column-1][row+1];
//		    	cellChecked[column][row+1] = !cellChecked[column][row+1];
//    		 }
//
//	    	invalidate();
//    	} else if (currentAngle == 180) {
//    		for (int i = 0; i < numColumns; i++) {
//	            for (int j = 0; j < numRows; j++) {
//	                if (cellChecked[i][j]) {
//	                    column = i;
//	                    row = j;
//	                }
//	            }
//	        }
//    		//single grid movement
//	        //cellChecked[column][row] = !cellChecked[column][row];
//	    	//cellChecked[column-1][row] = !cellChecked[column-1][row];
//
//    		//uncheck cells
//    		if(column-2 != -1) {
//	    		cellChecked[column][row] = !cellChecked[column][row];
//	    		cellChecked[column][row-1] = !cellChecked[column][row-1];
//	    		//check cells
//	    		cellChecked[column-2][row] = !cellChecked[column-2][row];
//	    		cellChecked[column-2][row-1] = !cellChecked[column-2][row-1];
//    		}
//
//	    	invalidate();
//    	} else if (currentAngle == 270) {
//    		for (int i = 0; i < numColumns; i++) {
//	            for (int j = 0; j < numRows; j++) {
//	                if (cellChecked[i][j]) {
//	                    column = i;
//	                    row = j;
//	                }
//	            }
//	        }
//    		//single grid movement
//	        //cellChecked[column][row] = !cellChecked[column][row];
//	    	//cellChecked[column][row-1] = !cellChecked[column][row-1];
//
//    		if(row-2 != -1) {
//	    		//uncheck cells
//	    		cellChecked[column][row] = !cellChecked[column][row];
//	    		cellChecked[column-1][row] = !cellChecked[column-1][row];
//	    		//check cells
//	    		cellChecked[column][row-2] = !cellChecked[column][row-2];
//	    		cellChecked[column-1][row-2] = !cellChecked[column-1][row-2];
//    		}
//
//    		invalidate();
//    	}
//    }
//
//    public void moveRight() {
//    	if (currentAngle == 0) {
//    		currentAngle = 90;
//    	} else if (currentAngle == 90) {
//    		currentAngle = 180;
//    	} else if (currentAngle == 180) {
//    		currentAngle = 270;
//    	} else {
//    		currentAngle = 0;
//    	}
//    	invalidate();
//    }
//
//    public void moveLeft() {
//    	if (currentAngle == 0) {
//    		currentAngle = 270;
//    	} else if (currentAngle == 90) {
//    		currentAngle = 0;
//    	} else if (currentAngle == 180) {
//    		currentAngle = 90;
//    	} else {
//    		currentAngle = 180;
//    	}
//    	invalidate();
//    }

    public void updateMap() {
        invalidate();
    }

    public void mapInString(String map, int column, int row, String direction) {

        if (direction.equalsIgnoreCase("n")) {
            currentAngle = 0;
        } else if (direction.equalsIgnoreCase("e")) {
            currentAngle = 90;
        } else if (direction.equalsIgnoreCase("s")) {
            currentAngle = 180;
        } else if (direction.equalsIgnoreCase("w")) {
            currentAngle = 270;
        }

        String mapFilter = map.replaceAll(" ", "");
        //String mapFilter = map.replaceAll(",", "");

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (mapFilter.length() != 0) {
                    cellType[i][j] = Integer.parseInt(mapFilter.substring(0,1));
                    mapFilter = mapFilter.substring(1);
                }
            }
        }

        invalidate();
    }

    public void mapInStringAMD(String map, int column, int row) {
//        int colBlue = 0;
//        int rowBlue = 0;
//
//        int colRed = 1;
//        int rowRed = 0;
//    	cellChecked[colBlue][rowBlue] = !cellChecked[rowBlue][rowBlue];
//    	cellChecked[colRed][rowRed] = !cellChecked[colRed][rowRed];

        String mapFilter = map.replaceAll(" ", "");

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                if (mapFilter.length() != 0) {
                    cellType[i][j] = Integer.parseInt(mapFilter.substring(0,1));
                    mapFilter = mapFilter.substring(1);
                }
            }
        }
        invalidate();
    }
}
