package kevin.park.connectfour.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import kevin.park.connectfour.R;

public class GameBoard extends View {
    public Context mContext;
    public DisplayMetrics dm; int screen_width; int screen_height;
    int density;
    boolean initialized = false;
    //Board
    BitmapDrawable board_drawable;
    int board_width;
    int board_height;
    int board_xoffset = 0;
    int board_yoffset = 100;
    //Checkers
    //NOTE: IN THIS GAME, 1 = RED. 2 = BLACK.
    int radius = 58; //The Bitmap itself indicates it should be 56
    int red_checkers; int black_checkers;
    int[] red_coords = new int[21];
    int[] blk_coords = new int[21];
    int[] column_checkers = new int[7];
    Paint red_paint = new Paint();
    Paint blk_paint = new Paint();

    public GameBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        Initialize();
    }

    public void Initialize(){
        dm = getResources().getDisplayMetrics();
        screen_width = dm.widthPixels;
        screen_height = dm.heightPixels;
        density = (int)dm.density;
        board_drawable = (BitmapDrawable)mContext.getResources().getDrawable(R.drawable.board_large, null);
        board_width = board_drawable.getBitmap().getWidth();
        board_height = board_drawable.getBitmap().getHeight();
        board_xoffset = screen_width/2-board_width/2;
        red_paint.setColor(Color.RED);
        blk_paint.setColor(Color.BLACK);
        initialized = true;
    }

    public void onDraw(Canvas c){
        drawCheckers(c);
        drawBoard(c);
    }

    //Below this are all self-contained methods that are not onDraw or Initialize

    public boolean checkForWin(int played, int turn){
        int[] coords = new int[21];
        int checkers = 0;
        //VALUES ARE SWAPPED BECAUSE IT'S ALREADY THE OTHER PLAYER'S TURN
        if(turn == 2){coords = red_coords; checkers = red_checkers;}
        if(turn == 1){coords = blk_coords; checkers = black_checkers;}

        //Check for Four in a row
        boolean same_row = false;
        boolean one_adj = false; boolean two_adj = false; boolean three_adj = false;
        boolean one_adjR = false; boolean two_adjR = false; boolean three_adjR = false;
        boolean one_adjL = false; boolean two_adjL = false; boolean three_adjL = false;
        //1) Check for 4 in a row to the left
        for(int i = 0; i < checkers; i++){
            if(played-coords[i] == 1){one_adjL = true;}
            if(played-coords[i] == 2){two_adjL = true;}
            if(played-coords[i] == 3 && (played%7 <= 4)){three_adjL = true; same_row = true;}
        }
        if(one_adjL && two_adjL && three_adjL && same_row){return true;}
        //2) Check for 4 in a row to the right
        same_row = false;
        for(int i = 0; i < checkers; i++){
            if(played-coords[i] == -1){one_adjR = true;}
            if(played-coords[i] == -2){two_adjR = true;}
            if(played-coords[i] == -3 && played%7 < 4 && played%7 != 0){three_adjR = true; same_row = true;}
        }
        if(one_adjR && two_adjR && three_adjR && same_row){return true;}

        //3) Check for one adjacent to the left and two to the right (or vice versa)
            if(one_adjL && one_adjR && two_adjR){return true;}
            if(one_adjR && one_adjL && two_adjL){return true;}

        //4) Check for 4 in a row up (down is physically impossible based on the game)
        same_row = false;
        for(int i = 0; i < checkers; i++){
            if(played-coords[i] == -7){one_adj = true;}
            if(played-coords[i] == -14){two_adj = true;}
            if(played-coords[i] == -21){three_adj = true;}
        }
        if(one_adj && two_adj && three_adj){return true;}

        //5) Check for 4 in a row D/R or U/L diagonally
        one_adjL = false; two_adjL = false; three_adjL = false;
        one_adjR = false; two_adjR = false; three_adjR = false;
        for(int i = 0; i < checkers; i++){
            if(played-coords[i] == 8){one_adjL = true;}
            if(played-coords[i] == 16){two_adjL = true;}
            if(played-coords[i] == 24){three_adjL = true;}
            if(played-coords[i] == -8){one_adjR = true;}
            if(played-coords[i] == -16){two_adjR = true;}
            if(played-coords[i] == -24){three_adjR = true;}
        }
        if(one_adjL && two_adjL && three_adjL){return true;}
        if(one_adjR && two_adjR && three_adjR){return true;}
        if(one_adjR && two_adjR && one_adjL){return true;}
        if(one_adjL && two_adjL && one_adjR){return true;}

        //6) Check for 4 in a row D/L or U/R diagonally
        one_adjL = false; two_adjL = false; three_adjL = false;
        one_adjR = false; two_adjR = false; three_adjR = false;
        for(int i = 0; i < checkers; i++){
            if(played-coords[i] == 6){one_adjL = true;}
            if(played-coords[i] == 12){two_adjL = true;}
            if(played-coords[i] == 18){three_adjL = true;}
            if(played-coords[i] == -6){one_adjR = true;}
            if(played-coords[i] == -12){two_adjR = true;}
            if(played-coords[i] == -18){three_adjR = true;}
        }
        if(one_adjL && two_adjL && three_adjL){return true;}
        if(one_adjR && two_adjR && three_adjR){return true;}
        if(one_adjR && two_adjR && one_adjL){return true;}
        if(one_adjL && two_adjL && one_adjR){return true;}

        //If nothing found, return false
        return false;
    }

    public void drawBoard(Canvas c){
        c.drawBitmap(board_drawable.getBitmap(),board_xoffset,board_yoffset,null);
    }

    public void drawCheckers(Canvas c) {
        //If Red
        for(int i = 0; i < red_checkers; i++){
            c.drawCircle(69+board_xoffset+140*(valuetoCol(red_coords[i])-1),74+board_yoffset+140*(valuetoRow(red_coords[i])),radius,red_paint);
        }
        //If Black
        for(int i = 0; i < black_checkers; i++) {
            c.drawCircle(69+board_xoffset+140*(valuetoCol(blk_coords[i])-1),74+board_yoffset+140*(valuetoRow(blk_coords[i])),radius,blk_paint);
        }
    }

    public int valuetoCol(int coord){
        if(coord%7 == 0){return 7;}
        return coord%7;
    }

    public int valuetoRow(int coord){
        int index = coord%7;
        index = (coord-coord%7)/7;
        if(coord%7==0){index-=1;}
        return index;
    }

    //Below this are all methods that interact with the MainActivity
    public void checkValues(){
        for(int i = 0; i < red_checkers; i++){
            Log.d("HELPMECHECKRED", ""+red_coords[i]);
        }
        for(int i = 0; i < black_checkers; i++){
            Log.d("HELPMECHECKBLACK", ""+blk_coords[i]);
        }
    }

    public int getScreenHeight(){
        if(initialized){return screen_height;}
        return 0;
    }

    public int getXOffset(){
        if(initialized){return board_xoffset;}
        return 0;
    }

    public int playChecker(int turn, int col){
        int coord = 0;
        switch(turn){
            case 1: //RED
                if(red_checkers < 21 && column_checkers[col-1] < 6){
                    red_coords[red_checkers] = 35+col-7*column_checkers[col-1];
                    column_checkers[col-1]++;
                    red_checkers++;}
                invalidate();
                coord = red_coords[red_checkers-1];
                break;
            case 2: //BLK
                if(black_checkers < 21 && column_checkers[col-1] < 6){
                    blk_coords[black_checkers] = 35+col-7*column_checkers[col-1];
                    column_checkers[col-1]++;
                    black_checkers++;}
                invalidate();
                coord = blk_coords[black_checkers-1];
                break;
        }
        return coord;
    }

    public void resetGame(){
        for(int i = 0; i < 21; i++){
            red_coords[i] = 0; red_checkers = 0;
            blk_coords[i] = 0; black_checkers = 0;
        }
        for(int i = 0; i < 7; i++){
            column_checkers[i] = 0;
        }
        invalidate();
    }
}
