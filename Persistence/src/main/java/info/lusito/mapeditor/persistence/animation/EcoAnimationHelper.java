package info.lusito.mapeditor.persistence.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EcoAnimationHelper {

    public float totalDuration;
    public float[] durationArray;
    public int[] indexArray;
    public EcoAnimation.Mode mode;
    public int indexLimit;
    private double lastStateTimeRandom;
    private double randomTimeWait = -1;
    private int lastRandomIndex;
    private final Random random;
    
    public EcoAnimationHelper(Random random) {
        this.random = random;
    }

    public void setup(String durations, String indexes, int indexLimit, EcoAnimation.Mode mode) {
        indexArray = parseIndexes(indexes, indexLimit, false);
        if(indexArray != null)
            indexLimit = indexArray.length;
        durationArray = parseDurations(durations, indexLimit, false);
        this.mode = mode;
        this.indexLimit = indexLimit;
        totalDuration = 0;
        for (float duration : durationArray) {
            assert (duration >= 0);
            totalDuration += duration;
        }
        if (mode == EcoAnimation.Mode.LOOP_PINGPONG) {
            totalDuration *= 2;
        }
    }

    private int getFrameNum(double t) {
        double dt = 0;
        for (int i = 0; i < durationArray.length; i++) {
            dt += durationArray[i];
            if (dt >= t) {
                return i;
            }
        }
        return indexLimit - 1;
    }
    
    public boolean isValid() {
        if(mode != null && durationArray != null && totalDuration > 0) {
            return indexArray == null || indexArray.length == durationArray.length;
        }
        return false;
    }

    public int getFrameIndex(double stateTime) {
        assert (isValid());

        double correctedStateTime = stateTime;
        switch (mode) {
            case LOOP_PINGPONG:
            case LOOP:
            case LOOP_REVERSED:
            case LOOP_RANDOM:
                correctedStateTime %= totalDuration;
                break;
        }
        int index;
        switch (mode) {
            case LOOP_PINGPONG:
                if (correctedStateTime > totalDuration * 0.5) {
                    correctedStateTime = totalDuration - correctedStateTime;
                }
            default:
            case NORMAL:
            case LOOP:
                index = getFrameNum(correctedStateTime);
                break;
            case REVERSED:
            case LOOP_REVERSED:
                index = indexLimit - getFrameNum(correctedStateTime) - 1;
                break;
            case LOOP_RANDOM:
                double deltaTime;
                if(lastStateTimeRandom <= correctedStateTime)
                    deltaTime = correctedStateTime - lastStateTimeRandom;
                else
                    deltaTime = correctedStateTime + (totalDuration - lastStateTimeRandom);
                randomTimeWait -= deltaTime;
                if(randomTimeWait <= 0) {
                    index = random.nextInt(indexLimit);
                    lastRandomIndex = index;
                    randomTimeWait = durationArray[index];
                } else {
                    index = lastRandomIndex;
                }
                lastStateTimeRandom = correctedStateTime;
                break;
        }
        if(indexArray == null)
            return index;
        return indexArray[index];
    }

    public static int[] parseIndexes(String text, int limit, boolean throwException) {
        try {
            text = text.trim();
            if(text.isEmpty()) {
                return null;
            }
            String[] parts = text.split(",");
            List<Integer> list = new ArrayList();
            for (String part : parts) {
                String[] parts2 = part.split("-");
                if(parts2.length == 2) {
                    int start = Integer.parseInt(parts2[0]);
                    int end = Integer.parseInt(parts2[1]);
                    while(start <= end) {
                        if(start < limit) {
                            list.add(start);
                        }
                        start++;
                    }
                } else {
                    final int value = Integer.parseInt(part);
                    if(value < limit) {
                        list.add(value);
                    }
                }
            }
            int[] indexArray = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                indexArray[i] = list.get(i);
            }
            return indexArray;
        } catch(NumberFormatException e) {
            if(throwException)
                throw e;
            return null;
        }
    }

    public static float[] parseDurations(String text, int indexCount, boolean throwException) {
        try {
            text = text.trim();
            String[] parts = text.split(",");
            if(parts.length == 0) {
                if(throwException)
                    throw new IllegalArgumentException("Empty value not allowed");
                return null;
            }
            float[] durations = new float[indexCount];
            int i=0;
            while(i < indexCount) {
                for (String part : parts) {
                    durations[i] = Math.max(0, Float.parseFloat(part));
                    i++;
                    if(i >= indexCount)
                        break;
                }
            }
            return durations;
        } catch(NumberFormatException e) {
            if(throwException)
                throw e;
            return null;
        }
    }
}
