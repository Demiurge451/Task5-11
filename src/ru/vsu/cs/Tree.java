package ru.vsu.cs;

import java.util.Iterator;

/**
 * Интерфейс для дерева
 */
public interface Tree<T> extends Iterable<T>{

    interface TreeNode<T> extends Iterable<T>{
        /**
         * @return Значение в узле дерева
         */
        T getValue();
    }

    /**
     * @return Вершину дерева
     */
    TreeNode<T> getRoot();

    /**
     * Реализаци Iterable
     * @return Итератор
     */
    @Override
    default Iterator<T> iterator(){return this.getRoot().iterator();}
}
