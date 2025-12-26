import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import  javax.swing.Timer;

public class WordSearchGameGUI extends JFrame {

    private static final int ROWS = 10;
    private static final int COLS = 10;

    private final JLabel[][] gridLabels = new JLabel[ROWS][COLS];
    private final JTextArea statusArea = new JTextArea(8, 24);
    private final JTextArea wordListArea = new JTextArea(12, 20);
    private final JTextField wordInput = new JTextField(18);

    private WordSearchGame game;
    private final List<Coord> selected = new ArrayList<>();
    private final StringBuilder current = new StringBuilder();

    public WordSearchGameGUI() {
        super("Word Search Game");
        game = new WordSearchGame();
        buildGUI();
        loadGridToUI();
        refreshWordList();
        promptPlayerName();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildGUI() {
        setLayout(new BorderLayout(8, 8));

        //-------------Grid panel-------------
        JPanel gridPanel=new JPanel(new GridLayout(ROWS, COLS));
        Font gridFont=new Font(Font.MONOSPACED, Font.BOLD, 18);
        for (int r=0;r<ROWS;r++) {
            for (int c=0;c<COLS;c++) {
                JLabel lbl = new JLabel("", SwingConstants.CENTER);
                lbl.setFont(gridFont);
                lbl.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                lbl.setOpaque(true);
                lbl.setBackground(Color.WHITE);
                final int rr = r, cc = c;
                lbl.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        handleCellClick(rr, cc);
                    }
                });
                gridLabels[r][c] = lbl;
                gridPanel.add(lbl);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        //--------Right information panel----------
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(300, 420));

        statusArea.setEditable(false);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setBorder(BorderFactory.createTitledBorder("Status | Score: 0"));
        right.add(statusScroll);
        right.add(Box.createVerticalStrut(8));

        wordListArea.setEditable(false);
        wordListArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane wordScroll = new JScrollPane(wordListArea);
        wordScroll.setBorder(BorderFactory.createTitledBorder("Words to Find"));
        right.add(wordScroll);

        add(right, BorderLayout.EAST);

