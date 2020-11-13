package com.neusoft.CharMatch;

import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Title: SkipList
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/9/2510:15
 */
public class SkipList<T> {

    private transient volatile SkipList.HeadIndex<T> head;

    final Comparator<T> comparator;

    public SkipList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /**
     * 头节点：标记层
     * @param <T>
     */
    private static class HeadIndex<T> extends Index<T> {
        // 层级
        int level;

        public HeadIndex(Node<T> node, Index<T> down, Index<T> right, int level) {
            super(node, down, right);
            this.level = level;
        }
    }

    /**
     * 索引节点：引用着真实节点
     * @param <T>
     */
    private static class Index<T> {
        // 真实节点
        Node<T> node;
        // 下指针（第一层的索引实际上是没有下指针的）
        Index<T> down;
        // 右指针
        Index<T> right;

        public Index(Node<T> node, Index<T> down, Index<T> right) {
            this.node = node;
            this.down = down;
            this.right = right;
        }
    }

    /**
     * 链表中的节点：真正存数据的节点
     * @param <T>
     */
    static class Node<T> {
        // 节点元素值
        T value;
        // 下一个节点
        Node<T> next;

        public Node(T value, Node<T> next) {
            this.value = value;
            this.next = next;
        }

        @Override
        public String toString() {
            return (value==null?"h0":value.toString()) +"->" + (next==null?"null":next.toString());
        }
    }

    /**
     * 查找元素
     * 先找到前置索引节点，再往后查找
     * @param value
     * @return
     */
    public T get(T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        Comparator<T> cmp = this.comparator;
        // 第一大步：先找到前置的索引节点
        Node<T> preIndexNode = findPreIndexNode(value, true);
        // 如果要查找的值正好是索引节点
        if (preIndexNode.value != null && cmp.compare(preIndexNode.value, value) == 0) {
            return value;
        }
        // 第二大步：再按链表的方式查找
        Node<T> q;
        Node<T> n;
        int c;
        for (q = preIndexNode;;) {
            n = q.next;
            c = cmp.compare(n.value, value);
            // 找到了
            if (c == 0) {
                return value;
            }
            // 没找到
            if (c > 0) {
                return null;
            }
            // 看看下一个
            q = n;
        }
    }

    /**
     *
     * @param value 要查找的值
     * @param contain 是否包含value的索引
     * @return
     */
    private Node<T> findPreIndexNode(T value, boolean contain) {
        /*
         * q---->r---->r
         * |     |
         * |     |
         * v     v
         * d     d
         * q = query
         * r = right
         * d = down
         */
        // 从头节点开始查找，规律是先往右再往下，再往右再往下
        Index<T> q = this.head;
        Index<T> r, d;
        Comparator<T> cmp = this.comparator;
        for(;;) {
            r = q.right;
            if (r != null) {
                // 包含value的索引，正好有
                if (contain && cmp.compare(r.node.value, value) == 0) {
                    return r.node;
                }
                // 如果右边的节点比value小，则右移
                if (cmp.compare(r.node.value, value) < 0) {
                    q = r;
                    continue;
                }
            }
            d = q.down;
            // 如果下面的索引为空了，则返回该节点
            if (d == null) {
                return q.node;
            }
            // 否则，下移
            q = d;
        }
    }

    /**
     * 添加元素
     * 不能添加相同的元素
     * @param value
     */
    public void add(T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        Comparator<T> cmp = this.comparator;
        // 第一步：先找到前置的索引节点
        Node<T> preIndexNode = findPreIndexNode(value, true);
        if (preIndexNode.value != null && cmp.compare(preIndexNode.value, value) == 0) {
            return;
        }

        // 第二步：加入到链表中
        Node<T> q, n, t;
        int c;
        for (q = preIndexNode;;) {
            n = q.next;
            if (n == null) {
                c = 1;
            } else {
                c = cmp.compare(n.value, value);
                if (c == 0) {
                    return;
                }
            }
            if (c > 0) {
                // 插入链表节点
                q.next = t = new Node<>(value, n);
                break;
            }
            q = n;
        }

        // 决定索引层数，每次最多只能比最大层数高1
        int random = ThreadLocalRandom.current().nextInt();
        // 倒数第一位是0的才建索引
        if ((random & 1) == 0) {
            int level = 1;
            // 从倒数第二位开始连续的1
            while (((random >>>= 1) & 1) != 0) {
                level++;
            }

            HeadIndex<T> oldHead = this.head;
            int maxLevel = oldHead.level;
            Index<T> idx = null;
            // 如果小于或等于最大层数，则不用再额外建head索引
            if (level <= maxLevel) {
                // 第三步1：先连好竖线
                for (int i = 1; i <= level; i++) {
                    idx = new Index<>(t, idx, null);
                }
            } else {
                // 大于了最大层数，则最多比最大层数多1
                level = maxLevel + 1;
                // 第三步2：先连好竖线
                for (int i = 1; i <= level; i++) {
                    idx = new Index<>(t, idx, null);
                }
                // 新建head索引，并连好新head到最高node的线
                HeadIndex<T> newHead = new HeadIndex<>(oldHead.node, oldHead, idx, level);
                this.head = newHead;
                idx = idx.down;
            }

            // 第四步：再连横线，从旧head开始再走一遍遍历
            Index<T> qx, r, d;
            int currentLevel;
            for (qx = oldHead, currentLevel=oldHead.level;qx != null;) {
                r = qx.right;
                if (r != null) {
                    // 如果右边的节点比value小，则右移
                    if (cmp.compare(r.node.value, value) < 0) {
                        qx = r;
                        continue;
                    }
                }
                // 如果目标层级比当前层级小，直接下移
                if (level < currentLevel) {
                    qx = qx.down;
                } else {
                    // 右边到尽头了，连上
                    idx.right = r;
                    qx.right = idx;
                    qx = qx.down;
                    idx = idx.down;
                }
                currentLevel--;
            }
        }
    }

    /**
     * 删除元素
     * @param value
     */
    public void delete(T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        Index<T> q = this.head;
        Index<T> r, d;
        Comparator<T> cmp = this.comparator;
        Node<T> preIndexNode;
        // 第一步：寻找元素
        for(;;) {
            r = q.right;
            if (r != null) {
                // 包含value的索引，正好有
                if (cmp.compare(r.node.value, value) == 0) {
                    // 纠正：顺便修正向右的索引
                    q.right = r.right;
                }
                // 如果右边的节点比value小，则右移
                if (cmp.compare(r.node.value, value) < 0) {
                    q = r;
                    continue;
                }
            }
            d = q.down;
            // 如果下面的索引为空了，则返回该节点
            if (d == null) {
                preIndexNode = q.node;
                break;
            }
            // 否则，下移
            q = d;
        }

        // 第二步：从链表中删除
        Node<T> p = preIndexNode;
        Node<T> n;
        int c;
        for (;;) {
            n = p.next;
            if (n == null) {
                return;
            }
            c = cmp.compare(n.value, value);
            if (c == 0) {
                // 找到了
                p.next = n.next;
                return;
            }
            if (c > 0) {
                // 没找到
                return;
            }
            // 后移
            p = n;
        }
    }

}
