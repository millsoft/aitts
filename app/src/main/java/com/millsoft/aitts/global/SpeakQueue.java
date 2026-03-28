package com.millsoft.aitts.global;

import java.util.ArrayList;
import java.util.List;

public class SpeakQueue {

    int taskNumber;

    private List<SpeakTask> _list = new ArrayList<SpeakTask>();


    public SpeakQueue() {

    }

    public void addToQueue(SpeakTask st) {
        _list.add(st);

    }

    public int getCount() {
        return _list.size();
    }

    public SpeakTask getSentence() throws Exception {

        if (_list.size() == 0) {
            throw new Exception("The Queue is empty");
        }
        SpeakTask st = _list.get(0);
        _list.remove(0);

        return st;
    }


    public void clearQueue() {
        _list.clear();
        taskNumber = 0;
    }
}
