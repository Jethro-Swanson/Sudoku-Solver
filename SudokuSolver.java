/**
 * SudokuSolver
 * 
 * @author   Jethro Swanson
 * @version  May 26 / 2021
 * 
 * PURPOSE: Creates a representation of a sudoku board from a user input (taken in line by line) and then attempts to recursively solve that 
 * board if it is of a valid size
 */

import java.util.Scanner;

public class SudokuSolver{

  public static void main (String[] args){
    System.out.println("Please enter a game board as a consecutive list of values, using - to represent empty cells and \nspaces bewteen "
                         + "each cell value:");
    
    Scanner userInput = new Scanner(System.in);
    String inputBoard = userInput.nextLine();
   
    
    Sudoku game = new Sudoku(inputBoard);
    
    System.out.println("The original board is:\n" + game);
    
    boolean wasSolved = game.solve();//tracks if the board was solved succesfully 
    
    if(wasSolved){
      System.out.println("The solution is:\n" + game);   
    }
    else{
      System.out.println("The Input board was unsolvable:\n" + game);
    }
    
    System.out.println("Program completed successfully");
  }
  
  
}

/*
 * represends a sudoku board and can take user generated sukoku puzzles and recursibly solve them via its methods
 */
class Sudoku{
  private int boardSize;//holds the board size as a single int, total board size will be this int squared
  private int[][] board;//holds the board once it is properly proccessed from input in 2d array
  
  private static final int DEFAULT_SIZE = 9;//size of board if incorrect board is input as value
  private static final int DEFAULT_EMPTY_CELLS = 81;//
  private static final int MIN_SIZE = 2;//represents the smallest value of boardSize allowable  
  private static final String BLANK = "-";//represents an empty cell in the input board state
  private static final int EMPTY = -1; //represents an empty cell in the array sodukuBoard
  private static final int CURRENT_NUM_START = 1;//the value that the solver always starts at when attempting values
  
  //constructor
  public Sudoku(String inputBoard){
    Scanner boardReader = new Scanner(inputBoard);
    
    int[] sodukuBoard = new int[MIN_SIZE * MIN_SIZE];//holds a 1d representation of the board retrieved from input string
    int boardCells = 0; //counts the number of cells extracted from the input board
    
    while(boardReader.hasNext()){
      
      //doubles the board array size whenever if fills up
      if(boardCells == sodukuBoard.length){
        int[] newSodukuBoard = new int[sodukuBoard.length*2];
        System.arraycopy(sodukuBoard, 0, newSodukuBoard, 0, boardCells);
        sodukuBoard = newSodukuBoard;               
      }
      
      //takes the next value in the list and puts it into the soduku board
      String nextElement = boardReader.next();
      if(nextElement.compareTo(BLANK) == 0){
        sodukuBoard[boardCells] = EMPTY;
        
      }
      else{
        sodukuBoard[boardCells] = Integer.parseInt(nextElement);
      }
      
      boardCells++;
    }
    
    //checks to see if the board is of valid size and either uses it if it is or creates a default board if it is not
    //this requires that the number of cells squares to a whole number, and that the resulting number also squartes to 
    //a whole number, hence n^2 by n^2 size. also ensures the board size is always greater than 1
    if((Math.sqrt(boardCells)%((int)Math.sqrt(boardCells)) == 0) && (Math.sqrt(boardCells) > 1) 
         && ((Math.sqrt(Math.sqrt(boardCells))) % (int)(Math.sqrt(Math.sqrt(boardCells))))==0){ 
      boardSize = (int) Math.sqrt(boardCells);
      board = new int[boardSize][boardSize];
      
      //loops through all spaces on the 2d array representing the board and takes the corrosponding element of the 1d
      //array and insets it at its corrosponding location
      for(int i=0; i<boardSize; i++){
        for(int k=0; k<boardSize; k++){ 
          board[i][k] = sodukuBoard[i*boardSize + k];
        }
      }
    }
    else{
      boardSize = DEFAULT_SIZE;
      board = new int[DEFAULT_SIZE][DEFAULT_SIZE];
      for(int i=0; i<boardSize; i++){
        for(int k=0; k<boardSize; k++){
          board[i][k] = EMPTY;
        }
      }
    }
  }
 
  //used to allow user to call solve without any parameters, solves the current board by callig true solver method
  //retuns boolean of whether the board has been properly solved or not (true for solved, false for not)
  public boolean solve(){
    return solve(CURRENT_NUM_START);
  }
  
