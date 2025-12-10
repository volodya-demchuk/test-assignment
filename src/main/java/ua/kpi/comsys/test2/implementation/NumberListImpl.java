/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * @author Demchuk Volodymyr, IS-32
 * 3 variant
 * C3 = 0 (Linear Bidirectional)
 * C5 = 3 (Decimal) -> Additional: Hexadecimal
 * C7 = 3 (Integer Part of Division of Two Numbers)
 */

package ua.kpi.comsys.test2.implementation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import ua.kpi.comsys.test2.NumberList;

public class NumberListImpl implements NumberList {

    private static class Node {
        Byte value;
        Node next;
        Node prev;

        Node(Byte value, Node prev, Node next) {
            this.value = value;
            this.prev = prev;
            this.next = next;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    /**
     * Default constructor. Returns empty <tt>NumberListImpl</tt>
     */
    public NumberListImpl() {
        this.size = 0;
    }

    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * from file, defined in string format.
     *
     * @param file - file where number is stored.
     */
    public NumberListImpl(File file) {
        this();
        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextLine()) {
                parseAndAdd(scanner.nextLine().trim());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * in string notation.
     *
     * @param value - number in string notation.
     */
    public NumberListImpl(String value) {
        this();
        parseAndAdd(value);
    }

    private void parseAndAdd(String value) {
        for (char c : value.toCharArray()) {
            if (Character.isDigit(c)) {
                add((byte) Character.getNumericValue(c));
            } else if (c >= 'A' && c <= 'F') {
                add((byte) (c - 'A' + 10));
            } else if (c >= 'a' && c <= 'f') {
                add((byte) (c - 'a' + 10));
            }
        }
    }

    /**
     * Saves the number, stored in the list, into specified file
     * in <b>decimal</b> scale of notation.
     *
     * @param file - file where number has to be stored.
     */
    public void saveList(File file) {
        try (PrintWriter out = new PrintWriter(file)) {
            out.print(this.toDecimalString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns student's record book number.
     */
    public static int getRecordBookNumber() {
        return 3;
    }

    /**
     * Returns new <tt>NumberListImpl</tt> which represents the same number
     * in Hexadecimal scale of notation.
     *
     * @return <tt>NumberListImpl</tt> in Hex scale.
     */
    public NumberListImpl changeScale() {
        
        NumberListImpl result = new NumberListImpl();
        NumberListImpl currentNumber = this.cloneList(); 
        
        if (currentNumber.isEmpty() || (currentNumber.size() == 1 && currentNumber.get(0) == 0)) {
            result.add((byte) 0);
            return result;
        }

        while (!currentNumber.isZero()) {
            int remainder = currentNumber.divideBySmallInt(16);
            result.add(0, (byte) remainder); 
        }

        if (result.isEmpty()) result.add((byte) 0);
        return result;
    }

    /**
     * Returns new <tt>NumberListImpl</tt> which represents the result of
     * Integer Division (this / arg).
     *
     * @param arg - second argument (divisor)
     * @return result of division.
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        NumberListImpl dividend = this.cloneList();
        NumberListImpl divisor = ((NumberListImpl) arg).cloneList();
        
        if (divisor.isZero()) {
            throw new ArithmeticException("Division by zero");
        }
        
        if (compareLists(dividend, divisor) < 0) {
            NumberListImpl zero = new NumberListImpl();
            zero.add((byte)0);
            return zero;
        }

        NumberListImpl result = new NumberListImpl();
        NumberListImpl currentChunk = new NumberListImpl();

        for (Byte digit : dividend) {
            currentChunk.add(digit);
            
            while(currentChunk.size() > 1 && currentChunk.get(0) == 0) {
                currentChunk.remove(0);
            }
            if (currentChunk.size() == 1 && currentChunk.get(0) == 0) {
            }

            int count = 0;
            while (compareLists(currentChunk, divisor) >= 0) {
                currentChunk = subtractLists(currentChunk, divisor);
                count++;
            }
            
            if (!result.isEmpty() || count > 0) {
                result.add((byte) count);
            }
        }

        if (result.isEmpty()) {
            result.add((byte) 0);
        }
        
        return result;
    }

    private int divideBySmallInt(int divisor) {
        int remainder = 0;
        Node current = head;
        while (current != null) {
            int val = current.value + remainder * 10;
            current.value = (byte) (val / divisor);
            remainder = val % divisor;
            current = current.next;
        }
        while (size > 0 && head.value == 0) {
            remove(0);
        }
        return remainder;
    }

    private boolean isZero() {
        if (size == 0) return true;
        return size == 1 && head.value == 0;
    }

    private NumberListImpl cloneList() {
        NumberListImpl copy = new NumberListImpl();
        for (Byte b : this) {
            copy.add(b);
        }
        return copy;
    }

    private int compareLists(NumberListImpl a, NumberListImpl b) {
        while(a.size() > 1 && a.get(0) == 0) a.remove(0);
        while(b.size() > 1 && b.get(0) == 0) b.remove(0);

        if (a.size() > b.size()) return 1;
        if (a.size() < b.size()) return -1;
        
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) > b.get(i)) return 1;
            if (a.get(i) < b.get(i)) return -1;
        }
        return 0;
    }

    private NumberListImpl subtractLists(NumberListImpl a, NumberListImpl b) {
        NumberListImpl res = new NumberListImpl();
        int borrow = 0;
        int i = a.size() - 1;
        int j = b.size() - 1;

        LinkedList<Byte> stack = new LinkedList<>();
        
        while (i >= 0) {
            int valA = a.get(i);
            int valB = (j >= 0) ? b.get(j) : 0;
            int diff = valA - valB - borrow;
            
            if (diff < 0) {
                diff += 10;
                borrow = 1;
            } else {
                borrow = 0;
            }
            stack.addFirst((byte)diff);
            i--;
            j--;
        }
        
        for(Byte val : stack) res.add(val);
        
        while (res.size() > 1 && res.get(0) == 0) {
            res.remove(0);
        }
        return res;
    }

    /**
     * Returns string representation of number in <b>decimal</b> scale.
     */
    public String toDecimalString() {
        StringBuilder sb = new StringBuilder();
        for (Byte b : this) {
            sb.append(b);
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Byte b : this) {
            if (b >= 0 && b <= 9) sb.append(b);
            else sb.append(Integer.toHexString(b).toUpperCase());
        }
        return sb.length() == 0 ? "[]" : sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof List)) return false;
        List<?> that = (List<?>) o;
        if (this.size() != that.size()) return false;
        Iterator<Byte> it1 = this.iterator();
        Iterator<?> it2 = that.iterator();
        while (it1.hasNext()) {
            Object e1 = it1.next();
            Object e2 = it2.next();
            if (!Objects.equals(e1, e2)) return false;
        }
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public Iterator<Byte> iterator() {
        return new ListIteratorImpl(0);
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node x = head; x != null; x = x.next)
            result[i++] = x.value;
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(Byte e) {
        final Node l = tail;
        final Node newNode = new Node(e, l, null);
        tail = newNode;
        if (l == null)
            head = newNode;
        else
            l.next = newNode;
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) return false;
        for (Node x = head; x != null; x = x.next) {
            if (o.equals(x.value)) {
                unlink(x);
                return true;
            }
        }
        return false;
    }

    Byte unlink(Node x) {
        final Byte element = x.value;
        final Node next = x.next;
        final Node prev = x.prev;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.value = null;
        size--;
        return element;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e)) return false;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        boolean modified = false;
        for (Byte e : c) {
            if (add(e)) modified = true;
        }
        return modified;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        checkPositionIndex(index);
        boolean modified = false;
        for (Byte e : c) {
            add(index++, e);
            modified = true;
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object e : c) {
            while (contains(e)) {
                remove(e);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Iterator<Byte> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        for (Node x = head; x != null; ) {
            Node next = x.next;
            x.value = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        head = tail = null;
        size = 0;
    }

    @Override
    public Byte get(int index) {
        checkElementIndex(index);
        return node(index).value;
    }

    @Override
    public Byte set(int index, Byte element) {
        checkElementIndex(index);
        Node x = node(index);
        Byte oldVal = x.value;
        x.value = element;
        return oldVal;
    }

    @Override
    public void add(int index, Byte element) {
        checkPositionIndex(index);
        if (index == size)
            add(element);
        else
            linkBefore(element, node(index));
    }

    void linkBefore(Byte e, Node succ) {
        final Node pred = succ.prev;
        final Node newNode = new Node(e, pred, succ);
        succ.prev = newNode;
        if (pred == null)
            head = newNode;
        else
            pred.next = newNode;
        size++;
    }

    @Override
    public Byte remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

    @Override
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) return -1;
        for (Node x = head; x != null; x = x.next) {
            if (o.equals(x.value)) return index;
            index++;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int index = size - 1;
        if (o == null) return -1;
        for (Node x = tail; x != null; x = x.prev) {
            if (o.equals(x.value)) return index;
            index--;
        }
        return -1;
    }

    @Override
    public ListIterator<Byte> listIterator() {
        return new ListIteratorImpl(0);
    }

    @Override
    public ListIterator<Byte> listIterator(int index) {
        checkPositionIndex(index);
        return new ListIteratorImpl(index);
    }

    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not implemented for this assignment");
    }

    Node node(int index) {
        if (index < (size >> 1)) {
            Node x = head;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node x = tail;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    private void checkPositionIndex(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) return false;
        if (index1 == index2) return true;
        
        Byte val1 = get(index1);
        Byte val2 = get(index2);
        
        set(index1, val2);
        set(index2, val1);
        return true;
    }

    @Override
    public void sortAscending() {
        if (size <= 1) return;
        boolean swapped;
        do {
            swapped = false;
            Node current = head;
            while (current.next != null) {
                if (current.value > current.next.value) {
                    Byte temp = current.value;
                    current.value = current.next.value;
                    current.next.value = temp;
                    swapped = true;
                }
                current = current.next;
            }
        } while (swapped);
    }

    @Override
    public void sortDescending() {
        if (size <= 1) return;
        boolean swapped;
        do {
            swapped = false;
            Node current = head;
            while (current.next != null) {
                if (current.value < current.next.value) {
                    Byte temp = current.value;
                    current.value = current.next.value;
                    current.next.value = temp;
                    swapped = true;
                }
                current = current.next;
            }
        } while (swapped);
    }

    @Override
    public void shiftLeft() {
        if (size <= 1) return;
        Byte first = remove(0);
        add(first);
    }

    @Override
    public void shiftRight() {
        if (size <= 1) return;
        Byte last = remove(size - 1);
        add(0, last);
    }
    
    private class ListIteratorImpl implements ListIterator<Byte> {
        private Node lastReturned;
        private Node next;
        private int nextIndex;

        ListIteratorImpl(int index) {
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }

        public boolean hasNext() {
            return nextIndex < size;
        }

        public Byte next() {
            if (!hasNext()) throw new NoSuchElementException();
            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.value;
        }

        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        public Byte previous() {
            if (!hasPrevious()) throw new NoSuchElementException();
            lastReturned = next = (next == null) ? tail : next.prev;
            nextIndex--;
            return lastReturned.value;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            Node lastNext = lastReturned.next;
            unlink(lastReturned);
            if (next == lastReturned)
                next = lastNext;
            else
                nextIndex--;
            lastReturned = null;
        }

        public void set(Byte e) {
            if (lastReturned == null) throw new IllegalStateException();
            lastReturned.value = e;
        }

        public void add(Byte e) {
            lastReturned = null;
            if (next == null)
                NumberListImpl.this.add(e);
            else
                linkBefore(e, next);
            nextIndex++;
        }
    }
}
