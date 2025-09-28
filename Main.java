import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;


class GameState implements Serializable{
    private int CurrentRound;
    private ArrayList<Piece> WhitePieces = new ArrayList<>();
    private ArrayList<Piece> BlackPieces = new ArrayList<>();
    private Piece WhiteKing;
    private Piece BlackKing;
    @Serial
    private static final long serialVersionUID = 1L;


    public GameState() {
        CurrentRound = -1;

        // Pawns
        for (int row = 0; row <= 7; row++) {
            for (int col = 0; col <= 7; col++) {
                if (row == 1) {
                    BlackPieces.add(new Pawn(row, col, "myPicture/BPawn.png", 1));
                } else if (row == 6) {
                    WhitePieces.add(new Pawn(row, col, "myPicture/WPawn.png", -1));
                }
            }
        }

        // Back ranks
        setupBackRank(0, 1, "B", BlackPieces);
        setupBackRank(7, -1, "W", WhitePieces);
    }

    /**
     * Helper method to set up the back rank pieces using switch-case.
     */
    private void setupBackRank(int row, int direction, String prefix, ArrayList<Piece> targetList) {
        for (int col = 0; col <= 7; col++) {
            Piece piece = null;
            switch (col) {
                case 0, 7 -> piece = new Rook(row, col, "myPicture/" + prefix + "Rook.png", direction);
                case 1, 6 -> piece = new Knight(row, col, "myPicture/" + prefix + "Knight.png", direction);
                case 2, 5 -> piece = new Bishop(row, col, "myPicture/" + prefix + "Bishop.png", direction);
                case 3 -> piece = new Queen(row, col, "myPicture/" + prefix + "Queen.png", direction);
                case 4 -> piece = new King(row, col, "myPicture/" + prefix + "King.png", direction);
            }

            if (piece != null) {
                targetList.add(piece);
                // Store kings separately
                if (piece instanceof King) {
                    if (prefix.equals("W")) {
                        WhiteKing = piece;
                    } else {
                        BlackKing = piece;
                    }
                }
            }
        }
    }

    public ArrayList<Piece> getWhitePieces(){
        return WhitePieces;
    }
    public ArrayList<Piece> getBlackPieces(){
        return BlackPieces;
    }

    public int getCurrentRound(){
        return CurrentRound;
    }
    public Piece getWhiteKing(){
        return WhiteKing;

    }
    public Piece getBlackKing(){
        return BlackKing;
    }

    public void setCurrentRound(int currentRound) {
        this.CurrentRound = currentRound;
    }

}

class Mouse implements Serializable{
    public static Piece PlayerPiece;
    public static Tile TargetTile;

}

class Tile extends JButton implements ComponentListener, MouseListener{
    private final int row;
    private final int column;
    private Piece piece;
    private Color backGroundColor = null;

    public Tile(int row, int column){
        this.row = row;
        this.column = column;

        if (row % 2 == column % 2){
            backGroundColor = new Color(234, 221, 202);

        } else{
            backGroundColor = new Color(92, 64, 51);
        }

        this.setBackground(backGroundColor);
        this.addComponentListener(this);
        this.addMouseListener(this);

    }

    public void setPiece(Piece IncomingPiece){
        if (IncomingPiece == null){
            piece = null;
            this.setIcon(null);
        } else{
            piece = IncomingPiece;
            this.setIcon(new ImageIcon(IncomingPiece.getImage()));

        }
    }

    public Color getOriginalBackground(){
        return this.backGroundColor;
    }

    public int row(){
        return row;
    }
    public int column(){
        return column;
    }

