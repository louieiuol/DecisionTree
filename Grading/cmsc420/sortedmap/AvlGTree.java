package cmsc420.sortedmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cmsc420.sortedmap.AvlNode.TraversalTask;

@SuppressWarnings("unchecked")
public class AvlGTree<K, V> extends AbstractMap<K, V> implements SortedMap<K, V> {

    private final Comparator<? super K> mComparator;
    /* package */public AvlNode<K, V> mRoot = null;
    /* package */final int g;

    public AvlGTree(final Comparator<? super K> comparator, final int g) {
        mComparator = comparator;
        this.g = g;
    }

    public AvlGTree() {
        this(null, 1);
    }

    public AvlGTree(final int g) {
        this(null, g);
    }

    public Node createXml(final Node parent) {
        final Element root = parent.getOwnerDocument().createElement("AvlGTree");
        root.setAttribute("height", mRoot == null ? "0" : String.valueOf(mRoot.mHeight));
        root.setAttribute("maxImbalance", String.valueOf(g));
        root.setAttribute("cardinality", String.valueOf(size()));
        root.appendChild(mRoot == null ?
                parent.getOwnerDocument().createElement("emptyChild") :
                mRoot.buildXmlNode(root));
        return root;
    }

    /**
     * If a Comparator is present, compares based on that comparator. Otherwise
     * a canonical ordering is used. Nulls are made high.
     * 
     * Since I never remember: key1 > key2 => this returns greater than zero.
     * key1 < key2 => this returns less than zero
     * 
     * @param key1
     *            is the first key to compare.
     * @param key2
     *            is the second key to compare
     * @return an integer that represents the comparison as above.
     * @throws ClassCastException
     *             if no Comparator is provided but the keys are not mutually
     *             comparable.
     */
    /* package */int keyCompare(final K key1, final K key2) {
        if (key1 == null) return -1;
        else if (key2 == null) return 1;
        else if (mComparator == null) return ((Comparable<? super K>) key1).compareTo(key2);
        else return mComparator.compare(key1, key2);
    }

    @Override
    public void clear() {
        mRoot = null;
    }

    @Override
    public boolean containsKey(final Object key) {
        return mRoot != null && mRoot.contains(this, (K) key);
    }

    @Override
    public boolean containsValue(final Object val) {
        return mRoot != null && mRoot.inOrderTraverse(new TraversalTask<K, V, Boolean>() {
            @Override
            public Boolean visitItem(final K key, final V value, final Boolean accumulator) {
                return accumulator || value.equals(val);
            }
        }, false);
    }

    @Override
    public V get(final Object key) {
        return mRoot == null ? null : mRoot.get(this, (K) key);
    }

    @Override
    public boolean isEmpty() {
        return mRoot == null;
    }

    @Override
    public V put(final K key, final V value) {
        final V originalValue;
        if (mRoot == null) {
            mRoot = new AvlNode<K, V>(key, value);
            originalValue = null;
        } else if (mRoot != null) {
            originalValue = mRoot.put(this, key, value);
            mRoot = mRoot.rebalance(g);
            mRoot.balanceFactor();
        } else originalValue = null;
        return originalValue;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> other) {
        for (final Map.Entry<? extends K, ? extends V> item : other.entrySet()) {
            put(item.getKey(), item.getValue());
        }
    }

