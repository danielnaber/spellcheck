package org.languagetool.spell.hunspell;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

public class DictionaryLoader {
  
  public Dictionary load(String basePath) throws IOException {
    File dicFile = new File(basePath + ".dic");
    File affFile = new File(basePath + ".aff");
    if (!dicFile.exists()) {
      throw new RuntimeException(".dic file not found: " + dicFile);
    }
    if (!affFile.exists()) {
      throw new RuntimeException(".aff file not found: " + affFile);
    }
    String encoding;
    try (InputStream aff = new FileInputStream(affFile)) {
      encoding = getEncoding(aff);
      if (encoding == null) {
        throw new RuntimeException("No encoding found in " + affFile);
      }
    }
    try (InputStream dic = new FileInputStream(dicFile);
         InputStream aff = new FileInputStream(affFile)) {
      return load(aff, dic, encoding);
    }
  }

  public Dictionary load(InputStream affixFile, InputStream dictFile, String encoding) throws IOException {
    Map<Character, List<AffixCommand>> affixMap = new AffixLoader().load(affixFile, encoding);
    return loadDict(dictFile, affixMap, encoding);
  }

  private String getEncoding(InputStream is) {
    try (Scanner s = new Scanner(is, "ASCII")) {
      while (s.hasNextLine()) {
        String line = s.nextLine().trim();
        if (line.startsWith("SET ")) {
          return line.substring(4).trim();
        }
      }
    }
    return null;
  }

  Dictionary loadDict(InputStream is, Map<Character, List<AffixCommand>> affixMap, String encoding) {
    List<Dictionary> dicts = new ArrayList<>();
    try (Scanner s = new Scanner(is, encoding)) {
      if (s.hasNextLine()) {
        s.nextLine(); // skip first line
      }
      while (s.hasNextLine()) {
        String line = s.nextLine().trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        dicts.addAll(applyFlags(line, affixMap));
      }
    }
    Dictionary merged = mergeListsToDict(dicts);
    //debug("", merged);
    return merged;
  }

  /*private void debug(String pattern, Dictionary dictionary) {
    debug(pattern, "CompoundBeginWords", dictionary.getCompoundBeginWords());
    debug(pattern, "CompoundEndWords", dictionary.getCompoundEndWords());
    debug(pattern, "NonCompoundWords", dictionary.getNonCompoundWords());
  }

  private void debug(String pattern, String name, List<String> list) {
    System.out.println("--- " + name);
    for (String l : list) {
      if (l.matches(pattern)) {
        System.out.println("  " + l);
      }
    }
  }*/

  private Dictionary mergeListsToDict(List<Dictionary> dicts) {
    Set<String> nonCompoundWords = new HashSet<>();
    Set<String> compoundBeginWords = new HashSet<>();
    Set<String> compoundEndWords = new HashSet<>();
    for (Dictionary dict : dicts) {
      nonCompoundWords.addAll(dict.getNonCompoundWords());
      compoundBeginWords.addAll(dict.getCompoundBeginWords());
      compoundEndWords.addAll(dict.getCompoundEndWords());
    }
    List<String> nonCompoundWordsSorted = new ArrayList<>(nonCompoundWords);
    Collections.sort(nonCompoundWordsSorted);
    List<String> compoundBeginWordsSorted = new ArrayList<>(compoundBeginWords);
    Collections.sort(compoundBeginWordsSorted);
    List<String> compoundEndWordsSorted = new ArrayList<>(compoundEndWords);
    Collections.sort(compoundEndWordsSorted);
    return new Dictionary(nonCompoundWordsSorted, compoundBeginWordsSorted, compoundEndWordsSorted);
  }

  private List<Dictionary> applyFlags(String wordWithOptionalFlags, Map<Character, List<AffixCommand>> affixMap) {
    List<Dictionary> dicts = new ArrayList<>();
    String[] parts = wordWithOptionalFlags.split("/");
    if (parts.length == 1) {
      dicts.add(new Dictionary(Collections.singletonList(wordWithOptionalFlags), Collections.emptyList(), Collections.emptyList()));
    } else if (parts.length == 2) {
      String word = parts[0];
      //System.out.println("##"+word);
      String flagsAsString = parts[1];
      //System.out.println("=====================================================");
      for (int i = 0; i < flagsAsString.length(); i++) {
        char flag = flagsAsString.charAt(i);
        //System.out.println("=============== " + flag);
        List<AffixCommand> affixCommands = affixMap.get(flag);
        //word = applySuffixes(words, word, affixCommands);
        //TODO: apply all?
        Dictionary dictionary = applyFlags(word, affixCommands, affixMap);
        dicts.add(dictionary);
      }
    } else {
      throw new RuntimeException("Could not load: '" + wordWithOptionalFlags + "'");
    }
    System.out.println(wordWithOptionalFlags + " ==> " + mergeListsToDict(dicts));
    return dicts;
  }

