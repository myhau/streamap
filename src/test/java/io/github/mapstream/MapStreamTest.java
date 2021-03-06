package io.github.mapstream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MapStreamTest {

    Map<Integer, Integer> fullMap;
    MapStream<Integer, Integer> fullStream;
    Map<Integer, Integer> emptyMap;
    MapStream<Integer, Integer> emptyStream;

    @Before
    public void setUp() throws Exception {
        fullMap = new HashMap<>();
        fullMap.put(1, 2);
        fullMap.put(3, 500);
        fullMap.put(5, 100);
        fullMap.put(10, 10);
        fullMap.put(-10, 50);
        fullStream = MapStream.from(fullMap);

        emptyMap = new HashMap<>();
        emptyStream = MapStream.from(emptyMap);
    }

    @Test
    public void keyStream() throws Exception {

        // when
        long emptyCount = emptyStream.keyStream().count();
        Set<Integer> fullKeySet = fullStream.keyStream().collect(toSet());

        // then
        assertEquals(emptyCount, 0);
        assertEquals(fullKeySet, fullMap.keySet());

    }

    @Test
    public void valueStream() throws Exception {
        // when
        long emptyCount = emptyStream.keyStream().count();
        Set<Integer> fullValueSet = fullStream.valueStream().collect(toSet());

        // then
        assertEquals(emptyCount, 0);
        assertEquals(fullValueSet, new HashSet<>(fullMap.values()));

    }

    @Test
    public void map() throws Exception {
        // when
        Map<Integer, Integer> mapped = Map(fullStream.map((key, value) -> PairEntry.pair(key * 10, value * 20)));
        Map<Integer, Integer> expected = mapOf(
                10, 40,
                30, 10000,
                50, 2000,
                100, 200,
                -100, 1000
        );

        // then
        assertEquals(expected, mapped);
    }

    @Test(expected = IllegalStateException.class)
    public void testMapWithKeyCollisionWithException() throws Exception {
        // given
        Map<Integer, Integer> expected = mapOf(
                1, 10000
        );

        // when
        Map<Integer, Integer> mapped = fullStream.map((key, value) -> PairEntry.pair(1, value * 20)).toMap();

        // then throw exception
    }

    @Test
    public void testMapWithKeyCollision() throws Exception {
        // given
        Map<Integer, Integer> expected = mapOf(
                1, 10000
        );

        // when
        Map<Integer, Integer> mapped = fullStream.map((key, value) -> PairEntry.pair(1, value * 20)).toMap((x, y) -> x > y ? x : y);


        // then
        assertEquals(expected, mapped);
    }

    @Test
    public void mapKeys() throws Exception {

    }

    @Test
    public void mapKeys1() throws Exception {

    }

    @Test
    public void mapValues() throws Exception {

    }

    @Test
    public void flatMap() throws Exception {

    }

    @Test
    public void flatMapKeys() throws Exception {

    }

    @Test
    public void flatMapValues() throws Exception {

    }

    @Test
    public void mapValues1() throws Exception {

    }

    @Test
    public void filter() throws Exception {

    }

    @Test
    public void filterKeys() throws Exception {

    }

    @Test
    public void filterValues() throws Exception {

    }

    private void peekPairs(Map<Integer, Integer> map) throws Exception {
        // given
        Set<Pair<Integer, Integer>> expected = map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).collect(toSet());
        Set<Pair<Integer, Integer>> actualCalledWith = new HashSet<>();

        // when
        consume(MapStream.from(map).peek((k, v) -> actualCalledWith.add(Pair.of(k, v))));

        // then
        assertEquals(expected, actualCalledWith);
    }


    @Test
    public void peekPairsFull() throws Exception {
        peekPairs(fullMap);
    }

    @Test
    public void peekPairsEmpty() throws Exception {
        peekPairs(emptyMap);
    }

    private void peekKeys(Map<Integer, Integer> map) throws Exception {
        // given
        Set<Integer> expected = map.keySet();
        Set<Integer> actualCalledWith = new HashSet<>();

        // when
        consume(MapStream.from(map).peekKeys(actualCalledWith::add));

        // then
        assertEquals(expected, actualCalledWith);
    }

    @Test
    public void peekKeysFull() throws Exception {
        peekKeys(fullMap);
    }

    @Test
    public void peekKeysEmpty() throws Exception {
        peekKeys(emptyMap);
    }

    private void peekValues(Map<Integer, Integer> map) throws Exception {
        // given
        Set<Integer> expected = new HashSet<>(map.values());
        Set<Integer> actualCalledWith = new HashSet<>();

        // when
        consume(MapStream.from(map).peekValues(actualCalledWith::add));

        // then
        assertEquals(expected, actualCalledWith);
    }

    @Test
    public void peekValuesFull() throws Exception {
        peekValues(fullMap);
    }

    @Test
    public void peekValuesEmpty() throws Exception {
        peekValues(emptyMap);
    }

    @Test
    public void distinctShouldDoNothingForEmptyStream() throws Exception {
        // when
        Map<Integer, Integer> afterDistinctMap = Map(emptyStream.distinctValues());

        // then
        Assert.assertTrue(afterDistinctMap.isEmpty());
    }

    @Test
    public void swapShouldDoNothingForEmptyStream() throws Exception {
        // when
        Map<Integer, Integer> afterSwapMap = Map(emptyStream.distinctValues());

        // then
        Assert.assertTrue(afterSwapMap.isEmpty());
    }

    @Test
    public void distinctShouldRemoveDuplicateValues() throws Exception {
        // given
        Map<Integer, Integer> withDuplicatesMap = mapOf(
                1, 2,
                2, 2,
                3, 2,
                5, 2,
                6, 5,
                7, 8,
                10, 5,
                30, 8
        );

        Map<Integer, Integer> expectedDistinctMap = mapOf(
                1, 2,
                6, 5,
                7, 8
        );
        // when
        Map<Integer, Integer> distinctMap = Map(MapStream.from(withDuplicatesMap).distinctValues());

        // then

        Assert.assertEquals(expectedDistinctMap, distinctMap);
    }

    @Test
    public void distinctShouldRemoveDuplicateValuesWithMergeFunction() throws Exception {
        // given
        Map<Integer, Integer> withDuplicatesMap = mapOf(
                1, 2,
                2, 2,
                3, 2,
                5, 2,
                6, 5,
                7, 8,
                10, 5,
                30, 8
        );

        Map<Integer, Integer> expectedDistinctMap = mapOf(
                5, 2,
                10, 5,
                30, 8
        );

        BinaryOperator<Integer> mergeFunction = (v1, v2) -> v1 > v2 ? v1 : v2;

        // when
        Map<Integer, Integer> distinctMap = Map(MapStream.from(withDuplicatesMap).distinctValues(mergeFunction));

        // then

        Assert.assertEquals(expectedDistinctMap, distinctMap);
    }

    @Test
    public void swapShouldSwapKeysWithValues() throws Exception {
        // given
        Map<Integer, Integer> withDuplicatesMap = mapOf(
                1, 2,
                2, 2,
                3, 2,
                5, 2,
                6, 5,
                7, 8,
                10, 5,
                30, 8
        );

        Map<Integer, Integer> expectedSwappedMap = mapOf(
                2, 1,
                5, 6,
                8, 7
        );
        // when
        Map<Integer, Integer> swappedMap = Map(MapStream.from(withDuplicatesMap).swap());

        // then

        Assert.assertEquals(expectedSwappedMap, swappedMap);
    }

    @Test
    public void swapShouldSwapKeysWithValuesWithMergeFunction() throws Exception {
        // given
        Map<Integer, Integer> withDuplicatesMap = mapOf(
                1, 2,
                2, 2,
                3, 2,
                5, 2,
                6, 5,
                7, 8,
                10, 5,
                30, 8
        );

        Map<Integer, Integer> expectedSwappedMap = mapOf(
                2, 5,
                5, 10,
                8, 30
        );

        BinaryOperator<Integer> mergeFunction = (v1, v2) -> v1 > v2 ? v1 : v2;

        // when
        Map<Integer, Integer> swappedMap = Map(MapStream.from(withDuplicatesMap).swap(mergeFunction));

        // then

        Assert.assertEquals(expectedSwappedMap, swappedMap);
    }

    @Test
    public void countShouldBeEqualToOriginMapSize() throws Exception {
        // expect
        assertEquals(fullMap.size(), fullStream.count());
    }

    @Test
    public void countShouldBeZeroIfEmpty() throws Exception {
        // expect
        assertEquals(0, emptyStream.count());
    }

    @Test
    public void countShouldNotCareAboutDuplicateValues() throws Exception {
        // given
        Map<Integer, Integer> inputMap = mapOf(
                1, 1,
                2, 1,
                3, 1,
                4, 1
        );

        // when
        long count = MapStream.from(inputMap).count();

        // then
        assertEquals(4, count);
    }

    @Test
    public void countShouldWorkWithSingletonMap() throws Exception {
        assertEquals(1, MapStream.from(singletonMap(1, 1)).count());
    }

    public void testMatchMethodsForEmptyStream(boolean expectedForAllMethods, BiPredicate<MapStream<Integer, Integer>, Predicate<Integer>>... matchKeyFunctions) throws Exception {
        // given
        List<Predicate<Integer>> predList =
                asList(
                        x -> true,
                        x -> false,
                        x -> x != 1,
                        x -> x == 1,
                        x -> x > 1,
                        x -> x < 1
                );

        // when

        boolean allMatch = Stream.of(matchKeyFunctions)
                .map(
                        predicateMapper -> predList.stream()
                                .map(pred -> predicateMapper.test(MapStream.from(emptyMap), pred))
                                .allMatch(booleanRes -> booleanRes == expectedForAllMethods)
                )
                .allMatch(thatTrue -> thatTrue);

        // then
        assertTrue(allMatch);
    }

    @Test
    public void allMatchShouldAlwaysBeTrueForEmptyStream() throws Exception {
        testMatchMethodsForEmptyStream(
                true,
                MapStream::allKeysMatch,
                MapStream::allValuesMatch,
                (x, pred) -> x.allMatch((f1, f2) -> pred.test(f2)),
                (x, pred) -> x.allMatch((f1, f2) -> pred.test(f1))
        );
    }

    @Test
    public void anyMatchShouldAlwaysBeFalseForEmptyStream() throws Exception {
        testMatchMethodsForEmptyStream(
                false,
                MapStream::anyKeysMatch,
                MapStream::anyValuesMatch,
                (x, pred) -> x.anyMatch((f1, f2) -> pred.test(f2)),
                (x, pred) -> x.anyMatch((f1, f2) -> pred.test(f1))
        );
    }

    @Test
    public void noneMatchShouldAlwaysBeTrueForEmptyStream() throws Exception {
        testMatchMethodsForEmptyStream(
                true,
                MapStream::noneKeysMatch,
                MapStream::noneValuesMatch,
                (x, pred) -> x.noneMatch((f1, f2) -> pred.test(f2)),
                (x, pred) -> x.noneMatch((f1, f2) -> pred.test(f1))
        );
    }

    @Test
    public void allKeysMatch() throws Exception {

    }

    @Test
    public void anyMatchForEmptyIsAlwaysFalse() throws Exception {

    }

    @Test
    public void anyValuesMatch() throws Exception {

    }

    @Test
    public void anyKeysMatch() throws Exception {

    }

    @Test
    public void noneMatch() throws Exception {

    }

    @Test
    public void noneValuesMatch() throws Exception {

    }

    @Test
    public void noneKeysMatch() throws Exception {

    }

    @Test
    public void min() throws Exception {

    }

    @Test
    public void minKey() throws Exception {

    }

    @Test
    public void minValue() throws Exception {

    }

    @Test
    public void max() throws Exception {

    }

    @Test
    public void maxKey() throws Exception {

    }

    @Test
    public void maxValue() throws Exception {

    }

    @Test
    public void findAnyValue() throws Exception {

    }

    @Test
    public void findAnyKey() throws Exception {

    }

    @Test
    public void findAny() throws Exception {

    }

    @Test
    public void forEach() throws Exception {

    }

    @Test
    public void forEach1() throws Exception {

    }

    @Test
    public void collect() throws Exception {

    }

    @Test
    public void collectKey() throws Exception {

    }

    @Test
    public void collectValue() throws Exception {

    }

    @Test
    public void collect1() throws Exception {

    }

    @Test
    public void collectKey1() throws Exception {

    }

    @Test
    public void collectValue1() throws Exception {

    }

    @Test
    public void reduce() throws Exception {

    }

    @Test
    public void reduceKey() throws Exception {

    }

    @Test
    public void reduceValue() throws Exception {

    }

    @Test
    public void reduce1() throws Exception {

    }

    @Test
    public void reduceKey1() throws Exception {

    }

    @Test
    public void reduceValue1() throws Exception {

    }

    @Test
    public void reduce2() throws Exception {

    }

    @Test
    public void reduceKey2() throws Exception {

    }

    @Test
    public void reduceValue2() throws Exception {

    }

    @Test
    public void toMap() throws Exception {

    }

    @Test
    public void keySet() throws Exception {

    }

    @Test
    public void valueSet() throws Exception {

    }

    private <K, V> void consume(MapStream<K, V> stream) {
        stream.collect(Collectors.counting());
    }

    private static <T> Map<T, T> mapOf(T... elements) {
        if (elements.length % 2 != 0) {
            throw new IllegalArgumentException("elements must be even so that map (from key->value pairs) can be built");
        }

        Map<T, T> map = new HashMap<>();

        for (int i = 0; i < elements.length - 1; i += 2) {
            map.put(elements[i], elements[i + 1]);
        }

        return map;
    }

    private static <K, V> Map<K, V> Map(MapStream<K, V> map) {
        return map.pairStream().collect(Collectors.toMap(PairEntry::k, PairEntry::v));
    }

    private static <K, V> Set<PairEntry<K, V>> Set(MapStream<K, V> map) {
        return map.pairStream().collect(toSet());
    }

    private static <K> Set<K> KeySet(MapStream<K, ?> map) {
        return map.pairStream().map(PairEntry::k).collect(toSet());
    }

    private static <V> Set<V> ValueSet(MapStream<?, V> map) {
        return map.pairStream().map(PairEntry::v).collect(toSet());
    }

}