    public Piece getPiece() {
        return piece;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (piece != null){
            int width = this.getWidth();
            int height = this.getHeight();

            BufferedImage blankImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = blankImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(piece.getImage(),0, 0, width, height, null);
            this.setIcon(new ImageIcon(blankImage));
            g2d.dispose();
        }
    }
    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }
    @Override
    public void componentHidden(ComponentEvent e) {
    }
    @Override
    public void mouseClicked(MouseEvent e) {

        if (!Main.underPromotion){
            if (piece != null){
                Mouse.PlayerPiece = piece;
            }
        }
    }
    @Override
    public void mousePressed(MouseEvent e) {
        if (!Main.underPromotion){
            if (piece != null){
                Mouse.PlayerPiece = piece;
                for (Tile eachTile : Mouse.PlayerPiece.getAttackRadius()){
                    eachTile.setBackground(new Color(200, 0, 0));
                }
            } else{
                Mouse.PlayerPiece = null;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!Main.underPromotion){
            if (Mouse.TargetTile == this){
                Mouse.TargetTile.mouseClicked(null);
                Main.Repaint();
                return;
            }
            if (Mouse.PlayerPiece != null && Mouse.TargetTile != null){

                // move piece
                if (Mouse.PlayerPiece.CanBeMovedTo(Mouse.TargetTile.row(), Mouse.TargetTile.column())){
                    Main.AllowMove(this, Mouse.TargetTile);
                    Mouse.PlayerPiece = null;
                    //   System.out.println("MOve");

                }
            }
            Main.Repaint();
            Mouse.PlayerPiece = null;
        }

    }

    @Override
    public void mouseEntered(MouseEvent e) {

        if (!Main.underPromotion){
            Mouse.TargetTile = this;
        } else{
            Mouse.TargetTile = null;
        }

    }

    @Override
    public void mouseExited(MouseEvent e) {
        Mouse.TargetTile = null;

    }
}

public class Main implements Serializable {
    private static GameState gameState = new GameState();
    public static boolean underPromotion;
    public static ArrayList<Piece> WhitePieces = gameState.getWhitePieces();
    public static ArrayList<Piece> BlackPieces = gameState.getBlackPieces();
    public static Piece WhiteKing = gameState.getWhiteKing();
    public static Piece BlackKing = gameState.getBlackKing();
    private static final int ROWS = 8;
    private static final int COLS = 8;
    public static Tile[][] board = new Tile[ROWS][COLS];

