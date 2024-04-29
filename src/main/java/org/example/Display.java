package org.example;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Display implements ChangeListener, ActionListener {
    private final Executor executor = Executors.newCachedThreadPool();
    static final Color highlightColor;
    JFrame mainWindow;
    JPanel board;
    JPanel mainPanel;
    JPanel sizePanel;
    JPanel densityPanel;
    JSlider densitySlider;
    JSlider sizeSlider;
    JButton stopGo;
    JButton reset;
    JTextArea display;
    GridLayout gl;
    static Square[][] grid;
    static JTextField currentSquare;
    static int size;
    static int density;
    static int halfway;
    static double startTime;
    static double time;
    Crossword crossword;
    boolean across;
    String startInfo = "Click on the board to type in words.\nUse arrows to navigate the board.\nDouble-clicking alternates between across & down.";
    String endInfo = "Click on the board or press 'Reset' to clear the board";

    static {
        highlightColor = Color.cyan;
    }

    public Display() {
        size = 8;
        density = 30;
        this.across = true;
        halfway = (int)Math.ceil((double)size / 2.0);
        this.mainWindow = new JFrame();
        this.mainWindow.setLayout(new GridLayout(0, 2));
        this.board = new JPanel();
        this.stopGo = new JButton("FILL");
        this.stopGo.setFont(new Font("Dialog", 1, 40));
        this.stopGo.setBackground(Color.ORANGE);
        this.stopGo.setFocusPainted(false);
        this.stopGo.addActionListener(this);
        this.reset = new JButton("RESET");
        this.reset.setFont(new Font("Dialog", 1, 40));
        this.reset.setBackground(Color.ORANGE);
        this.reset.setFocusPainted(false);
        this.reset.addActionListener(this);
        this.densitySlider = new JSlider(0, 0, 45, density);
        this.densitySlider.setMajorTickSpacing(5);
        this.densitySlider.setPaintTicks(true);
        this.densitySlider.setPaintLabels(true);
        this.densitySlider.setSnapToTicks(true);
        this.densitySlider.setBackground(Color.YELLOW);
        this.densitySlider.addChangeListener(this);
        this.densityPanel = new JPanel(new GridLayout(2, 0));
        JLabel label = new JLabel();
        label.setFont(new Font("Dialog", 1, 20));
        label.setText("% of Black Squares");
        this.densityPanel.add(label);
        this.densityPanel.add(this.densitySlider);
        this.densityPanel.setBackground(Color.YELLOW);
        this.densityPanel.setToolTipText("This is roughly the percentage of black squares that will appear on the board.");
        this.sizeSlider = new JSlider(0, 2, 14, size);
        this.sizeSlider.setMajorTickSpacing(1);
        this.sizeSlider.setPaintLabels(true);
        this.sizeSlider.setSnapToTicks(true);
        this.sizeSlider.setBackground(Color.YELLOW);
        this.sizeSlider.addChangeListener(this);
        this.sizePanel = new JPanel(new GridLayout(2, 0));
        label = new JLabel();
        label.setFont(new Font("Dialog", 1, 20));
        label.setText("Size of Crossword");
        this.sizePanel.add(label);
        this.sizePanel.add(this.sizeSlider);
        this.sizePanel.setBackground(Color.YELLOW);
        this.sizePanel.setToolTipText("This is the number of tiles on a side of the crossword square");
        this.display = new JTextArea();
        this.display.setFont(new Font("Dialog", 1, 18));
        this.display.setOpaque(true);
        this.display.setBackground(Color.YELLOW);
        this.display.setText(this.startInfo);
        this.mainPanel = new JPanel(new GridLayout(5, 0));
        this.mainPanel.add(this.densityPanel);
        this.mainPanel.add(this.sizePanel);
        this.mainPanel.add(this.stopGo);
        this.mainPanel.add(this.reset);
        this.mainPanel.add(this.display);
        this.mainWindow.getContentPane().add(this.mainPanel);
        this.mainWindow.getContentPane().add(this.board);
        this.mainWindow.pack();
        this.mainWindow.setDefaultCloseOperation(3);
        this.mainWindow.setExtendedState(6);
        this.populateBoard();
    }

    private void grayAll() {
        for(int x = 0; x < grid.length; ++x) {
            for(int y = 0; y < grid.length; ++y) {
                if (grid[x][y].getText().isEmpty() && !grid[x][y].isLocked()) {
                    grid[x][y].setBackground(Color.lightGray);
                }
            }
        }

    }

    private void redHighlight(WordInfo word) {
        int i;
        if (word.direction == 'a') {
            for(i = 0; i < word.word.length; ++i) {
                grid[word.x][word.y + i].setBackground(Color.pink);
            }
        } else if (word.direction == 'd') {
            for(i = 0; i < word.word.length; ++i) {
                grid[word.x + i][word.y].setBackground(Color.pink);
            }
        }

    }

    private void lockAll() {
        for(int x = 0; x < size; ++x) {
            for(int y = 0; y < size; ++y) {
                grid[x][y].setLock(true);
            }
        }

    }

    private String getAlpha(String key) {
        if (key.length() != 1) {
            return "";
        } else {
            try {
                Integer.parseInt(key);
                return "";
            } catch (NumberFormatException var3) {
                return key;
            }
        }
    }

    public void populateBoard() {
        grid = new Square[size][size];
        this.gl = new GridLayout(size, size);
        this.board.removeAll();
        this.board.setLayout(this.gl);
        Random rand = new Random();
        int[][] seeds = new int[size][halfway];

        int x2;
        int y2;
        for(x2 = 0; x2 < size; ++x2) {
            for(y2 = 0; y2 < halfway; ++y2) {
                seeds[x2][y2] = rand.nextInt(100);
            }
        }

        x2 = size;

        for(int x = 0; x < size; ++x) {
            --x2;
            y2 = size % 2 == 0 ? halfway : halfway - 1;

            for(int y = 0; y < size; ++y) {
                grid[x][y] = new Square(x, y);
                boolean setBlack;
                if (y < halfway) {
                    if (seeds[x][y] < density) {
                        setBlack = true;
                    } else {
                        setBlack = false;
                    }
                } else {
                    --y2;
                    if (seeds[x2][y2] < density) {
                        setBlack = true;
                    } else {
                        setBlack = false;
                    }
                }

                if (setBlack) {
                    grid[x][y].setBackground(Color.black);
                    grid[x][y].setLock(true);
                    grid[x][y].setFocusable(false);
                } else {
                    grid[x][y].setBackground(Color.lightGray);
                }

                this.board.add(grid[x][y]);
            }
        }

        this.board.repaint();
        this.mainWindow.setVisible(true);
    }

    public char[][] getChars() {
        char[][] crossword = new char[size + 1][size + 1];

        int x;
        for(x = 0; x < size; ++x) {
            for(int y = 0; y < size; ++y) {
                if (grid[x][y].getBackground() == Color.black) {
                    crossword[x][y] = '\n';
                } else if (!grid[x][y].getText().isEmpty()) {
                    crossword[x][y] = grid[x][y].getText().charAt(0);
                } else {
                    crossword[x][y] = ' ';
                }
            }
        }

        for(x = 0; x < size; ++x) {
            crossword[x][size] = '\n';
        }

        for(x = 0; x < size; ++x) {
            crossword[size][x] = '\n';
        }

        return crossword;
    }

    static void displayWords(char[][] crossword) {
        for(int x = 0; x < crossword.length - 1; ++x) {
            for(int y = 0; y < crossword.length - 1; ++y) {
                if (crossword[x][y] != '\n') {
                    grid[x][y].setText("" + crossword[x][y]);
                }
            }
        }

    }

    private void ungray() {
        for(int x = 0; x < size; ++x) {
            for(int y = 0; y < size; ++y) {
                grid[x][y].setLock(true);
                if (grid[x][y].getText().isEmpty() && grid[x][y].getBackground() != Color.black) {
                    grid[x][y].setBackground(Color.white);
                }
            }
        }

    }

    private void reset() {
        for(int x = 0; x < size; ++x) {
            for(int y = 0; y < size; ++y) {
                if (grid[x][y].getBackground() != Color.black) {
                    grid[x][y].setText("");
                    grid[x][y].setBackground(Color.lightGray);
                    grid[x][y].setLock(false);
                }
            }
        }

        this.display.setText(this.startInfo);
    }

    public void stateChanged(ChangeEvent e) {
        this.stop();
        JSlider source = (JSlider)e.getSource();
        if (source.equals(this.sizeSlider) && source.getValue() != size) {
            size = source.getValue();
            halfway = (int)Math.ceil((double)size / 2.0);
            this.populateBoard();
        } else if (source.equals(this.densitySlider) && (source.getValue() <= density - 5 || source.getValue() >= density + 5)) {
            density = source.getValue();
            this.populateBoard();
        }

    }

    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton)e.getSource();
        if (source.equals(this.stopGo) && this.stopGo.getText() == "FILL") {
            this.start();
        } else if (source.equals(this.stopGo) && this.stopGo.getText() == "STOP") {
            this.stop();
        } else if (source.equals(this.reset)) {
            this.stop();
            this.reset();
        }

    }

    private void start() {
        try {
            this.crossword = new Crossword(this.getChars());
            ArrayList<WordInfo> invalidWords = this.crossword.getInvalidWords();
            if (invalidWords.isEmpty()) {
                this.ungray();
                this.stopGo.setText("STOP");
                this.executor.execute(new crosswordWorker((crosswordWorker)null));
                this.executor.execute(new timeWorker((timeWorker)null));
            } else {
                this.lockAll();
                StringBuffer sb = new StringBuffer();
                boolean incomplete = false;
                sb.append("Unfortunate news:\n");
                Iterator var5 = invalidWords.iterator();

                while(var5.hasNext()) {
                    WordInfo word = (WordInfo)var5.next();

                    for(int i = 0; i < word.word.length; ++i) {
                        if (word.word[i] == ' ') {
                            word.word[i] = '_';
                            incomplete = true;
                        }
                    }

                    if (incomplete) {
                        sb.append("None of my " + word.word.length + " letter words complete ");
                        sb.append(word.word);
                        sb.append(".\n");
                    } else {
                        sb.append(word.word);
                        sb.append(" is not in my dictionary.\n");
                    }

                    this.redHighlight(word);
                    this.display.setText(sb.toString() + this.endInfo);
                }
            }
        } catch (Exception var7) {
            Exception e1 = var7;
            this.display.setText(e1.toString());
        }

    }

    private void stop() {
        if (this.crossword != null) {
            this.crossword.stop();
            this.stopGo.setText("FILL");
        }

    }

    public static void main(String[] args) throws Exception {
        new Display();
    }

    class Square extends JTextField implements KeyListener, FocusListener, MouseListener {
        int x;
        int y;
        int count;
        boolean locked;
        boolean clicked;

        public Square(int x, int y) {
            this.x = x;
            this.y = y;
            this.locked = false;
            this.clicked = false;
            this.setFont(new Font("Dialog", 0, (int)(500.0 / (double)Display.size)));
            this.setForeground(Color.black);
            this.setDisabledTextColor(Color.black);
            this.setHorizontalAlignment(0);
            this.setEnabled(true);
            this.setBorder(new LineBorder(Color.black, 1));
            this.addKeyListener(this);
            this.addFocusListener(this);
            this.addMouseListener(this);
            this.setEditable(false);
        }

        public void setLock(boolean bool) {
            this.locked = bool;
        }

        public boolean isLocked() {
            return this.locked;
        }

        private void highlightAcross() {
            Display.this.across = true;
            Display.this.grayAll();

            int count;
            for(count = 0; this.y - count >= 0 && Display.grid[this.x][this.y - count].isFocusable(); ++count) {
                Display.grid[this.x][this.y - count].setBackground(Display.highlightColor);
            }

            for(count = 0; this.y + count < Display.size && Display.grid[this.x][this.y + count].isFocusable(); ++count) {
                Display.grid[this.x][this.y + count].setBackground(Display.highlightColor);
            }

        }

        private void highlightDown() {
            Display.this.across = false;
            Display.this.grayAll();

            int count;
            for(count = 0; this.x - count >= 0 && Display.grid[this.x - count][this.y].isFocusable(); ++count) {
                Display.grid[this.x - count][this.y].setBackground(Display.highlightColor);
            }

            for(count = 0; this.x + count < Display.size && Display.grid[this.x + count][this.y].isFocusable(); ++count) {
                Display.grid[this.x + count][this.y].setBackground(Display.highlightColor);
            }

        }

        private boolean isAcrossSquare() {
            if ((this.squareToLeft() == null || this.squareToLeft().isBlack()) && (this.squareToRight() == null || this.squareToRight().isBlack()) && this.squareBelow() != null && !this.squareBelow().isBlack() && this.squareAbove() != null && !this.squareAbove().isBlack()) {
                return false;
            } else {
                return (this.squareAbove() == null || this.squareAbove().isBlack() || this.squareBelow() != null && !this.squareBelow().isBlack()) && (this.squareAbove() != null && !this.squareAbove().isBlack() || this.squareBelow() == null || this.squareBelow().isBlack());
            }
        }

        public boolean isBlack() {
            return this.getBackground() == Color.black;
        }

        private Square squareToLeft() {
            return this.y - 1 >= 0 ? Display.grid[this.x][this.y - 1] : null;
        }

        private Square squareToRight() {
            return this.y + 1 < Display.size ? Display.grid[this.x][this.y + 1] : null;
        }

        private Square squareAbove() {
            return this.x - 1 >= 0 ? Display.grid[this.x - 1][this.y] : null;
        }

        private Square squareBelow() {
            return this.x + 1 < Display.size ? Display.grid[this.x + 1][this.y] : null;
        }

        public void keyPressed(KeyEvent e) {
            if (!this.locked) {
                switch (e.getKeyCode()) {
                    case 8:
                        this.count = 1;
                        if (!this.getText().isEmpty()) {
                            this.setText("");
                            break;
                        } else if (Display.this.across) {
                            while(this.y - this.count < Display.size) {
                                if (Display.grid[this.x][this.y - this.count].isFocusable()) {
                                    Display.grid[this.x][this.y - this.count].requestFocus();
                                    Display.grid[this.x][this.y - this.count].setText("");
                                    return;
                                }

                                ++this.count;
                            }

                            return;
                        } else {
                            while(this.x - this.count < Display.size) {
                                if (Display.grid[this.x - this.count][this.y].isFocusable()) {
                                    Display.grid[this.x - this.count][this.y].requestFocus();
                                    Display.grid[this.x - this.count][this.y].setText("");
                                    return;
                                }

                                ++this.count;
                            }

                            return;
                        }
                    case 10:
                        Display.this.start();
                        break;
                    case 37:
                        Display.this.across = true;
                        this.highlightAcross();

                        for(this.count = 1; this.y - this.count < Display.size; ++this.count) {
                            if (Display.grid[this.x][this.y - this.count].isFocusable()) {
                                Display.grid[this.x][this.y - this.count].requestFocus();
                                return;
                            }
                        }

                        return;
                    case 38:
                        this.count = 1;
                        Display.this.across = false;
                        this.highlightDown();

                        while(this.x - this.count < Display.size) {
                            if (Display.grid[this.x - this.count][this.y].isFocusable()) {
                                Display.grid[this.x - this.count][this.y].requestFocus();
                                return;
                            }

                            ++this.count;
                        }

                        return;
                    case 39:
                        Display.this.across = true;

                        for(this.count = 1; this.y + this.count < Display.size; ++this.count) {
                            if (Display.grid[this.x][this.y + this.count].isFocusable()) {
                                Display.grid[this.x][this.y + this.count].requestFocus();
                                return;
                            }
                        }

                        return;
                    case 40:
                        Display.this.across = false;

                        for(this.count = 1; this.x + this.count < Display.size; ++this.count) {
                            if (Display.grid[this.x + this.count][this.y].isFocusable()) {
                                Display.grid[this.x + this.count][this.y].requestFocus();
                                return;
                            }
                        }

                        return;
                    default:
                        String key = Display.this.getAlpha(KeyEvent.getKeyText(e.getKeyCode()));
                        if (key != "") {
                            this.setText(String.valueOf(key));
                            this.count = 1;
                            if (Display.this.across) {
                                while(this.y + this.count < Display.size) {
                                    if (Display.grid[this.x][this.y + this.count].isFocusable()) {
                                        Display.grid[this.x][this.y + this.count].requestFocus();
                                        break;
                                    }

                                    ++this.count;
                                }
                            } else {
                                while(this.x + this.count < Display.size) {
                                    if (Display.grid[this.x + this.count][this.y].isFocusable()) {
                                        Display.grid[this.x + this.count][this.y].requestFocus();
                                        break;
                                    }

                                    ++this.count;
                                }
                            }
                        }
                }

            }
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }

        public void focusGained(FocusEvent e) {
            if (!this.locked) {
                this.getCaret().setVisible(true);
                if (Display.this.across) {
                    this.highlightAcross();
                } else if (!Display.this.across) {
                    this.highlightDown();
                }
            }

        }

        public void focusLost(FocusEvent e) {
            this.getCaret().setVisible(false);
            this.clicked = false;
        }

        public void mouseClicked(MouseEvent arg0) {
            if (this.locked) {
                if (Display.this.stopGo.getText() == "STOP") {
                    Display.this.stop();
                } else if (Display.this.stopGo.getText() == "FILL") {
                    Display.this.reset();
                }
            } else if (this.clicked) {
                if (Display.this.across) {
                    Display.this.across = false;
                    this.highlightDown();
                } else {
                    Display.this.across = true;
                    this.highlightAcross();
                }
            } else {
                this.clicked = true;
            }

        }

        public void mouseEntered(MouseEvent arg0) {
            if (!this.locked) {
                if (this.isAcrossSquare()) {
                    Display.this.across = true;
                    this.highlightAcross();
                } else {
                    Display.this.across = false;
                    this.highlightDown();
                }

            }
        }

        public void mouseExited(MouseEvent arg0) {
            Display.this.grayAll();
        }

        public void mousePressed(MouseEvent arg0) {
        }

        public void mouseReleased(MouseEvent arg0) {
        }
    }

    private class crosswordWorker extends SwingWorker<Void, Void> {
        private crosswordWorker() {
        }

        public Void doInBackground() throws Exception {
            try {
                Display.this.crossword.fill();
                Display.this.stopGo.setText("FILL");
            } catch (Exception var2) {
                Exception e1 = var2;
                e1.printStackTrace();
            }

            return (Void)this.get();
        }
    }

    private class timeWorker extends SwingWorker<Void, Void> {
        private timeWorker() {
        }

        public Void doInBackground() throws Exception {
            DecimalFormat format = new DecimalFormat("#.###");
            Display.startTime = (double)System.currentTimeMillis();

            while(!Display.this.crossword.isDone()) {
                Display.time = ((double)System.currentTimeMillis() - Display.startTime) / 1000.0;
                String message = Display.time >= 60.0 ? (int)(Display.time / 60.0) + " minutes " + format.format(Display.time % 60.0) + " seconds" : format.format(Display.time) + " seconds";
                Display.this.display.setText(message);
            }

            return (Void)this.get();
        }
    }
}

