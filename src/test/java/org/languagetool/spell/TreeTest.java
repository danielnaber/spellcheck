package org.languagetool.spell;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.languagetool.spell.TestTools.assertSim1;

public class TreeTest {
  
  private static final File DICT_FILE = new File("/media/Data/german_dict_jan_schreiber/german.dic");

  // 1. Wörter erkennen -> DONE
  // 2. ähnliche Wörter finden -> DONE
  // 3. Komposita erkennen -> DONE
  // 4. ähnliche Komposita finden -> mostly done
  // 5. Improve performance
  // 6. parse hunspell dicts
  
  @Test
  public void testTemp() {
    
  }
  
  @Test
  @Ignore("needs local file")
  public void testLargeFile() throws IOException {
    List<String> lines = Files.readAllLines(DICT_FILE.toPath());
    Tree root = TestTools.makeTreeWithSkippingEntries(lines, 250_000, 0.95f);
    assertTrue(root.containsWord("Charakterwahl"));
    assertSim1("Charakterwal", "Charakterwahl", root);
  }
  
  @Test
  public void testContainsWord() {
    Tree root = new Tree();
    root.add("Haus");
    root.add("Haut");

    assertTrue(root.containsWord("Haus"));
    assertTrue(root.containsWord("Haut"));

    assertFalse(root.containsWord("Hau"));
    assertFalse(root.containsWord("Hausx"));
    assertFalse(root.containsWord("Hautx"));
    assertFalse(root.containsWord("Hausen"));
    assertFalse(root.containsWord("Hausenx"));
    assertFalse(root.containsWord(""));
    assertFalse(root.containsWord("x"));
  }

  @Test
  public void testContainsWordDifferentWordLength() {
    Tree root = new Tree();
    root.add("Haus");
    root.add("Hauses");
    assertTrue(root.containsWord("Haus"));
    assertTrue(root.containsWord("Hauses"));
  }

  @Test
  public void testContainsWordDifferentWordLength2() {
    Tree root = new Tree();
    root.add("Haus");
    root.add("Hauses");
    root.add("Hausen");
    assertTrue(root.containsWord("Haus"));
    assertTrue(root.containsWord("Hauses"));
    assertTrue(root.containsWord("Hausen"));
    assertFalse(root.containsWord("Hause"));
    assertFalse(root.containsWord("Hausens"));
  }

  @Test
  public void testGetSimilarWords() {
    Tree root = new Tree();
    root.add("Haus");
    root.add("Haut");
    root.add("Hase");
    root.add("Fusel");

    assertSim1("Haus", "Haus", root);   // TODO: both???
    assertSim1("Haut", "Haut", root);   // TODO: both???
    assertSim1("Hasen", "Hase", root);
    assertSim1("Hasee", "Hase", root);
    assertSim1("Hasse", "Hase", root);
    assertSim1("Haase", "Hase", root);
    assertSim1("Hhase", "Hase", root);
    assertSim1("ase", "Hase", root);
    assertSim1("Hse", "Hase", root);
    assertSim1("Hae", "Hase", root);
    assertSim1("Has", "Hase", root);
    //TODO:
    //assertSim1("Haes", "Hase", root));
    //assertSim1("Hsae", "Hase", root));
    assertSim1("fusel", "Fusel", root);
    
    assertSim1("xaut", "Haut", root);
    assertSim1("Hxut", "Haut", root);
    assertSim1("Haxt", "Haut", root);
    assertSim1("Haux", "Haus, Haut", root);

    // dist too large:
    assertSim1("xxut", "", root);
    assertSim1("Haxx", "", root);
    assertSim1("xxxx", "", root);
    assertSim1("x", "", root);
    assertSim1("Ha", "", root);
    assertSim1("ut", "", root);
  }
  
}