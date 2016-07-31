package org.languagetool.spell;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TreeResultTest {

  private static final File DICT_FILE = new File("/media/Data/german_dict_jan_schreiber/german.dic");
  private static final int DICT_SIZE = 100_000;
  private static final int MAX_DIST = 2;

  @Test
  @Ignore("needs local file, no asserts")
  public void testCheck() throws IOException {
    List<String> lines = Files.readAllLines(DICT_FILE.toPath());
    System.out.println("=== Test set size: " + DICT_SIZE + " ===");
    Tree root = TestTools.makeTreeWithSkippingEntries(lines, DICT_SIZE, 1);
    List<String> testLines = lines.subList(0, DICT_SIZE);
    int notFound = 0;
    for (String line : testLines) {
      boolean contained = root.containsWord(line);
      System.out.println((contained ? "Y" : "N") + " " + line);
      if (!contained) {
        List<String> similarWords = root.getSimilarWords(line, MAX_DIST);
        System.out.println("  " + similarWords);
        notFound++;
      }
    }
    System.out.println("Not found: " + notFound);
  }

  @Test
  @Ignore("interactive use only, no asserts")
  public void testCheckSingleWord() throws IOException {
    File dictFile = new File("/media/Data/german_dict_jan_schreiber/german_small.dic");
    List<String> lines = Files.readAllLines(dictFile.toPath());
    Tree root = TestTools.makeTreeWithSkippingEntries(lines, DICT_SIZE, 1);
    String word = "Abbaugeräuschel";
    boolean contained = root.containsWord(word);
    System.out.println((contained ? "Y" : "N") + " " + word);
    if (!contained) {
      List<String> similarWords = root.getSimilarWords(word, MAX_DIST);
      System.out.println("  " + similarWords);
    }
  }

}
