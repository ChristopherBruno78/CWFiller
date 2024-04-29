package org.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class Dictionary {
    private final String dictFile = "/FinalDict";
    final String bufFile = "/BufMap";
    TreeMap<String, Double> freqMap;
    HashMap<Integer, Integer> bufferMap;
    InputStream fis;
    FileWriter fos;
    Scanner scanner;
    FileReader fileReader;
    File file;
    BufferedWriter out;
    byte[] buf;
    int currBufPos = 0;
    int currWordSize = 0;
    int markedBufPos = 0;
    int markedWordSize = 0;
    int bufLen;

    public Dictionary() throws Exception {
        this.setupBuffer();
    }

    private void setupFreqChart() {
        this.freqMap = new TreeMap();
        this.freqMap.put("a", 0.08167);
        this.freqMap.put("b", 0.01492);
        this.freqMap.put("c", 0.02782);
        this.freqMap.put("d", 0.04253);
        this.freqMap.put("e", 0.12702);
        this.freqMap.put("f", 0.02228);
        this.freqMap.put("g", 0.02015);
        this.freqMap.put("h", 0.06094);
        this.freqMap.put("i", 0.06966);
        this.freqMap.put("j", 0.00153);
        this.freqMap.put("k", 0.00772);
        this.freqMap.put("l", 0.04025);
        this.freqMap.put("m", 0.02406);
        this.freqMap.put("n", 0.06749);
        this.freqMap.put("o", 0.07507);
        this.freqMap.put("p", 0.01929);
        this.freqMap.put("q", 9.5E-4);
        this.freqMap.put("r", 0.05987);
        this.freqMap.put("s", 0.06327);
        this.freqMap.put("t", 0.09056);
        this.freqMap.put("u", 0.02758);
        this.freqMap.put("v", 0.00987);
        this.freqMap.put("w", 0.0236);
        this.freqMap.put("x", 0.0015);
        this.freqMap.put("y", 0.01974);
        this.freqMap.put("z", 7.4E-4);
    }

    private double getFreqValue(char[] s) {
        double value = 0.0;

        for(int i = 0; i < s.length; ++i) {
            value += (Double)this.freqMap.get("" + Character.toLowerCase(s[i]));
        }

        value /= (double)s.length;
        return value;
    }

    private void setBufMap() throws Exception {
        this.bufferMap = new HashMap();
        this.bufLen = (int)(new File("/FinalDict")).length();
        this.fis = this.getClass().getResourceAsStream("/FinalDict");
        this.buf = new byte[this.bufLen];
        this.fis.read(this.buf);
        this.fis.close();
        this.currBufPos = 0;
        this.bufferMap.put(1, 0);
        int currLength = 1;

        int i;
        while(this.hasNext()) {
            i = this.currBufPos;
            char[] string = this.next();
            if (string.length > currLength) {
                currLength = string.length;
                this.bufferMap.put(currLength, i);
            }
        }

        this.fos = new FileWriter("/BufMap");

        for(i = 0; i <= 20; ++i) {
            this.fos.write(i + " " + this.bufferMap.get(i) + "\n");
        }

        this.fos.close();
    }

    private void setupBuffer() throws Exception {
        this.scanner = new Scanner(this.getClass().getResourceAsStream("/BufMap"));
        this.bufferMap = new HashMap();

        while(this.scanner.hasNextInt()) {
            int length = this.scanner.nextInt();
            int buffMark = this.scanner.nextInt();
            this.bufferMap.put(length, buffMark);
        }

        this.bufLen = 369088;
        this.fis = this.getClass().getResourceAsStream("/FinalDict");
        this.buf = new byte[this.bufLen];
        this.fis.read(this.buf);
        this.fis.close();
    }

    public void setWordLength(int size) {
        this.currWordSize = size;
        this.currBufPos = (Integer)this.bufferMap.get(size);
    }

    public boolean hasNext() {
        return this.currBufPos < (Integer)this.bufferMap.get(this.currWordSize + 1);
    }

    public char[] next() {
        char[] string = new char[this.currWordSize];

        for(int index = 0; this.buf[this.currBufPos] != 10; ++index) {
            string[index] = (char)this.buf[this.currBufPos];
            ++this.currBufPos;
        }

        ++this.currBufPos;
        return string;
    }

    public boolean contains(char[] word) {
        this.setWordLength(word.length);

        while(this.hasNext()) {
            if (this.compareChars(word, this.next())) {
                return true;
            }
        }

        return false;
    }

    public boolean containsIncomplete(char[] word) {
        this.setWordLength(word.length);

        while(this.hasNext()) {
            if (this.compareUnfinished(word, this.next())) {
                return true;
            }
        }

        return false;
    }

    public int howManyMatches(char[] word) {
        this.setWordLength(word.length);
        int matches = 0;

        while(this.hasNext()) {
            if (this.compareUnfinished(word, this.next())) {
                ++matches;
            }
        }

        return matches;
    }

    public int howManyWords(int length) {
        return ((Integer)this.bufferMap.get(length + 1) - (Integer)this.bufferMap.get(length)) / (length + 1);
    }

    boolean compareChars(char[] l, char[] r) {
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

    boolean compareUnfinished(char[] uf, char[] f) {
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

    public void mark() {
        if (this.currBufPos != 0 && this.currWordSize != 0) {
            this.markedBufPos = this.currBufPos;
            this.markedWordSize = this.currWordSize;
            this.currBufPos = (Integer)this.bufferMap.get(this.currWordSize);
        }

    }

    public void goBack() {
        this.currBufPos = this.markedBufPos;
        this.currWordSize = this.markedWordSize;
    }
}

