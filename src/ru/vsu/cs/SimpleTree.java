package ru.vsu.cs;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Реализовать дерево, поддерживающее произвольное кол-во потомков в узле.
 * Реализовать для данного дерева процедуру "переворота" дерева справа налево.
 * @param <T>
 */
public class SimpleTree<T> implements Tree<T> {

    protected class SimpleTreeNode implements Tree.TreeNode<T> {
        public T value;
        public List<SimpleTreeNode> children = new ArrayList<>();

        public SimpleTreeNode(T value) {
            this.value = value;
        }

        @Override
        public T getValue() {
            return this.value;
        }

        @Override
        public Iterator<T> iterator() {
            return null;
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            TreeNode.super.forEach(action);
        }
    }

    protected SimpleTreeNode root = null;

    protected Function<String, T> fromStrFunc;
    protected Function<T, String> toStrFunc;

    public SimpleTree(Function<String, T> fromStrFunc, Function<T, String> toStrFunc) {
        this.fromStrFunc = fromStrFunc;
        this.toStrFunc = toStrFunc;
    }

    public SimpleTree(Function<String, T> fromStrFunc) {
        this(fromStrFunc, Object::toString);
    }

    @Override
    public TreeNode<T> getRoot() {
        return root;
    }


    private T fromStr(String s) throws Exception {
        s = s.trim();
        if (s.length() > 0 && s.charAt(0) == '"') {
            s.substring(1);
        }
        if (s.length() > 0 && s.charAt(s.length() - 1) == '"') {
            s.substring(0, s.length() - 1);
        }
        if (fromStrFunc == null) {
            throw new Exception("Не определена функция конвертации строки в T");
        }
        return fromStrFunc.apply(s);
    }

    private static class IndexWrapper {
        public int index = 0;
    }

    private void skipSpaces(String bracketStr, IndexWrapper iw) {
        while (iw.index < bracketStr.length() && Character.isWhitespace(bracketStr.charAt(iw.index))) {
            iw.index++;
        }
    }

    private T readValue(String bracketStr, IndexWrapper iw) throws Exception {
        skipSpaces(bracketStr, iw);
        if (iw.index >= bracketStr.length()) {
            return null;
        }
        int from = iw.index;
        boolean quote = bracketStr.charAt(iw.index) == '"';
        if (quote) {
            iw.index++;
        }
        while (iw.index < bracketStr.length() && (
                quote && bracketStr.charAt(iw.index) != '"' ||
                        !quote && !Character.isWhitespace(bracketStr.charAt(iw.index)) && "(),".indexOf(bracketStr.charAt(iw.index)) < 0)) {
            iw.index++;
        }

        if (quote && bracketStr.charAt(iw.index) == '"') {
            iw.index++;
        }

        String valueStr = bracketStr.substring(from, iw.index);
        T value = fromStr(valueStr);
        skipSpaces(bracketStr, iw);
        return value;
    }

    private SimpleTreeNode fromBracketStr(String bracketStr, IndexWrapper iw) throws Exception {
        T parentValue = readValue(bracketStr, iw);
        SimpleTreeNode parentNode = new SimpleTreeNode(parentValue);
        if (bracketStr.charAt(iw.index) == '(') {
            iw.index++;
            skipSpaces(bracketStr, iw);
            while (bracketStr.charAt(iw.index) != ')'){
                if (bracketStr.charAt(iw.index) != ',') {
                    parentNode.children.add(fromBracketStr(bracketStr, iw));
                    skipSpaces(bracketStr, iw);
                }
                if (bracketStr.charAt(iw.index) == ',') {
                    iw.index++;
                    skipSpaces(bracketStr, iw);
                }
            }

            if(iw.index > bracketStr.length()){
                throw new Exception("Неправильная входная строка");
            }
            iw.index++;
        }

        return parentNode;
    }

    public void fromBracketNotation(String bracketStr) throws Exception {
        IndexWrapper iw = new IndexWrapper();
        SimpleTreeNode root  = fromBracketStr(bracketStr, iw);
        if (iw.index < bracketStr.length()){
            throw new Exception(String.format("Ожидался конец стркои [%d]", iw.index));
        }
        this.root = root;
    }

    private void reverseList(List<T> list){
        for (int i = 0; i < list.size() / 2; i++) {
            T tmp = list.get(i);
            list.set(i, list.get(list.size() - 1 - i));
            list.set(list.size() - 1 - i, tmp);
        }
    }



    public void reverseTreeDFS(SimpleTreeNode parentNode){
        reverseList((List<T>) parentNode.children);
        for (SimpleTreeNode node: parentNode.children
             ) {
            reverseTreeDFS(node);
        }
    }

    public void reverseTreeBFS(SimpleTreeNode parentNode){
        Queue<SimpleTreeNode> queue = new LinkedList<>();

        queue.add(parentNode);
        while (queue.size() > 0){
            SimpleTreeNode current = queue.poll();
            reverseList((List<T>) current.children);
            for (SimpleTreeNode node : current.children){
                if (queue.contains(node)){
                    continue;
                }
                queue.add(node);
            }
        }
    }

    static int countSpaces = 0;

    public void treeToString(SimpleTreeNode parentNode){
        String res = parentNode.value.toString();
        for (int i = 0; i < countSpaces; i++) {
            res = "  " + res;
        }
        System.out.println(res);
        countSpaces++;
        for(SimpleTreeNode node: parentNode.children){
            treeToString(node);
        }
        countSpaces--;
    }


    public static void main(String[] args) throws Exception {
        String s = new String("1 (2(3, 4 , 5), 6(7))");
        SimpleTree<Integer> tree1 = new SimpleTree<>(Integer::parseInt);
        tree1.fromBracketNotation(s);
        tree1.treeToString(tree1.root);
        System.out.println("------------------------");
        tree1.reverseTreeBFS(tree1.root);
        tree1.treeToString(tree1.root);


        System.out.println("------------------------");

        String s2 = new String("A(A1B1(AB1C1, AB1C2, AB1C3), AB2(AB2C1(AB2C1D1)), AB3)");
        SimpleTree<String> tree2 = new SimpleTree<>(String::toString);
        tree2.fromBracketNotation(s2);
        tree2.treeToString(tree2.root);
        System.out.println("------------------------");
        tree2.reverseTreeBFS(tree2.root);
        tree2.treeToString(tree2.root);
    }
}
