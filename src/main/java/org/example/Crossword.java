package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;

public class Crossword {
    int depth;
    boolean stop = false;
    boolean done = false;
    boolean surrounded;
    char[][] crossword;
    char[][] startState;
    ArrayList<int[]> acrossBlackSquares;
    ArrayList<int[]> downBlackSquares;
    ArrayList<WordInfo> unfinishedWords;
    ArrayList<WordInfo> finishedWords;
    ArrayList<WordInfo> blankWords;
    ArrayList<WordInfo> nextWords;
    Stack<WordInfo> lastWords;
    Stack<WordInfo> tempLastWords;
    Stack<node> nodeList;
    Dictionary dictionary = new Dictionary();

    public Crossword(char[][] crossword) throws Exception {
        this.crossword = this.copyChars(crossword);
        this.startState = this.copyChars(crossword);
        this.getBlackSquares();
        this.lastWords = new Stack();
        this.tempLastWords = new Stack();
        this.updateWordLists();
        this.depth = this.blankWords.size() + this.finishedWords.size() + this.unfinishedWords.size();
    }

    public int size() {
        return this.crossword.length;
    }

    public int getDepth() {
        return this.depth;
    }

    public char[][] getChars() {
        return this.crossword;
    }

    public ArrayList<WordInfo> getFinished() {
        return this.finishedWords;
    }

    public ArrayList<WordInfo> getUnfinished() {
        return this.unfinishedWords;
    }

    private void getBlackSquares() {
        this.acrossBlackSquares = new ArrayList();
        this.downBlackSquares = new ArrayList();

        int y;
        int x;
        for(y = 0; y < this.crossword.length - 1; ++y) {
            for(x = -1; x < this.crossword.length; ++x) {
                if (x == -1 || this.crossword[y][x] == '\n') {
                    this.acrossBlackSquares.add(new int[]{y, x});
                }
            }
        }

        for(y = 0; y < this.crossword.length - 1; ++y) {
            for(x = -1; x < this.crossword.length; ++x) {
                if (x == -1 || this.crossword[x][y] == '\n') {
                    this.downBlackSquares.add(new int[]{x, y});
                }
            }
        }

    }

    private void updateWordLists() {
        this.unfinishedWords = new ArrayList();
        this.finishedWords = new ArrayList();
        this.blankWords = new ArrayList();
        this.nextWords = new ArrayList();
        this.surrounded = false;

        int y;
        int blackRow;
        int blackColumn;
        int nextBlackRow;
        int nextBlackColumn;
        char[] word;
        WordInfo wi;
        int spaceCount;
        int j;
        for(y = 0; y < this.acrossBlackSquares.size() - 1; ++y) {
            blackRow = ((int[])this.acrossBlackSquares.get(y))[0];
            blackColumn = ((int[])this.acrossBlackSquares.get(y))[1];
            nextBlackRow = ((int[])this.acrossBlackSquares.get(y + 1))[0];
            nextBlackColumn = ((int[])this.acrossBlackSquares.get(y + 1))[1];
            if (blackRow == nextBlackRow && blackColumn + 1 < nextBlackColumn) {
                word = Arrays.copyOfRange(this.crossword[blackRow], blackColumn + 1, nextBlackColumn);
                wi = new WordInfo(word, blackRow, blackColumn + 1, 'a');
                spaceCount = 0;

                for(j = 0; j < word.length; ++j) {
                    if (word[j] == ' ') {
                        ++spaceCount;
                    }
                }

                if (spaceCount == 0) {
                    this.finishedWords.add(wi);
                } else if (spaceCount == word.length) {
                    this.blankWords.add(wi);
                } else {
                    this.unfinishedWords.add(wi);
                }
            }
        }

        for(y = 0; y < this.downBlackSquares.size() - 1; ++y) {
            blackRow = ((int[])this.downBlackSquares.get(y))[0];
            blackColumn = ((int[])this.downBlackSquares.get(y))[1];
            nextBlackRow = ((int[])this.downBlackSquares.get(y + 1))[0];
            nextBlackColumn = ((int[])this.downBlackSquares.get(y + 1))[1];
            if (blackColumn == nextBlackColumn && blackRow + 1 < nextBlackRow) {
                word = new char[nextBlackRow - (blackRow + 1)];

                for(int i = 0; i < word.length; ++i) {
                    word[i] = this.crossword[blackRow + 1 + i][blackColumn];
                }

                wi = new WordInfo(word, blackRow + 1, blackColumn, 'd');
                spaceCount = 0;

                for(j = 0; j < word.length; ++j) {
                    if (word[j] == ' ') {
                        ++spaceCount;
                    }
                }

                if (spaceCount == 0) {
                    this.finishedWords.add(wi);
                } else if (spaceCount == word.length) {
                    this.blankWords.add(wi);
                } else {
                    this.unfinishedWords.add(wi);
                }
            }
        }

        this.getNextWords();
    }