  @SuppressWarnings("AssignmentToMethodParameter")
  private Dictionary applyFlags(String word, List<AffixCommand> affixCommands, Map<Character, List<AffixCommand>> affixMap) {
    String orig = word;
    List<String> words = new ArrayList<>();
    if (affixCommands != null) {
      for (AffixCommand affixCommand : affixCommands) {
        words.addAll(applyAffixCommand(word, affixCommand, affixMap));
      }
      //System.out.println(orig + " -> " + words);

      //COMPOUNDBEGIN x    kommt in de_DE nur indirekt via 'j' vor, z.B. Ähren/hij, Abkling/hij.
      //COMPOUNDMIDDLE y   kommt in de_DE nur indirekt via 'i' vor, z.B. Ähren/hij, Abkling/hij. MIDDLE = in der Mitte, nie am Ende
      // -> fürs Deutsche sind COMPOUNDBEGIN und COMPOUNDMIDDLE äquivalent und treten immer zusammen auf
      //COMPOUNDEND z      z.B. äcker/Nozm, plakette/Nozm
      //ONLYINCOMPOUND o   in de_DE nur zusammen mit z  

      List<String> nonCompoundWords = new ArrayList<>();
      List<String> compoundBeginWords = new ArrayList<>();
      List<String> compoundEndWords = new ArrayList<>();

      boolean compound = false;
      if (affixCommands.stream().anyMatch(f-> f.flag == 'i' || f.flag == 'j' )) {
        //System.out.println("Adding begin words: " + words);
        compoundBeginWords.addAll(words);
        compound = true;
      }
      if (affixCommands.stream().anyMatch(f-> f.flag == 'z')) {
        //System.out.println("Adding end words: " + words);
        compoundEndWords.addAll(words);
        compound = true;
      }
      //if (!compound) {
      nonCompoundWords.addAll(words);
      //}

      // TODO: stop after first match?
      Dictionary dictionary = new Dictionary(nonCompoundWords, compoundBeginWords, compoundEndWords);
      //System.out.println(orig + " -> " + dictionary);
      return dictionary;
    }
    Dictionary dictionary = new Dictionary(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    //System.out.println(orig + " -> NO EXP.");
    return dictionary;
  }

  @SuppressWarnings("AssignmentToMethodParameter")
  List<String> applyAffixCommand(String word, AffixCommand affixCommand, Map<Character, List<AffixCommand>> affixMap) {
    //System.out.println("-> " + affixCommand);
    //System.out.println("=====applyAffixCommand");
    List<String> words = new ArrayList<>();
    Matcher m = affixCommand.getRegex().matcher(word);
    if (m.matches()) {
      
      if (affixCommand instanceof Prefix) {
        if (word.startsWith(affixCommand.getCutOff())) {
          //System.out.println("N1: " + word + ", " + (word.length()- affixCommand.getCutOff().length()) + ", cutoff: " + affixCommand.getCutOff());
          word = word.substring(affixCommand.getCutOff().length());
          //System.out.println("N2: " + word);
        }
        xxx(word, affixCommand, affixMap, words, (x, y) -> y + x);
        //System.out.println("affixCommand.getAppend(): " + affixCommand.getAppend());
        // TODO: getAppend() may contain more flags, so recursion is needed here!
        //words.add(word);
        //System.out.println(affixCommand + ": " + words + " <- " + word);
        
      } else if (affixCommand instanceof Suffix) {
        if (word.endsWith(affixCommand.getCutOff())) {
          //System.out.println("N1: " + word + ", " + (word.length()- affixCommand.getCutOff().length()) + ", cutoff: " + affixCommand.getCutOff());
          word = word.substring(0, word.length() - affixCommand.getCutOff().length());
          //System.out.println("N2: " + word);
        }
        xxx(word, affixCommand, affixMap, words, (x, y) -> x + y);
        //System.out.println(affixCommand + ": " + word + " <- " + orig);
      }
    }
    return words;
  }

  private void xxx(String word, AffixCommand affixCommand, Map<Character, List<AffixCommand>> affixMap, List<String> words, Combiner c) {
    //System.out.println("=====xxx");
    String[] parts = affixCommand.getAppend().split("/");
    if (parts.length == 1) {
      words.add(c.combine(word, affixCommand.getAppend()));
    } else {
      //
      for (int i = 0; i < parts[1].length(); i++) {
        String appendPrepend = parts[0].equals("0") ? "" : parts[0];
        char flag = parts[1].charAt(i);
        List<AffixCommand> affixCommands = affixMap.get(flag);
        if (affixCommands == null) {
          //TODO?
          //throw new RuntimeException("Flag not found: '" + flag + "' for '" + affixCommand.getAppend() + "' and word '" + word + "'");
          words.add(c.combine(word, appendPrepend));
        } else {
          for (AffixCommand command : affixCommands) {
            words.addAll(applyAffixCommand(c.combine(word, appendPrepend), command, affixMap));
          }
        }
      }
    }
  }

  @FunctionalInterface
  interface Combiner {
    String combine(String x, String y);
  }
}
