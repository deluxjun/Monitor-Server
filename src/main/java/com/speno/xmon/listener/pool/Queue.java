package com.speno.xmon.listener.pool;

import java.util.*;

/**
 * Title:        asysQueue
 * Description:  This class provides a synchronized queue. The capacity of the
 *               queue can be fixed or unlimited.
 * Copyright:    Copyright (c) 2001
 * Company:      windfireCom
 * @author Patrick M. Hayes
 * @version 1.0
 */

public class Queue {
  private LinkedList list=null;
  private int capacity = 0;
  private int lowWaterMark = 0;
  private boolean fixed = false;
  protected boolean full = false;

  /**
   * This constructure creates a queue of unlimited capacity
   */
  public Queue() {
    list = new LinkedList();

  }

  /**
   * This constructure creates a queue of fix capacity.
   * @param cap - the maxium number of element the queue can hold (int)
   */
  public Queue(int cap) {
    list = new LinkedList();
    capacity = (cap > 0) ? cap : 1; // at least 1
    lowWaterMark = capacity; // at least capacity
    fixed = true;
  }

  /**
   * This constructure creates a queue of fix capacity with a low water mark
   * level.
   * @param cap - the maxium number of element the queue can hold (int)
   * @param low - minium number of items in the queue before you can start
   *              adding new elements (int)
   */
  public Queue(int cap, int low) {
    list = new LinkedList();
    capacity = (cap > 0) ? cap : 1; // at least 1
    lowWaterMark = (low > 0) ? low : capacity; // at least capacity
    fixed = true;
  }

  /**
   * This method get the maxium number of elements that the queue can hold if
   * the queue is a fixed size queue. Otherwise, the method return a zero.
   * @return maxium number of elements that the queue could hold (int)
   */
  public synchronized int getCapacity() { return capacity; }

  /**
   * This method get the minium number of items in the queue before you can
   * start adding new elements if the queue is a fixed size queue. Otherwise,
   * the method return a zero.
   * @return minium number of items in the queue before you can start
   * adding new elements (int)
   */
  public synchronized int getLowWaterMark() { return lowWaterMark; }

  /**
   * This method get the element at the given index of the list.
   * @param i - the index to uses.
   * @return the object that is stored at the index.
   */
  public synchronized Object get(int i) {
    if (i < 0 || i >= list.size())
        return null;
    return list.get(i);
  }

  /**
   * This method get the number of elements in the queue at the time the method
   * is executed
   * @return the number of elements in the queue (int)
   */
  public synchronized int getSize() { return list.size(); }

  /**
   * This method determines if queue is empty
   * @return true if the queue is empty otherwise returns false (boolean)
   */
  public synchronized boolean isEmpty() { return list.isEmpty(); }

  /**
   * This method determines if queue is full. This method always returns false
   * if the queue is of unlimited capacity.
   * @return true if the queue is full otherwise returns false (boolean)
   */
  public synchronized boolean isFull() { return (fixed == false) ? false : full; }

  /**
   * This method adds an object to the queue
   * @param the object to add (Object)
   * @return true if the list changed (boolean)
   */
  public synchronized boolean add(Object obj)  throws InterruptedException {
    waitWhileFull(); // waits while the queue is full only if the queue is
                     // a fixed capacity queue

    boolean changed = list.add(obj);
    if (list.size() == this.getCapacity())
      full = true;
    notifyAll();
    return changed;
  }

  /**
   * This method return and removes an object from the queue
   * @return the object that has been removed from the queue (Object)
   */
  public synchronized Object remove() throws InterruptedException {
    waitWhileEmpty();

    Object obj = list.removeFirst();
    if (full && list.size() < this.getLowWaterMark())
      full = false;

    notifyAll();
    return(obj);
  }

  /**
   * This method return and removes an object from the queue without waiting.
   * @return the object that has been removed from the queue (Object)
   */
  public synchronized Object removeNoWait() throws InterruptedException {
    Object obj = list.removeFirst();
    if (full && list.size() < this.getLowWaterMark())
      full = false;
    return(obj);
  }

  /**
   * This method removes all objects from the queue
   */
  public synchronized void clear() throws InterruptedException {
    while (!list.isEmpty()) {
      list.removeFirst();
    }

    full = false;
  }

  /**
   * This method removes all objects from the queue and returns them in an array
   * @return array of the objects removed from the list (Object[])
   */
  public synchronized Object[] removeAll() throws InterruptedException {

    Object[] objs = list.toArray();
    clear();
    return objs;
  }

  /**
   * This method waits a given amount of time will the queue is empty and then
   * returns indicating if the queue is empty or not
   * @param the number of milliseconds to wait (long)
   * @return true if the queue is empty otherwise false (boolean)
   */
  public synchronized boolean waitWhileEmpty(long msTimeout) throws InterruptedException {
    if (msTimeout == 0L) {
      waitWhileEmpty(); // use other method when msTimeout is zero
      return true;
    }

    // wait only for the specified amount of time, Dabby, 5/9/07
    long endTime = System.currentTimeMillis() + msTimeout;
    long msRemaining = msTimeout;

    while (!isEmpty() && (msRemaining > 0L)) {
      wait(msRemaining);
      // Dabby, 5/9/07
      msRemaining = endTime - System.currentTimeMillis();
    }

    return isEmpty();
  }

  /**
   * This method does not return until the queue is empty
   */
  public synchronized void waitUntilEmpty() throws InterruptedException {
    while (!isEmpty()) {
      wait();
    }
  }

  /**
   * This method waits will the queue is empty
   */
  public synchronized void waitWhileEmpty() throws InterruptedException {
    while (isEmpty()) {
      wait();
    }
  }

  /**
   * This method does not return until the queue is full
   */
  public synchronized void waitUntilFull() throws InterruptedException {
    if (fixed == false) // The queue will never be full if it has unlimited
      return;           // capacity - stops it from waiting infinitely

    while (!isFull()) {
      wait();
    }
  }

  /**
   * This method waits will the queue is full
   */
  public synchronized void waitWhileFull() throws InterruptedException {
    while (isFull()) {
      wait();
    }
  }

  /**
   * This method returns the list
   */
  protected synchronized LinkedList getList() { return list; }
}