    private void getNextWords() {
        this.tempLastWords = (Stack)this.lastWords.clone();

        while(!this.tempLastWords.isEmpty() && this.nextWords.isEmpty()) {
            Iterator var2 = this.unfinishedWords.iterator();

            while(var2.hasNext()) {
                WordInfo uf = (WordInfo)var2.next();
                if (this.intersects(uf, (WordInfo)this.tempLastWords.peek())) {
                    this.nextWords.add(uf);
                }
            }

            if (this.nextWords.isEmpty()) {
                this.tempLastWords.pop();
                this.surrounded = true;
            }
        }

    }

    private WordInfo getNextWord() {
        WordInfo nextWord = new WordInfo();
        int leastMatches = Integer.MAX_VALUE;
        WordInfo wi;
        Iterator var4;
        int matches;
        if (!this.nextWords.isEmpty()) {
            var4 = this.nextWords.iterator();

            while(var4.hasNext()) {
                wi = (WordInfo)var4.next();
                matches = this.dictionary.howManyMatches(wi.word);
                if (matches < leastMatches) {
                    leastMatches = matches;
                    nextWord = wi;
                }
            }
        } else if (!this.unfinishedWords.isEmpty()) {
            var4 = this.unfinishedWords.iterator();

            while(var4.hasNext()) {
                wi = (WordInfo)var4.next();
                if (wi.word.length > nextWord.word.length) {
                    nextWord = wi;
                }
            }
        } else {
            var4 = this.blankWords.iterator();

            while(var4.hasNext()) {
                wi = (WordInfo)var4.next();
                matches = this.dictionary.howManyWords(wi.word.length);
                if (matches < leastMatches) {
                    leastMatches = matches;
                    nextWord = wi;
                }
            }
        }

        return nextWord;
    }

    private boolean intersects(WordInfo l, WordInfo r) {
        if (l.direction == r.direction) {
            return false;
        } else {
            return l.direction == 'a' && l.x >= r.x && l.x <= r.x + r.word.length - 1 && l.y <= r.y && r.y <= l.y + l.word.length - 1 || l.direction == 'd' && l.y >= r.y && l.y <= r.y + r.word.length - 1 && l.x <= r.x && r.x <= l.x + l.word.length - 1;
        }
    }

    private void enterWord(WordInfo wi) {
        int x;
        if (wi.direction == 'a') {
            for(x = wi.y; x < wi.y + wi.word.length; ++x) {
                this.crossword[wi.x][x] = wi.word[x - wi.y];
            }
        } else if (wi.direction == 'd') {
            for(x = wi.x; x < wi.x + wi.word.length; ++x) {
                this.crossword[x][wi.y] = wi.word[x - wi.x];
            }
        }

        this.updateWordLists();
    }

    void reset() {
        this.crossword = this.copyChars(this.startState);
        this.updateWordLists();
    }

    private char[][] copyChars(char[][] r) {
        char[][] temp = new char[r.length][r.length];

        for(int x = 0; x < r.length; ++x) {
            for(int y = 0; y < r.length; ++y) {
                temp[x][y] = r[x][y];
            }
        }

        return temp;
    }

