package org.example;

public class WordInfo {
    public char[] word;
    int x;
    int y;
    char direction;

    WordInfo() {
        this.word = new char[0];
        this.x = 0;
        this.y = 0;
        this.direction = ' ';
    }

    WordInfo(char[] word, int x, int y, char direction) {
        this.word = (char[])word.clone();
        this.x = x;
        this.y = y;
        this.direction = direction;
    }
}

