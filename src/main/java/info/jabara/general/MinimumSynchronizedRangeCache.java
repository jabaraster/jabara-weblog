package info.jabara.general;

import jabara.general.ArgUtil;
import jabara.general.IProducer2;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jabaraster
 * @param <K> キャッシュから値を取得するためのキー.
 * @param <V> キャッシュしている値.
 */
public class MinimumSynchronizedRangeCache<K, V> implements IKeyValue<K, V> {

    private final IProducer2<K, V> valueCreator;

    private final Object           lock = new String();

    private Map<K, V>              map  = new HashMap<>();

    /**
     * @param pValueCreator -
     */
    public MinimumSynchronizedRangeCache(final IProducer2<K, V> pValueCreator) {
        this.valueCreator = ArgUtil.checkNull(pValueCreator, "pValueCreator"); //$NON-NLS-1$ 
    }

    /**
     * @param pKey -
     * @return -
     */
    public V get(final K pKey) {
        final V value = this.map.get(pKey);
        if (value != null) {
            return value;
        }
        synchronized (this.lock) {
            // 次の行のthis.mapは、このメソッドの先頭行のthis.mapと異なるインスタンスである可能性がある.
            // 他のスレッドが書き換えてしまっている可能性があるから.
            final Map<K, V> newMap = new HashMap<>(this.map);

            // 他のスレッドが同じキーでこのメソッドを呼び出し、this.mapを書き換えてしまっている場合は
            // newMapに値が入っているので、これを返す.
            if (newMap.containsKey(pKey)) {
                return newMap.get(pKey);
            }

            // newMapを作る処理から新しいキーを追加する処理までは
            // 同時実行スレッド数を１にする必要がある.
            // さもなければ、キーの追加し損ねが発生してしまいかねない.
            final V newValue = this.valueCreator.produce(pKey);
            newMap.put(pKey, newValue);
            this.map = newMap;
            return newValue;
        }
    }
}