    private boolean compareFinished(char[] l, char[] r) {
        if (l.length != r.length) {
            return false;
        } else {
            for(int i = 0; i < l.length; ++i) {
                if (l[i] != r[i]) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean compareUnfinished(char[] uf, char[] f) {
        if (uf.length != f.length) {
            return false;
        } else {
            for(int i = 0; i < uf.length; ++i) {
                if (uf[i] != ' ' && uf[i] != f[i]) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean isOnBoard(char[] s) {
        Iterator var3 = this.finishedWords.iterator();

        while(var3.hasNext()) {
            WordInfo wi = (WordInfo)var3.next();
            if (this.compareFinished(s, wi.word)) {
                return true;
            }
        }

        return false;
    }

    public boolean isValid() {
        int finishedCount = 0;
        int unfinishedCount = 0;
        this.dictionary.mark();

        int i;
        for(i = 0; i < this.finishedWords.size(); ++i) {
            if (this.dictionary.contains(((WordInfo)this.finishedWords.get(i)).word)) {
                ++finishedCount;
            }
        }

        if (finishedCount < this.finishedWords.size()) {
            this.dictionary.goBack();
            return false;
        } else {
            for(i = 0; i < this.unfinishedWords.size(); ++i) {
                if (this.dictionary.containsIncomplete(((WordInfo)this.unfinishedWords.get(i)).word)) {
                    ++unfinishedCount;
                }
            }

            if (unfinishedCount < this.unfinishedWords.size()) {
                this.dictionary.goBack();
                return false;
            } else {
                this.dictionary.goBack();
                return true;
            }
        }
    }

    public ArrayList<WordInfo> getInvalidWords() {
        ArrayList<WordInfo> invalidWords = new ArrayList();

        int i;
        for(i = 0; i < this.finishedWords.size(); ++i) {
            if (!this.dictionary.contains(((WordInfo)this.finishedWords.get(i)).word)) {
                invalidWords.add((WordInfo)this.finishedWords.get(i));
            }
        }

        for(i = 0; i < this.unfinishedWords.size(); ++i) {
            if (!this.dictionary.containsIncomplete(((WordInfo)this.unfinishedWords.get(i)).word)) {
                invalidWords.add((WordInfo)this.unfinishedWords.get(i));
            }
        }

        return invalidWords;
    }

    private ArrayList<node> getMatches(WordInfo nextWord, int c) throws Exception {
        ArrayList<node> matches = new ArrayList();
        int count = 0;
        this.dictionary.setWordLength(nextWord.word.length);

        while(this.dictionary.hasNext() && count < c) {
            char[] s = this.dictionary.next();
            if (this.compareUnfinished(nextWord.word, s) && !this.isOnBoard(s)) {
                WordInfo wi = new WordInfo((char[])s.clone(), nextWord.x, nextWord.y, nextWord.direction);
                char[][] tempCrossword = this.copyChars(this.crossword);
                this.lastWords.push(wi);
                this.enterWord(wi);
                if (this.isValid()) {
                    matches.add(new node(this.copyChars(this.crossword), (Stack)this.lastWords.clone()));
                    if (this.surrounded) {
                        return matches;
                    }

                    ++count;
                    if (count != c) {
                        this.lastWords.pop();
                    }
                } else {
                    this.crossword = this.copyChars(tempCrossword);
                    this.lastWords.pop();
                    this.updateWordLists();
                }
            }
        }

        return matches;
    }

    public void itBroad(int c) throws Exception {
        this.nodeList = new Stack();
        this.lastWords = new Stack();

        while(this.finishedWords.size() < this.depth && !this.stop) {
            Iterator var3 = this.getMatches(this.getNextWord(), c).iterator();

            while(var3.hasNext()) {
                node match = (node)var3.next();
                this.nodeList.push(match);
            }

            if (this.nodeList.isEmpty()) {
                this.reset();
                ++c;
                this.itBroad(c);
            } else {
                this.crossword = this.copyChars(((node)this.nodeList.peek()).crossword);
                this.lastWords = (Stack)((node)this.nodeList.pop()).path.clone();
                this.updateWordLists();
                Display.displayWords(this.crossword);
            }
        }

        this.done = true;
    }

    public void stop() {
        this.stop = true;
    }

    public boolean isDone() {
        return this.done;
    }

    public void fill() throws Exception {
        this.itBroad(2);
    }

    private class node {
        char[][] crossword;
        Stack<WordInfo> path;

        public node(char[][] crossword, Stack<WordInfo> path) {
            this.crossword = Crossword.this.copyChars(crossword);
            this.path = path;
        }
    }
}
