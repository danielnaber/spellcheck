package org.languagetool.spell;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.languagetool.spell.TestTools.assertSim1;

public class CompoundTreeTest {

  @Test
  public void testTemp() {
    Tree root = new Tree();
    root.add("Haus");
    root.add("Hütte");

    Tree suffixRoot = new Tree();
    suffixRoot.add("tür");
    suffixRoot.add("test");

    CompoundTree tree = new CompoundTree(root, suffixRoot);
    assertSim1("Hausxür", "Haustür", tree);
  }
  
  @Test
  public void testContainsWordCompounds1() {
    Tree root = new Tree();
    root.add("Haus");

    Tree suffixRoot = new Tree();
    suffixRoot.add("tür");

    CompoundTree tree = new CompoundTree(root, suffixRoot);
    assertTrue(tree.containsWord("Haus"));
    assertTrue(tree.containsWord("Haustür"));
  }

  @Test
  public void testContainsWordCompounds2() {
    Tree root = new Tree();
    root.add("Haus");

    Tree suffixRoot = new Tree();
    suffixRoot.add("tür");
    suffixRoot.add("test");

    CompoundTree tree = new CompoundTree(root, suffixRoot);

    assertTrue(tree.containsWord("Haus"));
    assertTrue(tree.containsWord("Haustür"));
    assertTrue(tree.containsWord("Haustürtür"));
    assertTrue(tree.containsWord("Haustürtest"));
    assertTrue(tree.containsWord("Haustesttür"));
    assertTrue(tree.containsWord("Haustesttest"));

    assertFalse(tree.containsWord("Haushaus"));
    assertFalse(tree.containsWord("Hausetest"));
    assertFalse(tree.containsWord("Hausentest"));
  }

  @Test
  public void testContainsWordCompounds3() {
    Tree root = new Tree();
    root.add("Haus");
    root.add("Haustür");

    Tree suffixRoot = new Tree();
    suffixRoot.add("tür");
    suffixRoot.add("test");

    CompoundTree tree = new CompoundTree(root, suffixRoot);

    assertTrue(tree.containsWord("Haus"));
    assertTrue(tree.containsWord("Haustür"));
    assertTrue(tree.containsWord("Haustürtür"));
    assertTrue(tree.containsWord("Haustürtest"));
    assertTrue(tree.containsWord("Haustesttür"));
    assertTrue(tree.containsWord("Haustesttest"));
    
    assertFalse(tree.containsWord("Haushaus"));
    assertFalse(tree.containsWord("Hausetest"));
    assertFalse(tree.containsWord("Hausentest"));
  }

  @Test
  public void testContainsWordCompounds4() {
    Tree root = new Tree();
    root.add("Haus");
    root.add("Hütte");

    Tree suffixRoot = new Tree();
    suffixRoot.add("tür");
    suffixRoot.add("test");

    CompoundTree tree = new CompoundTree(root, suffixRoot);

    assertTrue(tree.containsWord("Haustür"));
    assertTrue(tree.containsWord("Haustürtür"));
    assertTrue(tree.containsWord("Haustest"));
    assertTrue(tree.containsWord("Haustesttür"));
    assertTrue(tree.containsWord("Haustesttest"));
    assertTrue(tree.containsWord("Hütte"));
    
    assertFalse(tree.containsWord("Hause"));
    assertFalse(tree.containsWord("Hausetest"));
    assertFalse(tree.containsWord("Hause"));
    assertFalse(tree.containsWord("Hau"));
    assertFalse(tree.containsWord("Hauss"));
    assertFalse(tree.containsWord("Hausx"));
    assertFalse(tree.containsWord("Hütten"));
  }

