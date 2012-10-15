package test.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import tiled.util.NumberedSet;

public class Test_NumberedSet {
	
    @Test
    public void pre_data () {
       NumberedSet<Object> s = new NumberedSet<Object>();
       Iterator<Object> it; 
       
       // pre-data values
       assertTrue( "", s.size() == 0 );
       assertTrue( "", s.getLastId() == -1 );
       assertTrue( "", s.getIdOf(null) == -1 );
       assertTrue( "", s.getIdOf(0) == -1 );
       assertFalse( "", s.contains(null) );
       assertFalse( "", s.contains(0) );
       assertFalse( "", s.containsId(0) );
       assertFalse( "", s.equals(null) );
       assertFalse( "", s.equals(0) );
       assertTrue( "", s.equals( s ) );
       assertTrue( "", s.hashCode() == s.hashCode() );

       // operations
       assertTrue( "", s.get(0) == null );
       assertTrue( "", s.get(-1) == null );
       assertTrue( "", s.get(999) == null );
       assertTrue( "", s.remove(0) == null );
       assertTrue( "", s.remove(-1) == null );
       assertTrue( "", s.remove(999) == null );
       
       // iterator
       it = s.iterator();
       assertFalse( "", it == null );
       assertFalse( "", it.hasNext() );
    }

    @SuppressWarnings("unchecked")
    private void testbed_array_1 ( int[] values, String descr ) {
        
        NumberedSet<Integer> lc, lcc;
        Iterator<Integer> iter;
        int sortVal[], h, m;
        String hstr;
        boolean emptyArr = values.length == 0;
        boolean moved;
        
        // method 1: PUT elements to their own values as keys 
        lc = new NumberedSet<Integer>();
        for ( int i : values ) {
            lc.put(i, i);
        }
        
        // test list state
        hstr = "testbed_array PUT ("+descr+")";
        lcc = (NumberedSet<Integer>)lc.clone();
        assertTrue( "list size failure on "+hstr, lc.size() == values.length );
        assertTrue( "\"isEmpty()\" failure on "+hstr, lc.isEmpty() == emptyArr );
        iter = lc.iterator();
        assertTrue( "iterator falsely reports no element after "+hstr, iter.hasNext() != emptyArr );

        // sort input array (bubble-sort)
        sortVal = values.clone();
        moved = true;
        while ( moved ) {
            moved = false;
            for ( int i=0; i<values.length-1; i++) {
                h = sortVal[i];
                m = sortVal[i+1];
                if ( h > m ) {
                    moved = true;
                    sortVal[i] = m;
                    sortVal[i+1] = h;
                }
            }
        }
        
        // test for list values (iterator)
        for ( int i = 0; iter.hasNext(); i++ ) {
            if ( !iter.next().equals( sortVal[i] ) ) {
                assertTrue( "value mismatch failure Iterator in "+hstr, false );
            }
        }

        // test if "last-Id" returns correctly
        h = sortVal.length == 0 ? -1 : sortVal[sortVal.length-1];
        assertTrue( "false result for last-Id in "+hstr, lcc.getLastId() == h );
        
        // test "last-Id" after removal of last element
        if ( values.length > 0 ) {
            lcc.remove(h);
            h = sortVal.length == 1 ? -1 : sortVal[sortVal.length-2];
            assertTrue( "false result for last-Id after remove-last in "+hstr, lcc.getLastId() == h );
        }
        
        // test for list values (get)
        for ( int i : values ) {
            Integer v = lc.get(i);
            
            // test value match
            if ( !v.equals( i )  ) {
                assertTrue( "value mismatch failure Get in "+hstr, false );
            }
            // test value contained relation
            if ( !lc.contains(v)  ) {
                assertTrue( "value falsely reported uncontained in "+hstr, false );
            }
            // test key contained relation
            if ( !lc.containsId(v)  ) {
                assertTrue( "key-as-value falsely reported uncontained in "+hstr, false );
            }
            // test key-searched relation
            if ( lc.getIdOf(v) != v ) {
                assertTrue( "key-search results false value in "+hstr, false );
            }
            
        }
    }
    