  //Solves the current sudoku board recursivly cell by cell
  //accepts an int representing the number currehntly being attempted to be placed in the next empty cell
  //returns a boolean reprecenting if the board has been succesfully solved
  public boolean solve(int currentNum){ 
    while(true){
      int[] emptyIndex= linierSearchForEmpty();//holds the x and y values of the next empty cell
      int emptyIndexX = emptyIndex[0];//x coords of next empty cell
      int emptyIndexY = emptyIndex[1];//y coords of next empty cell
      boolean lastCell = false;//tracks if this is the final cell that must be filled
      
      //handles the case when the last cell has been filled (though not nessesarily correctly)
      if(emptyIndexX == EMPTY || emptyIndexY == EMPTY){
        emptyIndexX = boardSize -1;
        emptyIndexY = boardSize -1;
        lastCell = true;
      }
      
      //tests all possible values in the current empty location to find one that fits or all options are exahausted
      board[emptyIndexX][emptyIndexY] = currentNum;
      while(currentNum<boardSize
             &&(!checkRow(emptyIndexY, currentNum) || !checkCol(emptyIndexX, currentNum) 
                  || !checkBlock(emptyIndexX, emptyIndexY))){
        currentNum++; 
        board[emptyIndexX][emptyIndexY] = currentNum;        
      }
      
      //tests if the selected current num actually works in this location
      if((checkRow(emptyIndexY, currentNum) && checkCol(emptyIndexX, currentNum) 
            && checkBlock(emptyIndexX, emptyIndexY)) && (currentNum <= boardSize)){
        
        //stops the recusroin if the last correct value has been found, otherwise recursivly checks the next value 
        //untill a contradiction is found or all values are correctly placed
        if(lastCell){
          return true;
        }
        else if(!solve(CURRENT_NUM_START)){
          board[emptyIndexX][emptyIndexY] = EMPTY;
          if(++currentNum >boardSize){
            return false;
          }   
        }
        else{
          return true;
        }
      }
      else{//case where there is no possible value for the empty space
        board[emptyIndexX][emptyIndexY] = EMPTY;
        lastCell = false;
        return false;
      }
    }     
}
  
  
  //searches through the board location untill an empty cell is reached or all cells have been searched
  //returns an int array containing two values representing the x and y location of the empty cell or the EMPTY value 
  //-1 to represent there being no cells that are empty ie the board is complete
  private int[] linierSearchForEmpty(){
    int[] emptyLocation = new int[2];//records the x and y position of the empty cell
    boolean emptyFound = false;//tells if an empty cell has been found to stop the search proccess
    
    //searches through the array untill an empty space has been found or no empty spaces remain
    int i=0;
    while(i<boardSize && !emptyFound){
      int j=0;
      while(j<boardSize && !emptyFound){
        if(board[i][j] == EMPTY){
          emptyFound = true;
          emptyLocation[0] = i;
          emptyLocation[1] = j;
        }
        j++;
      }
      i++;
    }
    
    //handles case where no more empty spaces are found
    if(!emptyFound){
      emptyLocation[0] = EMPTY;
      emptyLocation[1] = EMPTY;
    }
    
    return emptyLocation;
  }
  
  
  //checks a specified row on the board to see if a specified value appears multiple times
  //Accepts an int representing the row and an int representing the specified value
  //returns a boolean of true if there are no repeats or false if there are 
  private boolean checkRow(int emptyIndexY, int currentNum){
    boolean isCorrect = true; //tracks if the row is currently correct, ie has no duplicates
    
    //tracks the number if currentNum has already been enounterd, as it is the only value possible for duplication
    boolean currentNumFound = false;
    
    for(int i=0; i<boardSize; i++){
      if(board[i][emptyIndexY] == currentNum){
        if(currentNumFound){
          isCorrect = false;
        }
        else{
          currentNumFound = true;
        }
      }
    }
    
    return isCorrect;
  }
  
  
  //Checks if a specific columb on the board has a specified value repeating multiple times
  //Accepts an int representing the columg and an int representing the value that could be doublicated
  //returns a boolean of true if there are no duplicates or false if the value repeats itself
  private boolean checkCol(int emptyIndexX, int currentNum){
    boolean isCorrect = true; //tracks if the row is currently correct, ie has no duplicates
    
    //tracks the number if currentNum has already been enounterd, as it is the only value possible for duplication
    boolean currentNumFound = false;
    
    for(int i=0; i<boardSize; i++){
      if(board[emptyIndexX][i] == currentNum){
        if(currentNumFound){
          isCorrect = false;
        }
        else{
          currentNumFound = true;
        }
      }
    }
    
    return isCorrect;
  }
  
  //Checks a specific block of the board to see if there are any repeated values in it
  //Accepts 2 ints representing the x and y indicies of the newly inseted value
  //retuns a boolean of true if no duplicates were found or false if some were found.
  private boolean checkBlock(int emptyBlockX , int emptyBlockY){
    boolean isCorrect = true;//tracks if a duplicate had been found, false if it has otherwise ture   
    int blockSize = (int)Math.sqrt(boardSize);//finds the size of a blocks size, full size is this value squared
    int blockX = emptyBlockX/blockSize; //gets the x position of the block
    int blockY = emptyBlockY/blockSize; //gets the y position of the block
    
    //tracks if a value has been found in the block yet, when found the correspoding index (found value -1) is 
    //changed to true
    boolean[] valuesInBlock = new boolean[boardSize];
    

    //goes to the specific block that must be tested and itereates through
    for(int i=blockSize*blockX; i<blockSize*(blockX +1); i++){
      for(int j=blockSize*blockY; j<blockSize*(blockY+1); j++){
        if(!(board[i][j] == EMPTY)){
          if(valuesInBlock[(board[i][j] -1)] == true){
            isCorrect = false;
          }
          else{
            valuesInBlock[(board[i][j] -1)] = true;
          }
        }
      }
    }
    
    return isCorrect;
  }
                             
                             

  //creates a string representation of the soduku board in 2d.
  //retruns a String representing the board in the form 3 - 2 3 where empty spaces are "-" and there is a space beteen
  //each element
  public String toString(){
    String returnString = "";
    for(int i = 0; i<boardSize; i++){
      for(int k=0; k<boardSize; k++){       
        if(board[i][k] == EMPTY){
          returnString += BLANK;  
        }
        else{
          returnString += board[i][k];
        }
        returnString += " ";
      }
      returnString += "\n";
    }
    
    return returnString;
  } 
}
