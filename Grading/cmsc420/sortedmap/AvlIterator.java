package cmsc420.sortedmap;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Stack;

public class AvlIterator<K, V> implements Iterator<Map.Entry<K, V>> {

    private final Stack<AvlNode<K, V>> mItemsToVisit = new Stack<AvlNode<K, V>>();
    private final AvlGTree<K, V> mTree;
    private AvlEntry justPopped;

    public AvlIterator(final AvlGTree<K, V> tree) {
        mTree = tree;
        pushAll(mTree.mRoot);
    }

    private void pushAll(AvlNode<K, V> item) {
        while (item != null) {
            mItemsToVisit.push(item);
            item = item.left;
        }
    }

    @Override
    public boolean hasNext() {
        return !mItemsToVisit.isEmpty();
    }

    @Override
    public Entry<K, V> next() {
        if (!hasNext()) throw new NoSuchElementException();
        justPopped = new AvlEntry(mItemsToVisit.pop());
        pushAll(justPopped.mItem.right);
        return justPopped;
    }

    @Override
    public void remove() {
        if (justPopped != null) {
            mTree.remove(justPopped.getKey());
            justPopped = null;
        } else throw new IllegalStateException();
    }

    public Iterator<K> keyIterator() {
        return new Iterator<K>() {

            @Override
            public boolean hasNext() {
                return AvlIterator.this.hasNext();
            }

            @Override
            public K next() {
                return AvlIterator.this.next().getKey();
            }

            @Override
            public void remove() {
                AvlIterator.this.remove();
            }

        };
    }

    public Iterator<V> valueIterator() {
        return new Iterator<V>() {

            @Override
            public boolean hasNext() {
                return AvlIterator.this.hasNext();
            }

            @Override
            public V next() {
                return AvlIterator.this.next().getValue();
            }

            @Override
            public void remove() {
                AvlIterator.this.remove();
            }

        };
    }

    private class AvlEntry implements Map.Entry<K, V> {
        private AvlNode<K, V> mItem;

        public AvlEntry(final AvlNode<K, V> item) {
            mItem = item;
        }

        @Override
        public K getKey() {
            return mItem.mKey;
        }

        @Override
        public V getValue() {
            return mItem.mValue;
        }

        @Override
        public V setValue(final V value) {
            final V oldVal = mItem.mValue;
            mItem.mValue = value;
            return oldVal;
        }

        public String toString() {
            return mItem.mKey + "=" + mItem.mValue;
        }

        public boolean equals(final Object other) {
            if (other instanceof Map.Entry) {
                return ((Map.Entry<?, ?>) other).getKey().equals(mItem.mKey)
                        && ((Map.Entry<?, ?>) other).getValue().equals(mItem.mValue);
            } else return false;
        }

        public int hashCode() {
            int keyHash = (mItem.mKey == null ? 0 : mItem.mKey.hashCode());
            int valueHash = (mItem.mValue == null ? 0 : mItem.mValue.hashCode());
            return keyHash ^ valueHash;
        }
    }
}
