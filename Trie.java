package backend;

import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord = false;
}

public class Trie {
    private TrieNode root = new TrieNode();

    public void insert(String word) {
        if (word == null) return;
        TrieNode node = root;
        for (char ch : word.toLowerCase().toCharArray()) {
            node.children.putIfAbsent(ch, new TrieNode());
            node = node.children.get(ch);
        }
        node.isEndOfWord = true;
    }

    public List<String> autoSuggest(String prefix) {
        List<String> res = new ArrayList<>();
        if (prefix == null || prefix.length() == 0) return res;
        TrieNode node = root;
        for (char ch : prefix.toLowerCase().toCharArray()) {
            if (!node.children.containsKey(ch)) return res;
            node = node.children.get(ch);
        }
        dfs(node, prefix.toLowerCase(), res, prefix.substring(0,prefix.length()-prefix.length()));
        // but we need to reconstruct correct strings: instead just collect by walking and prefix
        collect(node, new StringBuilder(prefix.toLowerCase()), res);
        return res.size() > 10 ? res.subList(0,10) : res;
    }

    private void collect(TrieNode node, StringBuilder cur, List<String> out) {
        if (node.isEndOfWord) out.add(cur.toString());
        for (Map.Entry<Character, TrieNode> e : node.children.entrySet()) {
            cur.append(e.getKey());
            collect(e.getValue(), cur, out);
            cur.deleteCharAt(cur.length()-1);
        }
    }

    private void dfs(TrieNode node, String current, List<String> results, String originalPrefix) {
        // unused but kept
    }
}
