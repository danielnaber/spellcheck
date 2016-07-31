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
    Tree root = TestTools.makeTree(lines, DICT_SIZE);
    List<String> testLines = lines.subList(0, DICT_SIZE);
    for (String line : testLines) {
      boolean contained = root.containsWord(line);
      System.out.println((contained ? "Y" : "N") + " " + line);
      if (!contained) {
        List<String> similarWords = root.getSimilarWords(line, MAX_DIST);
        System.out.println("  " + similarWords);
      }
    }
  }

}
