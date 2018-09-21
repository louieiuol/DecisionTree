package cmsc420.sortedmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;

@SuppressWarnings("unchecked")
public class RestrictedMap<K, V> extends AbstractMap<K, V> implements SortedMap<K, V> {
    private SortedMap<K, V> mBacker;
    private K mMin, mMax;

    public RestrictedMap(final SortedMap<K, V> backer, final K min, final K max) {
        mBacker = backer;
        mMin = min;
        mMax = max;
    }

    @Override
    public Comparator<? super K> comparator() {
        return mBacker.comparator();
    }

    @Override
    public K firstKey() {
        for (final K key : mBacker.keySet()) {
            if (overMin(key)) return key;
        }
        return null;
    }

    @Override
    public SortedMap<K, V> headMap(K arg0) {
        if (underMax(arg0)) return new RestrictedMap<K, V>(this, null, arg0);
        else throw new IllegalArgumentException();
    }

    @Override
    public K lastKey() {
        K oldKey = null;
        for (final K key : mBacker.keySet()) {
            if (underMax(key)) return oldKey;
            oldKey = key;
        }
        return oldKey;
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        if (underMax(toKey) && overMin(fromKey)) return new RestrictedMap<K, V>(this, fromKey, toKey);
        else throw new IllegalArgumentException();
    }

    @Override
    public SortedMap<K, V> tailMap(K arg0) {
        if (overMin(arg0)) return new RestrictedMap<K, V>(this, arg0, null);
        else throw new IllegalArgumentException();
    }

    public class  RestrictedIterator implements Iterator<Map.Entry<K, V>>{
    
        private Map.Entry<K, V> curr;
        private Iterator<Map.Entry<K,V>> iter;
        
        public RestrictedIterator(){
        	iter = mBacker.entrySet().iterator();
        	while(iter.hasNext()){
        		if(overMin((curr = iter.next()).getKey())){
        			break;
        		}
        	}
        }
        
        
        @Override
        public boolean hasNext() {
            return curr != null ||
                    iter.hasNext() && underMax((curr = iter.next()).getKey());
        }

        @Override
        public Map.Entry<K, V> next() {
            if (curr != null) {
                if (!underMax(curr.getKey())) throw new NoSuchElementException();
                final Map.Entry<K, V> item = curr;
                curr = null;
                return item;
            } else if (hasNext()) {
                return curr;
            } else throw new NoSuchElementException();
        }

        /*needs to implement*/
        @Override
        public void remove() {
           if(curr != null){
//        	   mBacker.remove(curr.getKey());
        	   iter.remove();
        	   curr = null;
           }
           else{
        	   throw new IllegalStateException();
           }
        }

    }
    
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
            	return new RestrictedIterator(); 
            }

            
            @Override
            public int size() {
                int i = 0;
                for (final Map.Entry<K, V> item : mBacker.entrySet()) {
                    if (overMin(item.getKey()) && underMax(item.getKey())
                            || item.getKey().equals(mMin)) i++;
                    if (!underMax(item.getKey())) break;
                }
                return i;
            }

        };
    }

    private int keyCompare(final K key1, final K key2) {
        if (key1 == null) return -1;
        else if (key2 == null) return 1;
        else if (comparator() == null) return ((Comparable<? super K>) key1).compareTo(key2);
        else return comparator().compare(key1, key2);
    }

    public int size() {
        return entrySet().size();
    }

    private boolean overMin(final K key) {
        return mMin == null || keyCompare(mMin, key) <= 0;
    }

    private boolean underMax(final K key) {
        return mMax == null || keyCompare(key, mMax) < 0;
    }

    public boolean equals(final Object other) {
        if (other == this) return true;
        else if (other instanceof RestrictedMap) {
            RestrictedMap<?, ?> otherMap = (RestrictedMap<?, ?>) other;
            return otherMap.mBacker.equals(mBacker) &&
                    mMin == null ^ mMin.equals(otherMap.mMin) &&
                    mMax == null ^ mMax.equals(otherMap.mMax);
        } else if (other instanceof Map) {
            Map<?, ?> otherMap = (Map<?, ?>) other;
            return entrySet().containsAll(otherMap.entrySet())
                    && otherMap.size() == size();
        } else return false;
    }
}
