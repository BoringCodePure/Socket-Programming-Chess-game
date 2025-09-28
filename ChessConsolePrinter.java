import java.util.ArrayList;

class ChessConsolePrinter {

    private static String getSymbol(Piece piece){

        if (piece instanceof Pawn){
            return " p ";
        }
        if (piece instanceof King){
            return " k ";
        }
        if (piece instanceof Queen){
            return " Q ";
        }
        if (piece instanceof Knight){
            return " N ";
        }
        if (piece instanceof Rook){
            return " R ";
        }
        if (piece instanceof Bishop){
            return " B ";
        }
        return "   ";
    }

    public static String printBoard(Tile[][] board){
        ArrayList<Piece> DebugWhite = new ArrayList<>(){
            public String toString(){
                String start = "[";
                for (Piece eachPiece : this){
                    start = start + getSymbol(eachPiece) + ", ";
                }
                start = start + " ]";
                return start;
            }
        };
        ArrayList<Piece> DebugBlack = new ArrayList<>(){
            public String toString(){
                StringBuilder start = new StringBuilder("[");
                for (Piece eachPiece : this){
                    start.append(getSymbol(eachPiece)).append(", ");
                }
                start.append(" ]");
                return start.toString();
            }
        };
        for (Piece eachPiece : Main.WhitePieces){
            DebugWhite.add(eachPiece);
        }
        for (Piece eachPiece : Main.BlackPieces){
            DebugBlack.add(eachPiece);
        }

//        System.out.println("WHITE PIECE IS " + DebugWhite);
//        System.out.println("BLACK PIECE IS " + DebugBlack);
        String boardString = "";

        for (Tile[] subTile : board){
            boardString = boardString + "[";
            for (Tile eachTile : subTile){
                boardString = boardString + getSymbol(eachTile.getPiece());
            }
            boardString = boardString + "]\n" ;
        }
        return boardString;
    }
}