    public static void main(String[] args) throws IOException {

        // Always start Swing on the Event Dispatch Thread
        SwingUtilities.invokeLater(Main::createAndShowUI);

    }
    private static void createAndShowUI() {


        JFrame frame = new JFrame("6x6 Button Grid");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel MainPanel = new JPanel(new BorderLayout());
        JPanel gridPanel = new JPanel(new GridLayout(ROWS, COLS, 0, 0));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        MainPanel.add(gridPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        MainPanel.add(buttonPanel, BorderLayout.WEST);
        @SuppressWarnings("unused")
        String[] textArray = {"Save Game", "Reset Game", "Open Game"};
        buttonPanel.add(Box.createVerticalGlue());
        JButton button = new JButton("Restart Game");
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                emptyBoard();
                restartGame(new GameState());
            }
        });
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(150, 50)); // width x height
        button.setMaximumSize(new Dimension(150, 50)); // prevent resizing
        button.setBackground(new Color(113, 183, 255));
        button.setFocusPainted(false);
        buttonPanel.add(button);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15))); // space between buttons



        JButton SaveGame = new JButton("Save Game");
        SaveGame.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                if (!underPromotion){
                    JFileChooser fileChooser = new JFileChooser();
                    int state = fileChooser.showSaveDialog(null);

                    if (state == JFileChooser.APPROVE_OPTION) {
                        String selectedFile = fileChooser.getSelectedFile().getAbsolutePath();

                        try (FileOutputStream fileStream = new FileOutputStream(selectedFile + ".ser");
                             ObjectOutputStream outputStream = new ObjectOutputStream(fileStream)) {
                            //    System.out.println("Saving game state to " + selectedFile + ".ser");

                            outputStream.writeObject(gameState);

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        SaveGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        SaveGame.setPreferredSize(new Dimension(150, 50));
        SaveGame.setMaximumSize(new Dimension(150, 50));
        SaveGame.setBackground(new Color(113, 183, 255));
        SaveGame.setFocusPainted(false);
        buttonPanel.add(SaveGame);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15))); // space between buttons

        JButton OpenGame = new JButton("Open Game");
        OpenGame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!underPromotion){
                    JFileChooser fileChooser = new JFileChooser();
                    int state = fileChooser.showOpenDialog(null);

                    if (state == JFileChooser.APPROVE_OPTION) {
                        String selectedFile = fileChooser.getSelectedFile().getAbsolutePath();

                        FileInputStream fileStream = null;
                        try {
                            fileStream = new FileInputStream(selectedFile);
                        } catch (FileNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }
                        try {
                            ObjectInputStream outputStream = new ObjectInputStream(fileStream);
                            GameState data = (GameState) outputStream.readObject();
                            emptyBoard();
                            restartGame(data);
                        } catch (IOException | ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        });
        OpenGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        OpenGame.setPreferredSize(new Dimension(150, 50)); // width x height
        OpenGame.setMaximumSize(new Dimension(150, 50)); // prevent resizing
        OpenGame.setBackground(new Color(113, 183, 255));
        OpenGame.setFocusPainted(false);
        buttonPanel.add(OpenGame);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15))); // space between buttons

        buttonPanel.add(Box.createVerticalGlue());

        startGame(gridPanel);

        frame.add(MainPanel);
        frame.pack();
        frame.setSize(1100, 1000);// size to fit contents
        frame.setLocationRelativeTo(null); // center on screen
        frame.setVisible(true);
    }

    private static void startGame(JPanel gridPanel){
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Tile btn = new Tile(r, c);
                board[r][c] = btn;
                gridPanel.add(btn);
            }
        }


        for (Piece eachPiece : WhitePieces){
            board[eachPiece.row()][eachPiece.column()].setPiece(eachPiece);
        }
        for (Piece eachPiece : BlackPieces){
            board[eachPiece.row()][eachPiece.column()].setPiece(eachPiece);
        }

    }

    private static void restartGame(GameState new_gameState){
        gameState = new_gameState;
        WhitePieces = gameState.getWhitePieces();
        BlackPieces = gameState.getBlackPieces();
        WhiteKing = gameState.getWhiteKing();
        BlackKing  = gameState.getBlackKing();


        for (Piece eachPiece : WhitePieces){
            board[eachPiece.row()][eachPiece.column()].setPiece(eachPiece);
        }
        for (Piece eachPiece : BlackPieces){
            board[eachPiece.row()][eachPiece.column()].setPiece(eachPiece);
        }

        Repaint();
    }


    @SuppressWarnings("unused")
    private static void rotateBoard(JPanel gridpanel, int currentTeam){

        // if currentTeam is white rotate to white face;
        if (currentTeam == -1){
            for (int i = 0 ; i < ROWS; i++){
                for (int j = 0; j < COLS; j++){
                    gridpanel.add(board[i][j]);
                }
            }
        } else{
            for (int i = ROWS - 1; i >= 0; i--){
                for (int j = 0; j < COLS; j++){
                    gridpanel.add(board[i][j]);
                }
            }
        }
    }

    public static ArrayList<Piece> getCheckCount(int kingColor){
        Tile KingTile;
        ArrayList<Piece> EnemyPiece;
        ArrayList<Piece> attackerList = new ArrayList<>();

        if (kingColor == -1){
            KingTile = board[WhiteKing.row][WhiteKing.column];

            EnemyPiece = BlackPieces;
        } else{
            KingTile = board[BlackKing.row][BlackKing.column];

            EnemyPiece = WhitePieces;
        }

        for (Piece eachPiece : EnemyPiece){
            if (eachPiece.getAttackRadius().contains(KingTile)){
                attackerList.add(eachPiece);
            }
        }

        return attackerList;

    }
    public static boolean isStaletMate(King king){
        ArrayList<Piece> Pieces;
        if (king.color == -1){
            Pieces = WhitePieces;
        } else{
            Pieces = BlackPieces;
        }

        for (Tile eachTile : king.getAttackRadius()){
            if (king.CanBeMovedTo(eachTile.row(), eachTile.column())){
                return false;
            }
        }
        // king has no safe square.
        // Is there any piece that can move in its attack radius?

        for (Piece eachPiece : Pieces){
            for (Tile eachTile : eachPiece.getAttackRadius()){
                if (eachPiece.CanBeMovedTo(eachTile.row(), eachTile.column())){
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isCheckMate(King king){

        ArrayList<Piece> myPiece;

        if (king.color == -1){
            myPiece = WhitePieces;

        } else{
            myPiece = BlackPieces;
        }

        // get the check count first
        ArrayList<Piece> Attacker = getCheckCount(king.color);

        int checkCount = Attacker.size();

        if (checkCount == 0){
            return false;
        }


        //  System.out.println("CHECK COUNT = " + checkCount);

        //is there any safe square for king?
        boolean SafeSquare = false;
        for (Tile eachTile : king.getAttackRadius()){
            if (king.CanBeMovedTo(eachTile.row(), eachTile.column())){
                SafeSquare = true;
            }
        }

        // double check
        if (checkCount > 1) {
            //is there any safe square for king?
            return !SafeSquare;
        }
        // single check;
        if (SafeSquare){
            return false;
        }
        Piece attack = Attacker.getFirst();
        if (attack instanceof Rook || attack instanceof Queen || attack instanceof Bishop){
            for (Piece eachPiece : myPiece){
                for (Tile eachTile : eachPiece.getAttackRadius()){
                    if (getCheckSight(attack, king).contains(eachTile) && eachPiece.CanBeMovedTo(eachTile.row(), eachTile.column())){
                        return false;
                    }
                }
            }
        }
        // given that king has no safe square and the piece that is checking is knight or pawn, then the only way is to capture the attacking pieces
        if (attack instanceof Knight || attack instanceof Pawn){
            for (Piece eachPiece : myPiece){
                if (eachPiece.CanBeMovedTo(attack.row(), attack.column())){
                    return false;
                }
            }
        }

        return true;
    }

    private static ArrayList<Tile> getCheckSight(Piece attacker, King king){

        ArrayList<Tile> path = new ArrayList<>();

        int dr = 0;
        int dc = 0;

        if (king.row() > attacker.row()){
            dr = 1;
        } else{
            dr = -1;
        }

        if (king.column() > attacker.column()){
            dc = 1;
        } else{
            dc = -1;
        }

        if (king.row() == attacker.row()){
            dr = 0;
        }

        if (king.column() == attacker.column()){
            dc = 0;
        }

        path.addAll(CheckSightHelper(attacker, king, dr,dc));
        return path;
    }

    private static ArrayList<Tile> CheckSightHelper(Piece attacker, Piece king, int dr, int dc){


        ArrayList<Tile> path = new ArrayList<>();
        path.add(board[attacker.row()][attacker.column()]);

        int row = attacker.row() + dr;
        int column = attacker.column() + dc;


        while (row <= 7 && row >= 0 && column <= 7 && column >= 0){
            if (board[row][column].getPiece() == king){
                return path;
            } else if (board[row][column].getPiece() == null){
                path.add(board[row][column]);
            }
            row = row + dr;
            column = column + dc;
        }
        return null;
    }

    public static HashMap<Tile, Integer> getTileUnderAttack(int EnemyColor){
        HashMap<Tile, Integer> AttackerPerTile = new HashMap<>();

        ArrayList<Piece> EnemyPiece;
        // white
        if (EnemyColor == -1){
            EnemyPiece = WhitePieces;
        } else{
            EnemyPiece = BlackPieces;
        }


        for (Piece eachPiece : EnemyPiece){

            ArrayList<Tile> TileUnderAttacked = eachPiece.getAttackRadius();

            for (Tile tile : TileUnderAttacked){
                if (AttackerPerTile.containsKey(tile)){
                    int count = AttackerPerTile.get(tile);
                    AttackerPerTile.put(tile, count + 1);
                } else{
                    AttackerPerTile.put(tile, 1);
                }
            }
        }
        return AttackerPerTile;
    }

    public static void AllowMove(Tile from, Tile to){
        // check for capture
        if (to.getPiece() != null){
            if (to.getPiece().color == -1){
                WhitePieces.remove(to.getPiece());
            } else{
                BlackPieces.remove(to.getPiece());
            }
        }
        from.getPiece().updatePosition(to.row(), to.column());
        to.setPiece(from.getPiece());
        from.setPiece(null);

        nextRound();
        Repaint();
    }
    public static void summonPiece(Piece newPiece){
        int row = newPiece.row();
        int column = newPiece.column();

        board[row][column].setPiece((newPiece));

    }

    public static void Repaint(){
        for (Tile[] TileArray : board){
            for (Tile eachTile : TileArray){
                if (eachTile.getPiece() != null){
                    eachTile.componentResized(null);
                    eachTile.setText(null);
                }
                eachTile.setText(null);
                eachTile.setBackground(eachTile.getOriginalBackground());
                eachTile.componentResized(null);
            }
        }
    }

    public static boolean pseudoLegalMove(Tile tileFrom, Tile tileTo) {

        //    System.out.println(gameState.getCurrentRound());
        if (gameState.getCurrentRound() != tileFrom.getPiece().color){
            return false;
        }

        int FromRow = tileFrom.row();
        int FromColumn = tileFrom.column();
        int ToRow = tileTo.row();
        int ToColumn = tileTo.column();
        Piece movingPiece = tileFrom.getPiece();
        Piece CapturePiece = tileTo.getPiece();
        Tile kingTile;
        ArrayList<Piece> EnemyPiece;
        // if capture Piece is not null

        tileTo.setPiece(movingPiece);
        tileFrom.setPiece(null);
        movingPiece.row = ToRow;
        movingPiece.column = ToColumn;
        if (movingPiece.color == -1){
            kingTile = board[WhiteKing.row][WhiteKing.column];
            EnemyPiece = BlackPieces;
        } else{
            kingTile = board[BlackKing.row][BlackKing.column];
            EnemyPiece = WhitePieces;
        }

        if (CapturePiece != null){
            EnemyPiece.remove(CapturePiece);
        }
        boolean legal = true;
        for (Piece eachPiece : EnemyPiece){
            if (eachPiece.getAttackRadius().contains(kingTile)){
                legal = false;
                break;
            }
        }
        // undo everything
        tileTo.setPiece(CapturePiece);
        if (CapturePiece != null){
            EnemyPiece.add(CapturePiece);
        }
        tileFrom.setPiece(movingPiece);
        movingPiece.row = FromRow;
        movingPiece.column = FromColumn;


        Repaint();
        return legal;


    }

    public static void nextRound(){
        if (gameState.getCurrentRound() == -1){
            gameState.setCurrentRound(1);
        } else{
            gameState.setCurrentRound(-1);
        }

        Piece king;
        if (gameState.getCurrentRound() == -1){
            king = WhiteKing;
        } else{
            king = BlackKing;
        }

        if (isCheckMate((King) king)){
            System.out.println("CHECKMATE");
        } else{
            if (isStaletMate((King) king)){
                System.out.println("StaleMate");
            } else{
                System.out.println("NOT CHECKMATE");
            }
        }

        //   System.out.println(ChessConsolePrinter.printBoard(Main.board));

    }
    public static void emptyBoard(){
        WhitePieces = null;
        BlackPieces = null;
        for (Tile[] eachRow : board){
            for (Tile eachTile : eachRow){
                eachTile.setPiece(null);
            }
        }
    }
}

abstract class Piece implements Serializable {
    transient BufferedImage  PieceImage;
    protected int row;
    protected int column;
    protected int color;
    private final String imagePath;


    public Piece(int row, int column, String imagePath, int color){
        try{
            InputStream input = getClass().getClassLoader().getResourceAsStream(imagePath);
            PieceImage = ImageIO.read(input);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
        this.row = row;
        this.column = column;
        this.color = color;
        this.imagePath = imagePath;
    }

    public void updatePosition(int newRow, int newColumn){
        this.row = newRow;
        this.column = newColumn;
    }

    public int row(){
        return row;
    }
    public int column(){
        return column;
    }
    public BufferedImage getImage(){

        if (PieceImage == null){
            try{
                InputStream input = getClass().getClassLoader().getResourceAsStream(imagePath);
                PieceImage = ImageIO.read(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return PieceImage;
    }
    public abstract ArrayList<Tile> getAttackRadius();

    public ArrayList<Tile> helperRadius(int dr, int dc){
        ArrayList<Tile> PossibleTile = new ArrayList<>();
        int row = this.row() + dr;
        int column = this.column() + dc;

        while (row <= 7 && row >= 0 && column  <= 7 && column  >= 0) {
            if (Main.board[row][column].getPiece() == null){
                PossibleTile.add(Main.board[row ][column ]);
            } else{
                PossibleTile.add(Main.board[row ][column] );
                break;
            }

            row = row + dr;
            column = column + dc;
        }
        return PossibleTile;
    }

    public boolean CanBeMovedTo(int row, int column){
        Tile tileTo = Main.board[row][column];

        if (!getAttackRadius().contains(tileTo)){
            return false;
        }

        if (tileTo.getPiece() != null && tileTo.getPiece().color == this.color){
            return false;
        }

        return Main.pseudoLegalMove(Main.board[this.row()][this.column()], Main.board[row][column]);
    }
}


class King extends Piece{
    public King(int row, int column, String imagePath, int color){
        super(row, column, imagePath, color);
    }

    @Override
    public ArrayList<Tile> getAttackRadius() {
        ArrayList<Tile> PossibleTile = new ArrayList<>();

        int[] offset = {-1, 0, 1};

        for (int dy : offset){
            for (int dx : offset){
                if (!(dy == 0 && dx == 0)) {
                    if (row() + dy <= 7 && row() + dy >= 0 && column() + dx <= 7 && column() + dx >= 0){
                        PossibleTile.add(Main.board[row() + dy][column() + dx]);
                    }
                }
            }
        }
        return PossibleTile;
    }
}

class Rook extends Piece{
    public Rook(int row, int column, String imagePath, int color){
        super(row, column, imagePath, color);
    }

    public ArrayList<Tile> getAttackRadius(){
        ArrayList<Tile> PossibleTile = new ArrayList<>();
        PossibleTile.addAll(helperRadius(1,0));
        PossibleTile.addAll(helperRadius(-1, 0));
        PossibleTile.addAll(helperRadius(0, 1));
        PossibleTile.addAll(helperRadius(0, -1));

        return PossibleTile;
    }

}

class Bishop extends Piece{

    public Bishop(int row, int column, String imagePath, int color) {
        super(row, column, imagePath, color);
    }
    @Override
    public ArrayList<Tile> getAttackRadius() {
        ArrayList<Tile> PossibleTile = new ArrayList<>();

        PossibleTile.addAll(helperRadius(1, 1));
        PossibleTile.addAll(helperRadius(1, -1));
        PossibleTile.addAll(helperRadius(-1, 1));
        PossibleTile.addAll(helperRadius(-1, -1));

        return PossibleTile;
    }
}

class Knight extends Piece{

    public Knight(int row, int column, String imagePath, int color) {
        super(row, column, imagePath, color);
    }

    @Override
    public ArrayList<Tile> getAttackRadius() {
        ArrayList<Tile> PossibleTile = new ArrayList<>();

        int[] diff = {-2, -1, 1, 2};

        for (int dy : diff){
            for (int dx : diff){
                int absX = Math.abs(dx);
                int absY = Math.abs(dy);
                if (absX != absY && row() + dy <= 7 && row() + dy >= 0 && column() + dx <= 7 && column() + dx >= 0){
                    PossibleTile.add(Main.board[row() + dy][column() + dx]);
                }
            }
        }
        return PossibleTile;
    }

}

class Queen extends Piece{

    public Queen(int row, int column, String imagePath, int color) {
        super(row, column, imagePath, color);
    }

    @Override
    public ArrayList<Tile> getAttackRadius() {
        ArrayList<Tile> PossibleTile = new ArrayList<>();
        PossibleTile.addAll(helperRadius(1, 1));
        PossibleTile.addAll(helperRadius(1, -1));
        PossibleTile.addAll(helperRadius(-1, 1));
        PossibleTile.addAll(helperRadius(-1, -1));
        PossibleTile.addAll(helperRadius(1,0));
        PossibleTile.addAll(helperRadius(-1, 0));
        PossibleTile.addAll(helperRadius(0, 1));
        PossibleTile.addAll(helperRadius(0, -1));

        return PossibleTile;
    }
}

class Pawn extends Piece{

    private boolean doubleMove = false;

    public Pawn(int row, int column, String imagePath, int color) {
        super(row, column, imagePath, color);
    }
    public void updatePosition(int newRow, int newColumn){

        if (Math.abs(this.row - newRow) >= 1){
            doubleMove = true;
        }

        super.updatePosition(newRow, newColumn);

        // check for a promotion option;
        if (color == -1){
            if (row == 0) {
                displayPromotionScreen();
            }
        } else{
            if (row == 7){
                displayPromotionScreen();
            }
        }

    }

    private void displayPromotionScreen(){
        Main.underPromotion = true;
        int row = this.row();
        int column = this.column();

        Piece Queen;
        Piece Rook;
        Piece Knight;
        Piece Bishop;

        ArrayList<Piece> PieceSet;
        if (this.color == -1){
            Queen = new Queen(row, column, "myPicture/WQueen.png", -1);
            Rook = new Rook(row, column, "myPicture/WRook.png", - 1);
            Knight = new Knight(row, column, "myPicture/WKnight.png", -1);
            Bishop = new Bishop(row, column, "myPicture/WBishop.png", -1);

            PieceSet = Main.WhitePieces;
        } else{
            Rook = new Rook(row, column, "myPicture/BRook.png", 1);
            Knight = new Knight(row, column, "myPicture/BKnight.png", 1);
            Bishop = new Bishop(row, column, "myPicture/BBishop.png", 1);
            Queen = new Queen(row, column, "myPicture/BQueen.png", 1);
            PieceSet = Main.BlackPieces;
        }
        PieceSet.remove(this);

        Piece[] option = {Queen, Rook, Bishop, Knight};

        JFrame newFrame = new JFrame("Choose Piece");

        JPanel gridPanel = new JPanel(new GridLayout(1, 4, 0, 0));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        for (Piece eachPiece : option){
            JButton PieceOption = new JButton();
            PieceOption.setIcon(new ImageIcon(eachPiece.getImage()));
            PieceOption.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Main.summonPiece(eachPiece);

                    PieceSet.add(eachPiece);
                    newFrame.dispatchEvent(new WindowEvent(newFrame, WindowEvent.WINDOW_CLOSING));
                    Main.underPromotion = false;
                }
            });
            gridPanel.add(PieceOption);
        }
        newFrame.setContentPane(gridPanel);
        newFrame.pack();
        newFrame.setSize(900, 300);
        newFrame.setLocationRelativeTo(null);
        newFrame.setVisible(true);
    }

    @Override
    public ArrayList<Tile> getAttackRadius() {
        ArrayList<Tile> PossiblePath = new ArrayList<>();
        int dr = 0;
        if (this.color == -1){
            dr = -1;
        } else{
            dr = 1;
        }

        int row = this.row() + dr;
        int column = this.column();

        // just the straight forward path, not include attacking;
        if (!doubleMove){
            for (int i = 0; i <= 1; i++){
                if (row <= 7 && row >= 0  && Main.board[row][column].getPiece() == null){
                    PossiblePath.add(Main.board[row][column]);
                    row = row + dr;
                } else{
                    break;
                }
            }
        } else{
            if (row <= 7 && row >= 0 && Main.board[row][column].getPiece() == null) {
                PossiblePath.add(Main.board[row][column]);
            }
        }
        row = this.row();
        column = this.column();
        int[] dc = {-1, 1};
        // include attacking;
        // check for adjacent pieces
        for (int dx : dc){
            if (row + dr <= 7 && row + dr >= 0 && column + dx <= 7 && column + dx >= 0){
                Piece opponentPiece = Main.board[row + dr][column + dx].getPiece();
                if (opponentPiece != null){
                    PossiblePath.add(Main.board[row + dr][column + dx]);
                }
            }
        }
        return PossiblePath;
    }
}


// try to implement socket programming and multithread for project based