        // Bottom control panel
        JPanel bottom = new JPanel(new BorderLayout());
        JPanel input = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wordInput.setEditable(false);
        input.add(new JLabel("Selected:"));
        input.add(wordInput);
        JButton submit = new JButton("Submit");
        submit.addActionListener(e -> onSubmitOrClear());
        input.add(submit);
        bottom.add(input, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton undoBtn = new JButton("Undo");
        JButton boardBtn = new JButton("Leaderboard");
        JButton quitBtn = new JButton("Quit");

        undoBtn.addActionListener(e -> onUndo());
        boardBtn.addActionListener(e -> onLeaderboard());
        quitBtn.addActionListener(e -> onQuit());

        buttons.add(undoBtn);
        buttons.add(boardBtn);
        buttons.add(quitBtn);

        bottom.add(buttons, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
    }

    private void promptPlayerName() {
        String name = JOptionPane.showInputDialog(this, "Enter your name:", "Player Name", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty())
            game.setPlayerName(name.trim());
        statusArea.append("Welcome, " + game.getPlayerName() + " — find the words!\n");
        updateScoreTitle();
    }

    private void loadGridToUI() {
        char[][] g = game.getGrid();
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                gridLabels[r][c].setText(String.valueOf(g[r][c]));
    }

    private void refreshWordList() {
        StringBuilder sb = new StringBuilder();
        sb.append("Words to find:\n\n");
        for (String w : game.getWordsToPlace()) {
            sb.append(game.getFoundList().contains(w) ? "✓ " : "  ").append(w).append("\n");
        }
        wordListArea.setText(sb.toString());
    }

    private void updateScoreTitle() {
        Component container=statusArea.getParent(); // Jpanel of status area
        if (container!=null && container.getParent() instanceof JScrollPane) // parent of jpanel
        {
            JScrollPane sp = (JScrollPane) container.getParent(); //casting to access Border
            if (sp.getBorder() instanceof javax.swing.border.TitledBorder) {
                ((javax.swing.border.TitledBorder) sp.getBorder()).setTitle("Status | Score: " + game.getScore());
                repaint();
            }
        }
    }

    private void appendStatus(String message) {
        statusArea.append(message);
    }


    private void handleCellClick(int r, int c) {
        Coord coord = new Coord(r, c);

        if (!selected.isEmpty() && selected.get(selected.size() - 1).equals(coord)) {
            deselectLast();
            return;
        }
        if (selected.isEmpty() || isAdjacent(selected.get(selected.size() - 1), coord)) {
            select(coord);
            return;
        }
        appendStatus("Invalid move.\n");
        clearSelection(true);
        select(coord);
    }

    private void select(Coord coord) {
        if (selected.contains(coord))
            return;
        selected.add(coord);
        char ch = gridLabels[coord.r][coord.c].getText().charAt(0);
        current.append(ch);
        gridLabels[coord.r][coord.c].setBackground(Color.YELLOW);
        wordInput.setText(current.toString());
    }

    private void deselectLast() {
        if (selected.isEmpty())
            return;
        Coord last = selected.remove(selected.size() - 1);
        if (current.length() > 0)
            current.setLength(current.length() - 1);
        gridLabels[last.r][last.c].setBackground(Color.WHITE);
        wordInput.setText(current.toString());
    }

    private void clearSelection(boolean visual) {
        if (visual) selected.forEach(c -> gridLabels[c.r][c.c].setBackground(Color.WHITE));
        selected.clear();
        current.setLength(0);
        wordInput.setText("");
    }

    private boolean isAdjacent(Coord a, Coord b) {
        int dr = Math.abs(a.r - b.r);
        int dc = Math.abs(a.c - b.c);
        return (dr <= 1 && dc <= 1 && (dr != 0 || dc != 0));
    }

    private boolean isLinear(List<Coord> path) {
        if (path.size() <= 1) return true;
        int dr = Integer.signum(path.get(1).r - path.get(0).r);
        int dc = Integer.signum(path.get(1).c - path.get(0).c);
        for (int i = 1; i < path.size() - 1; i++) {
            if (Integer.signum(path.get(i + 1).r - path.get(i).r) != dr ||
                    Integer.signum(path.get(i + 1).c - path.get(i).c) != dc) return false;
        }
        return true;
    }
    private void flashErrorSelection() {
        selected.forEach(c -> gridLabels[c.r][c.c].setBackground(Color.RED));
        Timer timer = new Timer(300, new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                selected.forEach(c -> gridLabels[c.r][c.c].setBackground(Color.WHITE));
                ((Timer)e.getSource()).stop();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void onSubmitOrClear() {
        if (current.length() == 0) {
            clearSelection(true);
            appendStatus("Selection cleared.\n" );
            return;
        }

        String w=current.toString();
        final int deduction=5;

        if (w.length()<3) {
            game.deductScore(deduction);
            appendStatus("Word too short. -" +deduction+ " points.\n");
            flashErrorSelection();
            clearSelection(false);
            updateScoreTitle();
            return;
        }
        if (!game.getDictionary().contains(w)) {
            game.deductScore(deduction);
            appendStatus("\"" + w + "\" not in puzzle. -" +deduction+ " points.\n");
            flashErrorSelection();
            clearSelection(false);
            updateScoreTitle();
            return;
        }
        if (game.getFoundList().contains(w)) {
            game.deductScore(deduction);
            appendStatus("\"" + w + "\" already found. -" +deduction+ " points.\n");
            flashErrorSelection();
            clearSelection(false);
            updateScoreTitle();
            return;
        }
        if (!isLinear(selected)) {
            game.deductScore(deduction);
            appendStatus("Please select linearly. -" +deduction+ " points.\n");
            flashErrorSelection();
            clearSelection(false);
            updateScoreTitle();
            return;
        }

        //-------------Word found----------------------
        game.processFoundWord(w, selected);
        selected.forEach(c -> {
            gridLabels[c.r][c.c].setBackground(Color.GREEN.darker());
            gridLabels[c.r][c.c].setForeground(Color.WHITE);
        });

        appendStatus("Found \"" + w + "\"! +" + (w.length() * 2) + " points\n");

        clearSelection(false);
        refreshWordList();
        updateScoreTitle();

        if (game.getFoundList().size() == game.getWordsToPlace().length) {
            JOptionPane.showMessageDialog(this, "CONGRATULATIONS! You found all words!");
            onQuit();
        }
    }

    private void onUndo() {
        String undone = game.undoLast();
        if (undone == null)
            appendStatus("Nothing to undo.\n");
        else
            appendStatus("Undo \"" + undone + "\".\n");
        refreshWordList();

        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                gridLabels[r][c].setBackground(Color.WHITE);
                gridLabels[r][c].setForeground(Color.BLACK);
            }

        for (String fw : game.getFoundList().toArray()) markFoundWordOnGrid(fw);
        updateScoreTitle();
    }

    private void markFoundWordOnGrid(String word) {
        char[] chars = word.toCharArray();
        char[][] g = game.getGrid();
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                for (int dir = 0; dir < 8; dir++) {
                    int rr = r, cc = c, k = 0;
                    while (k < chars.length && rr>=0 && rr<ROWS && cc>=0 && cc<COLS && g[rr][cc] == chars[k]) {
                        rr += WordSearchGame.dr[dir];
                        cc += WordSearchGame.dc[dir];
                        k++;
                    }
                    if (k==chars.length) {
                        rr=r;
                        cc=c;
                        for (int i= 0; i<chars.length;i++) {
                            gridLabels[rr][cc].setBackground(Color.GREEN.darker());
                            gridLabels[rr][cc].setForeground(Color.WHITE);
                            rr += WordSearchGame.dr[dir];
                            cc += WordSearchGame.dc[dir];
                        }
                    }
                }
    }

    private void onLeaderboard() {
        game.saveScore();
        JDialog dlg = new JDialog(this, "Leaderboard", true);
        JTextArea txt = new JTextArea(game.getLeaderboard().printDescToString());
        txt.setEditable(false);
        dlg.add(new JScrollPane(txt));
        dlg.setSize(360, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void onQuit() {
        game.saveScore();
        JOptionPane.showMessageDialog(this, "Thanks for playing, " + game.getPlayerName() + "! Final score: " + game.getScore());
        onLeaderboard();
        System.exit(0);
    }
    public List getSelected() {
        return selected;
    }
    private static record Coord(int r, int c) {
    }


    private static class WordSearchGame {
        WordSearchGame() {
            for(String w: wordsToPlace){
                dictionary.insert(w);
            }
            placeWordsRandomly();
            fillRandomLetters();
        }

        private final char[][] grid={
                {'X','X','X','X','X','X','X','X','X','X'},
                {'X','X','X','X','X','X','X','X','X','X'},
                {'X','X','X','X','X','X','X','X','X','X'},
                {'X','X','X','X','X','X','X','X','X','X'},
                {'X','X','X','X','X','X','X','X','X','X'},
                {'X','X','X','X','X','X','X','X','X','X'},
                {'X','X','X','X','X','X','X','X','X','X'},
                {'X','X','X','X','X','X','X','X','X','X'},
                {'X','X','X','X','X','X','X','X','X','X'},
                {'X','X','X','X','X','X','X','X','X','X'}
        };
        private int score=0;
        private String playerName="Player";
        private String[] wordsToPlace = {"JAVA","CODE","PLAYER","STACK","QUEUE","METHOD", "PUBLIC", "ERROR", "CONST", "FINAL", "FIELD"};
        private FoundList foundList = new FoundList();
        private Deque<FoundWord> Stack = new ArrayDeque<>();
        private ScoreBST leaderboard = new ScoreBST();
        private WordBST dictionary = new WordBST();
        private static final int[] dr={-1,-1,0,1,1,1,0,-1};
        private static final int[] dc={0,1,1,1,0,-1,-1,-1};

        private Random random = new Random();
        private static final int ROWS=10;
        private static final int COLS=10;

        private void fillRandomLetters() {
            for (int r=0;r<ROWS;r++) {
                for (int c=0;c<COLS;c++) {
                    if (grid[r][c]=='X') {
                        char randomChar = (char) ('A' + random.nextInt(26));
                        grid[r][c]=randomChar;
                    }
                }
            }
        }
        private void placeWordsRandomly() {
            for (String word : wordsToPlace) {
                for (int attempt=0;attempt<100;attempt++) {
                    int rStart=random.nextInt(ROWS);
                    int cStart=random.nextInt(COLS);
                    int dir=random.nextInt(8);

                    if (canPlaceWord(word,rStart,cStart,dir)) {
                        placeWord(word,rStart,cStart,dir);
                        break;
                    }
                }
            }
        }
        private boolean canPlaceWord(String word,int rStart,int cStart,int dir) {
            int r=rStart;
            int c=cStart;
            for (char ch : word.toCharArray()) {
                if (r<0 || r>=ROWS || c<0 || c>=COLS)
                    return false;
                if (grid[r][c]!='X' && grid[r][c]!=ch)
                    return false;
                r += dr[dir];
                c += dc[dir];
            }
            return true;
        }
        private void placeWord(String word,int rStart,int cStart,int dir) {
            int r=rStart;
            int c=cStart;
            for (char ch : word.toCharArray()) {
                grid[r][c] = ch;
                r+=dr[dir];
                c+=dc[dir];
            }
        }
        public char[][] getGrid(){
            return grid;
        }
        public int getScore(){
            return score;
        }
        public void setPlayerName(String n){
            playerName=n;
        }
        public String getPlayerName(){
            return playerName;
        }
        public String[] getWordsToPlace(){
            return wordsToPlace;
        }
        public FoundList getFoundList(){
            return foundList;
        }
        public ScoreBST getLeaderboard(){
            return leaderboard;
        }
        public WordBST getDictionary(){
            return dictionary;
        }
        //------Deduct points----------------
        public void deductScore(int points){
            score = Math.max(0, score - points);
        }
        private static class FoundWord {
            String word;
            List<Coord> path;
            FoundWord(String w,List<Coord> p){
                word=w;
                path=p;
            }
        }

        public void processFoundWord(String word,List<Coord> path){
            foundList.add(word);
            Stack.push(new FoundWord(word,new ArrayList<>(path)));
            score += word.length()*2;
        }
        public String undoLast(){
            if(Stack.isEmpty())
                return null;
            FoundWord fw = Stack.pop();
            if(foundList.remove(fw.word)){
                score = Math.max(0,score-fw.word.length()*2);
                return fw.word;
            }
            return null;
        }
        public void saveScore(){
            leaderboard.insert(playerName,score);
        }


        public static class FoundList{
            private Node head;
            private int count=0;
            private static class Node{
                String word; Node next;
                Node(String w){
                    word=w;
                }
            }
            public void add(String w){
                Node n=new Node(w);
                n.next=head;
                head=n;
                count++;
            }
            public boolean contains(String w){
                Node cur=head;
                while(cur!=null){
                    if(cur.word.equals(w))
                        return true;
                    cur=cur.next;
                }
                return false;
            }
            public boolean remove(String w){
                Node cur=head,prev=null;
                while(cur!=null){
                    if(cur.word.equals(w)){
                        if(prev==null)
                            head=cur.next;
                        else
                            prev.next=cur.next;
                        count--;
                        return true;
                    }
                    prev=cur;
                    cur=cur.next;
                }
                return false;
            }
            public int size(){
                return count;
            }
            public String[] toArray(){
                String[] arr=new String[count];
                Node cur=head;
                int i=0;
                while(cur!=null){
                    arr[i++]=cur.word;
                    cur=cur.next;
                }
                return arr;
            }
        }
        public static class WordBST {
            private WordNode root;

            private static class WordNode {
                String word;
                WordNode left,right;

                WordNode(String w) {
                    word=w;
                }
            }
            public void insert(String s) {
                if (root==null) {
                    root=new WordNode(s);
                    return;
                }

                WordNode current=root;

                while (true) {
                    int compare=s.compareTo(current.word);

                    if (compare<0) {
                        if (current.left==null) {
                            current.left=new WordNode(s);
                            return;
                        }
                        current=current.left;
                    }
                    else if (compare > 0) {
                        if (current.right==null) {
                            current.right=new WordNode(s);
                            return;
                        }
                        current=current.right;
                    }
                    else {
                        return;
                    }
                }
            }
            public boolean contains(String s) {
                WordNode current=root;

                while (current!=null) {
                    int compare=s.compareTo(current.word);

                    if (compare==0)
                        return true;
                    if (compare<0)
                        current = current.left;
                    else
                        current=current.right;
                }

                return false;
            }
        }
        public static class ScoreBST{
            private SNode root;
            private static class SNode{
                String name;
                int score;
                SNode left,right;
                SNode(String n,int s){
                    name=n;
                    score=s;
                }
            }
            public void insert(String name,int score){
                root=insertRec(root,name,score);
            }
            private SNode insertRec(SNode node,String name,int score){
                if(node==null)
                    return new SNode(name,score);
                if(name.equals(node.name)) {
                    if(score>node.score)
                        node.score=score;
                    return node;
                }
                if(score>node.score)
                    node.left=insertRec(node.left, name, score);
                else if(score<node.score)
                    node.right=insertRec(node.right, name, score);
                else {
                    if(name.compareTo(node.name) < 0)
                        node.left = insertRec(node.left, name, score);
                    else node.right = insertRec(node.right, name, score);
                }
                return node;
            }
            public String printDescToString(){
                if(root==null)
                    return "(no scores yet)";
                StringBuilder sb=new StringBuilder();
                printRec(root,sb,1);
                return sb.toString();
            }
            private int printRec(SNode node, StringBuilder sb, int rank) {
                if (node == null)
                    return rank;
                rank = printRec(node.left, sb, rank);
                sb.append(rank + ". " + node.name + " : " + node.score + "\n");
                rank++;
                rank = printRec(node.right, sb, rank);
                return rank;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WordSearchGameGUI());

    }

}