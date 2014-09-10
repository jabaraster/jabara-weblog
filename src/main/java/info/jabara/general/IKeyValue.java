/**
 * 
 */
package info.jabara.general;

/**
 * @author jabaraster
 * @param <K>
 * @param <V>
 */
public interface IKeyValue<K, V> {

    /**
     * @param pKey -
     * @return -
     */
    V get(final K pKey);
}
