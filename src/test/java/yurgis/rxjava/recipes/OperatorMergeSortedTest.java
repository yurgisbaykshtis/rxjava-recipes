package yurgis.rxjava.recipes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import rx.Observable;
import rx.functions.Action1;
import rx.internal.util.RxRingBuffer;
import yurgis.rxjava.recipes.OperatorMergeSorted;

public class OperatorMergeSortedTest {
  
  final static Comparator<Integer> naturalComparator = new Comparator<Integer>() {

    @Override
    public int compare(Integer o1, Integer o2) {
      return o1.compareTo(o2);
    }
    
  };

  final static Comparator<Integer> reverseComparator = new Comparator<Integer>() {

    @Override
    public int compare(Integer o1, Integer o2) {
      return o2.compareTo(o1);
    }
    
  };
  
  @Test
  public void testTwo() {
    Observable<Integer> o1 = Observable.just(2, 4, 6, 8, 10, 20);
    Observable<Integer> o2 = Observable.just(1, 3, 5, 7, 9,  99);
    Iterator<Integer> iter = Observable.just(o1, o2)
        .lift(new OperatorMergeSorted<Integer>(naturalComparator))
        .toBlocking().toIterable().iterator();
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(1, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(2, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(3, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(4, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(5, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(6, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(7, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(8, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(9, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(10, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(20, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(99, (int)iter.next());
    Assert.assertFalse(iter.hasNext());
  }

  @Test
  public void testNoSources() {
    Iterator<Integer> iter = Observable.<Observable<Integer>>empty()
        .lift(new OperatorMergeSorted<Integer>(naturalComparator))
        .toBlocking().toIterable().iterator();
    Assert.assertFalse(iter.hasNext());
    
  }
  
  @Test
  public void testTwoEmpty() {
    Iterator<Integer> iter = Observable.just(Observable.<Integer>empty(), Observable.<Integer>empty())
        .lift(new OperatorMergeSorted<Integer>(naturalComparator))
        .toBlocking().toIterable().iterator();
    Assert.assertFalse(iter.hasNext());
    
  }
  
  @Test
  public void testTwoWithOneEmpty() {
    Observable<Integer> o1 = Observable.just(1, 2, 3);
    Iterator<Integer> iter = Observable.just(o1, Observable.<Integer>empty())
        .lift(new OperatorMergeSorted<Integer>(naturalComparator))
        .toBlocking().toIterable().iterator();
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(1, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(2, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(3, (int)iter.next());
    Assert.assertFalse(iter.hasNext());
  }

  @Test
  public void testOneSource() {
    Observable<Integer> o1 = Observable.just(1, 2, 3);
    Iterator<Integer> iter = Observable.just(o1)
        .lift(new OperatorMergeSorted<Integer>(naturalComparator))
        .toBlocking().toIterable().iterator();
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(1, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(2, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(3, (int)iter.next());
    Assert.assertFalse(iter.hasNext());
  }
  
  @Test
  public void testTwoWithOneDelayed() {
    Observable<Integer> o1 = Observable.range(101, 100);
    Observable<Integer> o2 = Observable.range(1, 100).delay(1000, TimeUnit.MILLISECONDS);
    Iterator<Integer> iter = Observable.just(o1, o2)
        .lift(new OperatorMergeSorted<Integer>(naturalComparator))
        .toBlocking().toIterable().iterator();
    for (int i = 1; i <= 200; i++) {
      Assert.assertTrue(iter.hasNext());
      Assert.assertEquals(i, (int)iter.next());
    }
    Assert.assertFalse(iter.hasNext());
  }

  @Test
  public void testMany() {
    List<Observable<Integer>> many = new ArrayList<Observable<Integer>>();
    int n = 100;
    for (int i = 0; i < n; i++) {
      //the lesser item's value the longer it will be delayed
      many.add(Observable.range(i * n, n).delay((n - i)*10, TimeUnit.MILLISECONDS));
    }
    Iterator<Integer> iter = Observable.from(many)
        .lift(new OperatorMergeSorted<Integer>(naturalComparator))
        .toBlocking().toIterable().iterator();
    for (int i = 0; i < n * n; i++) {
      Assert.assertTrue(iter.hasNext());
      int value = iter.next();
      Assert.assertEquals(i, value);
    }
    Assert.assertFalse(iter.hasNext());
  }
  
  @Test
  public void testTwoReverse() {
    Observable<Integer> o1 = Observable.just(5, 3, 1);
    Observable<Integer> o2 = Observable.just(6, 4, 2);
    Iterator<Integer> iter = Observable.just(o1, o2)
        .lift(new OperatorMergeSorted<Integer>(reverseComparator))
        .toBlocking().toIterable().iterator();
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(6, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(5, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(4, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(3, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(2, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(1, (int)iter.next());
    Assert.assertFalse(iter.hasNext());
  }

  @Test
  public void testDefaultComparator() {
    Observable<Integer> o1 = Observable.just(1, 3, 5);
    Observable<Integer> o2 = Observable.just(2, 4, 6);
    Iterator<Integer> iter = Observable.just(o1, o2)
        .lift(new OperatorMergeSorted<Integer>())
        .toBlocking().toIterable().iterator();
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(1, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(2, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(3, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(4, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(5, (int)iter.next());
    Assert.assertTrue(iter.hasNext());
    Assert.assertEquals(6, (int)iter.next());
    Assert.assertFalse(iter.hasNext());
  }

  @Test
  public void testBackpressure() {
    final AtomicInteger max1 = new AtomicInteger();
    Observable<Integer> o1 = Observable.range(0, RxRingBuffer.SIZE * 100).delay(1, TimeUnit.SECONDS)
        .doOnNext(new Action1<Integer>() {
    
          @Override
          public void call(Integer value) {
            if (value > max1.get()) {
              max1.set(value);
            }
          }
          
        });
    final AtomicInteger max2 = new AtomicInteger();
    Observable<Integer> o2 = Observable.range(RxRingBuffer.SIZE * 100, RxRingBuffer.SIZE * 100)
        .doOnNext(new Action1<Integer>() {

          @Override
          public void call(Integer value) {
            value = value - RxRingBuffer.SIZE * 100;
            if (value > max2.get()) {
              max2.set(value);
            }
          }
        });
 
    Iterator<Integer> iter = Observable.just(o1, o2)
        .lift(new OperatorMergeSorted<Integer>(naturalComparator))
        .take(RxRingBuffer.SIZE/2)
        .toBlocking().getIterator();

    for (int i = 0; i < RxRingBuffer.SIZE/2; i++) {
      Assert.assertTrue(iter.hasNext());
      Assert.assertEquals(i, (int)iter.next());
    }
    Assert.assertFalse(iter.hasNext());
    
    //make sure source observables are not fetched beyond the buffer size 
    Assert.assertTrue(max1.get() <= RxRingBuffer.SIZE);
    Assert.assertTrue(max2.get() <= RxRingBuffer.SIZE);
    
  }
}
