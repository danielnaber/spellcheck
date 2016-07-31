package org.languagetool.spell;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TreePerformanceTest {

  private static final File DICT_FILE = new File("/media/Data/german_dict_jan_schreiber/german.dic");

  @Test
  @Ignore("needs local file, no asserts")
  public void testPerformance() throws IOException {
    List<String> lines = Files.readAllLines(DICT_FILE.toPath());
    int maxSimLookups = 100;  // to keep test time low 
    List<Integer> dictSizes = Arrays.asList(1000, 10_000, 100_000);
    List<Integer> maxDists = Arrays.asList(1, 2);
    for (Integer dictSize : dictSizes) {
      System.out.println("=== Test set size: " + dictSize + " ===");
      Tree root = TestTools.makeTree(lines, dictSize);
      List<String> warmupLines = lines.subList(0, 100);
      for (String line : warmupLines) {
        root.containsWord(line);
        root.getSimilarWords(line, 1);
      }
      List<String> testLines = lines.subList(0, dictSize);
      for (Integer maxDist : maxDists) {
        int wordContainsCount = 0;
        int similarCount = 0;
        long simTimeSum = 0;
        long lookupTimeSumNanos = 0;
        for (String line : testLines) {
          long lookupStartTime = System.nanoTime();
          boolean contained = root.containsWord(line);
          wordContainsCount++;
          lookupTimeSumNanos += System.nanoTime() - lookupStartTime;
          if (!contained) {
            long simStartTime = System.currentTimeMillis();
            root.getSimilarWords(line, maxDist);
            simTimeSum += System.currentTimeMillis() - simStartTime;
            if (++similarCount >= maxSimLookups) {
              break;
            }
          }
        }
        double lookupTimeSumMillis = lookupTimeSumNanos / 1000.0 / 1000.0;
        double lookupTimePerWordMillis = lookupTimeSumMillis / wordContainsCount;
        double lookupWordsPerSecond = 1000 / lookupTimePerWordMillis;
        System.out.printf(Locale.ENGLISH, "maxDist " + maxDist +
                          ": Similarity search took " + simTimeSum + "ms = %.2f" +
                          "ms per word, similarity lookups: " + similarCount + ", containsWord() time per word: %.7fms = %.2f containsWord/s\n",
                          (double)simTimeSum/similarCount, lookupTimePerWordMillis, lookupWordsPerSecond);
      }
    }
    /*
    Result 2016-07-10 12:00:
      === Test set size: 1000 ===
      Inserting 958 elements into tree took 20ms
      maxDist 1: Similarity search took 275ms = 6.55ms per word, similarity lookups: 42, lookup time per word: 0.001626ms
      maxDist 2: Similarity search took 168ms = 4.00ms per word, similarity lookups: 42, lookup time per word: 0.000831ms
      === Test set size: 10000 ===
      Inserting 9491 elements into tree took 38ms
      maxDist 1: Similarity search took 1179ms = 11.79ms per word, similarity lookups: 100, lookup time per word: 0.000135ms
      maxDist 2: Similarity search took 629ms = 6.29ms per word, similarity lookups: 100, lookup time per word: 0.000102ms
      === Test set size: 100000 ===
      Inserting 94967 elements into tree took 40ms
      maxDist 1: Similarity search took 6456ms = 64.56ms per word, similarity lookups: 100, lookup time per word: 0.000010ms
      maxDist 2: Similarity search took 5661ms = 56.61ms per word, similarity lookups: 100, lookup time per word: 0.000010ms
    
    Result 2016-07-31 after upwork patch:
      === Test set size: 1000 ===
      Inserting 958 elements into tree took 7ms
      maxDist 1: Similarity search took 66ms = 1.57ms per word, similarity lookups: 42, containsWord() time per word: 0.0009583ms = 1043499.31 containsWord/s
      maxDist 2: Similarity search took 71ms = 1.69ms per word, similarity lookups: 42, containsWord() time per word: 0.0004845ms = 2064030.35 containsWord/s
      === Test set size: 10000 ===
      Inserting 9491 elements into tree took 7ms
      maxDist 1: Similarity search took 860ms = 8.60ms per word, similarity lookups: 100, containsWord() time per word: 0.0003719ms = 2688937.33 containsWord/s
      maxDist 2: Similarity search took 666ms = 6.66ms per word, similarity lookups: 100, containsWord() time per word: 0.0003639ms = 2747641.78 containsWord/s
      === Test set size: 100000 ===
      Inserting 94967 elements into tree took 39ms
      maxDist 1: Similarity search took 5941ms = 59.41ms per word, similarity lookups: 100, containsWord() time per word: 0.0003637ms = 2749431.55 containsWord/s
      maxDist 2: Similarity search took 5962ms = 59.62ms per word, similarity lookups: 100, containsWord() time per word: 0.0003823ms = 2615743.79 containsWord/s

    For comparison: for a German text, hunspell needs 65ms per set of suggestions.
    Reproduce with: time ./src/tools/hunspell -a test.txt |grep -c "^&"
    */
  }

}