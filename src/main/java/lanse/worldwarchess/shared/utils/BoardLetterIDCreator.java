package lanse.worldwarchess.shared.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

public class BoardLetterIDCreator implements Iterator<String> {
    private final ArrayList<Character> curState;
    public BoardLetterIDCreator() {
        curState = new ArrayList<>();
    }

    private String string() {
        return curState.stream().map(Object::toString).collect(Collectors.joining(""));
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public String next() {
        for (int i = curState.size() - 1; i >= 0; --i) {
            if (curState.get(i) != 'Z') {
                curState.set(i, (char)(curState.get(i) + (char)1));
                return string();
            }
            else {
                curState.set(i, 'A');
            }
        }
        curState.add(0, 'A');
        return string();
    }
}