    @SuppressWarnings("unchecked")
    private void testbed_array_2 ( int[] values, String descr ) {
        NumberedSet<Integer> lc, lcc;
        Iterator<Integer> iter;
        int h;
        String hstr;
        boolean emptyArr = values.length == 0;
        
        // method 2: ADD elements to their own values as keys 
        lc = new NumberedSet<Integer>();
        for ( int i : values ) {
            lc.add(i);
        }
        
        // test list state
        hstr = "testbed_array ADD ("+descr+")";
        lcc = (NumberedSet<Integer>)lc.clone();
        assertTrue( "list size failure on "+hstr, lc.size() == values.length );
        assertTrue( "\"isEmpty()\" failure on "+hstr, lc.isEmpty() == emptyArr );
        iter = lc.iterator();
        assertTrue( "iterator falsely reports no element after "+hstr, iter.hasNext() != emptyArr );
    
        // test for list values (iterator)
        for ( int i = 0; iter.hasNext(); i++ ) {
            if ( !iter.next().equals( values[i] ) ) {
                assertTrue( "value mismatch failure Iterator in "+hstr, false );
            }
        }
    
        // test if "last-Id" returns correctly
        h = values.length == 0 ? -1 : values.length-1;
        assertTrue( "false result for last-Id in "+hstr, lcc.getLastId() == h );
        
        // test "last-Id" after removal of last element
        if ( values.length > 0 ) {
            lcc.remove(h);
            h = values.length == 1 ? -1 : values.length-2;
            assertTrue( "false result for last-Id after remove-last in "+hstr, lcc.getLastId() == h );
        }
        
        // test for list values (get)
        for ( int i=0; i<values.length; i++ ) {
            Integer v = lc.get(i);
            
            // test value match
            if ( !v.equals( values[i] )  ) {
                assertTrue( "value mismatch failure Get in "+hstr, false );
            }
            // test value contained relation
            if ( !lc.contains(v)  ) {
                assertTrue( "value falsely reported uncontained in "+hstr, false );
            }
            // test key contained relation
            if ( !lc.containsId(i)  ) {
                assertTrue( "key-as-value falsely reported uncontained in "+hstr, false );
            }
            // test key-searched relation
            if ( lc.getIdOf(v) != i ) {
                assertTrue( "key-search results false value in "+hstr, false );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void testbed_array_3 ( int[] values, String descr ) {
        NumberedSet<Integer> lc, lcc;
        Iterator<Integer> iter;
        int h;
        String hstr;
        boolean emptyArr = values.length == 0;
        
        // method 2: ADD elements to their own values as keys 
        lc = new NumberedSet<Integer>();
        for ( int i : values ) {
            lc.ensureElement(i);
        }
        
        // test list state
        hstr = "testbed_array ENSURE ("+descr+")";
        lcc = (NumberedSet<Integer>)lc.clone();
        assertTrue( "list size failure on "+hstr, lc.size() == values.length );
        assertTrue( "\"isEmpty()\" failure on "+hstr, lc.isEmpty() == emptyArr );
        iter = lc.iterator();
        assertTrue( "iterator falsely reports no element after "+hstr, iter.hasNext() != emptyArr );
    
        // test for list values (iterator)
        for ( int i = 0; iter.hasNext(); i++ ) {
            if ( !iter.next().equals( values[i] ) ) {
                assertTrue( "value mismatch failure Iterator in "+hstr, false );
            }
        }
    
        // test if "last-Id" returns correctly
        h = values.length == 0 ? -1 : values.length-1;
        assertTrue( "false result for last-Id in "+hstr, lcc.getLastId() == h );
        
        // test "last-Id" after removal of last element
        if ( values.length > 0 ) {
            lcc.remove(h);
            h = values.length == 1 ? -1 : values.length-2;
            assertTrue( "false result for last-Id after remove-last in "+hstr, lcc.getLastId() == h );
        }
        
        // test for list values (get)
        for ( int i=0; i<values.length; i++ ) {
            Integer v = lc.get(i);
            
            // test value match
            if ( !v.equals( values[i] )  ) {
                assertTrue( "value mismatch failure Get in "+hstr, false );
            }
            // test value contained relation
            if ( !lc.contains(v)  ) {
                assertTrue( "value falsely reported uncontained in "+hstr, false );
            }
            // test key contained relation
            if ( !lc.containsId(i)  ) {
                assertTrue( "key-as-value falsely reported uncontained in "+hstr, false );
            }
            // test key-searched relation
            if ( lc.getIdOf(v) != i ) {
                assertTrue( "key-search results false value in "+hstr, false );
            }
        }
    }

    @Test
    public void test_values_1 () {
        int[] ia;
        
        ia = new int[] {};
        testbed_array_1(ia, "void array" );
        testbed_array_2(ia, "void array" );
        testbed_array_3(ia, "void array" );

        ia = new int[] {0};
        testbed_array_1(ia, "one zero array" );
        testbed_array_2(ia, "one zero array" );
        testbed_array_3(ia, "one zero array" );

        ia = new int[] {0,1,2,3};
        testbed_array_1(ia, "four consequtive array" );
        testbed_array_2(ia, "four consequtive array" );
        testbed_array_3(ia, "four consequtive array" );

        ia = new int[] {5};
        testbed_array_1(ia, "one any array" );
        testbed_array_2(ia, "one any array" );
        testbed_array_3(ia, "one any array" );
        
        ia = new int[] {5,9,20,126,1049};
        testbed_array_1(ia, "five ascending array" );
        testbed_array_2(ia, "five ascending array" );
        testbed_array_3(ia, "five ascending array" );
        
        ia = new int[] {5,6,128,33,26,1050,512};
        testbed_array_1(ia, "seven scatter array" );
        testbed_array_2(ia, "seven scatter array" );
        testbed_array_3(ia, "seven scatter array" );
        
    }

    
    @Test
    public void test_remove () {
        int[] ia;
        
        ia = new int[] {};
        testbed_remove(ia, "void array" );
        
        ia = new int[] {0};
        testbed_remove(ia, "one zero array" );

        ia = new int[] {0,1,2,3};
        testbed_remove(ia, "four consequtive array" );

        ia = new int[] {5,9,20,126,1049};
        testbed_remove(ia, "five ascending array" );

        ia = new int[] {5,6,128,33,26,1050,512};
        testbed_remove(ia, "seven scatter array" );
    }

    @SuppressWarnings("unchecked")
    private void testbed_remove(int[] values, String descr) {
        NumberedSet<Integer> lc, lcc;
        Iterator<Integer> iter;
        int listSz, count;
        String hstr;
        
        // method 1: PUT elements to their own values as keys 
        lc = new NumberedSet<Integer>();
        for ( int i : values ) {
            lc.put(i, i);
        }
        
        hstr = "testbed_remove PUT (key) ("+descr+")";
        lcc = (NumberedSet<Integer>)lc.clone();
        listSz = lc.size();
        
        // test remove all by key, one-at-a-time
        count = 0;
        for ( int i : values ) {
            Integer j = lc.remove(i);
            count++;
            
            assertFalse( "remove(int) does not return a value on "+hstr, j == null );
            assertTrue( "remove(int) returns false value on "+hstr, j.equals(i) );
            assertTrue( "list size failure on "+hstr, lc.size() == listSz - count );
            iter = lc.iterator();
            assertTrue( "iterator falsely reports hasNext after "+hstr, iter.hasNext() != lc.isEmpty() );
            int c2 = 0;
            while ( iter.hasNext() ) {
                assertFalse( "iterator falsely contains removed element, "+hstr, j.equals(iter.next()) );
                c2++;
            }
            assertTrue( "iterator size failure at "+hstr, c2 == listSz - count );
        }

        hstr = "testbed_remove PUT (value) ("+descr+")";
        lc = (NumberedSet<Integer>)lcc.clone();
        listSz = lc.size();
        
        // test remove all by value, one-at-a-time
        count = 0;
        for ( int i : values ) {
            Integer j = new Integer(i);
            boolean rem = lc.remove( j );
            count++;
            
            assertTrue( "remove(E) was not reported successful on "+hstr, rem );
            assertFalse( "remove(E) was not removed on "+hstr, lc.contains(j) );
            assertTrue( "list size failure on "+hstr, lc.size() == listSz - count );
            iter = lc.iterator();
            assertTrue( "iterator falsely reports hasNext after "+hstr, iter.hasNext() != lc.isEmpty() );
            int c2 = 0;
            while ( iter.hasNext() ) {
                assertFalse( "iterator falsely contains removed element, "+hstr, j.equals(iter.next()) );
                c2++;
            }
            assertTrue( "iterator size failure at "+hstr, c2 == listSz - count );
        }

        // test clear command
        lc = (NumberedSet<Integer>)lcc.clone();
        lc.clear();
        assertTrue( "list ont empry after clear at "+hstr, lc.isEmpty() );
        
    }
}