    @Override
    public V remove(final Object key) {
    	V originalValue = null;
        if (mRoot == null) {
            return null;
        } else if (mRoot != null) {
            if(this.keyCompare((K) key, mRoot.mKey) == 0){
            	originalValue = mRoot.mValue;
            	if(mRoot.left != null && mRoot.right != null){
            		AvlNode<K,V> oldRoot = mRoot;
            		originalValue = oldRoot.mValue;
            		mRoot = AvlNode.rotate(mRoot, mRoot.right);
            		mRoot.left = AvlNode.rotate(mRoot.left, mRoot.left.left);
            		mRoot.left.right = mRoot.left.right.moveDown(this, oldRoot);
            		mRoot.left = mRoot.left.rebalance(g);
            		mRoot.left.balanceFactor();
            		mRoot = mRoot.rebalance(g);
            		mRoot.balanceFactor();
            	}
            	else if(mRoot.left != null){
            		AvlNode<K,V> oldRoot = mRoot;
            		originalValue = oldRoot.mValue;
            		mRoot = AvlNode.rotate(mRoot, mRoot.left);
            		mRoot.right = mRoot.right.moveDown(this, oldRoot);
            		mRoot = mRoot.rebalance(g);
            		mRoot.balanceFactor();
            	} else if(mRoot.right != null){
            		AvlNode<K,V> oldRoot = mRoot;
            		originalValue = oldRoot.mValue;
            		mRoot = AvlNode.rotate(mRoot, mRoot.right);
            		mRoot.left = mRoot.left.moveDown(this, oldRoot);
            		mRoot = mRoot.rebalance(g);
            		mRoot.balanceFactor();
            	}else{
            		mRoot = null;
            	}
            }else{
            	
            	originalValue = mRoot.remove(this, (K) key);
            	mRoot = mRoot.rebalance(g);
            	mRoot.balanceFactor();
            }
        } 
        return originalValue;
    }

    @Override
    public int size() {
        return mRoot == null ? 0 : mRoot.inOrderTraverse(new TraversalTask<K, V, Integer>() {
            @Override
            public Integer visitItem(final K key, final V value, final Integer accumulator) {
                return accumulator + 1;
            }
        }, 0);
    }

    @Override
    public Comparator<? super K> comparator() {
        return mComparator;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new AvlIterator<K, V>(AvlGTree.this);
            }

            @Override
            public int size() {
                return AvlGTree.this.size();
            }

            @Override
            public boolean equals(final Object other) {
                int i = ((Collection<?>) other).size(), j = size();
                return ((Collection<?>) other).containsAll(this) && i == j;
            }
            @Override
            public boolean contains(final Object key) {
                if (key instanceof Map.Entry<?, ?>) {
                    final V value = AvlGTree.this.get(key);
                    return value != null && value.equals(((Map.Entry<?, ?>) key).getValue());
                } else return false;
            }
        };
    }

    @Override
    public K firstKey() {
        if (mRoot == null) return null;
        AvlNode<K, V> first = mRoot;
        while (first.left != null)
            first = first.left;
        return first.mKey;
    }

    @Override
    public SortedMap<K, V> headMap(final K key) {
        return new RestrictedMap<K, V>(this, null, key);
    }

    @Override
    public Set<K> keySet() {
        return new AbstractSet<K>() {

            @Override
            public Iterator<K> iterator() {
                return new AvlIterator<K, V>(AvlGTree.this).keyIterator();
            }

            @Override
            public boolean equals(final Object other) {
                int i = ((Collection<?>) other).size(), j = size();
                return ((Collection<?>) other).containsAll(this) && i == j;
            }
            @Override
            public int size() {
                return AvlGTree.this.size();
            }
        };
    }

    @Override
    public K lastKey() {
        if (mRoot == null) return null;
        AvlNode<K, V> last = mRoot;
        while (last.right != null)
            last = last.right;
        return last.mKey;
    }

    @Override
    public Collection<V> values() {
        return new AbstractSet<V>() {

            @Override
            public Iterator<V> iterator() {
                return new AvlIterator<K, V>(AvlGTree.this).valueIterator();
            }

            @Override
            public boolean equals(final Object other) {
                int i = ((Collection<?>) other).size(), j = size();
                return ((Collection<?>) other).containsAll(this) && i == j;
            }

            @Override
            public int size() {
                return AvlGTree.this.size();
            }
        };
    }

    @Override
    public SortedMap<K, V> subMap(final K start, final K end) {
        return new RestrictedMap<K, V>(this, start, end);
    }

    @Override
    public SortedMap<K, V> tailMap(final K start) {
        return new RestrictedMap<K, V>(this, start, null);
    }
}