  @Test
  public void testRealContainsWordCompounds() {
    Tree root = new Tree();
    root.add("Arbeit", Tree.EndBehavior.MustEnd);
    root.add("Arbeits", Tree.EndBehavior.CannotEnd);
    root.add("Test", Tree.EndBehavior.CanEnd);

    Tree suffixRoot = new Tree();
    suffixRoot.add("arbeit", Tree.EndBehavior.MustEnd);
    suffixRoot.add("arbeits", Tree.EndBehavior.CannotEnd);
    suffixRoot.add("test", Tree.EndBehavior.CanEnd);

    CompoundTree tree = new CompoundTree(root, suffixRoot);

    assertTrue(tree.containsWord("Arbeit"));
    assertTrue(tree.containsWord("Test"));
    assertTrue(tree.containsWord("Arbeitstest"));  // Problem: backtracking is broken, this should be accepted as "Arbeits" + "test" is correct
    assertTrue(tree.containsWord("Testarbeit"));
    assertTrue(tree.containsWord("Arbeitstestarbeit"));
    assertTrue(tree.containsWord("Testarbeitstest"));

    assertFalse(tree.containsWord("Arbeits"));
    assertFalse(tree.containsWord("Testarbeits"));
    assertFalse(tree.containsWord("Arbeittest"));
    assertFalse(tree.containsWord("Arbeits"));
    assertFalse(tree.containsWord("arbeit"));
    assertFalse(tree.containsWord("arbeits"));
    assertFalse(tree.containsWord("Tests"));
    assertFalse(tree.containsWord("Arbeitstestarbeits"));
    assertFalse(tree.containsWord("Testarbeittest"));
  }

  @Test
  public void testRealContainsWordCompounds2() {
    Tree root = new Tree();
    root.add("Abbau", Tree.EndBehavior.CanEnd);

    Tree suffixRoot = new Tree();
    suffixRoot.add("geräusch", Tree.EndBehavior.CanEnd);
    suffixRoot.add("geräuschs", Tree.EndBehavior.MustEnd);
    suffixRoot.add("geräusche", Tree.EndBehavior.MustEnd);
    suffixRoot.add("geräusches", Tree.EndBehavior.MustEnd);
    suffixRoot.add("geräuschen", Tree.EndBehavior.MustEnd);

    //System.out.println("tree : " + root);
    //System.out.println("stree: " + suffixRoot);

    CompoundTree tree = new CompoundTree(root, suffixRoot);

    assertTrue(tree.containsWord("Abbau"));
    assertTrue(tree.containsWord("Abbaugeräusche"));
    assertTrue(tree.containsWord("Abbaugeräusche"));
    assertTrue(tree.containsWord("Abbaugeräuschen"));
    assertTrue(tree.containsWord("Abbaugeräusches"));
    assertTrue(tree.containsWord("Abbaugeräuschs"));

    assertFalse(tree.containsWord("Abbausgeräuschs"));
  }

  @Test
  public void testSimilarWords() {
    Tree root = new Tree();
    root.add("Haus");
    root.add("Hütte");

    Tree suffixRoot = new Tree();
    suffixRoot.add("tür");
    suffixRoot.add("test");

    CompoundTree tree = new CompoundTree(root, suffixRoot);

    assertSim1("xaus", "Haus", tree);
    assertSim1("Hxus", "Haus", tree);
    assertSim1("Haxs", "Haus", tree);
    assertSim1("Haux", "Haus", tree);

    // dist too large:
    assertSim1("xxus", "", tree);
    assertSim1("Hxxs", "", tree);
    assertSim1("Haxx", "", tree);
  }

  @Test
  public void testSimilarCompoundWords() {
    Tree root = new Tree();
    root.add("Haus");
    root.add("Hütte");

    Tree suffixRoot = new Tree();
    suffixRoot.add("tür");
    suffixRoot.add("test");

    CompoundTree tree = new CompoundTree(root, suffixRoot);

    assertSim1("xaustür", "Haustür", tree);
    assertSim1("Hxustür", "Haustür", tree);
    assertSim1("Haxstür", "Haustür", tree);
    assertSim1("Hauxtür", "Haustür", tree);
    assertSim1("Hausxür", "Haustür", tree);
    assertSim1("Haustxr", "Haustür", tree);
    assertSim1("Haustüx", "Haustür", tree);

    // dist too large:
    assertSim1("xxustür", "", tree);
    assertSim1("Hxxstür", "", tree);
    assertSim1("Haxxtür", "", tree);
    assertSim1("Hauxxür", "", tree);
    assertSim1("Hausxxr", "", tree);
    assertSim1("Haustxx", "", tree);
  }

}