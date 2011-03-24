package edu.cornell.mannlib.ingest.util;

/*
Copyright © 2003-2008 by the Cornell University and the Cornell
Research Foundation, Inc.  All Rights Reserved.

Permission to use, copy, modify and distribute any part of VITRO
("WORK") and its associated copyrights for educational, research and
non-profit purposes, without fee, and without a written agreement is
hereby granted, provided that the above copyright notice, this
paragraph and the following three paragraphs appear in all copies.

Those desiring to incorporate WORK into commercial products or use
WORK and its associated copyrights for commercial purposes should
contact the Cornell Center for Technology Enterprise and
Commercialization at 395 Pine Tree Road, Suite 310, Ithaca, NY 14850;
email:cctecconnect@cornell.edu; Tel: 607-254-4698; FAX: 607-254-5454
for a commercial license.

IN NO EVENT SHALL THE CORNELL RESEARCH FOUNDATION, INC. AND CORNELL
UNIVERSITY BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING
OUT OF THE USE OF WORK AND ITS ASSOCIATED COPYRIGHTS, EVEN IF THE
CORNELL RESEARCH FOUNDATION, INC. AND CORNELL UNIVERSITY MAY HAVE BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  */

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class StringUtils {

    /*
        From an email from Chas Emerick to Michael Gilleland, 22 October 2003:
        Mr. Gilleland,

        As you may know, the Apache Jakarta Commons project had appropriated
        your sample implementation of the Levenshtein Distance algorithm for
        its commons-lang Java library.  While attempting to use it with two
        very large strings, I encountered an OutOfMemoryError, due to the fact
        that a matrix is created with the dimensions of the two strings'
        lengths.  I know you created the implementation to go with your
        (excellent) illustration of the algorithm, so this matrix approach
        translates that illustration and tutorial perfectly.

        However, as I said, the matrix approach doesn't lend itself to getting
        the edit distance of two large strings.  For this purpose, I modified
        your implementation to use two single-dimensional arrays; this is
        clearly more memory-friendly (although it probably results in some very
        slight performance degradation when comparing smaller strings).

        I've submitted the modification to the maintainers of the commons-lang
        project, and I've appended the relevant method below.

        Thanks!

        Chas Emerick
     */
        public static int getLevenshteinDistance (String s, String t) {
          if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
          }

          /*
            The difference between this impl. and the previous is that, rather
             than creating and retaining a matrix of size s.length()+1 by t.length()+1,
             we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
             is the 'current working' distance array that maintains the newest distance cost
             counts as we iterate through the characters of String s.  Each time we increment
             the index of String t we are comparing, d is copied to p, the second int[].  Doing so
             allows us to retain the previous cost counts as required by the algorithm (taking
             the minimum of the cost count to the left, up one, and diagonally up and to the left
             of the current cost count being calculated).  (Note that the arrays aren't really
             copied anymore, just switched...this is clearly much better than cloning an array
             or doing a System.arraycopy() each time  through the outer loop.)

             Effectively, the difference between the two implementations is this one does not
             cause an out of memory condition when calculating the LD over two very large strings.
          */

          int n = s.length(); // length of s
          int m = t.length(); // length of t

          if (n == 0) {
            return m;
          } else if (m == 0) {
            return n;
          }

          int p[] = new int[n+1]; //'previous' cost array, horizontally
          int d[] = new int[n+1]; // cost array, horizontally
          int _d[]; //placeholder to assist in swapping p and d

          // indexes into strings s and t
          int i; // iterates through s
          int j; // iterates through t

          char t_j; // jth character of t

          int cost; // cost

          for (i = 0; i<=n; i++) {
             p[i] = i;
          }

          for (j = 1; j<=m; j++) {
             t_j = t.charAt(j-1);
             d[0] = j;

             for (i=1; i<=n; i++) {
                cost = s.charAt(i-1)==t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);
             }

             // copy current distance counts to 'previous row' distance counts
             _d = p;
             p = d;
             d = _d;
          }

          // our last action in the above loop was to switch d and p, so p now
          // actually has the most recent cost counts
          return p[n];
        }

        public static boolean similar(String a, String b){
            if( a == null || b == null )
                throw new IllegalArgumentException();

            int aveLen = (a.length() + b.length()) / 2;
            int cutoff =  (31 - Integer.numberOfLeadingZeros(aveLen)) >> 1; //like log2(x)/2
            return getLevenshteinDistance(a,b) < cutoff;
        }

        /**
         * Attempts to turn a string into a DOM4j Document.
         */
        public Document parseWithSax(String in)throws DocumentException{
            SAXReader xmlReader = new SAXReader();
            return xmlReader.read( in );
        }
}
